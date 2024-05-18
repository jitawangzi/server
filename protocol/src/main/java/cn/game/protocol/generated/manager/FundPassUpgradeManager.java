package cn.game.protocol.generated.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cn.game.protocol.generated.config.FundPassUpgradeConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class FundPassUpgradeManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(FundPassUpgradeManager.class);

	private static FundPassUpgradeManager instance = new FundPassUpgradeManager();
	private static final String xmlFileName = "FundPassUpgrade";
	
	/** 总数据，按id取值 */
	private Map<Integer, FundPassUpgradeConfig> fundpassupgrades = new HashMap<>();

	public static FundPassUpgradeManager instance() {
		return instance;
	}
	private FundPassUpgradeManager() {
		WatchServiceManager.getInstance().register(this);
	}
	/**
	 * 根据id获取数据，一般用这个方法，如果数据不存在，一般是配置错误，直接抛出异常
	 * 
	 * @param id
	 * @return
	 */
	public FundPassUpgradeConfig get(int id) {
		FundPassUpgradeConfig config = this.fundpassupgrades.get(id);
		if (config == null) { 
			throw new NullPointerException("【FundPassUpgrade】表的" + "id【" + id + "】不存在"); 
		}
		return config;
	}
	/**
	 * 根据id获取数据，允许返回null
	 * 
	 * @param id
	 * @return
	 */
	public FundPassUpgradeConfig getNullable(int id) {
		return this.fundpassupgrades.get(id);
	}

	/**
	 * 获取所有数据
	 * @return
	 */
	public Collection<FundPassUpgradeConfig> list() {
		return this.fundpassupgrades.values();
	}
	@Override
	public void load() {
		try {
			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, FundPassUpgradeConfig> fundpassupgrades = new HashMap<>();
			for (Element e : list) {
				FundPassUpgradeConfig fundpassupgrade = new FundPassUpgradeConfig(e);
				FundPassUpgradeConfig old = fundpassupgrades.put(fundpassupgrade.ID, fundpassupgrade);
				if (old != null) {
					throw new IllegalArgumentException("[FundPassUpgradeConfig]表存在重复的数据id： " + old.ID);
				}
			}			

			this.fundpassupgrades = com.google.common.collect.ImmutableMap.copyOf(fundpassupgrades);

			log.info("load FundPassUpgradeConfig size[{}]", fundpassupgrades.size());

		} catch (Exception e) {
			throw new RuntimeException("load FundPassUpgradeConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
