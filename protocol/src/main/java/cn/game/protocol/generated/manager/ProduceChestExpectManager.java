package cn.game.protocol.generated.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cn.game.protocol.generated.config.ProduceChestExpectConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class ProduceChestExpectManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(ProduceChestExpectManager.class);

	private static ProduceChestExpectManager instance = new ProduceChestExpectManager();
	private static final String xmlFileName = "ProduceChestExpect";
	
	private Map<Integer, ProduceChestExpectConfig> producechestexpects = new HashMap<>();

	public static ProduceChestExpectManager getInstance() {
		return instance;
	}

	private ProduceChestExpectManager() {
		WatchServiceManager.getInstance().register(this);
	}
	/**
	 * 根据id获取数据，一般用这个方法，如果数据不存在，一般是配置错误，直接抛出异常
	 * 
	 * @param id
	 * @return
	 */
	public ProduceChestExpectConfig getProduceChestExpectConfig(int id) {
		ProduceChestExpectConfig config = this.producechestexpects.get(id);
		if (config == null) { 
			throw new NullPointerException("【ProduceChestExpect】表的" + "id【" + id + "】不存在"); 
		}
		return config;
	}
	/**
	 * 根据id获取数据，允许返回null
	 * 
	 * @param id
	 * @return
	 */
	public ProduceChestExpectConfig getProduceChestExpectConfigNullable(int id) {
		return this.producechestexpects.get(id);
	}

	public Collection<ProduceChestExpectConfig> list() {
		return this.producechestexpects.values();
	}

	@Override
	public void load() {

		try {
			ClassLoader classLoader = Thread.currentThread().getClass().getClassLoader();
			if (classLoader == null) {
				classLoader = ProduceChestExpectManager.class.getClassLoader();
			}
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, ProduceChestExpectConfig> map = new HashMap<>();
			for (Element e : list) {
				ProduceChestExpectConfig producechestexpect = new ProduceChestExpectConfig(e);
				map.put(producechestexpect.getId(), producechestexpect);
			}
			
			this.producechestexpects = map;

			log.info("load ProduceChestExpectConfig size[{}]", map.size());

		} catch (Exception e) {
			log.error("load ProduceChestExpectConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
