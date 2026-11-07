package cn.game.games.net.game.helper;

import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import javax.sql.DataSource;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import cn.game.core.base.ServerContext;
import cn.game.core.execute.SaveMailboxFixture;
import cn.game.core.net.client.NetClient;
import cn.game.core.net.process.IdVirtualThreadProcessor;
import cn.game.core.net.socket.controller.DispatcherImpl;
import cn.game.core.net.vertx.MsgConsumerVerticle;
import cn.game.core.net.vertx.VxHolder;
import cn.game.games.cache.entity.Player;
import cn.game.games.cache.entity.PlayerData;
import cn.game.games.core.clazz.ClassManager;
import cn.game.games.net.client.GameClient;
import cn.game.games.net.data.mapper.PlayerDataMapper;
import cn.game.games.net.game.handler.ServerHandler;
import cn.game.games.net.game.module.account.Account;
import cn.game.games.net.game.manager.PlayerManager;
import cn.game.games.net.game.module.recharge.PayItem;
import cn.game.games.net.game.module.player.CountingModule;
import cn.game.protocol.generated.enume.Asset;
import cn.game.protocol.generated.enume.ConditionTypeEnum;
import cn.game.protocol.generated.helper.ManagerHelper;
import cn.game.protocol.protobuf.ServerMsg.PaymentOrderShipRequest_7d000022;
import cn.game.protocol.protobuf.ServerMsg.PaymentOrderShipResponse_7d000023;
import cn.game.util.DateUtil;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.eventbus.EventBusOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.spi.cluster.zookeeper.ZookeeperClusterManager;

/**
 * 独立验收进程：沿正式跨服编解码、分派、充值奖励和保存链执行。
 * 只替换节点装配、玩家定位及同库工厂；故障门只暂停实际边界，由父进程强制终止。
 * 不模拟支付平台验单，也不向公共游戏地址注册消费者。
 */
public final class PaymentProcessNode {
    /** 本进程的观测日志。 */
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PaymentProcessNode.class);
    /** 本次场景的非秘密参数和独占文件目录。 */
    private final JsonObject spec;
    private final Path directory;
    /** 进程身份与故障门，一次启动不会沿用旧实例身份。 */
    private final String instance = UUID.randomUUID().toString();
    private final String role;
    private final String gate;
    /** 实际处理次数、业务执行次数与当前原对象。 */
    private final AtomicInteger received = new AtomicInteger(), business = new AtomicInteger();
    private volatile Player player;
    /** 每进程自己的数据源与会话工厂，不从旧进程继承缓存。 */
    private SqlSessionFactory factory;
    private PaymentDedupStore dedup;
    private PlayerSaveCoordinator saves;
    private IdVirtualThreadProcessor mailbox;
    private Vertx vertx;
    /** 实际集群成员视图，用于区分进程已退出与路由已收敛。 */
    private ZookeeperClusterManager cluster;

    /** @param args 场景目录、target/sender角色和故障门 */
    public static void main(String[] args) throws Exception {
        try { new PaymentProcessNode(Path.of(args[0]), args[1], args[2]).start(); }
        catch (Throwable failure) { log.error("独立业务节点启动失败", failure); System.exit(2); }
    }

    /** @param directory 父测试创建的目录 @param role 节点角色 @param gate 本进程唯一暂停点 */
    private PaymentProcessNode(Path directory, String role, String gate) throws Exception {
        this.directory = directory; this.role = role; this.gate = gate;
        spec = new JsonObject(Files.readString(directory.resolve("scenario.json")));
    }

    /** @return 私有连接配置；不写入报告或进程参数 */
    static Properties configuration() throws Exception {
        Properties properties = new Properties();
        try (InputStream input = Files.newInputStream(Path.of(System.getenv("S2_DB_CONFIG")))) { properties.load(input); }
        return properties;
    }

    /** @return 有界独立连接源 */
    static DriverManagerDataSource source() throws Exception {
        Properties properties = configuration();
        var source = new DriverManagerDataSource();
        source.setDriverClassName(properties.getProperty("driverClassName"));
        source.setUrl(properties.getProperty("dburl")); source.setUsername(properties.getProperty("dbuser"));
        source.setPassword(properties.getProperty("dbpass"));
        Properties limits = new Properties(); limits.setProperty("connectTimeout", "3000");
        limits.setProperty("socketTimeout", "20000"); limits.setProperty("autoReconnect", "false");
        source.setConnectionProperties(limits); return source;
    }

    /** @param source 独立源 @return 使用生产映射及事务工厂的真实会话工厂 */
    static SqlSessionFactory factory(DataSource source) throws Exception {
        Configuration mapping = new Configuration(new Environment("payment-process", new JdbcTransactionFactory(), source));
        try (InputStream xml = PaymentProcessNode.class.getResourceAsStream("/mapping/PlayerDataMapper.xml")) {
            new XMLMapperBuilder(xml, mapping, "mapping/PlayerDataMapper.xml", mapping.getSqlFragments()).parse();
        }
        return new SqlSessionFactoryBuilder().build(mapping);
    }

    /** @param factory 独立工厂 @param spec 场景身份 @return 新会话冷读的归属匹配行 */
    static PlayerData read(SqlSessionFactory factory, JsonObject spec) {
        try (var session = factory.openSession()) {
            PlayerData data = session.getMapper(PlayerDataMapper.class).selectByPrimaryKey(spec.getLong("playerId"));
            if (data == null || !spec.getString("runId").equals(data.getAccountId())
                    || !spec.getString("runId").equals(data.getDeviceId())) { throw new IllegalStateException("FIXTURE_IDENTITY_MISMATCH"); }
            return data;
        }
    }

    /** 装配正式依赖并把控制面绑定到随机回环端口；就绪记录在订阅完成之后写入。 */
    private void start() throws Exception {
        ServerContext.getInstance().setServerId(spec.getString(role));
        JsonObject zk = new JsonObject(configuration().getProperty("zk"));
        zk.put("rootPath", spec.getString("zkRoot"));
        cluster = new ZookeeperClusterManager(zk);
        vertx = Vertx.builder().with(new VertxOptions().setEventLoopPoolSize(2)
                .setEventBusOptions(new EventBusOptions().setHost("127.0.0.1").setPort(0)
                        .setClusterNodeMetadata(new JsonObject().put("instance", instance).put("role", role))))
                .withClusterManager(cluster).buildClustered()
                .toCompletionStage().toCompletableFuture().get(30, TimeUnit.SECONDS);
        VxHolder.vertx = vertx;
        vertx.eventBus().registerCodec(VxHolder.universalMessageCodec);
        if (role.equals("target")) { target(); }
        var server = vertx.createHttpServer().requestHandler(request -> {
            if (request.path().equals("/state")) { request.response().end(state().encode()); return; }
            if (role.equals("target") && request.path().equals("/observe")) {
                mailbox.processAsync(player.getPlayerId(), () -> request.response().end(observe().encode()), false)
                        .onFailure(failure -> { log.error("节点观测失败", failure); request.response().setStatusCode(500).end(); });
                return;
            }
            if (!role.equals("sender") || !request.path().equals("/ship")) { request.response().setStatusCode(404).end(); return; }
            long timeout = Long.parseLong(request.getParam("timeout"));
            // 此专用发送进程只接受父测试顺序命令；实际超时由生产调用的 EventBus 等待触发。
            VxHolder.universalOptions.setSendTimeout(timeout);
            var message = PaymentOrderShipRequest_7d000022.newBuilder().setPlayerId(spec.getLong("playerId"))
                    .setUid(spec.getLong("orderId")).setSdkOrderId(spec.getString("sdkOrderId")).build();
            VxHolder.<PaymentOrderShipResponse_7d000023>requestRemoteServer(spec.getString("target"), message)
                    .onComplete(result -> {
                        JsonObject response = state().put("success", result.succeeded() && result.result().getSuccess());
                        if (result.failed()) {
                            log.warn("测试发货传输未确认，保留相同订单供核对", result.cause());
                            response.put("failure", result.cause().getClass().getSimpleName());
                            if (result.cause() instanceof io.vertx.core.eventbus.ReplyException reply) { response.put("replyFailure", reply.failureType().name()); }
                        }
                        request.response().end(response.encode());
                    });
        }).listen(0, "127.0.0.1").toCompletionStage().toCompletableFuture().get(10, TimeUnit.SECONDS);
        publish(role + "-ready.json", state().put("port", server.actualPort()));
    }

    /** 装配正式玩家构造、配置和完整模块集；不替换实际奖励实现。 */
    private void target() throws Exception {
        ClassManager.getInstance().init(); ManagerHelper.init();
        factory = factory(gatedSource(source())); dedup = new PaymentDedupStore(() -> factory, spec.getString("suffix"));
        player = new Player(read(factory, spec)); player.setAccount(new Account());
        player.setGameClient(new GameClient(null)); player.setOnline(false);
        DispatcherImpl dispatchService = new DispatcherImpl();
        mailbox = new IdVirtualThreadProcessor(SaveMailboxFixture.create()) { { this.dispatcher = dispatchService; } };
        var writer = new PlayerSnapshotWriter(() -> factory, Executors.newVirtualThreadPerTaskExecutor());
        saves = new PlayerSaveCoordinator(4, 15_000, writer::write,
                (action, delay) -> { long timer = vertx.setTimer(delay, ignored -> action.run()); return () -> vertx.cancelTimer(timer); },
                DateUtil::currentTimeMillis);
        saves.register(player.getPlayerId(), player, () -> {});
        var processorField = ServerContext.class.getDeclaredField("processor"); processorField.setAccessible(true);
        processorField.set(ServerContext.getInstance(), mailbox);
        PlayerManager.getInstance().initAdd(player);
        restoreModules();
        NodeHandler handler = new NodeHandler(dispatchService); handler.init();
        // 注册完成才发布就绪；沿用正式 MsgConsumerVerticle 的解码、ServerClient 与调度。
        var consumer = new MsgConsumerVerticle(spec.getString("target"), null, mailbox);
        vertx.eventBus().consumer(spec.getString("target"), message -> {
            received.incrementAndGet(); consumer.handleMessage(message);
        }).completion().toCompletionStage().toCompletableFuture().get(10, TimeUnit.SECONDS);
    }

    /** @return 节点身份及观测计数；资产只在故障门或邮箱内生成最终证据 */
    private JsonObject state() {
        return new JsonObject().put("pid", ProcessHandle.current().pid()).put("instance", instance)
                .put("clusterNodeId", cluster.getNodeId()).put("members", cluster.getNodes())
                .put("role", role).put("node", spec.getString(role)).put("received", received.get()).put("business", business.get());
    }

    /** 使用正式查询清单及模块恢复钩子；只接受本轮新玩家的空独立表结果。 */
    private void restoreModules() throws Exception {
        var results = new java.util.ArrayList<Object>();
        for (var task : PlayerHelper.initDbTasks(player)) {
            if (!(task.getArg() instanceof Number id) || id.longValue() != player.getPlayerId()) {
                throw new IllegalStateException("RESTORE_QUERY_OUTSIDE_FIXTURE");
            }
            if (!factory.getConfiguration().hasMapper(task.getMapper())) {
                String resource = "mapping/" + task.getMapper().getSimpleName() + ".xml";
                try (InputStream xml = getClass().getResourceAsStream("/" + resource)) {
                    if (xml == null) { throw new IllegalStateException("RESTORE_MAPPING_MISSING:" + resource); }
                    new XMLMapperBuilder(xml, factory.getConfiguration(), resource, factory.getConfiguration().getSqlFragments()).parse();
                }
            }
            try (var session = factory.openSession()) {
                var methods = java.util.Arrays.stream(task.getMapper().getMethods())
                        .filter(m -> m.getName().equals(task.getMethod()) && m.getParameterCount() == 1).toList();
                if (methods.size() != 1) { throw new IllegalStateException("RESTORE_MAPPING_AMBIGUOUS"); }
                Object value = methods.getFirst().invoke(session.getMapper(task.getMapper()), task.getArg());
                if (value != null && (!(value instanceof List<?> rows) || !rows.isEmpty())) {
                    throw new IllegalStateException("FIXTURE_HAS_UNEXPECTED_RELATED_DATA:" + task.getMapper().getSimpleName() + "." + task.getMethod());
                }
                results.add(value);
            }
        }
        PlayerHelper.initPlayerModuleFromDb(player, results, true, (value, action) -> mailbox.process(value.getPlayerId(), () -> {
            try (var scope = new PlayerWriteGuard.Scope(saves.beginMutation(value.getPlayerId(), value))) { return action.get(); }
        }, null)).toCompletionStage().toCompletableFuture().get(15, TimeUnit.SECONDS);
    }

    /** @return 当前新进程实际恢复对象的字段；只在玩家邮箱读取 */
    private JsonObject observe() {
        PayItem item = player.getPlayerModule().getPayItems(spec.getLong("orderId"));
        return state().put("diamonds", player.getCurrencyModule().getCount(100001))
                .put("recharge", player.getModule(CountingModule.class).getCumulativeCount(ConditionTypeEnum.AccumulatedRecharge))
                .put("rechargeCount", player.getModule(CountingModule.class).getCumulativeCount(ConditionTypeEnum.RechargeCnt))
                .put("vipLevel", player.getPlayerModule().getExpLevelMap().getValue(Asset.VIPExp.ID))
                .put("vipExp", player.getCurrencyModule().getCount(Asset.VIPExp.ID))
                .put("finish", item.isFinish()).put("proof", item.hasCommittedCompletionProof(player.getPlayerId(), spec.getString("sdkOrderId")));
    }

    /** @param name 固定文件名 @param value 完整观测；发布前不会暴露半写JSON */
    private void publish(String name, JsonObject value) throws Exception {
        Path temporary = directory.resolve(name + "." + instance + ".tmp");
        Files.writeString(temporary, value.encode());
        Files.move(temporary, directory.resolve(name), java.nio.file.StandardCopyOption.ATOMIC_MOVE, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
    }

    /** @param point 已到达的实际边界；等待父测试释放或直接终止本进程 */
    private void pause(String point) {
        if (!gate.equals(point)) { return; }
        try {
            publish("gate.json", observe().put("point", point));
            long until = System.nanoTime() + TimeUnit.SECONDS.toNanos(60);
            while (!Files.exists(directory.resolve("release"))) {
                if (System.nanoTime() >= until) { throw new IllegalStateException("FAULT_GATE_DEADLINE"); }
                Thread.sleep(20);
            }
        } catch (Exception failure) { log.error("故障门失败", failure); throw new IllegalStateException(failure); }
    }

    /** @param source 原源 @return 仅在聚合事务 commit 前暂停的代理，不更改 SQL 或事务结果 */
    private DataSource gatedSource(DataSource source) {
        return (DataSource) Proxy.newProxyInstance(PaymentDatabaseBoundary.Source.class.getClassLoader(),
                new Class<?>[] {PaymentDatabaseBoundary.Source.class}, (proxy, method, args) -> {
            try {
                if (method.getName().equals("acquisitionMillis")) { return 60000; }
                if (method.getName().equals("mysqlDriver")) { return true; }
                if (method.getName().equals("discard")) {
                    Connection connection = (Connection) args[0];
                    connection.unwrap(com.mysql.cj.jdbc.JdbcConnection.class).abortInternal(); connection.close(); return null;
                }
                Object result = method.invoke(source, args);
                if (!(result instanceof Connection connection)) { return result; }
                boolean[] aggregate = {false};
                return Proxy.newProxyInstance(Connection.class.getClassLoader(), new Class<?>[] {Connection.class}, (p, m, a) -> {
                    if (m.getName().equals("prepareStatement") && ((String) a[0]).toLowerCase(java.util.Locale.ROOT).contains("update t_player_data")) { aggregate[0] = true; }
                    if (m.getName().equals("commit") && aggregate[0]) { pause("before_commit"); }
                    try { return m.invoke(connection, a); }
                    catch (InvocationTargetException failure) { log.warn("数据库故障由原连接返回", failure.getCause()); throw failure.getCause(); }
                });
            } catch (InvocationTargetException failure) { log.warn("数据源失败", failure.getCause()); throw failure.getCause(); }
        });
    }

    /** 只注入本轮对象与存储依赖；正式 ship、去重、奖励和保存完成判断全部保留。 */
    private final class NodeHandler extends ServerHandler {
        /** @param dispatcher 正式协议分派器 */
        NodeHandler(DispatcherImpl dispatcher) { this.dispatcher = dispatcher; }
        @Override protected Player paymentPlayer(long id) { return id == player.getPlayerId() ? player : null; }
        @Override protected Future<Void> submitPayment(long id, Runnable action) { return mailbox.processAsync(id, action, false); }
        @Override protected boolean singlePaymentStorage() { return true; }
        @Override protected PaymentDedupStore paymentDedupStore() { return dedup; }
        @Override protected void freezePayment(Player value, Throwable cause) { saves.freezePayment(value.getPlayerId(), value, cause); }
        @Override protected void requireDurablePaymentReady(Player value) { saves.requirePaymentReady(value.getPlayerId(), value); }
        @Override protected PlayerWriteGuard.Scope beginPaymentMutation(Player value) { return new PlayerWriteGuard.Scope(saves.beginMutation(value.getPlayerId(), value)); }
        @Override protected boolean processPayment(Player value, PayItem item, boolean online) {
            business.incrementAndGet(); pause("before_reward"); return super.processPayment(value, item, online);
        }
        @Override protected Future<List<Object>> savePayment(Player value) {
            pause("before_save");
            return PlayerHelper.saveClientCache(value.getPlayerId(), ignored -> player, () -> true, saves,
                    tasks -> { throw new AssertionError("独立表不属于本场景"); });
        }
        @Override protected void sendResult(NetClient client, boolean success) {
            if (success) { pause("after_commit"); }
            super.sendResult(client, success);
        }
        @Override protected boolean removePaymentPlayer(Player value) {
            // 保留已确认对象供同节点重试；进程退出后下一节点必须冷读，不能复用该对象。
            return false;
        }
    }
}
