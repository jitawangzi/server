package cn.game.protocol.generated.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cn.game.protocol.generated.config.PlayerLevelConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

public class PlayerLevelManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(PlayerLevelManager.class);

	private static PlayerLevelManager instance = new PlayerLevelManager();
	public static final String xmlFileName = "PlayerLevel";
	private Map<Integer, PlayerLevelConfig> playerlevels = new HashMap<>();

	public static PlayerLevelManager getInstance() {
		return instance;
	}

	private PlayerLevelManager() {
		WatchServiceManager.getInstance().register(this);
	};

	public PlayerLevelConfig getPlayerLevelConfig(int id) {
		return this.playerlevels.get(id);
	}
	public Collection<PlayerLevelConfig> list() {
		return playerlevels.values();
	}

	@Override
	public void load() {

		try {
			Document document = XmlUtils.load("xml/" + xmlFileName + ".xml");
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			Map<Integer, PlayerLevelConfig> map = new HashMap<>();

			for (Element e : list) {
				PlayerLevelConfig playerlevel = new PlayerLevelConfig(e);
				map.put(playerlevel.getId(), playerlevel);
			}
			this.playerlevels = map;
			log.info("load PlayerLevelConfig size[{}]", map.size());

		} catch (Exception e) {
			log.error("load PlayerLevelConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
