package cn.game.protocol.generated.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cn.game.protocol.generated.config.RoleRisingStarConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class RoleRisingStarManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(RoleRisingStarManager.class);

	private static RoleRisingStarManager instance = new RoleRisingStarManager();
	private static final String xmlFileName = "RoleRisingStar";
	
	private Map<Integer, RoleRisingStarConfig> rolerisingstars = new HashMap<>();

	public static RoleRisingStarManager getInstance() {
		return instance;
	}

	private RoleRisingStarManager() {
		WatchServiceManager.getInstance().register(this);
	}
	/**
	 * 根据id获取数据，一般用这个方法，如果数据不存在，一般是配置错误，直接抛出异常
	 * 
	 * @param id
	 * @return
	 */
	public RoleRisingStarConfig getRoleRisingStarConfig(int id) {
		RoleRisingStarConfig config = this.rolerisingstars.get(id);
		if (config == null) { 
			throw new NullPointerException("【RoleRisingStar】表的" + "id【" + id + "】不存在"); 
		}
		return config;
	}
	/**
	 * 根据id获取数据，允许返回null
	 * 
	 * @param id
	 * @return
	 */
	public RoleRisingStarConfig getRoleRisingStarConfigNullable(int id) {
		return this.rolerisingstars.get(id);
	}

	public Collection<RoleRisingStarConfig> list() {
		return this.rolerisingstars.values();
	}

	@Override
	public void load() {

		try {
			ClassLoader classLoader = Thread.currentThread().getClass().getClassLoader();
			if (classLoader == null) {
				classLoader = RoleRisingStarManager.class.getClassLoader();
			}
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, RoleRisingStarConfig> rolerisingstars = new HashMap<>();
			for (Element e : list) {
				RoleRisingStarConfig rolerisingstar = new RoleRisingStarConfig(e);
				rolerisingstars.put(rolerisingstar.getId(), rolerisingstar);
			}			

			this.rolerisingstars = com.google.common.collect.ImmutableMap.copyOf(rolerisingstars);

			log.info("load RoleRisingStarConfig size[{}]", rolerisingstars.size());

		} catch (Exception e) {
			throw new RuntimeException("load RoleRisingStarConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
