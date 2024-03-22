package cn.game.protocol.generated.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cn.game.protocol.generated.config.BuildingFeatureConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class BuildingFeatureManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(BuildingFeatureManager.class);

	private static BuildingFeatureManager instance = new BuildingFeatureManager();
	private static final String xmlFileName = "BuildingFeature";
	
	private Map<Integer, BuildingFeatureConfig> buildingfeatures = new HashMap<>();

	public static BuildingFeatureManager getInstance() {
		return instance;
	}

	private BuildingFeatureManager() {
		WatchServiceManager.getInstance().register(this);
	}
	/**
	 * 根据id获取数据，一般用这个方法，如果数据不存在，一般是配置错误，直接抛出异常
	 * 
	 * @param id
	 * @return
	 */
	public BuildingFeatureConfig getBuildingFeatureConfig(int id) {
		BuildingFeatureConfig config = this.buildingfeatures.get(id);
		if (config == null) { 
			throw new NullPointerException("【BuildingFeature】表的" + "id【" + id + "】不存在"); 
		}
		return config;
	}
	/**
	 * 根据id获取数据，允许返回null
	 * 
	 * @param id
	 * @return
	 */
	public BuildingFeatureConfig getBuildingFeatureConfigNullable(int id) {
		return this.buildingfeatures.get(id);
	}

	public Collection<BuildingFeatureConfig> list() {
		return this.buildingfeatures.values();
	}

	@Override
	public void load() {

		try {
			ClassLoader classLoader = Thread.currentThread().getClass().getClassLoader();
			if (classLoader == null) {
				classLoader = BuildingFeatureManager.class.getClassLoader();
			}
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, BuildingFeatureConfig> map = new HashMap<>();
			for (Element e : list) {
				BuildingFeatureConfig buildingfeature = new BuildingFeatureConfig(e);
				map.put(buildingfeature.getId(), buildingfeature);
			}
			
			this.buildingfeatures = map;

			log.info("load BuildingFeatureConfig size[{}]", map.size());

		} catch (Exception e) {
			log.error("load BuildingFeatureConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
