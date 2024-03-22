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

import cn.game.protocol.generated.config.BattleLevelConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class BattleLevelManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(BattleLevelManager.class);

	private static BattleLevelManager instance = new BattleLevelManager();
	private static final String xmlFileName = "BattleLevel";
	
	private Map<Integer, BattleLevelConfig> battlelevels = new HashMap<>();
	private Map<Integer,List<BattleLevelConfig>> battleChapterIds = new HashMap<>();

	public static BattleLevelManager getInstance() {
		return instance;
	}

	private BattleLevelManager() {
		WatchServiceManager.getInstance().register(this);
	}
	/**
	 * 根据id获取数据，一般用这个方法，如果数据不存在，一般是配置错误，直接抛出异常
	 * 
	 * @param id
	 * @return
	 */
	public BattleLevelConfig getBattleLevelConfig(int id) {
		BattleLevelConfig config = this.battlelevels.get(id);
		if (config == null) { 
			throw new NullPointerException("【BattleLevel】表的" + "id【" + id + "】不存在"); 
		}
		return config;
	}
	/**
	 * 根据id获取数据，允许返回null
	 * 
	 * @param id
	 * @return
	 */
	public BattleLevelConfig getBattleLevelConfigNullable(int id) {
		return this.battlelevels.get(id);
	}

	public List<BattleLevelConfig> getBattleChapterIdList(int battleChapterId) {
		return this.battleChapterIds.get(battleChapterId);
	}
	public Collection<BattleLevelConfig> list() {
		return this.battlelevels.values();
	}

	@Override
	public void load() {

		try {
			ClassLoader classLoader = Thread.currentThread().getClass().getClassLoader();
			if (classLoader == null) {
				classLoader = BattleLevelManager.class.getClassLoader();
			}
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, BattleLevelConfig> battlelevels = new HashMap<>();
			Map<Integer, List<BattleLevelConfig>> battleChapterIds = new HashMap<>();
			for (Element e : list) {
				BattleLevelConfig battlelevel = new BattleLevelConfig(e);
				List<BattleLevelConfig> battleChapterIdList = battleChapterIds.get(battlelevel.getBattleChapterId()); 
				if (battleChapterIdList == null){
					battleChapterIdList = new ArrayList<BattleLevelConfig>(2) ; 
					battleChapterIds.put(battlelevel.getBattleChapterId() ,battleChapterIdList) ; 
				}
				battleChapterIdList.add(battlelevel) ;
				battlelevels.put(battlelevel.getId(), battlelevel);
			}			

			this.battleChapterIds = com.google.common.collect.ImmutableMap.copyOf(battleChapterIds);			
			this.battlelevels = com.google.common.collect.ImmutableMap.copyOf(battlelevels);

			log.info("load BattleLevelConfig size[{}]", battlelevels.size());

		} catch (Exception e) {
			throw new RuntimeException("load BattleLevelConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
