package cn.game.protocol.generated.manager;

import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cn.game.protocol.generated.config.CombatLogConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

public class CombatLogManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(CombatLogManager.class);

	private static CombatLogManager instance = new CombatLogManager();
	public static final String xmlFileName = "CombatLog";
	
	private Map<Integer, CombatLogConfig> combatlogs = new HashMap<>();

	public static CombatLogManager getInstance() {
		return instance;
	}

	private CombatLogManager() {
		WatchServiceManager.getInstance().register(this);
	}
	public CombatLogConfig getCombatLogConfig(int id) {
		return this.combatlogs.get(id);
	}

	public Collection<CombatLogConfig> list() {
		return this.combatlogs.values();
	}

	@Override
	public void load() {

		try {
			ClassLoader classLoader = Thread.currentThread().getClass().getClassLoader();
			if (classLoader == null) {
				classLoader = CombatLogManager.class.getClassLoader();
			}
			URL url = classLoader.getResource("xml/" + xmlFileName + ".xml");
			Document document = XmlUtils.load(url.getPath());
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, CombatLogConfig> map = new HashMap<>();
			for (Element e : list) {
				CombatLogConfig combatlog = new CombatLogConfig(e);
				map.put(combatlog.getId(), combatlog);
			}
			
			this.combatlogs = map;

			log.info("load CombatLogConfig size[{}]", map.size());

		} catch (Exception e) {
			log.error("load CombatLogConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
