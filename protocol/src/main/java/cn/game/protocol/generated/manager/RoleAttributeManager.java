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

import cn.game.protocol.generated.enume.AttributeTypeEnum;
import cn.game.protocol.generated.enume.AttributeSubTypeEnum;
import cn.game.protocol.generated.enume.AttributeTypeEnum;
import cn.game.protocol.generated.enume.AttributeSubTypeEnum;
import cn.game.protocol.generated.config.RoleAttributeConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class RoleAttributeManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(RoleAttributeManager.class);

	private static RoleAttributeManager instance = new RoleAttributeManager();
	private static final String xmlFileName = "RoleAttribute";
	
	private Map<Integer, RoleAttributeConfig> roleattributes = new HashMap<>();
	private Map<AttributeTypeEnum,List<RoleAttributeConfig>> types = new HashMap<>();
	private Map<AttributeSubTypeEnum,List<RoleAttributeConfig>> subTypes = new HashMap<>();
	private Map<Long,List<RoleAttributeConfig>> typesubTypes = new HashMap<>();

	public static RoleAttributeManager getInstance() {
		return instance;
	}

	private RoleAttributeManager() {
		WatchServiceManager.getInstance().register(this);
	}
	/**
	 * 根据id获取数据，一般用这个方法，如果数据不存在，一般是配置错误，直接抛出异常
	 * 
	 * @param id
	 * @return
	 */
	public RoleAttributeConfig getRoleAttributeConfig(int id) {
		RoleAttributeConfig config = this.roleattributes.get(id);
		if (config == null) { 
			throw new NullPointerException("【RoleAttribute】表的" + "id【" + id + "】不存在"); 
		}
		return config;
	}
	/**
	 * 根据id获取数据，允许返回null
	 * 
	 * @param id
	 * @return
	 */
	public RoleAttributeConfig getRoleAttributeConfigNullable(int id) {
		return this.roleattributes.get(id);
	}

	public List<RoleAttributeConfig> getTypeList(AttributeTypeEnum type) {
		return this.types.get(type);
	}
	public List<RoleAttributeConfig> getSubTypeList(AttributeSubTypeEnum subType) {
		return this.subTypes.get(subType);
	}
	private long hashIndex1(AttributeTypeEnum type,AttributeSubTypeEnum subType) {
		if (type.getId() > 999999) {
			throw new IllegalArgumentException("type 联合索引范围超过最大值999999");
		}
		if (subType.getId() > 999999) {
			throw new IllegalArgumentException("subType 联合索引范围超过最大值999999");
		}
		long result = 0;
		result = result | (long) type.getId() << 43;
		result = result | (long) subType.getId() << 22;
		return result;
	}

  	public List<RoleAttributeConfig> getTypesubTypeList(AttributeTypeEnum type,AttributeSubTypeEnum subType) {
		return this.typesubTypes.get(hashIndex1(type,subType));
	}
	public Collection<RoleAttributeConfig> list() {
		return this.roleattributes.values();
	}

	@Override
	public void load() {

		try {
			ClassLoader classLoader = Thread.currentThread().getClass().getClassLoader();
			if (classLoader == null) {
				classLoader = RoleAttributeManager.class.getClassLoader();
			}
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, RoleAttributeConfig> roleattributes = new HashMap<>();
			Map<AttributeTypeEnum, List<RoleAttributeConfig>> types = new HashMap<>();
			Map<AttributeSubTypeEnum, List<RoleAttributeConfig>> subTypes = new HashMap<>();
  			Map<Long, List<RoleAttributeConfig>> typesubTypes = new HashMap<>();
			for (Element e : list) {
				RoleAttributeConfig roleattribute = new RoleAttributeConfig(e);
				List<RoleAttributeConfig> typeList = types.get(roleattribute.getType()); 
				if (typeList == null){
					typeList = new ArrayList<RoleAttributeConfig>(2) ; 
					types.put(roleattribute.getType() ,typeList) ; 
				}
				typeList.add(roleattribute) ;
				List<RoleAttributeConfig> subTypeList = subTypes.get(roleattribute.getSubType()); 
				if (subTypeList == null){
					subTypeList = new ArrayList<RoleAttributeConfig>(2) ; 
					subTypes.put(roleattribute.getSubType() ,subTypeList) ; 
				}
				subTypeList.add(roleattribute) ;
  				List<RoleAttributeConfig> typesubTypesList = typesubTypes.get(hashIndex1(roleattribute.getType(),roleattribute.getSubType())); 
				if (typesubTypesList == null){
					typesubTypesList = new ArrayList<RoleAttributeConfig>(2) ; 
					typesubTypes.put(hashIndex1(roleattribute.getType(),roleattribute.getSubType()) ,typesubTypesList) ; 
				}
				typesubTypesList.add(roleattribute) ;
				roleattributes.put(roleattribute.getId(), roleattribute);
			}			

			this.types = com.google.common.collect.ImmutableMap.copyOf(types);			
			this.subTypes = com.google.common.collect.ImmutableMap.copyOf(subTypes);			
  			this.typesubTypes = com.google.common.collect.ImmutableMap.copyOf(typesubTypes);
			this.roleattributes = com.google.common.collect.ImmutableMap.copyOf(roleattributes);

			log.info("load RoleAttributeConfig size[{}]", roleattributes.size());

		} catch (Exception e) {
			throw new RuntimeException("load RoleAttributeConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
