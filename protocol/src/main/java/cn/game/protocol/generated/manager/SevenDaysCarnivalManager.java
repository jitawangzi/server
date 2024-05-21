package cn.game.protocol.generated.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cn.game.protocol.generated.config.SevenDaysCarnivalConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class SevenDaysCarnivalManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(SevenDaysCarnivalManager.class);

	private static SevenDaysCarnivalManager instance = new SevenDaysCarnivalManager();
	private static final String xmlFileName = "SevenDaysCarnival";
	
	/** 唯一索引 */
	private Map<Long,SevenDaysCarnivalConfig> TypeDays = new HashMap<>();

	public static SevenDaysCarnivalManager instance() {
		return instance;
	}
	private SevenDaysCarnivalManager() {
		WatchServiceManager.getInstance().register(this);
	}

	private long hashIndexUnique1(int Type,int Day) {
		if (Type > 999999) {
			throw new IllegalArgumentException("Type 唯一索引范围超过最大值999999");
		}
		if (Day > 999999) {
			throw new IllegalArgumentException("Day 唯一索引范围超过最大值999999");
		}
		long result = 0;
		result = result | (long) Type << 43;
		result = result | (long) Day << 22;
		return result;
	}
	/**
	 * 根据唯一索引获取一条数据
	 * 
	 * @param  Type Day
	 * @return
	 */
	public SevenDaysCarnivalConfig getUITypeDay(int Type,int Day) {
		return this.TypeDays.get(hashIndexUnique1(Type,Day));
	}
	/**
	 * 获取所有数据
	 * @return
	 */
	public Collection<SevenDaysCarnivalConfig> list() {
		return TypeDays.values();
	}
	@Override
	public void load() {
		try {
			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			for (Element e : list) {
				SevenDaysCarnivalConfig sevendayscarnival = new SevenDaysCarnivalConfig(e);
  				Long hashIndexUnique1 = hashIndexUnique1(sevendayscarnival.Type,sevendayscarnival.Day) ; 
				SevenDaysCarnivalConfig old1 = TypeDays.put(hashIndexUnique1 ,sevendayscarnival);
				if (old1 != null) {
					throw new IllegalArgumentException("[SevenDaysCarnivalConfig]表重复的唯一索引[Type,Day] , 重复id : " + old1.ID +" "  + sevendayscarnival.ID);
				}
			}			

  			this.TypeDays = com.google.common.collect.ImmutableMap.copyOf(TypeDays);

  			log.info("load SevenDaysCarnivalConfig size[{}]",this.TypeDays.size()) ;

		} catch (Exception e) {
			throw new RuntimeException("load SevenDaysCarnivalConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
