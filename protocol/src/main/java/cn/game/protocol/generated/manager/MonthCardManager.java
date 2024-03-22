package cn.game.protocol.generated.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cn.game.protocol.generated.config.MonthCardConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class MonthCardManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(MonthCardManager.class);

	private static MonthCardManager instance = new MonthCardManager();
	private static final String xmlFileName = "MonthCard";
	
	/** 总数据，按id取值 */
	private Map<Integer, MonthCardConfig> monthcards = new HashMap<>();

	public static MonthCardManager instance() {
		return instance;
	}
	private MonthCardManager() {
		WatchServiceManager.getInstance().register(this);
	}
	/**
	 * 根据id获取数据，一般用这个方法，如果数据不存在，一般是配置错误，直接抛出异常
	 * 
	 * @param id
	 * @return
	 */
	public MonthCardConfig get(int id) {
		MonthCardConfig config = this.monthcards.get(id);
		if (config == null) { 
			throw new NullPointerException("【MonthCard】表的" + "id【" + id + "】不存在"); 
		}
		return config;
	}
	/**
	 * 根据id获取数据，允许返回null
	 * 
	 * @param id
	 * @return
	 */
	public MonthCardConfig getNullable(int id) {
		return this.monthcards.get(id);
	}

	/**
	 * 获取所有数据
	 * @return
	 */
	public Collection<MonthCardConfig> list() {
		return this.monthcards.values();
	}
	@Override
	public void load() {
		try {
			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, MonthCardConfig> monthcards = new HashMap<>();
			for (Element e : list) {
				MonthCardConfig monthcard = new MonthCardConfig(e);
				MonthCardConfig old = monthcards.put(monthcard.ID, monthcard);
				if (old != null) {
					throw new IllegalArgumentException("[MonthCardConfig]表存在重复的数据id： " + old.ID);
				}
			}			

			this.monthcards = com.google.common.collect.ImmutableMap.copyOf(monthcards);

			log.info("load MonthCardConfig size[{}]", monthcards.size());

		} catch (Exception e) {
			throw new RuntimeException("load MonthCardConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
