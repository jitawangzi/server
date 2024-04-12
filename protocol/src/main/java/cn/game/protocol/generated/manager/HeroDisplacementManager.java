package cn.game.protocol.generated.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cn.game.protocol.generated.config.HeroDisplacementConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class HeroDisplacementManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(HeroDisplacementManager.class);

	private static HeroDisplacementManager instance = new HeroDisplacementManager();
	private static final String xmlFileName = "HeroDisplacement";
	
	/** 总数据，按id取值 */
	private Map<Integer, HeroDisplacementConfig> herodisplacements = new HashMap<>();

	public static HeroDisplacementManager instance() {
		return instance;
	}
	private HeroDisplacementManager() {
		WatchServiceManager.getInstance().register(this);
	}
	/**
	 * 根据id获取数据，一般用这个方法，如果数据不存在，一般是配置错误，直接抛出异常
	 * 
	 * @param id
	 * @return
	 */
	public HeroDisplacementConfig get(int id) {
		HeroDisplacementConfig config = this.herodisplacements.get(id);
		if (config == null) { 
			throw new NullPointerException("【HeroDisplacement】表的" + "id【" + id + "】不存在"); 
		}
		return config;
	}
	/**
	 * 根据id获取数据，允许返回null
	 * 
	 * @param id
	 * @return
	 */
	public HeroDisplacementConfig getNullable(int id) {
		return this.herodisplacements.get(id);
	}

	/**
	 * 获取所有数据
	 * @return
	 */
	public Collection<HeroDisplacementConfig> list() {
		return this.herodisplacements.values();
	}
	@Override
	public void load() {
		try {
			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, HeroDisplacementConfig> herodisplacements = new HashMap<>();
			for (Element e : list) {
				HeroDisplacementConfig herodisplacement = new HeroDisplacementConfig(e);
				HeroDisplacementConfig old = herodisplacements.put(herodisplacement.ID, herodisplacement);
				if (old != null) {
					throw new IllegalArgumentException("[HeroDisplacementConfig]表存在重复的数据id： " + old.ID);
				}
			}			

			this.herodisplacements = com.google.common.collect.ImmutableMap.copyOf(herodisplacements);

			log.info("load HeroDisplacementConfig size[{}]", herodisplacements.size());

		} catch (Exception e) {
			throw new RuntimeException("load HeroDisplacementConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
