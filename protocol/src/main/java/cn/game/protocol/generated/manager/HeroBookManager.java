package cn.game.protocol.generated.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cn.game.protocol.generated.config.HeroBookConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class HeroBookManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(HeroBookManager.class);

	private static HeroBookManager instance = new HeroBookManager();
	private static final String xmlFileName = "HeroBook";
	
	/** 总数据，按id取值 */
	private Map<Integer, HeroBookConfig> herobooks = new HashMap<>();

	public static HeroBookManager instance() {
		return instance;
	}
	private HeroBookManager() {
		WatchServiceManager.getInstance().register(this);
	}
	/**
	 * 根据id获取数据，一般用这个方法，如果数据不存在，一般是配置错误，直接抛出异常
	 * 
	 * @param id
	 * @return
	 */
	public HeroBookConfig get(int id) {
		HeroBookConfig config = this.herobooks.get(id);
		if (config == null) { 
			throw new NullPointerException("【HeroBook】表的" + "id【" + id + "】不存在"); 
		}
		return config;
	}
	/**
	 * 根据id获取数据，允许返回null
	 * 
	 * @param id
	 * @return
	 */
	public HeroBookConfig getNullable(int id) {
		return this.herobooks.get(id);
	}

	/**
	 * 获取所有数据
	 * @return
	 */
	public Collection<HeroBookConfig> list() {
		return this.herobooks.values();
	}
	@Override
	public void load() {
		try {
			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, HeroBookConfig> herobooks = new HashMap<>();
			for (Element e : list) {
				HeroBookConfig herobook = new HeroBookConfig(e);
				HeroBookConfig old = herobooks.put(herobook.ID, herobook);
				if (old != null) {
					throw new IllegalArgumentException("[HeroBookConfig]表存在重复的数据id： " + old.ID);
				}
			}			

			this.herobooks = com.google.common.collect.ImmutableMap.copyOf(herobooks);

			log.info("load HeroBookConfig size[{}]", herobooks.size());

		} catch (Exception e) {
			throw new RuntimeException("load HeroBookConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
