package cn.game.protocol.generated.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cn.game.protocol.generated.config.HeroAttributeConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class HeroAttributeManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(HeroAttributeManager.class);

	private static HeroAttributeManager instance = new HeroAttributeManager();
	private static final String xmlFileName = "HeroAttribute";
	
	/** 总数据，按id取值 */
	private Map<Integer, HeroAttributeConfig> heroattributes = new HashMap<>();

	public static HeroAttributeManager instance() {
		return instance;
	}
	private HeroAttributeManager() {
		WatchServiceManager.getInstance().register(this);
	}
	/**
	 * 根据id获取数据，一般用这个方法，如果数据不存在，一般是配置错误，直接抛出异常
	 * 
	 * @param id
	 * @return
	 */
	public HeroAttributeConfig get(int id) {
		HeroAttributeConfig config = this.heroattributes.get(id);
		if (config == null) { 
			throw new NullPointerException("【HeroAttribute】表的" + "id【" + id + "】不存在"); 
		}
		return config;
	}
	/**
	 * 根据id获取数据，允许返回null
	 * 
	 * @param id
	 * @return
	 */
	public HeroAttributeConfig getNullable(int id) {
		return this.heroattributes.get(id);
	}

	/**
	 * 获取所有数据
	 * @return
	 */
	public Collection<HeroAttributeConfig> list() {
		return this.heroattributes.values();
	}
	@Override
	public void load() {
		try {
			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, HeroAttributeConfig> heroattributes = new HashMap<>();
			for (Element e : list) {
				HeroAttributeConfig heroattribute = new HeroAttributeConfig(e);
				HeroAttributeConfig old = heroattributes.put(heroattribute.ID, heroattribute);
				if (old != null) {
					throw new IllegalArgumentException("[HeroAttributeConfig]表存在重复的数据id： " + old.ID);
				}
			}			

			this.heroattributes = com.google.common.collect.ImmutableMap.copyOf(heroattributes);

			log.info("load HeroAttributeConfig size[{}]", heroattributes.size());

		} catch (Exception e) {
			throw new RuntimeException("load HeroAttributeConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
