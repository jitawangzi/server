package cn.game.protocol.generated.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cn.game.protocol.generated.config.DaoHeartConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class DaoHeartManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(DaoHeartManager.class);

	private static DaoHeartManager instance = new DaoHeartManager();
	private static final String xmlFileName = "DaoHeart";
	
	/** 总数据，按id取值 */
	private Map<Integer, DaoHeartConfig> daohearts = new HashMap<>();

	public static DaoHeartManager instance() {
		return instance;
	}
	private DaoHeartManager() {
		WatchServiceManager.getInstance().register(this);
	}
	/**
	 * 根据id获取数据，一般用这个方法，如果数据不存在，一般是配置错误，直接抛出异常
	 * 
	 * @param id
	 * @return
	 */
	public DaoHeartConfig get(int id) {
		DaoHeartConfig config = this.daohearts.get(id);
		if (config == null) { 
			throw new NullPointerException("【DaoHeart】表的" + "id【" + id + "】不存在"); 
		}
		return config;
	}
	/**
	 * 根据id获取数据，允许返回null
	 * 
	 * @param id
	 * @return
	 */
	public DaoHeartConfig getNullable(int id) {
		return this.daohearts.get(id);
	}

	/**
	 * 获取所有数据
	 * @return
	 */
	public Collection<DaoHeartConfig> list() {
		return this.daohearts.values();
	}
	@Override
	public void load() {
		try {
			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, DaoHeartConfig> daohearts = new HashMap<>();
			for (Element e : list) {
				DaoHeartConfig daoheart = new DaoHeartConfig(e);
				DaoHeartConfig old = daohearts.put(daoheart.ID, daoheart);
				if (old != null) {
					throw new IllegalArgumentException("[DaoHeartConfig]表存在重复的数据id： " + old.ID);
				}
			}			

			this.daohearts = com.google.common.collect.ImmutableMap.copyOf(daohearts);

			log.info("load DaoHeartConfig size[{}]", daohearts.size());

		} catch (Exception e) {
			throw new RuntimeException("load DaoHeartConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
