package cn.game.protocol.generated.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cn.game.protocol.generated.config.RoleFettersConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class RoleFettersManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(RoleFettersManager.class);

	private static RoleFettersManager instance = new RoleFettersManager();
	private static final String xmlFileName = "RoleFetters";
	
	private Map<Integer, RoleFettersConfig> rolefetterss = new HashMap<>();
	private Map<Integer,List<RoleFettersConfig>> types = new HashMap<>();
	private Map<Long,List<RoleFettersConfig>> roleId1roleId2s = new HashMap<>();

	public static RoleFettersManager getInstance() {
		return instance;
	}

	private RoleFettersManager() {
		WatchServiceManager.getInstance().register(this);
	}
	/**
	 * 根据id获取数据，一般用这个方法，如果数据不存在，一般是配置错误，直接抛出异常
	 * 
	 * @param id
	 * @return
	 */
	public RoleFettersConfig getRoleFettersConfig(int id) {
		RoleFettersConfig config = this.rolefetterss.get(id);
		if (config == null) { 
			throw new NullPointerException("【RoleFetters】表的" + "id【" + id + "】不存在"); 
		}
		return config;
	}
	/**
	 * 根据id获取数据，允许返回null
	 * 
	 * @param id
	 * @return
	 */
	public RoleFettersConfig getRoleFettersConfigNullable(int id) {
		return this.rolefetterss.get(id);
	}

	public List<RoleFettersConfig> getTypeList(int type) {
		return this.types.get(type);
	}
	private long hashIndex1(int roleId1,int roleId2) {
		if (roleId1 > 999999) {
			throw new IllegalArgumentException("roleId1 联合索引范围超过最大值999999");
		}
		if (roleId2 > 999999) {
			throw new IllegalArgumentException("roleId2 联合索引范围超过最大值999999");
		}
		long result = 0;
		result = result | (long) roleId1 << 43;
		result = result | (long) roleId2 << 22;
		return result;
	}

  	public List<RoleFettersConfig> getRoleId1roleId2List(int roleId1,int roleId2) {
		return this.roleId1roleId2s.get(hashIndex1(roleId1,roleId2));
	}
	public Collection<RoleFettersConfig> list() {
		return this.rolefetterss.values();
	}

	@Override
	public void load() {

		try {
			ClassLoader classLoader = Thread.currentThread().getClass().getClassLoader();
			if (classLoader == null) {
				classLoader = RoleFettersManager.class.getClassLoader();
			}
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, RoleFettersConfig> rolefetterss = new HashMap<>();
			Map<Integer, List<RoleFettersConfig>> types = new HashMap<>();
  			Map<Long, List<RoleFettersConfig>> roleId1roleId2s = new HashMap<>();
			for (Element e : list) {
				RoleFettersConfig rolefetters = new RoleFettersConfig(e);
				List<RoleFettersConfig> typeList = types.get(rolefetters.getType()); 
				if (typeList == null){
					typeList = new ArrayList<RoleFettersConfig>(2) ; 
					types.put(rolefetters.getType() ,typeList) ; 
				}
				typeList.add(rolefetters) ;
  				List<RoleFettersConfig> roleId1roleId2sList = roleId1roleId2s.get(hashIndex1(rolefetters.getRoleId1(),rolefetters.getRoleId2())); 
				if (roleId1roleId2sList == null){
					roleId1roleId2sList = new ArrayList<RoleFettersConfig>(2) ; 
					roleId1roleId2s.put(hashIndex1(rolefetters.getRoleId1(),rolefetters.getRoleId2()) ,roleId1roleId2sList) ; 
				}
				roleId1roleId2sList.add(rolefetters) ;
				rolefetterss.put(rolefetters.getId(), rolefetters);
			}			
			types.forEach((k, v) -> java.util.Collections.sort(v));			
  			roleId1roleId2s.forEach((k, v) -> java.util.Collections.sort(v));

			this.types = com.google.common.collect.ImmutableMap.copyOf(types);			
  			this.roleId1roleId2s = com.google.common.collect.ImmutableMap.copyOf(roleId1roleId2s);
			this.rolefetterss = com.google.common.collect.ImmutableMap.copyOf(rolefetterss);

			log.info("load RoleFettersConfig size[{}]", rolefetterss.size());

		} catch (Exception e) {
			throw new RuntimeException("load RoleFettersConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
