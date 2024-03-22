package cn.game.protocol.generated.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cn.game.protocol.generated.config.EventTriggerConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class EventTriggerManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(EventTriggerManager.class);

	private static EventTriggerManager instance = new EventTriggerManager();
	private static final String xmlFileName = "EventTrigger";
	
	private Map<Integer, EventTriggerConfig> eventtriggers = new HashMap<>();

	public static EventTriggerManager getInstance() {
		return instance;
	}

	private EventTriggerManager() {
		WatchServiceManager.getInstance().register(this);
	}
	/**
	 * 根据id获取数据，一般用这个方法，如果数据不存在，一般是配置错误，直接抛出异常
	 * 
	 * @param id
	 * @return
	 */
	public EventTriggerConfig getEventTriggerConfig(int id) {
		EventTriggerConfig config = this.eventtriggers.get(id);
		if (config == null) { 
			throw new NullPointerException("【EventTrigger】表的" + "id【" + id + "】不存在"); 
		}
		return config;
	}
	/**
	 * 根据id获取数据，允许返回null
	 * 
	 * @param id
	 * @return
	 */
	public EventTriggerConfig getEventTriggerConfigNullable(int id) {
		return this.eventtriggers.get(id);
	}

	public Collection<EventTriggerConfig> list() {
		return this.eventtriggers.values();
	}

	@Override
	public void load() {

		try {
			ClassLoader classLoader = Thread.currentThread().getClass().getClassLoader();
			if (classLoader == null) {
				classLoader = EventTriggerManager.class.getClassLoader();
			}
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, EventTriggerConfig> eventtriggers = new HashMap<>();
			for (Element e : list) {
				EventTriggerConfig eventtrigger = new EventTriggerConfig(e);
				eventtriggers.put(eventtrigger.getId(), eventtrigger);
			}			

			this.eventtriggers = com.google.common.collect.ImmutableMap.copyOf(eventtriggers);

			log.info("load EventTriggerConfig size[{}]", eventtriggers.size());

		} catch (Exception e) {
			throw new RuntimeException("load EventTriggerConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
