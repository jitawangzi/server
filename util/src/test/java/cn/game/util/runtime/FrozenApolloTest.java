package cn.game.util.runtime;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.ctrip.framework.apollo.Config;
import com.ctrip.framework.apollo.ConfigFile;
import com.ctrip.framework.apollo.ConfigFileChangeListener;
import com.ctrip.framework.apollo.ConfigService;
import com.ctrip.framework.apollo.core.enums.ConfigFileFormat;
import com.ctrip.framework.apollo.enums.ConfigSourceType;
import com.ctrip.framework.apollo.internals.AbstractConfig;
import com.ctrip.framework.apollo.model.ConfigChangeEvent;
import com.ctrip.framework.apollo.model.ConfigFileChangeEvent;
import com.ctrip.framework.apollo.spi.ConfigFactory;

/** 在独立 JVM 中验证真正的 Apollo SPI，避免 ConfigService 静态缓存掩盖顺序错误。 */
public class FrozenApolloTest {
    /** 隔离每个独立 JVM 的输出。 */
    @TempDir Path directory;

    /** 真实 SPI 必须在首次读取时生效，XML 恢复不能解除撤销。 */
    @Test void strictSpiFreezesValuesAndRevokesOnRestoredUpdate() throws Exception { runChild("strict"); }
    /** properties 更新不能把固定存储目标切换到新值。 */
    @Test void propertyUpdatesDoNotRedirectStorageAndRemainRevoked() throws Exception { runChild("properties"); }
    /** 默认入口保留原 Apollo 注入路径。 */
    @Test void ordinaryModeDoesNotReplaceApollo() throws Exception { runChild("ordinary"); }
    /** 同名日志文件被声明为 Spring 输入时不能享有日志用途的豁免。 */
    @Test void springLog4jNameDoesNotBypassValidation() throws Exception { runChild("spring-log4j"); }
    /** 外部属性来源的命名空间和 Bean 写法都在屏障前拒绝。 */
    @Test void externalSpringConfigurationIsRejected() {
        Assertions.assertThrows(IllegalStateException.class, () -> FrozenApollo.validateSpringXml("<beans><import resource='file:/outside.xml'/></beans>"));
        Assertions.assertThrows(IllegalStateException.class, () -> FrozenApollo.validateSpringXml("<beans><property-placeholder location='file:/outside.properties'/></beans>"));
        Assertions.assertThrows(IllegalStateException.class, () -> FrozenApollo.validateSpringXml("<beans><property-override location='file:/override.properties'/></beans>"));
        Assertions.assertThrows(IllegalStateException.class, () -> FrozenApollo.validateSpringXml("<beans xmlns:util='http://www.springframework.org/schema/util'><util:properties location='file:/outside.properties'/></beans>"));
        Assertions.assertThrows(IllegalStateException.class, () -> FrozenApollo.validateSpringXml("<beans><bean class='org.springframework.beans.factory.config.PropertyPlaceholderConfigurer'><property name='location' value='file:/outside.properties'/></bean></beans>"));
        Assertions.assertThrows(IllegalStateException.class, () -> FrozenApollo.validateSpringXml("<beans><bean class='org.springframework.beans.factory.config.PropertiesFactoryBean'><property name='locations'><list><value>file:/outside.properties</value></list></property></bean></beans>"));
        for (String attribute : java.util.List.of("location", "locations", "location-ref", "locations-ref")) {
            Assertions.assertThrows(IllegalStateException.class, () -> FrozenApollo.validateSpringXml("<beans xmlns:p='http://www.springframework.org/schema/p'><bean p:" + attribute + "='external'/></beans>"));
        }
        Assertions.assertThrows(IllegalStateException.class, () -> FrozenApollo.validateSpringXml("<beans><bean class='org.springframework.beans.factory.config.PropertiesFactoryBean'/></beans>"));
        Assertions.assertThrows(IllegalStateException.class, () -> FrozenApollo.validateSpringXml("<beans><bean class='org.springframework.beans.factory.config.YamlPropertiesFactoryBean'/></beans>"));
        Assertions.assertThrows(IllegalStateException.class, () -> FrozenApollo.validateSpringXml("<beans><bean class='missing.UnverifiedConfigurer'/></beans>"));
        Assertions.assertThrows(IllegalStateException.class, () -> FrozenApollo.validateSpringXml("<beans><bean factory-bean='source' factory-method='make'/></beans>"));
        Assertions.assertThrows(IllegalStateException.class, () -> FrozenApollo.validateSpringXml("<beans xmlns:context='http://www.springframework.org/schema/context'><context:property-placeholder/></beans>"));
        Assertions.assertThrows(IllegalStateException.class, () -> FrozenApollo.validateSpringXml("<beans><property-override/></beans>"));
        Assertions.assertThrows(IllegalStateException.class, () -> FrozenApollo.validateSpringXml("<beans><property value=\"#{systemProperties['fixture.only.system']}\"/></beans>"));
        Assertions.assertThrows(IllegalStateException.class, () -> FrozenApollo.validateSpringXml("<beans><value>&#35;{systemEnvironment['PATH']}</value></beans>"));
        Assertions.assertThrows(IllegalStateException.class, () -> FrozenApollo.validateSpringXml("<beans><bean class='" + FrozenApolloTest.class.getName() + "$NonInitializingPropertiesLoader'/></beans>"));
        Assertions.assertNull(System.getProperty("fixture.class.initialized"));
        FrozenApollo.validateSpringXml("<beans><!-- <import resource='old.xml'/> --></beans>");
    }

    /** @param mode 子进程独立用例，每个用例都重新执行 ServiceLoader 和 ConfigService 初始化。 */
    private void runChild(String mode) throws Exception {
        Path log = directory.resolve(mode + ".log");
        Process process = new ProcessBuilder(Path.of(System.getProperty("java.home"), "bin", "java").toString(),
                "-cp", System.getProperty("surefire.test.class.path", System.getProperty("java.class.path")),
                FrozenApolloTest.class.getName(), mode).redirectErrorStream(true).redirectOutput(log.toFile()).start();
        try {
            Assertions.assertTrue(process.waitFor(30, TimeUnit.SECONDS), "Child timeout");
            Assertions.assertEquals(0, process.exitValue(), Files.readString(log));
            Assertions.assertTrue(Files.readString(log).contains("FROZEN_APOLLO_PASS:" + mode));
        } finally { if (process.isAlive()) { process.destroyForcibly(); } }
    }

    /** @param args 严格/普通模式；仅使用内存配置来源，不初始化业务存储。 */
    public static void main(String[] args) throws Exception {
        if (args[0].equals("ordinary")) {
            Assertions.assertNull(new FrozenApolloInjector().getInstance(com.ctrip.framework.apollo.internals.ConfigManager.class));
        } else {
            System.setProperty("runtime.config.freeze", "true");
            System.setProperty("runtime.test.fallback", "reserved");
            System.setProperty("fixture.only.system", "before");
            System.setProperty("runtime.config.overrideKeys", "fixture.only.system");
            Assertions.assertThrows(IllegalStateException.class, ConfigService::getAppConfig);
            Fixture factory = new Fixture();
            if (args[0].equals("spring-log4j")) {
                factory.springArgs = "log4j2.xml";
                factory.xml.content = "<beans><import resource='file:/outside.xml'/></beans>";
                Assertions.assertThrows(IllegalStateException.class, () -> FrozenApollo.INSTANCE.freeze(factory));
                System.out.println("FROZEN_APOLLO_PASS:" + args[0]);
                System.exit(0);
            }
            FrozenApollo.INSTANCE.freeze(factory);
            Config config = ConfigService.getConfig("jdbc");
            Assertions.assertEquals("storage-A", config.getProperty("dburl", ""));
            Assertions.assertEquals("before", config.getProperty("fixture.only.system", ""));
            Assertions.assertEquals(16, config.getIntProperty("poolSize", 0));
            Assertions.assertFalse(config.getPropertyNames().contains("fixture.only.system"));
            System.setProperty("fixture.only.system", "after");
            Assertions.assertEquals("before", config.getProperty("fixture.only.system", ""));
            try (var context = FrozenApollo.INSTANCE.newSpringContext()) {
                Assertions.assertEquals("before", context.getEnvironment().getProperty("fixture.only.system"));
                context.refresh();
                Assertions.assertEquals("before", ((Map<?, ?>) context.getBean("systemProperties")).get("fixture.only.system"));
            }
            try (var context = FrozenApollo.INSTANCE.newSpringContext()) {
                var definition = new org.springframework.beans.factory.support.RootBeanDefinition(StringBuilder.class);
                definition.getConstructorArgumentValues().addGenericArgumentValue("#{systemProperties['fixture.only.system']}");
                context.registerBeanDefinition("dynamicValue", definition);
                Assertions.assertThrows(org.springframework.beans.BeansException.class, context::refresh);
            }
            Assertions.assertThrows(IllegalStateException.class, () -> config.getProperty("runtime.test.fallback", "missing"));
            Assertions.assertThrows(IllegalStateException.class, () -> config.getBooleanProperty("runtime.test.fallback", false));
            Assertions.assertThrows(IllegalStateException.class, () -> config.getIntProperty("runtime.test.fallback", 0));
            Assertions.assertThrows(IllegalStateException.class, () -> config.getArrayProperty("runtime.test.fallback", ",", new String[0]));
            Assertions.assertThrows(IllegalStateException.class, () -> config.getProperty("runtime.test.fallback", (com.google.common.base.Function<String, String>) value -> value, "missing"));
            Assertions.assertTrue(config.equals(config));
            Assertions.assertFalse(FrozenApollo.INSTANCE.identityValues().containsKey("fallback/runtime.test.fallback"));
            Assertions.assertThrows(IllegalStateException.class, () -> ConfigService.getConfig("undeclared"));
            Assertions.assertThrows(IllegalStateException.class, () -> ConfigService.getConfigFile("fixture", ConfigFileFormat.JSON));
            Assertions.assertThrows(IllegalStateException.class, () -> ConfigService.getConfigFile("undeclared", ConfigFileFormat.XML));
            factory.jdbc.values.put("dburl", "storage-B");
            Assertions.assertEquals("storage-A", config.getProperty("dburl", ""));
            if (args[0].equals("properties")) {
                factory.jdbc.update("storage-B");
                factory.jdbc.update("storage-A");
                org.awaitility.Awaitility.await().atMost(5, TimeUnit.SECONDS).untilAsserted(
                        () -> Assertions.assertThrows(IllegalStateException.class, FrozenApollo.INSTANCE::assertUnchanged));
            } else {
                // XML 更新是同步夹具事件；正文恢复和后续系统覆盖都不能恢复身份。
                factory.xml.update("<changed/>");
                factory.xml.update("<original/>");
            }
            Assertions.assertEquals("<original/>", ConfigService.getConfigFile("fixture", ConfigFileFormat.XML).getContent());
            Assertions.assertThrows(IllegalStateException.class, FrozenApollo.INSTANCE::assertUnchanged);
            Assertions.assertEquals("storage-A", config.getProperty("dburl", ""));
        }
        System.out.println("FROZEN_APOLLO_PASS:" + args[0]);
        System.exit(0);
    }

    private static final class Fixture implements ConfigFactory {
        /** 当前场景需要的 Spring 输入名。 */
        String springArgs = "fixture.xml";
        /** 受控的可变存储配置。 */
        final MutableConfig jdbc = new MutableConfig(Map.of("dburl", "storage-A", "poolSize", "16"));
        /** 受控 XML 来源，用于区分业务固定值和更新事件。 */
        final MutableFile xml = new MutableFile();
        @Override public Config create(String namespace) {
            return namespace.equals("jdbc") ? jdbc : new MutableConfig(namespace.equals("application") ? Map.of("springArgs", springArgs) : Map.of());
        }
        @Override public ConfigFile createConfigFile(String namespace, ConfigFileFormat format) { return xml; }
    }

    private static final class MutableConfig extends AbstractConfig {
        /** 模拟原始 Apollo 仓库，允许发生更新。 */
        final Map<String, String> values;
        /** @param values 原始键值，复制后供夹具修改。 */
        MutableConfig(Map<String, String> values) { this.values = new HashMap<>(values); }
        /** @param value 下一版本存储目标，使用真实 Apollo 事件派发器通知。 */
        void update(String value) {
            String old = values.put("dburl", value);
            var change = new com.ctrip.framework.apollo.model.ConfigChange("jdbc", "dburl", old, value, com.ctrip.framework.apollo.enums.PropertyChangeType.MODIFIED);
            fireConfigChange(new ConfigChangeEvent("jdbc", Map.of("dburl", change)));
        }
        @Override public String getProperty(String key, String defaultValue) { return values.getOrDefault(key, defaultValue); }
        @Override public Set<String> getPropertyNames() { return values.keySet(); }
        @Override public ConfigSourceType getSourceType() { return ConfigSourceType.REMOTE; }
    }

    private static final class MutableFile implements ConfigFile {
        /** 当前来源正文，可变而业务副本不可变。 */
        String content = "<original/>";
        /** 冻结管理器在采集前注册的观察者。 */
        final java.util.List<ConfigFileChangeListener> listeners = new java.util.ArrayList<>();
        /** @param value 下一版本 XML 正文。 */
        void update(String value) {
            String old = content; content = value;
            listeners.forEach(listener -> listener.onChange(new ConfigFileChangeEvent("fixture.xml", old, value, com.ctrip.framework.apollo.enums.PropertyChangeType.MODIFIED)));
        }
        @Override public String getContent() { return content; }
        @Override public boolean hasContent() { return true; }
        @Override public String getNamespace() { return "fixture.xml"; }
        @Override public ConfigFileFormat getConfigFileFormat() { return ConfigFileFormat.XML; }
        @Override public ConfigSourceType getSourceType() { return ConfigSourceType.REMOTE; }
        @Override public void addChangeListener(ConfigFileChangeListener listener) { listeners.add(listener); }
        @Override public boolean removeChangeListener(ConfigFileChangeListener listener) { return listeners.remove(listener); }
    }

    /** 类型检查可以加载类元数据，但不得执行配置器的静态初始化。 */
    public static final class NonInitializingPropertiesLoader extends org.springframework.beans.factory.config.PropertiesFactoryBean {
        static { System.setProperty("fixture.class.initialized", "true"); }
    }
}
