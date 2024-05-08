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

import cn.game.protocol.generated.enume.QuestTypeEnum;
import cn.game.protocol.generated.config.QuestConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class QuestManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(QuestManager.class);

	private static QuestManager instance = new QuestManager();
	private static final String xmlFileName = "Mission";
	
	private Map<Integer, QuestConfig> missions = new HashMap<>();
	private Map<Integer,List<QuestConfig>> levels = new HashMap<>();
	private Map<QuestTypeEnum,List<QuestConfig>> types = new HashMap<>();

	public static QuestManager getInstance() {
		return instance;
	}

	private QuestManager() {
		WatchServiceManager.getInstance().register(this);
	}
	/**
	 * 根据id获取数据，一般用这个方法，如果数据不存在，一般是配置错误，直接抛出异常
	 * 
	 * @param id
	 * @return
	 */
	public QuestConfig getMissionConfig(int id) {
		QuestConfig config = this.missions.get(id);
		if (config == null) { 
			throw new NullPointerException("【Mission】表的" + "id【" + id + "】不存在"); 
		}
		return config;
	}
	/**
	 * 根据id获取数据，允许返回null
	 * 
	 * @param id
	 * @return
	 */
	public QuestConfig getMissionConfigNullable(int id) {
		return this.missions.get(id);
	}

	public List<QuestConfig> getLevelList(int level) {
		return this.levels.get(level);
	}
	public List<QuestConfig> getTypeList(QuestTypeEnum type) {
		return this.types.get(type);
	}
	public Collection<QuestConfig> list() {
		return this.missions.values();
	}

	@Override
	public void load() {

		try {
			ClassLoader classLoader = Thread.currentThread().getClass().getClassLoader();
			if (classLoader == null) {
				classLoader = QuestManager.class.getClassLoader();
			}
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, QuestConfig> missions = new HashMap<>();
			Map<Integer, List<QuestConfig>> levels = new HashMap<>();
			Map<QuestTypeEnum, List<QuestConfig>> types = new HashMap<>();
			for (Element e : list) {
				QuestConfig mission = new QuestConfig(e);
				List<QuestConfig> levelList = levels.get(mission.getLevel()); 
				if (levelList == null){
					levelList = new ArrayList<QuestConfig>(2) ; 
					levels.put(mission.getLevel() ,levelList) ; 
				}
				levelList.add(mission) ;
				List<QuestConfig> typeList = types.get(mission.getType()); 
				if (typeList == null){
					typeList = new ArrayList<QuestConfig>(2) ; 
					types.put(mission.getType() ,typeList) ; 
				}
				typeList.add(mission) ;
				missions.put(mission.getId(), mission);
			}			

			this.levels = com.google.common.collect.ImmutableMap.copyOf(levels);			
			this.types = com.google.common.collect.ImmutableMap.copyOf(types);			
			this.missions = com.google.common.collect.ImmutableMap.copyOf(missions);

			log.info("load MissionConfig size[{}]", missions.size());

		} catch (Exception e) {
			throw new RuntimeException("load MissionConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
