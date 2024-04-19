package cn.game.protocol.generated.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cn.game.protocol.generated.config.ConsumeIncrementalConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class ConsumeIncrementalManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(ConsumeIncrementalManager.class);

	private static ConsumeIncrementalManager instance = new ConsumeIncrementalManager();
	private static final String xmlFileName = "ConsumeIncremental";
	
	/** 总数据，按id取值 */
	private Map<Integer, ConsumeIncrementalConfig> consumeincrementals = new HashMap<>();

	public static ConsumeIncrementalManager instance() {
		return instance;
	}
	private ConsumeIncrementalManager() {
		WatchServiceManager.getInstance().register(this);
	}
	/**
	 * 根据id获取数据，一般用这个方法，如果数据不存在，一般是配置错误，直接抛出异常
	 * 
	 * @param id
	 * @return
	 */
	public ConsumeIncrementalConfig get(int id) {
		ConsumeIncrementalConfig config = this.consumeincrementals.get(id);
		if (config == null) { 
			throw new NullPointerException("【ConsumeIncremental】表的" + "id【" + id + "】不存在"); 
		}
		return config;
	}
	/**
	 * 根据id获取数据，允许返回null
	 * 
	 * @param id
	 * @return
	 */
	public ConsumeIncrementalConfig getNullable(int id) {
		return this.consumeincrementals.get(id);
	}

	/**
	 * 获取所有数据
	 * @return
	 */
	public Collection<ConsumeIncrementalConfig> list() {
		return this.consumeincrementals.values();
	}
	@Override
	public void load() {
		try {
			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, ConsumeIncrementalConfig> consumeincrementals = new HashMap<>();
			for (Element e : list) {
				ConsumeIncrementalConfig consumeincremental = new ConsumeIncrementalConfig(e);
				ConsumeIncrementalConfig old = consumeincrementals.put(consumeincremental.ID, consumeincremental);
				if (old != null) {
					throw new IllegalArgumentException("[ConsumeIncrementalConfig]表存在重复的数据id： " + old.ID);
				}
			}			

			this.consumeincrementals = com.google.common.collect.ImmutableMap.copyOf(consumeincrementals);

			log.info("load ConsumeIncrementalConfig size[{}]", consumeincrementals.size());

		} catch (Exception e) {
			throw new RuntimeException("load ConsumeIncrementalConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
