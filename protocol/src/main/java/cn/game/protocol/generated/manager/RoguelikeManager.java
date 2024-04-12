package cn.game.protocol.generated.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cn.game.protocol.generated.config.RoguelikeConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class RoguelikeManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(RoguelikeManager.class);

	private static RoguelikeManager instance = new RoguelikeManager();
	private static final String xmlFileName = "Roguelike";
	
	/** 总数据，按id取值 */
	private Map<Integer, RoguelikeConfig> roguelikes = new HashMap<>();

	public static RoguelikeManager instance() {
		return instance;
	}
	private RoguelikeManager() {
		WatchServiceManager.getInstance().register(this);
	}
	/**
	 * 根据id获取数据，一般用这个方法，如果数据不存在，一般是配置错误，直接抛出异常
	 * 
	 * @param id
	 * @return
	 */
	public RoguelikeConfig get(int id) {
		RoguelikeConfig config = this.roguelikes.get(id);
		if (config == null) { 
			throw new NullPointerException("【Roguelike】表的" + "id【" + id + "】不存在"); 
		}
		return config;
	}
	/**
	 * 根据id获取数据，允许返回null
	 * 
	 * @param id
	 * @return
	 */
	public RoguelikeConfig getNullable(int id) {
		return this.roguelikes.get(id);
	}

	/**
	 * 获取所有数据
	 * @return
	 */
	public Collection<RoguelikeConfig> list() {
		return this.roguelikes.values();
	}
	@Override
	public void load() {
		try {
			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, RoguelikeConfig> roguelikes = new HashMap<>();
			for (Element e : list) {
				RoguelikeConfig roguelike = new RoguelikeConfig(e);
				RoguelikeConfig old = roguelikes.put(roguelike.ID, roguelike);
				if (old != null) {
					throw new IllegalArgumentException("[RoguelikeConfig]表存在重复的数据id： " + old.ID);
				}
			}			

			this.roguelikes = com.google.common.collect.ImmutableMap.copyOf(roguelikes);

			log.info("load RoguelikeConfig size[{}]", roguelikes.size());

		} catch (Exception e) {
			throw new RuntimeException("load RoguelikeConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
