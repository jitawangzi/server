package cn.game.protocol.generated.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cn.game.protocol.generated.config.EquipAttributeConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

public class EquipAttributeManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(EquipAttributeManager.class);

	private static EquipAttributeManager instance = new EquipAttributeManager();
	public static final String xmlFileName = "EquipAttribute";
	private Map<Integer, EquipAttributeConfig> equipattributes = new HashMap<>();

	public static EquipAttributeManager getInstance() {
		return instance;
	}

	private EquipAttributeManager() {
		WatchServiceManager.getInstance().register(this);
	};

	public EquipAttributeConfig getEquipAttributeConfig(int id) {
		return this.equipattributes.get(id);
	}
	public Collection<EquipAttributeConfig> list() {
		return equipattributes.values();
	}

	@Override
	public void load() {

		try {
			Document document = XmlUtils.load("xml/" + xmlFileName + ".xml");
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			Map<Integer, EquipAttributeConfig> map = new HashMap<>();

			for (Element e : list) {
				EquipAttributeConfig equipattribute = new EquipAttributeConfig(e);
				map.put(equipattribute.getId(), equipattribute);
			}
			this.equipattributes = map;
			log.info("load EquipAttributeConfig size[{}]", map.size());

		} catch (Exception e) {
			log.error("load EquipAttributeConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
