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

import cn.game.protocol.generated.config.MercenaryNameConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class MercenaryNameManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(MercenaryNameManager.class);

	private static MercenaryNameManager instance = new MercenaryNameManager();
	private static final String xmlFileName = "MercenaryName";
	
	private Map<Integer, MercenaryNameConfig> mercenarynames = new HashMap<>();
	private Map<Integer,List<MercenaryNameConfig>> nameIds = new HashMap<>();

	public static MercenaryNameManager getInstance() {
		return instance;
	}

	private MercenaryNameManager() {
		WatchServiceManager.getInstance().register(this);
	}
	/**
	 * 根据id获取数据，一般用这个方法，如果数据不存在，一般是配置错误，直接抛出异常
	 * 
	 * @param id
	 * @return
	 */
	public MercenaryNameConfig getMercenaryNameConfig(int id) {
		MercenaryNameConfig config = this.mercenarynames.get(id);
		if (config == null) { 
			throw new NullPointerException("【MercenaryName】表的" + "id【" + id + "】不存在"); 
		}
		return config;
	}
	/**
	 * 根据id获取数据，允许返回null
	 * 
	 * @param id
	 * @return
	 */
	public MercenaryNameConfig getMercenaryNameConfigNullable(int id) {
		return this.mercenarynames.get(id);
	}

	public List<MercenaryNameConfig> getNameIdList(int nameId) {
		return this.nameIds.get(nameId);
	}
	public Collection<MercenaryNameConfig> list() {
		return this.mercenarynames.values();
	}

	@Override
	public void load() {

		try {
			ClassLoader classLoader = Thread.currentThread().getClass().getClassLoader();
			if (classLoader == null) {
				classLoader = MercenaryNameManager.class.getClassLoader();
			}
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, MercenaryNameConfig> mercenarynames = new HashMap<>();
			Map<Integer, List<MercenaryNameConfig>> nameIds = new HashMap<>();
			for (Element e : list) {
				MercenaryNameConfig mercenaryname = new MercenaryNameConfig(e);
				List<MercenaryNameConfig> nameIdList = nameIds.get(mercenaryname.getNameId()); 
				if (nameIdList == null){
					nameIdList = new ArrayList<MercenaryNameConfig>(2) ; 
					nameIds.put(mercenaryname.getNameId() ,nameIdList) ; 
				}
				nameIdList.add(mercenaryname) ;
				mercenarynames.put(mercenaryname.getId(), mercenaryname);
			}			

			this.nameIds = com.google.common.collect.ImmutableMap.copyOf(nameIds);			
			this.mercenarynames = com.google.common.collect.ImmutableMap.copyOf(mercenarynames);

			log.info("load MercenaryNameConfig size[{}]", mercenarynames.size());

		} catch (Exception e) {
			throw new RuntimeException("load MercenaryNameConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
