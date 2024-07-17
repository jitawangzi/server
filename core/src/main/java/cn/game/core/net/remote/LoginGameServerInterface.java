package cn.game.core.net.remote;

import io.vertx.core.Future;

@Deprecated
public interface LoginGameServerInterface {
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
