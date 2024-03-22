package cn.game.protocol.generated.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cn.game.protocol.generated.config.ConsumeConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class ConsumeManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(ConsumeManager.class);

	private static ConsumeManager instance = new ConsumeManager();
	private static final String xmlFileName = "Consume";
	
	/** 总数据，按id取值 */
	private Map<Integer, ConsumeConfig> consumes = new HashMap<>();

	public static ConsumeManager instance() {
		return instance;
	}
	private ConsumeManager() {
		WatchServiceManager.getInstance().register(this);
	}
	/**
	 * 根据id获取数据，一般用这个方法，如果数据不存在，一般是配置错误，直接抛出异常
	 * 
	 * @param id
	 * @return
	 */
	public ConsumeConfig get(int id) {
		ConsumeConfig config = this.consumes.get(id);
		if (config == null) { 
			throw new NullPointerException("【Consume】表的" + "id【" + id + "】不存在"); 
		}
		return config;
	}
	/**
	 * 根据id获取数据，允许返回null
	 * 
	 * @param id
	 * @return
	 */
	public ConsumeConfig getNullable(int id) {
		return this.consumes.get(id);
	}

	/**
	 * 获取所有数据
	 * @return
	 */
	public Collection<ConsumeConfig> list() {
		return this.consumes.values();
	}
	@Override
	public void load() {
		try {
			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, ConsumeConfig> consumes = new HashMap<>();
			for (Element e : list) {
				ConsumeConfig consume = new ConsumeConfig(e);
				ConsumeConfig old = consumes.put(consume.ID, consume);
				if (old != null) {
					throw new IllegalArgumentException("[ConsumeConfig]表存在重复的数据id： " + old.ID);
				}
			}			

			this.consumes = com.google.common.collect.ImmutableMap.copyOf(consumes);

			log.info("load ConsumeConfig size[{}]", consumes.size());

		} catch (Exception e) {
			throw new RuntimeException("load ConsumeConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
