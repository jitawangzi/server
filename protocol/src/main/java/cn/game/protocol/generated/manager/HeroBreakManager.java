package cn.game.protocol.generated.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cn.game.protocol.generated.config.HeroBreakConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class HeroBreakManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(HeroBreakManager.class);

	private static HeroBreakManager instance = new HeroBreakManager();
	private static final String xmlFileName = "HeroBreak";
	
	/** 唯一索引 */
	private Map<Long,HeroBreakConfig> InitialQualityStars = new HashMap<>();

	public static HeroBreakManager instance() {
		return instance;
	}
	private HeroBreakManager() {
		WatchServiceManager.getInstance().register(this);
	}

	private long hashIndexUnique1(int InitialQuality,int Star) {
		if (InitialQuality > 999999) {
			throw new IllegalArgumentException("InitialQuality 唯一索引范围超过最大值999999");
		}
		if (Star > 999999) {
			throw new IllegalArgumentException("Star 唯一索引范围超过最大值999999");
		}
		long result = 0;
		result = result | (long) InitialQuality << 43;
		result = result | (long) Star << 22;
		return result;
	}
	/**
	 * 根据唯一索引获取一条数据
	 * 
	 * @param  InitialQuality Star
	 * @return
	 */
	public HeroBreakConfig getUIInitialQualityStar(int InitialQuality,int Star) {
		return this.InitialQualityStars.get(hashIndexUnique1(InitialQuality,Star));
	}
	/**
	 * 获取所有数据
	 * @return
	 */
	public Collection<HeroBreakConfig> list() {
		return InitialQualityStars.values();
	}
	@Override
	public void load() {
		try {
			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			for (Element e : list) {
				HeroBreakConfig herobreak = new HeroBreakConfig(e);
  				Long hashIndexUnique1 = hashIndexUnique1(herobreak.InitialQuality,herobreak.Star) ; 
				HeroBreakConfig old1 = InitialQualityStars.put(hashIndexUnique1 ,herobreak);
				if (old1 != null) {
					throw new IllegalArgumentException("[HeroBreakConfig]表重复的唯一索引[InitialQuality,Star] , 重复id : " + old1.ID +" "  + herobreak.ID);
				}
			}			

  			this.InitialQualityStars = com.google.common.collect.ImmutableMap.copyOf(InitialQualityStars);

  			log.info("load HeroBreakConfig size[{}]",this.InitialQualityStars.size()) ;

		} catch (Exception e) {
			throw new RuntimeException("load HeroBreakConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
