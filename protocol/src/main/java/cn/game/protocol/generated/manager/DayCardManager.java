package cn.game.protocol.generated.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cn.game.protocol.generated.config.DayCardConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class DayCardManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(DayCardManager.class);

	private static DayCardManager instance = new DayCardManager();
	private static final String xmlFileName = "DayCard";
	
	/** 总数据，按id取值 */
	private Map<Integer, DayCardConfig> daycards = new HashMap<>();

	public static DayCardManager instance() {
		return instance;
	}
	private DayCardManager() {
		WatchServiceManager.getInstance().register(this);
	}
	/**
	 * 根据id获取数据，一般用这个方法，如果数据不存在，一般是配置错误，直接抛出异常
	 * 
	 * @param id
	 * @return
	 */
	public DayCardConfig get(int id) {
		DayCardConfig config = this.daycards.get(id);
		if (config == null) { 
			throw new NullPointerException("【DayCard】表的" + "id【" + id + "】不存在"); 
		}
		return config;
	}
	/**
	 * 根据id获取数据，允许返回null
	 * 
	 * @param id
	 * @return
	 */
	public DayCardConfig getNullable(int id) {
		return this.daycards.get(id);
	}

	/**
	 * 获取所有数据
	 * @return
	 */
	public Collection<DayCardConfig> list() {
		return this.daycards.values();
	}
	@Override
	public void load() {
		try {
			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, DayCardConfig> daycards = new HashMap<>();
			for (Element e : list) {
				DayCardConfig daycard = new DayCardConfig(e);
				DayCardConfig old = daycards.put(daycard.ID, daycard);
				if (old != null) {
					throw new IllegalArgumentException("[DayCardConfig]表存在重复的数据id： " + old.ID);
				}
			}			

			this.daycards = com.google.common.collect.ImmutableMap.copyOf(daycards);

			log.info("load DayCardConfig size[{}]", daycards.size());

		} catch (Exception e) {
			throw new RuntimeException("load DayCardConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
