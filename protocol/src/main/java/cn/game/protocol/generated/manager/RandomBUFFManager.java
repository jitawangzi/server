package cn.game.protocol.generated.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cn.game.protocol.generated.config.RandomBUFFConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class RandomBUFFManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(RandomBUFFManager.class);

	private static RandomBUFFManager instance = new RandomBUFFManager();
	private static final String xmlFileName = "RandomBUFF";
	
	/** 总数据，按id取值 */
	private Map<Integer, RandomBUFFConfig> randombuffs = new HashMap<>();

	public static RandomBUFFManager instance() {
		return instance;
	}
	private RandomBUFFManager() {
		WatchServiceManager.getInstance().register(this);
	}
	/**
	 * 根据id获取数据，一般用这个方法，如果数据不存在，一般是配置错误，直接抛出异常
	 * 
	 * @param id
	 * @return
	 */
	public RandomBUFFConfig get(int id) {
		RandomBUFFConfig config = this.randombuffs.get(id);
		if (config == null) { 
			throw new NullPointerException("【RandomBUFF】表的" + "id【" + id + "】不存在"); 
		}
		return config;
	}
	/**
	 * 根据id获取数据，允许返回null
	 * 
	 * @param id
	 * @return
	 */
	public RandomBUFFConfig getNullable(int id) {
		return this.randombuffs.get(id);
	}

	/**
	 * 获取所有数据
	 * @return
	 */
	public Collection<RandomBUFFConfig> list() {
		return this.randombuffs.values();
	}
	@Override
	public void load() {
		try {
			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, RandomBUFFConfig> randombuffs = new HashMap<>();
			for (Element e : list) {
				RandomBUFFConfig randombuff = new RandomBUFFConfig(e);
				RandomBUFFConfig old = randombuffs.put(randombuff.ID, randombuff);
				if (old != null) {
					throw new IllegalArgumentException("[RandomBUFFConfig]表存在重复的数据id： " + old.ID);
				}
			}			

			this.randombuffs = com.google.common.collect.ImmutableMap.copyOf(randombuffs);

			log.info("load RandomBUFFConfig size[{}]", randombuffs.size());

		} catch (Exception e) {
			throw new RuntimeException("load RandomBUFFConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
