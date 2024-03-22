package cn.game.protocol.generated.config;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.w3c.dom.Element;

import cn.game.protocol.generated.enume.ActivityTypeEnum;
import cn.game.util.DateUtil;

/**
 * 活动表
 * 
 * 工具生成的，不要手动修改
 */
 public class ActivityConfig {

	/** id -- 活动id */
	private final int id;		
	/** 展示时间 -- 达到此时间，显示活动, 如果为空表示常驻 */
	private final Date viewTime;		
	/** 销毁时间 -- 活动彻底销毁，不在展示 */
	private final List<Date> destroyTime;		
	/** 开始时间 -- 活动正式开启时间，可以参与活动 */
	private final List<Date> startTime;		
	/** 结束时间 -- 活动不能参加，可以查看 */
	private final List<Date> endTime;		
	/** 活动类型 -- ActivityTypeEnum.xlsm的id,根据不同的类型，读取具体的活动配置 */
	private final ActivityTypeEnum type;		
	/** 周期 -- 距离下次开启活动的时间（秒），大于0表示周期性的开启 */
	private final int period;		
	/** 是否启用 */
	private final boolean enable;		
	/** 是否是玩家的活动 */
	private final boolean isPlayer;		

	public ActivityConfig (Element element) throws Exception {
	
		this.id = Integer.parseInt(element.getAttribute("id") == null || element.getAttribute("id").length() == 0 ? "0"
			: element.getAttribute("id")); // id
		String viewTime = element.getAttribute("viewTime"); // 展示时间
		this.viewTime = viewTime != null && viewTime.length() > 0 ? DateUtil.parse(viewTime) : null;
		
		String destroyTimeString = element.getAttribute("destroyTime"); // 销毁时间
		if (destroyTimeString != null && destroyTimeString.length() > 0) {
			String[] destroyTimeStrings = destroyTimeString.split("\\|"); 
			List<Date> destroyTime = new ArrayList<Date>(destroyTimeStrings.length) ; 
			for (int i = 0; i < destroyTimeStrings.length; i++) {
				Date temp = DateUtil.parse(destroyTimeStrings[i]) ;
				destroyTime.add(temp);
			}
			this.destroyTime = com.google.common.collect.ImmutableList.copyOf(destroyTime);						
		} else {
			this.destroyTime = java.util.Collections.emptyList();
		}
		String startTimeString = element.getAttribute("startTime"); // 开始时间
		if (startTimeString != null && startTimeString.length() > 0) {
			String[] startTimeStrings = startTimeString.split("\\|"); 
			List<Date> startTime = new ArrayList<Date>(startTimeStrings.length) ; 
			for (int i = 0; i < startTimeStrings.length; i++) {
				Date temp = DateUtil.parse(startTimeStrings[i]) ;
				startTime.add(temp);
			}
			this.startTime = com.google.common.collect.ImmutableList.copyOf(startTime);						
		} else {
			this.startTime = java.util.Collections.emptyList();
		}
		String endTimeString = element.getAttribute("endTime"); // 结束时间
		if (endTimeString != null && endTimeString.length() > 0) {
			String[] endTimeStrings = endTimeString.split("\\|"); 
			List<Date> endTime = new ArrayList<Date>(endTimeStrings.length) ; 
			for (int i = 0; i < endTimeStrings.length; i++) {
				Date temp = DateUtil.parse(endTimeStrings[i]) ;
				endTime.add(temp);
			}
			this.endTime = com.google.common.collect.ImmutableList.copyOf(endTime);						
		} else {
			this.endTime = java.util.Collections.emptyList();
		}
		this.type = ActivityTypeEnum.get(Integer.parseInt(element.getAttribute("type")));	// 活动类型
		this.period = Integer.parseInt(element.getAttribute("period") == null || element.getAttribute("period").length() == 0 ? "0"
			: element.getAttribute("period")); // 周期
		this.enable = Boolean.parseBoolean(element.getAttribute("enable") == null || element.getAttribute("enable").length() == 0 ? "false"
			: element.getAttribute("enable")); // 是否启用
		this.isPlayer = Boolean.parseBoolean(element.getAttribute("isPlayer") == null || element.getAttribute("isPlayer").length() == 0 ? "false"
			: element.getAttribute("isPlayer")); // 是否是玩家的活动
	}
	
	public int getId() {
		return id;
	}
	
	public Date getViewTime() {
		return viewTime;
	}
	
	public List<Date> getDestroyTime() {
		return destroyTime;
	}
	
	public List<Date> getStartTime() {
		return startTime;
	}
	
	public List<Date> getEndTime() {
		return endTime;
	}
	
	public ActivityTypeEnum getType() {
		return type;
	}
	
	public int getPeriod() {
		return period;
	}
	
	public boolean getEnable() {
		return enable;
	}
	
	public boolean getIsPlayer() {
		return isPlayer;
	}
	
}
