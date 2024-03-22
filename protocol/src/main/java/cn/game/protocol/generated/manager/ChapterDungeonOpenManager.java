package cn.game.protocol.generated.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cn.game.protocol.generated.config.ChapterDungeonOpenConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

public class ChapterDungeonOpenManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(ChapterDungeonOpenManager.class);

	private static ChapterDungeonOpenManager instance = new ChapterDungeonOpenManager();
	public static final String xmlFileName = "ChapterDungeonOpen";
	private Map<Integer, ChapterDungeonOpenConfig> chapterdungeonopens = new HashMap<>();

	public static ChapterDungeonOpenManager getInstance() {
		return instance;
	}

	private ChapterDungeonOpenManager() {
		WatchServiceManager.getInstance().register(this);
	};

	public ChapterDungeonOpenConfig getChapterDungeonOpenConfig(int id) {
		return this.chapterdungeonopens.get(id);
	}
	public Collection<ChapterDungeonOpenConfig> list() {
		return chapterdungeonopens.values();
	}

	@Override
	public void load() {

		try {
			Document document = XmlUtils.load("xml/" + xmlFileName + ".xml");
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			Map<Integer, ChapterDungeonOpenConfig> map = new HashMap<>();

			for (Element e : list) {
				ChapterDungeonOpenConfig chapterdungeonopen = new ChapterDungeonOpenConfig(e);
				map.put(chapterdungeonopen.getId(), chapterdungeonopen);
			}
			this.chapterdungeonopens = map;
			log.info("load ChapterDungeonOpenConfig size[{}]", map.size());

		} catch (Exception e) {
			log.error("load ChapterDungeonOpenConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
