package cn.game.protocol.generated.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cn.game.protocol.generated.config.PromotionRulesConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class PromotionRulesManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(PromotionRulesManager.class);

	private static PromotionRulesManager instance = new PromotionRulesManager();
	private static final String xmlFileName = "PromotionRules";
	
	private Map<Integer, PromotionRulesConfig> promotionruless = new HashMap<>();

	public static PromotionRulesManager getInstance() {
		return instance;
	}

	private PromotionRulesManager() {
		WatchServiceManager.getInstance().register(this);
	}
	/**
	 * 根据id获取数据，一般用这个方法，如果数据不存在，一般是配置错误，直接抛出异常
	 * 
	 * @param id
	 * @return
	 */
	public PromotionRulesConfig getPromotionRulesConfig(int id) {
		PromotionRulesConfig config = this.promotionruless.get(id);
		if (config == null) { 
			throw new NullPointerException("【PromotionRules】表的" + "id【" + id + "】不存在"); 
		}
		return config;
	}
	/**
	 * 根据id获取数据，允许返回null
	 * 
	 * @param id
	 * @return
	 */
	public PromotionRulesConfig getPromotionRulesConfigNullable(int id) {
		return this.promotionruless.get(id);
	}

	public Collection<PromotionRulesConfig> list() {
		return this.promotionruless.values();
	}

	@Override
	public void load() {

		try {
			ClassLoader classLoader = Thread.currentThread().getClass().getClassLoader();
			if (classLoader == null) {
				classLoader = PromotionRulesManager.class.getClassLoader();
			}
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, PromotionRulesConfig> promotionruless = new HashMap<>();
			for (Element e : list) {
				PromotionRulesConfig promotionrules = new PromotionRulesConfig(e);
				promotionruless.put(promotionrules.getId(), promotionrules);
			}			

			this.promotionruless = com.google.common.collect.ImmutableMap.copyOf(promotionruless);

			log.info("load PromotionRulesConfig size[{}]", promotionruless.size());

		} catch (Exception e) {
			throw new RuntimeException("load PromotionRulesConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
