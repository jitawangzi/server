package cn.game.protocol.generated.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cn.game.protocol.generated.config.HeroLevelConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

public class HeroLevelManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(HeroLevelManager.class);

	private static HeroLevelManager instance = new HeroLevelManager();
	public static final String xmlFileName = "HeroLevel";
	private Map<Integer, HeroLevelConfig> herolevels = new HashMap<>();

	public static HeroLevelManager getInstance() {
		return instance;
	}

	private HeroLevelManager() {
		WatchServiceManager.getInstance().register(this);
	};

	public HeroLevelConfig getHeroLevelConfig(int id) {
		return this.herolevels.get(id);
	}
	public Collection<HeroLevelConfig> list() {
		return herolevels.values();
	}

	@Override
	public void load() {

		try {
			Document document = XmlUtils.load("xml/" + xmlFileName + ".xml");
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			Map<Integer, HeroLevelConfig> map = new HashMap<>();

			for (Element e : list) {
				HeroLevelConfig herolevel = new HeroLevelConfig(e);
				map.put(herolevel.getId(), herolevel);
			}
			this.herolevels = map;
			log.info("load HeroLevelConfig size[{}]", map.size());

		} catch (Exception e) {
			log.error("load HeroLevelConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
