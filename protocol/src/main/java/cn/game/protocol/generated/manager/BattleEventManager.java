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

import cn.game.protocol.generated.config.BattleEventConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class BattleEventManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(BattleEventManager.class);

	private static BattleEventManager instance = new BattleEventManager();
	private static final String xmlFileName = "BattleEvent";
	
	private Map<Integer, BattleEventConfig> battleevents = new HashMap<>();
	private Map<Integer,List<BattleEventConfig>> rankIntervals = new HashMap<>();

	public static BattleEventManager getInstance() {
		return instance;
	}

	private BattleEventManager() {
		WatchServiceManager.getInstance().register(this);
	}
	/**
	 * 根据id获取数据，一般用这个方法，如果数据不存在，一般是配置错误，直接抛出异常
	 * 
	 * @param id
	 * @return
	 */
	public BattleEventConfig getBattleEventConfig(int id) {
		BattleEventConfig config = this.battleevents.get(id);
		if (config == null) { 
			throw new NullPointerException("【BattleEvent】表的" + "id【" + id + "】不存在"); 
		}
		return config;
	}
	/**
	 * 根据id获取数据，允许返回null
	 * 
	 * @param id
	 * @return
	 */
	public BattleEventConfig getBattleEventConfigNullable(int id) {
		return this.battleevents.get(id);
	}

	public List<BattleEventConfig> getRankIntervalList(int rankInterval) {
		return this.rankIntervals.get(rankInterval);
	}
	public Collection<BattleEventConfig> list() {
		return this.battleevents.values();
	}

	@Override
	public void load() {

		try {
			ClassLoader classLoader = Thread.currentThread().getClass().getClassLoader();
			if (classLoader == null) {
				classLoader = BattleEventManager.class.getClassLoader();
			}
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, BattleEventConfig> battleevents = new HashMap<>();
			Map<Integer, List<BattleEventConfig>> rankIntervals = new HashMap<>();
			for (Element e : list) {
				BattleEventConfig battleevent = new BattleEventConfig(e);
				List<BattleEventConfig> rankIntervalList = rankIntervals.get(battleevent.getRankInterval()); 
				if (rankIntervalList == null){
					rankIntervalList = new ArrayList<BattleEventConfig>(2) ; 
					rankIntervals.put(battleevent.getRankInterval() ,rankIntervalList) ; 
				}
				rankIntervalList.add(battleevent) ;
				battleevents.put(battleevent.getId(), battleevent);
			}			

			this.rankIntervals = com.google.common.collect.ImmutableMap.copyOf(rankIntervals);			
			this.battleevents = com.google.common.collect.ImmutableMap.copyOf(battleevents);

			log.info("load BattleEventConfig size[{}]", battleevents.size());

		} catch (Exception e) {
			throw new RuntimeException("load BattleEventConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
