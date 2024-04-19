package cn.game.protocol.generated.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cn.game.protocol.generated.config.GemConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class GemManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(GemManager.class);

	private static GemManager instance = new GemManager();
	private static final String xmlFileName = "Gem";
	
	/** 总数据，按id取值 */
	private Map<Integer, GemConfig> gems = new HashMap<>();

	public static GemManager instance() {
		return instance;
	}
	private GemManager() {
		WatchServiceManager.getInstance().register(this);
	}
	/**
	 * 根据id获取数据，一般用这个方法，如果数据不存在，一般是配置错误，直接抛出异常
	 * 
	 * @param id
	 * @return
	 */
	public GemConfig get(int id) {
		GemConfig config = this.gems.get(id);
		if (config == null) { 
			throw new NullPointerException("【Gem】表的" + "id【" + id + "】不存在"); 
		}
		return config;
	}
	/**
	 * 根据id获取数据，允许返回null
	 * 
	 * @param id
	 * @return
	 */
	public GemConfig getNullable(int id) {
		return this.gems.get(id);
	}

	/**
	 * 获取所有数据
	 * @return
	 */
	public Collection<GemConfig> list() {
		return this.gems.values();
	}
	@Override
	public void load() {
		try {
			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, GemConfig> gems = new HashMap<>();
			for (Element e : list) {
				GemConfig gem = new GemConfig(e);
				GemConfig old = gems.put(gem.ID, gem);
				if (old != null) {
					throw new IllegalArgumentException("[GemConfig]表存在重复的数据id： " + old.ID);
				}
			}			

			this.gems = com.google.common.collect.ImmutableMap.copyOf(gems);

			log.info("load GemConfig size[{}]", gems.size());

		} catch (Exception e) {
			throw new RuntimeException("load GemConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
