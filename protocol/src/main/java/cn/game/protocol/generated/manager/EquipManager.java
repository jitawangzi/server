package cn.game.protocol.generated.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cn.game.protocol.generated.config.EquipConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

public class EquipManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(EquipManager.class);

	private static EquipManager instance = new EquipManager();
	public static final String xmlFileName = "Equip";
	private Map<Integer, EquipConfig> equips = new HashMap<>();

	public static EquipManager getInstance() {
		return instance;
	}

	private EquipManager() {
		WatchServiceManager.getInstance().register(this);
	};

	public EquipConfig getEquipConfig(int id) {
		return this.equips.get(id);
	}
	public Collection<EquipConfig> list() {
		return equips.values();
	}

	@Override
	public void load() {

		try {
			Document document = XmlUtils.load("xml/" + xmlFileName + ".xml");
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			Map<Integer, EquipConfig> map = new HashMap<>();

			for (Element e : list) {
				EquipConfig equip = new EquipConfig(e);
				map.put(equip.getId(), equip);
			}
			this.equips = map;
			log.info("load EquipConfig size[{}]", map.size());

		} catch (Exception e) {
			log.error("load EquipConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
