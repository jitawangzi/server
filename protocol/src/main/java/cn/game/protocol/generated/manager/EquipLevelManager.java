package cn.game.protocol.generated.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cn.game.protocol.generated.config.EquipLevelConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

public class EquipLevelManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(EquipLevelManager.class);

	private static EquipLevelManager instance = new EquipLevelManager();
	public static final String xmlFileName = "EquipLevel";
	private Map<Integer, EquipLevelConfig> equiplevels = new HashMap<>();

	public static EquipLevelManager getInstance() {
		return instance;
	}

	private EquipLevelManager() {
		WatchServiceManager.getInstance().register(this);
	};

	public EquipLevelConfig getEquipLevelConfig(int id) {
		return this.equiplevels.get(id);
	}
	public Collection<EquipLevelConfig> list() {
		return equiplevels.values();
	}

	@Override
	public void load() {

		try {
			Document document = XmlUtils.load("xml/" + xmlFileName + ".xml");
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			Map<Integer, EquipLevelConfig> map = new HashMap<>();

			for (Element e : list) {
				EquipLevelConfig equiplevel = new EquipLevelConfig(e);
				map.put(equiplevel.getId(), equiplevel);
			}
			this.equiplevels = map;
			log.info("load EquipLevelConfig size[{}]", map.size());

		} catch (Exception e) {
			log.error("load EquipLevelConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
