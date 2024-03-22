package cn.game.protocol.generated.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cn.game.protocol.generated.config.HeroResourceConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class HeroResourceManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(HeroResourceManager.class);

	private static HeroResourceManager instance = new HeroResourceManager();
	private static final String xmlFileName = "HeroResource";
	
	/** 总数据，按id取值 */
	private Map<Integer, HeroResourceConfig> heroresources = new HashMap<>();

	public static HeroResourceManager instance() {
		return instance;
	}
	private HeroResourceManager() {
		WatchServiceManager.getInstance().register(this);
	}
	/**
	 * 根据id获取数据，一般用这个方法，如果数据不存在，一般是配置错误，直接抛出异常
	 * 
	 * @param id
	 * @return
	 */
	public HeroResourceConfig get(int id) {
		HeroResourceConfig config = this.heroresources.get(id);
		if (config == null) { 
			throw new NullPointerException("【HeroResource】表的" + "id【" + id + "】不存在"); 
		}
		return config;
	}
	/**
	 * 根据id获取数据，允许返回null
	 * 
	 * @param id
	 * @return
	 */
	public HeroResourceConfig getNullable(int id) {
		return this.heroresources.get(id);
	}

	/**
	 * 获取所有数据
	 * @return
	 */
	public Collection<HeroResourceConfig> list() {
		return this.heroresources.values();
	}
	@Override
	public void load() {
		try {
			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, HeroResourceConfig> heroresources = new HashMap<>();
			for (Element e : list) {
				HeroResourceConfig heroresource = new HeroResourceConfig(e);
				HeroResourceConfig old = heroresources.put(heroresource.ID, heroresource);
				if (old != null) {
					throw new IllegalArgumentException("[HeroResourceConfig]表存在重复的数据id： " + old.ID);
				}
			}			

			this.heroresources = com.google.common.collect.ImmutableMap.copyOf(heroresources);

			log.info("load HeroResourceConfig size[{}]", heroresources.size());

		} catch (Exception e) {
			throw new RuntimeException("load HeroResourceConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
