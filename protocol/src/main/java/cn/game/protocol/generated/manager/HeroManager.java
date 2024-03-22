package cn.game.protocol.generated.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cn.game.protocol.generated.config.HeroConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class HeroManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(HeroManager.class);

	private static HeroManager instance = new HeroManager();
	private static final String xmlFileName = "Hero";
	
	/** 总数据，按id取值 */
	private Map<Integer, HeroConfig> heros = new HashMap<>();

	public static HeroManager instance() {
		return instance;
	}
	private HeroManager() {
		WatchServiceManager.getInstance().register(this);
	}
	/**
	 * 根据id获取数据，一般用这个方法，如果数据不存在，一般是配置错误，直接抛出异常
	 * 
	 * @param id
	 * @return
	 */
	public HeroConfig get(int id) {
		HeroConfig config = this.heros.get(id);
		if (config == null) { 
			throw new NullPointerException("【Hero】表的" + "id【" + id + "】不存在"); 
		}
		return config;
	}
	/**
	 * 根据id获取数据，允许返回null
	 * 
	 * @param id
	 * @return
	 */
	public HeroConfig getNullable(int id) {
		return this.heros.get(id);
	}

	/**
	 * 获取所有数据
	 * @return
	 */
	public Collection<HeroConfig> list() {
		return this.heros.values();
	}
	@Override
	public void load() {
		try {
			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, HeroConfig> heros = new HashMap<>();
			for (Element e : list) {
				HeroConfig hero = new HeroConfig(e);
				HeroConfig old = heros.put(hero.ID, hero);
				if (old != null) {
					throw new IllegalArgumentException("[HeroConfig]表存在重复的数据id： " + old.ID);
				}
			}			

			this.heros = com.google.common.collect.ImmutableMap.copyOf(heros);

			log.info("load HeroConfig size[{}]", heros.size());

		} catch (Exception e) {
			throw new RuntimeException("load HeroConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
