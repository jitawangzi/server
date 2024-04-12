package cn.game.protocol.generated.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cn.game.protocol.generated.config.HeroBulletConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class HeroBulletManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(HeroBulletManager.class);

	private static HeroBulletManager instance = new HeroBulletManager();
	private static final String xmlFileName = "HeroBullet";
	
	/** 总数据，按id取值 */
	private Map<Integer, HeroBulletConfig> herobullets = new HashMap<>();

	public static HeroBulletManager instance() {
		return instance;
	}
	private HeroBulletManager() {
		WatchServiceManager.getInstance().register(this);
	}
	/**
	 * 根据id获取数据，一般用这个方法，如果数据不存在，一般是配置错误，直接抛出异常
	 * 
	 * @param id
	 * @return
	 */
	public HeroBulletConfig get(int id) {
		HeroBulletConfig config = this.herobullets.get(id);
		if (config == null) { 
			throw new NullPointerException("【HeroBullet】表的" + "id【" + id + "】不存在"); 
		}
		return config;
	}
	/**
	 * 根据id获取数据，允许返回null
	 * 
	 * @param id
	 * @return
	 */
	public HeroBulletConfig getNullable(int id) {
		return this.herobullets.get(id);
	}

	/**
	 * 获取所有数据
	 * @return
	 */
	public Collection<HeroBulletConfig> list() {
		return this.herobullets.values();
	}
	@Override
	public void load() {
		try {
			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, HeroBulletConfig> herobullets = new HashMap<>();
			for (Element e : list) {
				HeroBulletConfig herobullet = new HeroBulletConfig(e);
				HeroBulletConfig old = herobullets.put(herobullet.ID, herobullet);
				if (old != null) {
					throw new IllegalArgumentException("[HeroBulletConfig]表存在重复的数据id： " + old.ID);
				}
			}			

			this.herobullets = com.google.common.collect.ImmutableMap.copyOf(herobullets);

			log.info("load HeroBulletConfig size[{}]", herobullets.size());

		} catch (Exception e) {
			throw new RuntimeException("load HeroBulletConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
