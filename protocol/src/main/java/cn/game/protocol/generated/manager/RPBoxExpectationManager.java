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

import cn.game.protocol.generated.config.RPBoxExpectationConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class RPBoxExpectationManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(RPBoxExpectationManager.class);

	private static RPBoxExpectationManager instance = new RPBoxExpectationManager();
	private static final String xmlFileName = "RPBoxExpectation";
	
	private List<RPBoxExpectationConfig> rpboxexpectations = new ArrayList<RPBoxExpectationConfig>();
	private Map<Integer,List<RPBoxExpectationConfig>> landformss = new HashMap<>();

	public static RPBoxExpectationManager getInstance() {
		return instance;
	}

	private RPBoxExpectationManager() {
		WatchServiceManager.getInstance().register(this);
	}

	public List<RPBoxExpectationConfig> getLandformsList(int landforms) {
		return this.landformss.get(landforms);
	}
	public List<RPBoxExpectationConfig> list() {
		return rpboxexpectations;
	}

	@Override
	public void load() {

		try {
			ClassLoader classLoader = Thread.currentThread().getClass().getClassLoader();
			if (classLoader == null) {
				classLoader = RPBoxExpectationManager.class.getClassLoader();
			}
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			List<RPBoxExpectationConfig> rpboxexpectations = new ArrayList<RPBoxExpectationConfig>();
			Map<Integer, List<RPBoxExpectationConfig>> landformss = new HashMap<>();
			for (Element e : list) {
				RPBoxExpectationConfig rpboxexpectation = new RPBoxExpectationConfig(e);
				List<RPBoxExpectationConfig> landformsList = landformss.get(rpboxexpectation.getLandforms()); 
				if (landformsList == null){
					landformsList = new ArrayList<RPBoxExpectationConfig>(2) ; 
					landformss.put(rpboxexpectation.getLandforms() ,landformsList) ; 
				}
				landformsList.add(rpboxexpectation) ;
				rpboxexpectations.add(rpboxexpectation);
			}			

			this.landformss = com.google.common.collect.ImmutableMap.copyOf(landformss);			
			this.rpboxexpectations = com.google.common.collect.ImmutableList.copyOf(rpboxexpectations);

			log.info("load RPBoxExpectationConfig size[{}]", rpboxexpectations.size());

		} catch (Exception e) {
			throw new RuntimeException("load RPBoxExpectationConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
