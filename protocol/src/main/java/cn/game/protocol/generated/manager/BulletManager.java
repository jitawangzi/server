package cn.game.protocol.generated.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cn.game.protocol.generated.config.BulletConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class BulletManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(BulletManager.class);

	private static BulletManager instance = new BulletManager();
	private static final String xmlFileName = "Bullet";
	
	/** 总数据，按id取值 */
	private Map<Integer, BulletConfig> bullets = new HashMap<>();

	public static BulletManager instance() {
		return instance;
	}
	private BulletManager() {
		WatchServiceManager.getInstance().register(this);
	}
	/**
	 * 根据id获取数据，一般用这个方法，如果数据不存在，一般是配置错误，直接抛出异常
	 * 
	 * @param id
	 * @return
	 */
	public BulletConfig get(int id) {
		BulletConfig config = this.bullets.get(id);
		if (config == null) { 
			throw new NullPointerException("【Bullet】表的" + "id【" + id + "】不存在"); 
		}
		return config;
	}
	/**
	 * 根据id获取数据，允许返回null
	 * 
	 * @param id
	 * @return
	 */
	public BulletConfig getNullable(int id) {
		return this.bullets.get(id);
	}

	/**
	 * 获取所有数据
	 * @return
	 */
	public Collection<BulletConfig> list() {
		return this.bullets.values();
	}
	@Override
	public void load() {
		try {
			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, BulletConfig> bullets = new HashMap<>();
			for (Element e : list) {
				BulletConfig bullet = new BulletConfig(e);
				BulletConfig old = bullets.put(bullet.ID, bullet);
				if (old != null) {
					throw new IllegalArgumentException("[BulletConfig]表存在重复的数据id： " + old.ID);
				}
			}			

			this.bullets = com.google.common.collect.ImmutableMap.copyOf(bullets);

			log.info("load BulletConfig size[{}]", bullets.size());

		} catch (Exception e) {
			throw new RuntimeException("load BulletConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
