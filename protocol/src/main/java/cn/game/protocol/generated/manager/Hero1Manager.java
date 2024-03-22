package cn.game.protocol.generated.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cn.game.protocol.generated.config.Hero1Config;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class Hero1Manager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(Hero1Manager.class);

	private static Hero1Manager instance = new Hero1Manager();
	private static final String xmlFileName = "Hero1";
	
	private Map<Integer, Hero1Config> hero1s = new HashMap<>();

	public static Hero1Manager getInstance() {
		return instance;
	}

	private Hero1Manager() {
		WatchServiceManager.getInstance().register(this);
	}
	/**
	 * 根据id获取数据，一般用这个方法，如果数据不存在，一般是配置错误，直接抛出异常
	 * 
	 * @param id
	 * @return
	 */
	public Hero1Config get(int id) {
		Hero1Config config = this.hero1s.get(id);
		if (config == null) { 
			throw new NullPointerException("【Hero1】表的" + "id【" + id + "】不存在"); 
		}
		return config;
	}
	/**
	 * 根据id获取数据，允许返回null
	 * 
	 * @param id
	 * @return
	 */
	public Hero1Config getNullable(int id) {
		return this.hero1s.get(id);
	}

	public Collection<Hero1Config> list() {
		return this.hero1s.values();
	}

	@Override
	public void load() {

		try {
			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, Hero1Config> hero1s = new HashMap<>();
			for (Element e : list) {
				Hero1Config hero1 = new Hero1Config(e);
				hero1s.put(hero1.getID(), hero1);
			}			

			this.hero1s = com.google.common.collect.ImmutableMap.copyOf(hero1s);

			log.info("load Hero1Config size[{}]", hero1s.size());

		} catch (Exception e) {
			throw new RuntimeException("load Hero1Config error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
