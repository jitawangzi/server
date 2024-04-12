package cn.game.protocol.generated.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cn.game.protocol.generated.config.HeroBUFFConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class HeroBUFFManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(HeroBUFFManager.class);

	private static HeroBUFFManager instance = new HeroBUFFManager();
	private static final String xmlFileName = "HeroBUFF";
	
	/** 总数据，按id取值 */
	private Map<Integer, HeroBUFFConfig> herobuffs = new HashMap<>();

	public static HeroBUFFManager instance() {
		return instance;
	}
	private HeroBUFFManager() {
		WatchServiceManager.getInstance().register(this);
	}
	/**
	 * 根据id获取数据，一般用这个方法，如果数据不存在，一般是配置错误，直接抛出异常
	 * 
	 * @param id
	 * @return
	 */
	public HeroBUFFConfig get(int id) {
		HeroBUFFConfig config = this.herobuffs.get(id);
		if (config == null) { 
			throw new NullPointerException("【HeroBUFF】表的" + "id【" + id + "】不存在"); 
		}
		return config;
	}
	/**
	 * 根据id获取数据，允许返回null
	 * 
	 * @param id
	 * @return
	 */
	public HeroBUFFConfig getNullable(int id) {
		return this.herobuffs.get(id);
	}

	/**
	 * 获取所有数据
	 * @return
	 */
	public Collection<HeroBUFFConfig> list() {
		return this.herobuffs.values();
	}
	@Override
	public void load() {
		try {
			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, HeroBUFFConfig> herobuffs = new HashMap<>();
			for (Element e : list) {
				HeroBUFFConfig herobuff = new HeroBUFFConfig(e);
				HeroBUFFConfig old = herobuffs.put(herobuff.ID, herobuff);
				if (old != null) {
					throw new IllegalArgumentException("[HeroBUFFConfig]表存在重复的数据id： " + old.ID);
				}
			}			

			this.herobuffs = com.google.common.collect.ImmutableMap.copyOf(herobuffs);

			log.info("load HeroBUFFConfig size[{}]", herobuffs.size());

		} catch (Exception e) {
			throw new RuntimeException("load HeroBUFFConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
