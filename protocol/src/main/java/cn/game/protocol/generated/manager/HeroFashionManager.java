package cn.game.protocol.generated.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cn.game.protocol.generated.config.HeroFashionConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class HeroFashionManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(HeroFashionManager.class);

	private static HeroFashionManager instance = new HeroFashionManager();
	private static final String xmlFileName = "HeroFashion";
	
	private Map<Integer, HeroFashionConfig> herofashions = new HashMap<>();

	public static HeroFashionManager instance() {
		return instance;
	}

	private HeroFashionManager() {
		WatchServiceManager.getInstance().register(this);
	}
	/**
	 * 根据id获取数据，一般用这个方法，如果数据不存在，一般是配置错误，直接抛出异常
	 * 
	 * @param id
	 * @return
	 */
	public HeroFashionConfig get(int id) {
		HeroFashionConfig config = this.herofashions.get(id);
		if (config == null) { 
			throw new NullPointerException("【HeroFashion】表的" + "id【" + id + "】不存在"); 
		}
		return config;
	}
	/**
	 * 根据id获取数据，允许返回null
	 * 
	 * @param id
	 * @return
	 */
	public HeroFashionConfig getNullable(int id) {
		return this.herofashions.get(id);
	}

	public Collection<HeroFashionConfig> list() {
		return this.herofashions.values();
	}

	@Override
	public void load() {

		try {
			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, HeroFashionConfig> herofashions = new HashMap<>();
			for (Element e : list) {
				HeroFashionConfig herofashion = new HeroFashionConfig(e);
				herofashions.put(herofashion.ID, herofashion);
			}			

			this.herofashions = com.google.common.collect.ImmutableMap.copyOf(herofashions);

			log.info("load HeroFashionConfig size[{}]", herofashions.size());

		} catch (Exception e) {
			throw new RuntimeException("load HeroFashionConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
