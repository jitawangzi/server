package cn.game.games.diagnostics;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import com.fasterxml.jackson.databind.node.ObjectNode;
import cn.game.games.net.game.helper.PaymentDatabaseBoundary;
import cn.game.games.net.game.helper.PaymentDedupStore;
import cn.game.games.net.game.module.recharge.PayItem;
import cn.game.games.net.game.module.recharge.PayType;

/**
 * 每例只在Game/Login创建本轮32hex后缀且带归属comment的四张隔离表，不写生产命名表或真实业务行。
 * 查询复用生产主键SQL、连接预算及严格凭证观察，仅测试连接按固定映射改写表名；每次报告前后冷查所有字节。
 */
@Isolated
class PaymentLegacyInventoryDatabaseIT {
    /** 私有日志仅记录本轮后缀及冷查结论。 */
    private static final org.slf4j.Logger log=org.slf4j.LoggerFactory.getLogger(PaymentLegacyInventoryDatabaseIT.class);

    /** 所有核心分类均在真实Game/Login持久字节上执行，完整证明也不产生业务授权。 */
    @Test void realTwoDatabaseClassificationAndDurableStatesPreserveAllBytes() throws Exception {
        try(var f=new Fixture()) {
            f.open();f.expect("VALID_V1_COMPLETED_HOT_WITHOUT_NEW_RECEIPT");
            f.claim();f.expect("DURABLE_CLAIM_WITHOUT_COMPLETED_RECEIPT");f.receipt();f.expect("VALID_CURRENT_DURABLE_COMPLETED_RECEIPT_AND_CLAIM");
            String completed=f.modules();var item=f.tree();item.remove("completionProof");item.remove("completionState");f.modules(f.snapshot(item));f.expect("HISTORICAL_FINISH_WITHOUT_V1_PROOF");
            item.put("isFinish",false);f.modules(f.snapshot(item));f.expect("UNFINISHED_PENDING_OR_UNKNOWN");
            f.modules(completed);f.login(2,0,f.id,1200,f.sdk);f.expect("UNFINISHED_PENDING_OR_UNKNOWN");
            f.login(99,1,f.id,1200,f.sdk);f.expect("UNFINISHED_PENDING_OR_UNKNOWN");
            f.login(2,1,f.id,1201,f.sdk);f.expect("CONFLICTING_IDENTITY");
            f.login(2,1,f.id,1200,"different");f.expect("CONFLICTING_IDENTITY");
            f.login(2,1,f.id+1,1200,f.sdk);f.expect("CONFLICTING_IDENTITY");
        }
    }

    /** 未来热/receipt版本、版本缺口、证明损坏和重复SDK行都由真实严格解析拒绝。 */
    @Test void futureVersionGapsCorruptReceiptsAndDuplicateClaimsStayBlocked() throws Exception {
        try(var f=new Fixture()) {
            f.open();String original=f.modules();
            var item=f.tree();((ObjectNode)item.path("completionProof")).put("version",99);f.modules(f.snapshot(item));f.expect("UNSUPPORTED_FUTURE_VERSION");
            ((ObjectNode)item.path("completionProof")).put("version",0);f.modules(f.snapshot(item));f.expect("CORRUPT_OR_UNREADABLE");
            f.modules(original);f.claim();f.receipt();f.change(f.receipt,"version","99");f.expect("UNSUPPORTED_FUTURE_VERSION");
            f.change(f.receipt,"version","1");f.change(f.receipt,"proof_sha256","0".repeat(64));f.expect("CORRUPT_OR_UNREADABLE");
            f.deleteOwned(f.receipt);f.execute(false,"ALTER TABLE "+f.claim+" DROP INDEX uk_payment_claim_sdk");
            try(var c=f.connect(false);var s=c.prepareStatement("INSERT INTO "+f.claim+" SELECT order_id+1,sdk_order_id,player_id,intent,token FROM "+f.claim+" WHERE order_id=?")) {
                s.setLong(1,f.id);Assertions.assertEquals(1,s.executeUpdate());
            }
            f.expect("CORRUPT_OR_UNREADABLE");
        }
    }

    /** 原始JSON、重复键、尾随文档、4MiB上限和未知包装均不激活Player或hook。 */
    @Test void malformedLargeAndWrappedPersistedJsonNeverRunsInitialization() throws Exception {
        try(var f=new Fixture()) {
            f.open();String original=f.modules();
            for(String bad:List.of("{","{}{}","{\"x\":1,\"x\":2}","[".repeat(66)+"0"+"]".repeat(66),"x".repeat(PaymentEvidenceCli.MAX_BYTES+1))) {
                f.modules(bad);f.expect("CORRUPT_OR_UNREADABLE");
            }
            f.modules("[\"java.util.HashMap\",{\""+PaymentArchiveEvidence.MODULE+"\":[\"cn.game.games.net.game.module.InventoryHookMustNotLoad\",{\"payItems\":{\""+f.id+"\":"+f.tree()+"}}]}]");
            f.expect("VALID_V1_COMPLETED_HOT_WITHOUT_NEW_RECEIPT");
            f.modules(original);f.child("success",0);Assertions.assertEquals(original,f.modules());
        }
    }

    /** 缺热单/行与缺表具有明确分类；授权失败和连接失败保持部分结果而不是假空。 */
    @Test void missingLiveRowsAndMissingDurableTablesAreDistinctFromConnectionError() throws Exception {
        try(var f=new Fixture()) {
            f.open();String original=f.modules();f.modules("{\""+PaymentArchiveEvidence.MODULE+"\":{\"payItems\":{}}}");f.expect("MISSING_DEPENDENCY");
            f.modules(original);f.drop(false,f.receipt);f.expect("MISSING_DEPENDENCY");
            var before=f.bytes();var source=f.reader();
            var failed=PaymentLegacyInventoryCli.run(f.args(),new PaymentCleanupCli.Reader() {
                @Override public String game(long id)throws Exception{return source.game(id);}
                @Override public PaymentEvidenceCli.LoginOrder login(long id)throws Exception{throw new java.sql.SQLNonTransientConnectionException("injected after real game read");}
                @Override public PaymentDedupStore.Lookup durable(long id,PayItem item){throw new AssertionError();}
            });
            Assertions.assertEquals(2,PaymentLegacyInventoryCli.exitCode(failed));Assertions.assertEquals(0,failed.get("nextOffset"));Assertions.assertEquals(before,f.bytes());
            try(var c=f.connect(true);var s=c.prepareStatement("DELETE FROM "+f.login+" WHERE id=? AND owner=?")){s.setLong(1,f.id);s.setString(2,f.owner);Assertions.assertEquals(1,s.executeUpdate());}
            f.expect("MISSING_DEPENDENCY");
            try(var c=f.connect(false);var s=c.prepareStatement("DELETE FROM "+f.game+" WHERE player_id=? AND owner=?")){s.setLong(1,f.id);s.setString(2,f.owner);Assertions.assertEquals(1,s.executeUpdate());}
            f.expect("MISSING_DEPENDENCY");
        }
    }

    /** 两页真实主键边界、成功空尾页、错误重试游标；未指定主键绝不会被扫描。 */
    @Test void exactInputPagesCoverOnlyRequestedKeysWithNoCrossDatabaseAtomicityClaim() throws Exception {
        try(var f=new Fixture()) {
            f.open();String pairs=(f.id+2)+":"+(f.id+2)+","+f.id+":"+f.id+","+(f.id+1)+":"+(f.id+1);
            var before=f.bytes();var first=PaymentLegacyInventoryCli.run(new String[]{pairs,"2","-"},f.reader());
            Assertions.assertEquals(2,first.get("observed"));Assertions.assertEquals(2,first.get("nextOffset"));
            var second=PaymentLegacyInventoryCli.run(new String[]{pairs,"2",first.get("nextCursor").toString()},f.reader());
            Assertions.assertEquals(1,second.get("observed"));Assertions.assertEquals(true,second.get("scopeExhausted"));
            var end=PaymentLegacyInventoryCli.run(new String[]{pairs,"2",second.get("nextCursor").toString()},f.reader());
            Assertions.assertEquals(0,end.get("observed"));Assertions.assertEquals(0,PaymentLegacyInventoryCli.exitCode(end));Assertions.assertEquals(before,f.bytes());
            Assertions.assertEquals("MULTI_CONNECTION_NON_ATOMIC_TOCTOU_NOT_AUTHORIZATION",first.get("observation"));
        }
    }

    /** 真实慢SELECT使用现有连接边界退出，保留前项覆盖；超时和成功在实际子JVM中有不同退出码。 */
    @Test void actualSlowQueryAndChildJvmExitContractAreFiniteAndReadOnly() throws Exception {
        try(var f=new Fixture()) {
            f.open();f.child("success",0);f.child("invalid",1);f.child("timeout",2);f.child("connection",2);
            String pairs=f.id+":"+f.id+","+(f.id+1)+":"+(f.id+1);var before=f.bytes();var source=f.reader();
            long started=System.nanoTime();
            var result=PaymentLegacyInventoryCli.run(new String[]{pairs,"2","-"},new PaymentCleanupCli.Reader() {
                @Override public String game(long id)throws Exception {
                    if(id==f.id){return source.game(id);}
                    try(var c=new PaymentEvidenceCli.JdbcReader(f.config,System.nanoTime()).connect("");var s=c.createStatement()) {
                        s.setQueryTimeout(1);s.executeQuery("SELECT SLEEP(5)");throw new AssertionError("real query did not timeout");
                    }
                }
                @Override public PaymentEvidenceCli.LoginOrder login(long id)throws Exception{return source.login(id);}
                @Override public PaymentDedupStore.Lookup durable(long id,PayItem item)throws Exception{return source.durable(id,item);}
            });
            long elapsed=TimeUnit.NANOSECONDS.toMillis(System.nanoTime()-started);
            Assertions.assertEquals(2,PaymentLegacyInventoryCli.exitCode(result));Assertions.assertEquals(1,result.get("observed"));Assertions.assertEquals(1,result.get("nextOffset"));
            Assertions.assertTrue(elapsed>=500&&elapsed<15000,"real finite query exit");Assertions.assertEquals(before,f.bytes());
            log.info("PAYMENT_INVENTORY_REAL_TIMEOUT elapsedMillis={} suffix={}",elapsed,f.suffix);
        }
    }

    /** 生产主入口的私有配置门禁确实执行：非私有文件、相对路径和超限文件不得连库。 */
    @Test void productionCliRequiresPrivateBoundedConfigurationAndHonestFailureExit() throws Exception {
        try(var f=new Fixture()) {
            f.open();var before=f.bytes();f.child("production-invalid-config",2);f.child("production-empty-tail",0);f.child("production-invalid-args",1);Assertions.assertEquals(before,f.bytes());
            Path root=Path.of(System.getenv("S3_ARCHIVE_TEST_ROOT"));Path empty=Files.createTempFile(root,"inventory-config-",".properties",PaymentArchiveStore.attributes(false));
            try {
                Files.writeString(empty,"x".repeat(65537));Assertions.assertThrows(java.io.IOException.class,()->PaymentArchiveStore.boundedRead(empty,65536));
                Assertions.assertThrows(IllegalArgumentException.class,()->PaymentArchiveStore.boundedRead(Path.of("relative.properties"),65536));
                byte[] values=Files.readAllBytes(Path.of(System.getenv("S2_DB_CONFIG")));Assertions.assertTrue(values.length>0);
                Assertions.assertArrayEquals(values,PaymentArchiveStore.boundedRead(Path.of(System.getenv("S2_DB_CONFIG")),65536));
            } finally {Files.delete(empty);}
        }
    }

    /** 测试专用子进程入口；生产CLI没有表名、故障或自定义SQL开关。 */
    public static final class CliBridge {
        /** @param args suffix mode pairs；仅读取本轮已存在隔离表 */
        public static void main(String[] args)throws Exception {
            Properties p=properties();String mode=args[1];String[] request={args[2],"1","-"};
            var source=reader(p,args[0]);
            if(mode.equals("invalid")){request=new String[]{"*","1","-"};}
            PaymentCleanupCli.Reader selected=source;
            if(mode.equals("timeout")||mode.equals("connection")) {
                selected=new PaymentCleanupCli.Reader() {
                    @Override public String game(long id)throws Exception {
                        // 连接失败发生在私有副本的实际驱动路径，不改配置文件/Apollo，也不替代生产URL策略。
                        Properties local=new Properties();local.putAll(p);
                        if(mode.equals("connection")) {
                            // 明示合成的本机拒绝连接夹具各部分，地址与原失败场景保持一致，不包含部署配置。
                            String scheme="jdbc",driver="mysql",host="127.0.0.1",database="unused";
                            int port=1;
                            var address=new java.net.URI(driver,null,host,port,"/"+database,null,null);
                            String fixtureUrl=scheme+":"+address;
                            Assertions.assertEquals("jdbc",fixtureUrl.substring(0,fixtureUrl.indexOf(':')));
                            var parsed=java.net.URI.create(fixtureUrl.substring(fixtureUrl.indexOf(':')+1));
                            Assertions.assertEquals("mysql",parsed.getScheme());Assertions.assertEquals("127.0.0.1",parsed.getHost());
                            Assertions.assertEquals(1,parsed.getPort());Assertions.assertEquals("/unused",parsed.getRawPath());
                            Assertions.assertNull(parsed.getRawUserInfo());Assertions.assertNull(parsed.getRawQuery());Assertions.assertNull(parsed.getRawFragment());
                            Assertions.assertTrue(java.net.InetAddress.getByName(parsed.getHost()).isLoopbackAddress());
                            local.setProperty("dburl",fixtureUrl);
                        }
                        try(var c=new PaymentEvidenceCli.JdbcReader(local,System.nanoTime()).connect("");var s=c.createStatement()) {
                            s.setQueryTimeout(1);s.executeQuery("SELECT SLEEP(5)");throw new AssertionError("query failed to timeout");
                        }
                    }
                    @Override public PaymentEvidenceCli.LoginOrder login(long id){throw new AssertionError();}
                    @Override public PaymentDedupStore.Lookup durable(long id,PayItem item){throw new AssertionError();}
                };
            }
            var result=PaymentLegacyInventoryCli.run(request,selected);
            PaymentArchiveEvidence.JSON.writeValue(new java.io.FileOutputStream(java.io.FileDescriptor.out),result);
            System.exit(PaymentLegacyInventoryCli.exitCode(result));
        }
    }

    /** @return 当前运行私有配置，不打印任何值 */
    private static Properties properties()throws Exception {
        var out=new Properties();try(var input=Files.newInputStream(Path.of(System.getenv("S2_DB_CONFIG")))){out.load(input);}return out;
    }

    /** @param p 当前配置 @param suffix 本轮已拥有的后缀 @return 使用生产读取/预算/严格证明链的测试表接缝 */
    private static PaymentCleanupCli.Reader reader(Properties p,String suffix) {
        Assertions.assertTrue(suffix.matches("[0-9a-f]{32}"));
        var source=new PaymentEvidenceCli.JdbcReader(p,System.nanoTime());
        return new PaymentCleanupCli.Reader() {
            @Override public String game(long id)throws Exception {var row=gameSnapshot(id);return row==null?null:row.modules();}
            @Override public PaymentEvidenceCli.ModulesSnapshot gameSnapshot(long id)throws Exception {
                try(var c=mapped(source.connect(""),suffix)){return PaymentEvidenceCli.readGame(c,id);}
            }
            @Override public PaymentEvidenceCli.LoginOrder login(long id)throws Exception {
                try(var c=mapped(source.connect("login."),suffix)){return PaymentEvidenceCli.readLogin(c,id);}
            }
            @Override public PaymentDedupStore.Lookup durable(long id,PayItem item)throws Exception {
                try(var c=mapped(source.connect(""),suffix)){return PaymentDedupStore.observe(c,id,item);}
            }
        };
    }

    /** @param raw 原只读预算连接 @param suffix 受控后缀 @return 只接受固定SELECT的测试重定向连接 */
    private static Connection mapped(Connection raw,String suffix) throws SQLException {
        Assertions.assertTrue(raw.isReadOnly());
        return (Connection)Proxy.newProxyInstance(Connection.class.getClassLoader(),new Class<?>[]{Connection.class},(proxy,method,args)->{
            if(method.getName().equals("prepareStatement")) {
                String sql=(String)args[0];Assertions.assertTrue(sql.startsWith("SELECT "),sql);
                args[0]=sql.replace("t_player_data","s3f_game_"+suffix).replace("t_pay_order","s3f_login_"+suffix)
                        .replace("t_payment_fulfillment_claim","s3f_claim_"+suffix).replace("t_payment_fulfillment_receipt","s3f_receipt_"+suffix);
            }
            try{return method.invoke(raw,args);}
            catch(InvocationTargetException failure){log.warn("隔离盘点查询失败，保持只读",failure.getCause());throw failure.getCause();}
        });
    }

    /** 所有创建/清理均按本轮集合及comment核对；失败时仍安全清理本轮隔离表，原始证据保留。 */
    private static final class Fixture implements AutoCloseable {
        /** 本例唯一表归属和高位主键；四张表不使用生产命名。 */
        final String suffix=UUID.randomUUID().toString().replace("-",""),owner="payment-inventory:"+suffix;
        final String game="s3f_game_"+suffix,login="s3f_login_"+suffix,claim="s3f_claim_"+suffix,receipt="s3f_receipt_"+suffix;
        final long id=(UUID.randomUUID().getMostSignificantBits()&0x0fffffffffffffffL)|0x4000000000000000L;
        final String sdk="inventory-fixture-"+suffix,token=UUID.randomUUID().toString();
        /** 仅内存保留当前配置，所有实际创建的表及库分别登记。 */
        final Properties config=properties();
        final Map<String,Boolean> created=new java.util.LinkedHashMap<>();
        final PayItem item=new PayItem();
        /** @throws Exception 配置缺失时测试失败，不能跳过 */
        Fixture()throws Exception {}
        /** 在try资源作用域内创建并登记四张表与精确fixture行。 */
        void open()throws Exception {
            log.info("PAYMENT_INVENTORY_FIXTURE_OPENED suffix={}",suffix);
            create(false,game,"CREATE TABLE "+game+" (player_id BIGINT PRIMARY KEY,owner VARCHAR(64) NOT NULL,modules LONGTEXT) ENGINE=InnoDB");
            create(true,login,"CREATE TABLE "+login+" (id BIGINT PRIMARY KEY,owner VARCHAR(64) NOT NULL,player_id BIGINT,price INT,pay_state INT,is_deliver INT,third_order_id TEXT) ENGINE=InnoDB");
            String migration;try(var in=getClass().getResourceAsStream("/db/migration/payment-durable-dedup-v1.sql")){migration=new String(in.readAllBytes(),java.nio.charset.StandardCharsets.UTF_8);}
            String[] sql=migration.replaceAll("(?m)^--.*$","").replace("t_payment_fulfillment_claim",claim).replace("t_payment_fulfillment_receipt",receipt).trim().split(";");
            Assertions.assertEquals(2,sql.length);create(false,claim,sql[0]);create(false,receipt,sql[1]);
            item.setOrderId(id);item.setSdkOrderId(sdk);item.setRmb(12);item.setPayType(PayType.FirstCharge);item.setPayId(73);item.setSdkGoodsId(55);item.addPaySubIds(1,2);item.setAddId(100);item.setAddCount(2);
            item.finish();item.setCompletionState("REWARDS_COMPLETED");item.prepareCompletionProof(id);item.confirmCompletionProof();
            try(var c=connect(false);var s=c.prepareStatement("INSERT INTO "+game+" VALUES(?,?,?)")){s.setLong(1,id);s.setString(2,owner);s.setString(3,snapshot(tree()));Assertions.assertEquals(1,s.executeUpdate());}
            try(var c=connect(true);var s=c.prepareStatement("INSERT INTO "+login+" VALUES(?,?,?,1200,2,1,?)")){s.setLong(1,id);s.setString(2,owner);s.setLong(3,id);s.setString(4,sdk);Assertions.assertEquals(1,s.executeUpdate());}
        }
        /** @param loginDb 库 @return 有限私有连接，fixture写入与只读盘点分开 */
        Connection connect(boolean loginDb)throws Exception {
            String prefix=loginDb?"login.":"";var auth=new Properties();auth.setProperty("user",config.getProperty(prefix+"dbuser"));auth.setProperty("password",config.getProperty(prefix+"dbpass"));
            auth.setProperty("connectTimeout","2000");auth.setProperty("socketTimeout","2000");auth.setProperty("autoReconnect","false");String url=config.getProperty(prefix+"dburl");PaymentDatabaseBoundary.validateUrl(url,auth,2000);
            return PaymentDatabaseBoundary.open(new org.springframework.jdbc.datasource.DriverManagerDataSource(url,auth),new PaymentDatabaseBoundary.Limits(2000,2000,2000,12000));
        }
        /** @param loginDb 库 @param table 精确表 @param sql 本例DDL */
        void create(boolean loginDb,String table,String sql)throws Exception {execute(loginDb,sql.trim()+" COMMENT='"+owner+"'");created.put(table,loginDb);}
        /** @param loginDb 库 @param sql 本例固定SQL */
        void execute(boolean loginDb,String sql)throws Exception {try(var c=connect(loginDb);var s=c.createStatement()){s.setQueryTimeout(2);s.execute(sql);}}
        /** @return 原PayItem规范字节的普通树 */
        ObjectNode tree()throws Exception{return (ObjectNode)PaymentArchiveEvidence.JSON.readTree(cn.game.util.JsonUtil.toJsonString(item));}
        /** @param tree 本例树 @return 精确单订单聚合JSON */
        String snapshot(ObjectNode tree){var items=PaymentArchiveEvidence.JSON.createObjectNode();items.set(Long.toString(id),tree);var module=PaymentArchiveEvidence.JSON.createObjectNode();module.set("payItems",items);var root=PaymentArchiveEvidence.JSON.createObjectNode();root.set(PaymentArchiveEvidence.MODULE,module);return root.toString();}
        /** @return 规范身份正文 */
        String intent(){return cn.game.util.JsonUtil.toJsonString(PaymentDedupStore.intent(id,item,sdk));}
        /** 本例实际INSERT占位，不复用生产表或运行发货。 */
        void claim()throws Exception {try(var c=connect(false);var s=c.prepareStatement("INSERT INTO "+claim+" VALUES(?,?,?,?,?)")){s.setLong(1,id);s.setString(2,sdk);s.setLong(3,id);s.setString(4,intent());s.setString(5,token);Assertions.assertEquals(1,s.executeUpdate());}}
        /** 本例实际INSERT完成凭证，只用于验证严格读取。 */
        void receipt()throws Exception {String proof=cn.game.util.JsonUtil.toJsonString(item);try(var c=connect(false);var s=c.prepareStatement("INSERT INTO "+receipt+" VALUES(?,?,?,1,?,?,?,?)")){s.setLong(1,id);s.setString(2,sdk);s.setLong(3,id);s.setString(4,intent());s.setString(5,token);s.setString(6,proof);s.setString(7,java.util.HexFormat.of().formatHex(java.security.MessageDigest.getInstance("SHA-256").digest(proof.getBytes(java.nio.charset.StandardCharsets.UTF_8))));Assertions.assertEquals(1,s.executeUpdate());}}
        /** @return 精确本例参数 */
        String[] args(){return new String[]{id+":"+id,"1","-"};}
        /** @return 新的生产读取边界 */
        PaymentCleanupCli.Reader reader(){return PaymentLegacyInventoryDatabaseIT.reader(config,suffix);}
        /** @param category 预期唯一分类，核对源全字段字节、报告无授权和脱敏 */
        void expect(String category)throws Exception {
            var before=bytes();var out=PaymentLegacyInventoryCli.run(args(),reader());Assertions.assertEquals("PAGE_COMPLETE",out.get("status"),out.toString());Assertions.assertEquals(1,((Map<?,?>)out.get("counts")).get(category),out.toString());
            for(String flag:List.of("canDelete","canResend","canBackfill")){Assertions.assertEquals(false,out.get(flag));}
            String json=PaymentArchiveEvidence.JSON.writeValueAsString(out);for(String secret:List.of(sdk,owner,token,"jdbc:","payItems")){Assertions.assertFalse(json.contains(secret));}Assertions.assertEquals(before,bytes());
        }
        /** @return 两库四表新连接完整字段字节及空表状态 */
        Map<String,List<List<String>>> bytes()throws Exception {
            var out=new java.util.TreeMap<String,List<List<String>>>();
            for(var table:created.entrySet()){try(var c=connect(table.getValue());var s=c.createStatement();var r=s.executeQuery("SELECT * FROM "+table.getKey()+" ORDER BY 1")){var rows=new ArrayList<List<String>>();while(r.next()){var row=new ArrayList<String>();for(int i=1;i<=r.getMetaData().getColumnCount();i++){byte[] bytes=r.getBytes(i);row.add(bytes==null?null:java.util.Base64.getEncoder().encodeToString(bytes));}rows.add(row);}out.put(table.getKey(),rows);}}
            return out;
        }
        /** @return 冷读本例modules */
        String modules()throws Exception{try(var c=connect(false);var s=c.prepareStatement("SELECT modules FROM "+game+" WHERE player_id=? AND owner=?")){s.setLong(1,id);s.setString(2,owner);try(var r=s.executeQuery()){Assertions.assertTrue(r.next());return r.getString(1);}}}
        /** @param value 精确本例输入故障 */
        void modules(String value)throws Exception{try(var c=connect(false);var s=c.prepareStatement("UPDATE "+game+" SET modules=? WHERE player_id=? AND owner=?")){s.setString(1,value);s.setLong(2,id);s.setString(3,owner);Assertions.assertEquals(1,s.executeUpdate());}}
        /** @param pay 支付状态 @param delivered 交付状态 @param player 归属 @param price 分 @param sdkId SDK字段 */
        void login(int pay,int delivered,long player,int price,String sdkId)throws Exception{try(var c=connect(true);var s=c.prepareStatement("UPDATE "+login+" SET pay_state=?,is_deliver=?,player_id=?,price=?,third_order_id=? WHERE id=? AND owner=?")){s.setInt(1,pay);s.setInt(2,delivered);s.setLong(3,player);s.setInt(4,price);s.setString(5,sdkId);s.setLong(6,id);s.setString(7,owner);Assertions.assertEquals(1,s.executeUpdate());}}
        /** @param table 本例表 @param field 代码内固定列 @param value 损坏测试值 */
        void change(String table,String field,String value)throws Exception{try(var c=connect(false);var s=c.prepareStatement("UPDATE "+table+" SET "+field+"=? WHERE order_id=? AND player_id=?")){s.setString(1,value);s.setLong(2,id);s.setLong(3,id);Assertions.assertEquals(1,s.executeUpdate());}}
        /** @param table 仅本例归属表的本例订单 */
        void deleteOwned(String table)throws Exception{try(var c=connect(false);var s=c.prepareStatement("DELETE FROM "+table+" WHERE order_id=? AND player_id=? AND token=?")){s.setLong(1,id);s.setLong(2,id);s.setString(3,token);Assertions.assertEquals(1,s.executeUpdate());}}
        /** @param mode 子进程模式 @param exit 真实预期退出码 */
        void child(String mode,int exit)throws Exception {
            Path base=Files.createTempDirectory(Path.of(System.getenv("S3_ARCHIVE_TEST_ROOT")),"inventory-child-",PaymentArchiveStore.attributes(true));
            Path stdout=base.resolve("stdout.json"),stderr=base.resolve("stderr.log"),loads=base.resolve("classes.log");
            var command=new ArrayList<String>(List.of(Path.of(System.getProperty("java.home"),"bin","java").toString(),
                    "-Dlog4j.configurationFile="+Path.of("target/classes/payment-evidence-log4j2.xml").toAbsolutePath().toUri(),
                    "-Xlog:class+init=info:file="+loads.getFileName(),"-cp",System.getProperty("surefire.test.class.path",System.getProperty("java.class.path"))));
            boolean production=mode.startsWith("production-");command.add(production?PaymentLegacyInventoryCli.class.getName():CliBridge.class.getName());
            if(production){
                String[] request=args();
                if(mode.equals("production-invalid-args")){request[0]="*";}
                if(mode.equals("production-empty-tail")){
                    // 从实际只读成功报告取合法尾游标；生产主入口应在建连前完成空页返回。
                    request[2]=PaymentLegacyInventoryCli.run(args(),reader()).get("nextCursor").toString();
                }
                command.addAll(List.of(request));
            }else{command.addAll(List.of(suffix,mode,args()[0]));}
            // 类初始化日志使用子进程私有工作目录；classpath中的所有路径先绝对化。
            int cp=command.indexOf("-cp")+1;command.set(cp,java.util.Arrays.stream(command.get(cp).split(java.io.File.pathSeparator)).map(path->Path.of(path).toAbsolutePath().toString()).collect(java.util.stream.Collectors.joining(java.io.File.pathSeparator)));
            var builder=new ProcessBuilder(command).directory(base.toFile()).redirectOutput(stdout.toFile()).redirectError(stderr.toFile());
            if(production){builder.environment().put("S2_DB_CONFIG",base.resolve("not-present.properties").toString());}
            var before=bytes();Process process=builder.start();
            try {
                Assertions.assertTrue(process.waitFor(25,TimeUnit.SECONDS));Assertions.assertEquals(exit,process.exitValue(),"private child logs retained at "+base);
                String text=Files.readString(stdout);JsonNodeResult.assertResult(text,exit);Assertions.assertFalse(text.contains(sdk));Assertions.assertFalse(text.contains(owner));Assertions.assertTrue(text.length()<12000);
                String initialized=Files.readString(loads);
                Assertions.assertFalse(initialized.contains("Initializing 'cn/game/games/cache/entity/Player'"));
                Assertions.assertFalse(initialized.contains("Initializing 'cn/game/games/net/game/module/player/PlayerModule'"));
                Assertions.assertEquals(before,bytes());
            } finally {if(process.isAlive()){process.destroyForcibly();Assertions.assertTrue(process.waitFor(5,TimeUnit.SECONDS));}}
        }
        /** @param loginDb 库 @param table 本轮创建且comment匹配的精确表 */
        void drop(boolean loginDb,String table)throws Exception {
            Assertions.assertEquals(loginDb,created.get(table));
            try(var c=connect(loginDb);var s=c.prepareStatement("SELECT table_comment FROM information_schema.tables WHERE table_schema=DATABASE() AND table_name=?")){s.setString(1,table);try(var r=s.executeQuery()){Assertions.assertTrue(r.next());Assertions.assertEquals(owner,r.getString(1));}}
            execute(loginDb,"DROP TABLE "+table);created.remove(table);
        }
        @Override public void close()throws Exception {
            for(String table:List.copyOf(created.keySet())){drop(created.get(table),table);}
            for(boolean loginDb:List.of(false,true)){try(var c=connect(loginDb);var s=c.prepareStatement("SELECT COUNT(*) FROM information_schema.tables WHERE table_schema=DATABASE() AND table_name IN (?,?,?,?)")){s.setString(1,game);s.setString(2,login);s.setString(3,claim);s.setString(4,receipt);try(var r=s.executeQuery()){Assertions.assertTrue(r.next());Assertions.assertEquals(0,r.getInt(1));}}}
            log.info("PAYMENT_INVENTORY_FIXTURE_COLD_EMPTY suffix={} game=1 login=1 tables=4",suffix);
        }
    }

    /** 子进程输出只包含固定聚合字段，所有真实退出码需与内容一致。 */
    private static final class JsonNodeResult {
        /** @param text 输出JSON @param exit 实际退出码 */
        static void assertResult(String text,int exit)throws Exception {
            var tree=PaymentArchiveEvidence.JSON.readTree(text);
            Assertions.assertEquals(exit==0?"PAGE_COMPLETE":exit==1?"INVALID_INPUT":"PARTIAL_READ",tree.path("status").asText());
            for(String flag:List.of("canDelete","canResend","canBackfill")){Assertions.assertTrue(tree.path(flag).isBoolean());Assertions.assertFalse(tree.path(flag).booleanValue());}
        }
    }
}
