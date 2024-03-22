package cn.game.protocol.generated.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cn.game.protocol.generated.config.BuildingOccupationTalentConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class BuildingOccupationTalentManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(BuildingOccupationTalentManager.class);

	private static BuildingOccupationTalentManager instance = new BuildingOccupationTalentManager();
	private static final String xmlFileName = "BuildingOccupationTalent";
	
	private Map<Integer, BuildingOccupationTalentConfig> buildingoccupationtalents = new HashMap<>();

	public static BuildingOccupationTalentManager getInstance() {
		return instance;
	}

	private BuildingOccupationTalentManager() {
		WatchServiceManager.getInstance().register(this);
	}
	/**
	 * 根据id获取数据，一般用这个方法，如果数据不存在，一般是配置错误，直接抛出异常
	 * 
	 * @param id
	 * @return
	 */
	public BuildingOccupationTalentConfig getBuildingOccupationTalentConfig(int id) {
		BuildingOccupationTalentConfig config = this.buildingoccupationtalents.get(id);
		if (config == null) { 
			throw new NullPointerException("【BuildingOccupationTalent】表的" + "id【" + id + "】不存在"); 
		}
		return config;
	}
	/**
	 * 根据id获取数据，允许返回null
	 * 
	 * @param id
	 * @return
	 */
	public BuildingOccupationTalentConfig getBuildingOccupationTalentConfigNullable(int id) {
		return this.buildingoccupationtalents.get(id);
	}

	public Collection<BuildingOccupationTalentConfig> list() {
		return this.buildingoccupationtalents.values();
	}

	@Override
	public void load() {

		try {
			ClassLoader classLoader = Thread.currentThread().getClass().getClassLoader();
			if (classLoader == null) {
				classLoader = BuildingOccupationTalentManager.class.getClassLoader();
			}
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, BuildingOccupationTalentConfig> map = new HashMap<>();
			for (Element e : list) {
				BuildingOccupationTalentConfig buildingoccupationtalent = new BuildingOccupationTalentConfig(e);
				map.put(buildingoccupationtalent.getId(), buildingoccupationtalent);
			}
			
			this.buildingoccupationtalents = map;

			log.info("load BuildingOccupationTalentConfig size[{}]", map.size());

		} catch (Exception e) {
			log.error("load BuildingOccupationTalentConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
