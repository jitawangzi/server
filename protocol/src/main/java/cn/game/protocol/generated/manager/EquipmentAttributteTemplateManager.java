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

import cn.game.protocol.generated.config.EquipmentAttributteTemplateConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class EquipmentAttributteTemplateManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(EquipmentAttributteTemplateManager.class);

	private static EquipmentAttributteTemplateManager instance = new EquipmentAttributteTemplateManager();
	private static final String xmlFileName = "EquipmentAttributteTemplate";
	
	private Map<Integer, EquipmentAttributteTemplateConfig> equipmentattributtetemplates = new HashMap<>();
	private Map<Long,List<EquipmentAttributteTemplateConfig>> typestrengths = new HashMap<>();

	public static EquipmentAttributteTemplateManager getInstance() {
		return instance;
	}

	private EquipmentAttributteTemplateManager() {
		WatchServiceManager.getInstance().register(this);
	}
	/**
	 * 根据id获取数据，一般用这个方法，如果数据不存在，一般是配置错误，直接抛出异常
	 * 
	 * @param id
	 * @return
	 */
	public EquipmentAttributteTemplateConfig getEquipmentAttributteTemplateConfig(int id) {
		EquipmentAttributteTemplateConfig config = this.equipmentattributtetemplates.get(id);
		if (config == null) { 
			throw new NullPointerException("【EquipmentAttributteTemplate】表的" + "id【" + id + "】不存在"); 
		}
		return config;
	}
	/**
	 * 根据id获取数据，允许返回null
	 * 
	 * @param id
	 * @return
	 */
	public EquipmentAttributteTemplateConfig getEquipmentAttributteTemplateConfigNullable(int id) {
		return this.equipmentattributtetemplates.get(id);
	}

	private long hashIndex1(int type,int strength) {
		if (type > 999999) {
			throw new IllegalArgumentException("type 联合索引范围超过最大值999999");
		}
		if (strength > 999999) {
			throw new IllegalArgumentException("strength 联合索引范围超过最大值999999");
		}
		long result = 0;
		result = result | (long) type << 43;
		result = result | (long) strength << 22;
		return result;
	}

  	public List<EquipmentAttributteTemplateConfig> getTypestrengthList(int type,int strength) {
		return this.typestrengths.get(hashIndex1(type,strength));
	}
	public Collection<EquipmentAttributteTemplateConfig> list() {
		return this.equipmentattributtetemplates.values();
	}

	@Override
	public void load() {

		try {
			ClassLoader classLoader = Thread.currentThread().getClass().getClassLoader();
			if (classLoader == null) {
				classLoader = EquipmentAttributteTemplateManager.class.getClassLoader();
			}
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, EquipmentAttributteTemplateConfig> equipmentattributtetemplates = new HashMap<>();
  			Map<Long, List<EquipmentAttributteTemplateConfig>> typestrengths = new HashMap<>();
			for (Element e : list) {
				EquipmentAttributteTemplateConfig equipmentattributtetemplate = new EquipmentAttributteTemplateConfig(e);
  				List<EquipmentAttributteTemplateConfig> typestrengthsList = typestrengths.get(hashIndex1(equipmentattributtetemplate.getType(),equipmentattributtetemplate.getStrength())); 
				if (typestrengthsList == null){
					typestrengthsList = new ArrayList<EquipmentAttributteTemplateConfig>(2) ; 
					typestrengths.put(hashIndex1(equipmentattributtetemplate.getType(),equipmentattributtetemplate.getStrength()) ,typestrengthsList) ; 
				}
				typestrengthsList.add(equipmentattributtetemplate) ;
				equipmentattributtetemplates.put(equipmentattributtetemplate.getId(), equipmentattributtetemplate);
			}			

  			this.typestrengths = com.google.common.collect.ImmutableMap.copyOf(typestrengths);
			this.equipmentattributtetemplates = com.google.common.collect.ImmutableMap.copyOf(equipmentattributtetemplates);

			log.info("load EquipmentAttributteTemplateConfig size[{}]", equipmentattributtetemplates.size());

		} catch (Exception e) {
			throw new RuntimeException("load EquipmentAttributteTemplateConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
