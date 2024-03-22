package cn.game.protocol.generated.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cn.game.protocol.generated.config.ProduceChestVacancyRateParaConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class ProduceChestVacancyRateParaManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(ProduceChestVacancyRateParaManager.class);

	private static ProduceChestVacancyRateParaManager instance = new ProduceChestVacancyRateParaManager();
	private static final String xmlFileName = "ProduceChestVacancyRatePara";
	
	private Map<Integer, ProduceChestVacancyRateParaConfig> producechestvacancyrateparas = new HashMap<>();

	public static ProduceChestVacancyRateParaManager getInstance() {
		return instance;
	}

	private ProduceChestVacancyRateParaManager() {
		WatchServiceManager.getInstance().register(this);
	}
	/**
	 * 根据id获取数据，一般用这个方法，如果数据不存在，一般是配置错误，直接抛出异常
	 * 
	 * @param id
	 * @return
	 */
	public ProduceChestVacancyRateParaConfig getProduceChestVacancyRateParaConfig(int id) {
		ProduceChestVacancyRateParaConfig config = this.producechestvacancyrateparas.get(id);
		if (config == null) { 
			throw new NullPointerException("【ProduceChestVacancyRatePara】表的" + "id【" + id + "】不存在"); 
		}
		return config;
	}
	/**
	 * 根据id获取数据，允许返回null
	 * 
	 * @param id
	 * @return
	 */
	public ProduceChestVacancyRateParaConfig getProduceChestVacancyRateParaConfigNullable(int id) {
		return this.producechestvacancyrateparas.get(id);
	}

	public Collection<ProduceChestVacancyRateParaConfig> list() {
		return this.producechestvacancyrateparas.values();
	}

	@Override
	public void load() {

		try {
			ClassLoader classLoader = Thread.currentThread().getClass().getClassLoader();
			if (classLoader == null) {
				classLoader = ProduceChestVacancyRateParaManager.class.getClassLoader();
			}
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, ProduceChestVacancyRateParaConfig> map = new HashMap<>();
			for (Element e : list) {
				ProduceChestVacancyRateParaConfig producechestvacancyratepara = new ProduceChestVacancyRateParaConfig(e);
				map.put(producechestvacancyratepara.getId(), producechestvacancyratepara);
			}
			
			this.producechestvacancyrateparas = map;

			log.info("load ProduceChestVacancyRateParaConfig size[{}]", map.size());

		} catch (Exception e) {
			log.error("load ProduceChestVacancyRateParaConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
