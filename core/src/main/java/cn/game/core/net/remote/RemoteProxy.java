package cn.game.core.net.remote;

import java.lang.reflect.Method;

import org.springframework.util.ReflectionUtils;

import cn.game.util.reflect.ClassHelper;

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