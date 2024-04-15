package cn.game.protocol.generated.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cn.game.protocol.generated.config.BattleConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class BattleManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(BattleManager.class);

	private static BattleManager instance = new BattleManager();
	private static final String xmlFileName = "Battle";
	
	/** 总数据，按id取值 */
	private Map<Integer, BattleConfig> battles = new HashMap<>();

	public static BattleManager instance() {
		return instance;
	}
	private BattleManager() {
		WatchServiceManager.getInstance().register(this);
	}
	/**
	 * 根据id获取数据，一般用这个方法，如果数据不存在，一般是配置错误，直接抛出异常
	 * 
	 * @param id
	 * @return
	 */
	public BattleConfig get(int id) {
		BattleConfig config = this.battles.get(id);
		if (config == null) { 
			throw new NullPointerException("【Battle】表的" + "id【" + id + "】不存在"); 
		}
		return config;
	}
	/**
	 * 根据id获取数据，允许返回null
	 * 
	 * @param id
	 * @return
	 */
	public BattleConfig getNullable(int id) {
		return this.battles.get(id);
	}

	/**
	 * 获取所有数据
	 * @return
	 */
	public Collection<BattleConfig> list() {
		return this.battles.values();
	}
	@Override
	public void load() {
		try {
			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, BattleConfig> battles = new HashMap<>();
			for (Element e : list) {
				BattleConfig battle = new BattleConfig(e);
				BattleConfig old = battles.put(battle.ID, battle);
				if (old != null) {
					throw new IllegalArgumentException("[BattleConfig]表存在重复的数据id： " + old.ID);
				}
			}			

			this.battles = com.google.common.collect.ImmutableMap.copyOf(battles);

			log.info("load BattleConfig size[{}]", battles.size());

		} catch (Exception e) {
			throw new RuntimeException("load BattleConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
