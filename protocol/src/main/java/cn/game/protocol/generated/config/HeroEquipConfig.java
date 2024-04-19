package cn.game.protocol.generated.config;

import org.w3c.dom.Element;


/**
 * 法宝
 * 
 * 工具生成的，不要手动修改
 */
 public class HeroEquipConfig {

	/** 物品ID 物品id6位=种类型1位+物品类型2位+品质1位+序列号2位 */
	public final int ID;		
	/** 总类型 1-货币 2-物品 3-英雄 4-经验 5-能量 6-英雄武器 7-英雄装备 8-英雄时装 9-英雄宝石 10-龙 */
	public final int TotalType;		
	/** 英雄装备类型 1-头盔 2-披风 3-衣服 4-项链 5-戒指 6-靴子 */
	public final int ItemType;		
	/** 品质 1-白色 2-绿色 3-蓝色 4-紫色 5-金色 6-红色 7-彩色 8-永恒 9-唯一 */
	public final int Quality;		
	/** 星级 0星 1星 2星 3星 4星 */
	public final int Star;		
	/** 物品名称 */
	public final String Name;		
	/** 物品tips */
	public final String Tips;		
	/** 图标Icon 文件名 */
	public final String Icon;		
	/** 基础属性 属性id */
	public final int AttributeId;		
	/** 基础属性 初始值 */
	public final int InitialValue;		
	/** 基础属性 强化每级数值 */
	public final int StrengthenEachLevel;		
	/** 基础属性 每次突破数值 */
	public final int Breakthrough;		
	/** 【附加属性】  最多出现条目 */
	public final int ExtraAttributeNumber;		
	/** 【附加属性】id 调用RandomAttribute随机属性表id  【进行突破时】 如果属性为数值d类： 属性值*BreakthroughExtra1 如果属性为百分比%类： 属性值*BreakthroughExtra2 */
	public final int RandomAttributeId;		
	/** 【突破】影响【附加属性】【数值类d属性】  每进行突破，所有[Almost]AttrEffectConfig 表中AttributeType=1的数值类属性固定加成百分比 需/10000用 */
	public final int BreakthroughExtra1;		
	/** 【突破】影响响【附加属性】【百分比类%属性】  每进行突破，所有[Almost]AttrEffectConfig 表中AttributeType=2的百分比类属性固定加成百分比 需/10000用 */
	public final int BreakthroughExtra2;		
	/** 装备强化每级消耗金币增量   强化消耗道具数（item=204301）=装备强化等级lv+1 强化消耗金币数（item=100002）=当前等级*每级消耗金币增量 */
	public final int EnhanceConsumeMoney;		
	/** 装备突破 消耗id  调用：【Almost配置表_通用_消耗表】—【消耗表#Consume】中ID  按照34个星级分段即可 所有部位消耗一致 */
	public final int BreakthroughConsumeId;		
	/** 装备分解 消耗id  调用：【Almost配置表_通用_消耗表】—【消耗表#Consume】中ID  按照34个星级分段即可 所有部位消耗一致 */
	public final int SalvageConsumeId;		

	public HeroEquipConfig (Element element) throws Exception {
	
		ID = Integer.parseInt(element.getAttribute("ID") == null || element.getAttribute("ID").length() == 0 ? "0"
			: element.getAttribute("ID")); // 物品ID 物品id6位=种类型1位+物品类型2位+品质1位+序列号2位
		TotalType = Integer.parseInt(element.getAttribute("TotalType") == null || element.getAttribute("TotalType").length() == 0 ? "0"
			: element.getAttribute("TotalType")); // 总类型 1-货币 2-物品 3-英雄 4-经验 5-能量 6-英雄武器 7-英雄装备 8-英雄时装 9-英雄宝石 10-龙
		ItemType = Integer.parseInt(element.getAttribute("ItemType") == null || element.getAttribute("ItemType").length() == 0 ? "0"
			: element.getAttribute("ItemType")); // 英雄装备类型 1-头盔 2-披风 3-衣服 4-项链 5-戒指 6-靴子
		Quality = Integer.parseInt(element.getAttribute("Quality") == null || element.getAttribute("Quality").length() == 0 ? "0"
			: element.getAttribute("Quality")); // 品质 1-白色 2-绿色 3-蓝色 4-紫色 5-金色 6-红色 7-彩色 8-永恒 9-唯一
		Star = Integer.parseInt(element.getAttribute("Star") == null || element.getAttribute("Star").length() == 0 ? "0"
			: element.getAttribute("Star")); // 星级 0星 1星 2星 3星 4星
		Name = element.getAttribute("Name"); // 物品名称
		Tips = element.getAttribute("Tips"); // 物品tips
		Icon = element.getAttribute("Icon"); // 图标Icon 文件名
		AttributeId = Integer.parseInt(element.getAttribute("AttributeId") == null || element.getAttribute("AttributeId").length() == 0 ? "0"
			: element.getAttribute("AttributeId")); // 基础属性 属性id
		InitialValue = Integer.parseInt(element.getAttribute("InitialValue") == null || element.getAttribute("InitialValue").length() == 0 ? "0"
			: element.getAttribute("InitialValue")); // 基础属性 初始值
		StrengthenEachLevel = Integer.parseInt(element.getAttribute("StrengthenEachLevel") == null || element.getAttribute("StrengthenEachLevel").length() == 0 ? "0"
			: element.getAttribute("StrengthenEachLevel")); // 基础属性 强化每级数值
		Breakthrough = Integer.parseInt(element.getAttribute("Breakthrough") == null || element.getAttribute("Breakthrough").length() == 0 ? "0"
			: element.getAttribute("Breakthrough")); // 基础属性 每次突破数值
		ExtraAttributeNumber = Integer.parseInt(element.getAttribute("ExtraAttributeNumber") == null || element.getAttribute("ExtraAttributeNumber").length() == 0 ? "0"
			: element.getAttribute("ExtraAttributeNumber")); // 【附加属性】  最多出现条目
		RandomAttributeId = Integer.parseInt(element.getAttribute("RandomAttributeId") == null || element.getAttribute("RandomAttributeId").length() == 0 ? "0"
			: element.getAttribute("RandomAttributeId")); // 【附加属性】id 调用RandomAttribute随机属性表id  【进行突破时】 如果属性为数值d类： 属性值*BreakthroughExtra1 如果属性为百分比%类： 属性值*BreakthroughExtra2
		BreakthroughExtra1 = Integer.parseInt(element.getAttribute("BreakthroughExtra1") == null || element.getAttribute("BreakthroughExtra1").length() == 0 ? "0"
			: element.getAttribute("BreakthroughExtra1")); // 【突破】影响【附加属性】【数值类d属性】  每进行突破，所有[Almost]AttrEffectConfig 表中AttributeType=1的数值类属性固定加成百分比 需/10000用
		BreakthroughExtra2 = Integer.parseInt(element.getAttribute("BreakthroughExtra2") == null || element.getAttribute("BreakthroughExtra2").length() == 0 ? "0"
			: element.getAttribute("BreakthroughExtra2")); // 【突破】影响响【附加属性】【百分比类%属性】  每进行突破，所有[Almost]AttrEffectConfig 表中AttributeType=2的百分比类属性固定加成百分比 需/10000用
		EnhanceConsumeMoney = Integer.parseInt(element.getAttribute("EnhanceConsumeMoney") == null || element.getAttribute("EnhanceConsumeMoney").length() == 0 ? "0"
			: element.getAttribute("EnhanceConsumeMoney")); // 装备强化每级消耗金币增量   强化消耗道具数（item=204301）=装备强化等级lv+1 强化消耗金币数（item=100002）=当前等级*每级消耗金币增量
		BreakthroughConsumeId = Integer.parseInt(element.getAttribute("BreakthroughConsumeId") == null || element.getAttribute("BreakthroughConsumeId").length() == 0 ? "0"
			: element.getAttribute("BreakthroughConsumeId")); // 装备突破 消耗id  调用：【Almost配置表_通用_消耗表】—【消耗表#Consume】中ID  按照34个星级分段即可 所有部位消耗一致
		SalvageConsumeId = Integer.parseInt(element.getAttribute("SalvageConsumeId") == null || element.getAttribute("SalvageConsumeId").length() == 0 ? "0"
			: element.getAttribute("SalvageConsumeId")); // 装备分解 消耗id  调用：【Almost配置表_通用_消耗表】—【消耗表#Consume】中ID  按照34个星级分段即可 所有部位消耗一致
	}
	

}
