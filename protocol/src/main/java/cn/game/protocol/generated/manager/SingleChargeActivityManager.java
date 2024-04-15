package cn.game.protocol.generated.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cn.game.protocol.generated.config.SingleChargeActivityConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class SingleChargeActivityManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(SingleChargeActivityManager.class);

	private static SingleChargeActivityManager instance = new SingleChargeActivityManager();
	private static final String xmlFileName = "SingleChargeActivity";
	
	/** 总数据，按id取值 */
	private Map<Integer, SingleChargeActivityConfig> singlechargeactivitys = new HashMap<>();

	public static SingleChargeActivityManager instance() {
		return instance;
	}
	private SingleChargeActivityManager() {
		WatchServiceManager.getInstance().register(this);
	}
	/**
	 * 根据id获取数据，一般用这个方法，如果数据不存在，一般是配置错误，直接抛出异常
	 * 
	 * @param id
	 * @return
	 */
	public SingleChargeActivityConfig get(int id) {
		SingleChargeActivityConfig config = this.singlechargeactivitys.get(id);
		if (config == null) { 
			throw new NullPointerException("【SingleChargeActivity】表的" + "id【" + id + "】不存在"); 
		}
		return config;
	}
	/**
	 * 根据id获取数据，允许返回null
	 * 
	 * @param id
	 * @return
	 */
	public SingleChargeActivityConfig getNullable(int id) {
		return this.singlechargeactivitys.get(id);
	}

	/**
	 * 获取所有数据
	 * @return
	 */
	public Collection<SingleChargeActivityConfig> list() {
		return this.singlechargeactivitys.values();
	}
	@Override
	public void load() {
		try {
			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, SingleChargeActivityConfig> singlechargeactivitys = new HashMap<>();
			for (Element e : list) {
				SingleChargeActivityConfig singlechargeactivity = new SingleChargeActivityConfig(e);
				SingleChargeActivityConfig old = singlechargeactivitys.put(singlechargeactivity.ID, singlechargeactivity);
				if (old != null) {
					throw new IllegalArgumentException("[SingleChargeActivityConfig]表存在重复的数据id： " + old.ID);
				}
			}			

			this.singlechargeactivitys = com.google.common.collect.ImmutableMap.copyOf(singlechargeactivitys);

			log.info("load SingleChargeActivityConfig size[{}]", singlechargeactivitys.size());

		} catch (Exception e) {
			throw new RuntimeException("load SingleChargeActivityConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
