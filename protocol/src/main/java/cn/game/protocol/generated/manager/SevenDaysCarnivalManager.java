package cn.game.protocol.generated.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cn.game.protocol.generated.config.SevenDaysCarnivalConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class SevenDaysCarnivalManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(SevenDaysCarnivalManager.class);

	private static SevenDaysCarnivalManager instance = new SevenDaysCarnivalManager();
	private static final String xmlFileName = "SevenDaysCarnival";
	
	/** 总数据，按id取值 */
	private Map<Integer, SevenDaysCarnivalConfig> sevendayscarnivals = new HashMap<>();

	public static SevenDaysCarnivalManager instance() {
		return instance;
	}
	private SevenDaysCarnivalManager() {
		WatchServiceManager.getInstance().register(this);
	}
	/**
	 * 根据id获取数据，一般用这个方法，如果数据不存在，一般是配置错误，直接抛出异常
	 * 
	 * @param id
	 * @return
	 */
	public SevenDaysCarnivalConfig get(int id) {
		SevenDaysCarnivalConfig config = this.sevendayscarnivals.get(id);
		if (config == null) { 
			throw new NullPointerException("【SevenDaysCarnival】表的" + "id【" + id + "】不存在"); 
		}
		return config;
	}
	/**
	 * 根据id获取数据，允许返回null
	 * 
	 * @param id
	 * @return
	 */
	public SevenDaysCarnivalConfig getNullable(int id) {
		return this.sevendayscarnivals.get(id);
	}

	/**
	 * 获取所有数据
	 * @return
	 */
	public Collection<SevenDaysCarnivalConfig> list() {
		return this.sevendayscarnivals.values();
	}
	@Override
	public void load() {
		try {
			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, SevenDaysCarnivalConfig> sevendayscarnivals = new HashMap<>();
			for (Element e : list) {
				SevenDaysCarnivalConfig sevendayscarnival = new SevenDaysCarnivalConfig(e);
				SevenDaysCarnivalConfig old = sevendayscarnivals.put(sevendayscarnival.ID, sevendayscarnival);
				if (old != null) {
					throw new IllegalArgumentException("[SevenDaysCarnivalConfig]表存在重复的数据id： " + old.ID);
				}
			}			

			this.sevendayscarnivals = com.google.common.collect.ImmutableMap.copyOf(sevendayscarnivals);

			log.info("load SevenDaysCarnivalConfig size[{}]", sevendayscarnivals.size());

		} catch (Exception e) {
			throw new RuntimeException("load SevenDaysCarnivalConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
