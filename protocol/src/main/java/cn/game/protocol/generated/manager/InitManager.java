package cn.game.protocol.generated.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cn.game.protocol.generated.config.InitConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class InitManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(InitManager.class);

	private static InitManager instance = new InitManager();
	private static final String xmlFileName = "Init";
	
	private Map<Integer, InitConfig> inits = new HashMap<>();

	public static InitManager getInstance() {
		return instance;
	}

	private InitManager() {
		WatchServiceManager.getInstance().register(this);
	}
	/**
	 * 根据id获取数据，一般用这个方法，如果数据不存在，一般是配置错误，直接抛出异常
	 * 
	 * @param id
	 * @return
	 */
	public InitConfig get(int id) {
		InitConfig config = this.inits.get(id);
		if (config == null) { 
			throw new NullPointerException("【Init】表的" + "id【" + id + "】不存在"); 
		}
		return config;
	}
	/**
	 * 根据id获取数据，允许返回null
	 * 
	 * @param id
	 * @return
	 */
	public InitConfig getNullable(int id) {
		return this.inits.get(id);
	}

	public Collection<InitConfig> list() {
		return this.inits.values();
	}

	@Override
	public void load() {

		try {
			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, InitConfig> inits = new HashMap<>();
			for (Element e : list) {
				InitConfig init = new InitConfig(e);
				inits.put(init.getID(), init);
			}			

			this.inits = com.google.common.collect.ImmutableMap.copyOf(inits);

			log.info("load InitConfig size[{}]", inits.size());

		} catch (Exception e) {
			throw new RuntimeException("load InitConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
