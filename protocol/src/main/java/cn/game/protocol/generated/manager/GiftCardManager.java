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

import cn.game.protocol.generated.config.GiftCardConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class GiftCardManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(GiftCardManager.class);

	private static GiftCardManager instance = new GiftCardManager();
	private static final String xmlFileName = "GiftCard";
	
	/** 普通索引 */
	private Map<Integer,List<GiftCardConfig>> DrawIds = new HashMap<>();

	public static GiftCardManager instance() {
		return instance;
	}
	private GiftCardManager() {
		WatchServiceManager.getInstance().register(this);
	}

	public List<GiftCardConfig> getDrawIdList(int DrawId) {
		return this.DrawIds.get(DrawId);
	}
	public Map<Integer,List<GiftCardConfig>> getDrawIds() {
		return this.DrawIds;
	}
	/**
	 * 获取所有数据
	 * @return
	 */
	public Collection<List<GiftCardConfig>> list() {
		return DrawIds.values();
	}
	@Override
	public void load() {
		try {
			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, List<GiftCardConfig>> DrawIds = new HashMap<>();
			for (Element e : list) {
				GiftCardConfig giftcard = new GiftCardConfig(e);
				List<GiftCardConfig> DrawIdList = DrawIds.get(giftcard.DrawId); 
				if (DrawIdList == null){
					DrawIdList = new ArrayList<GiftCardConfig>(2) ; 
					DrawIds.put(giftcard.DrawId ,DrawIdList) ; 
				}
				DrawIdList.add(giftcard) ;
			}			

			this.DrawIds = com.google.common.collect.ImmutableMap.copyOf(DrawIds);			

			log.info("load GiftCardConfig size[{}]", DrawIds.values().stream().flatMap(List::stream).count());

		} catch (Exception e) {
			throw new RuntimeException("load GiftCardConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
