package cn.game.protocol.generated.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cn.game.protocol.generated.config.EnergyCalculationConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class EnergyCalculationManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(EnergyCalculationManager.class);

	private static EnergyCalculationManager instance = new EnergyCalculationManager();
	private static final String xmlFileName = "EnergyCalculation";
	
	/** 总数据，按id取值 */
	private Map<Integer, EnergyCalculationConfig> energycalculations = new HashMap<>();

	public static EnergyCalculationManager instance() {
		return instance;
	}
	private EnergyCalculationManager() {
		WatchServiceManager.getInstance().register(this);
	}
	/**
	 * 根据id获取数据，一般用这个方法，如果数据不存在，一般是配置错误，直接抛出异常
	 * 
	 * @param id
	 * @return
	 */
	public EnergyCalculationConfig get(int id) {
		EnergyCalculationConfig config = this.energycalculations.get(id);
		if (config == null) { 
			throw new NullPointerException("【EnergyCalculation】表的" + "id【" + id + "】不存在"); 
		}
		return config;
	}
	/**
	 * 根据id获取数据，允许返回null
	 * 
	 * @param id
	 * @return
	 */
	public EnergyCalculationConfig getNullable(int id) {
		return this.energycalculations.get(id);
	}

	/**
	 * 获取所有数据
	 * @return
	 */
	public Collection<EnergyCalculationConfig> list() {
		return this.energycalculations.values();
	}
	@Override
	public void load() {
		try {
			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, EnergyCalculationConfig> energycalculations = new HashMap<>();
			for (Element e : list) {
				EnergyCalculationConfig energycalculation = new EnergyCalculationConfig(e);
				EnergyCalculationConfig old = energycalculations.put(energycalculation.ID, energycalculation);
				if (old != null) {
					throw new IllegalArgumentException("[EnergyCalculationConfig]表存在重复的数据id： " + old.ID);
				}
			}			

			this.energycalculations = com.google.common.collect.ImmutableMap.copyOf(energycalculations);

			log.info("load EnergyCalculationConfig size[{}]", energycalculations.size());

		} catch (Exception e) {
			throw new RuntimeException("load EnergyCalculationConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
