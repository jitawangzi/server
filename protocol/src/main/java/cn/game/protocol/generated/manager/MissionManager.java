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

import cn.game.protocol.generated.enume.MissionTypeEnum;
import cn.game.protocol.generated.config.MissionConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class MissionManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(MissionManager.class);

	private static MissionManager instance = new MissionManager();
	private static final String xmlFileName = "Mission";
	
	private Map<Integer, MissionConfig> missions = new HashMap<>();
	private Map<Integer,List<MissionConfig>> levels = new HashMap<>();
	private Map<MissionTypeEnum,List<MissionConfig>> types = new HashMap<>();

	public static MissionManager getInstance() {
		return instance;
	}

	private MissionManager() {
		WatchServiceManager.getInstance().register(this);
	}
	/**
	 * 根据id获取数据，一般用这个方法，如果数据不存在，一般是配置错误，直接抛出异常
	 * 
	 * @param id
	 * @return
	 */
	public MissionConfig getMissionConfig(int id) {
		MissionConfig config = this.missions.get(id);
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
	public MissionConfig getMissionConfigNullable(int id) {
		return this.missions.get(id);
	}

	public List<MissionConfig> getLevelList(int level) {
		return this.levels.get(level);
	}
	public List<MissionConfig> getTypeList(MissionTypeEnum type) {
		return this.types.get(type);
	}
	public Collection<MissionConfig> list() {
		return this.missions.values();
	}

	@Override
	public void load() {

		try {
			ClassLoader classLoader = Thread.currentThread().getClass().getClassLoader();
			if (classLoader == null) {
				classLoader = MissionManager.class.getClassLoader();
			}
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, MissionConfig> missions = new HashMap<>();
			Map<Integer, List<MissionConfig>> levels = new HashMap<>();
			Map<MissionTypeEnum, List<MissionConfig>> types = new HashMap<>();
			for (Element e : list) {
				MissionConfig mission = new MissionConfig(e);
				List<MissionConfig> levelList = levels.get(mission.getLevel()); 
				if (levelList == null){
					levelList = new ArrayList<MissionConfig>(2) ; 
					levels.put(mission.getLevel() ,levelList) ; 
				}
				levelList.add(mission) ;
				List<MissionConfig> typeList = types.get(mission.getType()); 
				if (typeList == null){
					typeList = new ArrayList<MissionConfig>(2) ; 
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
