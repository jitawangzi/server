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

import cn.game.protocol.generated.config.CityEquipmentStoreConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class CityEquipmentStoreManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(CityEquipmentStoreManager.class);

	private static CityEquipmentStoreManager instance = new CityEquipmentStoreManager();
	private static final String xmlFileName = "CityEquipmentStore";
	
	private Map<Integer, CityEquipmentStoreConfig> cityequipmentstores = new HashMap<>();
	private Map<Integer,List<CityEquipmentStoreConfig>> levels = new HashMap<>();

	public static CityEquipmentStoreManager getInstance() {
		return instance;
	}

	private CityEquipmentStoreManager() {
		WatchServiceManager.getInstance().register(this);
	}
	/**
	 * 根据id获取数据，一般用这个方法，如果数据不存在，一般是配置错误，直接抛出异常
	 * 
	 * @param id
	 * @return
	 */
	public CityEquipmentStoreConfig getCityEquipmentStoreConfig(int id) {
		CityEquipmentStoreConfig config = this.cityequipmentstores.get(id);
		if (config == null) { 
			throw new NullPointerException("【CityEquipmentStore】表的" + "id【" + id + "】不存在"); 
		}
		return config;
	}
	/**
	 * 根据id获取数据，允许返回null
	 * 
	 * @param id
	 * @return
	 */
	public CityEquipmentStoreConfig getCityEquipmentStoreConfigNullable(int id) {
		return this.cityequipmentstores.get(id);
	}

	public List<CityEquipmentStoreConfig> getLevelList(int level) {
		return this.levels.get(level);
	}
	public Collection<CityEquipmentStoreConfig> list() {
		return this.cityequipmentstores.values();
	}

	@Override
	public void load() {

		try {
			ClassLoader classLoader = Thread.currentThread().getClass().getClassLoader();
			if (classLoader == null) {
				classLoader = CityEquipmentStoreManager.class.getClassLoader();
			}
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, CityEquipmentStoreConfig> cityequipmentstores = new HashMap<>();
			Map<Integer, List<CityEquipmentStoreConfig>> levels = new HashMap<>();
			for (Element e : list) {
				CityEquipmentStoreConfig cityequipmentstore = new CityEquipmentStoreConfig(e);
				List<CityEquipmentStoreConfig> levelList = levels.get(cityequipmentstore.getLevel()); 
				if (levelList == null){
					levelList = new ArrayList<CityEquipmentStoreConfig>(2) ; 
					levels.put(cityequipmentstore.getLevel() ,levelList) ; 
				}
				levelList.add(cityequipmentstore) ;
				cityequipmentstores.put(cityequipmentstore.getId(), cityequipmentstore);
			}			

			this.levels = com.google.common.collect.ImmutableMap.copyOf(levels);			
			this.cityequipmentstores = com.google.common.collect.ImmutableMap.copyOf(cityequipmentstores);

			log.info("load CityEquipmentStoreConfig size[{}]", cityequipmentstores.size());

		} catch (Exception e) {
			throw new RuntimeException("load CityEquipmentStoreConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
