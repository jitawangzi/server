package cn.game.protocol.generated.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cn.game.protocol.generated.config.ChapterPacksConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class ChapterPacksManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(ChapterPacksManager.class);

	private static ChapterPacksManager instance = new ChapterPacksManager();
	private static final String xmlFileName = "ChapterPacks";
	
	/** 总数据，按id取值 */
	private Map<Integer, ChapterPacksConfig> chapterpackss = new HashMap<>();

	public static ChapterPacksManager instance() {
		return instance;
	}
	private ChapterPacksManager() {
		WatchServiceManager.getInstance().register(this);
	}
	/**
	 * 根据id获取数据，一般用这个方法，如果数据不存在，一般是配置错误，直接抛出异常
	 * 
	 * @param id
	 * @return
	 */
	public ChapterPacksConfig get(int id) {
		ChapterPacksConfig config = this.chapterpackss.get(id);
		if (config == null) { 
			throw new NullPointerException("【ChapterPacks】表的" + "id【" + id + "】不存在"); 
		}
		return config;
	}
	/**
	 * 根据id获取数据，允许返回null
	 * 
	 * @param id
	 * @return
	 */
	public ChapterPacksConfig getNullable(int id) {
		return this.chapterpackss.get(id);
	}

	/**
	 * 获取所有数据
	 * @return
	 */
	public Collection<ChapterPacksConfig> list() {
		return this.chapterpackss.values();
	}
	@Override
	public void load() {
		try {
			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, ChapterPacksConfig> chapterpackss = new HashMap<>();
			for (Element e : list) {
				ChapterPacksConfig chapterpacks = new ChapterPacksConfig(e);
				ChapterPacksConfig old = chapterpackss.put(chapterpacks.ID, chapterpacks);
				if (old != null) {
					throw new IllegalArgumentException("[ChapterPacksConfig]表存在重复的数据id： " + old.ID);
				}
			}			

			this.chapterpackss = com.google.common.collect.ImmutableMap.copyOf(chapterpackss);

			log.info("load ChapterPacksConfig size[{}]", chapterpackss.size());

		} catch (Exception e) {
			throw new RuntimeException("load ChapterPacksConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
