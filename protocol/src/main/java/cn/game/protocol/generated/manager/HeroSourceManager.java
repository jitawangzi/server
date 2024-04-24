package cn.game.protocol.generated.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cn.game.protocol.generated.config.HeroSourceConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class HeroSourceManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(HeroSourceManager.class);

	private static HeroSourceManager instance = new HeroSourceManager();
	private static final String xmlFileName = "HeroSource";
	
	/** 总数据，按id取值 */
	private Map<Integer, HeroSourceConfig> herosources = new HashMap<>();

	public static HeroSourceManager instance() {
		return instance;
	}
	private HeroSourceManager() {
		WatchServiceManager.getInstance().register(this);
	}
	/**
	 * 根据id获取数据，一般用这个方法，如果数据不存在，一般是配置错误，直接抛出异常
	 * 
	 * @param id
	 * @return
	 */
	public HeroSourceConfig get(int id) {
		HeroSourceConfig config = this.herosources.get(id);
		if (config == null) { 
			throw new NullPointerException("【HeroSource】表的" + "id【" + id + "】不存在"); 
		}
		return config;
	}
	/**
	 * 根据id获取数据，允许返回null
	 * 
	 * @param id
	 * @return
	 */
	public HeroSourceConfig getNullable(int id) {
		return this.herosources.get(id);
	}

	/**
	 * 获取所有数据
	 * @return
	 */
	public Collection<HeroSourceConfig> list() {
		return this.herosources.values();
	}
	@Override
	public void load() {
		try {
			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, HeroSourceConfig> herosources = new HashMap<>();
			for (Element e : list) {
				HeroSourceConfig herosource = new HeroSourceConfig(e);
				HeroSourceConfig old = herosources.put(herosource.ID, herosource);
				if (old != null) {
					throw new IllegalArgumentException("[HeroSourceConfig]表存在重复的数据id： " + old.ID);
				}
			}			

			this.herosources = com.google.common.collect.ImmutableMap.copyOf(herosources);

			log.info("load HeroSourceConfig size[{}]", herosources.size());

		} catch (Exception e) {
			throw new RuntimeException("load HeroSourceConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
