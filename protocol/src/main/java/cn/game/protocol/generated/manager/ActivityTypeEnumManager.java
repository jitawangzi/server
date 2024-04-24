package cn.game.protocol.generated.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cn.game.protocol.generated.config.ActivityTypeEnumConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class ActivityTypeEnumManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(ActivityTypeEnumManager.class);

	private static ActivityTypeEnumManager instance = new ActivityTypeEnumManager();
	private static final String xmlFileName = "ActivityTypeEnum";
	
	/** 总数据，按id取值 */
	private Map<Integer, ActivityTypeEnumConfig> activitytypeenums = new HashMap<>();

	public static ActivityTypeEnumManager instance() {
		return instance;
	}
	private ActivityTypeEnumManager() {
		WatchServiceManager.getInstance().register(this);
	}
	/**
	 * 根据id获取数据，一般用这个方法，如果数据不存在，一般是配置错误，直接抛出异常
	 * 
	 * @param id
	 * @return
	 */
	public ActivityTypeEnumConfig get(int id) {
		ActivityTypeEnumConfig config = this.activitytypeenums.get(id);
		if (config == null) { 
			throw new NullPointerException("【ActivityTypeEnum】表的" + "id【" + id + "】不存在"); 
		}
		return config;
	}
	/**
	 * 根据id获取数据，允许返回null
	 * 
	 * @param id
	 * @return
	 */
	public ActivityTypeEnumConfig getNullable(int id) {
		return this.activitytypeenums.get(id);
	}

	/**
	 * 获取所有数据
	 * @return
	 */
	public Collection<ActivityTypeEnumConfig> list() {
		return this.activitytypeenums.values();
	}
	@Override
	public void load() {
		try {
			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, ActivityTypeEnumConfig> activitytypeenums = new HashMap<>();
			for (Element e : list) {
				ActivityTypeEnumConfig activitytypeenum = new ActivityTypeEnumConfig(e);
				ActivityTypeEnumConfig old = activitytypeenums.put(activitytypeenum.ID, activitytypeenum);
				if (old != null) {
					throw new IllegalArgumentException("[ActivityTypeEnumConfig]表存在重复的数据id： " + old.ID);
				}
			}			

			this.activitytypeenums = com.google.common.collect.ImmutableMap.copyOf(activitytypeenums);

			log.info("load ActivityTypeEnumConfig size[{}]", activitytypeenums.size());

		} catch (Exception e) {
			throw new RuntimeException("load ActivityTypeEnumConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
