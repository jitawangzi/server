package cn.game.core.net.remote;

import java.lang.reflect.Method;

import org.springframework.util.ReflectionUtils;

import cn.game.util.reflect.ClassHelper;

/**    
 * 基本的远程代理接口，远程方法默认同步调用
 * 如果方法的返回值类型是io.vertx.core.Future，则是异步调用
 * 尽量使用异步调用，避免阻塞线程，低频的调用可以考虑同步
 * 2024年11月12日 18:48:58
 * @author SYQ
 */
public interface RemoteProxy {
	/** 
	 * 默认反射调用方法，通过参数类型查找方法
	 * @param methodName
	 * @param paramTypes
	 * @param args
	 * @return
	 */
	@Deprecated
	default Object invoke(String methodName, Class<?>[] paramTypes, Object... args) {
		try {
			Method method = ClassHelper.findMethod(this.getClass(), methodName, paramTypes);
			// 可以调用非public方法
			ReflectionUtils.makeAccessible(method);
			return method.invoke(this, args);
		} catch (Exception e) {
			throw new RuntimeException("RemoteProxy Method invocation failed", e);
		}
	}

	/** 
	 * 默认反射调用方法，通过参数查找方法，不够准确，有些限制，不推荐使用
	 * @param methodName
	 * @param args
	 * @return
	 */
	@Deprecated
	default Object invoke(String methodName, Object... args) {
		try {
			Method method = ClassHelper.findMethodByArgs(this.getClass(), methodName, args);
			// 可以调用非public方法
			ReflectionUtils.makeAccessible(method);
			return method.invoke(this, args);
		} catch (Exception e) {
			throw new RuntimeException("RemoteProxy Method invocation failed", e);
		}
	}

}