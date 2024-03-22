package cn.game.core.net.rpc;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RpcFactory {

	private static Logger log = LoggerFactory.getLogger(RpcFactory.class);

	private RpcFactory() {
	}

	public static <T> T getImpl(Class<T> rpcInterfaceClass, RpcClient rpcClient, boolean block) {
		Invocation invocation = new Invocation();
		invocation.setBlock(block);
		invocation.setRpcClient(rpcClient);
		T instance = (T) Proxy.newProxyInstance(rpcInterfaceClass.getClassLoader(), new Class[] { rpcInterfaceClass }, invocation);
		return instance;
	}
	public static <T> T getImpl(Class<T> rpcInterfaceClass, RpcClient rpcClient, boolean block, String serverId) {
		Invocation invocation = new Invocation();
		invocation.setBlock(block);
		invocation.setRpcClient(rpcClient);
		invocation.setServerId(serverId);
		T instance = (T) Proxy.newProxyInstance(rpcInterfaceClass.getClassLoader(), new Class[] { rpcInterfaceClass }, invocation);
		return instance;
	}

	public static <T> T getImplCallback(RpcClient rpcClient, Class<T> rpcInterfaceClass, Consumer<?> callBackTask,
			String serverId) {
		Invocation invocation = new Invocation();
		invocation.setRpcClient(rpcClient);
		invocation.setCallBackTask(callBackTask);
		invocation.setServerId(serverId);
		T instance = (T) Proxy.newProxyInstance(rpcInterfaceClass.getClassLoader(), new Class[] { rpcInterfaceClass }, invocation);
		return instance;
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
		private Consumer<?> callBackTask;
		private boolean block;
		private String serverId;

		@Override
		public Object invoke(Object proxy, Method method, Object[] args) {

			try {
				// Object的方法，直接调用原始方法
				String mname = method.getName();
				if (objectMethods.get(mname) != null) {
					return method.invoke(proxy, args);
				}
				return rpcClient.invoke(method.getName(), method.getParameterTypes(), method.getReturnType(), args, callBackTask,
						block, serverId);

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

		public void setServerId(String serverId) {
			this.serverId = serverId;
		}

	}
}
