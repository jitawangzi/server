package cn.game.protocol.generated.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cn.game.protocol.generated.config.MoneyRecoveryConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class MoneyRecoveryManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(MoneyRecoveryManager.class);

	private static MoneyRecoveryManager instance = new MoneyRecoveryManager();
	private static final String xmlFileName = "MoneyRecovery";
	
	/** 总数据，按id取值 */
	private Map<Integer, MoneyRecoveryConfig> moneyrecoverys = new HashMap<>();

	public static MoneyRecoveryManager instance() {
		return instance;
	}
	private MoneyRecoveryManager() {
		WatchServiceManager.getInstance().register(this);
	}
	/**
	 * 根据id获取数据，一般用这个方法，如果数据不存在，一般是配置错误，直接抛出异常
	 * 
	 * @param id
	 * @return
	 */
	public MoneyRecoveryConfig get(int id) {
		MoneyRecoveryConfig config = this.moneyrecoverys.get(id);
		if (config == null) { 
			throw new NullPointerException("【MoneyRecovery】表的" + "id【" + id + "】不存在"); 
		}
		return config;
	}
	/**
	 * 根据id获取数据，允许返回null
	 * 
	 * @param id
	 * @return
	 */
	public MoneyRecoveryConfig getNullable(int id) {
		return this.moneyrecoverys.get(id);
	}

	/**
	 * 获取所有数据
	 * @return
	 */
	public Collection<MoneyRecoveryConfig> list() {
		return this.moneyrecoverys.values();
	}
	@Override
	public void load() {
		try {
			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, MoneyRecoveryConfig> moneyrecoverys = new HashMap<>();
			for (Element e : list) {
				MoneyRecoveryConfig moneyrecovery = new MoneyRecoveryConfig(e);
				MoneyRecoveryConfig old = moneyrecoverys.put(moneyrecovery.ID, moneyrecovery);
				if (old != null) {
					throw new IllegalArgumentException("[MoneyRecoveryConfig]表存在重复的数据id： " + old.ID);
				}
			}			

			this.moneyrecoverys = com.google.common.collect.ImmutableMap.copyOf(moneyrecoverys);

			log.info("load MoneyRecoveryConfig size[{}]", moneyrecoverys.size());

		} catch (Exception e) {
			throw new RuntimeException("load MoneyRecoveryConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
