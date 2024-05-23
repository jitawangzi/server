package cn.game.protocol.generated.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cn.game.protocol.generated.config.PotentialConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class PotentialManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(PotentialManager.class);

	private static PotentialManager instance = new PotentialManager();
	private static final String xmlFileName = "Potential";
	
	/** 总数据，按id取值 */
	private Map<Integer, PotentialConfig> potentials = new HashMap<>();

	public static PotentialManager instance() {
		return instance;
	}
	private PotentialManager() {
		WatchServiceManager.getInstance().register(this);
	}
	/**
	 * 根据id获取数据，一般用这个方法，如果数据不存在，一般是配置错误，直接抛出异常
	 * 
	 * @param id
	 * @return
	 */
	public PotentialConfig get(int id) {
		PotentialConfig config = this.potentials.get(id);
		if (config == null) { 
			throw new NullPointerException("【Potential】表的" + "id【" + id + "】不存在"); 
		}
		return config;
	}
	/**
	 * 根据id获取数据，允许返回null
	 * 
	 * @param id
	 * @return
	 */
	public PotentialConfig getNullable(int id) {
		return this.potentials.get(id);
	}

	/**
	 * 获取所有数据
	 * @return
	 */
	public Collection<PotentialConfig> list() {
		return this.potentials.values();
	}
	@Override
	public void load() {
		try {
			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, PotentialConfig> potentials = new HashMap<>();
			for (Element e : list) {
				PotentialConfig potential = new PotentialConfig(e);
				PotentialConfig old = potentials.put(potential.ID, potential);
				if (old != null) {
					throw new IllegalArgumentException("[PotentialConfig]表存在重复的数据id： " + old.ID);
				}
			}			

			this.potentials = com.google.common.collect.ImmutableMap.copyOf(potentials);

			log.info("load PotentialConfig size[{}]", potentials.size());

		} catch (Exception e) {
			throw new RuntimeException("load PotentialConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
