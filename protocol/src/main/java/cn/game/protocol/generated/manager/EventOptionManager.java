package cn.game.protocol.generated.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cn.game.protocol.generated.config.EventOptionConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class EventOptionManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(EventOptionManager.class);

	private static EventOptionManager instance = new EventOptionManager();
	private static final String xmlFileName = "EventOption";
	
	private Map<Integer, EventOptionConfig> eventoptions = new HashMap<>();
	private Map<Integer,List<EventOptionConfig>> eventIds = new HashMap<>();

	public static EventOptionManager getInstance() {
		return instance;
	}

	private EventOptionManager() {
		WatchServiceManager.getInstance().register(this);
	}
	/**
	 * 根据id获取数据，一般用这个方法，如果数据不存在，一般是配置错误，直接抛出异常
	 * 
	 * @param id
	 * @return
	 */
	public EventOptionConfig getEventOptionConfig(int id) {
		EventOptionConfig config = this.eventoptions.get(id);
		if (config == null) { 
			throw new NullPointerException("【EventOption】表的" + "id【" + id + "】不存在"); 
		}
		return config;
	}
	/**
	 * 根据id获取数据，允许返回null
	 * 
	 * @param id
	 * @return
	 */
	public EventOptionConfig getEventOptionConfigNullable(int id) {
		return this.eventoptions.get(id);
	}

	public List<EventOptionConfig> getEventIdList(int eventId) {
		return this.eventIds.get(eventId);
	}
	public Collection<EventOptionConfig> list() {
		return this.eventoptions.values();
	}

	@Override
	public void load() {

		try {
			ClassLoader classLoader = Thread.currentThread().getClass().getClassLoader();
			if (classLoader == null) {
				classLoader = EventOptionManager.class.getClassLoader();
			}
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, EventOptionConfig> eventoptions = new HashMap<>();
			Map<Integer, List<EventOptionConfig>> eventIds = new HashMap<>();
			for (Element e : list) {
				EventOptionConfig eventoption = new EventOptionConfig(e);
				List<EventOptionConfig> eventIdList = eventIds.get(eventoption.getEventId()); 
				if (eventIdList == null){
					eventIdList = new ArrayList<EventOptionConfig>(2) ; 
					eventIds.put(eventoption.getEventId() ,eventIdList) ; 
				}
				eventIdList.add(eventoption) ;
				eventoptions.put(eventoption.getId(), eventoption);
			}			

			this.eventIds = com.google.common.collect.ImmutableMap.copyOf(eventIds);			
			this.eventoptions = com.google.common.collect.ImmutableMap.copyOf(eventoptions);

			log.info("load EventOptionConfig size[{}]", eventoptions.size());

		} catch (Exception e) {
			throw new RuntimeException("load EventOptionConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
