package cn.game.protocol.generated.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cn.game.protocol.generated.config.HeroUpgradeConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

public class HeroUpgradeManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(HeroUpgradeManager.class);

	private static HeroUpgradeManager instance = new HeroUpgradeManager();
	public static final String xmlFileName = "HeroUpgrade";
	private Map<Integer, HeroUpgradeConfig> heroupgrades = new HashMap<>();

	public static HeroUpgradeManager getInstance() {
		return instance;
	}

	private HeroUpgradeManager() {
		WatchServiceManager.getInstance().register(this);
	};

	public HeroUpgradeConfig getHeroUpgradeConfig(int id) {
		return this.heroupgrades.get(id);
	}
	public Collection<HeroUpgradeConfig> list() {
		return heroupgrades.values();
	}

	@Override
	public void load() {

		try {
			Document document = XmlUtils.load("xml/" + xmlFileName + ".xml");
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			Map<Integer, HeroUpgradeConfig> map = new HashMap<>();

			for (Element e : list) {
				HeroUpgradeConfig heroupgrade = new HeroUpgradeConfig(e);
				map.put(heroupgrade.getId(), heroupgrade);
			}
			this.heroupgrades = map;
			log.info("load HeroUpgradeConfig size[{}]", map.size());

		} catch (Exception e) {
			log.error("load HeroUpgradeConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
