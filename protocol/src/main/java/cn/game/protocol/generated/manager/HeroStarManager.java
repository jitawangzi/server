package cn.game.protocol.generated.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cn.game.protocol.generated.config.HeroStarConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class HeroStarManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(HeroStarManager.class);

	private static HeroStarManager instance = new HeroStarManager();
	private static final String xmlFileName = "HeroStar";
	
	private Map<Integer, HeroStarConfig> herostars = new HashMap<>();

	public static HeroStarManager instance() {
		return instance;
	}

	private HeroStarManager() {
		WatchServiceManager.getInstance().register(this);
	}
	/**
	 * 根据id获取数据，一般用这个方法，如果数据不存在，一般是配置错误，直接抛出异常
	 * 
	 * @param id
	 * @return
	 */
	public HeroStarConfig get(int id) {
		HeroStarConfig config = this.herostars.get(id);
		if (config == null) { 
			throw new NullPointerException("【HeroStar】表的" + "id【" + id + "】不存在"); 
		}
		return config;
	}
	/**
	 * 根据id获取数据，允许返回null
	 * 
	 * @param id
	 * @return
	 */
	public HeroStarConfig getNullable(int id) {
		return this.herostars.get(id);
	}

	public Collection<HeroStarConfig> list() {
		return this.herostars.values();
	}

	@Override
	public void load() {

		try {
			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, HeroStarConfig> herostars = new HashMap<>();
			for (Element e : list) {
				HeroStarConfig herostar = new HeroStarConfig(e);
				herostars.put(herostar.ID, herostar);
			}			

			this.herostars = com.google.common.collect.ImmutableMap.copyOf(herostars);

			log.info("load HeroStarConfig size[{}]", herostars.size());

		} catch (Exception e) {
			throw new RuntimeException("load HeroStarConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
