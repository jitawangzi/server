package cn.game.protocol.generated.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cn.game.protocol.generated.config.RPBoxVacancyRateParaConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class RPBoxVacancyRateParaManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(RPBoxVacancyRateParaManager.class);

	private static RPBoxVacancyRateParaManager instance = new RPBoxVacancyRateParaManager();
	private static final String xmlFileName = "RPBoxVacancyRatePara";
	
	private List<RPBoxVacancyRateParaConfig> rpboxvacancyrateparas = new ArrayList<RPBoxVacancyRateParaConfig>();

	public static RPBoxVacancyRateParaManager getInstance() {
		return instance;
	}

	private RPBoxVacancyRateParaManager() {
		WatchServiceManager.getInstance().register(this);
	}

	public List<RPBoxVacancyRateParaConfig> list() {
		return rpboxvacancyrateparas;
	}

	@Override
	public void load() {

		try {
			ClassLoader classLoader = Thread.currentThread().getClass().getClassLoader();
			if (classLoader == null) {
				classLoader = RPBoxVacancyRateParaManager.class.getClassLoader();
			}
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			List<RPBoxVacancyRateParaConfig> rpboxvacancyrateparas = new ArrayList<RPBoxVacancyRateParaConfig>();
			for (Element e : list) {
				RPBoxVacancyRateParaConfig rpboxvacancyratepara = new RPBoxVacancyRateParaConfig(e);
				rpboxvacancyrateparas.add(rpboxvacancyratepara);
			}			

			this.rpboxvacancyrateparas = com.google.common.collect.ImmutableList.copyOf(rpboxvacancyrateparas);

			log.info("load RPBoxVacancyRateParaConfig size[{}]", rpboxvacancyrateparas.size());

		} catch (Exception e) {
			log.error("load RPBoxVacancyRateParaConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
