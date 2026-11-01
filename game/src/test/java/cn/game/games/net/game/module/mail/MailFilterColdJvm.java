package cn.game.games.net.game.module.mail;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import cn.game.core.execute.TimingMetrics;
import cn.game.core.net.protocol.object.ProtobufProtocol;
import cn.game.games.cache.entity.Mail;
import cn.game.games.net.data.mapper.MailMapper;
import cn.game.protocol.protobuf.MailMsg;
import cn.game.protocol.protobuf.PbProtocol;
import cn.game.util.DateUtil;

/**
 * 两个独立JVM的真实Mail表验收。A创建专用账号和邮件并经loopback查询，退出后B新连接冷读再查。
 * 登录和认证状态为显式测试夹具；真实dispatcher、handler、玩家邮箱、协议编解码和生产MailMapper均不替换。
 */
public final class MailFilterColdJvm {
    /** 私有日志仅输出归属ID和阶段，不输出连接配置。 */
    private static final org.slf4j.Logger log=org.slf4j.LoggerFactory.getLogger(MailFilterColdJvm.class);
    /** 本轮唯一归属清单。 */
    private final Properties state=new Properties();
    /** 自有游戏库连接源，无共享池。 */
    private final DriverManagerDataSource game;
    /** 自有登录库连接源，无共享池。 */
    private final DriverManagerDataSource login;
    /** 仅装载生产MailMapper。 */
    private final SqlSessionFactory factory;
    /** 归属清单文件。 */
    private final Path manifest;

    /** @param manifest 本轮私有目录内的夹具清单 */
    private MailFilterColdJvm(Path manifest) throws Exception {
        this.manifest=manifest; Properties p=new Properties();
        try(InputStream input=Files.newInputStream(Path.of(java.util.Objects.requireNonNull(System.getenv("S2_DB_CONFIG"))))){p.load(input);}
        game=source(p,"");login=source(p,"login.");
        var mapping=new Configuration(new Environment("owned-mail-fixture",new JdbcTransactionFactory(),game));
        mapping.setCacheEnabled(false);
        try(InputStream xml=MailFilterColdJvm.class.getResourceAsStream("/mapping/MailMapper.xml")){
            new XMLMapperBuilder(xml,mapping,"mapping/MailMapper.xml",mapping.getSqlFragments()).parse();
        }
        factory=new SqlSessionFactoryBuilder().build(mapping);
        if(Files.exists(manifest)){try(InputStream input=Files.newInputStream(manifest)){state.load(input);}}
    }

    /** @param args A、B或Cleanup，以及本轮独有清单路径 */
    public static void main(String[] args) throws Exception {
        var fixture=new MailFilterColdJvm(Path.of(args[1]));
        if(args[0].equals("A")){
            fixture.seed(); fixture.exercise("A");
        } else if(args[0].equals("B")) {
            try {fixture.exercise("B");} finally {fixture.cleanup();}
        } else if(args[0].equals("Cleanup")) {fixture.cleanup();}
        else {throw new IllegalArgumentException("phase");}
    }

    /** @param p 私有Properties @param prefix 游戏或登录前缀 @return 有限超时且不自动重连的独有数据源 */
    private static DriverManagerDataSource source(Properties p,String prefix){
        var source=new DriverManagerDataSource();source.setDriverClassName(p.getProperty(prefix+"driverClassName"));
        source.setUrl(p.getProperty(prefix+"dburl"));source.setUsername(p.getProperty(prefix+"dbuser"));source.setPassword(p.getProperty(prefix+"dbpass"));
        var limits=new Properties();limits.setProperty("connectTimeout","3000");limits.setProperty("socketTimeout","15000");limits.setProperty("autoReconnect","false");source.setConnectionProperties(limits);return source;
    }

    /** @return 主玩家ID */
    private long owner(){return Long.parseLong(state.getProperty("owner"));}
    /** @return 隔离对照玩家ID */
    private long peer(){return owner()+1;}
    /** @return 每封邮件同时写入的归属标记 */
    private String marker(){return state.getProperty("marker");}
    /** @return 邮件ID连续专用区间的起点 */
    private long base(){return Long.parseLong(state.getProperty("base"));}

    /** 只做INSERT，占有失败不复用任何现有行；清单在首次写入前落盘供失败清理核对归属。 */
    private void seed() throws Exception {
        if(!state.isEmpty()){throw new AssertionError("manifest already used");}
        String suffix=UUID.randomUUID().toString().replace("-","");
        long base=8000000000000000L+Math.floorMod(UUID.randomUUID().getMostSignificantBits(),1000000000000000L);
        state.setProperty("marker","mf"+suffix);state.setProperty("base",""+base);state.setProperty("owner",""+(base+100));
        try(var output=Files.newOutputStream(manifest,java.nio.file.StandardOpenOption.CREATE_NEW)){state.store(output,"本轮邮件夹具归属，不含凭据");}
        try(var connection=login.getConnection()){
            connection.setAutoCommit(false);
            for(long id:List.of(owner(),peer())){
                try(var statement=connection.prepareStatement("INSERT INTO t_user (id,username,pass,is_gm,user_type,channel_code,channel_label,third_uid,session_key,sub_channel_code,create_date,create_time,login_date,login_time,device_uid,servers) VALUES (?,?,?,0,1,'mail-fixture','mail-fixture',?,'','',?,?,?,?,'','{}')")){
                    statement.setLong(1,id);statement.setString(2,marker()+ (id==owner()?"a":"b"));statement.setString(3,"disabled-fixture");statement.setString(4,marker());
                    statement.setString(5,DateUtil.nowDateStr());statement.setString(6,DateUtil.nowTimeStr());statement.setString(7,DateUtil.nowDateStr());statement.setString(8,DateUtil.nowTimeStr());
                    require(statement.executeUpdate()==1,"account insert");
                }
            }
            connection.commit();
        }
        try(var session=factory.openSession(false)){
            var mapper=session.getMapper(MailMapper.class);int now=DateUtil.currentTimeSeconds();
            for(int i=1;i<=14;i++){
                Mail mail=MailFilterFixture.mail(base+i,owner(),now-100);mail.setSender(marker());mail.setTitle(marker());
                if(List.of(1,4,5,10,11,14).contains(i)){mail.setAttachment("[{\"id\":100001,\"count\":1}]");}
                if(i==2 || i==9){mail.setMailId(901);} if(i==6){mail.setMailId(902);}if(i==7){mail.setMailId(903);}if(i==8){mail.setMailId(904);}
                if(i==12){mail.setMailId(905);}if(i==13 || i==14){mail.setMailId(906);}
                if(i==4){mail.setReceive(true);mail.setSee(true);}if(i==5){mail.setSee(true);}if(i==9){mail.setAttachment("{");}
                if(i==10){mail.setIsDeleted(true);}if(i==11){mail.setCreateTime(now+3600);}
                require(mapper.insert(mail)==1,"mail insert");
            }
            Mail other=MailFilterFixture.mail(base+20,peer(),now-100);other.setSender(marker());other.setTitle(marker());other.setAttachment("[{\"id\":100001,\"count\":1}]");
            require(mapper.insert(other)==1,"peer mail insert");session.commit();
        }
        log.info("MAIL_FIXTURE_SEEDED marker={} owner={} peer={}",marker(),owner(),peer());
    }

    /** @param phase A本轮准备后或B独立进程冷读 */
    private void exercise(String phase) throws Exception {
        var before=bytes();
        try(var primary=new MailFilterFixture(owner());var other=new MailFilterFixture(peer());var route=new MailFilterRoute()){
            primary.config(901,2,0,"100001;1");primary.config(902,2,10,"100001;1");primary.config(903,1,0,"100001;1");
            primary.config(905,2,0,"100001");primary.config(906,2,0,"");
            try(var session=factory.openSession()){
                var mapper=session.getMapper(MailMapper.class);var own=mapper.selectByPlayerId(owner());var foreign=mapper.selectByPlayerId(peer());
                require(own.size()==14 && foreign.size()==1,"cold mapper count");
                for(var mail:own){require(mail.getSender().equals(marker()),"cold mapper ownership");}
                primary.load(own);other.load(foreign);
            }
            int moduleCount=primary.player.getModules().size();
            primary.player.registerEventHandler(cn.game.games.core.event.EventTypeEnum.Charge,event->{throw new AssertionError("query fired Charge");});
            TimingMetrics metrics=TimingMetrics.reset(true);
            try(var loop=new Loopback(route,primary,other,marker())){
                @SuppressWarnings("unchecked")
                Map<MailMsg.MailListFilter,List<MailMsg.MailInfo>> result=(Map<MailMsg.MailListFilter,List<MailMsg.MailInfo>>)
                        Class.forName("cn.game.simulation.test.ai.ClientMailFilterTest").getMethod("verify",Function.class)
                        .invoke(null,(Function<MailMsg.MailListRequest_12000001,MailMsg.MailListResponse_12000002>)(request)->{
                            try {var reply=loop.request(0,request);require(reply.error()==0,"loopback response");return (MailMsg.MailListResponse_12000002)reply.message();}
                            catch(Exception failure){log.error("自有loopback邮件请求失败",failure);throw new IllegalStateException(failure);}
                        });
                var all=new ArrayList<Integer>();all.add(11);for(int i=14;i>=1;i--){if(i!=10 && i!=11){all.add(i);}}
                require(ids(result.get(MailMsg.MailListFilter.ALL)).equals(all.stream().map(i->""+(base()+i)).toList()),"exact ALL");
                require(ids(result.get(MailMsg.MailListFilter.UNREAD)).equals(all.stream().filter(i->i!=4 && i!=5).map(i->""+(base()+i)).toList()),"exact UNREAD");
                require(ids(result.get(MailMsg.MailListFilter.HAS_CLAIMABLE_ATTACHMENT)).equals(List.of(""+(base()+14),""+(base()+5),""+(base()+2),""+(base()+1))),"exact CLAIMABLE");
                require(result.get(MailMsg.MailListFilter.ALL).stream().filter(mail->mail.getUid().equals(""+(base()+8))).allMatch(mail->mail.getId()==0),"missing config safe display");
                require(primary.module.get(base()+8).getMailId()==904,"missing config storage preserved");
                var request=MailMsg.MailListRequest_12000001.newBuilder().setFilterValue(0).setPageSize(1).build();
                var first=(MailMsg.MailListResponse_12000002)loop.request(0,request).message();
                Path restartCursor=manifest.resolveSibling("cursor-A.txt");
                if(phase.equals("A")){Files.writeString(restartCursor,first.getNextCursor());}
                else{require(loop.request(0,request.toBuilder().setCursor(Files.readString(restartCursor)).build()).error()==52,"restart cursor must be rejected");}
                require(loop.request(1,request.toBuilder().setCursor(first.getNextCursor()).build()).error()==52,"cross player cursor");
                require(loop.request(0,request.toBuilder().setFilterValue(1).setCursor(first.getNextCursor()).build()).error()==52,"cross filter cursor");
                for(var bad:List.of(request.toBuilder().setFilterValue(99).build(),request.toBuilder().setPageSize(0).build(),request.toBuilder().setPageSize(101).build(),request.toBuilder().setCursor("x".repeat(10000)).build())){
                    require(loop.request(0,bad).error()==52,"invalid query accepted");
                }
                var peerReply=(MailMsg.MailListResponse_12000002)loop.request(1,request).message();
                require(ids(peerReply.getMailsList()).equals(List.of(""+(base()+20))),"peer isolation");
                require(primary.player.getModules().size()==moduleCount && moduleCount==1,"unrelated module init");
                require(primary.client.replies.isEmpty() && other.client.replies.isEmpty(),"unrelated push");
                require(metrics.snapshot().rows().stream().anyMatch(r->r.stage()==TimingMetrics.Stage.REQUEST && r.outcome()==TimingMetrics.Outcome.SUCCESS && r.count()>0),"timing hook");
            } finally {TimingMetrics.reset(false);}
        }
        require(Arrays.equals(before,bytes()),"query changed persistent row bytes");
        Files.writeString(manifest.resolveSibling("phase-"+phase+".json"),"{\"phase\":\""+phase+"\",\"pid\":"+ProcessHandle.current().pid()+",\"all\":13,\"unread\":11,\"claimable\":4,\"rowBytesUnchanged\":true,\"transport\":\"owned-loopback-session-fixture\"}");
        log.info("MAIL_COLD_PHASE_PASSED phase={} rows=15 unchanged=true",phase);
    }

    /** @param rows 响应邮件 @return 响应顺序UID */
    private static List<String> ids(List<MailMsg.MailInfo> rows){return rows.stream().map(MailMsg.MailInfo::getUid).toList();}

    /** @return 新物理连接按实际列字节计算的摘要，覆盖两个自有玩家的全部Mail行 */
    private byte[] bytes() throws Exception {
        var digest=MessageDigest.getInstance("SHA-256");
        try(var connection=game.getConnection();var query=connection.prepareStatement("SELECT * FROM t_mail WHERE player_id IN (?,?) ORDER BY id")){
            query.setLong(1,owner());query.setLong(2,peer());try(var rows=query.executeQuery()){
                while(rows.next()){for(int i=1;i<=rows.getMetaData().getColumnCount();i++){
                    byte[] value=rows.getBytes(i);digest.update(java.nio.ByteBuffer.allocate(4).putInt(value==null?-1:value.length).array());if(value!=null){digest.update(value);}
                }}
            }
        }return digest.digest();
    }

    /** 每次删除都包含唯一ID与归属字段；不按玩家批量删除，不碰支付或其他业务表。 */
    private void cleanup() throws Exception {
        if(state.isEmpty()){throw new AssertionError("no ownership manifest");}
        try(var connection=game.getConnection()){
            connection.setAutoCommit(false);
            var ids=new ArrayList<Long>();for(int i=1;i<=14;i++){ids.add(base()+i);}ids.add(base()+20);
            for(long id:ids){
                try(var query=connection.prepareStatement("SELECT player_id,sender,title FROM t_mail WHERE id=?")){
                    query.setLong(1,id);try(var rows=query.executeQuery()){if(!rows.next()){continue;}
                        require(rows.getLong(1)==(id==base()+20?peer():owner()) && marker().equals(rows.getString(2)) && marker().equals(rows.getString(3)),"mail cleanup ownership");}
                }
                try(var delete=connection.prepareStatement("DELETE FROM t_mail WHERE id=? AND player_id=? AND sender=? AND title=?")){
                    delete.setLong(1,id);delete.setLong(2,id==base()+20?peer():owner());delete.setString(3,marker());delete.setString(4,marker());require(delete.executeUpdate()==1,"exact mail cleanup");
                }
            }connection.commit();
        }
        try(var connection=login.getConnection()){
            connection.setAutoCommit(false);
            for(long id:List.of(owner(),peer())){
                String username=marker()+(id==owner()?"a":"b");
                try(var query=connection.prepareStatement("SELECT username,third_uid FROM t_user WHERE id=?")){
                    query.setLong(1,id);try(var rows=query.executeQuery()){if(!rows.next()){continue;}require(username.equals(rows.getString(1)) && marker().equals(rows.getString(2)),"account cleanup ownership");}
                }
                try(var delete=connection.prepareStatement("DELETE FROM t_user WHERE id=? AND username=? AND third_uid=?")){
                    delete.setLong(1,id);delete.setString(2,username);delete.setString(3,marker());require(delete.executeUpdate()==1,"exact account cleanup");
                }
            }connection.commit();
        }
        for(var source:List.of(game,login)){
            String table=source==game?"t_mail":"t_user";String column=source==game?"player_id":"id";
            try(var connection=source.getConnection();var query=connection.prepareStatement("SELECT COUNT(*) FROM "+table+" WHERE "+column+" IN (?,?)")){
                query.setLong(1,owner());query.setLong(2,peer());try(var rows=query.executeQuery()){require(rows.next() && rows.getLong(1)==0,"fresh connection empty");}
            }
        }
        Files.writeString(manifest.resolveSibling("cold-empty.json"),"{\"fixtureColdEmpty\":true,\"mailRows\":0,\"accounts\":0,\"newConnections\":true}");
        log.info("MAIL_FIXTURE_COLD_EMPTY rows=0 accounts=0");
    }

    /** @param condition 必须满足的断言 @param reason 无秘密的断言位置 */
    private static void require(boolean condition,String reason){if(!condition){throw new AssertionError(reason);}}

    /** 仅监听127.0.0.1的自有端口；一次性令牌选择两个固定会话，不接受客户端玩家ID。 */
    private static final class Loopback implements AutoCloseable {
        /** 自有监听、客户端连接及接收线程。 */
        private final ServerSocket server;
        private final Socket client;
        private final Thread worker;
        /** 单个连接的有界二进制帧读写。 */
        private final DataInputStream input;
        private final DataOutputStream output;
        /** 响应序号严格递增。 */
        private int sequence;
        /** 服务线程故障必须在客户端侧显式失败。 */
        private volatile Throwable failure;

        /** @param route 真实路由 @param primary 自有主会话 @param peer 自有对照会话 @param token 本轮会话凭据 */
        Loopback(MailFilterRoute route,MailFilterFixture primary,MailFilterFixture peer,String token) throws Exception {
            server=new ServerSocket(0,1,InetAddress.getByName("127.0.0.1"));server.setSoTimeout(10000);
            worker=Thread.ofVirtual().name("owned-mail-loopback").start(()->{
                try(var socket=server.accept();var in=new DataInputStream(socket.getInputStream());var out=new DataOutputStream(socket.getOutputStream())){
                    socket.setSoTimeout(15000);require(token.equals(in.readUTF()),"session fixture token");
                    while(true){int slot=in.readInt();if(slot==-1){break;}require(slot==0 || slot==1,"session fixture slot");
                        int id=in.readInt(),seq=in.readInt(),length=in.readInt();require(id==PbProtocol.MailListRequest_12000001 && length>=0 && length<=16384,"frame bounds");
                        byte[] data=in.readNBytes(length);require(data.length==length,"frame truncation");
                        var protocol=new ProtobufProtocol();protocol.setMsgID(id);protocol.setSeq(seq);protocol.deserializeData(data);require(protocol.getData()!=null,"request decoding");
                        var fixture=slot==0?primary:peer;route.processor.process(fixture.client,protocol);var reply=fixture.client.await();
                        require(reply.sequence()==seq,"response sequence");
                        byte[] bytes=reply.message().toByteArray();out.writeInt(PbProtocol.getInstance().getMsgId(reply.message().getClass().getSimpleName()));out.writeInt(reply.error());out.writeInt(bytes.length);out.write(bytes);out.flush();
                    }
                }catch(Exception | AssertionError error){failure=error;log.error("自有邮件loopback失败",error);}
            });
            client=new Socket("127.0.0.1",server.getLocalPort());client.setSoTimeout(15000);
            input=new DataInputStream(client.getInputStream());output=new DataOutputStream(client.getOutputStream());output.writeUTF(token);output.flush();
        }

        /** @param slot 已绑定的会话编号 @param request 正式请求 @return 真实服务端响应 */
        MailFilterFixture.Reply request(int slot,MailMsg.MailListRequest_12000001 request) throws Exception {
            // 场景之外的拒绝/隔离请求同样遵守真实连接的每秒十条限制。
            Thread.sleep(120);
            byte[] bytes=request.toByteArray();output.writeInt(slot);output.writeInt(PbProtocol.MailListRequest_12000001);output.writeInt(++sequence);output.writeInt(bytes.length);output.write(bytes);output.flush();
            int id=input.readInt(),error=input.readInt(),length=input.readInt();require(length>=0 && length<=1048576,"response bounds");byte[] payload=input.readNBytes(length);require(payload.length==length,"response truncation");
            var decoded=PbProtocol.getInstance().parseFrom(id,payload);require(decoded!=null,"response decoding");return new MailFilterFixture.Reply(decoded,error);
        }

        @Override public void close() throws Exception {
            try{output.writeInt(-1);output.flush();worker.join(TimeUnit.SECONDS.toMillis(5));}
            finally{client.close();server.close();}
            require(!worker.isAlive(),"owned loopback did not stop");if(failure!=null){throw new AssertionError("loopback worker failed",failure);}
        }
    }
}
