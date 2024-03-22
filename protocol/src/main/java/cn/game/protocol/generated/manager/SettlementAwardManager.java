package cn.game.protocol.generated.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cn.game.protocol.generated.config.SettlementAwardConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class SettlementAwardManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(SettlementAwardManager.class);

	private static SettlementAwardManager instance = new SettlementAwardManager();
	private static final String xmlFileName = "SettlementAward";
	
	private Map<Integer, SettlementAwardConfig> settlementawards = new HashMap<>();

	public static SettlementAwardManager getInstance() {
		return instance;
	}

	private SettlementAwardManager() {
		WatchServiceManager.getInstance().register(this);
	}
	/**
	 * 根据id获取数据，一般用这个方法，如果数据不存在，一般是配置错误，直接抛出异常
	 * 
	 * @param id
	 * @return
	 */
	public SettlementAwardConfig getSettlementAwardConfig(int id) {
		SettlementAwardConfig config = this.settlementawards.get(id);
		if (config == null) { 
			throw new NullPointerException("【SettlementAward】表的" + "id【" + id + "】不存在"); 
		}
		return config;
	}
	/**
	 * 根据id获取数据，允许返回null
	 * 
	 * @param id
	 * @return
	 */
	public SettlementAwardConfig getSettlementAwardConfigNullable(int id) {
		return this.settlementawards.get(id);
	}

	public Collection<SettlementAwardConfig> list() {
		return this.settlementawards.values();
	}

	@Override
	public void load() {

		try {
			ClassLoader classLoader = Thread.currentThread().getClass().getClassLoader();
			if (classLoader == null) {
				classLoader = SettlementAwardManager.class.getClassLoader();
			}
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, SettlementAwardConfig> settlementawards = new HashMap<>();
			for (Element e : list) {
				SettlementAwardConfig settlementaward = new SettlementAwardConfig(e);
				settlementawards.put(settlementaward.getId(), settlementaward);
			}			

			this.settlementawards = com.google.common.collect.ImmutableMap.copyOf(settlementawards);

			log.info("load SettlementAwardConfig size[{}]", settlementawards.size());

		} catch (Exception e) {
			throw new RuntimeException("load SettlementAwardConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
