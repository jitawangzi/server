package cn.game.util.runtime;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicBoolean;

import com.ctrip.framework.apollo.Config;
import com.ctrip.framework.apollo.ConfigFile;
import com.ctrip.framework.apollo.ConfigFileChangeListener;
import com.ctrip.framework.apollo.core.enums.ConfigFileFormat;
import com.ctrip.framework.apollo.enums.ConfigSourceType;
import com.ctrip.framework.apollo.internals.AbstractConfig;
import com.ctrip.framework.apollo.internals.ConfigManager;
import com.ctrip.framework.apollo.spi.ConfigFactory;
import com.ctrip.framework.apollo.spi.DefaultConfigFactory;

/** 严格验收进程只使用启动时采集的配置；远端更新仅撤销身份，不再改变业务配置。 */
public final class FrozenApollo implements ConfigManager {
    /** 仅记录失败操作与异常链，不记录配置正文。 */
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(FrozenApollo.class);
    /** 当前 JVM 的固定配置管理器，由 Apollo SPI 返回同一实例。 */
    public static final FrozenApollo INSTANCE = new FrozenApollo();
    /** 两种业务入口需要的 properties 空间，未声明空间不回退远端。 */
    public static final List<String> NAMESPACES = List.of("application", "config", "jdbc", "redisson", "zookeeper", "log4j2");
    /** 原始 Apollo 发生更新时永久置位，即使内容随后恢复。 */
    private final AtomicBoolean changed = new AtomicBoolean();
    /** 最后发布的不可变配置；null 表示屏障还没有完成采集。 */
    private volatile Map<String, Config> configs;
    /** 已声明 XML 的固定正文，与 configs 一起发布。 */
    private Map<String, ConfigFile> files;
    /** 未枚举键的系统/环境回退值，固定后不再读取全局属性。 */
    private Map<String, String> fallback;
    /** 显式声明的仅系统/环境配置键；声明本身也进入身份。 */
    private Set<String> overrideKeys;
    /** Spring 环境和默认 systemProperties Bean 使用同一份已声明系统值。 */
    private Map<String, Object> systemValues;
    /** Spring 默认 systemEnvironment Bean 使用固定环境值，不暴露执行器临时环境。 */
    private Map<String, Object> environmentValues;

    /** 不允许创建第二套业务管理器。 */
    private FrozenApollo() {}

    /** @return 当前 JVM 是否要求固定配置。 */
    public static boolean enabled() { return Boolean.getBoolean("runtime.config.freeze"); }

    /** 在任何业务读取前，通过独立工厂采集；不把可变对象放入 ConfigService 缓存。 */
    public void freeze() { freeze(new DefaultConfigFactory()); }

    /** @param factory 配置来源；包内测试可注入受控更新，不连接真实存储。 */
    synchronized void freeze(ConfigFactory factory) {
        if (configs != null) { throw new IllegalStateException("RUNTIME_CONFIG_ALREADY_FROZEN"); }
        Map<String, Object> system = new HashMap<>();
        System.getProperties().forEach((key, value) -> system.put(key.toString(), value.toString()));
        Map<String, Object> environment = new HashMap<>(System.getenv());
        Map<String, String> overrides = new HashMap<>(System.getenv());
        system.forEach((key, value) -> overrides.put(key, value.toString()));
        Set<String> declared = new java.util.HashSet<>();
        String extraKeys = System.getProperty("runtime.config.overrideKeys", "");
        if (!extraKeys.isBlank()) {
            for (String key : extraKeys.split(",")) {
                if (!key.matches("[A-Za-z0-9_.-]+")) { throw new IllegalStateException("RUNTIME_OVERRIDE_KEY_INVALID"); }
                declared.add(key);
            }
        }
        overrideKeys = Set.copyOf(declared);
        Map<String, Map<String, String>> valuesByNamespace = new HashMap<>();
        Map<String, ConfigSourceType> sources = new HashMap<>();
        Map<String, Config> captured = new HashMap<>();
        for (String namespace : NAMESPACES) {
            Config live = factory.create(namespace);
            live.addChangeListener(event -> changed.set(true));
            Map<String, String> values = new HashMap<>();
            for (String key : live.getPropertyNames()) { values.put(key, live.getProperty(key, "")); }
            valuesByNamespace.put(namespace, values);
            sources.put(namespace, live.getSourceType());
            declared.addAll(values.keySet());
        }
        // 只公开已声明的业务回退。终端/执行器临时环境不成为业务配置，也不造成每次启动摘要不同。
        Set<String> unavailable = new java.util.HashSet<>(overrides.keySet());
        unavailable.removeAll(declared);
        overrides.keySet().retainAll(declared);
        fallback = Map.copyOf(overrides);
        system.keySet().retainAll(declared);
        environment.keySet().retainAll(declared);
        systemValues = Map.copyOf(system);
        environmentValues = Map.copyOf(environment);
        Set<String> blocked = Set.copyOf(unavailable);
        valuesByNamespace.forEach((name, values) -> captured.put(name, guardReads(new SnapshotConfig(values, fallback, blocked, sources.get(name)), blocked)));
        Map<String, ConfigFile> capturedFiles = new HashMap<>();
        String spring = captured.get("application").getProperty("springArgs", "");
        Set<String> springFiles = new java.util.LinkedHashSet<>(java.util.Arrays.asList(spring.split(",")));
        Set<String> requestedFiles = new java.util.LinkedHashSet<>(springFiles);
        requestedFiles.add("log4j2.xml");
        for (String item : requestedFiles) {
            if (!item.matches("[A-Za-z0-9_-]+\\.xml")) { throw new IllegalStateException("RUNTIME_SPRING_ARGS_INVALID"); }
            String name = item.substring(0, item.length() - 4);
            ConfigFile live = factory.createConfigFile(item, ConfigFileFormat.XML);
            live.addChangeListener(event -> changed.set(true));
            String content = live.getContent();
            if (springFiles.contains(item)) { validateSpringXml(content); }
            capturedFiles.put(name, new SnapshotFile(content, live.getNamespace(), live.getSourceType()));
        }
        assertUnchanged();
        files = Map.copyOf(capturedFiles);
        configs = Map.copyOf(captured); // 最后发布，未完成采集的读取一律拒绝。
    }

    /**
     * Apollo 的类型读取会吞异常并返回默认值，因此必须在接口外层拒绝未声明覆盖。
     * @param snapshot 固定值及 Apollo 原有类型解析实现
     * @param blocked 有环境值却未声明的键
     * @return 所有值读取入口都先校验的 Config
     */
    private static Config guardReads(Config snapshot, Set<String> blocked) {
        return (Config) java.lang.reflect.Proxy.newProxyInstance(Config.class.getClassLoader(), new Class<?>[] {Config.class},
                (proxy, method, args) -> {
                    if (method.getDeclaringClass() == Object.class) {
                        return switch (method.getName()) {
                            case "equals" -> proxy == args[0];
                            case "hashCode" -> System.identityHashCode(proxy);
                            case "toString" -> "FrozenApolloConfig";
                            default -> throw new IllegalStateException("RUNTIME_CONFIG_METHOD_UNSUPPORTED");
                        };
                    }
                    if (method.getName().startsWith("get") && args != null && args.length > 0 && args[0] instanceof String key && blocked.contains(key)) {
                        throw new IllegalStateException("RUNTIME_CONFIG_UNDECLARED_OVERRIDE");
                    }
                    try { return method.invoke(snapshot, args); }
                    catch (java.lang.reflect.InvocationTargetException e) {
                        log.warn("固定配置接口读取失败", e.getCause());
                        throw e.getCause();
                    }
                });
    }

    /** @param content 已采集的 Spring XML；拒绝标准外部属性加载入口，避免核验后再读取未采集配置。 */
    static void validateSpringXml(String content) {
        if (content == null || content.isBlank()) { throw new IllegalStateException("RUNTIME_SPRING_CONFIG_MISSING"); }
        try {
            // 使用 JDK 的安全解析器，避免类路径上旧 Xerces 忽略/不识别外部访问限制。
            var factory = javax.xml.parsers.DocumentBuilderFactory.newDefaultInstance();
            factory.setNamespaceAware(true);
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setAttribute(javax.xml.XMLConstants.ACCESS_EXTERNAL_DTD, "");
            factory.setAttribute(javax.xml.XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
            var document = factory.newDocumentBuilder().parse(new org.xml.sax.InputSource(new java.io.StringReader(content)));
            var nodes = document.getElementsByTagNameNS("*", "*");
            for (int i = 0; i < nodes.getLength(); i++) {
                var element = (org.w3c.dom.Element) nodes.item(i);
                String name = element.getLocalName();
                // 覆盖 context、util 命名空间和标准配置器 Bean 的 location/locations 写法。
                Set<String> locations = Set.of("location", "locations", "location-ref", "locations-ref");
                for (int attrIndex = 0; attrIndex < element.getAttributes().getLength(); attrIndex++) {
                    var attribute = element.getAttributes().item(attrIndex);
                    String localName = attribute.getLocalName() == null ? attribute.getNodeName() : attribute.getLocalName();
                    if (locations.contains(localName)) { throw new IllegalStateException("RUNTIME_EXTERNAL_SPRING_CONFIG_UNSUPPORTED"); }
                }
                if (Set.of("import", "property-placeholder", "property-override").contains(name)
                        || element.hasAttribute("factory-method") || element.hasAttribute("factory-bean")
                        || ("property".equals(name) && locations.contains(element.getAttribute("name")))) {
                    throw new IllegalStateException("RUNTIME_EXTERNAL_SPRING_CONFIG_UNSUPPORTED");
                }
                for (int attrIndex = 0; attrIndex < element.getAttributes().getLength(); attrIndex++) {
                    rejectExpression(element.getAttributes().item(attrIndex).getNodeValue());
                }
                rejectExpression(element.getTextContent());
                if ("bean".equals(name) && element.hasAttribute("class")) {
                    Class<?> type = Class.forName(element.getAttribute("class"), false, org.springframework.util.ClassUtils.getDefaultClassLoader());
                    if (org.springframework.core.io.support.PropertiesLoaderSupport.class.isAssignableFrom(type)
                            || org.springframework.beans.factory.config.YamlProcessor.class.isAssignableFrom(type)) {
                        throw new IllegalStateException("RUNTIME_EXTERNAL_SPRING_CONFIG_UNSUPPORTED");
                    }
                }
            }
        } catch (IllegalStateException e) { log.warn("严格启动拒绝外部 Spring 配置", e); throw e; }
        catch (Exception e) { log.warn("严格启动无法解析固定 Spring 配置", e); throw new IllegalStateException("RUNTIME_SPRING_XML_INVALID", e); }
    }

    /** 已观察到更新时，即使原值恢复也永久拒绝身份。 */
    public void assertUnchanged() {
        if (changed.get()) { throw new IllegalStateException("RUNTIME_CONFIG_CHANGED"); }
    }

    /** @return 固定的完整有效输入，用于摘要；不向报告输出明文。 */
    public Map<String, String> identityValues() {
        requireFrozen();
        Map<String, String> result = new TreeMap<>();
        overrideKeys.forEach(key -> result.put("override-key/" + key, "declared"));
        fallback.forEach((key, value) -> result.put("fallback/" + key, value));
        systemValues.forEach((key, value) -> result.put("system/" + key, value.toString()));
        environmentValues.forEach((key, value) -> result.put("environment/" + key, value.toString()));
        configs.forEach((name, config) -> config.getPropertyNames().forEach(key -> result.put(name + "/" + key, config.getProperty(key, ""))));
        files.forEach((name, file) -> result.put("xml/" + name, file.getContent() == null ? "" : file.getContent()));
        return result;
    }

    /** @param value XML 解码或占位符展开后的值；严格启动不支持动态 SpEL 配置。 */
    private static void rejectExpression(String value) {
        if (value != null && value.contains("#{")) { throw new IllegalStateException("RUNTIME_SPRING_EXPRESSION_UNSUPPORTED"); }
    }

    /**
     * Spring 只获得已核验的系统/环境快照；最终值解析也拒绝占位符拼接产生的 SpEL。
     * @return 普通模式的原上下文，或冻结配置模式的受限上下文；调用方随后加载 XML 并 refresh
     */
    public org.springframework.context.support.GenericXmlApplicationContext newSpringContext() {
        if (!enabled()) { return new org.springframework.context.support.GenericXmlApplicationContext(); }
        requireFrozen();
        assertUnchanged();
        var context = new org.springframework.context.support.GenericXmlApplicationContext() {
            @Override protected void prepareBeanFactory(org.springframework.beans.factory.config.ConfigurableListableBeanFactory beanFactory) {
                super.prepareBeanFactory(beanFactory);
                beanFactory.setBeanExpressionResolver((value, evaluationContext) -> { rejectExpression(value); return value; });
            }
        };
        context.setEnvironment(new org.springframework.core.env.StandardEnvironment() {
            @Override public Map<String, Object> getSystemProperties() { return systemValues; }
            @Override public Map<String, Object> getSystemEnvironment() { return environmentValues; }
        });
        return context;
    }

    /** 早于启动屏障的读取不能退回可变 Apollo。 */
    private void requireFrozen() {
        if (configs == null) { throw new IllegalStateException("RUNTIME_CONFIG_NOT_FROZEN"); }
    }

    @Override public Config getConfig(String namespace) {
        requireFrozen();
        Config config = configs.get(namespace);
        if (config == null) { throw new IllegalStateException("RUNTIME_CONFIG_UNDECLARED_NAMESPACE"); }
        return config;
    }

    @Override public ConfigFile getConfigFile(String namespace, ConfigFileFormat format) {
        requireFrozen();
        ConfigFile file = files.get(namespace);
        if (format != ConfigFileFormat.XML || file == null) { throw new IllegalStateException("RUNTIME_CONFIG_UNDECLARED_FILE"); }
        return file;
    }

    /** 保留 Apollo 的枚举语义，系统/环境回退值也在启动时固定。 */
    private static final class SnapshotConfig extends AbstractConfig {
        /** 原空间枚举出的有效值，已应用源配置的覆盖优先级。 */
        private final Map<String, String> values;
        /** 启动时固定的系统及环境回退。 */
        private final Map<String, String> fallback;
        /** 有环境值但未声明的键必须报错，不能悄悄忽略调用方的覆盖。 */
        private final Set<String> blocked;
        /** 采集来源标记，仅用于保持 Apollo API 语义。 */
        private final ConfigSourceType source;
        /** @param values 枚举值 @param fallback 已声明的回退 @param blocked 禁用的未声明环境键 @param source 原始配置来源 */
        private SnapshotConfig(Map<String, String> values, Map<String, String> fallback, Set<String> blocked, ConfigSourceType source) {
            this.values = Map.copyOf(values); this.fallback = fallback; this.blocked = blocked; this.source = source;
        }
        @Override public String getProperty(String key, String defaultValue) {
            if (blocked.contains(key)) { throw new IllegalStateException("RUNTIME_CONFIG_UNDECLARED_OVERRIDE"); }
            return values.getOrDefault(key, fallback.getOrDefault(key, defaultValue));
        }
        @Override public Set<String> getPropertyNames() { return values.keySet(); }
        @Override public ConfigSourceType getSourceType() { return source; }
    }

    /** 固定 XML 正文，不把真实配置事件传给业务重载监听器。 */
    private record SnapshotFile(String content, String namespace, ConfigSourceType source) implements ConfigFile {
        @Override public String getContent() { return content; }
        @Override public boolean hasContent() { return content != null && !content.isEmpty(); }
        @Override public String getNamespace() { return namespace; }
        @Override public ConfigFileFormat getConfigFileFormat() { return ConfigFileFormat.XML; }
        @Override public ConfigSourceType getSourceType() { return source; }
        @Override public void addChangeListener(ConfigFileChangeListener listener) { /* 固定正文不派发业务更新。 */ }
        @Override public boolean removeChangeListener(ConfigFileChangeListener listener) { return false; }
    }
}
