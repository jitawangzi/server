package cn.game.protocol.generated.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cn.game.protocol.generated.config.RPMonsterTypeParaConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class RPMonsterTypeParaManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(RPMonsterTypeParaManager.class);

	private static RPMonsterTypeParaManager instance = new RPMonsterTypeParaManager();
	private static final String xmlFileName = "RPMonsterTypePara";
	
	private Map<Integer, RPMonsterTypeParaConfig> rpmonstertypeparas = new HashMap<>();

	public static RPMonsterTypeParaManager getInstance() {
		return instance;
	}

	private RPMonsterTypeParaManager() {
		WatchServiceManager.getInstance().register(this);
	}
	/**
	 * 根据id获取数据，一般用这个方法，如果数据不存在，一般是配置错误，直接抛出异常
	 * 
	 * @param id
	 * @return
	 */
	public RPMonsterTypeParaConfig getRPMonsterTypeParaConfig(int id) {
		RPMonsterTypeParaConfig config = this.rpmonstertypeparas.get(id);
		if (config == null) { 
			throw new NullPointerException("【RPMonsterTypePara】表的" + "id【" + id + "】不存在"); 
		}
		return config;
	}
	/**
	 * 根据id获取数据，允许返回null
	 * 
	 * @param id
	 * @return
	 */
	public RPMonsterTypeParaConfig getRPMonsterTypeParaConfigNullable(int id) {
		return this.rpmonstertypeparas.get(id);
	}

	public Collection<RPMonsterTypeParaConfig> list() {
		return this.rpmonstertypeparas.values();
	}

	@Override
	public void load() {

		try {
			ClassLoader classLoader = Thread.currentThread().getClass().getClassLoader();
			if (classLoader == null) {
				classLoader = RPMonsterTypeParaManager.class.getClassLoader();
			}
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, RPMonsterTypeParaConfig> rpmonstertypeparas = new HashMap<>();
			for (Element e : list) {
				RPMonsterTypeParaConfig rpmonstertypepara = new RPMonsterTypeParaConfig(e);
				rpmonstertypeparas.put(rpmonstertypepara.getId(), rpmonstertypepara);
			}			

			this.rpmonstertypeparas = com.google.common.collect.ImmutableMap.copyOf(rpmonstertypeparas);

			log.info("load RPMonsterTypeParaConfig size[{}]", rpmonstertypeparas.size());

		} catch (Exception e) {
			throw new RuntimeException("load RPMonsterTypeParaConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
