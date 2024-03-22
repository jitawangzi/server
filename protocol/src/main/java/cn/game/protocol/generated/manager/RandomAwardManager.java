package cn.game.protocol.generated.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cn.game.protocol.generated.config.RandomAwardConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class RandomAwardManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(RandomAwardManager.class);

	private static RandomAwardManager instance = new RandomAwardManager();
	private static final String xmlFileName = "RandomAward";
	
	private Map<Integer, RandomAwardConfig> randomawards = new HashMap<>();

	public static RandomAwardManager getInstance() {
		return instance;
	}

	private RandomAwardManager() {
		WatchServiceManager.getInstance().register(this);
	}
	/**
	 * 根据id获取数据，一般用这个方法，如果数据不存在，一般是配置错误，直接抛出异常
	 * 
	 * @param id
	 * @return
	 */
	public RandomAwardConfig get(int id) {
		RandomAwardConfig config = this.randomawards.get(id);
		if (config == null) { 
			throw new NullPointerException("【RandomAward】表的" + "id【" + id + "】不存在"); 
		}
		return config;
	}
	/**
	 * 根据id获取数据，允许返回null
	 * 
	 * @param id
	 * @return
	 */
	public RandomAwardConfig getNullable(int id) {
		return this.randomawards.get(id);
	}

	public Collection<RandomAwardConfig> list() {
		return this.randomawards.values();
	}

	@Override
	public void load() {

		try {
			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, RandomAwardConfig> randomawards = new HashMap<>();
			for (Element e : list) {
				RandomAwardConfig randomaward = new RandomAwardConfig(e);
				randomawards.put(randomaward.getID(), randomaward);
			}			

			this.randomawards = com.google.common.collect.ImmutableMap.copyOf(randomawards);

			log.info("load RandomAwardConfig size[{}]", randomawards.size());

		} catch (Exception e) {
			throw new RuntimeException("load RandomAwardConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
