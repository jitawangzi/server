package cn.game.protocol.generated.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cn.game.protocol.generated.config.RogueEnergyConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class RogueEnergyManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(RogueEnergyManager.class);

	private static RogueEnergyManager instance = new RogueEnergyManager();
	private static final String xmlFileName = "RogueEnergy";
	
	/** 总数据，按id取值 */
	private Map<Integer, RogueEnergyConfig> rogueenergys = new HashMap<>();

	public static RogueEnergyManager instance() {
		return instance;
	}
	private RogueEnergyManager() {
		WatchServiceManager.getInstance().register(this);
	}
	/**
	 * 根据id获取数据，一般用这个方法，如果数据不存在，一般是配置错误，直接抛出异常
	 * 
	 * @param id
	 * @return
	 */
	public RogueEnergyConfig get(int id) {
		RogueEnergyConfig config = this.rogueenergys.get(id);
		if (config == null) { 
			throw new NullPointerException("【RogueEnergy】表的" + "id【" + id + "】不存在"); 
		}
		return config;
	}
	/**
	 * 根据id获取数据，允许返回null
	 * 
	 * @param id
	 * @return
	 */
	public RogueEnergyConfig getNullable(int id) {
		return this.rogueenergys.get(id);
	}

	/**
	 * 获取所有数据
	 * @return
	 */
	public Collection<RogueEnergyConfig> list() {
		return this.rogueenergys.values();
	}
	@Override
	public void load() {
		try {
			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, RogueEnergyConfig> rogueenergys = new HashMap<>();
			for (Element e : list) {
				RogueEnergyConfig rogueenergy = new RogueEnergyConfig(e);
				RogueEnergyConfig old = rogueenergys.put(rogueenergy.ID, rogueenergy);
				if (old != null) {
					throw new IllegalArgumentException("[RogueEnergyConfig]表存在重复的数据id： " + old.ID);
				}
			}			

			this.rogueenergys = com.google.common.collect.ImmutableMap.copyOf(rogueenergys);

			log.info("load RogueEnergyConfig size[{}]", rogueenergys.size());

		} catch (Exception e) {
			throw new RuntimeException("load RogueEnergyConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
