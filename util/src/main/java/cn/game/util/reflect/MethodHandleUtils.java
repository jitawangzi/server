package cn.game.util.reflect;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class MethodHandleUtils {
	private static final Map<Method, MethodHandle> METHOD_HANDLE_CACHE = new HashMap<>();

	/**
	 * 获取方法句柄
	 */
	public static MethodHandle getMethodHandle(Method method) throws IllegalAccessException {
		return METHOD_HANDLE_CACHE.computeIfAbsent(method, m -> {
			try {
				return MethodHandles.lookup().unreflect(m);
			} catch (IllegalAccessException e) {
				throw new IllegalArgumentException("Cannot get MethodHandle for method: " + m, e);
			}
		});
	}

	/**
	 * 调用方法句柄
	 */
	public static Object invokeMethodHandle(MethodHandle methodHandle, Object... args) throws Throwable {
		try {
			return methodHandle.invokeWithArguments(args);
		} catch (Throwable throwable) {
			throw new RuntimeException("Error invoking MethodHandle", throwable);
		}
	}

	/**
	 * 创建静态方法句柄
	 */
	public static MethodHandle lookupStaticMethod(Class<?> declaringClass, String methodName,
			Class<?>... parameterTypes) throws NoSuchMethodException, IllegalAccessException {
		Method method = declaringClass.getMethod(methodName, parameterTypes);
		return getMethodHandle(method);
	}

	/**
	 * 创建实例方法句柄
	 */
	public static MethodHandle lookupInstanceMethod(Class<?> declaringClass, String methodName,
			Class<?>... parameterTypes) throws NoSuchMethodException, IllegalAccessException {
		MethodType methodType = MethodType.methodType(void.class, parameterTypes);
		return MethodHandles.lookup().findVirtual(declaringClass, methodName, methodType);
	}
	
	/**
	 * 创建实例方法句柄
	 */
	public static MethodHandle lookupInstanceMethod(Class<?> declaringClass, String methodName, Class<?> returnType,
			Class<?>... parameterTypes) throws NoSuchMethodException, IllegalAccessException {
		MethodType methodType = MethodType.methodType(returnType, parameterTypes);
		return MethodHandles.lookup().findVirtual(declaringClass, methodName, methodType);
	}
}
