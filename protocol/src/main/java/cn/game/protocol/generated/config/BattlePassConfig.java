package cn.game.protocol.generated.config;

import java.util.ArrayList;
import java.util.List;
import cn.game.util.DateUtil;
import java.util.Date;
import org.w3c.dom.Element;

/**
 * 通行证
 * 
 * 工具生成的，不要手动修改
 */
 public class BattlePassConfig {

	/** id -- 周期 */
	private final int id;		
	/** 上架日期 -- 上架日期 */
	private final Date upTime;		
	/** 下架日期 -- 下架日期 */
	private final Date downTime;		
	/** 是否上架 -- 是否上架 */
	private final boolean upDown;		
	/** 充值接口 -- 充值接口 */
	private final String rechargeSdk;		
	/** 充值金额 -- 充值金额 */
	private final int rechargeAmount;		
	/** 任务id -- 任务id */
	private final List<List<Integer>> taskId;		

	public BattlePassConfig (Element element) throws Exception {
	
		this.id = Integer.parseInt(element.getAttribute("id") == null || element.getAttribute("id").length() == 0 ? "0"
			: element.getAttribute("id")); // id
		String upTime = element.getAttribute("upTime"); // 上架日期
		this.upTime = upTime != null && upTime.length() > 0 ? DateUtil.parse(upTime) : null;
		
		String downTime = element.getAttribute("downTime"); // 下架日期
		this.downTime = downTime != null && downTime.length() > 0 ? DateUtil.parse(downTime) : null;
		
		this.upDown = Boolean.parseBoolean(element.getAttribute("upDown") == null || element.getAttribute("upDown").length() == 0 ? "false"
			: element.getAttribute("upDown")); // 是否上架
		this.rechargeSdk = element.getAttribute("rechargeSdk"); // 充值接口
		this.rechargeAmount = Integer.parseInt(element.getAttribute("rechargeAmount") == null || element.getAttribute("rechargeAmount").length() == 0 ? "0"
			: element.getAttribute("rechargeAmount")); // 充值金额
		String taskIdString = element.getAttribute("taskId"); // 任务id
		if (taskIdString != null && taskIdString.length() > 0) {
			String[] taskIdStrings = taskIdString.split("\\|"); 
			List<List<Integer>> taskId = new ArrayList<List<Integer>>(taskIdStrings.length) ; 
			for (int i = 0; i < taskIdStrings.length; i++) {
				String[] taskIdStrings2 = taskIdStrings[i].split(":"); 
				List<Integer> list = new ArrayList<Integer>(taskIdStrings2.length) ; 
				for (int j = 0; j < taskIdStrings2.length; j++) {
					Integer temp = Integer.parseInt(taskIdStrings2[j]);
					list.add(temp) ; 
				}
				taskId.add(list);
			}
			this.taskId = com.google.common.collect.ImmutableList.copyOf(taskId);						
		} else {
			this.taskId = java.util.Collections.emptyList();
		}
	}
	
	public int getId() {
		return id;
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
	
	public String getRechargeSdk() {
		return rechargeSdk;
	}
	
	public int getRechargeAmount() {
		return rechargeAmount;
	}
	
	public List<List<Integer>> getTaskId() {
		return taskId;
	}
	
}
