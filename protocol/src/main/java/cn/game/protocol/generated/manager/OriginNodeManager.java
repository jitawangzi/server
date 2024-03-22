package cn.game.protocol.generated.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cn.game.protocol.generated.config.OriginNodeConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class OriginNodeManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(OriginNodeManager.class);

	private static OriginNodeManager instance = new OriginNodeManager();
	private static final String xmlFileName = "OriginNode";
	
	private Map<Integer, OriginNodeConfig> originnodes = new HashMap<>();

	public static OriginNodeManager getInstance() {
		return instance;
	}

	private OriginNodeManager() {
		WatchServiceManager.getInstance().register(this);
	}
	/**
	 * 根据id获取数据，一般用这个方法，如果数据不存在，一般是配置错误，直接抛出异常
	 * 
	 * @param id
	 * @return
	 */
	public OriginNodeConfig getOriginNodeConfig(int id) {
		OriginNodeConfig config = this.originnodes.get(id);
		if (config == null) { 
			throw new NullPointerException("【OriginNode】表的" + "id【" + id + "】不存在"); 
		}
		return config;
	}
	/**
	 * 根据id获取数据，允许返回null
	 * 
	 * @param id
	 * @return
	 */
	public OriginNodeConfig getOriginNodeConfigNullable(int id) {
		return this.originnodes.get(id);
	}

	public Collection<OriginNodeConfig> list() {
		return this.originnodes.values();
	}

	@Override
	public void load() {

		try {
			ClassLoader classLoader = Thread.currentThread().getClass().getClassLoader();
			if (classLoader == null) {
				classLoader = OriginNodeManager.class.getClassLoader();
			}
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, OriginNodeConfig> originnodes = new HashMap<>();
			for (Element e : list) {
				OriginNodeConfig originnode = new OriginNodeConfig(e);
				originnodes.put(originnode.getId(), originnode);
			}			

			this.originnodes = com.google.common.collect.ImmutableMap.copyOf(originnodes);

			log.info("load OriginNodeConfig size[{}]", originnodes.size());

		} catch (Exception e) {
			throw new RuntimeException("load OriginNodeConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
