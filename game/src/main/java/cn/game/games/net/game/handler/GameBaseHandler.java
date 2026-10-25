package cn.game.games.net.game.handler;

import cn.game.core.net.client.NetClient;
import cn.game.core.net.protocol.IProtocol;
import cn.game.core.net.socket.handler.BaseHandler;
import cn.game.games.cache.entity.Player;
import cn.game.games.net.game.helper.PlayerWriteGuard;
import cn.game.games.net.game.helper.PlayerSaveCoordinator;
import cn.game.protocol.protobuf.PbProtocol;
import java.util.ArrayDeque;
import cn.game.games.net.game.manager.PlayerManager;
import cn.game.protocol.generated.enume.InitialUI;
import cn.game.protocol.manual.ErrorMsgEnum;
import cn.game.protocol.protobuf.PlayerMsg.PlayerErrorPush_01000099;

public abstract class GameBaseHandler extends BaseHandler {
    /** handler 为共享单例，作用域须随调用线程和嵌套dispatch隔离。 */
    private final ThreadLocal<ArrayDeque<PlayerWriteGuard.Scope>> scopes = ThreadLocal.withInitial(ArrayDeque::new);
    @Override
    public void dispatch(NetClient client, IProtocol<?> protocol) {
        ArrayDeque<PlayerWriteGuard.Scope> stack = scopes.get();
        int depth = stack.size();
        try { super.dispatch(client, protocol); }
        finally {
            while (stack.size() > depth) { stack.pop().close(); }
            if (stack.isEmpty()) { scopes.remove(); }
        }
    }
    /** 测试可注入独立协调器，不替换进程共享实例。 */
    protected PlayerWriteGuard.Scope beginWrite(Player player) { return PlayerWriteGuard.begin(player); }
    /** 异步业务回调按目标玩家重新投递，供协议入口共享。 */
    protected io.vertx.core.Future<Void> submitPlayerMutation(Player player, Runnable action) {
        return PlayerWriteGuard.submit(player, action);
    }
    /** 查找当前权威玩家。 */
    protected Player findPlayer(long id) { return PlayerManager.getInstance().getPlayer(id); }


	/** 
	 * 对应的业务功能,主要用来判断功能是否开启
	 * @return
	 */
	protected InitialUI getInitialUI() {
		return null;
	}
	@Override
	public boolean checkExt(NetClient client, IProtocol<?> protocol) {
		long playerId = client.getPlayerId();
		if (playerId <= 0) {
			return true;
		}
		Player player = findPlayer(playerId);
		if (player != null) {
            // 即使上次退出保存失败，仍允许心跳和再次进入退出保存通道。
            if (protocol.getMsgID() == PbProtocol.PlayerHeartbeatRequest_01000005
                    || protocol.getMsgID() == PbProtocol.PlayerLogoutRequest_01000003) { return true; }
			if (player.isIslogouting()) {
				client.sendProtocol(PlayerErrorPush_01000099.getDefaultInstance(), ErrorMsgEnum.player_logouting.getId());
				return false;
			}
			if (getInitialUI() != null && !player.isFuncOpen(getInitialUI())) {
				client.sendProtocol(PlayerErrorPush_01000099.getDefaultInstance(), ErrorMsgEnum.func_not_open.getId());
				return false;
			}
            // 心跳无聚合业务变化；退出属于保存恢复通道，不被写保护拦住。
            if (protocol.getMsgID() != PbProtocol.PlayerHeartbeatRequest_01000005
                    && protocol.getMsgID() != PbProtocol.PlayerLogoutRequest_01000003) {
                try { scopes.get().push(beginWrite(player)); }
                catch (PlayerSaveCoordinator.SaveFailure failure) {
                    log.warn("玩家业务因保存风险被拒绝 playerId={}", playerId, failure);
                    if (client instanceof cn.game.games.net.client.GameClient gameClient) {
                        // 已通过needProcess，必须按队列内响应发送；队列外reject会抑制当前序号。
                        gameClient.sendProtocol(PbProtocol.PlayerErrorPush_01000099, protocol.getSeq(),
                                PlayerErrorPush_01000099.newBuilder().setError(failure.code).build().toByteArray(),
                                ErrorMsgEnum.system_overload.getId(), true);
                    } else { client.sendProtocol(PlayerErrorPush_01000099.getDefaultInstance(), ErrorMsgEnum.system_overload.getId()); }
                    return false;
                }
            }
        }
        return true;
    }

}
