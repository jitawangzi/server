package cn.game.protocol.generated.config;

import java.util.Map.Entry;
import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Element;

/**
 * 事件选项表
 * 
 * 工具生成的，不要手动修改
 */
 public class EventOptionConfig {

	/** 选项id -- 事件id+序号 */
	private final int id;		
	/** 事件id */
	private final int eventId;		
	/** 选项顺序 -- 或者数据行顺序 */
	private final byte sequence;		
	/** 选项描述 */
	private final String desc;		
	/** 随机选项还是固定选项 -- 1-权重代替 0-顺序代替 */
	private final boolean isRandom;		
	/** 选项的buffId */
	private final int[] buffId;		
	/** buff权重 */
	private final int[] buffWeight;		
	/** 选择时花费 -- 选择时需要消耗道具/资源/货币 */
	private final List<Entry<Integer,Integer>> chooseCost;		

	public EventOptionConfig (Element element) throws Exception {
	
		this.id = Integer.parseInt(element.getAttribute("id") == null || element.getAttribute("id").length() == 0 ? "0"
			: element.getAttribute("id")); // 选项id
		this.eventId = Integer.parseInt(element.getAttribute("eventId") == null || element.getAttribute("eventId").length() == 0 ? "0"
			: element.getAttribute("eventId")); // 事件id
		this.sequence = Byte.parseByte(element.getAttribute("sequence") == null || element.getAttribute("sequence").length() == 0 ? "0"
			: element.getAttribute("sequence")); // 选项顺序
		this.desc = element.getAttribute("desc"); // 选项描述
		this.isRandom = Boolean.parseBoolean(element.getAttribute("isRandom") == null || element.getAttribute("isRandom").length() == 0 ? "false"
			: element.getAttribute("isRandom")); // 随机选项还是固定选项
		String buffIdString = element.getAttribute("buffId"); // 选项的buffId
		if (buffIdString != null && buffIdString.length() > 0) {
			String[] buffIdStrings = buffIdString.split("\\|"); 
			int[] buffId = new int[buffIdStrings.length] ; 
			for (int i = 0; i < buffIdStrings.length; i++) {
				int temp = Integer.parseInt(buffIdStrings[i]);
				buffId[i] = temp;
			}
			this.buffId = buffId ;			
		} else {
			this.buffId = new int[] {};
		}
		String buffWeightString = element.getAttribute("buffWeight"); // buff权重
		if (buffWeightString != null && buffWeightString.length() > 0) {
			String[] buffWeightStrings = buffWeightString.split("\\|"); 
			int[] buffWeight = new int[buffWeightStrings.length] ; 
			for (int i = 0; i < buffWeightStrings.length; i++) {
				int temp = Integer.parseInt(buffWeightStrings[i]);
				buffWeight[i] = temp;
			}
			this.buffWeight = buffWeight ;			
		} else {
			this.buffWeight = new int[] {};
		}
		String chooseCostString = element.getAttribute("chooseCost"); // 选择时花费
		if (chooseCostString != null && chooseCostString.length() > 0) {
			String[] chooseCostStrings = chooseCostString.split("\\|"); 
			List<Entry<Integer,Integer>> chooseCost = new ArrayList<Entry<Integer,Integer>>(chooseCostStrings.length) ; 
			for (int i = 0; i < chooseCostStrings.length; i++) {
			    String[] split = chooseCostStrings[i].split(":", 2);
				chooseCost.add(new Entry<Integer,Integer>()	{
					@Override
					public Integer setValue(Integer value) {
						return null;
					}
					@Override
					public Integer getValue() {
						try {
							return Integer.parseInt(split[1]);
						} catch (Exception e) {
							e.printStackTrace();
						}
						throw new NullPointerException();	
					}
					@Override
					public Integer getKey() {
						try {
							return Integer.parseInt(split[0]);
						} catch (Exception e) {
							e.printStackTrace();
						}
						throw new NullPointerException();
					}
				}) ; 
			}

			this.chooseCost = com.google.common.collect.ImmutableList.copyOf(chooseCost);						
		} else {
			this.chooseCost = java.util.Collections.emptyList();
		}
	}
	
	public int getId() {
		return id;
	}
	
	public int getEventId() {
		return eventId;
	}
	
	public byte getSequence() {
		return sequence;
	}
	
	public String getDesc() {
		return desc;
	}
	
	public boolean getIsRandom() {
		return isRandom;
	}
	
	public int[] getBuffId() {
		return buffId;
	}
	
	public int[] getBuffWeight() {
		return buffWeight;
	}
	
	public List<Entry<Integer,Integer>> getChooseCost() {
		return chooseCost;
	}
	
}
