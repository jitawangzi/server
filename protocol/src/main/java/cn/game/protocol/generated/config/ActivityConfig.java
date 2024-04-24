package cn.game.protocol.generated.config;

import java.util.ArrayList;
import java.util.List;
import cn.game.util.DateUtil;
import java.util.Date;
import org.w3c.dom.Element;


/**
 * 活动表
 * 
 * 工具生成的，不要手动修改
 */
 public class ActivityConfig {

	/** 活动id */
	public final int ID;		
	/** ActivityTypeEnum的id,根据不同的类型，读取具体的活动配置 */
	public final int type;		
	/** 开启类型： 1 创建账号 2 创建账号后xx天 3 玩家到达指定等级 4 具体时间段 */
	public final int openType;		
	/** 开启参数 */
	public final int openParam;		
	/** 重置类型： 0 不重置，一次性活动 1 日 2 周 3 月 */
	public final int resetType;		
	/** 达到此时间，显示活动 */
	public final Date viewTime;		
	/** 活动彻底销毁，不在展示 */
	public final List<Date> destroyTime;		
	/** 活动正式开启时间，可以参与活动 没有开始时间的，默认常驻活动。 多个开启时间可以指定时间重复开启 */
	public final List<Date> startTime;		
	/** 活动结束时间，结束后活动不能参加，可以查看 */
	public final List<Date> endTime;		
	/** 距离下次开启活动的时间（秒），大于0表示周期性的开启 */
	public final int period;		
	/** 参加活动的等级限制 */
	public final int levelLimit;		
	/** 活动奖励 */
	public final int[][] rewards;		
	/** 是否禁用 */
	public final boolean disable;		
	/** 玩家单人活动，或者多人参加的活动 */
	public final boolean isMultiplayer;		

	public ActivityConfig (Element element) throws Exception {
	
		ID = Integer.parseInt(element.getAttribute("ID") == null || element.getAttribute("ID").length() == 0 ? "0"
			: element.getAttribute("ID")); // 活动id
		type = Integer.parseInt(element.getAttribute("type") == null || element.getAttribute("type").length() == 0 ? "0"
			: element.getAttribute("type")); // ActivityTypeEnum的id,根据不同的类型，读取具体的活动配置
		openType = Integer.parseInt(element.getAttribute("openType") == null || element.getAttribute("openType").length() == 0 ? "0"
			: element.getAttribute("openType")); // 开启类型： 1 创建账号 2 创建账号后xx天 3 玩家到达指定等级 4 具体时间段
		openParam = Integer.parseInt(element.getAttribute("openParam") == null || element.getAttribute("openParam").length() == 0 ? "0"
			: element.getAttribute("openParam")); // 开启参数
		resetType = Integer.parseInt(element.getAttribute("resetType") == null || element.getAttribute("resetType").length() == 0 ? "0"
			: element.getAttribute("resetType")); // 重置类型： 0 不重置，一次性活动 1 日 2 周 3 月
		String viewTimeTemp = element.getAttribute("viewTime"); // 达到此时间，显示活动
		viewTime = viewTimeTemp != null && viewTimeTemp.length() > 0 ? DateUtil.parse(viewTimeTemp) : null;
		
		String destroyTimeString = element.getAttribute("destroyTime"); // 活动彻底销毁，不在展示
		if (destroyTimeString != null && destroyTimeString.length() > 0) {
			String[] destroyTimeStrings = destroyTimeString.split(";"); 
			List<Date> destroyTimeTemp = new ArrayList<Date>(destroyTimeStrings.length) ; 
			for (int i = 0; i < destroyTimeStrings.length; i++) {
				Date temp = DateUtil.parse(destroyTimeStrings[i]);	
				destroyTimeTemp.add(temp);
			}
			destroyTime = com.google.common.collect.ImmutableList.copyOf(destroyTimeTemp);						
		} else {
			destroyTime = java.util.Collections.emptyList();
		}
		String startTimeString = element.getAttribute("startTime"); // 活动正式开启时间，可以参与活动 没有开始时间的，默认常驻活动。 多个开启时间可以指定时间重复开启
		if (startTimeString != null && startTimeString.length() > 0) {
			String[] startTimeStrings = startTimeString.split(";"); 
			List<Date> startTimeTemp = new ArrayList<Date>(startTimeStrings.length) ; 
			for (int i = 0; i < startTimeStrings.length; i++) {
				Date temp = DateUtil.parse(startTimeStrings[i]);	
				startTimeTemp.add(temp);
			}
			startTime = com.google.common.collect.ImmutableList.copyOf(startTimeTemp);						
		} else {
			startTime = java.util.Collections.emptyList();
		}
		String endTimeString = element.getAttribute("endTime"); // 活动结束时间，结束后活动不能参加，可以查看
		if (endTimeString != null && endTimeString.length() > 0) {
			String[] endTimeStrings = endTimeString.split(";"); 
			List<Date> endTimeTemp = new ArrayList<Date>(endTimeStrings.length) ; 
			for (int i = 0; i < endTimeStrings.length; i++) {
				Date temp = DateUtil.parse(endTimeStrings[i]);	
				endTimeTemp.add(temp);
			}
			endTime = com.google.common.collect.ImmutableList.copyOf(endTimeTemp);						
		} else {
			endTime = java.util.Collections.emptyList();
		}
		period = Integer.parseInt(element.getAttribute("period") == null || element.getAttribute("period").length() == 0 ? "0"
			: element.getAttribute("period")); // 距离下次开启活动的时间（秒），大于0表示周期性的开启
		levelLimit = Integer.parseInt(element.getAttribute("levelLimit") == null || element.getAttribute("levelLimit").length() == 0 ? "0"
			: element.getAttribute("levelLimit")); // 参加活动的等级限制
		String rewardsString = element.getAttribute("rewards"); // 活动奖励
		if (rewardsString != null && rewardsString.length() > 0) {
			String[] rewardsStrings = rewardsString.split("\\|"); 
			int[][] rewardsTemp = new int[rewardsStrings.length][] ; 
			for (int i = 0; i < rewardsStrings.length; i++) {
				String[] rewardsStrings2 = rewardsStrings[i].split(";"); 
				int[] array = new int[rewardsStrings2.length];
				for (int j = 0; j < rewardsStrings2.length; j++) {
					int temp = Integer.parseInt(rewardsStrings2[j]);	
					array[j] = temp;
				}
				rewardsTemp[i] = array;
			}
			rewards = rewardsTemp ;			
		} else {
			rewards = new int[][] {};
		}
		disable = Boolean.parseBoolean(element.getAttribute("disable") == null || element.getAttribute("disable").length() == 0 ? "false"
			: element.getAttribute("disable")); // 是否禁用
		isMultiplayer = Boolean.parseBoolean(element.getAttribute("isMultiplayer") == null || element.getAttribute("isMultiplayer").length() == 0 ? "false"
			: element.getAttribute("isMultiplayer")); // 玩家单人活动，或者多人参加的活动
	}
	

}
