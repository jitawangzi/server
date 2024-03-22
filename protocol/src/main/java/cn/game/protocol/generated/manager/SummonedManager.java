package cn.game.protocol.generated.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cn.game.protocol.generated.config.SummonedConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class SummonedManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(SummonedManager.class);

	private static SummonedManager instance = new SummonedManager();
	private static final String xmlFileName = "Summoned";
	
	private Map<Integer, SummonedConfig> summoneds = new HashMap<>();

	public static SummonedManager getInstance() {
		return instance;
	}

	private SummonedManager() {
		WatchServiceManager.getInstance().register(this);
	}
	/**
	 * 根据id获取数据，一般用这个方法，如果数据不存在，一般是配置错误，直接抛出异常
	 * 
	 * @param id
	 * @return
	 */
	public SummonedConfig getSummonedConfig(int id) {
		SummonedConfig config = this.summoneds.get(id);
		if (config == null) { 
			throw new NullPointerException("【Summoned】表的" + "id【" + id + "】不存在"); 
		}
		return config;
	}
	/**
	 * 根据id获取数据，允许返回null
	 * 
	 * @param id
	 * @return
	 */
	public SummonedConfig getSummonedConfigNullable(int id) {
		return this.summoneds.get(id);
	}

	public Collection<SummonedConfig> list() {
		return this.summoneds.values();
	}

	@Override
	public void load() {

		try {
			ClassLoader classLoader = Thread.currentThread().getClass().getClassLoader();
			if (classLoader == null) {
				classLoader = SummonedManager.class.getClassLoader();
			}
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, SummonedConfig> summoneds = new HashMap<>();
			for (Element e : list) {
				SummonedConfig summoned = new SummonedConfig(e);
				summoneds.put(summoned.getId(), summoned);
			}			

			this.summoneds = com.google.common.collect.ImmutableMap.copyOf(summoneds);

			log.info("load SummonedConfig size[{}]", summoneds.size());

		} catch (Exception e) {
			throw new RuntimeException("load SummonedConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
