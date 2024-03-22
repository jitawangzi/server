package cn.game.protocol.generated.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cn.game.protocol.generated.config.CoreDecomposeConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class CoreDecomposeManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(CoreDecomposeManager.class);

	private static CoreDecomposeManager instance = new CoreDecomposeManager();
	private static final String xmlFileName = "CoreDecompose";
	
	private Map<Integer, CoreDecomposeConfig> coredecomposes = new HashMap<>();

	public static CoreDecomposeManager getInstance() {
		return instance;
	}

	private CoreDecomposeManager() {
		WatchServiceManager.getInstance().register(this);
	}
	/**
	 * 根据id获取数据，一般用这个方法，如果数据不存在，一般是配置错误，直接抛出异常
	 * 
	 * @param id
	 * @return
	 */
	public CoreDecomposeConfig getCoreDecomposeConfig(int id) {
		CoreDecomposeConfig config = this.coredecomposes.get(id);
		if (config == null) { 
			throw new NullPointerException("【CoreDecompose】表的" + "id【" + id + "】不存在"); 
		}
		return config;
	}
	/**
	 * 根据id获取数据，允许返回null
	 * 
	 * @param id
	 * @return
	 */
	public CoreDecomposeConfig getCoreDecomposeConfigNullable(int id) {
		return this.coredecomposes.get(id);
	}

	public Collection<CoreDecomposeConfig> list() {
		return this.coredecomposes.values();
	}

	@Override
	public void load() {

		try {
			ClassLoader classLoader = Thread.currentThread().getClass().getClassLoader();
			if (classLoader == null) {
				classLoader = CoreDecomposeManager.class.getClassLoader();
			}
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, CoreDecomposeConfig> coredecomposes = new HashMap<>();
			for (Element e : list) {
				CoreDecomposeConfig coredecompose = new CoreDecomposeConfig(e);
				coredecomposes.put(coredecompose.getId(), coredecompose);
			}			

			this.coredecomposes = com.google.common.collect.ImmutableMap.copyOf(coredecomposes);

			log.info("load CoreDecomposeConfig size[{}]", coredecomposes.size());

		} catch (Exception e) {
			throw new RuntimeException("load CoreDecomposeConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
