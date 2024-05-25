package cn.game.protocol.generated.manager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cn.game.protocol.generated.config.EquipConfig;
import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 工具生成的，不要手动修改
 */
public class EquipManager extends ResourceListener {
	private static final Logger log = LoggerFactory.getLogger(EquipManager.class);

	private static EquipManager instance = new EquipManager();
	private static final String xmlFileName = "Equip";
	
	/** 总数据，按id取值 */
	private Map<Integer, EquipConfig> equips = new HashMap<>();
	/** 唯一索引 */
	private Map<Long,EquipConfig> EquipGroupEquipLvs = new HashMap<>();

	public static EquipManager instance() {
		return instance;
	}
	private EquipManager() {
		WatchServiceManager.getInstance().register(this);
	}
	/**
	 * 根据id获取数据，一般用这个方法，如果数据不存在，一般是配置错误，直接抛出异常
	 * 
	 * @param id
	 * @return
	 */
	public EquipConfig get(int id) {
		EquipConfig config = this.equips.get(id);
		if (config == null) { 
			throw new NullPointerException("【Equip】表的" + "id【" + id + "】不存在"); 
		}
		return config;
	}
	/**
	 * 根据id获取数据，允许返回null
	 * 
	 * @param id
	 * @return
	 */
	public EquipConfig getNullable(int id) {
		return this.equips.get(id);
	}

	private long hashIndexUnique1(int EquipGroup,int EquipLv) {
		if (EquipGroup > 999999) {
			throw new IllegalArgumentException("EquipGroup 唯一索引范围超过最大值999999");
		}
		if (EquipLv > 999999) {
			throw new IllegalArgumentException("EquipLv 唯一索引范围超过最大值999999");
		}
		long result = 0;
		result = result | (long) EquipGroup << 43;
		result = result | (long) EquipLv << 22;
		return result;
	}
	/**
	 * 根据唯一索引获取一条数据
	 * 
	 * @param  EquipGroup EquipLv
	 * @return
	 */
	public EquipConfig getUIEquipGroupEquipLv(int EquipGroup,int EquipLv) {
		return this.EquipGroupEquipLvs.get(hashIndexUnique1(EquipGroup,EquipLv));
	}
	/**
	 * 获取所有数据
	 * @return
	 */
	public Collection<EquipConfig> list() {
		return this.equips.values();
	}
	@Override
	public void load() {
		try {
			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			
			Map<Integer, EquipConfig> equips = new HashMap<>();
			for (Element e : list) {
				EquipConfig equip = new EquipConfig(e);
  				Long hashIndexUnique1 = hashIndexUnique1(equip.EquipGroup,equip.EquipLv) ; 
				EquipConfig old1 = EquipGroupEquipLvs.put(hashIndexUnique1 ,equip);
				if (old1 != null) {
					throw new IllegalArgumentException("[EquipConfig]表重复的唯一索引[EquipGroup,EquipLv] , 重复id : " + old1.ID +" "  + equip.ID);
				}
				EquipConfig old = equips.put(equip.ID, equip);
				if (old != null) {
					throw new IllegalArgumentException("[EquipConfig]表存在重复的数据id： " + old.ID);
				}
			}			

  			this.EquipGroupEquipLvs = com.google.common.collect.ImmutableMap.copyOf(EquipGroupEquipLvs);
			this.equips = com.google.common.collect.ImmutableMap.copyOf(equips);

			log.info("load EquipConfig size[{}]", equips.size());

		} catch (Exception e) {
			throw new RuntimeException("load EquipConfig error", e);
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}

}
