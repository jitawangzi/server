package cn.game.protocol.generated.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cn.game.protocol.generated.config.HeroEquipConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class HeroEquipManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(HeroEquipManager.class);

	private static HeroEquipManager instance = new HeroEquipManager();
	private static final String xmlFileName = "HeroEquip";
	
	/** 总数据，按id取值 */
	private Map<Integer, HeroEquipConfig> heroequips = new HashMap<>();

	public static HeroEquipManager instance() {
		return instance;
	}
	private HeroEquipManager() {
		WatchServiceManager.getInstance().register(this);
	}
	/**
	 * 根据id获取数据，一般用这个方法，如果数据不存在，一般是配置错误，直接抛出异常
	 * 
	 * @param id
	 * @return
	 */
	public HeroEquipConfig get(int id) {
		HeroEquipConfig config = this.heroequips.get(id);
		if (config == null) { 
			throw new NullPointerException("【HeroEquip】表的" + "id【" + id + "】不存在"); 
		}
		return config;
	}
	/**
	 * 根据id获取数据，允许返回null
	 * 
	 * @param id
	 * @return
	 */
	public HeroEquipConfig getNullable(int id) {
		return this.heroequips.get(id);
	}

	/**
	 * 获取所有数据
	 * @return
	 */
	public Collection<HeroEquipConfig> list() {
		return this.heroequips.values();
	}
	@Override
	public void load() {
		try {
			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, HeroEquipConfig> heroequips = new HashMap<>();
			for (Element e : list) {
				HeroEquipConfig heroequip = new HeroEquipConfig(e);
				HeroEquipConfig old = heroequips.put(heroequip.ID, heroequip);
				if (old != null) {
					throw new IllegalArgumentException("[HeroEquipConfig]表存在重复的数据id： " + old.ID);
				}
			}			

			this.heroequips = com.google.common.collect.ImmutableMap.copyOf(heroequips);

			log.info("load HeroEquipConfig size[{}]", heroequips.size());

		} catch (Exception e) {
			throw new RuntimeException("load HeroEquipConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
