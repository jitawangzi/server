package cn.game.protocol.generated.config;

import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Element;

/**
 * 亲密度
 * 
 * 工具生成的，不要手动修改
 */
 public class FriendlyConfig {

	/** id -- 相当于亲密度等级 */
	private final int id;		
	/** 好感值 -- 大于后升级 */
	private final int FriendlyValue;		
	/** 奖励属性 -- 百分比加成 */
	private final float RewardAttribute;		

	public FriendlyConfig (Element element) throws Exception {
	
		this.id = Integer.parseInt(element.getAttribute("id") == null || element.getAttribute("id").length() == 0 ? "0"
			: element.getAttribute("id")); // id
		this.FriendlyValue = Integer.parseInt(element.getAttribute("FriendlyValue") == null || element.getAttribute("FriendlyValue").length() == 0 ? "0"
			: element.getAttribute("FriendlyValue")); // 好感值
		this.RewardAttribute = Float.parseFloat(element.getAttribute("RewardAttribute") == null || element.getAttribute("RewardAttribute").length() == 0 ? "0"
			: element.getAttribute("RewardAttribute")); // 奖励属性
	}
	
	public int getId() {
		return id;
	}
	
	public int getFriendlyValue() {
		return FriendlyValue;
	}
	
	public float getRewardAttribute() {
		return RewardAttribute;
	}
	
}
