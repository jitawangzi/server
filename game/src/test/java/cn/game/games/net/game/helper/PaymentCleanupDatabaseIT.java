package cn.game.games.net.game.helper;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import cn.game.games.diagnostics.PaymentArchiveCli;
import cn.game.games.diagnostics.PaymentCleanupCli;
import cn.game.games.diagnostics.PaymentEvidenceCli;
import cn.game.games.net.game.module.recharge.PayItem;
import cn.game.games.net.game.module.recharge.PayType;
import cn.game.util.JsonUtil;
import cn.game.util.CborSnappyUtil;
import cn.game.games.core.BasePlayerModule;
import cn.game.games.net.game.module.player.PlayerModule;

/**
 * 当前两库真实冷读、真实Game聚合与完成事务、真实子JVM CLI；每个Case只占有一行Game、一行Login、两张随机测试表。
 * 表名接缝仅存在于测试类，生产命令不支持覆盖；每次预检前后冷查全字段及归档字节相等。
 */
@Isolated
class PaymentCleanupDatabaseIT {
    /** 清理证据在私有日志中按本轮随机后缀一一核对，不输出业务正文。 */
    private static final org.slf4j.Logger log=org.slf4j.LoggerFactory.getLogger(PaymentCleanupDatabaseIT.class);
    /** 只准备测试写入所需模块类型，预检本身不加载 Player。 */
    @BeforeAll static void metadata() { PlayerSaveDatabaseIT.metadata(); }

    /** JDBC/Spring真实事务产生完成凭证，CLI冷读局部一致仍被独立终态和旧写入者证明阻断。 */
    @Test void completedTransactionAndStandaloneCliRemainReadOnlyAndBlocked() throws Exception {
        for(boolean spring:List.of(false,true)) {
            try(var f=new Case(spring)) {
                f.open(); f.assertReadOnly(true);
                Path stdout=f.base.resolve("stdout.json"),stderr=f.base.resolve("stderr.log");
                var command=new ArrayList<String>(List.of(Path.of(System.getProperty("java.home"),"bin","java").toString(),
                        "-Dlog4j.configurationFile="+Path.of("target/classes/payment-evidence-log4j2.xml").toAbsolutePath().toUri(),
                        "-cp",System.getProperty("surefire.test.class.path",System.getProperty("java.class.path")),
                        CliBridge.class.getName()));
                command.addAll(List.of(f.args())); command.add(f.suffix);
                List<List<String>> before=f.snapshot(); byte[] archive=Files.readAllBytes(f.artifact());
                Process child=new ProcessBuilder(command).redirectOutput(stdout.toFile()).redirectError(stderr.toFile()).start();
                try {
                    Assertions.assertTrue(child.waitFor(20,java.util.concurrent.TimeUnit.SECONDS)); Assertions.assertEquals(2,child.exitValue());
                    String text=Files.readString(stdout); var result=new com.fasterxml.jackson.databind.ObjectMapper().readTree(text);
                    Assertions.assertTrue(result.path("localEvidenceReady").booleanValue()); Assertions.assertFalse(result.path("canDelete").booleanValue());
                    Assertions.assertEquals(2,result.path("externalBlockers").size());
                    for(String secret:List.of(f.db.runId,f.root.toString(),f.key.toString(),"payItem","jdbc:")) { Assertions.assertFalse(text.contains(secret)); }
                } finally { if(child.isAlive()) { child.destroyForcibly(); Assertions.assertTrue(child.waitFor(5,java.util.concurrent.TimeUnit.SECONDS)); } }
                Assertions.assertEquals(before,f.snapshot()); Assertions.assertArrayEquals(archive,Files.readAllBytes(f.artifact()));
                // 固定生产表名入口也真实运行，但绝不使用测试表凭证伪造生产就绪或创建生产表。
                var production=PaymentCleanupCli.run(f.args()); Assertions.assertEquals(false,production.get("localEvidenceReady"));
                Assertions.assertEquals(before,f.snapshot()); Assertions.assertArrayEquals(archive,Files.readAllBytes(f.artifact()));
                command.set(command.indexOf(CliBridge.class.getName()),PaymentCleanupCli.class.getName()); command.remove(command.size()-1);
                Process fixedCli=new ProcessBuilder(command).redirectOutput(stdout.toFile()).redirectError(stderr.toFile()).start();
                try {
                    Assertions.assertTrue(fixedCli.waitFor(20,java.util.concurrent.TimeUnit.SECONDS));Assertions.assertEquals(3,fixedCli.exitValue());
                    var result=new com.fasterxml.jackson.databind.ObjectMapper().readTree(Files.readAllBytes(stdout));
                    Assertions.assertFalse(result.path("localEvidenceReady").booleanValue());Assertions.assertFalse(result.path("canDelete").booleanValue());
                } finally {if(fixedCli.isAlive()) {fixedCli.destroyForcibly();Assertions.assertTrue(fixedCli.waitFor(5,java.util.concurrent.TimeUnit.SECONDS));}}
                Assertions.assertEquals(before,f.snapshot());Assertions.assertArrayEquals(archive,Files.readAllBytes(f.artifact()));
            }
        }
    }

    /** 正确摘要也不能掩盖receipt版本、SDK、金额、参数、证明、token及claim身份损坏；缺表是真实查询失败。 */
    @Test void receiptClaimCorruptionAndMissingTableFailClosed() throws Exception {
        try(var f=new Case(false)) {
            f.open();
            for(String field:List.of("version","sdk_order_id","intent","token","proof","proof_sha256","player_id")) {
                List<String> saved=f.rows(f.receipt).get(0);
                String value=switch(field) { case "version" -> "99"; case "player_id" -> "1"; case "intent" -> "{}"; case "proof_sha256" -> "0".repeat(64); default -> "corrupt"; };
                f.change(f.receipt,field,value); f.assertReadOnly(false); f.restoreReceipt(saved);
            }
            String proof=f.rows(f.receipt).get(0).get(6);
            for(String changed:List.of(proof.replace("\"version\":1","\"version\":99"),proof.replace("\"rmb\":12","\"rmb\":13"),
                    proof.replace(f.db.runId,"other-sdk"),proof.replace("\"addCount\":2","\"addCount\":3"),
                    proof.replace("\"payId\":73","\"payId\":74"),proof.replace("FirstCharge","Recharge"))) {
                List<String> saved=f.rows(f.receipt).get(0); f.change(f.receipt,"proof",changed);
                f.change(f.receipt,"proof_sha256",java.util.HexFormat.of().formatHex(java.security.MessageDigest.getInstance("SHA-256").digest(changed.getBytes(java.nio.charset.StandardCharsets.UTF_8))));
                f.assertReadOnly(false); f.restoreReceipt(saved);
            }
            String intent=f.rows(f.claim).get(0).get(3); f.change(f.claim,"intent","{}"); f.assertReadOnly(false); f.change(f.claim,"intent",intent);
            f.drop(f.receipt);
            var before=List.of(f.rows("t_player_data"),f.rows("t_pay_order"),f.rows(f.claim));byte[] archive=Files.readAllBytes(f.artifact());
            var result=f.check(); Assertions.assertEquals(false,result.get("localEvidenceReady"));
            Assertions.assertTrue(((List<?>)result.get("localBlockers")).contains("DURABLE_QUERY_OR_TABLE_ERROR"));
            Assertions.assertEquals(before,List.of(f.rows("t_player_data"),f.rows("t_pay_order"),f.rows(f.claim)));Assertions.assertArrayEquals(archive,Files.readAllBytes(f.artifact()));
        }
    }

    /** 当前热项变化、UNKNOWN/旧证明、缺失Login/Game行、缺完成凭证及改写归档均不能变成清理成功。 */
    @Test void changedAbsentPendingAndArchiveRisksRemainBlocked() throws Exception {
        try(var f=new Case(false)) {
            f.open(); byte[] modules=f.db.read().getModules();
            for(String mode:List.of("UNKNOWN","EXECUTING","version","sdk","rmb","legacy","missingModule")) {
                byte[] changed=alterModules(modules,f.db.id,mode);
                Assertions.assertFalse(java.util.Arrays.equals(changed, modules), "夹具故障必须实际改变模块字节：" + mode);
                f.modules(changed); f.assertReadOnly(false);
            }
            f.modules(modules);
            var mapper = new com.fasterxml.jackson.databind.ObjectMapper(new com.fasterxml.jackson.dataformat.cbor.CBORFactory());
            byte[] cborBytes = org.xerial.snappy.Snappy.uncompress(modules);
            var tree = mapper.readTree(cborBytes);
            com.fasterxml.jackson.databind.JsonNode payload = tree.isArray() ? tree.get(1) : tree;
            var module = payload.path("cn.game.games.net.game.module.player.PlayerModule"); if (module.isArray()) { module = module.get(1); }
            var items = module.path("payItems"); if (items.isArray()) { items = items.get(1); }
            ((com.fasterxml.jackson.databind.node.ObjectNode) items).remove(Long.toString(f.db.id));
            f.modules(org.xerial.snappy.Snappy.compress(mapper.writeValueAsBytes(tree)));
            Assertions.assertEquals("ALREADY_NOT_PRESENT_OBSERVED",f.check().get("hotItem")); f.assertReadOnly(false); f.modules(modules);
            f.removeLogin(); f.assertReadOnly(false); f.insertLogin();
            var row=f.db.read();
            try(var c=f.db.source.getConnection();var s=c.prepareStatement("DELETE FROM t_player_data WHERE player_id=? AND account_id=?")) {
                s.setLong(1,f.db.id);s.setString(2,f.db.runId);Assertions.assertEquals(1,s.executeUpdate());
            }
            Assertions.assertEquals(false,f.check().get("localEvidenceReady"));
            Assertions.assertEquals(PlayerSaveCoordinator.Outcome.COMMITTED,await(f.db.writer.write(row,true)).outcome());
            f.change(f.loginTable(),"pay_state","1"); f.assertReadOnly(false); f.change(f.loginTable(),"pay_state","2");
            try(var c=f.db.source.getConnection();var s=c.prepareStatement("DELETE FROM "+f.receipt+" WHERE order_id=? AND player_id=?")) {
                s.setLong(1,f.db.id);s.setLong(2,f.db.id);Assertions.assertEquals(1,s.executeUpdate());
            }
            f.assertReadOnly(false); Assertions.assertTrue(((List<?>)f.check().get("localBlockers")).contains("DURABLE_CLAIMED_WITHOUT_COMPLETED_RECEIPT"));
            byte[] original=Files.readAllBytes(f.artifact()); Files.writeString(f.artifact(),"{}"); f.assertReadOnly(false); Files.write(f.artifact(),original);
        }
    }

    /** 子JVM只注入本轮随机测试表读取器，继续使用生产参数/总超时/认证/输出及退出码。 */
    public static final class CliBridge {
        /** @param args 原四参数及测试专用随机后缀；该类只存在test classpath */
        public static void main(String[] args) throws Exception {
            var source=PaymentCleanupCli.jdbcReader(); var config=properties();
            var result=PaymentCleanupCli.run(java.util.Arrays.copyOf(args,4),reader(source,config,args[4]));
            new com.fasterxml.jackson.databind.ObjectMapper().writeValue(new java.io.FileOutputStream(java.io.FileDescriptor.out),result);
            System.exit(PaymentCleanupCli.exitCode(result));
        }
    }

    /** @return 本轮私有JavaProperties，禁止输出 */
    private static Properties properties() throws Exception {
        var p=new Properties();try(InputStream in=Files.newInputStream(Path.of(System.getenv("S2_DB_CONFIG")))) { p.load(in); }return p;
    }
    /** @param raw 数据库返回的JSON，空白格式不作假设 @param order 本轮订单 @param mode 固定故障种类 @return 精确改变的JSON树 */
    private static byte[] alterModules(byte[] raw, long order, String mode) throws Exception {
        var mapper = new com.fasterxml.jackson.databind.ObjectMapper(new com.fasterxml.jackson.dataformat.cbor.CBORFactory());
        if ("missingModule".equals(mode)) { return org.xerial.snappy.Snappy.compress(mapper.writeValueAsBytes(mapper.createObjectNode())); }
        byte[] cborBytes = org.xerial.snappy.Snappy.uncompress(raw);
        var tree = mapper.readTree(cborBytes);
        var root = tree.isArray() ? tree.get(1) : tree;
        var module = root.path("cn.game.games.net.game.module.player.PlayerModule");
        if (module.isArray()) { module = module.get(1); }
        var items = module.path("payItems");
        if (items.isArray()) { items = items.get(1); }
        var item = items.path(Long.toString(order));
        if (item.isArray()) { item = item.get(1); }
        var object = (com.fasterxml.jackson.databind.node.ObjectNode) item;
        switch (mode) {
            case "UNKNOWN", "EXECUTING" -> object.put("completionState", mode);
            case "version" -> ((com.fasterxml.jackson.databind.node.ObjectNode) object.path("completionProof")).put("version", 99);
            case "sdk" -> object.put("sdkOrderId", "other-sdk");
            case "rmb" -> object.put("rmb", 13);
            case "legacy" -> { object.remove("completionProof"); object.remove("completionState"); }
            default -> throw new AssertionError(mode);
        }
        return org.xerial.snappy.Snappy.compress(mapper.writeValueAsBytes(tree));
    }
    /** @param future 本轮真实写入 @return 有界等待后的结果 @throws Exception 失败或超时 */
    private static <T> T await(io.vertx.core.Future<T> future) throws Exception {
        return future.toCompletionStage().toCompletableFuture().get(10,java.util.concurrent.TimeUnit.SECONDS);
    }
    /** @param p 本轮配置 @param login 是否Login @return 独立超时连接 */
    private static Connection connect(Properties p,boolean login) throws Exception {
        String prefix=login?"login.":"";var auth=new Properties();auth.setProperty("user",p.getProperty(prefix+"dbuser"));auth.setProperty("password",p.getProperty(prefix+"dbpass"));
        auth.setProperty("connectTimeout","2000");auth.setProperty("socketTimeout","3000");auth.setProperty("autoReconnect","false");
        return java.sql.DriverManager.getConnection(p.getProperty(prefix+"dburl"),auth);
    }
    /** @param source 生产Game/Login源 @param p 本轮配置 @param suffix 受控后缀 @return 只替换durable表接缝 */
    private static PaymentCleanupCli.Reader reader(PaymentCleanupCli.Reader source,Properties p,String suffix) {
        return new PaymentCleanupCli.Reader() {
            @Override public String game(long id)throws Exception { return source.game(id); }
            @Override public PaymentEvidenceCli.ModulesSnapshot gameSnapshot(long id)throws Exception { return source.gameSnapshot(id); }
            @Override public PaymentEvidenceCli.LoginOrder login(long id)throws Exception { return source.login(id); }
            @Override public PaymentDedupStore.Lookup durable(long id,PayItem archive)throws Exception {
                try(var c=connect(p,false)) { c.setReadOnly(true);return PaymentDedupStore.observe(c,suffix,id,archive); }
            }
        };
    }

    /** 精确占有、逐次冷查、finally清理；记录创建集合和持久comment后才DROP。 */
    private static final class Case implements AutoCloseable {
        /** 本轮真实玩家与事务写入器。 */
        final PlayerSaveDatabaseIT.DatabaseFixture db;
        /** 本轮随机表后缀及精确归属集合。 */
        final String suffix=UUID.randomUUID().toString().replace("-","");
        /** 只使用预先验证的随机表名。 */
        final String claim="s3d_claim_"+suffix,receipt="s3d_receipt_"+suffix;
        /** 仅实际创建后才拥有DROP权限。 */
        final java.util.Set<String> created=new java.util.HashSet<>();
        /** 临时密钥与归档，夹具结束后销毁。 */
        final Path base,root,key;
        /** 当前轮配置仅留内存，文件由runner finally删除。 */
        final Properties config;
        /** 登录/玩家占有状态。 */
        boolean seeded,loginInserted;
        /** @param spring 是否真实Spring事务 */
        Case(boolean spring)throws Exception {
            db=new PlayerSaveDatabaseIT.DatabaseFixture(spring);config=properties();
            base=Files.createTempDirectory(Path.of(System.getenv("S3_ARCHIVE_TEST_ROOT")),"cleanup-db-");root=base.resolve("archive");key=base.resolve("key.bin");
            byte[] bytes=new byte[32];new java.security.SecureRandom().nextBytes(bytes);Files.write(key,bytes);java.util.Arrays.fill(bytes,(byte)0);
        }
        /** 进入资源作用域后才建行/表，保存真实完成事务后认证封存。 */
        void open()throws Exception {
            db.seed();seeded=true;
            String sql;try(InputStream in=getClass().getResourceAsStream("/db/migration/payment-durable-dedup-v1.sql")) { sql=new String(in.readAllBytes(),java.nio.charset.StandardCharsets.UTF_8); }
            sql=sql.replaceAll("(?m)^--.*$","").replace("t_payment_fulfillment_claim",claim).replace("t_payment_fulfillment_receipt",receipt)
                    .replace(") ENGINE=InnoDB",") ENGINE=InnoDB COMMENT='cleanup_"+suffix+"'");
            String[] statements=sql.trim().split(";");Assertions.assertEquals(2,statements.length);
            for(int i=0;i<2;i++) { execute(statements[i]);created.add(i==0?claim:receipt); }
            insertLogin();var player=db.player();PayItem item=new PayItem();item.setOrderId(db.id);item.setSdkOrderId(db.runId);item.setRmb(12);item.setPayType(PayType.FirstCharge);
            item.setPayId(73);item.setSdkGoodsId(55);item.setAddId(100);item.setAddCount(2);item.addPaySubIds(1,2);
            var store=new PaymentDedupStore(()->db.factory,suffix);var ownership=store.claim(PaymentDedupStore.intent(db.id,item,db.runId));
            item.finish();item.setCompletionState("REWARDS_COMPLETED");item.prepareCompletionProof(db.id);item.confirmCompletionProof();
            player.getPlayerModule().addPayItems(item);player.getPlayerModule().setDurablePayment(PaymentDedupStore.completed(ownership,item));
            Assertions.assertEquals(PlayerSaveCoordinator.Outcome.COMMITTED,await(db.writer.write(PlayerDataSnapshot.capture(player),false)).outcome());
            Assertions.assertEquals("SEALED_AND_VERIFIED",PaymentArchiveCli.run(new String[]{"archive",Long.toString(db.id),Long.toString(db.id),root.toString(),key.toString()}).get("status"));
            log.info("PAYMENT_CLEANUP_FIXTURE_OPENED suffix={}",suffix);
        }
        /** @return 生产命令参数 */
        String[] args() { return new String[]{Long.toString(db.id),Long.toString(db.id),root.toString(),key.toString()}; }
        /** @return 固定归档文件 */
        Path artifact() { return root.resolve("payment-"+db.id+"-"+db.id+".json"); }
        /** @return 真实生产读链加随机表接缝 */
        Map<String,Object> check()throws Exception { return PaymentCleanupCli.run(args(),reader(PaymentCleanupCli.jdbcReader(),config,suffix)); }
        /** @param ready 预期局部状态；所有预检均核对全行及字节未变 */
        void assertReadOnly(boolean ready)throws Exception {
            var before=snapshot();byte[] bytes=Files.readAllBytes(artifact());var result=check();
            Assertions.assertEquals(ready,result.get("localEvidenceReady"),result.toString());Assertions.assertEquals(false,result.get("canDelete"));Assertions.assertEquals(false,result.get("canResend"));
            Assertions.assertEquals(2,((List<?>)result.get("externalBlockers")).size());Assertions.assertEquals(before,snapshot());Assertions.assertArrayEquals(bytes,Files.readAllBytes(artifact()));
        }
        /** @return 全字段冷读，包含Game模块、Login、claim、receipt及行数 */
        List<List<String>> snapshot()throws Exception {
            var out=new ArrayList<List<String>>();for(String table:List.of("t_player_data","t_pay_order",claim,receipt)) { out.addAll(rows(table)); }return out;
        }
        /** @param table 本例固定表 @return 精确主键各字段及null，无stdout */
        List<List<String>> rows(String table)throws Exception {
            boolean login=table.equals("t_pay_order");String column=login?"id":table.equals("t_player_data")?"player_id":"order_id";
            try(var c=connect(config,login);var s=c.prepareStatement("SELECT * FROM "+table+" WHERE "+column+"=?")) {
                s.setLong(1,db.id);s.setQueryTimeout(3);try(var r=s.executeQuery()) {var rows=new ArrayList<List<String>>();while(r.next()) {var row=new ArrayList<String>();for(int i=1;i<=r.getMetaData().getColumnCount();i++) {row.add(r.getString(i));}rows.add(row);}return rows;}
            }
        }
        /** @return 固定Login表，仅测试故障用 */
        String loginTable() { return "t_pay_order"; }
        /** @param table 受控本例表 @param field 代码内固定字段 @param value 夹具故障值 */
        void change(String table,String field,String value)throws Exception {
            try(var c=connect(config,table.equals("t_pay_order"));var s=c.prepareStatement("UPDATE "+table+" SET "+field+"=? WHERE "+(table.equals("t_pay_order")?"id":"order_id")+"=? AND player_id=?")) {
                s.setString(1,value);s.setLong(2,db.id);s.setLong(3,db.id);Assertions.assertEquals(1,s.executeUpdate());
            }
        }
        /** @param saved 本轮损坏前receipt整行 */
        void restoreReceipt(List<String> saved)throws Exception {
            try(var c=connect(config,false);var s=c.prepareStatement("UPDATE "+receipt+" SET sdk_order_id=?,player_id=?,version=?,intent=?,token=?,proof=?,proof_sha256=? WHERE order_id=?")) {
                s.setString(1,saved.get(1));s.setString(2,saved.get(2));s.setString(3,saved.get(3));s.setString(4,saved.get(4));s.setString(5,saved.get(5));s.setString(6,saved.get(6));s.setString(7,saved.get(7));s.setLong(8,db.id);Assertions.assertEquals(1,s.executeUpdate());
            }
        }
        /** @param value 本轮测试modules字节 */
        void modules(byte[] value)throws Exception {
            try(var c=connect(config,false);var s=c.prepareStatement("UPDATE t_player_data SET modules=? WHERE player_id=? AND account_id=?")) {s.setBytes(1,value);s.setLong(2,db.id);s.setString(3,db.runId);Assertions.assertEquals(1,s.executeUpdate());}
        }
        /** 只插入本轮唯一Login行，冲突立即失败。 */
        void insertLogin()throws Exception {
            try(var c=connect(config,true);var s=c.prepareStatement("INSERT INTO t_pay_order (id,game_server_id,channel_code,channel_label,user_id,player_id,third_uid,env,pay_state,is_deliver,create_date,create_time,debug_text,third_order_id,price,item_name,callback) VALUES (?,'s3d-test','s3d-test','s3d-test',?,?,?,1,2,1,?,?,?, ?,1200,'s3d-test','{}')")) {
                s.setLong(1,db.id);s.setLong(2,db.id);s.setLong(3,db.id);s.setString(4,db.runId);s.setString(5,cn.game.util.DateUtil.nowDateStr());s.setString(6,cn.game.util.DateUtil.nowTimeStr());s.setString(7,db.runId);s.setString(8,db.runId);Assertions.assertEquals(1,s.executeUpdate());loginInserted=true;
            }
        }
        /** 精确核对身份后删除本轮Login夹具并冷查为空。 */
        void removeLogin()throws Exception {
            try(var c=connect(config,true);var s=c.prepareStatement("DELETE FROM t_pay_order WHERE id=? AND player_id=? AND debug_text=? AND third_uid=? AND third_order_id=?")) {
                s.setLong(1,db.id);s.setLong(2,db.id);s.setString(3,db.runId);s.setString(4,db.runId);s.setString(5,db.runId);Assertions.assertEquals(1,s.executeUpdate());loginInserted=false;
            }Assertions.assertTrue(rows("t_pay_order").isEmpty());
        }
        /** @param sql 仅内部迁移副本或已验身份的DROP */
        void execute(String sql)throws Exception {try(var c=connect(config,false);var s=c.createStatement()) {s.setQueryTimeout(3);s.execute(sql);} }
        /** @param table 仅本例创建且comment匹配的精确表 */
        void drop(String table)throws Exception {
            Assertions.assertTrue(created.contains(table));try(var c=connect(config,false);var s=c.prepareStatement("SELECT table_comment FROM information_schema.tables WHERE table_schema=DATABASE() AND table_name=?")) {
                s.setString(1,table);try(var r=s.executeQuery()) {Assertions.assertTrue(r.next());Assertions.assertEquals("cleanup_"+suffix,r.getString(1));}
            }execute("DROP TABLE "+table);created.remove(table);
        }
        @Override public void close()throws Exception {
            db.close();
            try { if(loginInserted) {removeLogin();} }
            finally {
                try {if(seeded) {db.clean();} }
                finally {
                    try {
                        for(String table:List.copyOf(created)) {drop(table);}
                        try(var c=connect(config,false);var s=c.prepareStatement("SELECT COUNT(*) FROM information_schema.tables WHERE table_schema=DATABASE() AND table_name IN (?,?)")) {
                            s.setString(1,claim);s.setString(2,receipt);try(var r=s.executeQuery()) {Assertions.assertTrue(r.next());Assertions.assertEquals(0,r.getInt(1));}
                        }
                        Assertions.assertTrue(rows("t_pay_order").isEmpty());log.info("PAYMENT_CLEANUP_FIXTURE_COLD_EMPTY suffix={} game=1 login=1 tables=2",suffix);
                    } finally {try(var paths=Files.walk(base)) {for(Path p:paths.sorted(java.util.Comparator.reverseOrder()).toList()) {Files.delete(p);}}Assertions.assertFalse(Files.exists(base));}
                }
            }
        }
    }
}
