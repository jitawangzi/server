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

import cn.game.protocol.generated.config.BuildingConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class BuildingManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(BuildingManager.class);

	private static BuildingManager instance = new BuildingManager();
	private static final String xmlFileName = "Building";
	
	private Map<Integer, BuildingConfig> buildings = new HashMap<>();
	private Map<Integer,List<BuildingConfig>> types = new HashMap<>();

	public static BuildingManager getInstance() {
		return instance;
	}

	private BuildingManager() {
		WatchServiceManager.getInstance().register(this);
	}
	/**
	 * 根据id获取数据，一般用这个方法，如果数据不存在，一般是配置错误，直接抛出异常
	 * 
	 * @param id
	 * @return
	 */
	public BuildingConfig getBuildingConfig(int id) {
		BuildingConfig config = this.buildings.get(id);
		if (config == null) { 
			throw new NullPointerException("【Building】表的" + "id【" + id + "】不存在"); 
		}
		return config;
	}
	/**
	 * 根据id获取数据，允许返回null
	 * 
	 * @param id
	 * @return
	 */
	public BuildingConfig getBuildingConfigNullable(int id) {
		return this.buildings.get(id);
	}

	public List<BuildingConfig> getTypeList(int type) {
		return this.types.get(type);
	}
	public Collection<BuildingConfig> list() {
		return this.buildings.values();
	}

	@Override
	public void load() {

		try {
			ClassLoader classLoader = Thread.currentThread().getClass().getClassLoader();
			if (classLoader == null) {
				classLoader = BuildingManager.class.getClassLoader();
			}
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, BuildingConfig> buildings = new HashMap<>();
			Map<Integer, List<BuildingConfig>> types = new HashMap<>();
			for (Element e : list) {
				BuildingConfig building = new BuildingConfig(e);
				List<BuildingConfig> typeList = types.get(building.getType()); 
				if (typeList == null){
					typeList = new ArrayList<BuildingConfig>(2) ; 
					types.put(building.getType() ,typeList) ; 
				}
				typeList.add(building) ;
				buildings.put(building.getId(), building);
			}			

			this.types = com.google.common.collect.ImmutableMap.copyOf(types);			
			this.buildings = com.google.common.collect.ImmutableMap.copyOf(buildings);

			log.info("load BuildingConfig size[{}]", buildings.size());

		} catch (Exception e) {
			throw new RuntimeException("load BuildingConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
