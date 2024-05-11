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

import cn.game.protocol.generated.config.HeishiConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class HeishiManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(HeishiManager.class);

	private static HeishiManager instance = new HeishiManager();
	private static final String xmlFileName = "Heishi";
	
	/** 普通索引 */
	private Map<Integer,List<HeishiConfig>> Types = new HashMap<>();

	public static HeishiManager instance() {
		return instance;
	}
	private HeishiManager() {
		WatchServiceManager.getInstance().register(this);
	}

	public List<HeishiConfig> getTypeList(int Type) {
		return this.Types.get(Type);
	}
	public Map<Integer,List<HeishiConfig>> getTypes() {
		return this.Types;
	}
	/**
	 * 获取所有数据
	 * @return
	 */
	public Collection<List<HeishiConfig>> list() {
		return Types.values();
	}
	@Override
	public void load() {
		try {
			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, List<HeishiConfig>> Types = new HashMap<>();
			for (Element e : list) {
				HeishiConfig heishi = new HeishiConfig(e);
				List<HeishiConfig> TypeList = Types.get(heishi.Type); 
				if (TypeList == null){
					TypeList = new ArrayList<HeishiConfig>(2) ; 
					Types.put(heishi.Type ,TypeList) ; 
				}
				TypeList.add(heishi) ;
			}			

			this.Types = com.google.common.collect.ImmutableMap.copyOf(Types);			

			log.info("load HeishiConfig size[{}]", Types.values().stream().flatMap(List::stream).count());

		} catch (Exception e) {
			throw new RuntimeException("load HeishiConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
