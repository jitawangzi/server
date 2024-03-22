package cn.game.protocol.generated.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cn.game.protocol.generated.config.OldItemConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class OldItemManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(OldItemManager.class);

	private static OldItemManager instance = new OldItemManager();
	private static final String xmlFileName = "Item";
	
	private Map<Integer, OldItemConfig> items = new HashMap<>();

	public static OldItemManager getInstance() {
		return instance;
	}

	private OldItemManager() {
		WatchServiceManager.getInstance().register(this);
	}
	/**
	 * 根据id获取数据，一般用这个方法，如果数据不存在，一般是配置错误，直接抛出异常
	 * 
	 * @param id
	 * @return
	 */
	public OldItemConfig getItemConfig(int id) {
		OldItemConfig config = this.items.get(id);
		if (config == null) { 
			throw new NullPointerException("【Item】表的" + "id【" + id + "】不存在"); 
		}
		return config;
	}
	/**
	 * 根据id获取数据，允许返回null
	 * 
	 * @param id
	 * @return
	 */
	public OldItemConfig getItemConfigNullable(int id) {
		return this.items.get(id);
	}

	public Collection<OldItemConfig> list() {
		return this.items.values();
	}

	@Override
	public void load() {

		try {
			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, OldItemConfig> items = new HashMap<>();
			for (Element e : list) {
				OldItemConfig item = new OldItemConfig(e);
				items.put(item.getId(), item);
			}			

			this.items = com.google.common.collect.ImmutableMap.copyOf(items);

			log.info("load ItemConfig size[{}]", items.size());

		} catch (Exception e) {
			throw new RuntimeException("load ItemConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
