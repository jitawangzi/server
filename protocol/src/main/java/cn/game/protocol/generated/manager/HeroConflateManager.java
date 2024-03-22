package cn.game.protocol.generated.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cn.game.protocol.generated.config.HeroConflateConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class HeroConflateManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(HeroConflateManager.class);

	private static HeroConflateManager instance = new HeroConflateManager();
	private static final String xmlFileName = "HeroConflate";
	
	/** 总数据，按id取值 */
	private Map<Integer, HeroConflateConfig> heroconflates = new HashMap<>();

	public static HeroConflateManager instance() {
		return instance;
	}
	private HeroConflateManager() {
		WatchServiceManager.getInstance().register(this);
	}
	/**
	 * 根据id获取数据，一般用这个方法，如果数据不存在，一般是配置错误，直接抛出异常
	 * 
	 * @param id
	 * @return
	 */
	public HeroConflateConfig get(int id) {
		HeroConflateConfig config = this.heroconflates.get(id);
		if (config == null) { 
			throw new NullPointerException("【HeroConflate】表的" + "id【" + id + "】不存在"); 
		}
		return config;
	}
	/**
	 * 根据id获取数据，允许返回null
	 * 
	 * @param id
	 * @return
	 */
	public HeroConflateConfig getNullable(int id) {
		return this.heroconflates.get(id);
	}

	/**
	 * 获取所有数据
	 * @return
	 */
	public Collection<HeroConflateConfig> list() {
		return this.heroconflates.values();
	}
	@Override
	public void load() {
		try {
			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, HeroConflateConfig> heroconflates = new HashMap<>();
			for (Element e : list) {
				HeroConflateConfig heroconflate = new HeroConflateConfig(e);
				HeroConflateConfig old = heroconflates.put(heroconflate.ID, heroconflate);
				if (old != null) {
					throw new IllegalArgumentException("[HeroConflateConfig]表存在重复的数据id： " + old.ID);
				}
			}			

			this.heroconflates = com.google.common.collect.ImmutableMap.copyOf(heroconflates);

			log.info("load HeroConflateConfig size[{}]", heroconflates.size());

		} catch (Exception e) {
			throw new RuntimeException("load HeroConflateConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
