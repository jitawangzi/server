package cn.game.protocol.generated.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cn.game.protocol.generated.config.RoguelikeTriggerConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class RoguelikeTriggerManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(RoguelikeTriggerManager.class);

	private static RoguelikeTriggerManager instance = new RoguelikeTriggerManager();
	private static final String xmlFileName = "RoguelikeTrigger";
	
	/** 总数据，按id取值 */
	private Map<Integer, RoguelikeTriggerConfig> rogueliketriggers = new HashMap<>();

	public static RoguelikeTriggerManager instance() {
		return instance;
	}
	private RoguelikeTriggerManager() {
		WatchServiceManager.getInstance().register(this);
	}
	/**
	 * 根据id获取数据，一般用这个方法，如果数据不存在，一般是配置错误，直接抛出异常
	 * 
	 * @param id
	 * @return
	 */
	public RoguelikeTriggerConfig get(int id) {
		RoguelikeTriggerConfig config = this.rogueliketriggers.get(id);
		if (config == null) { 
			throw new NullPointerException("【RoguelikeTrigger】表的" + "id【" + id + "】不存在"); 
		}
		return config;
	}
	/**
	 * 根据id获取数据，允许返回null
	 * 
	 * @param id
	 * @return
	 */
	public RoguelikeTriggerConfig getNullable(int id) {
		return this.rogueliketriggers.get(id);
	}

	/**
	 * 获取所有数据
	 * @return
	 */
	public Collection<RoguelikeTriggerConfig> list() {
		return this.rogueliketriggers.values();
	}
	@Override
	public void load() {
		try {
			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, RoguelikeTriggerConfig> rogueliketriggers = new HashMap<>();
			for (Element e : list) {
				RoguelikeTriggerConfig rogueliketrigger = new RoguelikeTriggerConfig(e);
				RoguelikeTriggerConfig old = rogueliketriggers.put(rogueliketrigger.ID, rogueliketrigger);
				if (old != null) {
					throw new IllegalArgumentException("[RoguelikeTriggerConfig]表存在重复的数据id： " + old.ID);
				}
			}			

			this.rogueliketriggers = com.google.common.collect.ImmutableMap.copyOf(rogueliketriggers);

			log.info("load RoguelikeTriggerConfig size[{}]", rogueliketriggers.size());

		} catch (Exception e) {
			throw new RuntimeException("load RoguelikeTriggerConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
