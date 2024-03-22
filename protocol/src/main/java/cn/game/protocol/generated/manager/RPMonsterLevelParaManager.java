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

import cn.game.protocol.generated.enume.ResourceEnum;
import cn.game.protocol.generated.config.RPMonsterLevelParaConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class RPMonsterLevelParaManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(RPMonsterLevelParaManager.class);

	private static RPMonsterLevelParaManager instance = new RPMonsterLevelParaManager();
	private static final String xmlFileName = "RPMonsterLevelPara";
	
	private Map<ResourceEnum,List<RPMonsterLevelParaConfig>> types = new HashMap<>();

	public static RPMonsterLevelParaManager getInstance() {
		return instance;
	}

	private RPMonsterLevelParaManager() {
		WatchServiceManager.getInstance().register(this);
	}

	public List<RPMonsterLevelParaConfig> getTypeList(ResourceEnum type) {
		return this.types.get(type);
	}

	public Collection<List<RPMonsterLevelParaConfig>> list() {
		return types.values();
	}


	@Override
	public void load() {

		try {
			ClassLoader classLoader = Thread.currentThread().getClass().getClassLoader();
			if (classLoader == null) {
				classLoader = RPMonsterLevelParaManager.class.getClassLoader();
			}
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<ResourceEnum, List<RPMonsterLevelParaConfig>> types = new HashMap<>();
			for (Element e : list) {
				RPMonsterLevelParaConfig rpmonsterlevelpara = new RPMonsterLevelParaConfig(e);
				List<RPMonsterLevelParaConfig> typeList = types.get(rpmonsterlevelpara.getType()); 
				if (typeList == null){
					typeList = new ArrayList<RPMonsterLevelParaConfig>(2) ; 
					types.put(rpmonsterlevelpara.getType() ,typeList) ; 
				}
				typeList.add(rpmonsterlevelpara) ;
			}			

			this.types = com.google.common.collect.ImmutableMap.copyOf(types);			

			log.info("load RPMonsterLevelParaConfig size[{}]", types.size());

		} catch (Exception e) {
			throw new RuntimeException("load RPMonsterLevelParaConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
