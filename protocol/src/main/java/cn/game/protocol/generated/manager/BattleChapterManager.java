package cn.game.protocol.generated.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cn.game.protocol.generated.config.BattleChapterConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class BattleChapterManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(BattleChapterManager.class);

	private static BattleChapterManager instance = new BattleChapterManager();
	private static final String xmlFileName = "BattleChapter";
	
	private Map<Integer, BattleChapterConfig> battlechapters = new HashMap<>();

	public static BattleChapterManager getInstance() {
		return instance;
	}

	private BattleChapterManager() {
		WatchServiceManager.getInstance().register(this);
	}
	/**
	 * 根据id获取数据，一般用这个方法，如果数据不存在，一般是配置错误，直接抛出异常
	 * 
	 * @param id
	 * @return
	 */
	public BattleChapterConfig getBattleChapterConfig(int id) {
		BattleChapterConfig config = this.battlechapters.get(id);
		if (config == null) { 
			throw new NullPointerException("【BattleChapter】表的" + "id【" + id + "】不存在"); 
		}
		return config;
	}
	/**
	 * 根据id获取数据，允许返回null
	 * 
	 * @param id
	 * @return
	 */
	public BattleChapterConfig getBattleChapterConfigNullable(int id) {
		return this.battlechapters.get(id);
	}

	public Collection<BattleChapterConfig> list() {
		return this.battlechapters.values();
	}

	@Override
	public void load() {

		try {
			ClassLoader classLoader = Thread.currentThread().getClass().getClassLoader();
			if (classLoader == null) {
				classLoader = BattleChapterManager.class.getClassLoader();
			}
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, BattleChapterConfig> battlechapters = new HashMap<>();
			for (Element e : list) {
				BattleChapterConfig battlechapter = new BattleChapterConfig(e);
				battlechapters.put(battlechapter.getId(), battlechapter);
			}			

			this.battlechapters = com.google.common.collect.ImmutableMap.copyOf(battlechapters);

			log.info("load BattleChapterConfig size[{}]", battlechapters.size());

		} catch (Exception e) {
			throw new RuntimeException("load BattleChapterConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
