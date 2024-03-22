package cn.game.protocol.generated.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cn.game.protocol.generated.config.MoneyConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class MoneyManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(MoneyManager.class);

	private static MoneyManager instance = new MoneyManager();
	private static final String xmlFileName = "Money";
	
	private Map<Integer, MoneyConfig> moneys = new HashMap<>();

	public static MoneyManager getInstance() {
		return instance;
	}

	private MoneyManager() {
		WatchServiceManager.getInstance().register(this);
	}
	/**
	 * 根据id获取数据，一般用这个方法，如果数据不存在，一般是配置错误，直接抛出异常
	 * 
	 * @param id
	 * @return
	 */
	public MoneyConfig get(int id) {
		MoneyConfig config = this.moneys.get(id);
		if (config == null) { 
			throw new NullPointerException("【Money】表的" + "id【" + id + "】不存在"); 
		}
		return config;
	}
	/**
	 * 根据id获取数据，允许返回null
	 * 
	 * @param id
	 * @return
	 */
	public MoneyConfig getNullable(int id) {
		return this.moneys.get(id);
	}

	public Collection<MoneyConfig> list() {
		return this.moneys.values();
	}

	@Override
	public void load() {

		try {
			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, MoneyConfig> moneys = new HashMap<>();
			for (Element e : list) {
				MoneyConfig money = new MoneyConfig(e);
				moneys.put(money.getID(), money);
			}			

			this.moneys = com.google.common.collect.ImmutableMap.copyOf(moneys);

			log.info("load MoneyConfig size[{}]", moneys.size());

		} catch (Exception e) {
			throw new RuntimeException("load MoneyConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
