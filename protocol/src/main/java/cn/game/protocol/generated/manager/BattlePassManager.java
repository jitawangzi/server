package cn.game.protocol.generated.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cn.game.protocol.generated.config.BattlePassConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class BattlePassManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(BattlePassManager.class);

	private static BattlePassManager instance = new BattlePassManager();
	private static final String xmlFileName = "BattlePass";
	
	private Map<Integer, BattlePassConfig> battlepasss = new HashMap<>();

	public static BattlePassManager getInstance() {
		return instance;
	}

	private BattlePassManager() {
		WatchServiceManager.getInstance().register(this);
	}
	/**
	 * 根据id获取数据，一般用这个方法，如果数据不存在，一般是配置错误，直接抛出异常
	 * 
	 * @param id
	 * @return
	 */
	public BattlePassConfig getBattlePassConfig(int id) {
		BattlePassConfig config = this.battlepasss.get(id);
		if (config == null) { 
			throw new NullPointerException("【BattlePass】表的" + "id【" + id + "】不存在"); 
		}
		return config;
	}
	/**
	 * 根据id获取数据，允许返回null
	 * 
	 * @param id
	 * @return
	 */
	public BattlePassConfig getBattlePassConfigNullable(int id) {
		return this.battlepasss.get(id);
	}

	public Collection<BattlePassConfig> list() {
		return this.battlepasss.values();
	}

	@Override
	public void load() {

		try {
			ClassLoader classLoader = Thread.currentThread().getClass().getClassLoader();
			if (classLoader == null) {
				classLoader = BattlePassManager.class.getClassLoader();
			}
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, BattlePassConfig> battlepasss = new HashMap<>();
			for (Element e : list) {
				BattlePassConfig battlepass = new BattlePassConfig(e);
				battlepasss.put(battlepass.getId(), battlepass);
			}			

			this.battlepasss = com.google.common.collect.ImmutableMap.copyOf(battlepasss);

			log.info("load BattlePassConfig size[{}]", battlepasss.size());

		} catch (Exception e) {
			throw new RuntimeException("load BattlePassConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
