package cn.game.protocol.generated.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cn.game.protocol.generated.config.MonsterAttributeConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class MonsterAttributeManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(MonsterAttributeManager.class);

	private static MonsterAttributeManager instance = new MonsterAttributeManager();
	private static final String xmlFileName = "MonsterAttribute";
	
	private Map<Integer, MonsterAttributeConfig> monsterattributes = new HashMap<>();

	public static MonsterAttributeManager getInstance() {
		return instance;
	}

	private MonsterAttributeManager() {
		WatchServiceManager.getInstance().register(this);
	}
	/**
	 * 根据id获取数据，一般用这个方法，如果数据不存在，一般是配置错误，直接抛出异常
	 * 
	 * @param id
	 * @return
	 */
	public MonsterAttributeConfig get(int id) {
		MonsterAttributeConfig config = this.monsterattributes.get(id);
		if (config == null) { 
			throw new NullPointerException("【MonsterAttribute】表的" + "id【" + id + "】不存在"); 
		}
		return config;
	}
	/**
	 * 根据id获取数据，允许返回null
	 * 
	 * @param id
	 * @return
	 */
	public MonsterAttributeConfig getNullable(int id) {
		return this.monsterattributes.get(id);
	}

	public Collection<MonsterAttributeConfig> list() {
		return this.monsterattributes.values();
	}

	@Override
	public void load() {

		try {
			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, MonsterAttributeConfig> monsterattributes = new HashMap<>();
			for (Element e : list) {
				MonsterAttributeConfig monsterattribute = new MonsterAttributeConfig(e);
				monsterattributes.put(monsterattribute.getID(), monsterattribute);
			}			

			this.monsterattributes = com.google.common.collect.ImmutableMap.copyOf(monsterattributes);

			log.info("load MonsterAttributeConfig size[{}]", monsterattributes.size());

		} catch (Exception e) {
			throw new RuntimeException("load MonsterAttributeConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
