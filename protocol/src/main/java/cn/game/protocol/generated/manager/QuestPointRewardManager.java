package cn.game.protocol.generated.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cn.game.protocol.generated.config.QuestPointRewardConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class QuestPointRewardManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(QuestPointRewardManager.class);

	private static QuestPointRewardManager instance = new QuestPointRewardManager();
	private static final String xmlFileName = "QuestPointReward";
	
	/** 总数据，按id取值 */
	private Map<Integer, QuestPointRewardConfig> questpointrewards = new HashMap<>();

	public static QuestPointRewardManager instance() {
		return instance;
	}
	private QuestPointRewardManager() {
		WatchServiceManager.getInstance().register(this);
	}
	/**
	 * 根据id获取数据，一般用这个方法，如果数据不存在，一般是配置错误，直接抛出异常
	 * 
	 * @param id
	 * @return
	 */
	public QuestPointRewardConfig get(int id) {
		QuestPointRewardConfig config = this.questpointrewards.get(id);
		if (config == null) { 
			throw new NullPointerException("【QuestPointReward】表的" + "id【" + id + "】不存在"); 
		}
		return config;
	}
	/**
	 * 根据id获取数据，允许返回null
	 * 
	 * @param id
	 * @return
	 */
	public QuestPointRewardConfig getNullable(int id) {
		return this.questpointrewards.get(id);
	}

	/**
	 * 获取所有数据
	 * @return
	 */
	public Collection<QuestPointRewardConfig> list() {
		return this.questpointrewards.values();
	}
	@Override
	public void load() {
		try {
			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, QuestPointRewardConfig> questpointrewards = new HashMap<>();
			for (Element e : list) {
				QuestPointRewardConfig questpointreward = new QuestPointRewardConfig(e);
				QuestPointRewardConfig old = questpointrewards.put(questpointreward.ID, questpointreward);
				if (old != null) {
					throw new IllegalArgumentException("[QuestPointRewardConfig]表存在重复的数据id： " + old.ID);
				}
			}			

			this.questpointrewards = com.google.common.collect.ImmutableMap.copyOf(questpointrewards);

			log.info("load QuestPointRewardConfig size[{}]", questpointrewards.size());

		} catch (Exception e) {
			throw new RuntimeException("load QuestPointRewardConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
