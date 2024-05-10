package cn.game.protocol.generated.config;

import java.util.HashMap;
import java.util.Map;
import org.w3c.dom.Element;


/**
 * 肉鸽触发表
 * 
 * 工具生成的，不要手动修改
 */
 public class RoguelikeTriggerConfig {

	/** 流水号 */
	public final int ID;		
	/** 肉鸽触发次序  随战中每次肉鸽能量条满进行判断 */
	public final int RoguelikeTriggerOrder;		
	/** 肉鸽能量点数  每级战斗能量条exp */
	public final int RogueExp;		
	/** 专属战役标识  填Battle#战役id  只有专属战役才填，例如新手战役，其他战役全填999 */
	public final int ExclusiveBattleLogo;		
	/** 肉鸽大类 1-神将上阵    2-基础肉鸽   另：彩蛋肉鸽-GlobalConst#常量表 */
	public final int RoguelikeTriggerType;		
	/** 神将升级肉鸽最多出现个数 */
	public final int RogueHeroNum;		
	/** 神将升级肉鸽 控制组  蓝色神将升级肉鸽弹出权重；紫色神将升级肉鸽弹出权重；金色神将升级肉鸽弹出权重；红色神将升级肉鸽弹出权重；彩色神将升级肉鸽弹出权重；永恒神将升级肉鸽弹出权重；唯一神将升级肉鸽弹出权重 */
	public final int[] RogueHeroGroup;		
	/** 属性肉鸽 最多出现个数 */
	public final int RogueAttributeNum;		
	/** 属性肉鸽 控制组  属性1肉鸽id;权重|…|属性n肉鸽id;权重 */
	public final Map<Integer,Integer> RogueAttributeGroup;		
	/** 趣味肉鸽 最多出现个数 */
	public final int FunRogueNum;		
	/** 趣味肉鸽 控制组  趣味肉鸽1id;权重|…|趣味肉鸽2id;权重 */
	public final Map<Integer,Integer> FunRogueGroup;		

	public RoguelikeTriggerConfig (Element element) throws Exception {
	
		ID = Integer.parseInt(element.getAttribute("ID") == null || element.getAttribute("ID").length() == 0 ? "0"
			: element.getAttribute("ID")); // 流水号
		RoguelikeTriggerOrder = Integer.parseInt(element.getAttribute("RoguelikeTriggerOrder") == null || element.getAttribute("RoguelikeTriggerOrder").length() == 0 ? "0"
			: element.getAttribute("RoguelikeTriggerOrder")); // 肉鸽触发次序  随战中每次肉鸽能量条满进行判断
		RogueExp = Integer.parseInt(element.getAttribute("RogueExp") == null || element.getAttribute("RogueExp").length() == 0 ? "0"
			: element.getAttribute("RogueExp")); // 肉鸽能量点数  每级战斗能量条exp
		ExclusiveBattleLogo = Integer.parseInt(element.getAttribute("ExclusiveBattleLogo") == null || element.getAttribute("ExclusiveBattleLogo").length() == 0 ? "0"
			: element.getAttribute("ExclusiveBattleLogo")); // 专属战役标识  填Battle#战役id  只有专属战役才填，例如新手战役，其他战役全填999
		RoguelikeTriggerType = Integer.parseInt(element.getAttribute("RoguelikeTriggerType") == null || element.getAttribute("RoguelikeTriggerType").length() == 0 ? "0"
			: element.getAttribute("RoguelikeTriggerType")); // 肉鸽大类 1-神将上阵    2-基础肉鸽   另：彩蛋肉鸽-GlobalConst#常量表
		RogueHeroNum = Integer.parseInt(element.getAttribute("RogueHeroNum") == null || element.getAttribute("RogueHeroNum").length() == 0 ? "0"
			: element.getAttribute("RogueHeroNum")); // 神将升级肉鸽最多出现个数
		String RogueHeroGroupString = element.getAttribute("RogueHeroGroup"); // 神将升级肉鸽 控制组  蓝色神将升级肉鸽弹出权重；紫色神将升级肉鸽弹出权重；金色神将升级肉鸽弹出权重；红色神将升级肉鸽弹出权重；彩色神将升级肉鸽弹出权重；永恒神将升级肉鸽弹出权重；唯一神将升级肉鸽弹出权重
		if (RogueHeroGroupString != null && RogueHeroGroupString.length() > 0) {
			String[] RogueHeroGroupStrings = RogueHeroGroupString.split(";"); 
			int[] RogueHeroGroupTemp = new int[RogueHeroGroupStrings.length] ; 
			for (int i = 0; i < RogueHeroGroupStrings.length; i++) {
				int temp = Integer.parseInt(RogueHeroGroupStrings[i]);	
				RogueHeroGroupTemp[i] = temp;
			}
			RogueHeroGroup = RogueHeroGroupTemp ;			
		} else {
			RogueHeroGroup = new int[] {};
		}
		RogueAttributeNum = Integer.parseInt(element.getAttribute("RogueAttributeNum") == null || element.getAttribute("RogueAttributeNum").length() == 0 ? "0"
			: element.getAttribute("RogueAttributeNum")); // 属性肉鸽 最多出现个数
		String RogueAttributeGroupString = element.getAttribute("RogueAttributeGroup"); // 属性肉鸽 控制组  属性1肉鸽id;权重|…|属性n肉鸽id;权重
		if (RogueAttributeGroupString != null && RogueAttributeGroupString.length() > 0) {
			String[] RogueAttributeGroupStrings = RogueAttributeGroupString.split("\\|"); 
			Map<Integer,Integer> RogueAttributeGroupTemp = new HashMap<Integer,Integer>(RogueAttributeGroupStrings.length) ; 
			for (int i = 0; i < RogueAttributeGroupStrings.length; i++) {
				String[] split = RogueAttributeGroupStrings[i].split(";", 2);
				Integer key = Integer.parseInt(split[0]);
				Integer value = Integer.parseInt(split[1]);
				RogueAttributeGroupTemp.put(key, value) ; 				
			}
			RogueAttributeGroup = com.google.common.collect.ImmutableMap.copyOf(RogueAttributeGroupTemp);
		}else{
			RogueAttributeGroup = java.util.Collections.emptyMap() ; 
		}
		FunRogueNum = Integer.parseInt(element.getAttribute("FunRogueNum") == null || element.getAttribute("FunRogueNum").length() == 0 ? "0"
			: element.getAttribute("FunRogueNum")); // 趣味肉鸽 最多出现个数
		String FunRogueGroupString = element.getAttribute("FunRogueGroup"); // 趣味肉鸽 控制组  趣味肉鸽1id;权重|…|趣味肉鸽2id;权重
		if (FunRogueGroupString != null && FunRogueGroupString.length() > 0) {
			String[] FunRogueGroupStrings = FunRogueGroupString.split("\\|"); 
			Map<Integer,Integer> FunRogueGroupTemp = new HashMap<Integer,Integer>(FunRogueGroupStrings.length) ; 
			for (int i = 0; i < FunRogueGroupStrings.length; i++) {
				String[] split = FunRogueGroupStrings[i].split(";", 2);
				Integer key = Integer.parseInt(split[0]);
				Integer value = Integer.parseInt(split[1]);
				FunRogueGroupTemp.put(key, value) ; 				
			}
			FunRogueGroup = com.google.common.collect.ImmutableMap.copyOf(FunRogueGroupTemp);
		}else{
			FunRogueGroup = java.util.Collections.emptyMap() ; 
		}
	}
	

}
