package cn.game.protocol.generated.config;

import org.w3c.dom.Element;


/**
 * 邮件
 * 
 * 工具生成的，不要手动修改
 */
 public class MailConfig {

	/** ID */
	public final int ID;		
	/** 类型  1：游戏公告 2：系统邮件 */
	public final int Type;		
	/** 奖励 */
	public final int[][] Reward;		
	/** 有效期 单位：秒 */
	public final int Expiration;		

	public MailConfig (Element element) throws Exception {
	
		ID = Integer.parseInt(element.getAttribute("ID") == null || element.getAttribute("ID").length() == 0 ? "0"
			: element.getAttribute("ID")); // ID
		Type = Integer.parseInt(element.getAttribute("Type") == null || element.getAttribute("Type").length() == 0 ? "0"
			: element.getAttribute("Type")); // 类型  1：游戏公告 2：系统邮件
		String RewardString = element.getAttribute("Reward"); // 奖励
		if (RewardString != null && RewardString.length() > 0) {
			String[] RewardStrings = RewardString.split("\\|"); 
			int[][] RewardTemp = new int[RewardStrings.length][] ; 
			for (int i = 0; i < RewardStrings.length; i++) {
				String[] RewardStrings2 = RewardStrings[i].split(";"); 
				int[] array = new int[RewardStrings2.length];
				for (int j = 0; j < RewardStrings2.length; j++) {
					int temp = Integer.parseInt(RewardStrings2[j]);	
					array[j] = temp;
				}
				RewardTemp[i] = array;
			}
			Reward = RewardTemp ;			
		} else {
			Reward = new int[][] {};
		}
		Expiration = Integer.parseInt(element.getAttribute("Expiration") == null || element.getAttribute("Expiration").length() == 0 ? "0"
			: element.getAttribute("Expiration")); // 有效期 单位：秒
	}
	

}
