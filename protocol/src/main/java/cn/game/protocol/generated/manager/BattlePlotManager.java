package cn.game.protocol.generated.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cn.game.protocol.generated.config.BattlePlotConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class BattlePlotManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(BattlePlotManager.class);

	private static BattlePlotManager instance = new BattlePlotManager();
	private static final String xmlFileName = "BattlePlot";
	
	private Map<Integer, BattlePlotConfig> battleplots = new HashMap<>();

	public static BattlePlotManager getInstance() {
		return instance;
	}

	private BattlePlotManager() {
		WatchServiceManager.getInstance().register(this);
	}
	/**
	 * 根据id获取数据，一般用这个方法，如果数据不存在，一般是配置错误，直接抛出异常
	 * 
	 * @param id
	 * @return
	 */
	public BattlePlotConfig getBattlePlotConfig(int id) {
		BattlePlotConfig config = this.battleplots.get(id);
		if (config == null) { 
			throw new NullPointerException("【BattlePlot】表的" + "id【" + id + "】不存在"); 
		}
		return config;
	}
	/**
	 * 根据id获取数据，允许返回null
	 * 
	 * @param id
	 * @return
	 */
	public BattlePlotConfig getBattlePlotConfigNullable(int id) {
		return this.battleplots.get(id);
	}

	public Collection<BattlePlotConfig> list() {
		return this.battleplots.values();
	}

	@Override
	public void load() {

		try {
			ClassLoader classLoader = Thread.currentThread().getClass().getClassLoader();
			if (classLoader == null) {
				classLoader = BattlePlotManager.class.getClassLoader();
			}
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, BattlePlotConfig> battleplots = new HashMap<>();
			for (Element e : list) {
				BattlePlotConfig battleplot = new BattlePlotConfig(e);
				battleplots.put(battleplot.getId(), battleplot);
			}			

			this.battleplots = com.google.common.collect.ImmutableMap.copyOf(battleplots);

			log.info("load BattlePlotConfig size[{}]", battleplots.size());

		} catch (Exception e) {
			throw new RuntimeException("load BattlePlotConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
