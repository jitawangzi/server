package cn.game.protocol.generated.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cn.game.protocol.generated.config.ChapterDungeonConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

public class ChapterDungeonManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(ChapterDungeonManager.class);

	private static ChapterDungeonManager instance = new ChapterDungeonManager();
	public static final String xmlFileName = "ChapterDungeon";
	private Map<Integer, ChapterDungeonConfig> chapterdungeons = new HashMap<>();

	public static ChapterDungeonManager getInstance() {
		return instance;
	}

	private ChapterDungeonManager() {
		WatchServiceManager.getInstance().register(this);
	};

	public ChapterDungeonConfig getChapterDungeonConfig(int id) {
		return this.chapterdungeons.get(id);
	}
	public Collection<ChapterDungeonConfig> list() {
		return chapterdungeons.values();
	}

	@Override
	public void load() {

		try {
			Document document = XmlUtils.load("xml/" + xmlFileName + ".xml");
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			Map<Integer, ChapterDungeonConfig> map = new HashMap<>();

			for (Element e : list) {
				ChapterDungeonConfig chapterdungeon = new ChapterDungeonConfig(e);
				map.put(chapterdungeon.getId(), chapterdungeon);
			}
			this.chapterdungeons = map;
			log.info("load ChapterDungeonConfig size[{}]", map.size());

		} catch (Exception e) {
			log.error("load ChapterDungeonConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
