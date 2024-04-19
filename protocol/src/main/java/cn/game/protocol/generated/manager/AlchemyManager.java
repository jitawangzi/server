package cn.game.protocol.generated.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cn.game.protocol.generated.config.AlchemyConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class AlchemyManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(AlchemyManager.class);

	private static AlchemyManager instance = new AlchemyManager();
	private static final String xmlFileName = "Alchemy";
	
	/** 总数据，按id取值 */
	private Map<Integer, AlchemyConfig> alchemys = new HashMap<>();

	public static AlchemyManager instance() {
		return instance;
	}
	private AlchemyManager() {
		WatchServiceManager.getInstance().register(this);
	}
	/**
	 * 根据id获取数据，一般用这个方法，如果数据不存在，一般是配置错误，直接抛出异常
	 * 
	 * @param id
	 * @return
	 */
	public AlchemyConfig get(int id) {
		AlchemyConfig config = this.alchemys.get(id);
		if (config == null) { 
			throw new NullPointerException("【Alchemy】表的" + "id【" + id + "】不存在"); 
		}
		return config;
	}
	/**
	 * 根据id获取数据，允许返回null
	 * 
	 * @param id
	 * @return
	 */
	public AlchemyConfig getNullable(int id) {
		return this.alchemys.get(id);
	}

	/**
	 * 获取所有数据
	 * @return
	 */
	public Collection<AlchemyConfig> list() {
		return this.alchemys.values();
	}
	@Override
	public void load() {
		try {
			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, AlchemyConfig> alchemys = new HashMap<>();
			for (Element e : list) {
				AlchemyConfig alchemy = new AlchemyConfig(e);
				AlchemyConfig old = alchemys.put(alchemy.ID, alchemy);
				if (old != null) {
					throw new IllegalArgumentException("[AlchemyConfig]表存在重复的数据id： " + old.ID);
				}
			}			

			this.alchemys = com.google.common.collect.ImmutableMap.copyOf(alchemys);

			log.info("load AlchemyConfig size[{}]", alchemys.size());

		} catch (Exception e) {
			throw new RuntimeException("load AlchemyConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
