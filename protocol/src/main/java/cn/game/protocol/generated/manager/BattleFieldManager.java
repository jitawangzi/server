package cn.game.protocol.generated.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cn.game.protocol.generated.config.BattleFieldConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class BattleFieldManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(BattleFieldManager.class);

	private static BattleFieldManager instance = new BattleFieldManager();
	private static final String xmlFileName = "BattleField";
	
	/** 总数据，按id取值 */
	private Map<Integer, BattleFieldConfig> battlefields = new HashMap<>();

	public static BattleFieldManager instance() {
		return instance;
	}
	private BattleFieldManager() {
		WatchServiceManager.getInstance().register(this);
	}
	/**
	 * 根据id获取数据，一般用这个方法，如果数据不存在，一般是配置错误，直接抛出异常
	 * 
	 * @param id
	 * @return
	 */
	public BattleFieldConfig get(int id) {
		BattleFieldConfig config = this.battlefields.get(id);
		if (config == null) { 
			throw new NullPointerException("【BattleField】表的" + "id【" + id + "】不存在"); 
		}
		return config;
	}
	/**
	 * 根据id获取数据，允许返回null
	 * 
	 * @param id
	 * @return
	 */
	public BattleFieldConfig getNullable(int id) {
		return this.battlefields.get(id);
	}

	/**
	 * 获取所有数据
	 * @return
	 */
	public Collection<BattleFieldConfig> list() {
		return this.battlefields.values();
	}
	@Override
	public void load() {
		try {
			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, BattleFieldConfig> battlefields = new HashMap<>();
			for (Element e : list) {
				BattleFieldConfig battlefield = new BattleFieldConfig(e);
				BattleFieldConfig old = battlefields.put(battlefield.ID, battlefield);
				if (old != null) {
					throw new IllegalArgumentException("[BattleFieldConfig]表存在重复的数据id： " + old.ID);
				}
			}			

			this.battlefields = com.google.common.collect.ImmutableMap.copyOf(battlefields);

			log.info("load BattleFieldConfig size[{}]", battlefields.size());

		} catch (Exception e) {
			throw new RuntimeException("load BattleFieldConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
