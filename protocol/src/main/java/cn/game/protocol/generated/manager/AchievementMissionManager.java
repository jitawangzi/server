package cn.game.protocol.generated.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cn.game.protocol.generated.config.AchievementMissionConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class AchievementMissionManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(AchievementMissionManager.class);

	private static AchievementMissionManager instance = new AchievementMissionManager();
	private static final String xmlFileName = "AchievementMission";
	
	private Map<Integer, AchievementMissionConfig> achievementmissions = new HashMap<>();

	public static AchievementMissionManager getInstance() {
		return instance;
	}

	private AchievementMissionManager() {
		WatchServiceManager.getInstance().register(this);
	}
	/**
	 * 根据id获取数据，一般用这个方法，如果数据不存在，一般是配置错误，直接抛出异常
	 * 
	 * @param id
	 * @return
	 */
	public AchievementMissionConfig getAchievementMissionConfig(int id) {
		AchievementMissionConfig config = this.achievementmissions.get(id);
		if (config == null) { 
			throw new NullPointerException("【AchievementMission】表的" + "id【" + id + "】不存在"); 
		}
		return config;
	}
	/**
	 * 根据id获取数据，允许返回null
	 * 
	 * @param id
	 * @return
	 */
	public AchievementMissionConfig getAchievementMissionConfigNullable(int id) {
		return this.achievementmissions.get(id);
	}

	public Collection<AchievementMissionConfig> list() {
		return this.achievementmissions.values();
	}

	@Override
	public void load() {

		try {
			ClassLoader classLoader = Thread.currentThread().getClass().getClassLoader();
			if (classLoader == null) {
				classLoader = AchievementMissionManager.class.getClassLoader();
			}
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, AchievementMissionConfig> achievementmissions = new HashMap<>();
			for (Element e : list) {
				AchievementMissionConfig achievementmission = new AchievementMissionConfig(e);
				achievementmissions.put(achievementmission.getId(), achievementmission);
			}			

			this.achievementmissions = com.google.common.collect.ImmutableMap.copyOf(achievementmissions);

			log.info("load AchievementMissionConfig size[{}]", achievementmissions.size());

		} catch (Exception e) {
			throw new RuntimeException("load AchievementMissionConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
