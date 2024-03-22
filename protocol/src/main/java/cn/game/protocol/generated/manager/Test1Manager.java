package cn.game.protocol.generated.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cn.game.protocol.generated.config.Test1Config;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class Test1Manager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(Test1Manager.class);

	private static Test1Manager instance = new Test1Manager();
	private static final String xmlFileName = "Test1";
	
	/** 总数据，按id取值 */
	private Map<Integer, Test1Config> test1s = new HashMap<>();

	public static Test1Manager instance() {
		return instance;
	}
	private Test1Manager() {
		WatchServiceManager.getInstance().register(this);
	}
	/**
	 * 根据id获取数据，一般用这个方法，如果数据不存在，一般是配置错误，直接抛出异常
	 * 
	 * @param id
	 * @return
	 */
	public Test1Config get(int id) {
		Test1Config config = this.test1s.get(id);
		if (config == null) { 
			throw new NullPointerException("【Test1】表的" + "id【" + id + "】不存在"); 
		}
		return config;
	}
	/**
	 * 根据id获取数据，允许返回null
	 * 
	 * @param id
	 * @return
	 */
	public Test1Config getNullable(int id) {
		return this.test1s.get(id);
	}

	/**
	 * 获取所有数据
	 * @return
	 */
	public Collection<Test1Config> list() {
		return this.test1s.values();
	}
	@Override
	public void load() {
		try {
			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, Test1Config> test1s = new HashMap<>();
			for (Element e : list) {
				Test1Config test1 = new Test1Config(e);
				Test1Config old = test1s.put(test1.ID, test1);
				if (old != null) {
					throw new IllegalArgumentException("[Test1Config]表存在重复的数据id： " + old.ID);
				}
			}			

			this.test1s = com.google.common.collect.ImmutableMap.copyOf(test1s);

			log.info("load Test1Config size[{}]", test1s.size());

		} catch (Exception e) {
			throw new RuntimeException("load Test1Config error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
