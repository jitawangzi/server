package cn.game.protocol.generated.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cn.game.protocol.generated.config.ContentOpenConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

public class ContentOpenManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(ContentOpenManager.class);

	private static ContentOpenManager instance = new ContentOpenManager();
	public static final String xmlFileName = "ContentOpen";
	private Map<Integer, ContentOpenConfig> contentopens = new HashMap<>();

	public static ContentOpenManager getInstance() {
		return instance;
	}

	private ContentOpenManager() {
		WatchServiceManager.getInstance().register(this);
	};

	public ContentOpenConfig getContentOpenConfig(int id) {
		return this.contentopens.get(id);
	}
	public Collection<ContentOpenConfig> list() {
		return contentopens.values();
	}

	@Override
	public void load() {

		try {
			Document document = XmlUtils.load("xml/" + xmlFileName + ".xml");
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			Map<Integer, ContentOpenConfig> map = new HashMap<>();

			for (Element e : list) {
				ContentOpenConfig contentopen = new ContentOpenConfig(e);
				map.put(contentopen.getId(), contentopen);
			}
			this.contentopens = map;
			log.info("load ContentOpenConfig size[{}]", map.size());

		} catch (Exception e) {
			log.error("load ContentOpenConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
