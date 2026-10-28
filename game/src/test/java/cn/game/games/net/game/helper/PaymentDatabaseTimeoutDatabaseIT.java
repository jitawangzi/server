package cn.game.games.net.game.helper;

import java.io.InputStream;
import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import org.mybatis.spring.transaction.SpringManagedTransactionFactory;
import org.springframework.jdbc.datasource.AbstractDataSource;
import cn.game.games.cache.entity.PlayerData;

/** 真实私有Hikari及随机归属表验收；网络断开之外的提交/恢复故障明确为注入，不改全局网络或数据库设置。 */
@Isolated
class PaymentDatabaseTimeoutDatabaseIT {
    /** 真实MySQL慢查询由query timeout取消；与注入提交故障分开记录，不宣称事务回滚。 */
    @Test void slowSqlUsesRealQueryTimeout() throws Exception {
        try(Fixture f=new Fixture()) {
            long start=System.nanoTime();
            try(var c=PaymentDatabaseBoundary.open(f.pool,new PaymentDatabaseBoundary.Limits(600,2000,1000,5000));
                    var s=c.prepareStatement("SELECT SLEEP(3) FROM "+f.table+" WHERE id=? AND owner=?")) {
                s.setLong(1,f.id);s.setString(2,f.owner);Assertions.assertThrows(SQLException.class,s::executeQuery);
            }
            long elapsed=TimeUnit.NANOSECONDS.toMillis(System.nanoTime()-start);
            Assertions.assertTrue(elapsed>=800&&elapsed<4000,"真实查询超时毫秒="+elapsed);
            f.log("REAL_QUERY_TIMEOUT",elapsed);Assertions.assertEquals(0,f.read());f.clean();
        }
    }
    /** 饱和真实Hikari池按既有连接获取上限退出，不为一次调用改池配置。 */
    @Test void saturatedHikariAcquisitionHasFiniteWait() throws Exception {
        try(Fixture f=new Fixture()) {
            try(var one=f.pool.getConnection();var two=f.pool.getConnection()) {
                long start=System.nanoTime();
                Assertions.assertThrows(SQLException.class,()->PaymentDatabaseBoundary.open(f.pool,new PaymentDatabaseBoundary.Limits(600,200,200,2000)));
                long elapsed=TimeUnit.NANOSECONDS.toMillis(System.nanoTime()-start);
                Assertions.assertTrue(elapsed>=200 && elapsed<2500,"真实饱和获取毫秒="+elapsed);
                Assertions.assertEquals(250,f.pool.getConnectionTimeout());
                f.log("SATURATED_POOL",elapsed);
            }
            f.clean();
        }
    }
    /** 真实行锁触发驱动socket超时；两种事务均保守UNKNOWN，失败后不允许后继保存。 */
    @Test void blockedSqlIsUnknownForJdbcAndSpring() throws Exception {
        for(boolean spring:new boolean[]{false,true}) {
            try(Fixture f=new Fixture();var blocker=f.pool.getConnection()) {
                blocker.setAutoCommit(false);
                try(var s=blocker.prepareStatement("SELECT id FROM "+f.table+" WHERE id=? FOR UPDATE")) {
                    s.setLong(1,f.id);try(var r=s.executeQuery()){Assertions.assertTrue(r.next());}
                }
                var writer=f.writer(spring,new PaymentDatabaseBoundary.Limits(300,250,1000,3000));
                var saves=new PlayerSaveCoordinator(2,10,writer::write,(r,delay)->()->{},()->100L);
                Object owner=new Object();saves.register(f.id,owner,()->{});
                long start=System.nanoTime();var first=saves.submit(owner,f.data(),false);
                assertFailed(first.result());
                Assertions.assertEquals("UNKNOWN",saves.status(f.id).state());
                assertFailed(saves.submit(owner,f.data(),false).result());
                long elapsed=TimeUnit.NANOSECONDS.toMillis(System.nanoTime()-start);
                Assertions.assertTrue(elapsed<4000,"真实锁等待毫秒="+elapsed);
                Assertions.assertEquals(0,f.commits.get());Assertions.assertTrue(f.discards.get()>0);
                blocker.rollback(); Assertions.assertEquals(0,f.read());
                f.log(spring?"REAL_SOCKET_LOCK_SPRING":"REAL_SOCKET_LOCK_JDBC",elapsed); f.clean();
            }
        }
    }
    /** 实际SQL已提交后注入确认丢失，冷读虽已更新，协调器仍保留UNKNOWN和后继写入隔离。 */
    @Test void lostCommitAcknowledgementNeverAllowsReplay() throws Exception {
        for(boolean spring:new boolean[]{false,true}) {
            try(Fixture f=new Fixture()) {
                f.loseAck=true;
                var writer=f.writer(spring,new PaymentDatabaseBoundary.Limits(300,500,500,3000));
                var saves=new PlayerSaveCoordinator(2,10,writer::write,(r,delay)->()->{},()->100L);
                Object owner=new Object();saves.register(f.id,owner,()->{});
                assertFailed(saves.submit(owner,f.data(),false).result());
                Assertions.assertEquals("UNKNOWN",saves.status(f.id).state());Assertions.assertEquals(42,f.read());
                assertFailed(saves.submit(owner,f.data(),false).result());Assertions.assertEquals(1,f.commits.get());
                f.log("INJECTED_COMMIT_ACK_LOST_"+spring,0); f.clean();
            }
        }
    }
    /** 真实连接提交前暂停超过截止点，写入器不能提前完成；迟到真实提交后仍UNKNOWN，不启动第二个写入。 */
    @Test void lateCommitKeepsOwnershipUntilActualExit() throws Exception {
        for(boolean spring:new boolean[]{false,true}) {
            try(Fixture f=new Fixture();var executor=java.util.concurrent.Executors.newVirtualThreadPerTaskExecutor()) {
                f.holdCommit=true;
                var writer=new PlayerSnapshotWriter(()->f.factory(spring),executor,()->new PaymentDatabaseBoundary.Limits(300,300,300,600));
                var actual=writer.write(f.data(),false);
                Assertions.assertTrue(f.entered.await(3,TimeUnit.SECONDS));Thread.sleep(700);
                Assertions.assertFalse(actual.isComplete());Assertions.assertEquals(0,f.discards.get());
                f.release.countDown();
                var result=actual.toCompletionStage().toCompletableFuture().get(3,TimeUnit.SECONDS);
                Assertions.assertEquals(PlayerSaveCoordinator.Outcome.UNKNOWN,result.outcome());Assertions.assertEquals(42,f.read());
                Assertions.assertEquals(1,f.commits.get()); f.log("INJECTED_LATE_REAL_COMMIT_"+spring,700); f.clean();
            }
        }
    }
    /** 实际fixture连接被注入不支持网络预算，两种路径都必须在写入和提交前失败。 */
    @Test void unsupportedNetworkTimeoutFailsBeforeEitherMapper() throws Exception {
        try(Fixture f=new Fixture()) {
            f.unsupported=true;
            for(boolean spring:new boolean[]{false,true}) {
                var result=f.writer(spring,new PaymentDatabaseBoundary.Limits(300,300,300,2000)).write(f.data(),false).result();
                Assertions.assertEquals(PlayerSaveCoordinator.Outcome.NOT_WRITTEN,result.outcome());
            }
            Assertions.assertEquals(0,f.commits.get()); Assertions.assertEquals(0,f.writes.get());Assertions.assertEquals(0,f.read());
            f.log("INJECTED_UNSUPPORTED_NETWORK",0);f.clean();
        }
    }
    /** 原始自动提交/隔离/只读/网络状态在两条真实事务路径恢复；注入恢复失败时物理连接被废弃。 */
    @Test void springAndJdbcRestoreOrDiscardOwnedConnection() throws Exception {
        for(boolean spring:new boolean[]{false,true}) {
            try(Fixture f=new Fixture()) {
                var writer=f.writer(spring,new PaymentDatabaseBoundary.Limits(300,500,500,3000));
                Assertions.assertEquals(PlayerSaveCoordinator.Outcome.COMMITTED,writer.write(f.data(),false).result().outcome());
                Assertions.assertTrue(f.restored.get()>0);Assertions.assertEquals(0,f.discards.get());
                f.failRestore=true;
                // 已确认commit不被资源恢复失败抹除，但原连接永久废弃，不能回池重用。
                Assertions.assertEquals(PlayerSaveCoordinator.Outcome.COMMITTED,writer.write(f.data(),false).result().outcome());
                Assertions.assertTrue(f.discards.get()>0);Assertions.assertEquals(42,f.read());
                Assertions.assertFalse(org.springframework.transaction.support.TransactionSynchronizationManager.hasResource(f));
                f.log("INJECTED_RESTORE_FAILURE_"+spring,0);f.clean();
            }
        }
    }

    /** @param future 实际异步完成通知；等待真实失败，不能在回调投递前检查failed标志 */
    private static void assertFailed(io.vertx.core.Future<?> future) {
        Assertions.assertThrows(java.util.concurrent.ExecutionException.class,()->future.toCompletionStage().toCompletableFuture().get(5,TimeUnit.SECONDS));
    }

    /** 每实例持有随机32位后缀、唯一主键及所有权注释；仅精确清理本轮行和表。 */
    static final class Fixture extends AbstractDataSource implements PaymentDatabaseBoundary.Source,AutoCloseable {
        /** 本轮唯一标识及源表；不接触生产命名的新表或业务账号。 */
        final String suffix=UUID.randomUUID().toString().replace("-",""),table="s3e_timeout_"+suffix,owner="payment-timeout:"+suffix;
        final long id=(UUID.randomUUID().getMostSignificantBits()&0x1fffffffffffffffL)|0x4000000000000000L;
        /** 本场景私有池及操作轨迹。 */
        final HikariDataSource pool;
        final AtomicInteger commits=new AtomicInteger(),discards=new AtomicInteger(),restored=new AtomicInteger(),writes=new AtomicInteger();
        final java.util.Map<Connection,Connection> issued=new java.util.IdentityHashMap<>();
        final CountDownLatch entered=new CountDownLatch(1),release=new CountDownLatch(1);
        boolean loseAck,holdCommit,unsupported,failRestore,cleaned;
        /** @throws Exception 本轮私有配置或建表失败，不能跳过 */
        Fixture() throws Exception {
            Properties config=new Properties();try(InputStream in=Files.newInputStream(Path.of(System.getenv("S2_DB_CONFIG")))){config.load(in);}
            Properties properties=new Properties();properties.setProperty("connectTimeout","2000");properties.setProperty("socketTimeout","2000");properties.setProperty("autoReconnect","false");
            String url=config.getProperty("dburl");PaymentDatabaseBoundary.validateUrl(url,properties,2000);
            HikariConfig h=new HikariConfig();h.setJdbcUrl(url);h.setUsername(config.getProperty("dbuser"));h.setPassword(config.getProperty("dbpass"));
            h.setDataSourceProperties(properties);h.setMaximumPoolSize(2);h.setMinimumIdle(0);h.setConnectionTimeout(250);h.setValidationTimeout(250);h.setInitializationFailTimeout(1000);
            pool=new HikariDataSource(h);
            try(var c=pool.getConnection();var s=c.createStatement()) {
                s.setQueryTimeout(2);s.execute("CREATE TABLE "+table+" (id BIGINT PRIMARY KEY,owner CHAR(48) NOT NULL,value INT NOT NULL) ENGINE=InnoDB COMMENT='"+owner+"'");
                try(var insert=c.prepareStatement("INSERT INTO "+table+" VALUES(?,?,0)")){insert.setLong(1,id);insert.setString(2,owner);Assertions.assertEquals(1,insert.executeUpdate());}
            }
            org.slf4j.LoggerFactory.getLogger(getClass()).info("PAYMENT_TIMEOUT_FIXTURE_OPENED suffix={}",suffix);
        }
        @Override public boolean mysqlDriver(){return true;}
        @Override public int acquisitionMillis(){return 250;}
        @Override public Connection getConnection(String user,String pass)throws SQLException{throw new SQLException("不接受替代身份");}
        @Override public Connection getConnection()throws SQLException {
            Connection raw=pool.getConnection();int oldNetwork=raw.getNetworkTimeout(),oldIsolation=raw.getTransactionIsolation();
            boolean oldAuto=raw.getAutoCommit(),oldReadOnly=raw.isReadOnly();
            Connection c=(Connection)Proxy.newProxyInstance(Connection.class.getClassLoader(),new Class<?>[]{Connection.class},(p,m,a)->{
                if(m.getName().equals("setNetworkTimeout")&&unsupported){throw new java.sql.SQLFeatureNotSupportedException("注入不支持");}
                if(m.getName().equals("setReadOnly")&&failRestore&&commits.get()>0){throw new SQLException("注入恢复失败");}
                if(m.getName().equals("prepareStatement")&&((String)a[0]).startsWith("UPDATE ")){writes.incrementAndGet();}
                if(m.getName().equals("commit")&&holdCommit){entered.countDown();Assertions.assertTrue(release.await(4,TimeUnit.SECONDS));}
                if(m.getName().equals("close")) {
                    Assertions.assertEquals(oldNetwork,raw.getNetworkTimeout());Assertions.assertEquals(oldIsolation,raw.getTransactionIsolation());
                    Assertions.assertEquals(oldAuto,raw.getAutoCommit());Assertions.assertEquals(oldReadOnly,raw.isReadOnly());restored.incrementAndGet();
                }
                Object result;
                try{result=m.invoke(raw,a);}catch(InvocationTargetException failure){org.slf4j.LoggerFactory.getLogger(getClass()).warn("实际fixture JDBC调用失败 method={}",m.getName(),failure.getCause());throw failure.getCause();}
                if(m.getName().equals("commit")){commits.incrementAndGet();if(loseAck){throw new SQLException("注入真实commit确认丢失");}}
                return result;
            });
            issued.put(c,raw);return c;
        }
        @Override public void discard(Connection c)throws SQLException {
            Connection raw=issued.remove(c);Assertions.assertNotNull(raw);
            if(!raw.isClosed()){var physical=raw.unwrap(com.mysql.cj.jdbc.JdbcConnection.class);pool.evictConnection(raw);physical.abortInternal();}else{pool.evictConnection(raw);}
            discards.incrementAndGet();
        }
        /** @param spring 事务路由 @param limits 毫秒预算 @return 同步测试写入器 */
        PlayerSnapshotWriter writer(boolean spring,PaymentDatabaseBoundary.Limits limits){return new PlayerSnapshotWriter(()->factory(spring),Runnable::run,()->limits);}
        /** @param spring 事务路由 @return 真实MyBatis工厂，只有表名替换为本轮归属表 */
        SqlSessionFactory factory(boolean spring) {
            Configuration config=new Configuration(new Environment("timeout-owned",spring?new SpringManagedTransactionFactory():new JdbcTransactionFactory(),this));
            String xml="<?xml version='1.0' encoding='UTF-8'?><!DOCTYPE mapper PUBLIC '-//mybatis.org//DTD Mapper 3.0//EN' 'http://mybatis.org/dtd/mybatis-3-mapper.dtd'><mapper namespace='cn.game.games.net.data.mapper.PlayerDataMapper'><update id='updateByPrimaryKey'>UPDATE "+table+" SET value=#{exp} WHERE id=#{playerId}</update></mapper>";
            new XMLMapperBuilder(new StringReader(xml),config,"timeout-owned",config.getSqlFragments()).parse();return new SqlSessionFactoryBuilder().build(config);
        }
        /** @return 本轮专用参数 */
        PlayerData data(){PlayerData d=new PlayerData();d.setPlayerId(id);d.setExp(42);return d;}
        /** @return 新连接按主键与归属冷读 */
        int read()throws SQLException {
            try(var c=pool.getConnection();var s=c.prepareStatement("SELECT value FROM "+table+" WHERE id=? AND owner=?")) {
                s.setQueryTimeout(2);s.setLong(1,id);s.setString(2,owner);try(var r=s.executeQuery()){Assertions.assertTrue(r.next());return r.getInt(1);}
            }
        }
        /** @param kind 明确真实或注入证据类别 @param elapsed 实测毫秒 */
        void log(String kind,long elapsed){org.slf4j.LoggerFactory.getLogger(getClass()).info("PAYMENT_TIMEOUT_MEASURED kind={} elapsedMillis={} suffix={}",kind,elapsed,suffix);}
        /** @throws SQLException 精确归属验证及清理失败 */
        void clean()throws SQLException {
            try(var c=pool.getConnection();var check=c.prepareStatement("SELECT table_comment FROM information_schema.tables WHERE table_schema=DATABASE() AND table_name=?")) {
                check.setString(1,table);try(var r=check.executeQuery()){Assertions.assertTrue(r.next());Assertions.assertEquals(owner,r.getString(1));}
                try(var s=c.prepareStatement("DELETE FROM "+table+" WHERE id=? AND owner=?")){s.setLong(1,id);s.setString(2,owner);Assertions.assertEquals(1,s.executeUpdate());}
            }
            try(var c=pool.getConnection();var s=c.createStatement();var r=s.executeQuery("SELECT COUNT(*) FROM "+table)){Assertions.assertTrue(r.next());Assertions.assertEquals(0,r.getInt(1));}
            try(var c=pool.getConnection();var s=c.createStatement()){s.execute("DROP TABLE "+table);}
            try(var c=pool.getConnection();var s=c.prepareStatement("SELECT COUNT(*) FROM information_schema.tables WHERE table_schema=DATABASE() AND table_name=?")){s.setString(1,table);try(var r=s.executeQuery()){Assertions.assertTrue(r.next());Assertions.assertEquals(0,r.getInt(1));}}
            cleaned=true;org.slf4j.LoggerFactory.getLogger(getClass()).info("PAYMENT_TIMEOUT_FIXTURE_COLD_EMPTY suffix={}",suffix);
        }
        @Override public void close(){release.countDown();pool.close();if(!cleaned){org.slf4j.LoggerFactory.getLogger(getClass()).warn("PAYMENT_TIMEOUT_FIXTURE_RETAINED suffix={}",suffix);}}
    }
}
