package cn.game.protocol.generated.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cn.game.protocol.generated.config.TestConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class TestManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(TestManager.class);

	private static TestManager instance = new TestManager();
	private static final String xmlFileName = "Test";
	
	/** 总数据，按id取值 */
	private Map<Integer, TestConfig> tests = new HashMap<>();

	public static TestManager instance() {
		return instance;
	}
	private TestManager() {
		WatchServiceManager.getInstance().register(this);
	}
	/**
	 * 根据id获取数据，一般用这个方法，如果数据不存在，一般是配置错误，直接抛出异常
	 * 
	 * @param id
	 * @return
	 */
	public TestConfig get(int id) {
		TestConfig config = this.tests.get(id);
		if (config == null) { 
			throw new NullPointerException("【Test】表的" + "id【" + id + "】不存在"); 
		}
		return config;
	}
	/**
	 * 根据id获取数据，允许返回null
	 * 
	 * @param id
	 * @return
	 */
	public TestConfig getNullable(int id) {
		return this.tests.get(id);
	}

	/**
	 * 获取所有数据
	 * @return
	 */
	public Collection<TestConfig> list() {
		return this.tests.values();
	}
	@Override
	public void load() {
		try {
			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, TestConfig> tests = new HashMap<>();
			for (Element e : list) {
				TestConfig test = new TestConfig(e);
				TestConfig old = tests.put(test.ID, test);
				if (old != null) {
					throw new IllegalArgumentException("[TestConfig]表存在重复的数据id： " + old.ID);
				}
			}			

			this.tests = com.google.common.collect.ImmutableMap.copyOf(tests);

			log.info("load TestConfig size[{}]", tests.size());

		} catch (Exception e) {
			throw new RuntimeException("load TestConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
