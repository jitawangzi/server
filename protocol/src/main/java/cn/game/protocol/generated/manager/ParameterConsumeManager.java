package cn.game.protocol.generated.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cn.game.protocol.generated.config.ParameterConsumeConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class ParameterConsumeManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(ParameterConsumeManager.class);

	private static ParameterConsumeManager instance = new ParameterConsumeManager();
	private static final String xmlFileName = "ParameterConsume";
	
	/** 唯一索引 */
	private Map<Long,ParameterConsumeConfig> typetypeParams = new HashMap<>();

	public static ParameterConsumeManager instance() {
		return instance;
	}
	private ParameterConsumeManager() {
		WatchServiceManager.getInstance().register(this);
	}

	private long hashIndexUnique1(int type,int typeParam) {
		if (type > 999999) {
			throw new IllegalArgumentException("type 唯一索引范围超过最大值999999");
		}
		if (typeParam > 999999) {
			throw new IllegalArgumentException("typeParam 唯一索引范围超过最大值999999");
		}
		long result = 0;
		result = result | (long) type << 43;
		result = result | (long) typeParam << 22;
		return result;
	}
	/**
	 * 根据唯一索引获取一条数据
	 * 
	 * @param  type typeParam
	 * @return
	 */
	public ParameterConsumeConfig getUITypetypeParam(int type,int typeParam) {
		return this.typetypeParams.get(hashIndexUnique1(type,typeParam));
	}
	/**
	 * 获取所有数据
	 * @return
	 */
	public Collection<ParameterConsumeConfig> list() {
		return typetypeParams.values();
	}
	@Override
	public void load() {
		try {
			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			for (Element e : list) {
				ParameterConsumeConfig parameterconsume = new ParameterConsumeConfig(e);
  				Long hashIndexUnique1 = hashIndexUnique1(parameterconsume.type,parameterconsume.typeParam) ; 
				ParameterConsumeConfig old1 = typetypeParams.put(hashIndexUnique1 ,parameterconsume);
				if (old1 != null) {
					throw new IllegalArgumentException("[ParameterConsumeConfig]表重复的唯一索引[type,typeParam] , 重复id : " + old1.ID +" "  + parameterconsume.ID);
				}
			}			

  			this.typetypeParams = com.google.common.collect.ImmutableMap.copyOf(typetypeParams);

  			log.info("load ParameterConsumeConfig size[{}]",this.typetypeParams.size()) ;

		} catch (Exception e) {
			throw new RuntimeException("load ParameterConsumeConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
