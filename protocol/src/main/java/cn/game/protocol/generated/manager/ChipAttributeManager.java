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

import cn.game.protocol.generated.config.ChipAttributeConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class ChipAttributeManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(ChipAttributeManager.class);

	private static ChipAttributeManager instance = new ChipAttributeManager();
	private static final String xmlFileName = "ChipAttribute";
	
	private Map<Integer,List<ChipAttributeConfig>> indexs = new HashMap<>();

	public static ChipAttributeManager getInstance() {
		return instance;
	}

	private ChipAttributeManager() {
		WatchServiceManager.getInstance().register(this);
	}

	public List<ChipAttributeConfig> getIndexList(int index) {
		return this.indexs.get(index);
	}

	public Collection<List<ChipAttributeConfig>> list() {
		return indexs.values();
	}


	@Override
	public void load() {

		try {
			ClassLoader classLoader = Thread.currentThread().getClass().getClassLoader();
			if (classLoader == null) {
				classLoader = ChipAttributeManager.class.getClassLoader();
			}
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, List<ChipAttributeConfig>> indexs = new HashMap<>();
			for (Element e : list) {
				ChipAttributeConfig chipattribute = new ChipAttributeConfig(e);
				List<ChipAttributeConfig> indexList = indexs.get(chipattribute.getIndex()); 
				if (indexList == null){
					indexList = new ArrayList<ChipAttributeConfig>(2) ; 
					indexs.put(chipattribute.getIndex() ,indexList) ; 
				}
				indexList.add(chipattribute) ;
			}			

			this.indexs = com.google.common.collect.ImmutableMap.copyOf(indexs);			

			log.info("load ChipAttributeConfig size[{}]", indexs.size());

		} catch (Exception e) {
			throw new RuntimeException("load ChipAttributeConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
