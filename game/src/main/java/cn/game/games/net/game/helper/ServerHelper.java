package cn.game.games.net.game.helper;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import cn.game.core.base.ActiveServerListManager;
import cn.game.core.base.ServerContext;
import cn.game.core.cache.id.DistributedObjectType;
import cn.game.core.cache.id.IdCache;
import cn.game.core.net.rpc.CallType;
import cn.game.core.net.rpc.RpcFactory;
import cn.game.games.net.cross.zongmen.service.ZongmenServiceInterface;
import cn.game.util.ServerType;
import cn.game.util.reflect.ClassHelper;

public class ServerHelper {

	/** 
	 * 获取所有指定类型服务器的远程代理接口，用于点对点通讯。 
	 * @param <T>
	 * @param serverType
	 * @param clazz
	 * @return
	 */
	public static <T> List<T> getAllServerInterface(ServerType serverType, Class<T> clazz) {

		Set<String> serverSet = ActiveServerListManager.getInstance().getServerSet(serverType);
		List<T> ret = new ArrayList<>();

		for (String serverId : serverSet) {
			T impl = RpcFactory.getImpl(clazz, ServerContext.getInstance().getRpcClient(), CallType.PointToPoint, serverId, serverType, 0);
			ret.add(impl);
		}
		return ret;
	}

	/** 
	 * 获取服务器远程接口代理
	 * @param <T>
	 * @param ServerType 服务器类型
	 * @param clazz		远程接口class
	 * @param type	    分布式对象类型
	 * @param targetId	分布式对象对象ID
	 * @return
	 */
	public static <T> T getRemoteInterfaceProxy(ServerType serverType, Class<T> clazz, DistributedObjectType type, long targetId) {

		CallType callType = CallType.PointToPoint;
		String serverId = IdCache.getManager(type).getServerId(targetId);
		if (StringUtils.isEmpty(serverId)) {
			callType = CallType.LoadBalancer;
		}
		if (ServerContext.getInstance().getServerType() == serverType) {
			// 对象不在线，或者在当前服务器，直接由当前服务器处理
			if (StringUtils.isEmpty(serverId) || serverId.equals(ServerContext.getInstance().getServerId())) {
				// 直接返回本地代理
				return (T) ClassHelper.getSingletonInstance(clazz);
			}
		}
		// 在其他服务器，通过远程调用
		return RpcFactory.getImpl(clazz, ServerContext.getInstance().getRpcClient(), callType, serverId, serverType, targetId);
	}
	
	/** 
	 * 获取宗门远程代理接口
	 * @param targetId
	 * @return
	 */
	public static ZongmenServiceInterface getZongmenProxy(long targetId) {
		return getRemoteInterfaceProxy(ServerType.Cross, ZongmenServiceInterface.class, DistributedObjectType.ZONGMEN, targetId);
	}

}
