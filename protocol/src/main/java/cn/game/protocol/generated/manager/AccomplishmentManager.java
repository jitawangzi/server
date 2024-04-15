package cn.game.protocol.generated.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cn.game.protocol.generated.config.AccomplishmentConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class AccomplishmentManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(AccomplishmentManager.class);

	private static AccomplishmentManager instance = new AccomplishmentManager();
	private static final String xmlFileName = "Accomplishment";
	
	/** 总数据，按id取值 */
	private Map<Integer, AccomplishmentConfig> accomplishments = new HashMap<>();

	public static AccomplishmentManager instance() {
		return instance;
	}
	private AccomplishmentManager() {
		WatchServiceManager.getInstance().register(this);
	}
	/**
	 * 根据id获取数据，一般用这个方法，如果数据不存在，一般是配置错误，直接抛出异常
	 * 
	 * @param id
	 * @return
	 */
	public AccomplishmentConfig get(int id) {
		AccomplishmentConfig config = this.accomplishments.get(id);
		if (config == null) { 
			throw new NullPointerException("【Accomplishment】表的" + "id【" + id + "】不存在"); 
		}
		return config;
	}
	/**
	 * 根据id获取数据，允许返回null
	 * 
	 * @param id
	 * @return
	 */
	public AccomplishmentConfig getNullable(int id) {
		return this.accomplishments.get(id);
	}

	/**
	 * 获取所有数据
	 * @return
	 */
	public Collection<AccomplishmentConfig> list() {
		return this.accomplishments.values();
	}
	@Override
	public void load() {
		try {
			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, AccomplishmentConfig> accomplishments = new HashMap<>();
			for (Element e : list) {
				AccomplishmentConfig accomplishment = new AccomplishmentConfig(e);
				AccomplishmentConfig old = accomplishments.put(accomplishment.ID, accomplishment);
				if (old != null) {
					throw new IllegalArgumentException("[AccomplishmentConfig]表存在重复的数据id： " + old.ID);
				}
			}			

			this.accomplishments = com.google.common.collect.ImmutableMap.copyOf(accomplishments);

			log.info("load AccomplishmentConfig size[{}]", accomplishments.size());

		} catch (Exception e) {
			throw new RuntimeException("load AccomplishmentConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
