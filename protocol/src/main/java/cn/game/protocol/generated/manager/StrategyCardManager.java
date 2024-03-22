package cn.game.protocol.generated.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cn.game.protocol.generated.config.StrategyCardConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class StrategyCardManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(StrategyCardManager.class);

	private static StrategyCardManager instance = new StrategyCardManager();
	private static final String xmlFileName = "StrategyCard";
	
	private Map<Integer, StrategyCardConfig> strategycards = new HashMap<>();

	public static StrategyCardManager getInstance() {
		return instance;
	}

	private StrategyCardManager() {
		WatchServiceManager.getInstance().register(this);
	}
	/**
	 * 根据id获取数据，一般用这个方法，如果数据不存在，一般是配置错误，直接抛出异常
	 * 
	 * @param id
	 * @return
	 */
	public StrategyCardConfig getStrategyCardConfig(int id) {
		StrategyCardConfig config = this.strategycards.get(id);
		if (config == null) { 
			throw new NullPointerException("【StrategyCard】表的" + "id【" + id + "】不存在"); 
		}
		return config;
	}
	/**
	 * 根据id获取数据，允许返回null
	 * 
	 * @param id
	 * @return
	 */
	public StrategyCardConfig getStrategyCardConfigNullable(int id) {
		return this.strategycards.get(id);
	}

	public Collection<StrategyCardConfig> list() {
		return this.strategycards.values();
	}

	@Override
	public void load() {

		try {
			ClassLoader classLoader = Thread.currentThread().getClass().getClassLoader();
			if (classLoader == null) {
				classLoader = StrategyCardManager.class.getClassLoader();
			}
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, StrategyCardConfig> strategycards = new HashMap<>();
			for (Element e : list) {
				StrategyCardConfig strategycard = new StrategyCardConfig(e);
				strategycards.put(strategycard.getId(), strategycard);
			}			

			this.strategycards = com.google.common.collect.ImmutableMap.copyOf(strategycards);

			log.info("load StrategyCardConfig size[{}]", strategycards.size());

		} catch (Exception e) {
			throw new RuntimeException("load StrategyCardConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
