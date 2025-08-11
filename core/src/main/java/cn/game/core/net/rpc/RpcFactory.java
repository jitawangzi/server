package cn.game.core.net.rpc;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.game.core.net.vertx.VxHolder;
import cn.game.util.ServerType;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.SuperCall;
import net.bytebuddy.matcher.ElementMatchers;

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
	public static <T> T getImpl(Class<T> clazz, RpcClient rpcClient, CallType callType, String serverId, ServerType serverType,
			long objectId) {
		if (callType == CallType.PointToPoint && StringUtils.isBlank(serverId)) {
			throw new IllegalArgumentException("serverId can't be null when callType is PointToPoint");
		}
		String targetAddr = callType == CallType.PointToPoint ? VxHolder.rpcServiceAddr(serverId) : VxHolder.rpcServiceAddr(serverType);
		// 当objectId=0时使用缓存
		if (objectId == 0) {
			Key key = new Key(clazz, targetAddr, callType);
			return (T) instanceCache.computeIfAbsent(key, k -> createProxy(clazz, rpcClient, targetAddr, callType, 0));
		} else {
			// 直接创建新实例，不缓存,带有objectId后实例变多，缓存命中率低，不合算。
			return createProxy(clazz, rpcClient, targetAddr, callType, objectId);
		}
	}

	@SuppressWarnings("unchecked")
	private static <T> T createProxy(Class<T> clazz, RpcClient rpcClient, String targetAddr, CallType callType, long objectId) {
		Invocation invocation = new Invocation(rpcClient, targetAddr, callType, objectId);

		if (clazz.isInterface()) {
			// 用JDK Proxy代理接口
			return (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class[] { clazz }, invocation);
		} else {
			// 用Byte Buddy代理类
			try {
	            // 创建包含Invocation的拦截器实例
	            ByteBuddyInterceptor interceptor = new ByteBuddyInterceptor(invocation);
				// 只拦截非Object类方法
				return new ByteBuddy().subclass(clazz)
						.method(ElementMatchers.isPublic() // 只拦截public方法
								.and(ElementMatchers.not(ElementMatchers.isStatic()))
								.and(ElementMatchers.not(ElementMatchers.isFinal()))
								.and(ElementMatchers.not(ElementMatchers.isDeclaredBy(Object.class)))
								.and(ElementMatchers.not(ElementMatchers.isSynthetic()))
								.and(ElementMatchers.not(ElementMatchers.isBridge()))
						// 你可以根据实际情况加更多排除条件
						)
						.intercept(MethodDelegation.to(interceptor))
						.make()
						.load(clazz.getClassLoader(), ClassLoadingStrategy.Default.INJECTION)
						.getLoaded()
						.getDeclaredConstructor()
						.newInstance();
			} catch (Exception e) {
				log.error("byte buddy proxy create failed", e);
				throw new RuntimeException(e);
			}
		}
	}

	public static <T> T getImpl(Class<T> rpcInterfaceClass, RpcClient rpcClient, CallType callType, String serverId,
			ServerType serverType) {
		return getImpl(rpcInterfaceClass, rpcClient, callType, serverId, serverType, 0);
	}

	// 用于判断Object类方法
	private static final Map<String, String> objectMethods;
	static {
		objectMethods = new HashMap<>();
		Method[] ms = Object.class.getMethods();
		for (Method m : ms) {
			objectMethods.put(m.getName(), m.getName());
		}
	}

	// InvocationHandler 适用于JDK Proxy和Byte Buddy
	static class Invocation implements InvocationHandler {
		private RpcClient rpcClient;
		/** 是否同步阻塞调用 ,暂时不用了，使用 返回值类型和 callBackTask 来区分异步*/
		@Deprecated
		private boolean block;
		/** 服务器id/类型 */
		private String targetAddr;
		/** 调用类型 */
		private CallType callType;
		private long objectId;

		public Invocation(RpcClient rpcClient, String targetAddr, CallType callType, long objectId) {
			this.rpcClient = rpcClient;
			this.targetAddr = targetAddr;
			this.callType = callType;
			this.objectId = objectId;
		}

		@Override
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			try {
				// Object类方法直接执行原始逻辑
				String mname = method.getName();
				if (objectMethods.get(mname) != null) {
					// 在Byte Buddy场景下，proxy参数实际是目标对象
					return method.invoke(proxy, args);
				}
				return rpcClient.invoke(callType, method, args, targetAddr, objectId);
			} catch (Exception e) {
				log.error("rpc invoke 调用出现异常", e);
				throw new RuntimeException(e);
			}
		}

		public long getObjectId() {
			return objectId;
		}

		public RpcClient getRpcClient() {
			return rpcClient;
		}

		public String getTargetAddr() {
			return targetAddr;
		}
		
	}

	// 缓存Key
	private static class Key {
		private final Class<?> clazz;
		private final String serverId;
		private final CallType callType;

		public Key(Class<?> clazz, String serverId, CallType callType) {
			this.clazz = clazz;
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
			return clazz.equals(other.clazz) && serverId.equals(other.serverId) && callType == other.callType;
		}

		@Override
		public int hashCode() {
			int result = clazz.hashCode();
			result = 31 * result + serverId.hashCode();
			result = 31 * result + callType.hashCode();
			return result;
		}
	}

	// 定义拦截逻辑
	public static class ByteBuddyInterceptor {
	    private final Invocation invocation;
	    
	    public ByteBuddyInterceptor(Invocation invocation) {
	        this.invocation = invocation;
	    }
	    
	    @RuntimeType
	    public Object intercept(@Origin Method method, @AllArguments Object[] args, @SuperCall Callable<?> superCall) throws Exception {
	        try {
	            // Object类方法直接调用原方法
	            String mname = method.getName();
	            if (objectMethods.get(mname) != null) {
	                return superCall.call();
	            }
	            
	            // 其他方法通过RPC调用
	            return invocation.rpcClient.invoke(invocation.callType, method, args, invocation.targetAddr, invocation.objectId);
	        } catch (Exception e) {
	            log.error("rpc invoke 调用出现异常", e);
	            throw new RuntimeException(e);
	        }
	    }
	}

}