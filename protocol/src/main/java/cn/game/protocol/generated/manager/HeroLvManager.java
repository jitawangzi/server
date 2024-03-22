package cn.game.protocol.generated.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cn.game.protocol.generated.config.HeroLvConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class HeroLvManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(HeroLvManager.class);

	private static HeroLvManager instance = new HeroLvManager();
	private static final String xmlFileName = "HeroLv";
	
	private Map<Integer, HeroLvConfig> herolvs = new HashMap<>();

	public static HeroLvManager instance() {
		return instance;
	}

	private HeroLvManager() {
		WatchServiceManager.getInstance().register(this);
	}
	/**
	 * 根据id获取数据，一般用这个方法，如果数据不存在，一般是配置错误，直接抛出异常
	 * 
	 * @param id
	 * @return
	 */
	public HeroLvConfig get(int id) {
		HeroLvConfig config = this.herolvs.get(id);
		if (config == null) { 
			throw new NullPointerException("【HeroLv】表的" + "id【" + id + "】不存在"); 
		}
		return config;
	}
	/**
	 * 根据id获取数据，允许返回null
	 * 
	 * @param id
	 * @return
	 */
	public HeroLvConfig getNullable(int id) {
		return this.herolvs.get(id);
	}

	public Collection<HeroLvConfig> list() {
		return this.herolvs.values();
	}

	@Override
	public void load() {

		try {
			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, HeroLvConfig> herolvs = new HashMap<>();
			for (Element e : list) {
				HeroLvConfig herolv = new HeroLvConfig(e);
				herolvs.put(herolv.ID, herolv);
			}			

			this.herolvs = com.google.common.collect.ImmutableMap.copyOf(herolvs);

			log.info("load HeroLvConfig size[{}]", herolvs.size());

		} catch (Exception e) {
			throw new RuntimeException("load HeroLvConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
