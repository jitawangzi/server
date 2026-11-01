package cn.game.games.net.game.module.mail;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import javax.xml.parsers.DocumentBuilderFactory;
import cn.game.games.cache.entity.Mail;
import cn.game.games.cache.entity.Player;
import cn.game.games.net.client.GameClient;
import cn.game.games.net.game.manager.PlayerManager;
import cn.game.protocol.generated.config.MailConfig;
import cn.game.protocol.generated.manager.MailManager;
import cn.game.protocol.protobuf.PbProtocol;
import com.google.protobuf.Message;

/** 测试自有会话与配表夹具；仅装配邮件模块，不启动账号登录、集群或其他业务模块。 */
public final class MailFilterFixture implements AutoCloseable {
    /** 仅替换当前测试进程中的配置映射，退出时恢复原引用。 */
    private final java.lang.reflect.Field configField;
    /** 原映射不被修改。 */
    private final Object original;
    /** 独有在线玩家。 */
    public final Player player;
    /** 真实邮件模块。 */
    public final MailModule module = new MailModule();
    /** 真实 GameClient 的测试传输接缝。 */
    public final Client client = new Client();

    /** @param id 本用例唯一玩家ID */
    public MailFilterFixture(long id) throws Exception {
        cn.game.games.core.clazz.ClassManager.getInstance().init();
        player = new Player(); player.setPlayerId(id); player.setGameClient(client);
        client.setPlayerId(id); client.setSessionId("mail-fixture-" + id); client.state = GameClient.GameClientState.IN_GAME;
        module.initDefault(player); player.getModules().put(MailModule.class.getName(), module);
        if (PlayerManager.getInstance().getAllPlayer().putIfAbsent(id, player) != null) { throw new AssertionError("fixture player collision"); }
        configField = MailManager.class.getDeclaredField("mails"); configField.setAccessible(true);
        original = configField.get(MailManager.instance());
        configField.set(MailManager.instance(), new HashMap<>((Map<Integer, MailConfig>) original));
    }

    /** @param id 配置ID @param type 邮件类型 @param expiration 有效秒 @param reward 与正式XML一致的奖励表达式 */
    public void config(int id, int type, int expiration, String reward) throws Exception {
        var element = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument().createElement("Mail");
        element.setAttribute("ID", "" + id); element.setAttribute("Type", "" + type);
        element.setAttribute("Expiration", "" + expiration); element.setAttribute("Reward", reward);
        ((Map<Integer, MailConfig>) configField.get(MailManager.instance())).put(id, new MailConfig(element));
    }

    /** @param rows 新MailMapper会话或单元用例准备的实体；不调用有写副作用的登录清理阶段 */
    public void load(List<Mail> rows) throws Exception {
        var loader = MailModule.class.getDeclaredMethod("initFromDb", java.util.ListIterator.class); loader.setAccessible(true);
        loader.invoke(module, List.of(rows).listIterator());
    }

    /** @param id 邮件ID @param owner 归属 @param time 创建秒 @return 独立实体，无写入 */
    public static Mail mail(long id, long owner, int time) {
        Mail mail = new Mail(); mail.setId(id); mail.setPlayerId(owner); mail.setCreateTime(time);
        mail.setType((byte) 2); mail.setTitle("mail-fixture"); mail.setContent(""); mail.setSender("mail-fixture");
        mail.setAttachment("[]"); return mail;
    }

    @Override public void close() throws Exception {
        PlayerManager.getInstance().getAllPlayer().remove(player.getPlayerId(), player);
        configField.set(MailManager.instance(), original);
    }

    /** @param message 实际编码后的响应 @param error 真实错误码 @param sequence 实际响应序号，-1表示loopback上层未携带 */
    public record Reply(Message message, int error, int sequence) {
        /** @param message 解码响应 @param error 错误码 */
        public Reply(Message message,int error){this(message,error,-1);}
    }

    /** 只替换物理发送；保留needProcess、序号和真实响应编码入口。 */
    public static final class Client extends GameClient {
        /** 玩家线程到测试传输线程的响应队列。 */
        public final LinkedBlockingQueue<Reply> replies = new LinkedBlockingQueue<>();
        /** 自有socket接缝保留真实GameClient帧编码、缓存和计时。 */
        public Client() { super(socket()); }
        @Override public boolean sendProtocol(int messageId, int sequence, byte[] data, int error, boolean record) {
            if (!super.sendProtocol(messageId, sequence, data, error, record)) { throw new AssertionError("send failed"); }
            Message decoded = PbProtocol.getInstance().parseFrom(messageId, data);
            if (decoded == null) { throw new AssertionError("response decode failed"); }
            replies.add(new Reply(decoded, error, sequence)); return true;
        }
        /** @return 仅替代物理WebSocket写入，核对实际帧长度后释放自有缓冲区 */
        private static io.vertx.core.http.ServerWebSocket socket() {
            return (io.vertx.core.http.ServerWebSocket)java.lang.reflect.Proxy.newProxyInstance(
                    io.vertx.core.http.ServerWebSocket.class.getClassLoader(),new Class<?>[]{io.vertx.core.http.ServerWebSocket.class},
                    (proxy,method,args)->switch(method.getName()){
                        case "isClosed" -> false;
                        case "writeBinaryMessage" -> {
                            var buffer=(io.vertx.core.buffer.Buffer)args[0];
                            if(buffer.getInt(0)!=buffer.length() || buffer.length()<16){throw new AssertionError("encoded frame length");}
                            ((io.vertx.core.internal.buffer.BufferInternal)buffer).getByteBuf().release();
                            yield io.vertx.core.Future.succeededFuture();
                        }
                        case "binaryHandlerID","toString" -> "owned-mail-fixture";
                        default -> throw new AssertionError("unexpected socket operation "+method.getName());
                    });
        }
        /** @return 本次响应，超时明确失败 */
        public Reply await() throws Exception {
            Reply result = replies.poll(10, TimeUnit.SECONDS);
            if (result == null) { throw new AssertionError("mail response timeout"); }
            return result;
        }
    }
}
