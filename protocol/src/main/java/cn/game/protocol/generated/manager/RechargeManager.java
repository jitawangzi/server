package cn.game.protocol.generated.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cn.game.protocol.generated.config.RechargeConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class RechargeManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(RechargeManager.class);

	private static RechargeManager instance = new RechargeManager();
	private static final String xmlFileName = "Recharge";
	
	/** 总数据，按id取值 */
	private Map<Integer, RechargeConfig> recharges = new HashMap<>();

	public static RechargeManager instance() {
		return instance;
	}
	private RechargeManager() {
		WatchServiceManager.getInstance().register(this);
	}
	/**
	 * 根据id获取数据，一般用这个方法，如果数据不存在，一般是配置错误，直接抛出异常
	 * 
	 * @param id
	 * @return
	 */
	public RechargeConfig get(int id) {
		RechargeConfig config = this.recharges.get(id);
		if (config == null) { 
			throw new NullPointerException("【Recharge】表的" + "id【" + id + "】不存在"); 
		}
		return config;
	}
	/**
	 * 根据id获取数据，允许返回null
	 * 
	 * @param id
	 * @return
	 */
	public RechargeConfig getNullable(int id) {
		return this.recharges.get(id);
	}

	/**
	 * 获取所有数据
	 * @return
	 */
	public Collection<RechargeConfig> list() {
		return this.recharges.values();
	}
	@Override
	public void load() {
		try {
			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, RechargeConfig> recharges = new HashMap<>();
			for (Element e : list) {
				RechargeConfig recharge = new RechargeConfig(e);
				RechargeConfig old = recharges.put(recharge.ID, recharge);
				if (old != null) {
					throw new IllegalArgumentException("[RechargeConfig]表存在重复的数据id： " + old.ID);
				}
			}			

			this.recharges = com.google.common.collect.ImmutableMap.copyOf(recharges);

			log.info("load RechargeConfig size[{}]", recharges.size());

		} catch (Exception e) {
			throw new RuntimeException("load RechargeConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
