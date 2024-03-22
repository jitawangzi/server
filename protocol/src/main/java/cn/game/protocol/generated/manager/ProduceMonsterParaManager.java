package cn.game.protocol.generated.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cn.game.protocol.generated.config.ProduceMonsterParaConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class ProduceMonsterParaManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(ProduceMonsterParaManager.class);

	private static ProduceMonsterParaManager instance = new ProduceMonsterParaManager();
	private static final String xmlFileName = "ProduceMonsterPara";
	
	private Map<Integer, ProduceMonsterParaConfig> producemonsterparas = new HashMap<>();

	public static ProduceMonsterParaManager getInstance() {
		return instance;
	}

	private ProduceMonsterParaManager() {
		WatchServiceManager.getInstance().register(this);
	}
	/**
	 * 根据id获取数据，一般用这个方法，如果数据不存在，一般是配置错误，直接抛出异常
	 * 
	 * @param id
	 * @return
	 */
	public ProduceMonsterParaConfig getProduceMonsterParaConfig(int id) {
		ProduceMonsterParaConfig config = this.producemonsterparas.get(id);
		if (config == null) { 
			throw new NullPointerException("【ProduceMonsterPara】表的" + "id【" + id + "】不存在"); 
		}
		return config;
	}
	/**
	 * 根据id获取数据，允许返回null
	 * 
	 * @param id
	 * @return
	 */
	public ProduceMonsterParaConfig getProduceMonsterParaConfigNullable(int id) {
		return this.producemonsterparas.get(id);
	}

	public Collection<ProduceMonsterParaConfig> list() {
		return this.producemonsterparas.values();
	}

	@Override
	public void load() {

		try {
			ClassLoader classLoader = Thread.currentThread().getClass().getClassLoader();
			if (classLoader == null) {
				classLoader = ProduceMonsterParaManager.class.getClassLoader();
			}
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, ProduceMonsterParaConfig> map = new HashMap<>();
			for (Element e : list) {
				ProduceMonsterParaConfig producemonsterpara = new ProduceMonsterParaConfig(e);
				map.put(producemonsterpara.getId(), producemonsterpara);
			}
			
			this.producemonsterparas = map;

			log.info("load ProduceMonsterParaConfig size[{}]", map.size());

		} catch (Exception e) {
			log.error("load ProduceMonsterParaConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
