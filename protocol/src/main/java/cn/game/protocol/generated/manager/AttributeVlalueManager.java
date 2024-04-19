package cn.game.protocol.generated.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cn.game.protocol.generated.config.AttributeVlalueConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class AttributeVlalueManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(AttributeVlalueManager.class);

	private static AttributeVlalueManager instance = new AttributeVlalueManager();
	private static final String xmlFileName = "AttributeVlalue";
	
	/** 总数据，按id取值 */
	private Map<Integer, AttributeVlalueConfig> attributevlalues = new HashMap<>();

	public static AttributeVlalueManager instance() {
		return instance;
	}
	private AttributeVlalueManager() {
		WatchServiceManager.getInstance().register(this);
	}
	/**
	 * 根据id获取数据，一般用这个方法，如果数据不存在，一般是配置错误，直接抛出异常
	 * 
	 * @param id
	 * @return
	 */
	public AttributeVlalueConfig get(int id) {
		AttributeVlalueConfig config = this.attributevlalues.get(id);
		if (config == null) { 
			throw new NullPointerException("【AttributeVlalue】表的" + "id【" + id + "】不存在"); 
		}
		return config;
	}
	/**
	 * 根据id获取数据，允许返回null
	 * 
	 * @param id
	 * @return
	 */
	public AttributeVlalueConfig getNullable(int id) {
		return this.attributevlalues.get(id);
	}

	/**
	 * 获取所有数据
	 * @return
	 */
	public Collection<AttributeVlalueConfig> list() {
		return this.attributevlalues.values();
	}
	@Override
	public void load() {
		try {
			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, AttributeVlalueConfig> attributevlalues = new HashMap<>();
			for (Element e : list) {
				AttributeVlalueConfig attributevlalue = new AttributeVlalueConfig(e);
				AttributeVlalueConfig old = attributevlalues.put(attributevlalue.ID, attributevlalue);
				if (old != null) {
					throw new IllegalArgumentException("[AttributeVlalueConfig]表存在重复的数据id： " + old.ID);
				}
			}			

			this.attributevlalues = com.google.common.collect.ImmutableMap.copyOf(attributevlalues);

			log.info("load AttributeVlalueConfig size[{}]", attributevlalues.size());

		} catch (Exception e) {
			throw new RuntimeException("load AttributeVlalueConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
