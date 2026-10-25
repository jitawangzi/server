package cn.game.games.net.game.helper;

import java.io.InputStream;
import java.lang.reflect.Proxy;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.scripting.defaults.DefaultParameterHandler;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import cn.game.games.cache.base.DbEntity;
import cn.game.games.cache.entity.Player;
import cn.game.games.cache.entity.PlayerData;
import cn.game.games.core.BasePlayerModule;
import cn.game.games.core.clazz.ClassManager;
import cn.game.games.net.data.mapper.PlayerDataMapper;
import cn.game.games.net.game.constant.MapperConstant;
import cn.game.games.net.game.db.DbTask;
import cn.game.testsupport.SaveProbeModule;
import cn.game.util.JsonUtil;
import io.vertx.core.Future;
import io.vertx.core.Promise;

/** 用真实保存装配和 MyBatis 参数绑定验证提交快照，不连接数据库，也不把 JSON 往返当作落库证明。 */
public class PlayerSaveSnapshotTest {
    /** 从生产 XML 装载的映射配置，测试不维护另一份字段复制清单。 */
    private final Configuration mapping = loadMapping();

    /** 沿用生产类型注册顺序，仅准备 Player 静态模块扫描依赖的本地元数据，不启动服务。 */
    @BeforeAll
    static void prepareClassMetadata() { ClassManager.getInstance().init(); }

    /** A/B 交错及在线对象继续变化后，两条保存入口的全部 SQL 绑定值仍各自稳定。 */
    @Test
    void interleavedSavesKeepEveryMappedValue() throws SQLException {
        for (boolean insert : List.of(true, false)) {
            FixturePlayer player = player(1);
            DeferredWrites writes = new DeferredWrites();
            Map<String, Object> expectedA = expected(player, insert);
            Future<?> a = submit(player, insert, writes);
            fill(player.getData(), 2);
            player.module.values.add("B");
            Map<String, Object> expectedB = expected(player, insert);
            Future<?> b = submit(player, insert, writes);
            writes.complete(0);
            Assertions.assertTrue(a.succeeded());
            Assertions.assertFalse(b.isComplete());
            Assertions.assertEquals(expectedB, bind(writes.rows.get(1), insert), "A 完成后 B 立即晚读仍完整");
            fill(player.getData(), 3);
            player.module.values.clear();
            Assertions.assertNotSame(player.getData(), writes.rows.get(0));
            Assertions.assertNotSame(writes.rows.get(0), writes.rows.get(1));
            Assertions.assertEquals(expectedA, bind(writes.rows.get(0), insert));
            Assertions.assertEquals(expectedB, bind(writes.rows.get(1), insert));
            writes.complete(1);
            Assertions.assertTrue(b.succeeded());
            Assertions.assertEquals(expectedB, bind(writes.rows.get(1), insert), "完成不能清空已提交 JSON");
            if (insert) { Assertions.assertSame(player, b.result()); }
        }
    }

    /** DAO 的等待包装先失败或超时，底层保留原引用晚读也不得读到空 JSON 或后续在线字段。 */
    @Test
    void failedOrTimedOutDaoFutureDoesNotChangeLateParameters() throws SQLException {
        for (boolean insert : List.of(true, false)) {
            for (Throwable cause : List.of(new IllegalStateException("write failed"), new TimeoutException("wait expired"))) {
                FixturePlayer player = player(1);
                DeferredWrites writes = new DeferredWrites();
                Map<String, Object> original = expected(player, insert);
                Future<?> a = submit(player, insert, writes);
                fill(player.getData(), 2);
                player.module.values.add("new state");
                Map<String, Object> next = expected(player, insert);
                submit(player, insert, writes);
                writes.fail(0, cause);
                Assertions.assertSame(cause, a.cause());
                Assertions.assertEquals(original, bind(writes.rows.get(0), insert));
                Assertions.assertEquals(next, bind(writes.rows.get(1), insert));
            }
        }
    }

    /** 等待者超时不意味着底层结束；保留未完成 Future 后实际晚绑定参数。 */
    @Test
    void callerTimeoutLeavesPendingWriteSnapshotIntact() throws SQLException {
        FixturePlayer player = player(1);
        DeferredWrites writes = new DeferredWrites();
        Map<String, Object> original = expected(player, false);
        Future<?> result = submit(player, false, writes);
        Assertions.assertThrows(TimeoutException.class,
                () -> result.toCompletionStage().toCompletableFuture().get(1, TimeUnit.MILLISECONDS));
        Assertions.assertFalse(result.isComplete());
        fill(player.getData(), 2);
        player.module.values.add("late change");
        Assertions.assertEquals(original, bind(writes.rows.getFirst(), false));
        writes.complete(0);
        Assertions.assertTrue(result.succeeded());
    }

    /** 所有异步终态均不能回写在线 JSON，捕获成功时才同步释放旧承载字符串。 */
    @Test
    void completionNeverClearsLiveOrSubmittedJson() throws SQLException {
        for (boolean insert : List.of(true, false)) {
            for (int outcome = 0; outcome < 3; outcome++) {
                FixturePlayer player = player(1);
                DeferredWrites writes = new DeferredWrites();
                Map<String, Object> original = expected(player, insert);
                submit(player, insert, writes);
                Assertions.assertEquals("{}", player.getData().getModules());
                player.getData().setModules("later live state");
                if (outcome == 0) { writes.complete(0); }
                else { writes.fail(0, outcome == 1 ? new IllegalStateException("failed") : new TimeoutException("expired")); }
                Assertions.assertEquals("later live state", player.getData().getModules());
                Assertions.assertEquals(original, bind(writes.rows.getFirst(), insert));
            }
        }
    }

    /** 钩子的变更进入快照；完整带类型 JSON 保留所有模块且与后续嵌套集合变更隔离。 */
    @Test
    void beforeSaveAndTypedModuleGraphArePreserved() throws SQLException {
        for (boolean insert : List.of(true, false)) {
            HookData source = new HookData();
            fill(source, 1);
            FixturePlayer player = new FixturePlayer(source);
            SaveProbeModule second = new SaveProbeModule();
            second.values.clear();
            second.values.add("second module");
            player.getModules().put("second", second);
            DeferredWrites writes = new DeferredWrites();
            submit(player, insert, writes);
            Assertions.assertEquals(1, source.calls);
            Assertions.assertEquals("prepared", bind(writes.rows.getFirst(), insert).get("name"));
            String json = writes.rows.getFirst().getModules();
            player.module.values.add("changed");
            second.values.clear();
            Map<String, BasePlayerModule> restored = JsonUtil.parseObjectWithType(json);
            Assertions.assertEquals(2, restored.size());
            SaveProbeModule restoredFirst = Assertions.assertInstanceOf(SaveProbeModule.class, restored.get("first"));
            Assertions.assertNotSame(player.module, restoredFirst);
            Assertions.assertEquals(List.of("first module"), restoredFirst.values);
            Assertions.assertEquals(List.of("second module"), ((SaveProbeModule) restored.get("second")).values);
        }
    }

    /** 空字符串、null、布尔双向值必须保留，不能依赖默认值掩盖字段漏复制。 */
    @Test
    void nullableStringsAndBooleanValuesArePreserved() throws SQLException {
        for (boolean insert : List.of(true, false)) {
            FixturePlayer player = player(2);
            player.getData().setName(null);
            player.getData().setDeviceId("");
            player.getData().setIsGm(false);
            player.getData().setGender(true);
            Map<String, Object> original = expected(player, insert);
            DeferredWrites writes = new DeferredWrites();
            submit(player, insert, writes);
            fill(player.getData(), 3);
            Assertions.assertEquals(original, bind(writes.rows.getFirst(), insert));
        }
    }

    /** 钩子准备失败时不提交，也不提前清除在线 JSON。 */
    @Test
    void preparationFailureDoesNotSubmitOrClearSource() {
        HookData source = new HookData();
        source.failure = new IllegalStateException("prepare failed");
        source.setModules("original");
        FixturePlayer player = new FixturePlayer(source);
        DeferredWrites writes = new DeferredWrites();
        for (boolean insert : List.of(true, false)) {
            Assertions.assertSame(source.failure, Assertions.assertThrows(IllegalStateException.class,
                    () -> submit(player, insert, writes)));
        }
        Assertions.assertTrue(writes.rows.isEmpty());
        Assertions.assertEquals("original", source.getModules());
    }

    /** 保留玩家缺失、不活跃、独立表与空任务分支，不增加独立表写入或绕过原钩子。 */
    @Test
    void independentTableAndNoOpBranchesStayUnchanged() {
        DeferredWrites writes = new DeferredWrites();
        Future<List<Object>> missing = PlayerHelper.saveClientCache(9, id -> null,
                () -> { throw new AssertionError("missing player queried storage mode"); }, writes::update);
        Assertions.assertTrue(missing.succeeded());
        Assertions.assertNull(missing.result());
        FixturePlayer player = player(1);
        player.setActive(false);
        Assertions.assertTrue(PlayerHelper.saveClientCache(9, id -> player,
                () -> { throw new AssertionError("inactive player queried storage mode"); }, writes::update).succeeded());
        player.setActive(true);
        Assertions.assertTrue(PlayerHelper.saveClientCache(9, id -> player, () -> false, writes::update).succeeded());
        Assertions.assertTrue(writes.rows.isEmpty());
        HookData independent = new HookData();
        player.module.entities.add(independent);
        Future<List<Object>> result = PlayerHelper.saveClientCache(9, id -> player, () -> false, writes::update);
        Assertions.assertSame(independent, writes.rows.getFirst());
        Assertions.assertEquals(1, independent.calls);
        writes.complete(0);
        Assertions.assertEquals(List.of(1), result.result());
    }

    /** 用生产 Mapper 属性驱动断言；故意漏掉一个字段必须使同一覆盖校验失败。 */
    @Test
    void mapperCoverageRejectsMissingCopiedField() throws SQLException {
        FixturePlayer player = player(1);
        DeferredWrites writes = new DeferredWrites();
        Map<String, Object> original = expected(player, false);
        submit(player, false, writes);
        Assertions.assertEquals(original, bind(writes.rows.getFirst(), false));
        writes.rows.getFirst().setLevel(0);
        Assertions.assertThrows(AssertionError.class,
                () -> Assertions.assertEquals(original, bind(writes.rows.getFirst(), false)));
    }

    /** @return 不访问数据库的生产 XML 映射 */
    private static Configuration loadMapping() {
        Configuration configuration = new Configuration();
        String resource = "mapping/PlayerDataMapper.xml";
        try (InputStream input = PlayerSaveSnapshotTest.class.getClassLoader().getResourceAsStream(resource)) {
            Assertions.assertNotNull(input);
            new XMLMapperBuilder(input, configuration, resource, configuration.getSqlFragments()).parse();
            return configuration;
        } catch (java.io.IOException failure) {
            org.slf4j.LoggerFactory.getLogger(PlayerSaveSnapshotTest.class).error("Cannot read Mapper fixture", failure);
            throw new IllegalStateException(failure);
        }
    }

    /** @param insert 是否创建 @return 生产映射中的实际语句 */
    private MappedStatement statement(boolean insert) {
        return mapping.getMappedStatement(PlayerDataMapper.class.getName() + "." + (insert ? "insert" : "updateByPrimaryKey"));
    }

    /** @param source 在线对象 @param version 非默认哨兵版本，所有 SQL 字段独立赋值 */
    private void fill(PlayerData source, int version) {
        MetaObject bean = mapping.newMetaObject(source);
        int index = 0;
        var properties = new LinkedHashSet<String>();
        for (boolean insert : List.of(true, false)) {
            statement(insert).getBoundSql(source).getParameterMappings().forEach(parameter -> properties.add(parameter.getProperty()));
        }
        for (String property : properties) {
            Class<?> type = bean.getGetterType(property);
            Object value;
            if (type == String.class) { value = property + "-" + version; }
            else if (type == int.class) { value = 100 * version + ++index; }
            else if (type == long.class) { value = 1000L * version + ++index; }
            else if (type == boolean.class) { value = version % 2 == 1; }
            else { throw new AssertionError("新增 Mapper 类型需明确复制和测试策略: " + property + ":" + type); }
            bean.setValue(property, value);
        }
    }

    /** @param version 字段版本 @return 无业务初始化、使用真实模块集合的玩家 */
    private FixturePlayer player(int version) {
        PlayerData data = new PlayerData();
        fill(data, version);
        return new FixturePlayer(data);
    }

    /** @param player 当前状态 @param insert 操作类型 @return 提交前独立计算的全部绑定值 */
    private Map<String, Object> expected(FixturePlayer player, boolean insert) throws SQLException {
        Map<String, Object> values = bind(player.getData(), insert);
        values.put("modules", JsonUtil.toJsonStringWithType(player.getModules()));
        return values;
    }

    /** @param player 玩家 @param insert 操作类型 @param writes 可控提交器 @return 真实保存装配的结果 */
    private Future<?> submit(FixturePlayer player, boolean insert, DeferredWrites writes) {
        return insert ? PlayerHelper.savePlayerToDb(player, writes::insert)
                : PlayerHelper.saveClientCache(9, id -> { Assertions.assertEquals(9, id); return player; }, () -> true, writes::update);
    }

    /** @param row 原提交引用 @param insert 操作类型 @return 真实 MyBatis 参数处理器在晚读取时绑定的值 */
    private Map<String, Object> bind(PlayerData row, boolean insert) throws SQLException {
        MappedStatement statement = statement(insert);
        BoundSql sql = statement.getBoundSql(row);
        Map<String, Object> values = new LinkedHashMap<>();
        PreparedStatement target = (PreparedStatement) Proxy.newProxyInstance(PreparedStatement.class.getClassLoader(),
                new Class<?>[] {PreparedStatement.class}, (proxy, method, args) -> {
                    if (method.getName().startsWith("set") && args[0] instanceof Integer index) {
                        String property = sql.getParameterMappings().get(index - 1).getProperty();
                        values.put(property, method.getName().equals("setNull") ? null : args[1]);
                        return null;
                    }
                    throw new UnsupportedOperationException(method.getName());
                });
        new DefaultParameterHandler(statement, row, sql).setParameters(target);
        Assertions.assertEquals(sql.getParameterMappings().size(), values.size(), "全部映射参数都须实际绑定");
        return values;
    }

    /** 只保留原引用，故意不在提交时复制，以暴露延迟读取中的共享参数问题。 */
    private static final class DeferredWrites {
        /** 提交给 Mapper 的原始对象。 */
        private final List<PlayerData> rows = new ArrayList<>();
        /** 与各次提交对应的可控终态。 */
        private final List<Promise<Object>> results = new ArrayList<>();
        /** @param row 创建参数 @return 尚未完成的 DAO 结果 */
        Future<Object> insert(PlayerData row) {
            rows.add(row);
            Promise<Object> result = Promise.promise();
            results.add(result);
            return result.future();
        }
        /** @param tasks 原批量任务 @return 沿用批量结果形态 */
        Future<List<Object>> update(List<DbTask> tasks) {
            Assertions.assertEquals(1, tasks.size());
            Assertions.assertEquals(PlayerDataMapper.class, tasks.getFirst().getMapper());
            Assertions.assertEquals(MapperConstant.updateByPrimaryKey, tasks.getFirst().getMethod());
            return insert((PlayerData) tasks.getFirst().getArg()).map(value -> List.of(value));
        }
        /** @param index 完成指定提交，允许其他提交尚未读取参数 */
        void complete(int index) { results.get(index).complete(1); }
        /** @param index 指定提交 @param failure 等待包装的失败或超时原因 */
        void fail(int index, Throwable failure) { results.get(index).fail(failure); }
    }

    /** 省去完整业务模块初始化，仍使用 Player 自身的模块集合与活跃状态。 */
    private static final class FixturePlayer extends Player {
        /** 测试在线数据。 */
        private final PlayerData source;
        /** 可变模块内容。 */
        private final SaveProbeModule module = new SaveProbeModule();
        /** @param source 在线数据 */
        FixturePlayer(PlayerData source) { this.source = source; getModules().put("first", module); }
        @Override
        public PlayerData getData() { return source; }
        @Override
        public long getPlayerId() { return source.getPlayerId(); }
    }

    /** 让 beforeSave 的执行时机和失败可观察。 */
    private static final class HookData extends PlayerData {
        /** 调用次数。 */
        private int calls;
        /** 可选的准备阶段异常。 */
        private RuntimeException failure;
        @Override
        public void beforeSave() {
            calls++;
            if (failure != null) { throw failure; }
            setName("prepared");
        }
    }
}
