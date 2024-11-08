package cn.game.core.net.remote;

import java.util.List;

import cn.game.protocol.protobuf.RewardMsg.RewardInfo;

/**    
 * Game服务器提供给其他服务器调用的远程接口
 * 这里的其他服务器，一般指没有直接依赖关系的两个服务。 
 * 对于Game来说，例如Login ,GMT(如果有的话)
 * 2024年11月4日 17:16:01
 * @author SYQ
 */
public interface RemoteGameServerInterface extends RemoteProxy {

	public List<RewardInfo> addResources(long playerId, int id, int value);

	public boolean delResources(long playerId, int id, int value);

	public boolean alive();

	public void shutdown();

	public ServerStatus status();

}