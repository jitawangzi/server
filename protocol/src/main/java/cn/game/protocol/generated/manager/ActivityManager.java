package cn.game.protocol.generated.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
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
	
	private Map<Integer, ActivityConfig> activitys = new HashMap<>();

	public static ActivityManager getInstance() {
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
	public ActivityConfig getActivityConfig(int id) {
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
	public ActivityConfig getActivityConfigNullable(int id) {
		return this.activitys.get(id);
	}

	public Collection<ActivityConfig> list() {
		return this.activitys.values();
	}

	@Override
	public void load() {

		try {
			ClassLoader classLoader = Thread.currentThread().getClass().getClassLoader();
			if (classLoader == null) {
				classLoader = ActivityManager.class.getClassLoader();
			}
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, ActivityConfig> activitys = new HashMap<>();
			for (Element e : list) {
				ActivityConfig activity = new ActivityConfig(e);
				activitys.put(activity.getId(), activity);
			}			

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
