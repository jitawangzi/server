package cn.game.protocol.generated.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cn.game.protocol.generated.config.ProduceBattleSettlementBaseConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class ProduceBattleSettlementBaseManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(ProduceBattleSettlementBaseManager.class);

	private static ProduceBattleSettlementBaseManager instance = new ProduceBattleSettlementBaseManager();
	private static final String xmlFileName = "ProduceBattleSettlementBase";
	
	private Map<Integer, ProduceBattleSettlementBaseConfig> producebattlesettlementbases = new HashMap<>();

	public static ProduceBattleSettlementBaseManager getInstance() {
		return instance;
	}

	private ProduceBattleSettlementBaseManager() {
		WatchServiceManager.getInstance().register(this);
	}
	/**
	 * 根据id获取数据，一般用这个方法，如果数据不存在，一般是配置错误，直接抛出异常
	 * 
	 * @param id
	 * @return
	 */
	public ProduceBattleSettlementBaseConfig getProduceBattleSettlementBaseConfig(int id) {
		ProduceBattleSettlementBaseConfig config = this.producebattlesettlementbases.get(id);
		if (config == null) { 
			throw new NullPointerException("【ProduceBattleSettlementBase】表的" + "id【" + id + "】不存在"); 
		}
		return config;
	}
	/**
	 * 根据id获取数据，允许返回null
	 * 
	 * @param id
	 * @return
	 */
	public ProduceBattleSettlementBaseConfig getProduceBattleSettlementBaseConfigNullable(int id) {
		return this.producebattlesettlementbases.get(id);
	}

	public Collection<ProduceBattleSettlementBaseConfig> list() {
		return this.producebattlesettlementbases.values();
	}

	@Override
	public void load() {

		try {
			ClassLoader classLoader = Thread.currentThread().getClass().getClassLoader();
			if (classLoader == null) {
				classLoader = ProduceBattleSettlementBaseManager.class.getClassLoader();
			}
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, ProduceBattleSettlementBaseConfig> map = new HashMap<>();
			for (Element e : list) {
				ProduceBattleSettlementBaseConfig producebattlesettlementbase = new ProduceBattleSettlementBaseConfig(e);
				map.put(producebattlesettlementbase.getId(), producebattlesettlementbase);
			}
			
			this.producebattlesettlementbases = map;

			log.info("load ProduceBattleSettlementBaseConfig size[{}]", map.size());

		} catch (Exception e) {
			log.error("load ProduceBattleSettlementBaseConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
