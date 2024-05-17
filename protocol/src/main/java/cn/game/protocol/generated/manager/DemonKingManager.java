package cn.game.protocol.generated.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cn.game.protocol.generated.config.DemonKingConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class DemonKingManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(DemonKingManager.class);

	private static DemonKingManager instance = new DemonKingManager();
	private static final String xmlFileName = "DemonKing";
	
	/** 总数据，按id取值 */
	private Map<Integer, DemonKingConfig> demonkings = new HashMap<>();

	public static DemonKingManager instance() {
		return instance;
	}
	private DemonKingManager() {
		WatchServiceManager.getInstance().register(this);
	}
	/**
	 * 根据id获取数据，一般用这个方法，如果数据不存在，一般是配置错误，直接抛出异常
	 * 
	 * @param id
	 * @return
	 */
	public DemonKingConfig get(int id) {
		DemonKingConfig config = this.demonkings.get(id);
		if (config == null) { 
			throw new NullPointerException("【DemonKing】表的" + "id【" + id + "】不存在"); 
		}
		return config;
	}
	/**
	 * 根据id获取数据，允许返回null
	 * 
	 * @param id
	 * @return
	 */
	public DemonKingConfig getNullable(int id) {
		return this.demonkings.get(id);
	}

	/**
	 * 获取所有数据
	 * @return
	 */
	public Collection<DemonKingConfig> list() {
		return this.demonkings.values();
	}
	@Override
	public void load() {
		try {
			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, DemonKingConfig> demonkings = new HashMap<>();
			for (Element e : list) {
				DemonKingConfig demonking = new DemonKingConfig(e);
				DemonKingConfig old = demonkings.put(demonking.ID, demonking);
				if (old != null) {
					throw new IllegalArgumentException("[DemonKingConfig]表存在重复的数据id： " + old.ID);
				}
			}			

			this.demonkings = com.google.common.collect.ImmutableMap.copyOf(demonkings);

			log.info("load DemonKingConfig size[{}]", demonkings.size());

		} catch (Exception e) {
			throw new RuntimeException("load DemonKingConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
