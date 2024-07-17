package cn.game.games.net.game.constant;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.filter.TypeFilter;

/**
 * 常用的数据库操作方法字符串
 * 2017年4月14日 下午4:34:30
 * @author SYQ
 */
public class MapperConstant {

	public static final Map<Class<?>, Map<String, Method>> methodsMap = new HashMap<Class<?>, Map<String, Method>>();

	public static final String deleteByPrimaryKey = "deleteByPrimaryKey";
	public static final String deleteBatch = "deleteBatch";
	public static final String insert = "insert";
	public static final String insertSelective = "insertSelective";
	public static final String insertOrUpdate = "insertOrUpdate";
	public static final String insertBatch = "insertBatch";
	public static final String selectByPrimaryKey = "selectByPrimaryKey";
	/** 有选择的更新字段，可以更新blob字段 */
	public static final String updateByPrimaryKeySelective = "updateByPrimaryKeySelective";
	/** 更新整行全部数据，带blob字段 */
	public static final String updateByPrimaryKeyWithBLOBs = "updateByPrimaryKeyWithBLOBs";
	/** 更新整行数据，不包含blob字段 */
	public static final String updateByPrimaryKey = "updateByPrimaryKey";
	public static final String updateBatch = "updateBatch";

	// 下面两个是自定义的
	/** 按playerId查询数据的 */
	public static final String selectByPlayerId = "selectByPlayerId";
	/** 按uid查询数据的 */
	public static final String selectByUid = "selectByUid";
	/** 查询全部的 */
	public static final String selectAll = "selectAll";

	public static final String deleteByIds = "deleteByIds";

	static {
		Set<Class<?>> scanClass = scanMapperClass("cn.game.games.net.data.mapper");
		for (Class<?> clazz : scanClass) {
			Map<String, Method> map = methodsMap.get(clazz); 
			if (map == null) {
				map = new HashMap<String, Method>(); 
				methodsMap.put(clazz, map); 
			}
			Method[] methods = clazz.getMethods(); 
			for (Method m : methods) {
				Method old = map.put(m.getName(), m); 
				if (old != null) {
					throw new IllegalArgumentException(
							String.format("mapper[%s] repeated method[%s]", clazz.getSimpleName(), m.getName()));
				}
			}
		}
	}

	public static Method getMethod(Class<?> mapper, String method) {
		Method method2 = methodsMap.get(mapper).get(method);
		Objects.requireNonNull(method2,
				String.format("can not find method[%s],mapper[%s]", method, mapper.getSimpleName()));
		return method2;
	}

	public static Set<Class<?>> scanMapperClass(String packageName) {
		Set<Class<?>> classes = new HashSet<>();
		ClassPathScanningCandidateComponentProvider scanner = new MapperScanningCandidateComponentProvider(false);
		// 这里可以根据需要设置过滤条件，比如注解类型或者父类类型
		TypeFilter filter = new TypeFilter() {
			@Override
			public boolean match(MetadataReader metadataReader, MetadataReaderFactory metadataReaderFactory)
					throws IOException {
				String className = metadataReader.getClassMetadata().getClassName();
				return className.endsWith("Mapper");
			}
		};
		scanner.addIncludeFilter(filter);
		for (org.springframework.beans.factory.config.BeanDefinition bd : scanner
				.findCandidateComponents(packageName)) {
			try {
				Class<?> clazz = Class.forName(bd.getBeanClassName());
				classes.add(clazz);
			} catch (ClassNotFoundException e) {
				// Handle exception
			}
		}
		return classes;
	}

	private static class MapperScanningCandidateComponentProvider extends ClassPathScanningCandidateComponentProvider {
		public MapperScanningCandidateComponentProvider(boolean f) {
			super(f);
		}
		@Override
		protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
			return beanDefinition.getMetadata().isInterface() && beanDefinition.getMetadata().isIndependent();
		}
	}
}
