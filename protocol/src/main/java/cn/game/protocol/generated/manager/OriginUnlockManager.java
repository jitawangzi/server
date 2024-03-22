package cn.game.protocol.generated.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cn.game.protocol.generated.config.OriginUnlockConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class OriginUnlockManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(OriginUnlockManager.class);

	private static OriginUnlockManager instance = new OriginUnlockManager();
	private static final String xmlFileName = "OriginUnlock";
	
	private Map<Integer, OriginUnlockConfig> originunlocks = new HashMap<>();

	public static OriginUnlockManager getInstance() {
		return instance;
	}

	private OriginUnlockManager() {
		WatchServiceManager.getInstance().register(this);
	}
	/**
	 * 根据id获取数据，一般用这个方法，如果数据不存在，一般是配置错误，直接抛出异常
	 * 
	 * @param id
	 * @return
	 */
	public OriginUnlockConfig getOriginUnlockConfig(int id) {
		OriginUnlockConfig config = this.originunlocks.get(id);
		if (config == null) { 
			throw new NullPointerException("【OriginUnlock】表的" + "id【" + id + "】不存在"); 
		}
		return config;
	}
	/**
	 * 根据id获取数据，允许返回null
	 * 
	 * @param id
	 * @return
	 */
	public OriginUnlockConfig getOriginUnlockConfigNullable(int id) {
		return this.originunlocks.get(id);
	}

	public Collection<OriginUnlockConfig> list() {
		return this.originunlocks.values();
	}

	@Override
	public void load() {

		try {
			ClassLoader classLoader = Thread.currentThread().getClass().getClassLoader();
			if (classLoader == null) {
				classLoader = OriginUnlockManager.class.getClassLoader();
			}
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, OriginUnlockConfig> originunlocks = new HashMap<>();
			for (Element e : list) {
				OriginUnlockConfig originunlock = new OriginUnlockConfig(e);
				originunlocks.put(originunlock.getId(), originunlock);
			}			

			this.originunlocks = com.google.common.collect.ImmutableMap.copyOf(originunlocks);

			log.info("load OriginUnlockConfig size[{}]", originunlocks.size());

		} catch (Exception e) {
			throw new RuntimeException("load OriginUnlockConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
