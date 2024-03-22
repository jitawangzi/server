package cn.game.protocol.generated.manager;

import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cn.game.protocol.generated.config.CombatLogGroupConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

public class CombatLogGroupManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(CombatLogGroupManager.class);

	private static CombatLogGroupManager instance = new CombatLogGroupManager();
	public static final String xmlFileName = "CombatLogGroup";
	
	private Map<Integer, CombatLogGroupConfig> combatloggroups = new HashMap<>();

	public static CombatLogGroupManager getInstance() {
		return instance;
	}

	private CombatLogGroupManager() {
		WatchServiceManager.getInstance().register(this);
	}
	public CombatLogGroupConfig getCombatLogGroupConfig(int id) {
		return this.combatloggroups.get(id);
	}

	public Collection<CombatLogGroupConfig> list() {
		return this.combatloggroups.values();
	}

	@Override
	public void load() {

		try {
			ClassLoader classLoader = Thread.currentThread().getClass().getClassLoader();
			if (classLoader == null) {
				classLoader = CombatLogGroupManager.class.getClassLoader();
			}
			URL url = classLoader.getResource("xml/" + xmlFileName + ".xml");
			Document document = XmlUtils.load(url.getPath());
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, CombatLogGroupConfig> map = new HashMap<>();
			for (Element e : list) {
				CombatLogGroupConfig combatloggroup = new CombatLogGroupConfig(e);
				map.put(combatloggroup.getId(), combatloggroup);
			}
			
			this.combatloggroups = map;

			log.info("load CombatLogGroupConfig size[{}]", map.size());

		} catch (Exception e) {
			log.error("load CombatLogGroupConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
