package cn.game.protocol.generated.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cn.game.protocol.generated.config.EquipmentConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class EquipmentManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(EquipmentManager.class);

	private static EquipmentManager instance = new EquipmentManager();
	private static final String xmlFileName = "Equipment";
	
	private Map<Integer, EquipmentConfig> equipments = new HashMap<>();

	public static EquipmentManager getInstance() {
		return instance;
	}

	private EquipmentManager() {
		WatchServiceManager.getInstance().register(this);
	}
	/**
	 * 根据id获取数据，一般用这个方法，如果数据不存在，一般是配置错误，直接抛出异常
	 * 
	 * @param id
	 * @return
	 */
	public EquipmentConfig getEquipmentConfig(int id) {
		EquipmentConfig config = this.equipments.get(id);
		if (config == null) { 
			throw new NullPointerException("【Equipment】表的" + "id【" + id + "】不存在"); 
		}
		return config;
	}
	/**
	 * 根据id获取数据，允许返回null
	 * 
	 * @param id
	 * @return
	 */
	public EquipmentConfig getEquipmentConfigNullable(int id) {
		return this.equipments.get(id);
	}

	public Collection<EquipmentConfig> list() {
		return this.equipments.values();
	}

	@Override
	public void load() {

		try {
			ClassLoader classLoader = Thread.currentThread().getClass().getClassLoader();
			if (classLoader == null) {
				classLoader = EquipmentManager.class.getClassLoader();
			}
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, EquipmentConfig> equipments = new HashMap<>();
			for (Element e : list) {
				EquipmentConfig equipment = new EquipmentConfig(e);
				equipments.put(equipment.getId(), equipment);
			}			

			this.equipments = com.google.common.collect.ImmutableMap.copyOf(equipments);

			log.info("load EquipmentConfig size[{}]", equipments.size());

		} catch (Exception e) {
			throw new RuntimeException("load EquipmentConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
