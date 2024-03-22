package cn.game.protocol.generated.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cn.game.protocol.generated.config.StoryTriggerTypeEnumConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class StoryTriggerTypeEnumManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(StoryTriggerTypeEnumManager.class);

	private static StoryTriggerTypeEnumManager instance = new StoryTriggerTypeEnumManager();
	private static final String xmlFileName = "StoryTriggerTypeEnum";
	
	private Map<Integer, StoryTriggerTypeEnumConfig> storytriggertypeenums = new HashMap<>();

	public static StoryTriggerTypeEnumManager getInstance() {
		return instance;
	}

	private StoryTriggerTypeEnumManager() {
		WatchServiceManager.getInstance().register(this);
	}
	/**
	 * 根据id获取数据，一般用这个方法，如果数据不存在，一般是配置错误，直接抛出异常
	 * 
	 * @param id
	 * @return
	 */
	public StoryTriggerTypeEnumConfig getStoryTriggerTypeEnumConfig(int id) {
		StoryTriggerTypeEnumConfig config = this.storytriggertypeenums.get(id);
		if (config == null) { 
			throw new NullPointerException("【StoryTriggerTypeEnum】表的" + "id【" + id + "】不存在"); 
		}
		return config;
	}
	/**
	 * 根据id获取数据，允许返回null
	 * 
	 * @param id
	 * @return
	 */
	public StoryTriggerTypeEnumConfig getStoryTriggerTypeEnumConfigNullable(int id) {
		return this.storytriggertypeenums.get(id);
	}

	public Collection<StoryTriggerTypeEnumConfig> list() {
		return this.storytriggertypeenums.values();
	}

	@Override
	public void load() {

		try {
			ClassLoader classLoader = Thread.currentThread().getClass().getClassLoader();
			if (classLoader == null) {
				classLoader = StoryTriggerTypeEnumManager.class.getClassLoader();
			}
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, StoryTriggerTypeEnumConfig> storytriggertypeenums = new HashMap<>();
			for (Element e : list) {
				StoryTriggerTypeEnumConfig storytriggertypeenum = new StoryTriggerTypeEnumConfig(e);
				storytriggertypeenums.put(storytriggertypeenum.getId(), storytriggertypeenum);
			}			

			this.storytriggertypeenums = com.google.common.collect.ImmutableMap.copyOf(storytriggertypeenums);

			log.info("load StoryTriggerTypeEnumConfig size[{}]", storytriggertypeenums.size());

		} catch (Exception e) {
			throw new RuntimeException("load StoryTriggerTypeEnumConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
