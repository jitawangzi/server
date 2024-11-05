package cn.game.util.reflect;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.core.type.filter.TypeFilter;
import org.springframework.util.ReflectionUtils;

public class ClassHelper {
	public static <T> Set<Class<? extends T>> findSubclasses(String basePackage, Class<? extends T> superClass) {
		ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(false);
		TypeFilter filter = new AssignableTypeFilter(superClass);

		provider.addIncludeFilter(filter);

		Set<Class<? extends T>> subclasses = new HashSet<>();
		for (org.springframework.beans.factory.config.BeanDefinition candidate : provider
				.findCandidateComponents(basePackage)) {
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
	 * 反射查找Method，通过spring ReflectionUtils 缓存的Method加速查找
	 * @param clazz
	 * @param methodName
	 * @param args
	 * @return
	 */
	public static Method findMethod(Class<?> clazz, String methodName, Object... args) {

		Class<?>[] paramTypes = null;
		if (args != null) {
			paramTypes = new Class[args.length];
			for (int i = 0; i < args.length; i++) {
				paramTypes[i] = args[i] == null ? null : args[i].getClass();
			}
		}
		Method method = ReflectionUtils.findMethod(clazz, methodName, paramTypes);
		if (method == null) {
			throw new NoSuchMethodError("Method " + methodName + " not found in " + clazz.getName());
		}
		return method;
	}
}
