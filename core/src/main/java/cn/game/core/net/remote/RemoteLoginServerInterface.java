package cn.game.core.net.remote;

import io.vertx.core.Future;

/**    
 * Login服务器提供给其他服务器调用的远程接口
 * 这里的其他服务器，一般指没有直接依赖关系的两个服务。 
 * 对于Login来说，一般是Game
 * 2024年11月5日 10:16:01
 * @author SYQ
 */
@Deprecated
public interface RemoteLoginServerInterface extends RemoteProxy {
	public boolean isAvailable();

	/**
	 * 检查sessionId是否有效
	 * @param passportSessionId
	 * @return 有效的uid
	 */
	public long getUid(String passportSessionId);

	public Future<Long> getUid2(String passportSessionId);

	/**
	 * 测试使用，获取uid，没有则添加
	 * @param name
	 * @return
	 */
	public long getUidByName(String name);

	public void addUserServer(String serverId, long passportSessionId, long uid);


}
