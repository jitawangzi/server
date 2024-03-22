package cn.game.protocol.generated.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cn.game.protocol.generated.config.ChiefSkillConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

public class ChiefSkillManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(ChiefSkillManager.class);

	private static ChiefSkillManager instance = new ChiefSkillManager();
	public static final String xmlFileName = "ChiefSkill";
	private Map<Integer, ChiefSkillConfig> chiefskills = new HashMap<>();

	public static ChiefSkillManager getInstance() {
		return instance;
	}

	private ChiefSkillManager() {
		WatchServiceManager.getInstance().register(this);
	};

	public ChiefSkillConfig getChiefSkillConfig(int id) {
		return this.chiefskills.get(id);
	}
	public Collection<ChiefSkillConfig> list() {
		return chiefskills.values();
	}

	@Override
	public void load() {

		try {
			Document document = XmlUtils.load("xml/" + xmlFileName + ".xml");
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			Map<Integer, ChiefSkillConfig> map = new HashMap<>();

			for (Element e : list) {
				ChiefSkillConfig chiefskill = new ChiefSkillConfig(e);
				map.put(chiefskill.getId(), chiefskill);
			}
			this.chiefskills = map;
			log.info("load ChiefSkillConfig size[{}]", map.size());

		} catch (Exception e) {
			log.error("load ChiefSkillConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
