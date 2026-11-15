package cn.game.games.net.game.gm;

import java.lang.reflect.Method;
import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import cn.game.core.base.RunMode;

/**
 * GM Schema自描述完整性与生产安全裁剪测试
 *
 * @author shuyongqiang
 */
public class GmSchemaCompletenessTest {

	/**
	 * 扫描cn.game.games生产类路径下全部@GmGroup类并通过Spring装配注册到独立GmDispatcher
	 *
	 * @return 完整注册的GmDispatcher实例
	 * @throws Exception 扫描或装配异常
	 */
	private GmDispatcher createRegisteredDispatcherFromScan() throws Exception {
		ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
		scanner.addIncludeFilter(new AnnotationTypeFilter(GmGroup.class));
		Set<BeanDefinition> candidates = scanner.findCandidateComponents("cn.game.games");
		Assertions.assertFalse(candidates.isEmpty(), "包扫描必须至少发现一个@GmGroup类");

		Set<Class<?>> discoveredClasses = new LinkedHashSet<>();
		for (BeanDefinition bd : candidates) {
			Class<?> clazz = Class.forName(bd.getBeanClassName());
			if (isExcludedFromProductionScan(clazz)) {
				continue;
			}
			discoveredClasses.add(clazz);
		}
		Assertions.assertTrue(discoveredClasses.contains(PlayerInspectService.class),
				"包扫描必须自动发现PlayerInspectService");

		GmDispatcher dispatcher = new GmDispatcher(null);
		try (AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext()) {
			ctx.registerBean(ConfigQueryService.class);
			ctx.registerBean(ProtoCatalogService.class);
			for (Class<?> clazz : discoveredClasses) {
				ctx.registerBean(clazz);
			}
			ctx.refresh();
			for (Object bean : ctx.getBeansWithAnnotation(GmGroup.class).values()) {
				dispatcher.registerBean(bean);
			}
		}
		return dispatcher;
	}

	/**
	 * TW-01：自动包扫描发现的全部@GmGroup/@GmAction均完整进入Schema目录且别名唯一
	 *
	 * @throws Exception 扫描或反射异常
	 */
	@Test
	void schemaCompletenessCoversAllRegisteredActions() throws Exception {
		GmDispatcher dispatcher = createRegisteredDispatcherFromScan();
		GmSchemaCatalog devCatalog = dispatcher.getSchemaCatalog(RunMode.DEV);
		Assertions.assertEquals(GmResult.SCHEMA_VERSION, devCatalog.getSchemaVersion());
		Assertions.assertTrue(devCatalog.getGroups().size() >= 8);

		Set<String> schemaIds = new LinkedHashSet<>();
		for (GmActionSchema action : devCatalog.getActions()) {
			Assertions.assertNotNull(action.getId());
			Assertions.assertFalse(action.getId().isBlank());
			Assertions.assertNotNull(action.getGroup(), "Schema action group不能为空: " + action.getId());
			Assertions.assertFalse(action.getGroup().isBlank(), "Schema action group不能为空白: " + action.getId());
			Assertions.assertNotNull(action.getAction(), "Schema action name不能为空: " + action.getId());
			Assertions.assertFalse(action.getAction().isBlank(), "Schema action name不能为空白: " + action.getId());
			Assertions.assertNotNull(action.getDescription());
			Assertions.assertFalse(action.getDescription().isBlank());
			Assertions.assertNotNull(action.getSecurity());
			Assertions.assertNotNull(action.getTier(), "Schema action tier不能为空: " + action.getId());
			Assertions.assertNotNull(action.getScope());
			Assertions.assertNotNull(action.getParams(), "Schema action params列表不能为null: " + action.getId());
			for (GmParamSchema param : action.getParams()) {
				Assertions.assertNotNull(param.getName(), "Schema参数名不能为null: " + action.getId());
				Assertions.assertFalse(param.getName().isBlank(), "Schema参数名不能为空白: " + action.getId());
				Assertions.assertFalse(param.getName().matches("arg\\d+"),
						"编译参数名未保留(-parameters未生效): " + action.getId() + "." + param.getName());
				Assertions.assertNotNull(param.getParamType(),
						"Schema参数类型不能为null: " + action.getId() + "." + param.getName());
			}
			Assertions.assertTrue(schemaIds.add(action.getId()), "Schema action id重复: " + action.getId());
		}

		ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
		scanner.addIncludeFilter(new AnnotationTypeFilter(GmGroup.class));
		for (BeanDefinition bd : scanner.findCandidateComponents("cn.game.games")) {
			Class<?> clazz = Class.forName(bd.getBeanClassName());
			if (isExcludedFromProductionScan(clazz)) {
				continue;
			}
			GmGroup group = clazz.getAnnotation(GmGroup.class);
			for (Method method : clazz.getDeclaredMethods()) {
				GmAction action = method.getAnnotation(GmAction.class);
				if (action == null) {
					continue;
				}
				String actionName = action.name().isEmpty() ? method.getName() : action.name();
				String fullId = group.name() + "." + actionName;
				Assertions.assertTrue(schemaIds.contains(fullId), "包扫描动作缺失于Schema: " + fullId);
			}
		}

		Assertions.assertTrue(schemaIds.contains("gm.schema"));
		Assertions.assertTrue(schemaIds.contains("gm.inspect"));
		Assertions.assertTrue(schemaIds.contains("config.tables"));
		Assertions.assertTrue(schemaIds.contains("proto.catalog"));
		Assertions.assertTrue(schemaIds.contains("player.time.status"));
		Assertions.assertTrue(schemaIds.contains("player.time.offset"));
		Assertions.assertTrue(schemaIds.contains("player.time.set"));
		Assertions.assertTrue(schemaIds.contains("player.time.reset"));
		Assertions.assertTrue(schemaIds.contains("player.time"));

		GmSchemaCatalog filtered = dispatcher.getSchemaCatalog(RunMode.DEV, "config");
		Assertions.assertFalse(filtered.getActions().isEmpty());
		for (GmActionSchema action : filtered.getActions()) {
			Assertions.assertEquals("config", action.getGroup());
		}
	}

	/**
	 * 验证Schema契约字段及PRODUCTION模式下DEV_TEST_ONLY动作被安全裁剪
	 *
	 * @throws Exception 扫描或反射异常
	 */
	@Test
	void schemaMatchesDesignContractAndDisablesProductionActions() throws Exception {
		GmDispatcher dispatcher = createRegisteredDispatcherFromScan();
		GmSchemaCatalog prodCatalog = dispatcher.getSchemaCatalog(RunMode.PRODUCTION);
		Assertions.assertTrue(prodCatalog.getActions().stream().noneMatch(a -> a.getSecurity() == GmSecurityTier.DEV_TEST_ONLY));
		Assertions.assertTrue(prodCatalog.getActions().stream().anyMatch(a -> "gm.inspect".equals(a.getId())));
		Assertions.assertTrue(prodCatalog.getActions().stream().anyMatch(a -> "config.get".equals(a.getId())));
		Assertions.assertTrue(prodCatalog.getActions().stream().anyMatch(a -> "proto.catalog".equals(a.getId())));
		Assertions.assertTrue(prodCatalog.getActions().stream().noneMatch(a -> a.getId().startsWith("player.time.") || "player.time".equals(a.getId())));
	}

	private static boolean isExcludedFromProductionScan(Class<?> clazz) {
		if (GmDispatcher.class.equals(clazz.getEnclosingClass())) {
			return true;
		}
		java.security.CodeSource source = clazz.getProtectionDomain() != null ? clazz.getProtectionDomain().getCodeSource() : null;
		if (source != null && source.getLocation() != null) {
			String path = source.getLocation().getPath();
			if (path != null && path.contains("test-classes")) {
				return true;
			}
		}
		return false;
	}
}