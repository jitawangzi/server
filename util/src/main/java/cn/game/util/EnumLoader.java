package cn.game.util;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.stereotype.Component;

/**    
 * 扫描指定包下的所有枚举类，以便触发静态代码块执行，检查枚举重复值
 * 2025年1月28日 10:29:27
 * @author SYQ
 */
@Component
public class EnumLoader {

	@PostConstruct
	public void load() {
		loadAllEnums("cn.game");
	}

	public static void loadAllEnums(String packageName) {
		try {
			ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(false);
			provider.addIncludeFilter(new AssignableTypeFilter(Enum.class));

			for (BeanDefinition beanDef : provider.findCandidateComponents(packageName)) {
				Class.forName(beanDef.getBeanClassName());
			}
		} catch (Exception e) {
			throw new RuntimeException("Failed to validate enums", e);
		}
	}
}

