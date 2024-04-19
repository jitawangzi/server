package cn.game.protocol.generated.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cn.game.protocol.generated.config.FundPassConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class FundPassManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(FundPassManager.class);

	private static FundPassManager instance = new FundPassManager();
	private static final String xmlFileName = "FundPass";
	
	/** 总数据，按id取值 */
	private Map<Integer, FundPassConfig> fundpasss = new HashMap<>();

	public static FundPassManager instance() {
		return instance;
	}
	private FundPassManager() {
		WatchServiceManager.getInstance().register(this);
	}
	/**
	 * 根据id获取数据，一般用这个方法，如果数据不存在，一般是配置错误，直接抛出异常
	 * 
	 * @param id
	 * @return
	 */
	public FundPassConfig get(int id) {
		FundPassConfig config = this.fundpasss.get(id);
		if (config == null) { 
			throw new NullPointerException("【FundPass】表的" + "id【" + id + "】不存在"); 
		}
		return config;
	}
	/**
	 * 根据id获取数据，允许返回null
	 * 
	 * @param id
	 * @return
	 */
	public FundPassConfig getNullable(int id) {
		return this.fundpasss.get(id);
	}

	/**
	 * 获取所有数据
	 * @return
	 */
	public Collection<FundPassConfig> list() {
		return this.fundpasss.values();
	}
	@Override
	public void load() {
		try {
			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, FundPassConfig> fundpasss = new HashMap<>();
			for (Element e : list) {
				FundPassConfig fundpass = new FundPassConfig(e);
				FundPassConfig old = fundpasss.put(fundpass.ID, fundpass);
				if (old != null) {
					throw new IllegalArgumentException("[FundPassConfig]表存在重复的数据id： " + old.ID);
				}
			}			

			this.fundpasss = com.google.common.collect.ImmutableMap.copyOf(fundpasss);

			log.info("load FundPassConfig size[{}]", fundpasss.size());

		} catch (Exception e) {
			throw new RuntimeException("load FundPassConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
