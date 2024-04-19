package cn.game.protocol.generated.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cn.game.protocol.generated.config.HeroSwordConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class HeroSwordManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(HeroSwordManager.class);

	private static HeroSwordManager instance = new HeroSwordManager();
	private static final String xmlFileName = "HeroSword";
	
	private Map<Integer, HeroSwordConfig> heroswords = new HashMap<>();

	public static HeroSwordManager instance() {
		return instance;
	}

	private HeroSwordManager() {
		WatchServiceManager.getInstance().register(this);
	}
	/**
	 * 根据id获取数据，一般用这个方法，如果数据不存在，一般是配置错误，直接抛出异常
	 * 
	 * @param id
	 * @return
	 */
	public HeroSwordConfig get(int id) {
		HeroSwordConfig config = this.heroswords.get(id);
		if (config == null) { 
			throw new NullPointerException("【HeroSword】表的" + "id【" + id + "】不存在"); 
		}
		return config;
	}
	/**
	 * 根据id获取数据，允许返回null
	 * 
	 * @param id
	 * @return
	 */
	public HeroSwordConfig getNullable(int id) {
		return this.heroswords.get(id);
	}

	public Collection<HeroSwordConfig> list() {
		return this.heroswords.values();
	}

	@Override
	public void load() {

		try {
			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, HeroSwordConfig> heroswords = new HashMap<>();
			for (Element e : list) {
				HeroSwordConfig herosword = new HeroSwordConfig(e);
				heroswords.put(herosword.ID, herosword);
			}			

			this.heroswords = com.google.common.collect.ImmutableMap.copyOf(heroswords);

			log.info("load HeroSwordConfig size[{}]", heroswords.size());

		} catch (Exception e) {
			throw new RuntimeException("load HeroSwordConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
