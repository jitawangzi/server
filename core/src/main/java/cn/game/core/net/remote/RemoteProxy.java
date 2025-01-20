package cn.game.core.net.remote;

import java.lang.reflect.Method;

import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.util.ReflectionUtils;

import cn.game.util.SpringContextLoader;
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

	/** 
	 * 调用某类的静态方法
	 * @param clzss
	 * @param methodName
	 * @param paramTypes
	 * @param args
	 * @return
	 */
	default Object invoke(Class<?> clzss, String methodName, Class<?>[] paramTypes, Object... args) {
		try {
			Method method = ClassHelper.findMethod(clzss, methodName, paramTypes);
			// 可以调用非public方法
			ReflectionUtils.makeAccessible(method);
			return method.invoke(null, args);
		} catch (Exception e) {
			throw new RuntimeException("RemoteProxy Method invocation failed", e);
		}
	}

	/** 
	 * 调用某类的实例方法
	 * @param clzss
	 * @param methodName
	 * @param paramTypes
	 * @param args
	 * @return
	 */
	default Object invoke(String className, String methodName, Class<?>[] paramTypes, Object... args) {
		try {
			Object bean = null;
			// 1. 先尝试从Spring容器获取
			try {
				bean = SpringContextLoader.getContext().getBean(className);
			} catch (NoSuchBeanDefinitionException e) {
				// Spring容器中没有找到，尝试获取单例实例
				Class<?> clazz = Class.forName(className);
				bean = ClassHelper.getSingletonInstance(clazz);
			}

			if (bean == null) {
				throw new IllegalStateException("No instance found for class: " + className
						+ ". The instance must be either managed by Spring or be a singleton class.");
			}

			Method method = ClassHelper.findMethod(bean.getClass(), methodName, paramTypes);
			ReflectionUtils.makeAccessible(method);
			return method.invoke(bean, args);
		} catch (Exception e) {
			throw new RuntimeException("RemoteProxy Method invocation failed", e);
		}
	}



}