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

import cn.game.protocol.generated.config.BuildingUpgradeConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class BuildingUpgradeManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(BuildingUpgradeManager.class);

	private static BuildingUpgradeManager instance = new BuildingUpgradeManager();
	private static final String xmlFileName = "BuildingUpgrade";
	
	private Map<Integer, BuildingUpgradeConfig> buildingupgrades = new HashMap<>();
	private Map<Long,List<BuildingUpgradeConfig>> buildingIdlevels = new HashMap<>();

	public static BuildingUpgradeManager getInstance() {
		return instance;
	}

	private BuildingUpgradeManager() {
		WatchServiceManager.getInstance().register(this);
	}
	/**
	 * 根据id获取数据，一般用这个方法，如果数据不存在，一般是配置错误，直接抛出异常
	 * 
	 * @param id
	 * @return
	 */
	public BuildingUpgradeConfig getBuildingUpgradeConfig(int id) {
		BuildingUpgradeConfig config = this.buildingupgrades.get(id);
		if (config == null) { 
			throw new NullPointerException("【BuildingUpgrade】表的" + "id【" + id + "】不存在"); 
		}
		return config;
	}
	/**
	 * 根据id获取数据，允许返回null
	 * 
	 * @param id
	 * @return
	 */
	public BuildingUpgradeConfig getBuildingUpgradeConfigNullable(int id) {
		return this.buildingupgrades.get(id);
	}

	private long hashIndex1(int buildingId,int level) {
		if (buildingId > 999999) {
			throw new IllegalArgumentException("buildingId 联合索引范围超过最大值999999");
		}
		if (level > 999999) {
			throw new IllegalArgumentException("level 联合索引范围超过最大值999999");
		}
		long result = 0;
		result = result | (long) buildingId << 43;
		result = result | (long) level << 22;
		return result;
	}

  	public List<BuildingUpgradeConfig> getBuildingIdlevelList(int buildingId,int level) {
		return this.buildingIdlevels.get(hashIndex1(buildingId,level));
	}
	public Collection<BuildingUpgradeConfig> list() {
		return this.buildingupgrades.values();
	}

	@Override
	public void load() {

		try {
			ClassLoader classLoader = Thread.currentThread().getClass().getClassLoader();
			if (classLoader == null) {
				classLoader = BuildingUpgradeManager.class.getClassLoader();
			}
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, BuildingUpgradeConfig> buildingupgrades = new HashMap<>();
  			Map<Long, List<BuildingUpgradeConfig>> buildingIdlevels = new HashMap<>();
			for (Element e : list) {
				BuildingUpgradeConfig buildingupgrade = new BuildingUpgradeConfig(e);
  				List<BuildingUpgradeConfig> buildingIdlevelsList = buildingIdlevels.get(hashIndex1(buildingupgrade.getBuildingId(),buildingupgrade.getLevel())); 
				if (buildingIdlevelsList == null){
					buildingIdlevelsList = new ArrayList<BuildingUpgradeConfig>(2) ; 
					buildingIdlevels.put(hashIndex1(buildingupgrade.getBuildingId(),buildingupgrade.getLevel()) ,buildingIdlevelsList) ; 
				}
				buildingIdlevelsList.add(buildingupgrade) ;
				buildingupgrades.put(buildingupgrade.getId(), buildingupgrade);
			}			

  			this.buildingIdlevels = com.google.common.collect.ImmutableMap.copyOf(buildingIdlevels);
			this.buildingupgrades = com.google.common.collect.ImmutableMap.copyOf(buildingupgrades);

			log.info("load BuildingUpgradeConfig size[{}]", buildingupgrades.size());

		} catch (Exception e) {
			throw new RuntimeException("load BuildingUpgradeConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
