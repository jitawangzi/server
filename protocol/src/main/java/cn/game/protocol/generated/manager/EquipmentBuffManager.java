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

import cn.game.protocol.generated.config.EquipmentBuffConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class EquipmentBuffManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(EquipmentBuffManager.class);

	private static EquipmentBuffManager instance = new EquipmentBuffManager();
	private static final String xmlFileName = "EquipmentBuff";
	
	private Map<Integer, EquipmentBuffConfig> equipmentbuffs = new HashMap<>();
	private Map<Integer,List<EquipmentBuffConfig>> groups = new HashMap<>();

	public static EquipmentBuffManager getInstance() {
		return instance;
	}

	private EquipmentBuffManager() {
		WatchServiceManager.getInstance().register(this);
	}
	/**
	 * 根据id获取数据，一般用这个方法，如果数据不存在，一般是配置错误，直接抛出异常
	 * 
	 * @param id
	 * @return
	 */
	public EquipmentBuffConfig getEquipmentBuffConfig(int id) {
		EquipmentBuffConfig config = this.equipmentbuffs.get(id);
		if (config == null) { 
			throw new NullPointerException("【EquipmentBuff】表的" + "id【" + id + "】不存在"); 
		}
		return config;
	}
	/**
	 * 根据id获取数据，允许返回null
	 * 
	 * @param id
	 * @return
	 */
	public EquipmentBuffConfig getEquipmentBuffConfigNullable(int id) {
		return this.equipmentbuffs.get(id);
	}

	public List<EquipmentBuffConfig> getGroupList(int group) {
		return this.groups.get(group);
	}
	public Collection<EquipmentBuffConfig> list() {
		return this.equipmentbuffs.values();
	}

	@Override
	public void load() {

		try {
			ClassLoader classLoader = Thread.currentThread().getClass().getClassLoader();
			if (classLoader == null) {
				classLoader = EquipmentBuffManager.class.getClassLoader();
			}
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, EquipmentBuffConfig> equipmentbuffs = new HashMap<>();
			Map<Integer, List<EquipmentBuffConfig>> groups = new HashMap<>();
			for (Element e : list) {
				EquipmentBuffConfig equipmentbuff = new EquipmentBuffConfig(e);
				List<EquipmentBuffConfig> groupList = groups.get(equipmentbuff.getGroup()); 
				if (groupList == null){
					groupList = new ArrayList<EquipmentBuffConfig>(2) ; 
					groups.put(equipmentbuff.getGroup() ,groupList) ; 
				}
				groupList.add(equipmentbuff) ;
				equipmentbuffs.put(equipmentbuff.getId(), equipmentbuff);
			}			

			this.groups = com.google.common.collect.ImmutableMap.copyOf(groups);			
			this.equipmentbuffs = com.google.common.collect.ImmutableMap.copyOf(equipmentbuffs);

			log.info("load EquipmentBuffConfig size[{}]", equipmentbuffs.size());

		} catch (Exception e) {
			throw new RuntimeException("load EquipmentBuffConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
