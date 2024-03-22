package cn.game.protocol.generated.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cn.game.protocol.generated.config.BuildingLocationConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class BuildingLocationManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(BuildingLocationManager.class);

	private static BuildingLocationManager instance = new BuildingLocationManager();
	private static final String xmlFileName = "BuildingLocation";
	
	private Map<Integer, BuildingLocationConfig> buildinglocations = new HashMap<>();

	public static BuildingLocationManager getInstance() {
		return instance;
	}

	private BuildingLocationManager() {
		WatchServiceManager.getInstance().register(this);
	}
	/**
	 * 根据id获取数据，一般用这个方法，如果数据不存在，一般是配置错误，直接抛出异常
	 * 
	 * @param id
	 * @return
	 */
	public BuildingLocationConfig getBuildingLocationConfig(int id) {
		BuildingLocationConfig config = this.buildinglocations.get(id);
		if (config == null) { 
			throw new NullPointerException("【BuildingLocation】表的" + "id【" + id + "】不存在"); 
		}
		return config;
	}
	/**
	 * 根据id获取数据，允许返回null
	 * 
	 * @param id
	 * @return
	 */
	public BuildingLocationConfig getBuildingLocationConfigNullable(int id) {
		return this.buildinglocations.get(id);
	}

	public Collection<BuildingLocationConfig> list() {
		return this.buildinglocations.values();
	}

	@Override
	public void load() {

		try {
			ClassLoader classLoader = Thread.currentThread().getClass().getClassLoader();
			if (classLoader == null) {
				classLoader = BuildingLocationManager.class.getClassLoader();
			}
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, BuildingLocationConfig> buildinglocations = new HashMap<>();
			for (Element e : list) {
				BuildingLocationConfig buildinglocation = new BuildingLocationConfig(e);
				buildinglocations.put(buildinglocation.getId(), buildinglocation);
			}			

			this.buildinglocations = com.google.common.collect.ImmutableMap.copyOf(buildinglocations);

			log.info("load BuildingLocationConfig size[{}]", buildinglocations.size());

		} catch (Exception e) {
			throw new RuntimeException("load BuildingLocationConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
