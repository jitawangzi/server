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

import cn.game.protocol.generated.config.RandomAttributeConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class RandomAttributeManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(RandomAttributeManager.class);

	private static RandomAttributeManager instance = new RandomAttributeManager();
	private static final String xmlFileName = "RandomAttribute";
	
	/** 普通索引 */
	private Map<Integer,List<RandomAttributeConfig>> RandomAttributeIds = new HashMap<>();

	public static RandomAttributeManager instance() {
		return instance;
	}
	private RandomAttributeManager() {
		WatchServiceManager.getInstance().register(this);
	}

	public List<RandomAttributeConfig> getRandomAttributeIdList(int RandomAttributeId) {
		return this.RandomAttributeIds.get(RandomAttributeId);
	}
	/**
	 * 获取所有数据
	 * @return
	 */
	public Collection<List<RandomAttributeConfig>> list() {
		return RandomAttributeIds.values();
	}
	@Override
	public void load() {
		try {
			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, List<RandomAttributeConfig>> RandomAttributeIds = new HashMap<>();
			for (Element e : list) {
				RandomAttributeConfig randomattribute = new RandomAttributeConfig(e);
				List<RandomAttributeConfig> RandomAttributeIdList = RandomAttributeIds.get(randomattribute.RandomAttributeId); 
				if (RandomAttributeIdList == null){
					RandomAttributeIdList = new ArrayList<RandomAttributeConfig>(2) ; 
					RandomAttributeIds.put(randomattribute.RandomAttributeId ,RandomAttributeIdList) ; 
				}
				RandomAttributeIdList.add(randomattribute) ;
			}			

			this.RandomAttributeIds = com.google.common.collect.ImmutableMap.copyOf(RandomAttributeIds);			

			log.info("load RandomAttributeConfig size[{}]", RandomAttributeIds.values().stream().flatMap(List::stream).count());

		} catch (Exception e) {
			throw new RuntimeException("load RandomAttributeConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
