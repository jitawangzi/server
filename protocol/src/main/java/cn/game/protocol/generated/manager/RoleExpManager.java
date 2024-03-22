package cn.game.protocol.generated.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cn.game.protocol.generated.config.RoleExpConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class RoleExpManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(RoleExpManager.class);

	private static RoleExpManager instance = new RoleExpManager();
	private static final String xmlFileName = "RoleExp";
	
	private Map<Integer, RoleExpConfig> roleexps = new HashMap<>();

	public static RoleExpManager getInstance() {
		return instance;
	}

	private RoleExpManager() {
		WatchServiceManager.getInstance().register(this);
	}
	/**
	 * 根据id获取数据，一般用这个方法，如果数据不存在，一般是配置错误，直接抛出异常
	 * 
	 * @param id
	 * @return
	 */
	public RoleExpConfig getRoleExpConfig(int id) {
		RoleExpConfig config = this.roleexps.get(id);
		if (config == null) { 
			throw new NullPointerException("【RoleExp】表的" + "id【" + id + "】不存在"); 
		}
		return config;
	}
	/**
	 * 根据id获取数据，允许返回null
	 * 
	 * @param id
	 * @return
	 */
	public RoleExpConfig getRoleExpConfigNullable(int id) {
		return this.roleexps.get(id);
	}

	public Collection<RoleExpConfig> list() {
		return this.roleexps.values();
	}

	@Override
	public void load() {

		try {
			ClassLoader classLoader = Thread.currentThread().getClass().getClassLoader();
			if (classLoader == null) {
				classLoader = RoleExpManager.class.getClassLoader();
			}
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, RoleExpConfig> roleexps = new HashMap<>();
			for (Element e : list) {
				RoleExpConfig roleexp = new RoleExpConfig(e);
				roleexps.put(roleexp.getId(), roleexp);
			}			

			this.roleexps = com.google.common.collect.ImmutableMap.copyOf(roleexps);

			log.info("load RoleExpConfig size[{}]", roleexps.size());

		} catch (Exception e) {
			throw new RuntimeException("load RoleExpConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
