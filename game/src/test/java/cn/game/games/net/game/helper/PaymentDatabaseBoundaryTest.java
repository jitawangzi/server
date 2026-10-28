package cn.game.games.net.game.helper;

import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLTimeoutException;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.AbstractDataSource;

/** 独立验证连接预算、超期真实结果及恢复核对；故障连接不伪装真实网络验收。 */
class PaymentDatabaseBoundaryTest {
    /** 零、无限、负数及矛盾预算必须在获取连接前拒绝。 */
    @Test void invalidBudgetsFailBeforeAcquisition() {
        for (int[] values : new int[][]{{0,1,1,2},{1,0,1,2},{1,1,-1,2},{2,1,1,2},{1,1,1,300001}}) {
            Assertions.assertThrows(IllegalArgumentException.class, () -> new PaymentDatabaseBoundary.Limits(values[0],values[1],values[2],values[3]));
        }
    }
    /** 本地显式配置覆盖默认，非法文本不能回退成看似有限的默认值。 */
    @Test void localConfigurationPrecedenceIsStrict() {
        String name = "payment.db.networkMillis", old = System.getProperty(name);
        try {
            System.setProperty(name,"1234"); Assertions.assertEquals(1234,PaymentDatabaseBoundary.Limits.configured().network());
            for (String value : new String[]{"0","-1"," 10","unlimited","9999999999"}) {
                System.setProperty(name,value); Assertions.assertThrows(IllegalArgumentException.class,PaymentDatabaseBoundary.Limits::configured);
            }
        } finally { if(old==null){System.clearProperty(name);}else{System.setProperty(name,old);} }
    }
    /** 驱动URL优先级须按本地Connector/J实际解析结果验证，禁止改写URL掩盖无限配置。 */
    @Test void urlTimeoutPrecedenceNeverSilentlyOverridesUrl() throws Exception {
        Properties properties = new Properties(); properties.setProperty("connectTimeout","2000"); properties.setProperty("socketTimeout","2000");
        PaymentDatabaseBoundary.validateUrl("jdbc:mysql://localhost/test",properties,2000);
        for (String tail : new String[]{"socketTimeout=0","connectTimeout=3000","autoReconnect=true","socketFactory=custom","propertiesTransform=custom"}) {
            Assertions.assertThrows(SQLException.class,()->PaymentDatabaseBoundary.validateUrl("jdbc:mysql://localhost/test?"+tail,properties,2000));
        }
        Assertions.assertThrows(SQLException.class,()->PaymentDatabaseBoundary.validateUrl("jdbc:mysql://host1,host2/test",properties,2000));
        Assertions.assertThrows(SQLException.class,()->PaymentDatabaseBoundary.validateUrl("jdbc:mysql:loadbalance://localhost/test",properties,2000));
    }
    /** 未知数据源不能仅凭getConnection存在就宣称有限获取。 */
    @Test void unknownSourceFailsClosedWithoutCallingDriver() {
        var source = new AbstractDataSource() {
            @Override public Connection getConnection() { throw new AssertionError("不能调用未知获取路径"); }
            @Override public Connection getConnection(String user,String pass) { throw new AssertionError(); }
        };
        Assertions.assertThrows(SQLException.class,()->PaymentDatabaseBoundary.open(source,limits()));
    }
    /** 不支持网络超时的驱动在任何SQL前被废弃。 */
    @Test void unsupportedNetworkTimeoutDiscardsBeforeSql() {
        Fake source = new Fake(); source.unsupported = true;
        Assertions.assertThrows(SQLException.class,()->PaymentDatabaseBoundary.open(source,limits()));
        Assertions.assertEquals(1,source.discards); Assertions.assertEquals(0,source.executes); Assertions.assertEquals(0,source.closes);
    }
    /** 驱动忽略网络设置同样不具备支持能力，不能只检查没有抛异常。 */
    @Test void ignoredNetworkTimeoutFailsClosed() {
        Fake source = new Fake(); source.ignoreNetwork = true;
        Assertions.assertThrows(SQLException.class,()->PaymentDatabaseBoundary.open(source,limits())); Assertions.assertEquals(1,source.discards);
    }
    /** 私有连接使用后必须恢复并核对原始网络、隔离级别、只读与自动提交状态。 */
    @Test void successfulConnectionRestoresAllOriginalState() throws Exception {
        Fake source = new Fake();
        try(var c=PaymentDatabaseBoundary.open(source,new PaymentDatabaseBoundary.Limits(100,5000,5000,6000))) {
            try(var s=c.prepareStatement("SELECT 1")){s.setQueryTimeout(2);s.execute();Assertions.assertEquals(2,source.statementTimeout);}
            c.setAutoCommit(false); c.setReadOnly(true); c.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE); c.commit();
        }
        Assertions.assertEquals(1,source.closes); Assertions.assertEquals(0,source.discards);
        Assertions.assertEquals(37,source.network); Assertions.assertTrue(source.autoCommit); Assertions.assertFalse(source.readOnly);
        Assertions.assertEquals(Connection.TRANSACTION_READ_COMMITTED,source.isolation);
    }
    /** 恢复抛错或无效均须废弃，不能向池归还带有 altered 状态的连接。 */
    @Test void restoreFailuresNeverRecycleConnection() throws Exception {
        for(boolean ignored : new boolean[]{false,true}) {
            Fake source=new Fake();
            var c=PaymentDatabaseBoundary.open(source,limits()); c.setReadOnly(true); source.failRestore=!ignored; source.ignoreRestore=ignored;
            Assertions.assertThrows(SQLException.class,c::close); Assertions.assertEquals(1,source.discards); Assertions.assertEquals(0,source.closes);
        }
    }
    /** 未结束事务直接废弃，不以setAutoCommit(true)触发意外提交。 */
    @Test void unfinishedTransactionIsDiscardedWithoutAutoCommitRestore() throws Exception {
        Fake source=new Fake(); try(var c=PaymentDatabaseBoundary.open(source,limits())) { c.setAutoCommit(false); }
        Assertions.assertEquals(1,source.discards); Assertions.assertFalse(source.autoCommit); Assertions.assertEquals(0,source.commits);
    }
    /** 同一截止点跨多条SQL及结构检查，不为下一语句重建预算。 */
    @Test void remainingBudgetIsNotResetPerStatement() throws Exception {
        Fake source=new Fake();
        try(var c=PaymentDatabaseBoundary.open(source,new PaymentDatabaseBoundary.Limits(1,200,200,350))) {
            Thread.sleep(240);
            try(var s=c.prepareStatement("SELECT 1")){ s.execute(); }
            Assertions.assertTrue(source.network<=110);
            Thread.sleep(150);
            Assertions.assertThrows(SQLTimeoutException.class,()->c.prepareStatement("SELECT 2"));
        }
        Assertions.assertEquals(1,source.executes); Assertions.assertEquals(1,source.discards);
    }
    /** 提交入口后的异常及迟到确认都不能被预算当作回滚证明。 */
    @Test void lateCommitIsAwaitedAndReportedUnknown() throws Exception {
        Fake source=new Fake(); source.commitEntered=new CountDownLatch(1); source.commitRelease=new CountDownLatch(1);
        try(var executor=java.util.concurrent.Executors.newVirtualThreadPerTaskExecutor()) {
            var task=executor.submit(()->{
                try(var c=PaymentDatabaseBoundary.open(source,new PaymentDatabaseBoundary.Limits(1,100,100,200))) { c.setAutoCommit(false); c.commit(); }
                return true;
            });
            Assertions.assertTrue(source.commitEntered.await(2,TimeUnit.SECONDS)); Thread.sleep(250);
            Assertions.assertFalse(task.isDone()); Assertions.assertEquals(0,source.discards);
            source.commitRelease.countDown();
            var failure=Assertions.assertThrows(java.util.concurrent.ExecutionException.class,()->task.get(2,TimeUnit.SECONDS));
            Assertions.assertInstanceOf(SQLTimeoutException.class,failure.getCause()); Assertions.assertEquals(1,source.commits);
            Assertions.assertEquals(1,source.discards);
        } finally { source.commitRelease.countDown(); }
    }
    /** 直接诊断起点跨连接复用，耗尽后不启动第二次获取。 */
    @Test void sharedDiagnosticDeadlineRejectsNextConnection() {
        Fake source=new Fake(); long old=System.nanoTime()-TimeUnit.SECONDS.toNanos(1);
        Assertions.assertThrows(SQLTimeoutException.class,()->PaymentDatabaseBoundary.open(source,new PaymentDatabaseBoundary.Limits(100,100,100,200),old));
        Assertions.assertEquals(0,source.acquisitions);
    }
    /** MyBatis/Spring失败收尾不得用自动提交恢复把未决事务隐式提交。 */
    @Test void failedSqlCannotImplicitlyCommitDuringCleanup() throws Exception {
        Fake source=new Fake();source.failExecute=true;
        try(var c=PaymentDatabaseBoundary.open(source,limits())) {
            c.setAutoCommit(false);
            try(var s=c.prepareStatement("UPDATE owned SET value=1")){Assertions.assertThrows(SQLException.class,s::execute);}
            Assertions.assertThrows(SQLException.class,()->c.setAutoCommit(true));Assertions.assertFalse(source.autoCommit);
        }
        Assertions.assertEquals(1,source.discards);Assertions.assertEquals(0,source.commits);
    }
    /** 多条schema元数据SELECT共用连接预算，而不是每条重新获得完整期限。 */
    @Test void schemaMetadataConsumesOneTotalBudget() throws Exception {
        Fake source=new Fake();source.queryDelay=40;
        try(var c=PaymentDatabaseBoundary.open(source,new PaymentDatabaseBoundary.Limits(1,100,100,180))) {
            Assertions.assertThrows(SQLTimeoutException.class,()->PaymentDedupStore.inspectSchema(c));
        }
        Assertions.assertTrue(source.executes>1&&source.executes<11);Assertions.assertEquals(1,source.discards);
    }
    /** 首次Hikari同步初始化不受connectionTimeout单独约束，必须先由启动流程完成池初始化。 */
    @Test void unstartedPoolIsActionablePreExecutionFailure() {
        try(var pool=new com.zaxxer.hikari.HikariDataSource()) {
            var failure=Assertions.assertThrows(SQLException.class,()->PaymentDatabaseBoundary.open(pool,limits()));
            Assertions.assertEquals("PAYMENT_DB_POOL_NOT_STARTED",failure.getMessage());
        }
    }
    /** @return 小于默认的明确测试预算 */
    private static PaymentDatabaseBoundary.Limits limits(){return new PaymentDatabaseBoundary.Limits(100,500,500,2000);}

    /** 只模拟状态与故障，明确声明有限获取和永久废弃能力。 */
    static final class Fake extends AbstractDataSource implements PaymentDatabaseBoundary.Source {
        /** 原始连接状态与轨迹。 */
        int network=37,isolation=Connection.TRANSACTION_READ_COMMITTED,discards,closes,commits,executes,acquisitions,queryDelay,statementTimeout;
        boolean autoCommit=true,readOnly,unsupported,ignoreNetwork,failRestore,ignoreRestore,failExecute;
        /** 控制实际提交退出，超时不自动放行。 */
        CountDownLatch commitEntered,commitRelease;
        @Override public int acquisitionMillis(){return 1;}
        @Override public void discard(Connection c){discards++;}
        @Override public Connection getConnection(String user,String pass){return getConnection();}
        @Override public Connection getConnection(){
            acquisitions++;
            return (Connection)Proxy.newProxyInstance(Connection.class.getClassLoader(),new Class<?>[]{Connection.class},(p,m,a)->switch(m.getName()){
                case "getNetworkTimeout"->network;
                case "setNetworkTimeout"->{if(unsupported){throw new java.sql.SQLFeatureNotSupportedException();}if(!ignoreNetwork){network=(int)a[1];}yield null;}
                case "getAutoCommit"->autoCommit;
                case "setAutoCommit"->{autoCommit=(boolean)a[0];yield null;}
                case "isReadOnly"->readOnly;
                case "setReadOnly"->{if(failRestore){throw new SQLException("restore injected");}if(!ignoreRestore){readOnly=(boolean)a[0];}yield null;}
                case "getTransactionIsolation"->isolation;
                case "setTransactionIsolation"->{isolation=(int)a[0];yield null;}
                case "commit"->{if(commitEntered!=null){commitEntered.countDown();Assertions.assertTrue(commitRelease.await(3,TimeUnit.SECONDS));}commits++;yield null;}
                case "rollback"->null;
                case "close"->{closes++;yield null;}
                case "prepareStatement"->Proxy.newProxyInstance(java.sql.PreparedStatement.class.getClassLoader(),new Class<?>[]{java.sql.PreparedStatement.class},(s,op,params)->switch(op.getName()){
                    case "execute","executeQuery"->{
                        executes++;if(failExecute){throw new SQLException("注入语句失败");}if(queryDelay>0){Thread.sleep(queryDelay);}
                        yield op.getName().equals("execute")?false:Proxy.newProxyInstance(java.sql.ResultSet.class.getClassLoader(),new Class<?>[]{java.sql.ResultSet.class},(r,read,values)->switch(read.getName()){
                            case "next"->false;
                            case "close"->null;
                            default->throw new AssertionError(read.getName());
                        });
                    }
                    case "setQueryTimeout"->{statementTimeout=(int)params[0];yield null;}
                    case "getQueryTimeout"->statementTimeout;
                    case "setString","setMaxRows","close"->null;
                    default->throw new AssertionError(op.getName());
                });
                default->throw new AssertionError(m.getName());
            });
        }
    }
}
