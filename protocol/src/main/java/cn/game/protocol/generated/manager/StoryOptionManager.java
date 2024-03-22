package cn.game.protocol.generated.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cn.game.protocol.generated.config.StoryOptionConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class StoryOptionManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(StoryOptionManager.class);

	private static StoryOptionManager instance = new StoryOptionManager();
	private static final String xmlFileName = "StoryOption";
	
	private Map<Integer, StoryOptionConfig> storyoptions = new HashMap<>();

	public static StoryOptionManager getInstance() {
		return instance;
	}

	private StoryOptionManager() {
		WatchServiceManager.getInstance().register(this);
	}
	/**
	 * 根据id获取数据，一般用这个方法，如果数据不存在，一般是配置错误，直接抛出异常
	 * 
	 * @param id
	 * @return
	 */
	public StoryOptionConfig getStoryOptionConfig(int id) {
		StoryOptionConfig config = this.storyoptions.get(id);
		if (config == null) { 
			throw new NullPointerException("【StoryOption】表的" + "id【" + id + "】不存在"); 
		}
		return config;
	}
	/**
	 * 根据id获取数据，允许返回null
	 * 
	 * @param id
	 * @return
	 */
	public StoryOptionConfig getStoryOptionConfigNullable(int id) {
		return this.storyoptions.get(id);
	}

	public Collection<StoryOptionConfig> list() {
		return this.storyoptions.values();
	}

	@Override
	public void load() {

		try {
			ClassLoader classLoader = Thread.currentThread().getClass().getClassLoader();
			if (classLoader == null) {
				classLoader = StoryOptionManager.class.getClassLoader();
			}
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, StoryOptionConfig> storyoptions = new HashMap<>();
			for (Element e : list) {
				StoryOptionConfig storyoption = new StoryOptionConfig(e);
				storyoptions.put(storyoption.getId(), storyoption);
			}			

			this.storyoptions = com.google.common.collect.ImmutableMap.copyOf(storyoptions);

			log.info("load StoryOptionConfig size[{}]", storyoptions.size());

		} catch (Exception e) {
			throw new RuntimeException("load StoryOptionConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
