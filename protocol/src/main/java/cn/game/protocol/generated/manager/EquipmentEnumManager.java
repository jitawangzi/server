package cn.game.protocol.generated.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cn.game.protocol.generated.config.EquipmentEnumConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class EquipmentEnumManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(EquipmentEnumManager.class);

	private static EquipmentEnumManager instance = new EquipmentEnumManager();
	private static final String xmlFileName = "EquipmentEnum";
	
	private Map<Integer, EquipmentEnumConfig> equipmentenums = new HashMap<>();

	public static EquipmentEnumManager getInstance() {
		return instance;
	}

	private EquipmentEnumManager() {
		WatchServiceManager.getInstance().register(this);
	}
	/**
	 * 根据id获取数据，一般用这个方法，如果数据不存在，一般是配置错误，直接抛出异常
	 * 
	 * @param id
	 * @return
	 */
	public EquipmentEnumConfig getEquipmentEnumConfig(int id) {
		EquipmentEnumConfig config = this.equipmentenums.get(id);
		if (config == null) { 
			throw new NullPointerException("【EquipmentEnum】表的" + "id【" + id + "】不存在"); 
		}
		return config;
	}
	/**
	 * 根据id获取数据，允许返回null
	 * 
	 * @param id
	 * @return
	 */
	public EquipmentEnumConfig getEquipmentEnumConfigNullable(int id) {
		return this.equipmentenums.get(id);
	}

	public Collection<EquipmentEnumConfig> list() {
		return this.equipmentenums.values();
	}

	@Override
	public void load() {

		try {
			ClassLoader classLoader = Thread.currentThread().getClass().getClassLoader();
			if (classLoader == null) {
				classLoader = EquipmentEnumManager.class.getClassLoader();
			}
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, EquipmentEnumConfig> equipmentenums = new HashMap<>();
			for (Element e : list) {
				EquipmentEnumConfig equipmentenum = new EquipmentEnumConfig(e);
				equipmentenums.put(equipmentenum.getId(), equipmentenum);
			}			

			this.equipmentenums = com.google.common.collect.ImmutableMap.copyOf(equipmentenums);

			log.info("load EquipmentEnumConfig size[{}]", equipmentenums.size());

		} catch (Exception e) {
			throw new RuntimeException("load EquipmentEnumConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
