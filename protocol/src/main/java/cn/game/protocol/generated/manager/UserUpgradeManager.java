package cn.game.protocol.generated.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cn.game.protocol.generated.config.UserUpgradeConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class UserUpgradeManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(UserUpgradeManager.class);

	private static UserUpgradeManager instance = new UserUpgradeManager();
	private static final String xmlFileName = "UserUpgrade";
	
	/** 总数据，按id取值 */
	private Map<Integer, UserUpgradeConfig> userupgrades = new HashMap<>();

	public static UserUpgradeManager instance() {
		return instance;
	}
	private UserUpgradeManager() {
		WatchServiceManager.getInstance().register(this);
	}
	/**
	 * 根据id获取数据，一般用这个方法，如果数据不存在，一般是配置错误，直接抛出异常
	 * 
	 * @param id
	 * @return
	 */
	public UserUpgradeConfig get(int id) {
		UserUpgradeConfig config = this.userupgrades.get(id);
		if (config == null) { 
			throw new NullPointerException("【UserUpgrade】表的" + "id【" + id + "】不存在"); 
		}
		return config;
	}
	/**
	 * 根据id获取数据，允许返回null
	 * 
	 * @param id
	 * @return
	 */
	public UserUpgradeConfig getNullable(int id) {
		return this.userupgrades.get(id);
	}

	/**
	 * 获取所有数据
	 * @return
	 */
	public Collection<UserUpgradeConfig> list() {
		return this.userupgrades.values();
	}
	@Override
	public void load() {
		try {
			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, UserUpgradeConfig> userupgrades = new HashMap<>();
			for (Element e : list) {
				UserUpgradeConfig userupgrade = new UserUpgradeConfig(e);
				UserUpgradeConfig old = userupgrades.put(userupgrade.ID, userupgrade);
				if (old != null) {
					throw new IllegalArgumentException("[UserUpgradeConfig]表存在重复的数据id： " + old.ID);
				}
			}			

			this.userupgrades = com.google.common.collect.ImmutableMap.copyOf(userupgrades);

			log.info("load UserUpgradeConfig size[{}]", userupgrades.size());

		} catch (Exception e) {
			throw new RuntimeException("load UserUpgradeConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
