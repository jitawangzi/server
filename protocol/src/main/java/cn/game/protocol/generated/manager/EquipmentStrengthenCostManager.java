package cn.game.protocol.generated.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cn.game.protocol.generated.config.EquipmentStrengthenCostConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class EquipmentStrengthenCostManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(EquipmentStrengthenCostManager.class);

	private static EquipmentStrengthenCostManager instance = new EquipmentStrengthenCostManager();
	private static final String xmlFileName = "EquipmentStrengthenCost";
	
	private Map<Integer, EquipmentStrengthenCostConfig> equipmentstrengthencosts = new HashMap<>();

	public static EquipmentStrengthenCostManager getInstance() {
		return instance;
	}

	private EquipmentStrengthenCostManager() {
		WatchServiceManager.getInstance().register(this);
	}
	/**
	 * 根据id获取数据，一般用这个方法，如果数据不存在，一般是配置错误，直接抛出异常
	 * 
	 * @param id
	 * @return
	 */
	public EquipmentStrengthenCostConfig getEquipmentStrengthenCostConfig(int id) {
		EquipmentStrengthenCostConfig config = this.equipmentstrengthencosts.get(id);
		if (config == null) { 
			throw new NullPointerException("【EquipmentStrengthenCost】表的" + "id【" + id + "】不存在"); 
		}
		return config;
	}
	/**
	 * 根据id获取数据，允许返回null
	 * 
	 * @param id
	 * @return
	 */
	public EquipmentStrengthenCostConfig getEquipmentStrengthenCostConfigNullable(int id) {
		return this.equipmentstrengthencosts.get(id);
	}

	public Collection<EquipmentStrengthenCostConfig> list() {
		return this.equipmentstrengthencosts.values();
	}

	@Override
	public void load() {

		try {
			ClassLoader classLoader = Thread.currentThread().getClass().getClassLoader();
			if (classLoader == null) {
				classLoader = EquipmentStrengthenCostManager.class.getClassLoader();
			}
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, EquipmentStrengthenCostConfig> equipmentstrengthencosts = new HashMap<>();
			for (Element e : list) {
				EquipmentStrengthenCostConfig equipmentstrengthencost = new EquipmentStrengthenCostConfig(e);
				equipmentstrengthencosts.put(equipmentstrengthencost.getId(), equipmentstrengthencost);
			}			

			this.equipmentstrengthencosts = com.google.common.collect.ImmutableMap.copyOf(equipmentstrengthencosts);

			log.info("load EquipmentStrengthenCostConfig size[{}]", equipmentstrengthencosts.size());

		} catch (Exception e) {
			throw new RuntimeException("load EquipmentStrengthenCostConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
