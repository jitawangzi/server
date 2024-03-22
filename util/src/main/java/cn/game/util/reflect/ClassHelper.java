package cn.game.util.reflect;

import java.util.HashSet;
import java.util.Set;

import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.core.type.filter.TypeFilter;

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

}
