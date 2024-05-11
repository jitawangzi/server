package cn.game.protocol.generated.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cn.game.protocol.generated.config.ActivityConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class ActivityManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(ActivityManager.class);

	private static ActivityManager instance = new ActivityManager();
	private static final String xmlFileName = "Activity";
	
	/** 总数据，按id取值 */
	private Map<Integer, ActivityConfig> activitys = new HashMap<>();
	/** 普通索引 */
	private Map<Integer,List<ActivityConfig>> openTypes = new HashMap<>();

	public static ActivityManager instance() {
		return instance;
	}
	private ActivityManager() {
		WatchServiceManager.getInstance().register(this);
	}
	/**
	 * 根据id获取数据，一般用这个方法，如果数据不存在，一般是配置错误，直接抛出异常
	 * 
	 * @param id
	 * @return
	 */
	public ActivityConfig get(int id) {
		ActivityConfig config = this.activitys.get(id);
		if (config == null) { 
			throw new NullPointerException("【Activity】表的" + "id【" + id + "】不存在"); 
		}
		return config;
	}
	/**
	 * 根据id获取数据，允许返回null
	 * 
	 * @param id
	 * @return
	 */
	public ActivityConfig getNullable(int id) {
		return this.activitys.get(id);
	}

	public List<ActivityConfig> getOpenTypeList(int openType) {
		return this.openTypes.get(openType);
	}
	public Map<Integer,List<ActivityConfig>> getOpenTypes() {
		return this.openTypes;
	}
	/**
	 * 获取所有数据
	 * @return
	 */
	public Collection<ActivityConfig> list() {
		return this.activitys.values();
	}
	@Override
	public void load() {
		try {
			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, ActivityConfig> activitys = new HashMap<>();
			Map<Integer, List<ActivityConfig>> openTypes = new HashMap<>();
			for (Element e : list) {
				ActivityConfig activity = new ActivityConfig(e);
				List<ActivityConfig> openTypeList = openTypes.get(activity.openType); 
				if (openTypeList == null){
					openTypeList = new ArrayList<ActivityConfig>(2) ; 
					openTypes.put(activity.openType ,openTypeList) ; 
				}
				openTypeList.add(activity) ;
				ActivityConfig old = activitys.put(activity.ID, activity);
				if (old != null) {
					throw new IllegalArgumentException("[ActivityConfig]表存在重复的数据id： " + old.ID);
				}
			}			

			this.openTypes = com.google.common.collect.ImmutableMap.copyOf(openTypes);			
			this.activitys = com.google.common.collect.ImmutableMap.copyOf(activitys);

			log.info("load ActivityConfig size[{}]", activitys.size());

		} catch (Exception e) {
			throw new RuntimeException("load ActivityConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
