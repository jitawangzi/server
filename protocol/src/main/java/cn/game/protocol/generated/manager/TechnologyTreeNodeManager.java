package cn.game.protocol.generated.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cn.game.protocol.generated.config.TechnologyTreeNodeConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class TechnologyTreeNodeManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(TechnologyTreeNodeManager.class);

	private static TechnologyTreeNodeManager instance = new TechnologyTreeNodeManager();
	private static final String xmlFileName = "TechnologyTreeNode";
	
	private Map<Integer, TechnologyTreeNodeConfig> technologytreenodes = new HashMap<>();

	public static TechnologyTreeNodeManager getInstance() {
		return instance;
	}

	private TechnologyTreeNodeManager() {
		WatchServiceManager.getInstance().register(this);
	}
	/**
	 * 根据id获取数据，一般用这个方法，如果数据不存在，一般是配置错误，直接抛出异常
	 * 
	 * @param id
	 * @return
	 */
	public TechnologyTreeNodeConfig getTechnologyTreeNodeConfig(int id) {
		TechnologyTreeNodeConfig config = this.technologytreenodes.get(id);
		if (config == null) { 
			throw new NullPointerException("【TechnologyTreeNode】表的" + "id【" + id + "】不存在"); 
		}
		return config;
	}
	/**
	 * 根据id获取数据，允许返回null
	 * 
	 * @param id
	 * @return
	 */
	public TechnologyTreeNodeConfig getTechnologyTreeNodeConfigNullable(int id) {
		return this.technologytreenodes.get(id);
	}

	public Collection<TechnologyTreeNodeConfig> list() {
		return this.technologytreenodes.values();
	}

	@Override
	public void load() {

		try {
			ClassLoader classLoader = Thread.currentThread().getClass().getClassLoader();
			if (classLoader == null) {
				classLoader = TechnologyTreeNodeManager.class.getClassLoader();
			}
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, TechnologyTreeNodeConfig> technologytreenodes = new HashMap<>();
			for (Element e : list) {
				TechnologyTreeNodeConfig technologytreenode = new TechnologyTreeNodeConfig(e);
				technologytreenodes.put(technologytreenode.getId(), technologytreenode);
			}			

			this.technologytreenodes = com.google.common.collect.ImmutableMap.copyOf(technologytreenodes);

			log.info("load TechnologyTreeNodeConfig size[{}]", technologytreenodes.size());

		} catch (Exception e) {
			throw new RuntimeException("load TechnologyTreeNodeConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
