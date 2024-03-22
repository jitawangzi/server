package cn.game.protocol.generated.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cn.game.protocol.generated.config.StoryConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class StoryManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(StoryManager.class);

	private static StoryManager instance = new StoryManager();
	private static final String xmlFileName = "Story";
	
	private Map<Integer, StoryConfig> storys = new HashMap<>();

	public static StoryManager getInstance() {
		return instance;
	}

	private StoryManager() {
		WatchServiceManager.getInstance().register(this);
	}
	/**
	 * 根据id获取数据，一般用这个方法，如果数据不存在，一般是配置错误，直接抛出异常
	 * 
	 * @param id
	 * @return
	 */
	public StoryConfig getStoryConfig(int id) {
		StoryConfig config = this.storys.get(id);
		if (config == null) { 
			throw new NullPointerException("【Story】表的" + "id【" + id + "】不存在"); 
		}
		return config;
	}
	/**
	 * 根据id获取数据，允许返回null
	 * 
	 * @param id
	 * @return
	 */
	public StoryConfig getStoryConfigNullable(int id) {
		return this.storys.get(id);
	}

	public Collection<StoryConfig> list() {
		return this.storys.values();
	}

	@Override
	public void load() {

		try {
			ClassLoader classLoader = Thread.currentThread().getClass().getClassLoader();
			if (classLoader == null) {
				classLoader = StoryManager.class.getClassLoader();
			}
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, StoryConfig> storys = new HashMap<>();
			for (Element e : list) {
				StoryConfig story = new StoryConfig(e);
				storys.put(story.getId(), story);
			}			

			this.storys = com.google.common.collect.ImmutableMap.copyOf(storys);

			log.info("load StoryConfig size[{}]", storys.size());

		} catch (Exception e) {
			throw new RuntimeException("load StoryConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
