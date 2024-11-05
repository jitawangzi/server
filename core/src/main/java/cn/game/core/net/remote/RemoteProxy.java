package cn.game.core.net.remote;

import java.lang.reflect.Method;

import org.springframework.util.ReflectionUtils;

import cn.game.util.reflect.ClassHelper;

public interface RemoteProxy {
	default Object invoke(String methodName, Object... args) {
		try {
			Method method = ClassHelper.findMethod(this.getClass(), methodName, args);
			// 可以调用非public方法
			ReflectionUtils.makeAccessible(method);
			return method.invoke(this, args);
		} catch (Exception e) {
			throw new RuntimeException("RemoteProxy Method invocation failed", e);
		}
	}
}