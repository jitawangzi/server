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

import cn.game.protocol.generated.config.EventConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class EventManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(EventManager.class);

	private static EventManager instance = new EventManager();
	private static final String xmlFileName = "Event";
	
	private Map<Integer, EventConfig> events = new HashMap<>();
	private Map<Integer,List<EventConfig>> types = new HashMap<>();

	public static EventManager getInstance() {
		return instance;
	}

	private EventManager() {
		WatchServiceManager.getInstance().register(this);
	}
	/**
	 * 根据id获取数据，一般用这个方法，如果数据不存在，一般是配置错误，直接抛出异常
	 * 
	 * @param id
	 * @return
	 */
	public EventConfig getEventConfig(int id) {
		EventConfig config = this.events.get(id);
		if (config == null) { 
			throw new NullPointerException("【Event】表的" + "id【" + id + "】不存在"); 
		}
		return config;
	}
	/**
	 * 根据id获取数据，允许返回null
	 * 
	 * @param id
	 * @return
	 */
	public EventConfig getEventConfigNullable(int id) {
		return this.events.get(id);
	}

	public List<EventConfig> getTypeList(int type) {
		return this.types.get(type);
	}
	public Collection<EventConfig> list() {
		return this.events.values();
	}

	@Override
	public void load() {

		try {
			ClassLoader classLoader = Thread.currentThread().getClass().getClassLoader();
			if (classLoader == null) {
				classLoader = EventManager.class.getClassLoader();
			}
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, EventConfig> events = new HashMap<>();
			Map<Integer, List<EventConfig>> types = new HashMap<>();
			for (Element e : list) {
				EventConfig event = new EventConfig(e);
				List<EventConfig> typeList = types.get(event.getType()); 
				if (typeList == null){
					typeList = new ArrayList<EventConfig>(2) ; 
					types.put(event.getType() ,typeList) ; 
				}
				typeList.add(event) ;
				events.put(event.getId(), event);
			}			

			this.types = com.google.common.collect.ImmutableMap.copyOf(types);			
			this.events = com.google.common.collect.ImmutableMap.copyOf(events);

			log.info("load EventConfig size[{}]", events.size());

		} catch (Exception e) {
			throw new RuntimeException("load EventConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
