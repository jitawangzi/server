package cn.game.protocol.generated.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cn.game.protocol.generated.config.HeroSummonConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class HeroSummonManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(HeroSummonManager.class);

	private static HeroSummonManager instance = new HeroSummonManager();
	private static final String xmlFileName = "HeroSummon";
	
	/** 总数据，按id取值 */
	private Map<Integer, HeroSummonConfig> herosummons = new HashMap<>();

	public static HeroSummonManager instance() {
		return instance;
	}
	private HeroSummonManager() {
		WatchServiceManager.getInstance().register(this);
	}
	/**
	 * 根据id获取数据，一般用这个方法，如果数据不存在，一般是配置错误，直接抛出异常
	 * 
	 * @param id
	 * @return
	 */
	public HeroSummonConfig get(int id) {
		HeroSummonConfig config = this.herosummons.get(id);
		if (config == null) { 
			throw new NullPointerException("【HeroSummon】表的" + "id【" + id + "】不存在"); 
		}
		return config;
	}
	/**
	 * 根据id获取数据，允许返回null
	 * 
	 * @param id
	 * @return
	 */
	public HeroSummonConfig getNullable(int id) {
		return this.herosummons.get(id);
	}

	/**
	 * 获取所有数据
	 * @return
	 */
	public Collection<HeroSummonConfig> list() {
		return this.herosummons.values();
	}
	@Override
	public void load() {
		try {
			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, HeroSummonConfig> herosummons = new HashMap<>();
			for (Element e : list) {
				HeroSummonConfig herosummon = new HeroSummonConfig(e);
				HeroSummonConfig old = herosummons.put(herosummon.ID, herosummon);
				if (old != null) {
					throw new IllegalArgumentException("[HeroSummonConfig]表存在重复的数据id： " + old.ID);
				}
			}			

			this.herosummons = com.google.common.collect.ImmutableMap.copyOf(herosummons);

			log.info("load HeroSummonConfig size[{}]", herosummons.size());

		} catch (Exception e) {
			throw new RuntimeException("load HeroSummonConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
