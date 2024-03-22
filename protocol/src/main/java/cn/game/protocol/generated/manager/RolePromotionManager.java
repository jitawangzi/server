package cn.game.protocol.generated.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cn.game.protocol.generated.config.RolePromotionConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class RolePromotionManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(RolePromotionManager.class);

	private static RolePromotionManager instance = new RolePromotionManager();
	private static final String xmlFileName = "RolePromotion";
	
	private Map<Integer, RolePromotionConfig> rolepromotions = new HashMap<>();

	public static RolePromotionManager getInstance() {
		return instance;
	}

	private RolePromotionManager() {
		WatchServiceManager.getInstance().register(this);
	}
	/**
	 * 根据id获取数据，一般用这个方法，如果数据不存在，一般是配置错误，直接抛出异常
	 * 
	 * @param id
	 * @return
	 */
	public RolePromotionConfig getRolePromotionConfig(int id) {
		RolePromotionConfig config = this.rolepromotions.get(id);
		if (config == null) { 
			throw new NullPointerException("【RolePromotion】表的" + "id【" + id + "】不存在"); 
		}
		return config;
	}
	/**
	 * 根据id获取数据，允许返回null
	 * 
	 * @param id
	 * @return
	 */
	public RolePromotionConfig getRolePromotionConfigNullable(int id) {
		return this.rolepromotions.get(id);
	}

	public Collection<RolePromotionConfig> list() {
		return this.rolepromotions.values();
	}

	@Override
	public void load() {

		try {
			ClassLoader classLoader = Thread.currentThread().getClass().getClassLoader();
			if (classLoader == null) {
				classLoader = RolePromotionManager.class.getClassLoader();
			}
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, RolePromotionConfig> rolepromotions = new HashMap<>();
			for (Element e : list) {
				RolePromotionConfig rolepromotion = new RolePromotionConfig(e);
				rolepromotions.put(rolepromotion.getId(), rolepromotion);
			}			

			this.rolepromotions = com.google.common.collect.ImmutableMap.copyOf(rolepromotions);

			log.info("load RolePromotionConfig size[{}]", rolepromotions.size());

		} catch (Exception e) {
			throw new RuntimeException("load RolePromotionConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
