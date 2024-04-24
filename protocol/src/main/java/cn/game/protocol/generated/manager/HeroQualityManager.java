package cn.game.protocol.generated.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cn.game.protocol.generated.config.HeroQualityConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class HeroQualityManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(HeroQualityManager.class);

	private static HeroQualityManager instance = new HeroQualityManager();
	private static final String xmlFileName = "HeroQuality";
	
	/** 总数据，按id取值 */
	private Map<Integer, HeroQualityConfig> heroqualitys = new HashMap<>();

	public static HeroQualityManager instance() {
		return instance;
	}
	private HeroQualityManager() {
		WatchServiceManager.getInstance().register(this);
	}
	/**
	 * 根据id获取数据，一般用这个方法，如果数据不存在，一般是配置错误，直接抛出异常
	 * 
	 * @param id
	 * @return
	 */
	public HeroQualityConfig get(int id) {
		HeroQualityConfig config = this.heroqualitys.get(id);
		if (config == null) { 
			throw new NullPointerException("【HeroQuality】表的" + "id【" + id + "】不存在"); 
		}
		return config;
	}
	/**
	 * 根据id获取数据，允许返回null
	 * 
	 * @param id
	 * @return
	 */
	public HeroQualityConfig getNullable(int id) {
		return this.heroqualitys.get(id);
	}

	/**
	 * 获取所有数据
	 * @return
	 */
	public Collection<HeroQualityConfig> list() {
		return this.heroqualitys.values();
	}
	@Override
	public void load() {
		try {
			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, HeroQualityConfig> heroqualitys = new HashMap<>();
			for (Element e : list) {
				HeroQualityConfig heroquality = new HeroQualityConfig(e);
				HeroQualityConfig old = heroqualitys.put(heroquality.ID, heroquality);
				if (old != null) {
					throw new IllegalArgumentException("[HeroQualityConfig]表存在重复的数据id： " + old.ID);
				}
			}			

			this.heroqualitys = com.google.common.collect.ImmutableMap.copyOf(heroqualitys);

			log.info("load HeroQualityConfig size[{}]", heroqualitys.size());

		} catch (Exception e) {
			throw new RuntimeException("load HeroQualityConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
