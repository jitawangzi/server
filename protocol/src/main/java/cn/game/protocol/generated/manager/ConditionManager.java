package cn.game.protocol.generated.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cn.game.protocol.generated.config.ConditionConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class ConditionManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(ConditionManager.class);

	private static ConditionManager instance = new ConditionManager();
	private static final String xmlFileName = "Condition";
	
	/** 总数据，按id取值 */
	private Map<Integer, ConditionConfig> conditions = new HashMap<>();

	public static ConditionManager instance() {
		return instance;
	}
	private ConditionManager() {
		WatchServiceManager.getInstance().register(this);
	}
	/**
	 * 根据id获取数据，一般用这个方法，如果数据不存在，一般是配置错误，直接抛出异常
	 * 
	 * @param id
	 * @return
	 */
	public ConditionConfig get(int id) {
		ConditionConfig config = this.conditions.get(id);
		if (config == null) { 
			throw new NullPointerException("【Condition】表的" + "id【" + id + "】不存在"); 
		}
		return config;
	}
	/**
	 * 根据id获取数据，允许返回null
	 * 
	 * @param id
	 * @return
	 */
	public ConditionConfig getNullable(int id) {
		return this.conditions.get(id);
	}

	/**
	 * 获取所有数据
	 * @return
	 */
	public Collection<ConditionConfig> list() {
		return this.conditions.values();
	}
	@Override
	public void load() {
		try {
			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, ConditionConfig> conditions = new HashMap<>();
			for (Element e : list) {
				ConditionConfig condition = new ConditionConfig(e);
				ConditionConfig old = conditions.put(condition.ID, condition);
				if (old != null) {
					throw new IllegalArgumentException("[ConditionConfig]表存在重复的数据id： " + old.ID);
				}
			}			

			this.conditions = com.google.common.collect.ImmutableMap.copyOf(conditions);

			log.info("load ConditionConfig size[{}]", conditions.size());

		} catch (Exception e) {
			throw new RuntimeException("load ConditionConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
