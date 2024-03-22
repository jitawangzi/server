package cn.game.protocol.generated.config;

import java.util.Map.Entry;
import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Element;

/**
 * 商店充值
 * 
 * 工具生成的，不要手动修改
 */
 public class StoreRechargeConfig {

	/** 充值档位 -- 充值档位 */
	private final int id;		
	/** 图标路径 -- 图标路径 */
	private final String fileIconRes;		
	/** 充值奖励 -- 充值奖励 */
	private final List<Entry<Integer,Integer>> rechargeReward;		
	/** 首冲奖励 -- 首冲奖励 */
	private final List<Entry<Integer,Integer>> firstRechargeReward;		
	/** 充值接口 -- 充值接口 */
	private final String rechargeSdk;		
	/** 充值金额 -- 充值金额 */
	private final int rechargeAmount;		
	/** 是否上架 -- 是否上架 */
	private final boolean upDown;		

	public StoreRechargeConfig (Element element) throws Exception {
	
		this.id = Integer.parseInt(element.getAttribute("id") == null || element.getAttribute("id").length() == 0 ? "0"
			: element.getAttribute("id")); // 充值档位
		this.fileIconRes = element.getAttribute("fileIconRes"); // 图标路径
		String rechargeRewardString = element.getAttribute("rechargeReward"); // 充值奖励
		if (rechargeRewardString != null && rechargeRewardString.length() > 0) {
			String[] rechargeRewardStrings = rechargeRewardString.split("\\|"); 
			List<Entry<Integer,Integer>> rechargeReward = new ArrayList<Entry<Integer,Integer>>(rechargeRewardStrings.length) ; 
			for (int i = 0; i < rechargeRewardStrings.length; i++) {
			    String[] split = rechargeRewardStrings[i].split(":", 2);
				rechargeReward.add(new Entry<Integer,Integer>()	{
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

			this.rechargeReward = com.google.common.collect.ImmutableList.copyOf(rechargeReward);						
		} else {
			this.rechargeReward = java.util.Collections.emptyList();
		}
		String firstRechargeRewardString = element.getAttribute("firstRechargeReward"); // 首冲奖励
		if (firstRechargeRewardString != null && firstRechargeRewardString.length() > 0) {
			String[] firstRechargeRewardStrings = firstRechargeRewardString.split("\\|"); 
			List<Entry<Integer,Integer>> firstRechargeReward = new ArrayList<Entry<Integer,Integer>>(firstRechargeRewardStrings.length) ; 
			for (int i = 0; i < firstRechargeRewardStrings.length; i++) {
			    String[] split = firstRechargeRewardStrings[i].split(":", 2);
				firstRechargeReward.add(new Entry<Integer,Integer>()	{
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

			this.firstRechargeReward = com.google.common.collect.ImmutableList.copyOf(firstRechargeReward);						
		} else {
			this.firstRechargeReward = java.util.Collections.emptyList();
		}
		this.rechargeSdk = element.getAttribute("rechargeSdk"); // 充值接口
		this.rechargeAmount = Integer.parseInt(element.getAttribute("rechargeAmount") == null || element.getAttribute("rechargeAmount").length() == 0 ? "0"
			: element.getAttribute("rechargeAmount")); // 充值金额
		this.upDown = Boolean.parseBoolean(element.getAttribute("upDown") == null || element.getAttribute("upDown").length() == 0 ? "false"
			: element.getAttribute("upDown")); // 是否上架
	}
	
	public int getId() {
		return id;
	}
	
	public String getFileIconRes() {
		return fileIconRes;
	}
	
	public List<Entry<Integer,Integer>> getRechargeReward() {
		return rechargeReward;
	}
	
	public List<Entry<Integer,Integer>> getFirstRechargeReward() {
		return firstRechargeReward;
	}
	
	public String getRechargeSdk() {
		return rechargeSdk;
	}
	
	public int getRechargeAmount() {
		return rechargeAmount;
	}
	
	public boolean getUpDown() {
		return upDown;
	}
	
}
