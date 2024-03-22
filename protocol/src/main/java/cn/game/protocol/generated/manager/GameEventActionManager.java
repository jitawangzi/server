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

import cn.game.protocol.generated.config.GameEventActionConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class GameEventActionManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(GameEventActionManager.class);

	private static GameEventActionManager instance = new GameEventActionManager();
	private static final String xmlFileName = "GameEventAction";
	
	private Map<Integer, GameEventActionConfig> gameeventactions = new HashMap<>();
	private Map<Integer,List<GameEventActionConfig>> eventIds = new HashMap<>();

	public static GameEventActionManager getInstance() {
		return instance;
	}

	private GameEventActionManager() {
		WatchServiceManager.getInstance().register(this);
	}
	/**
	 * 根据id获取数据，一般用这个方法，如果数据不存在，一般是配置错误，直接抛出异常
	 * 
	 * @param id
	 * @return
	 */
	public GameEventActionConfig getGameEventActionConfig(int id) {
		GameEventActionConfig config = this.gameeventactions.get(id);
		if (config == null) { 
			throw new NullPointerException("【GameEventAction】表的" + "id【" + id + "】不存在"); 
		}
		return config;
	}
	/**
	 * 根据id获取数据，允许返回null
	 * 
	 * @param id
	 * @return
	 */
	public GameEventActionConfig getGameEventActionConfigNullable(int id) {
		return this.gameeventactions.get(id);
	}

	public List<GameEventActionConfig> getEventIdList(int eventId) {
		return this.eventIds.get(eventId);
	}
	public Collection<GameEventActionConfig> list() {
		return this.gameeventactions.values();
	}

	@Override
	public void load() {

		try {
			ClassLoader classLoader = Thread.currentThread().getClass().getClassLoader();
			if (classLoader == null) {
				classLoader = GameEventActionManager.class.getClassLoader();
			}
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, GameEventActionConfig> gameeventactions = new HashMap<>();
			Map<Integer, List<GameEventActionConfig>> eventIds = new HashMap<>();
			for (Element e : list) {
				GameEventActionConfig gameeventaction = new GameEventActionConfig(e);
				List<GameEventActionConfig> eventIdList = eventIds.get(gameeventaction.getEventId()); 
				if (eventIdList == null){
					eventIdList = new ArrayList<GameEventActionConfig>(2) ; 
					eventIds.put(gameeventaction.getEventId() ,eventIdList) ; 
				}
				eventIdList.add(gameeventaction) ;
				gameeventactions.put(gameeventaction.getId(), gameeventaction);
			}			

			this.eventIds = com.google.common.collect.ImmutableMap.copyOf(eventIds);			
			this.gameeventactions = com.google.common.collect.ImmutableMap.copyOf(gameeventactions);

			log.info("load GameEventActionConfig size[{}]", gameeventactions.size());

		} catch (Exception e) {
			throw new RuntimeException("load GameEventActionConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
