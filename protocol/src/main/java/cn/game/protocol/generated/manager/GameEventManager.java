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

import cn.game.protocol.generated.config.GameEventConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class GameEventManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(GameEventManager.class);

	private static GameEventManager instance = new GameEventManager();
	private static final String xmlFileName = "GameEvent";
	
	private Map<Integer, GameEventConfig> gameevents = new HashMap<>();
	private Map<Integer,List<GameEventConfig>> eventTypes = new HashMap<>();

	public static GameEventManager getInstance() {
		return instance;
	}

	private GameEventManager() {
		WatchServiceManager.getInstance().register(this);
	}
	/**
	 * 根据id获取数据，一般用这个方法，如果数据不存在，一般是配置错误，直接抛出异常
	 * 
	 * @param id
	 * @return
	 */
	public GameEventConfig getGameEventConfig(int id) {
		GameEventConfig config = this.gameevents.get(id);
		if (config == null) { 
			throw new NullPointerException("【GameEvent】表的" + "id【" + id + "】不存在"); 
		}
		return config;
	}
	/**
	 * 根据id获取数据，允许返回null
	 * 
	 * @param id
	 * @return
	 */
	public GameEventConfig getGameEventConfigNullable(int id) {
		return this.gameevents.get(id);
	}

	public List<GameEventConfig> getEventTypeList(int eventType) {
		return this.eventTypes.get(eventType);
	}
	public Collection<GameEventConfig> list() {
		return this.gameevents.values();
	}

	@Override
	public void load() {

		try {
			ClassLoader classLoader = Thread.currentThread().getClass().getClassLoader();
			if (classLoader == null) {
				classLoader = GameEventManager.class.getClassLoader();
			}
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, GameEventConfig> gameevents = new HashMap<>();
			Map<Integer, List<GameEventConfig>> eventTypes = new HashMap<>();
			for (Element e : list) {
				GameEventConfig gameevent = new GameEventConfig(e);
				List<GameEventConfig> eventTypeList = eventTypes.get(gameevent.getEventType()); 
				if (eventTypeList == null){
					eventTypeList = new ArrayList<GameEventConfig>(2) ; 
					eventTypes.put(gameevent.getEventType() ,eventTypeList) ; 
				}
				eventTypeList.add(gameevent) ;
				gameevents.put(gameevent.getId(), gameevent);
			}			

			this.eventTypes = com.google.common.collect.ImmutableMap.copyOf(eventTypes);			
			this.gameevents = com.google.common.collect.ImmutableMap.copyOf(gameevents);

			log.info("load GameEventConfig size[{}]", gameevents.size());

		} catch (Exception e) {
			throw new RuntimeException("load GameEventConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
