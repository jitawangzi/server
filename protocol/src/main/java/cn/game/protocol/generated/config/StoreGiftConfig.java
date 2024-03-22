package cn.game.protocol.generated.config;

import java.util.Map.Entry;
import java.util.ArrayList;
import java.util.List;
import cn.game.util.DateUtil;
import java.util.Date;
import org.w3c.dom.Element;

/**
 * 商店礼包
 * 
 * 工具生成的，不要手动修改
 */
 public class StoreGiftConfig {

	/** 礼包id -- 礼包id */
	private final int id;		
	/** 礼包类型 -- 礼包类型 */
	private final int giftType;		
	/** 限制参数1 -- 限制参数1 */
	private final int limitParameters1;		
	/** 限制参数2 -- 限制参数2 */
	private final int limitParameters2;		
	/** 特殊奖励 -- 特殊奖励 */
	private final List<Entry<Integer,Integer>> specialAward;		
	/** 礼包奖励 -- 礼包奖励 */
	private final List<Entry<Integer,Integer>> giftReward;		
	/** 充值接口 -- 充值接口 */
	private final String rechargeSdk;		
	/** 充值金额 -- 充值金额 */
	private final int rechargeAmount;		
	/** 上架时间 -- 上架时间 */
	private final Date upTime;		
	/** 下架时间 -- 下架时间 */
	private final Date downTime;		
	/** 是否上架 -- 是否上架 */
	private final boolean upDown;		

	public StoreGiftConfig (Element element) throws Exception {
	
		this.id = Integer.parseInt(element.getAttribute("id") == null || element.getAttribute("id").length() == 0 ? "0"
			: element.getAttribute("id")); // 礼包id
		this.giftType = Integer.parseInt(element.getAttribute("giftType") == null || element.getAttribute("giftType").length() == 0 ? "0"
			: element.getAttribute("giftType")); // 礼包类型
		this.limitParameters1 = Integer.parseInt(element.getAttribute("limitParameters1") == null || element.getAttribute("limitParameters1").length() == 0 ? "0"
			: element.getAttribute("limitParameters1")); // 限制参数1
		this.limitParameters2 = Integer.parseInt(element.getAttribute("limitParameters2") == null || element.getAttribute("limitParameters2").length() == 0 ? "0"
			: element.getAttribute("limitParameters2")); // 限制参数2
		String specialAwardString = element.getAttribute("specialAward"); // 特殊奖励
		if (specialAwardString != null && specialAwardString.length() > 0) {
			String[] specialAwardStrings = specialAwardString.split("\\|"); 
			List<Entry<Integer,Integer>> specialAward = new ArrayList<Entry<Integer,Integer>>(specialAwardStrings.length) ; 
			for (int i = 0; i < specialAwardStrings.length; i++) {
			    String[] split = specialAwardStrings[i].split(":", 2);
				specialAward.add(new Entry<Integer,Integer>()	{
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

			this.specialAward = com.google.common.collect.ImmutableList.copyOf(specialAward);						
		} else {
			this.specialAward = java.util.Collections.emptyList();
		}
		String giftRewardString = element.getAttribute("giftReward"); // 礼包奖励
		if (giftRewardString != null && giftRewardString.length() > 0) {
			String[] giftRewardStrings = giftRewardString.split("\\|"); 
			List<Entry<Integer,Integer>> giftReward = new ArrayList<Entry<Integer,Integer>>(giftRewardStrings.length) ; 
			for (int i = 0; i < giftRewardStrings.length; i++) {
			    String[] split = giftRewardStrings[i].split(":", 2);
				giftReward.add(new Entry<Integer,Integer>()	{
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

			this.giftReward = com.google.common.collect.ImmutableList.copyOf(giftReward);						
		} else {
			this.giftReward = java.util.Collections.emptyList();
		}
		this.rechargeSdk = element.getAttribute("rechargeSdk"); // 充值接口
		this.rechargeAmount = Integer.parseInt(element.getAttribute("rechargeAmount") == null || element.getAttribute("rechargeAmount").length() == 0 ? "0"
			: element.getAttribute("rechargeAmount")); // 充值金额
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
	
	public int getGiftType() {
		return giftType;
	}
	
	public int getLimitParameters1() {
		return limitParameters1;
	}
	
	public int getLimitParameters2() {
		return limitParameters2;
	}
	
	public List<Entry<Integer,Integer>> getSpecialAward() {
		return specialAward;
	}
	
	public List<Entry<Integer,Integer>> getGiftReward() {
		return giftReward;
	}
	
	public String getRechargeSdk() {
		return rechargeSdk;
	}
	
	public int getRechargeAmount() {
		return rechargeAmount;
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
