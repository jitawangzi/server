package cn.game.protocol.generated.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cn.game.protocol.generated.config.BattlePassPrizeConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class BattlePassPrizeManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(BattlePassPrizeManager.class);

	private static BattlePassPrizeManager instance = new BattlePassPrizeManager();
	private static final String xmlFileName = "BattlePassPrize";
	
	private Map<Integer, BattlePassPrizeConfig> battlepassprizes = new HashMap<>();

	public static BattlePassPrizeManager getInstance() {
		return instance;
	}

	private BattlePassPrizeManager() {
		WatchServiceManager.getInstance().register(this);
	}
	/**
	 * 根据id获取数据，一般用这个方法，如果数据不存在，一般是配置错误，直接抛出异常
	 * 
	 * @param id
	 * @return
	 */
	public BattlePassPrizeConfig getBattlePassPrizeConfig(int id) {
		BattlePassPrizeConfig config = this.battlepassprizes.get(id);
		if (config == null) { 
			throw new NullPointerException("【BattlePassPrize】表的" + "id【" + id + "】不存在"); 
		}
		return config;
	}
	/**
	 * 根据id获取数据，允许返回null
	 * 
	 * @param id
	 * @return
	 */
	public BattlePassPrizeConfig getBattlePassPrizeConfigNullable(int id) {
		return this.battlepassprizes.get(id);
	}

	public Collection<BattlePassPrizeConfig> list() {
		return this.battlepassprizes.values();
	}

	@Override
	public void load() {

		try {
			ClassLoader classLoader = Thread.currentThread().getClass().getClassLoader();
			if (classLoader == null) {
				classLoader = BattlePassPrizeManager.class.getClassLoader();
			}
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, BattlePassPrizeConfig> battlepassprizes = new HashMap<>();
			for (Element e : list) {
				BattlePassPrizeConfig battlepassprize = new BattlePassPrizeConfig(e);
				battlepassprizes.put(battlepassprize.getId(), battlepassprize);
			}			

			this.battlepassprizes = com.google.common.collect.ImmutableMap.copyOf(battlepassprizes);

			log.info("load BattlePassPrizeConfig size[{}]", battlepassprizes.size());

		} catch (Exception e) {
			throw new RuntimeException("load BattlePassPrizeConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
