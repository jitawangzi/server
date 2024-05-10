package cn.game.protocol.generated.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cn.game.protocol.generated.config.RankConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class RankManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(RankManager.class);

	private static RankManager instance = new RankManager();
	private static final String xmlFileName = "Rank";
	
	/** 总数据，按id取值 */
	private Map<Integer, RankConfig> ranks = new HashMap<>();

	public static RankManager instance() {
		return instance;
	}
	private RankManager() {
		WatchServiceManager.getInstance().register(this);
	}
	/**
	 * 根据id获取数据，一般用这个方法，如果数据不存在，一般是配置错误，直接抛出异常
	 * 
	 * @param id
	 * @return
	 */
	public RankConfig get(int id) {
		RankConfig config = this.ranks.get(id);
		if (config == null) { 
			throw new NullPointerException("【Rank】表的" + "id【" + id + "】不存在"); 
		}
		return config;
	}
	/**
	 * 根据id获取数据，允许返回null
	 * 
	 * @param id
	 * @return
	 */
	public RankConfig getNullable(int id) {
		return this.ranks.get(id);
	}

	/**
	 * 获取所有数据
	 * @return
	 */
	public Collection<RankConfig> list() {
		return this.ranks.values();
	}
	@Override
	public void load() {
		try {
			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, RankConfig> ranks = new HashMap<>();
			for (Element e : list) {
				RankConfig rank = new RankConfig(e);
				RankConfig old = ranks.put(rank.ID, rank);
				if (old != null) {
					throw new IllegalArgumentException("[RankConfig]表存在重复的数据id： " + old.ID);
				}
			}			

			this.ranks = com.google.common.collect.ImmutableMap.copyOf(ranks);

			log.info("load RankConfig size[{}]", ranks.size());

		} catch (Exception e) {
			throw new RuntimeException("load RankConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
