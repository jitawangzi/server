package cn.game.protocol.generated.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cn.game.protocol.generated.config.EquipSuitConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

public class EquipSuitManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(EquipSuitManager.class);

	private static EquipSuitManager instance = new EquipSuitManager();
	public static final String xmlFileName = "EquipSuit";
	private Map<Integer, EquipSuitConfig> equipsuits = new HashMap<>();

	public static EquipSuitManager getInstance() {
		return instance;
	}

	private EquipSuitManager() {
		WatchServiceManager.getInstance().register(this);
	};

	public EquipSuitConfig getEquipSuitConfig(int id) {
		return this.equipsuits.get(id);
	}
	public Collection<EquipSuitConfig> list() {
		return equipsuits.values();
	}

	@Override
	public void load() {

		try {
			Document document = XmlUtils.load("xml/" + xmlFileName + ".xml");
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			Map<Integer, EquipSuitConfig> map = new HashMap<>();

			for (Element e : list) {
				EquipSuitConfig equipsuit = new EquipSuitConfig(e);
				map.put(equipsuit.getId(), equipsuit);
			}
			this.equipsuits = map;
			log.info("load EquipSuitConfig size[{}]", map.size());

		} catch (Exception e) {
			log.error("load EquipSuitConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
