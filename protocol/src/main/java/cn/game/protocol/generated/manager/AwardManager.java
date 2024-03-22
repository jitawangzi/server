package cn.game.protocol.generated.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cn.game.protocol.generated.config.AwardConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class AwardManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(AwardManager.class);

	private static AwardManager instance = new AwardManager();
	private static final String xmlFileName = "Award";
	
	private Map<Integer, AwardConfig> awards = new HashMap<>();

	public static AwardManager getInstance() {
		return instance;
	}

	private AwardManager() {
		WatchServiceManager.getInstance().register(this);
	}
	/**
	 * 根据id获取数据，一般用这个方法，如果数据不存在，一般是配置错误，直接抛出异常
	 * 
	 * @param id
	 * @return
	 */
	public AwardConfig get(int id) {
		AwardConfig config = this.awards.get(id);
		if (config == null) { 
			throw new NullPointerException("【Award】表的" + "id【" + id + "】不存在"); 
		}
		return config;
	}
	/**
	 * 根据id获取数据，允许返回null
	 * 
	 * @param id
	 * @return
	 */
	public AwardConfig getNullable(int id) {
		return this.awards.get(id);
	}

	public Collection<AwardConfig> list() {
		return this.awards.values();
	}

	@Override
	public void load() {

		try {
			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, AwardConfig> awards = new HashMap<>();
			for (Element e : list) {
				AwardConfig award = new AwardConfig(e);
				awards.put(award.getID(), award);
			}			

			this.awards = com.google.common.collect.ImmutableMap.copyOf(awards);

			log.info("load AwardConfig size[{}]", awards.size());

		} catch (Exception e) {
			throw new RuntimeException("load AwardConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
