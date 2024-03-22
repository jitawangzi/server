package cn.game.protocol.generated.config;

import java.util.Map.Entry;
import java.util.ArrayList;
import java.util.List;
import cn.game.util.DateUtil;
import java.util.Date;
import org.w3c.dom.Element;

/**
 * 商店推荐
 * 
 * 工具生成的，不要手动修改
 */
 public class StoreRecommendConfig {

	/** 活动id -- 活动id */
	private final int id;		
	/** 活动名称 -- 活动名称 */
	private final String name;		
	/** 关联礼包id -- 关联礼包id */
	private final List<Integer> giftId;		
	/** 活动标签 -- 活动标签 */
	private final int activityType;		
	/** 跳转逻辑 -- 跳转逻辑 */
	private final List<Entry<Integer,Integer>> jumpId;		
	/** 上架时间 -- 上架时间 */
	private final Date upTime;		
	/** 下架时间 -- 下架时间 */
	private final Date downTime;		
	/** 是否上架 -- 是否上架 */
	private final boolean upDown;		

	public StoreRecommendConfig (Element element) throws Exception {
	
		this.id = Integer.parseInt(element.getAttribute("id") == null || element.getAttribute("id").length() == 0 ? "0"
			: element.getAttribute("id")); // 活动id
		this.name = element.getAttribute("name"); // 活动名称
		String giftIdString = element.getAttribute("giftId"); // 关联礼包id
		if (giftIdString != null && giftIdString.length() > 0) {
			String[] giftIdStrings = giftIdString.split("\\|"); 
			List<Integer> giftId = new ArrayList<Integer>(giftIdStrings.length) ; 
			for (int i = 0; i < giftIdStrings.length; i++) {
				Integer temp = Integer.parseInt(giftIdStrings[i]);
				giftId.add(temp);
			}
			this.giftId = com.google.common.collect.ImmutableList.copyOf(giftId);						
		} else {
			this.giftId = java.util.Collections.emptyList();
		}
		this.activityType = Integer.parseInt(element.getAttribute("activityType") == null || element.getAttribute("activityType").length() == 0 ? "0"
			: element.getAttribute("activityType")); // 活动标签
		String jumpIdString = element.getAttribute("jumpId"); // 跳转逻辑
		if (jumpIdString != null && jumpIdString.length() > 0) {
			String[] jumpIdStrings = jumpIdString.split("\\|"); 
			List<Entry<Integer,Integer>> jumpId = new ArrayList<Entry<Integer,Integer>>(jumpIdStrings.length) ; 
			for (int i = 0; i < jumpIdStrings.length; i++) {
			    String[] split = jumpIdStrings[i].split(":", 2);
				jumpId.add(new Entry<Integer,Integer>()	{
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

			this.jumpId = com.google.common.collect.ImmutableList.copyOf(jumpId);						
		} else {
			this.jumpId = java.util.Collections.emptyList();
		}
		String upTime = element.getAttribute("upTime"); // 上架时间
		this.upTime = upTime != null && upTime.length() > 0 ? DateUtil.parse(upTime) : null;
		
		String downTime = element.getAttribute("downTime"); // 下架时间
		this.downTime = downTime != null && downTime.length() > 0 ? DateUtil.parse(downTime) : null;
		
		this.upDown = Boolean.parseBoolean(element.getAttribute("upDown") == null || element.getAttribute("upDown").length() == 0 ? "false"
			: element.getAttribute("upDown")); // 是否上架
	}
	
	public int getId() {
		return id;
	}
	
	public String getName() {
		return name;
	}
	
	public List<Integer> getGiftId() {
		return giftId;
	}
	
	public int getActivityType() {
		return activityType;
	}
	
	public List<Entry<Integer,Integer>> getJumpId() {
		return jumpId;
	}
	
	public Date getUpTime() {
		return upTime;
	}
	
	public Date getDownTime() {
		return downTime;
	}
	
	public boolean getUpDown() {
		return upDown;
	}
	
}
