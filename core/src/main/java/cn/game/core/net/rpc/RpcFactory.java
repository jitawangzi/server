package cn.game.core.net.rpc;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.game.core.net.vertx.VxHolder;
import cn.game.util.ServerType;

public class RpcFactory {
	private static Logger log = LoggerFactory.getLogger(RpcFactory.class);

	private static final Map<Key, Object> instanceCache = new ConcurrentHashMap<>();

	private RpcFactory() {
	}

	/** 
	 * 获取远程接口动态代理实例
	 * @param <T>
	 * @param rpcInterfaceClass	远程接口class
	 * @param rpcClient	远程调用网络客户端
	 * @param callType 调用类型
	 * @param serverId	当调用类型是点对点时，需要此参数，远程节点的唯一地址。 
	 * @param serverType 当调用类型是负载均衡或广播时，需要此参数，远程节点的类型
	 * @param objectId 用来在远程服务器分配线程使用
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> T getImpl(Class<T> rpcInterfaceClass, RpcClient rpcClient, CallType callType, String serverId, ServerType serverType,
			long objectId) {
		if (callType == CallType.PointToPoint && StringUtils.isBlank(serverId)) {
			throw new IllegalArgumentException("serverId can't be null when callType is PointToPoint");
		}
		String targetAddr = callType == CallType.PointToPoint ? VxHolder.rpcServiceAddr(serverId) : VxHolder.rpcServiceAddr(serverType);
		// 当objectId=0时使用缓存
		if (objectId == 0) {
			Key key = new Key(rpcInterfaceClass, targetAddr, callType);
			return (T) instanceCache.computeIfAbsent(key, k -> createProxy(rpcInterfaceClass, rpcClient, targetAddr, callType, 0));
		} else {
			// 直接创建新实例，不缓存,带有objectId后实例变多，缓存命中率低，不合算。
			return createProxy(rpcInterfaceClass, rpcClient, targetAddr, callType, objectId);
		}
	}

	private static <T> T createProxy(Class<T> rpcInterfaceClass, RpcClient rpcClient, String targetAddr, CallType callType, long objectId) {
		Invocation invocation = new Invocation();
		invocation.setRpcClient(rpcClient);
		invocation.setBlock(false);
		invocation.setTargetAddr(targetAddr);
		invocation.setCallType(callType);
		invocation.setObjectId(objectId);
		return (T) Proxy.newProxyInstance(rpcInterfaceClass.getClassLoader(), new Class[] { rpcInterfaceClass }, invocation);
	}

	@SuppressWarnings("unchecked")
	public static <T> T getImpl(Class<T> rpcInterfaceClass, RpcClient rpcClient, CallType callType, String serverId,
			ServerType serverType) {
		return getImpl(rpcInterfaceClass, rpcClient, callType, serverId, serverType, 0);
	}

	private static Map<String, String> objectMethods;
	static {
		objectMethods = new HashMap<>();
		Method[] ms = Object.class.getMethods();
		for (Method m : ms) {
			objectMethods.put(m.getName(), m.getName());
		}
	}

	private static class Invocation implements InvocationHandler {

		private RpcClient rpcClient;
		/** 回调任务，只有在异步调用时才有用 */
		@Deprecated
		private Consumer<?> callBackTask;
		/** 是否同步阻塞调用 ,暂时不用了，使用 返回值类型和 callBackTask 来区分异步*/
		@Deprecated
		private boolean block;
		/** 服务器id/类型 */
		private String targetAddr;
		/** 调用类型 */
		private CallType callType;
		private long objectId;

		@Override
		public Object invoke(Object proxy, Method method, Object[] args) {

			try {
				// Object的方法，直接调用原始方法
				String mname = method.getName();
				if (objectMethods.get(mname) != null) {
					return method.invoke(proxy, args);
				}
				return rpcClient.invoke(callType, method, args, targetAddr, objectId);

			} catch (Exception e) {
				log.error("rpc invoke 调用出现异常", e);
				throw new RuntimeException(e);
			}
		}

		public void setRpcClient(RpcClient rpcClient) {
			this.rpcClient = rpcClient;
		}

		public void setCallBackTask(Consumer<?> callBackTask) {
			this.callBackTask = callBackTask;
		}

		public void setBlock(boolean block) {
			this.block = block;
		}


		public void setTargetAddr(String targetAddr) {
			this.targetAddr = targetAddr;
		}

		public void setCallType(CallType callType) {
			this.callType = callType;
		}

		public void setObjectId(long objectId) {
			this.objectId = objectId;
		}

		public long getObjectId() {
			return objectId;
		}

	}

	private static class Key {
		private final Class<?> rpcInterfaceClass;
		private final String serverId;
		private final CallType callType;

		public Key(Class<?> rpcInterfaceClass, String serverId, CallType callType) {
			this.rpcInterfaceClass = rpcInterfaceClass;
			this.callType = callType;
			this.serverId = serverId == null ? "" : serverId;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (!(obj instanceof Key))
				return false;
			Key other = (Key) obj;
			return rpcInterfaceClass.equals(other.rpcInterfaceClass) && serverId.equals(other.serverId) && callType == other.callType;
		}

		@Override
		public int hashCode() {
			int result = rpcInterfaceClass.hashCode();
			result = 31 * result + serverId.hashCode();
			result = 31 * result + callType.hashCode();
			return result;
		}
	}

}
