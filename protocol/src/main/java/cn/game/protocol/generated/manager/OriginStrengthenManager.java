package cn.game.protocol.generated.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cn.game.protocol.generated.config.OriginStrengthenConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class OriginStrengthenManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(OriginStrengthenManager.class);

	private static OriginStrengthenManager instance = new OriginStrengthenManager();
	private static final String xmlFileName = "OriginStrengthen";
	
	private Map<Integer, OriginStrengthenConfig> originstrengthens = new HashMap<>();

	public static OriginStrengthenManager getInstance() {
		return instance;
	}

	private OriginStrengthenManager() {
		WatchServiceManager.getInstance().register(this);
	}
	/**
	 * 根据id获取数据，一般用这个方法，如果数据不存在，一般是配置错误，直接抛出异常
	 * 
	 * @param id
	 * @return
	 */
	public OriginStrengthenConfig getOriginStrengthenConfig(int id) {
		OriginStrengthenConfig config = this.originstrengthens.get(id);
		if (config == null) { 
			throw new NullPointerException("【OriginStrengthen】表的" + "id【" + id + "】不存在"); 
		}
		return config;
	}
	/**
	 * 根据id获取数据，允许返回null
	 * 
	 * @param id
	 * @return
	 */
	public OriginStrengthenConfig getOriginStrengthenConfigNullable(int id) {
		return this.originstrengthens.get(id);
	}

	public Collection<OriginStrengthenConfig> list() {
		return this.originstrengthens.values();
	}

	@Override
	public void load() {

		try {
			ClassLoader classLoader = Thread.currentThread().getClass().getClassLoader();
			if (classLoader == null) {
				classLoader = OriginStrengthenManager.class.getClassLoader();
			}
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, OriginStrengthenConfig> originstrengthens = new HashMap<>();
			for (Element e : list) {
				OriginStrengthenConfig originstrengthen = new OriginStrengthenConfig(e);
				originstrengthens.put(originstrengthen.getId(), originstrengthen);
			}			

			this.originstrengthens = com.google.common.collect.ImmutableMap.copyOf(originstrengthens);

			log.info("load OriginStrengthenConfig size[{}]", originstrengthens.size());

		} catch (Exception e) {
			throw new RuntimeException("load OriginStrengthenConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
