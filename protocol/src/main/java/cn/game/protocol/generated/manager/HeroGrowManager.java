package cn.game.protocol.generated.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cn.game.protocol.generated.config.HeroGrowConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

public class HeroGrowManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(HeroGrowManager.class);

	private static HeroGrowManager instance = new HeroGrowManager();
	public static final String xmlFileName = "HeroGrow";
	private Map<Integer, HeroGrowConfig> herogrows = new HashMap<>();

	public static HeroGrowManager getInstance() {
		return instance;
	}

	private HeroGrowManager() {
		WatchServiceManager.getInstance().register(this);
	};

	public HeroGrowConfig getHeroGrowConfig(int id) {
		return this.herogrows.get(id);
	}
	public Collection<HeroGrowConfig> list() {
		return herogrows.values();
	}

	@Override
	public void load() {

		try {
			Document document = XmlUtils.load("xml/" + xmlFileName + ".xml");
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			Map<Integer, HeroGrowConfig> map = new HashMap<>();

			for (Element e : list) {
				HeroGrowConfig herogrow = new HeroGrowConfig(e);
				map.put(herogrow.getId(), herogrow);
			}
			this.herogrows = map;
			log.info("load HeroGrowConfig size[{}]", map.size());

		} catch (Exception e) {
			log.error("load HeroGrowConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
