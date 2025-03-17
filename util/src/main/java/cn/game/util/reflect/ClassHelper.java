package cn.game.util.reflect;

import java.io.File;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.core.type.filter.TypeFilter;
import org.springframework.util.ReflectionUtils;

public class ClassHelper {
	/** 按照优先级定义可能的方法名 */
	private static final String[] singletonMethodNames = { "getInstance", "getSingleton", "instance", "getDefault", "get" };

	public static <T> Set<Class<? extends T>> findSubclasses(String basePackage, Class<? extends T> superClass) {
		ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(false);
		TypeFilter filter = new AssignableTypeFilter(superClass);

		provider.addIncludeFilter(filter);

		Set<Class<? extends T>> subclasses = new HashSet<>();
		for (org.springframework.beans.factory.config.BeanDefinition candidate : provider.findCandidateComponents(basePackage)) {
			try {
				Class<? extends T> cls = (Class<? extends T>) Class.forName(candidate.getBeanClassName());
				boolean b = subclasses.add(cls);
				if (!b) {
					throw new IllegalArgumentException("重复的类名称: " + candidate.getBeanClassName());
				}
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}

		return subclasses;
	}

	/** 
	 * 从获取一个jar文件的绝对路径，优先从classpath中查找
	 * @param jarName
	 * @return jar文件的绝对路径 ，null 不存在
	 */
	public static String findJarPath(String jarName) {
		// 1. 尝试直接从 classpath 获取
		String classPath = System.getProperty("java.class.path");
		String[] paths = classPath.split(File.pathSeparator);
		for (String path : paths) {
			if (path.endsWith(jarName)) {
				File file = new File(path);
				if (file.exists()) {
					return file.getAbsolutePath();
				}
			}
		}
		// 2. 尝试使用不同的类加载器
		ClassLoader[] loaders = new ClassLoader[] { Thread.currentThread().getContextClassLoader(), ClassLoader.getSystemClassLoader(),
				ClassHelper.class.getClassLoader() };

		for (ClassLoader loader : loaders) {
			if (loader != null) {
				// 尝试不同的路径组合
				String[] pathPrefixes = { "", "lib/", "../lib/" };
				for (String prefix : pathPrefixes) {
					try {
						URL resource = loader.getResource(prefix + jarName);
						if (resource != null) {
							return new File(resource.toURI()).getAbsolutePath();
						}
					} catch (Exception e) {
						// 记录异常但继续尝试
						e.printStackTrace();
					}
				}
			}
		}

		// 3. 回退到文件系统直接查找
		String userDir = System.getProperty("user.dir");
		String[] searchDirs = { userDir + "/lib", userDir + "/../lib", userDir };

		for (String dir : searchDirs) {
			File jarFile = new File(dir, jarName);
			if (jarFile.exists()) {
				return jarFile.getAbsolutePath();
			}
		}
		return null;
	}

	/** 
	 * 通过方法参数值反射查找Method
	 * 对于复杂参数类型支持不够完善，尽量使用参数类型精确查找
	 * @param clazz
	 * @param methodName
	 * @param args 注意是方法参数值
	 * @return
	 */
	public static Method findMethodByArgs(Class<?> clazz, String methodName, Object... args) {
		if (args == null) {
			return ReflectionUtils.findMethod(clazz, methodName);
		}

		// 1. 先尝试精确匹配
		Class<?>[] paramTypes = new Class[args.length];
		for (int i = 0; i < args.length; i++) {
			if (args[i] == null) {
				paramTypes[i] = null;
				continue;
			}
			paramTypes[i] = args[i].getClass();
		}

		Method method = ReflectionUtils.findMethod(clazz, methodName, paramTypes);
		if (method != null) {
			return method;
		}

		// 2. 获取所有同名方法
		Method[] methods = ReflectionUtils.getAllDeclaredMethods(clazz);
		for (Method m : methods) {
			if (!m.getName().equals(methodName)) {
				continue;
			}

			Class<?>[] parameterTypes = m.getParameterTypes();
			if (parameterTypes.length != args.length) {
				continue;
			}

			boolean match = true;
			for (int i = 0; i < parameterTypes.length; i++) {
				if (args[i] == null) {
					continue;
				}

				// 处理基本类型
				if (parameterTypes[i].isPrimitive()) {
					if (!isPrimitiveWrapperType(args[i].getClass(), parameterTypes[i])) {
						match = false;
						break;
					}
				}
				// 处理继承关系
				else if (!parameterTypes[i].isAssignableFrom(args[i].getClass())) {
					match = false;
					break;
				}
			}

			if (match) {
				return m;
			}
		}

		throw new NoSuchMethodError("Method " + methodName + " not found in " + clazz.getName());
	}

	private static boolean isPrimitiveWrapperType(Class<?> wrapperType, Class<?> primitiveType) {
		return (primitiveType == int.class && wrapperType == Integer.class) || (primitiveType == long.class && wrapperType == Long.class)
				|| (primitiveType == double.class && wrapperType == Double.class)
				|| (primitiveType == float.class && wrapperType == Float.class)
				|| (primitiveType == boolean.class && wrapperType == Boolean.class)
				|| (primitiveType == char.class && wrapperType == Character.class)
				|| (primitiveType == byte.class && wrapperType == Byte.class)
				|| (primitiveType == short.class && wrapperType == Short.class);
	}

	/** 
	 * 反射查找Method，通过spring ReflectionUtils 缓存的Method加速查找
	 * 通过参数类型精确匹配
	 * @param clazz
	 * @param methodName 方法名
	 * @param paramTypes 方法类型
	 * @return
	 */
	public static Method findMethod(Class<?> clazz, String methodName, Class<?>... paramTypes) {
		Method method = ReflectionUtils.findMethod(clazz, methodName, paramTypes);
		if (method == null) {
			throw new NoSuchMethodError("Method " + methodName + " not found in " + clazz.getName());
		}
		return method;
	}

	public static Method findMethod(Class<?> clazz, String methodName) {
		Method method = ReflectionUtils.findMethod(clazz, methodName);
		if (method == null) {
			throw new NoSuchMethodError("Method " + methodName + " not found in " + clazz.getName());
		}
		return method;
	}

	public static Object getSingletonInstance(Class<?> clazz) {
		// 按优先级尝试常见的单例获取方法
		Method[] methods = clazz.getDeclaredMethods();

		// 1. 先尝试完全匹配的静态无参方法
		for (String methodName : singletonMethodNames) {
			try {
				Method method = clazz.getDeclaredMethod(methodName);
				if (Modifier.isStatic(method.getModifiers()) && method.getReturnType().isAssignableFrom(clazz)) {
					method.setAccessible(true);
					return method.invoke(null);
				}
			} catch (NoSuchMethodException e) {
				// 继续尝试下一个方法名
				continue;
			} catch (Exception e) {
				throw new RuntimeException("Failed to get singleton instance", e);
			}
		}

		// 2. 如果没找到完全匹配的，尝试模糊匹配
		for (Method method : methods) {
			if (Modifier.isStatic(method.getModifiers()) && method.getParameterCount() == 0
					&& method.getReturnType().isAssignableFrom(clazz)) {

				String methodName = method.getName().toLowerCase();
				if (methodName.contains("instance") || methodName.contains("singleton") || methodName.contains("get")) {

					method.setAccessible(true);
					try {
						return method.invoke(null);
					} catch (Exception e) {
						throw new RuntimeException("Failed to get singleton instance", e);
					}
				}
			}
		}

		throw new IllegalStateException("No singleton instance accessor found for class: " + clazz.getName());
	}

	/**
	 * 从泛型接口或泛型父类中提取指定位置的泛型参数类型
	 *
	 * @param obj 目标对象
	 * @param targetType 目标泛型类或接口,例如实现了多个接口，用来区分哪一个
	 * @param paramIndex 泛型参数位置索引(从0开始)
	 * @return 参数类型的Class对象
	 */
	public static Class<?> getGenericParameterType(Object obj, Class<?> targetType, int paramIndex) {
		Class<?> clazz = obj.getClass();

		// 检查类实现的接口
		Type[] interfaces = clazz.getGenericInterfaces();
		for (Type type : interfaces) {
			if (type instanceof ParameterizedType) {
				ParameterizedType pType = (ParameterizedType) type;
				if (pType.getRawType().equals(targetType)) {
					Type[] typeArgs = pType.getActualTypeArguments();
					if (paramIndex >= 0 && paramIndex < typeArgs.length) {
						return extractClass(typeArgs[paramIndex]);
					}
				}
			}
		}

		// 检查父类
		Type superClass = clazz.getGenericSuperclass();
		if (superClass instanceof ParameterizedType) {
			ParameterizedType pType = (ParameterizedType) superClass;
			if (pType.getRawType().equals(targetType)) {
				Type[] typeArgs = pType.getActualTypeArguments();
				if (paramIndex >= 0 && paramIndex < typeArgs.length) {
					return extractClass(typeArgs[paramIndex]);
				}
			}
		}

		throw new RuntimeException("无法确定泛型参数类型");
	}

	/**
	 * 从泛型接口或泛型父类中提取指定位置的泛型参数类型
	 * 当类只有一个泛型接口或一个泛型父类时，自动检测并提取泛型参数
	 * 
	 * @param obj 目标对象
	 * @param paramIndex 泛型参数位置索引(从0开始)
	 * @return 参数类型的Class对象
	 * @throws RuntimeException 如果找到多个泛型类型或没有找到泛型类型
	 */
	public static Class<?> getGenericParameterType(Object obj, int paramIndex) {
		Class<?> clazz = obj.getClass();
		List<ParameterizedType> parameterizedTypes = new ArrayList<>();

		// 检查类实现的接口
		Type[] interfaces = clazz.getGenericInterfaces();
		for (Type type : interfaces) {
			if (type instanceof ParameterizedType) {
				parameterizedTypes.add((ParameterizedType) type);
			}
		}

		// 检查父类
		Type superClass = clazz.getGenericSuperclass();
		if (superClass instanceof ParameterizedType) {
			parameterizedTypes.add((ParameterizedType) superClass);
		}

		// 判断泛型类型数量
		if (parameterizedTypes.isEmpty()) {
			throw new RuntimeException("未找到泛型接口或父类");
		} else if (parameterizedTypes.size() > 1) {
			throw new RuntimeException("找到多个泛型类型，请使用带targetType参数的方法明确指定");
		}

		// 只有一个泛型类型，直接使用
		ParameterizedType pType = parameterizedTypes.get(0);
		Type[] typeArgs = pType.getActualTypeArguments();
		if (paramIndex >= 0 && paramIndex < typeArgs.length) {
			return extractClass(typeArgs[paramIndex]);
		} else {
			throw new RuntimeException("泛型参数索引无效: " + paramIndex);
		}
	}

	/**
	 * 提取Type对象的实际Class类型
	 *
	 * @param type 类型对象
	 * @return Class对象
	 */
	public static Class<?> extractClass(Type type) {
		if (type instanceof Class) {
			return (Class<?>) type;
		} else if (type instanceof ParameterizedType) {
			return (Class<?>) ((ParameterizedType) type).getRawType();
		} else {
			throw new RuntimeException("不支持的泛型类型: " + type);
		}
	}
}
