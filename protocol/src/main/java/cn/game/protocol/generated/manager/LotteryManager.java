package cn.game.protocol.generated.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cn.game.protocol.generated.config.LotteryConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class LotteryManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(LotteryManager.class);

	private static LotteryManager instance = new LotteryManager();
	private static final String xmlFileName = "Lottery";
	
	/** 总数据，按id取值 */
	private Map<Integer, LotteryConfig> lotterys = new HashMap<>();

	public static LotteryManager instance() {
		return instance;
	}
	private LotteryManager() {
		WatchServiceManager.getInstance().register(this);
	}
	/**
	 * 根据id获取数据，一般用这个方法，如果数据不存在，一般是配置错误，直接抛出异常
	 * 
	 * @param id
	 * @return
	 */
	public LotteryConfig get(int id) {
		LotteryConfig config = this.lotterys.get(id);
		if (config == null) { 
			throw new NullPointerException("【Lottery】表的" + "id【" + id + "】不存在"); 
		}
		return config;
	}
	/**
	 * 根据id获取数据，允许返回null
	 * 
	 * @param id
	 * @return
	 */
	public LotteryConfig getNullable(int id) {
		return this.lotterys.get(id);
	}

	/**
	 * 获取所有数据
	 * @return
	 */
	public Collection<LotteryConfig> list() {
		return this.lotterys.values();
	}
	@Override
	public void load() {
		try {
			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, LotteryConfig> lotterys = new HashMap<>();
			for (Element e : list) {
				LotteryConfig lottery = new LotteryConfig(e);
				LotteryConfig old = lotterys.put(lottery.ID, lottery);
				if (old != null) {
					throw new IllegalArgumentException("[LotteryConfig]表存在重复的数据id： " + old.ID);
				}
			}			

			this.lotterys = com.google.common.collect.ImmutableMap.copyOf(lotterys);

			log.info("load LotteryConfig size[{}]", lotterys.size());

		} catch (Exception e) {
			throw new RuntimeException("load LotteryConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
