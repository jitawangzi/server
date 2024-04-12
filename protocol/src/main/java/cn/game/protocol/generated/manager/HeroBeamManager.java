package cn.game.protocol.generated.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cn.game.protocol.generated.config.HeroBeamConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class HeroBeamManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(HeroBeamManager.class);

	private static HeroBeamManager instance = new HeroBeamManager();
	private static final String xmlFileName = "HeroBeam";
	
	/** 总数据，按id取值 */
	private Map<Integer, HeroBeamConfig> herobeams = new HashMap<>();

	public static HeroBeamManager instance() {
		return instance;
	}
	private HeroBeamManager() {
		WatchServiceManager.getInstance().register(this);
	}
	/**
	 * 根据id获取数据，一般用这个方法，如果数据不存在，一般是配置错误，直接抛出异常
	 * 
	 * @param id
	 * @return
	 */
	public HeroBeamConfig get(int id) {
		HeroBeamConfig config = this.herobeams.get(id);
		if (config == null) { 
			throw new NullPointerException("【HeroBeam】表的" + "id【" + id + "】不存在"); 
		}
		return config;
	}
	/**
	 * 根据id获取数据，允许返回null
	 * 
	 * @param id
	 * @return
	 */
	public HeroBeamConfig getNullable(int id) {
		return this.herobeams.get(id);
	}

	/**
	 * 获取所有数据
	 * @return
	 */
	public Collection<HeroBeamConfig> list() {
		return this.herobeams.values();
	}
	@Override
	public void load() {
		try {
			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, HeroBeamConfig> herobeams = new HashMap<>();
			for (Element e : list) {
				HeroBeamConfig herobeam = new HeroBeamConfig(e);
				HeroBeamConfig old = herobeams.put(herobeam.ID, herobeam);
				if (old != null) {
					throw new IllegalArgumentException("[HeroBeamConfig]表存在重复的数据id： " + old.ID);
				}
			}			

			this.herobeams = com.google.common.collect.ImmutableMap.copyOf(herobeams);

			log.info("load HeroBeamConfig size[{}]", herobeams.size());

		} catch (Exception e) {
			throw new RuntimeException("load HeroBeamConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
