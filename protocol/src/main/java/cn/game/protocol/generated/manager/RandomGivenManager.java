package cn.game.protocol.generated.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cn.game.protocol.generated.config.RandomGivenConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class RandomGivenManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(RandomGivenManager.class);

	private static RandomGivenManager instance = new RandomGivenManager();
	private static final String xmlFileName = "RandomGiven";
	
	/** 总数据，按id取值 */
	private Map<Integer, RandomGivenConfig> randomgivens = new HashMap<>();

	public static RandomGivenManager instance() {
		return instance;
	}
	private RandomGivenManager() {
		WatchServiceManager.getInstance().register(this);
	}
	/**
	 * 根据id获取数据，一般用这个方法，如果数据不存在，一般是配置错误，直接抛出异常
	 * 
	 * @param id
	 * @return
	 */
	public RandomGivenConfig get(int id) {
		RandomGivenConfig config = this.randomgivens.get(id);
		if (config == null) { 
			throw new NullPointerException("【RandomGiven】表的" + "id【" + id + "】不存在"); 
		}
		return config;
	}
	/**
	 * 根据id获取数据，允许返回null
	 * 
	 * @param id
	 * @return
	 */
	public RandomGivenConfig getNullable(int id) {
		return this.randomgivens.get(id);
	}

	/**
	 * 获取所有数据
	 * @return
	 */
	public Collection<RandomGivenConfig> list() {
		return this.randomgivens.values();
	}
	@Override
	public void load() {
		try {
			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, RandomGivenConfig> randomgivens = new HashMap<>();
			for (Element e : list) {
				RandomGivenConfig randomgiven = new RandomGivenConfig(e);
				RandomGivenConfig old = randomgivens.put(randomgiven.ID, randomgiven);
				if (old != null) {
					throw new IllegalArgumentException("[RandomGivenConfig]表存在重复的数据id： " + old.ID);
				}
			}			

			this.randomgivens = com.google.common.collect.ImmutableMap.copyOf(randomgivens);

			log.info("load RandomGivenConfig size[{}]", randomgivens.size());

		} catch (Exception e) {
			throw new RuntimeException("load RandomGivenConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
