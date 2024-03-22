package cn.game.protocol.generated.config;

import cn.game.protocol.generated.enume.EffectEnum;
import cn.game.protocol.generated.enume.BuffTypeEnum;
import cn.game.protocol.generated.enume.EffectTargetEnum;
import org.w3c.dom.Element;

/**
 * Buff表
 * 
 * 工具生成的，不要手动修改
 */
 public class OldBuffConfig {

	/** id -- 道具类:100000 探索buff类:200000 事件类:300000 养成类:400000 策略卡:500000 角色和SAN类:600000 怪物类:700000 装备类:800000 */
	private final int id;		
	/** 子buff */
	private final int[] subIds;		
	/** 目标id */
	private final int target;		
	/** 叠加类型 -- 0-覆盖（更新回合） 1-新获得不加 2-叠加（多个共存） */
	private final int addType;		
	/** 叠加最大层数限制 */
	private final int addLimit;		
	/** 类型 -- -1-无 101-眩晕 201-定身 202-沉默 203-封禁 204-禁疗 301-属性弱化 302-属性弱化S 401-属性强化 501-流血 502-腐蚀 503-燃烧 504-渐冻 505-恐惧 506-慌乱 601-治愈 602-冷静 801-失去资源 802-获得资源 901-专属 读取BuffTypeEnum */
	private final BuffTypeEnum type;		
	/** 生效参数(此回合变更时,持续效果生效) -- 读取BuffMomentEnum表 */
	private final int useType;		
	/** 生效次数 -- 999-无限 */
	private final int useNum;		
	/** 探索回合计数参数(此回合为0时,buff删除) -- 读取BuffMomentEnum表 888-表示不走回合 不填表示不存在 */
	private final int roundType;		
	/** 回合数 -- 获得buff的初始回合 0-一次性 999-无限 */
	private final int round;		
	/** 战斗中回合计数参数 -- 888-不走回合 1-角色行动 不填表示不存在 */
	private final int battleRoundType;		
	/** 额外结束条件 -- 1-补给小于某值 2-出探索地图时 */
	private final int[] endPara;		
	/** 目标类型 -- 确定buff加在哪个目标上 */
	private final EffectTargetEnum targetType;		
	/** 效果类型 */
	private final EffectEnum effectType;		
	/** ID参数 */
	private final int idParam;		
	/** 数量参数 */
	private final int numParam;		
	/** 数值类型 -- 0-绝对值（装备可用） 1-(基础)面板百分比 2-当前百分比 3-损失百分比 4-身体百分比（装备可用） 5-上限百分比 */
	private final int numTypeParam;		
	/** 扩展参数 */
	private final int[] extParam;		
	/** 不作用的类型 -- buff不作用于哪些目标 填ExploreObjectEnumId */
	private final int[] excludeObjects;		
	/** 是否同步 -- 前后端不同步，各自计算效果 0-同步 1-不同步 */
	private final boolean notSync;		

	public OldBuffConfig (Element element) throws Exception {
	
		this.id = Integer.parseInt(element.getAttribute("id") == null || element.getAttribute("id").length() == 0 ? "0"
			: element.getAttribute("id")); // id
		String subIdsString = element.getAttribute("subIds"); // 子buff
		if (subIdsString != null && subIdsString.length() > 0) {
			String[] subIdsStrings = subIdsString.split("\\|"); 
			int[] subIds = new int[subIdsStrings.length] ; 
			for (int i = 0; i < subIdsStrings.length; i++) {
				int temp = Integer.parseInt(subIdsStrings[i]);
				subIds[i] = temp;
			}
			this.subIds = subIds ;			
		} else {
			this.subIds = new int[] {};
		}
		this.target = Integer.parseInt(element.getAttribute("target") == null || element.getAttribute("target").length() == 0 ? "0"
			: element.getAttribute("target")); // 目标id
		this.addType = Integer.parseInt(element.getAttribute("addType") == null || element.getAttribute("addType").length() == 0 ? "0"
			: element.getAttribute("addType")); // 叠加类型
		this.addLimit = Integer.parseInt(element.getAttribute("addLimit") == null || element.getAttribute("addLimit").length() == 0 ? "0"
			: element.getAttribute("addLimit")); // 叠加最大层数限制
		this.type = BuffTypeEnum.get(Integer.parseInt(element.getAttribute("type")));	// 类型
		this.useType = Integer.parseInt(element.getAttribute("useType") == null || element.getAttribute("useType").length() == 0 ? "0"
			: element.getAttribute("useType")); // 生效参数(此回合变更时,持续效果生效)
		this.useNum = Integer.parseInt(element.getAttribute("useNum") == null || element.getAttribute("useNum").length() == 0 ? "0"
			: element.getAttribute("useNum")); // 生效次数
		this.roundType = Integer.parseInt(element.getAttribute("roundType") == null || element.getAttribute("roundType").length() == 0 ? "0"
			: element.getAttribute("roundType")); // 探索回合计数参数(此回合为0时,buff删除)
		this.round = Integer.parseInt(element.getAttribute("round") == null || element.getAttribute("round").length() == 0 ? "0"
			: element.getAttribute("round")); // 回合数
		this.battleRoundType = Integer.parseInt(element.getAttribute("battleRoundType") == null || element.getAttribute("battleRoundType").length() == 0 ? "0"
			: element.getAttribute("battleRoundType")); // 战斗中回合计数参数
		String endParaString = element.getAttribute("endPara"); // 额外结束条件
		if (endParaString != null && endParaString.length() > 0) {
			String[] endParaStrings = endParaString.split("\\|"); 
			int[] endPara = new int[endParaStrings.length] ; 
			for (int i = 0; i < endParaStrings.length; i++) {
				int temp = Integer.parseInt(endParaStrings[i]);
				endPara[i] = temp;
			}
			this.endPara = endPara ;			
		} else {
			this.endPara = new int[] {};
		}
		this.targetType = EffectTargetEnum.get(Integer.parseInt(element.getAttribute("targetType")));	// 目标类型
		this.effectType = EffectEnum.get(Integer.parseInt(element.getAttribute("effectType")));	// 效果类型
		this.idParam = Integer.parseInt(element.getAttribute("idParam") == null || element.getAttribute("idParam").length() == 0 ? "0"
			: element.getAttribute("idParam")); // ID参数
		this.numParam = Integer.parseInt(element.getAttribute("numParam") == null || element.getAttribute("numParam").length() == 0 ? "0"
			: element.getAttribute("numParam")); // 数量参数
		this.numTypeParam = Integer.parseInt(element.getAttribute("numTypeParam") == null || element.getAttribute("numTypeParam").length() == 0 ? "0"
			: element.getAttribute("numTypeParam")); // 数值类型
		String extParamString = element.getAttribute("extParam"); // 扩展参数
		if (extParamString != null && extParamString.length() > 0) {
			String[] extParamStrings = extParamString.split("\\|"); 
			int[] extParam = new int[extParamStrings.length] ; 
			for (int i = 0; i < extParamStrings.length; i++) {
				int temp = Integer.parseInt(extParamStrings[i]);
				extParam[i] = temp;
			}
			this.extParam = extParam ;			
		} else {
			this.extParam = new int[] {};
		}
		String excludeObjectsString = element.getAttribute("excludeObjects"); // 不作用的类型
		if (excludeObjectsString != null && excludeObjectsString.length() > 0) {
			String[] excludeObjectsStrings = excludeObjectsString.split("\\|"); 
			int[] excludeObjects = new int[excludeObjectsStrings.length] ; 
			for (int i = 0; i < excludeObjectsStrings.length; i++) {
				int temp = Integer.parseInt(excludeObjectsStrings[i]);
				excludeObjects[i] = temp;
			}
			this.excludeObjects = excludeObjects ;			
		} else {
			this.excludeObjects = new int[] {};
		}
		this.notSync = Boolean.parseBoolean(element.getAttribute("notSync") == null || element.getAttribute("notSync").length() == 0 ? "false"
			: element.getAttribute("notSync")); // 是否同步
	}
	
	public int getId() {
		return id;
	}
	
	public int[] getSubIds() {
		return subIds;
	}
	
	public int getTarget() {
		return target;
	}
	
	public int getAddType() {
		return addType;
	}
	
	public int getAddLimit() {
		return addLimit;
	}
	
	public BuffTypeEnum getType() {
		return type;
	}
	
	public int getUseType() {
		return useType;
	}
	
	public int getUseNum() {
		return useNum;
	}
	
	public int getRoundType() {
		return roundType;
	}
	
	public int getRound() {
		return round;
	}
	
	public int getBattleRoundType() {
		return battleRoundType;
	}
	
	public int[] getEndPara() {
		return endPara;
	}
	
	public EffectTargetEnum getTargetType() {
		return targetType;
	}
	
	public EffectEnum getEffectType() {
		return effectType;
	}
	
	public int getIdParam() {
		return idParam;
	}
	
	public int getNumParam() {
		return numParam;
	}
	
	public int getNumTypeParam() {
		return numTypeParam;
	}
	
	public int[] getExtParam() {
		return extParam;
	}
	
	public int[] getExcludeObjects() {
		return excludeObjects;
	}
	
	public boolean getNotSync() {
		return notSync;
	}
	
}
