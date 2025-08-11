package cn.game.core.net.rpc;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.game.core.net.transport.Command;

/**
* RPC调用辅助类，提供了改变单次调用行为的工具方法。
*/
public final class Rpc {

	private static final Logger log = LoggerFactory.getLogger(Rpc.class);

	private Rpc() {
	}

	/**
	 * 将下一次RPC调用转换为"Fire-and-Forget"（单向）模式。
	 * <p>
	 * 这个方法会返回一个临时的代理，对该代理的任何方法调用都将以单向模式发送，
	 * 不会等待服务器响应，并且总是立即返回null（或void）。
	 * <p>
	 * 原始的serviceProxy实例不受影响。
	 *
	 * @param serviceProxy 原始的RPC服务代理实例，必须是由RpcFactory创建的基于接口的JDK代理。
	 * @param <T>          服务接口类型。
	 * @return 一个临时的、用于单向调用的服务代理。
	 */
	@SuppressWarnings("unchecked")
	public static <T> T oneWay(T serviceProxy) {
		if (serviceProxy == null) {
			throw new IllegalArgumentException("serviceProxy instance cannot be null.");
		}
		if (!Proxy.isProxyClass(serviceProxy.getClass())) {
			// 注意：这个实现目前只支持基于接口的JDK动态代理。
			// 如果要支持Byte Buddy生成的类代理，需要更复杂的逻辑来提取代理信息。
			throw new IllegalArgumentException("serviceProxy instance must be a JDK Proxy created by RpcFactory.");
		}

		InvocationHandler originalHandler = Proxy.getInvocationHandler(serviceProxy);

		if (!(originalHandler instanceof RpcFactory.Invocation)) {
			throw new IllegalStateException("The proxy's InvocationHandler is not of the expected type 'RpcFactory.Invocation'.");
		}

		RpcFactory.Invocation internalHandler = (RpcFactory.Invocation) originalHandler;

		OneWayInvocationHandler oneWayHandler = new OneWayInvocationHandler(internalHandler);

		Class<?>[] interfaces = serviceProxy.getClass().getInterfaces();

		return (T) Proxy.newProxyInstance(serviceProxy.getClass().getClassLoader(), interfaces, oneWayHandler);
	}

	/**
	 * 一个专门处理单向调用的InvocationHandler。
	 */
	private static class OneWayInvocationHandler implements InvocationHandler {
		private final RpcFactory.Invocation originalInvocation;

		public OneWayInvocationHandler(RpcFactory.Invocation originalInvocation) {
			this.originalInvocation = originalInvocation;
		}

		@Override
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			if (Object.class.equals(method.getDeclaringClass())) {
				return method.invoke(this, args);
			}

			if (method.getReturnType() != void.class) {
				log.warn("Method '{}' is called in oneWay mode, its return value of type '{}' will be ignored.", method.getName(),
						method.getReturnType().getSimpleName());
			}

			Command command = new Command(method.getDeclaringClass().getName(), method.getName(), method.getParameterTypes(), args,
					originalInvocation.getObjectId());

			// 使用原始代理的RpcClient和配置，强制以fire-and-forget模式发送
			originalInvocation.getRpcClient()
					.fireAndForget(originalInvocation.getTargetAddr(), command, cn.game.core.net.vertx.VxHolder.universalOptions // 假设这是你的通用配置
					);

			// 单向调用不返回任何东西 (对于原始类型返回默认值，对于对象返回null)
			return null;
		}
	}
}
