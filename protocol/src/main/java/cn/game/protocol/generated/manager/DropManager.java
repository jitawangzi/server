package cn.game.protocol.generated.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cn.game.protocol.generated.config.DropConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

public class DropManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(DropManager.class);

	private static DropManager instance = new DropManager();
	public static final String xmlFileName = "Drop";
	private Map<Integer, DropConfig> drops = new HashMap<>();

	public static DropManager getInstance() {
		return instance;
	}

	private DropManager() {
		WatchServiceManager.getInstance().register(this);
	};

	public DropConfig getDropConfig(int id) {
		return this.drops.get(id);
	}
	public Collection<DropConfig> list() {
		return drops.values();
	}

	@Override
	public void load() {

		try {
			Document document = XmlUtils.load("xml/" + xmlFileName + ".xml");
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			Map<Integer, DropConfig> map = new HashMap<>();

			for (Element e : list) {
				DropConfig drop = new DropConfig(e);
				map.put(drop.getId(), drop);
			}
			this.drops = map;
			log.info("load DropConfig size[{}]", map.size());

		} catch (Exception e) {
			log.error("load DropConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
