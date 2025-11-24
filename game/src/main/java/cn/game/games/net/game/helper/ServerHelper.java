package cn.game.games.net.game.helper;

import static java.util.stream.Collectors.toList;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import cn.game.core.base.ActiveServerListManager;
import cn.game.core.base.ServerContext;
import cn.game.core.base.VirtualServerRegistry.VirtualServerView;
import cn.game.core.cache.id.DistributedObjectType;
import cn.game.core.cache.id.IdCache;
import cn.game.core.net.rpc.CallType;
import cn.game.core.net.rpc.RpcFactory;
import cn.game.core.zookeeper.server.ValidServerService;
import cn.game.games.core.cache.GameCacheService;
import cn.game.games.net.cross.guild.service.GuildServiceInterface;
import cn.game.games.net.game.remote.GameServerInterface;
import cn.game.util.DateUtil;
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
				return ClassHelper.getSingletonInstance(clazz);
			}
		}
		// 在其他服务器，通过远程调用
		return RpcFactory.getImpl(clazz, ServerContext.getInstance().getRpcClient(), callType, serverId, serverType, targetId);
	}
	
	/** 
	 * 获取公会远程代理接口
	 * @param targetId
	 * @return
	 */
	public static GuildServiceInterface getGuildProxy(long guildId) {
		return getRemoteInterfaceProxy(ServerType.Cross, GuildServiceInterface.class, DistributedObjectType.GUILD, guildId);
	}
	
	/** 
	 * 获取玩家的远程代理接口
	 * @param targetId
	 * @return
	 */
	public static GameServerInterface getPlayerProxy(long playerId) {
		return ServerHelper.getRemoteInterfaceProxy(ServerType.Game, GameServerInterface.class, DistributedObjectType.PLAYER, playerId);
	}
	
	/** 
	 * 开服第几天了
	 * @param serverId
	 * @return 最小从1开始
	 */
	public static int getServerOpenDay(String serverId) {
		Map<String, VirtualServerView> validServers = getServerOpenMap(); 
		VirtualServerView virtualServerView = validServers.get(serverId); 
		if (virtualServerView == null || virtualServerView.openTime == null) {
			return 0;
		}
		return DateUtil.diffDays(virtualServerView.openTime.toLocalDate(), LocalDate.now()) + 1; 
	}
	
	public static Map<String, VirtualServerView> getServerOpenMap() {
		return ServerContext.getInstance().getValidGameService().getValidServers();
//		return GameCacheService.getInstance().getOpenServerMap(); 
	}
	public static String[] getServerIds() {
		return getServerOpenMap().values().stream().map(r -> r.ID).collect(toList()).toArray(new String[] {});
	}
	public static String getServerIdLatest() {
		VirtualServerView openServerLatest = GameCacheService.getInstance().getOpenServerLatest(); 
		Objects.requireNonNull(openServerLatest, "没有开启的服务器");
		return openServerLatest.ID;
	}
	public static String getServerIdLatestAsync() {
		VirtualServerView retServerView= null; 
		Map<String, VirtualServerView> validServers = ServerContext.getInstance().getValidGameService().getValidServers(); 
		if (validServers != null && validServers.size() > 0) {
			List<VirtualServerView> list = validServers.values()
					.stream()
					.sorted((a, b) -> b.getOpenTime().compareTo(a.getOpenTime()))
					.collect(Collectors.toList());
			retServerView = list.get(0);
		}
		return retServerView.ID;
	}
	public static String getServerName(String serverId) {
		VirtualServerView virtualServerView = getServerOpenMap().get(serverId); 
		if (virtualServerView == null) {
			if (ServerContext.getInstance().getRunMode().isProduction()) {
				Objects.requireNonNull(virtualServerView, "无效的serverId=" + serverId);
			}else {
				return " 默认服务器名[" + serverId + "]";
			}
		}
		return virtualServerView.name;
	}
}
