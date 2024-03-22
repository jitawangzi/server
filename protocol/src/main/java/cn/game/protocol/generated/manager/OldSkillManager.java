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

import cn.game.protocol.generated.config.OldSkillConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class OldSkillManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(OldSkillManager.class);

	private static OldSkillManager instance = new OldSkillManager();
	private static final String xmlFileName = "Skill";
	
	private Map<Integer, OldSkillConfig> skills = new HashMap<>();
	private Map<Long,List<OldSkillConfig>> typeoccupations = new HashMap<>();

	public static OldSkillManager getInstance() {
		return instance;
	}

	private OldSkillManager() {
		WatchServiceManager.getInstance().register(this);
	}
	/**
	 * 根据id获取数据，一般用这个方法，如果数据不存在，一般是配置错误，直接抛出异常
	 * 
	 * @param id
	 * @return
	 */
	public OldSkillConfig getSkillConfig(int id) {
		OldSkillConfig config = this.skills.get(id);
		if (config == null) { 
			throw new NullPointerException("【Skill】表的" + "id【" + id + "】不存在"); 
		}
		return config;
	}
	/**
	 * 根据id获取数据，允许返回null
	 * 
	 * @param id
	 * @return
	 */
	public OldSkillConfig getSkillConfigNullable(int id) {
		return this.skills.get(id);
	}

	private long hashIndex1(int type,int occupation) {
		if (type > 999999) {
			throw new IllegalArgumentException("type 联合索引范围超过最大值999999");
		}
		if (occupation > 999999) {
			throw new IllegalArgumentException("occupation 联合索引范围超过最大值999999");
		}
		long result = 0;
		result = result | (long) type << 43;
		result = result | (long) occupation << 22;
		return result;
	}

  	public List<OldSkillConfig> getTypeoccupationList(int type,int occupation) {
		return this.typeoccupations.get(hashIndex1(type,occupation));
	}
	public Collection<OldSkillConfig> list() {
		return this.skills.values();
	}

	@Override
	public void load() {

		try {
			ClassLoader classLoader = Thread.currentThread().getClass().getClassLoader();
			if (classLoader == null) {
				classLoader = OldSkillManager.class.getClassLoader();
			}
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, OldSkillConfig> skills = new HashMap<>();
  			Map<Long, List<OldSkillConfig>> typeoccupations = new HashMap<>();
			for (Element e : list) {
				OldSkillConfig skill = new OldSkillConfig(e);
  				List<OldSkillConfig> typeoccupationsList = typeoccupations.get(hashIndex1(skill.getType(),skill.getOccupation())); 
				if (typeoccupationsList == null){
					typeoccupationsList = new ArrayList<OldSkillConfig>(2) ; 
					typeoccupations.put(hashIndex1(skill.getType(),skill.getOccupation()) ,typeoccupationsList) ; 
				}
				typeoccupationsList.add(skill) ;
				skills.put(skill.getId(), skill);
			}			

  			this.typeoccupations = com.google.common.collect.ImmutableMap.copyOf(typeoccupations);
			this.skills = com.google.common.collect.ImmutableMap.copyOf(skills);

			log.info("load SkillConfig size[{}]", skills.size());

		} catch (Exception e) {
			throw new RuntimeException("load SkillConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
