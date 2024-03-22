package cn.game.protocol.generated.config;
import org.w3c.dom.Element;

/**
 * 技能效果表
 * 
 * 工具生成的，不要手动修改
 */
 public class SkillEffectConfig {

	/** 效果id */
	private int id;		
	/** 添加时机 */
	private int createType;		
	/** 添加概率 */
	private int createRate;		
	/** buff添加到的目标 */
	private int addTarget;		
	/** 触发条件 */
	private int triggerCondition;		
	/** 效果触发概率 */
	private int effectTriggerRate;		
	/** 检测目标 */
	private int checkTarget;		
	/** 检测类型 */
	private int checkType;		
	/** 检测值 */
	private int checkValue;		
	/** 执行到的目标 */
	private int target;		
	/** 效果类型 */
	private int effectType;		
	/** 效果类型 */
	private int type;		
	/** 元素类型 */
	private int elementType;		
	/** 状态变化 */
	private int change;		
	/** 主属性类型 */
	private int attribute;		
	/** 数值类型 */
	private int valueType;		
	/** 数值 */
	private int value;		
	/** 叠加上限 */
	private int maxNum;		
	/** 持续回合 */
	private int round;		

	public SkillEffectConfig (Element element) {
	
		this.id = Integer.parseInt(element.getAttribute("id") == null || element.getAttribute("id").length() == 0 ? "0"
			: element.getAttribute("id")); // 效果id
		this.createType = Integer.parseInt(element.getAttribute("createType") == null || element.getAttribute("createType").length() == 0 ? "0"
			: element.getAttribute("createType")); // 添加时机
		this.createRate = Integer.parseInt(element.getAttribute("createRate") == null || element.getAttribute("createRate").length() == 0 ? "0"
			: element.getAttribute("createRate")); // 添加概率
		this.addTarget = Integer.parseInt(element.getAttribute("addTarget") == null || element.getAttribute("addTarget").length() == 0 ? "0"
			: element.getAttribute("addTarget")); // 添加目标
		this.triggerCondition = Integer.parseInt(element.getAttribute("triggerCondition") == null || element.getAttribute("triggerCondition").length() == 0 ? "0"
			: element.getAttribute("triggerCondition")); // 触发条件
		this.effectTriggerRate = Integer.parseInt(element.getAttribute("effectTriggerRate") == null || element.getAttribute("effectTriggerRate").length() == 0 ? "0"
			: element.getAttribute("effectTriggerRate")); // 效果触发概率
		this.checkTarget = Integer.parseInt(element.getAttribute("checkTarget") == null || element.getAttribute("checkTarget").length() == 0 ? "0"
			: element.getAttribute("checkTarget")); // 检测目标
		this.checkType = Integer.parseInt(element.getAttribute("checkType") == null || element.getAttribute("checkType").length() == 0 ? "0"
			: element.getAttribute("checkType")); // 检测类型
		this.checkValue = Integer.parseInt(element.getAttribute("checkValue") == null || element.getAttribute("checkValue").length() == 0 ? "0"
			: element.getAttribute("checkValue")); // 检测值
		this.target = Integer.parseInt(element.getAttribute("target") == null || element.getAttribute("target").length() == 0 ? "0"
			: element.getAttribute("target")); // 效果目标
		this.effectType = Integer.parseInt(element.getAttribute("effectType") == null || element.getAttribute("effectType").length() == 0 ? "0"
			: element.getAttribute("effectType")); // 效果类型
		this.type = Integer.parseInt(element.getAttribute("type") == null || element.getAttribute("type").length() == 0 ? "0"
			: element.getAttribute("type")); // 效果类型
		this.elementType = Integer.parseInt(element.getAttribute("elementType") == null || element.getAttribute("elementType").length() == 0 ? "0"
			: element.getAttribute("elementType")); // 元素类型
		this.change = Integer.parseInt(element.getAttribute("change") == null || element.getAttribute("change").length() == 0 ? "0"
			: element.getAttribute("change")); // 状态变化
		this.attribute = Integer.parseInt(element.getAttribute("attribute") == null || element.getAttribute("attribute").length() == 0 ? "0"
			: element.getAttribute("attribute")); // 主属性类型
		this.valueType = Integer.parseInt(element.getAttribute("valueType") == null || element.getAttribute("valueType").length() == 0 ? "0"
			: element.getAttribute("valueType")); // 数值类型
		this.value = Integer.parseInt(element.getAttribute("value") == null || element.getAttribute("value").length() == 0 ? "0"
			: element.getAttribute("value")); // 数值
		this.maxNum = Integer.parseInt(element.getAttribute("maxNum") == null || element.getAttribute("maxNum").length() == 0 ? "0"
			: element.getAttribute("maxNum")); // 叠加上限
		this.round = Integer.parseInt(element.getAttribute("round") == null || element.getAttribute("round").length() == 0 ? "0"
			: element.getAttribute("round")); // 持续回合
	}
	
	public int getId() {
		return id;
	}
	
	public int getCreateType() {
		return createType;
	}
	
	public int getCreateRate() {
		return createRate;
	}
	
	public int getAddTarget() {
		return addTarget;
	}
	
	public int getTriggerCondition() {
		return triggerCondition;
	}
	
	public int getEffectTriggerRate() {
		return effectTriggerRate;
	}
	
	public int getCheckTarget() {
		return checkTarget;
	}
	
	public int getCheckType() {
		return checkType;
	}
	
	public int getCheckValue() {
		return checkValue;
	}
	
	public int getTarget() {
		return target;
	}
	
	public int getEffectType() {
		return effectType;
	}
	
	public int getType() {
		return type;
	}
	
	public int getElementType() {
		return elementType;
	}
	
	public int getChange() {
		return change;
	}
	
	public int getAttribute() {
		return attribute;
	}
	
	public int getValueType() {
		return valueType;
	}
	
	public int getValue() {
		return value;
	}
	
	public int getMaxNum() {
		return maxNum;
	}
	
	public int getRound() {
		return round;
	}
	
}
