package cn.game.protocol.generated.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import cn.game.util.XmlUtils;
import cn.game.util.file.ResourceListener;
import cn.game.util.file.WatchServiceManager;

/**
 * 
 * 
 * 工具生成的，不要手动修改
 */
public class OldGlobalConst extends ResourceListener {

	private static final String xmlFileName = "OldGlobalConst";
	private static OldGlobalConst instance = new OldGlobalConst();

	/** 账号初始角色 */
	public static int heroInit;
	/** 角色突破最大星数 */
	public static int roleStarMax;
	/** 角色最大等级 */
	public static int roleLevelMax;
	/** 核心突破最大等级 */
	public static int coreBreakLevelMax;
	/** 源质最大等级 */
	public static int originLevelMax;
	/** 机甲强化最大等级 */
	public static int mechaLevelMax;
	/** 誓约消耗道具 */
	public static List<Entry<Integer, Integer>> oathExpendItem;
	/** 芯片强化上限 */
	public static List<Entry<Integer, Integer>> chipStrengthenMax;
	/** 芯片A解锁等级 */
	public static int chipAUnlockLevel;
	/** 芯片B解锁等级 */
	public static int chipBUnlockLevel;
	/** 芯片重构概率组 */
	public static List<Integer> chipReconstructProbabilityGroup;
	/** 芯片重构高低段分界 */
	public static int chipReconstructBoundary;
	/** 芯片重构概率步长 */
	public static int chipReconstructStep;
	/** 主要芯片词缀数量 */
	public static List<Entry<Integer, Integer>> mainChipAffixNumber;
	/** 次要芯片词缀数量 */
	public static List<Entry<Integer, Integer>> secondaryChipAffixNumber;
	/** 进阶训练奖励次数 */
	public static int routineTrainingRewardNum;
	/** 进阶训练奖励次数上限 */
	public static int routineTrainingRewardNumMax;
	/** 随机事件事件间隔 */
	public static int randomEventInterval;
	/** 随机事件上限 */
	public static int randomEventNumMax;
	/** 随机事件存在时间 */
	public static int randomEventTimer;
	/** 商店NPC好感度 */
	public static int npcRatio;
	/** 主线突发事件点数量 */
	public static int battleChapterEventDotNum;
	/** 创群加群常量 */
	public static List<Integer> groupConst;
	/** 阵型数量 */
	public static int teamFormationNumMax;
	/** 分解返还比例 */
	public static int decomposeReturnProportion;
	/** 初始看板娘角色ID */
	public static int initialRole;
	/** 初始角色 */
	public static List<Integer> initialRoles;
	/** 体力恢复 */
	public static int spiritReply;
	/** 体力购买 */
	public static List<Entry<Integer, Integer>> spiritBuy;
	/** 体力上限 */
	public static List<Entry<Integer, Integer>> spiritLimit;
	/** 体力领取 */
	public static int spiritReceive;
	/** 体力领取时间段 */
	public static List<String> spiritReceiveTime;
	/** 体力购买道具 */
	public static int spiritBuyItem;
	/** 邮件有效期 */
	public static int mailValidity;
	/** 邮件发送有效期 */
	public static int mailDeliveryValidity;
	/** 邮件最大数量 */
	public static int mailMax;
	/** 预置SAN值 */
	public static int exploreSanDefault;
	/** 预置SAN每步消耗值 */
	public static int exploreSanCostPerStep;
	/** 最大SAN值 */
	public static int exploreSanMax;
	/** SAN的属性影响值 */
	public static float exploreAttributePerSan;
	/** 污染值界限 */
	public static int explorePollutionLimit;
	/** 进入安全屋降低污染值 */
	public static int explorePollutionSafeHouse;
	/** 有怪物时每步污染值 */
	public static int explorePollutionPerStep;
	/** 污染区内每步污染值 */
	public static int explorePollutionZone;
	/** 近距离有怪物时污染值 */
	public static int explorePollutionPerMoster;
	/** 近距离有怪物时上限值 */
	public static int explorePollutionMosterMax;
	/** 灯塔的SAN恢复值 */
	public static int exploreLanternSan;
	/** 安全屋的SAN恢复值 */
	public static int exploreSafeHouseSan;
	/** 每次电池消耗数量 */
	public static int exploreLanternBattery;
	/** 预置视野值 */
	public static int exploreViewDefault;
	/** 视野每区域衰减值 */
	public static float exploreViewCostPerArea;
	/** 灯塔奖励视野值 */
	public static float exploreLanternRewardView;
	/** 实际最大视野值 */
	public static int exploreViewMax;
	/** 实际最小视野值 */
	public static int exploreViewMin;
	/** 受惩罚的视野值 */
	public static int explorePunishView;
	/** 每步受惩罚的概率 */
	public static int explorePunishRate;
	/** 每次购买的价格增长值 */
	public static int explorePriceIncreasePerBuy;
	/** 收集器最大容量 */
	public static int exploreCollecterMax;
	/** 收集器每次恢复生命 */
	public static int exploreCollecterHealPerTime;
	/** 魂契开始绑定的品质 */
	public static int exploreEquipmentBindingQuality;
	/** 魂契装备最大数量 */
	public static int exploreEquipmentEquipMaxNum;
	/** 探索阵容ID */
	public static int exploreTeamId;
	/** 安全屋的随机奖励 */
	public static int exploreSafeHouseRandonReward;
	/** 奇点概率权重 */
	public static List<Integer> singularityProbability;
	/** 新手卡池上限次数 */
	public static int drawNoviceLimit;
	/** 主线小怪之间最小间距 */
	public static int mainlineMonsterDistance;
	/** 主线世界玩家移动最大距离（一次位置同步） */
	public static int mainlinePlayerMoveDistanceMax;
	/** 初始化道具 */
	public static List<Entry<Integer, Integer>> initItems;
	/** 初始化建筑 */
	public static List<Entry<Integer, Integer>> initBuildings;
	/** 初始队伍 */
	public static List<Entry<Integer, Integer>> initExploreLineup;
	/** 初始化章节（序章） */
	public static int initExploreChapter;
	/** 初始化地图|初始化坐标 */
	public static List<Integer> initMap;
	/** 编队中保存队伍的最大人数 */
	public static int lineupRoleMax;
	/** 爬塔每一层战场数量 */
	public static int climbingTowerEachLayerBattleCount;
	/** 首次进入探索的角色 */
	public static List<Integer> firstExploreRole;
	/** 初始补给量 */
	public static int supplyNumbers;
	/** 空投初始补给量 */
	public static int airdropSupplyNumbers;
	/** 自然回复值（有补给时可恢复） */
	public static int naturalRecovery;
	/** 装备词缀数量随机 */
	public static List<Integer> equipmentAffixProbability;
	/** 等级单只怪物经验基础值 */
	public static int levelMonsterExpBaseValue;
	/** 金币区域结算基础值 */
	public static int coinRegionalSettlementBaseValue;
	/** 晶体产出结晶数量浮动区间 */
	public static List<Integer> crystallizeNumbersSection;
	/** 钛合金怪物产出浮动区间 */
	public static List<Integer> titaniumSection;
	/** 石墨烯怪物产出浮动区间 */
	public static List<Integer> grapheneSection;
	/** 碳纤维怪物产出浮动区间 */
	public static List<Integer> carbonFibreSection;
	/** 寄合质怪物产出浮动区间 */
	public static List<Integer> zygoteSection;
	/** 异导物怪物产出浮动区间 */
	public static List<Integer> heteroderivativeSection;
	/** 遗迹派遣最大人数 */
	public static int relicMaximum;
	/** 遗迹派遣队伍最少保留人数 */
	public static int relicMinimal;
	/** 派遣时职业系数 */
	public static List<Float> relicDominanceCoefficient;
	/** 事件默认发生概率 */
	public static List<Integer> relicEventProbability;
	/** 遗迹耗时系数 */
	public static List<Integer> relicTimeConsuming;
	/** 遗迹奖励份数 */
	public static List<Integer> relicReward;
	/** 地图类型强度权重 */
	public static Map<Integer, Integer> mapMonsterStrengthWeight;
	/** 主城事件得分系数 */
	public static List<Float> mainEventScoreRatio;
	/** 主城事件得分上限 */
	public static List<Float> mainEventScoreMax;
	/** 主城事件得分 */
	public static List<Float> mainEventScore;
	/** 主城事件中的探索度计算参数 */
	public static int mainCityExplore;
	/** 主城事件中的战斗得分计算参数 */
	public static List<Float> mainCityBattle;
	/** 主城事件中的收集得分计算参数 */
	public static List<Float> mainCityCollect;
	/** 主城事件完成一个任务事件加分数量 */
	public static int finishEvent;
	/** 主城事件完成一个副本加分数量 */
	public static int finishCopy;
	/** 主城事件重伤一名队员扣分数量 */
	public static int injuryMember;
	/** 主城事件死亡一名队员扣分数量 */
	public static int dieMember;
	/** 主城事件收集一次性宝箱 */
	public static int collectTreasureChest;
	/** 主城事件击杀追猎者 */
	public static int killStalker;
	/** 主城事件区间值 */
	public static List<Integer> mainCitySectionValue;
	/** 镜像死亡惩罚效果 */
	public static int deathPenalty;
	/** 无人机获得奖励内容 */
	public static List<Entry<Integer, Integer>> uvaReward;
	/** 无人机领取奖励间隔时长 */
	public static int uvaInterval;
	/** 策略卡组上限 */
	public static int cardGroupLmint;
	/** 自定义卡组名称长度 */
	public static int cardGroupLength;
	/** 初始化策略卡 */
	public static List<Integer> initStrategyCards;
	/** 默认初始卡组 */
	public static List<Integer> initialCardGroup;
	/** 每套卡组cost值 */
	public static int cardGroupCost;
	/** 策略点最大最小区间 */
	public static int strategyPointSection;
	/** 有效交互 */
	public static int effectiveInteractive;
	/** 杀死小怪 */
	public static int killLittleMonster;
	/** 杀死精英 */
	public static int killEliteMonster;
	/** 杀死特殊怪 */
	public static int killSpecialMonster;
	/** 完成区域任务（第二阶段的目标） */
	public static int finishGoal;
	/** 局间洗牌消耗 */
	public static List<Integer> strategyCardCost;
	/** 探索结算时策略卡出现的概率系数 */
	public static float strategyCardProbability;
	/** 局间医疗仓消耗 */
	public static List<Integer> medicineCost;
	/** 好感度最大值 */
	public static int favorLimit;
	/** 局间礼物卡的COST上限 */
	public static int giftCardsCostLimit;
	/** 制式装备额外消耗 */
	public static List<Integer> standardEquipmentCost;
	/** 探索装备额外消耗 */
	public static List<Integer> exploreEquipmentCost;
	/** 第二阶段区域boss的出现倒计时参数 */
	public static List<Integer> bossAppearCountDown;
	/** 游荡者死后生成精神体的概率 */
	public static int genSpritbodyProbability;
	/** 怪物重生回合 */
	public static int monsterRebornRound;
	/** 追猎者出现的概率 */
	public static int stalkerProbability;
	/** 追猎者超回合必出 */
	public static int exceedRoundGenStalker;
	/** 游荡者出现概率 */
	public static int switchInWorldGenWandererProbability;
	/** 游荡者出现在玩家多少范围外 */
	public static int genWandererRangeOutside;
	/** 游荡者出现在玩家多少范围内 */
	public static int genWandererRangeInside;
	/** 单张地图游荡者数量上限 */
	public static int mapWandererNumnberLimit;
	/** 进入映射域每回合消耗充能数量 */
	public static int mappingFieldCostCharging;
	/** 表世界充能最大上限 */
	public static int ChargingLimit;
	/** 进入映射域最小充能值 */
	public static int ChargingMin;
	/** 希格斯水晶单张地图生成数量 */
	public static int HiggsGenerate;
	/** 希格斯水晶共计需要数量 */
	public static int HiggsTotal;
	/** 角色解锁技能的晋升等级 */
	public static List<Integer> RoleUnlockSkillPromotionLevel;
	/** 固定装备解锁词缀的晋升等级 */
	public static List<Integer> EquipUnlockBuffPromotionLevel;
	/** 添加创伤效果 */
	public static int TraumaBuffId;
	/** 添加崩溃效果 */
	public static List<Integer> AddBreakDown;
	/** 战斗增减san值buff列表 */
	public static List<Integer> battleSanChangeBuffList;

	static {
		WatchServiceManager.getInstance().register(instance);
	}
	
	public static OldGlobalConst getInstance() {
		return instance ; 
	}
	
	public static void init (Element element) throws Exception {	
		heroInit = Integer.parseInt(
				element.getAttribute("heroInit") == null || element.getAttribute("heroInit").length() == 0 ? "0"
						: element.getAttribute("heroInit")); // 账号初始角色
		roleStarMax = Integer.parseInt(
				element.getAttribute("roleStarMax") == null || element.getAttribute("roleStarMax").length() == 0 ? "0"
						: element.getAttribute("roleStarMax")); // 角色突破最大星数
		roleLevelMax = Integer.parseInt(
				element.getAttribute("roleLevelMax") == null || element.getAttribute("roleLevelMax").length() == 0 ? "0"
						: element.getAttribute("roleLevelMax")); // 角色最大等级
		coreBreakLevelMax = Integer.parseInt(element.getAttribute("coreBreakLevelMax") == null
				|| element.getAttribute("coreBreakLevelMax").length() == 0 ? "0"
						: element.getAttribute("coreBreakLevelMax")); // 核心突破最大等级
		originLevelMax = Integer.parseInt(
				element.getAttribute("originLevelMax") == null || element.getAttribute("originLevelMax").length() == 0
						? "0"
						: element.getAttribute("originLevelMax")); // 源质最大等级
		mechaLevelMax = Integer.parseInt(
				element.getAttribute("mechaLevelMax") == null || element.getAttribute("mechaLevelMax").length() == 0
						? "0"
						: element.getAttribute("mechaLevelMax")); // 机甲强化最大等级
		String oathExpendItemString = element.getAttribute("oathExpendItem"); // 誓约消耗道具
		if (oathExpendItemString != null && oathExpendItemString.length() > 0) {
			String[] oathExpendItemStrings = oathExpendItemString.split("\\|");
			oathExpendItem = new ArrayList<Entry<Integer, Integer>>(oathExpendItemStrings.length);
			for (int i = 0; i < oathExpendItemStrings.length; i++) {
				String[] split = oathExpendItemStrings[i].split(":", 2);
				oathExpendItem.add(new Entry<Integer, Integer>() {
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
				});
			}
		} else {
			oathExpendItem = new ArrayList<Entry<Integer, Integer>>();
		}
		String chipStrengthenMaxString = element.getAttribute("chipStrengthenMax"); // 芯片强化上限
		if (chipStrengthenMaxString != null && chipStrengthenMaxString.length() > 0) {
			String[] chipStrengthenMaxStrings = chipStrengthenMaxString.split("\\|");
			chipStrengthenMax = new ArrayList<Entry<Integer, Integer>>(chipStrengthenMaxStrings.length);
			for (int i = 0; i < chipStrengthenMaxStrings.length; i++) {
				String[] split = chipStrengthenMaxStrings[i].split(":", 2);
				chipStrengthenMax.add(new Entry<Integer, Integer>() {
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
				});
			}
		} else {
			chipStrengthenMax = new ArrayList<Entry<Integer, Integer>>();
		}
		chipAUnlockLevel = Integer.parseInt(element.getAttribute("chipAUnlockLevel") == null
				|| element.getAttribute("chipAUnlockLevel").length() == 0 ? "0"
						: element.getAttribute("chipAUnlockLevel")); // 芯片A解锁等级
		chipBUnlockLevel = Integer.parseInt(element.getAttribute("chipBUnlockLevel") == null
				|| element.getAttribute("chipBUnlockLevel").length() == 0 ? "0"
						: element.getAttribute("chipBUnlockLevel")); // 芯片B解锁等级
		String chipReconstructProbabilityGroupString = element.getAttribute("chipReconstructProbabilityGroup"); // 芯片重构概率组
		if (chipReconstructProbabilityGroupString != null && chipReconstructProbabilityGroupString.length() > 0) {
			String[] chipReconstructProbabilityGroupStrings = chipReconstructProbabilityGroupString.split("\\|");
			chipReconstructProbabilityGroup = new ArrayList<Integer>(chipReconstructProbabilityGroupStrings.length);
			for (int i = 0; i < chipReconstructProbabilityGroupStrings.length; i++) {
				Integer temp = Integer.parseInt(chipReconstructProbabilityGroupStrings[i]);
				chipReconstructProbabilityGroup.add(temp);
			}
		} else {
			chipReconstructProbabilityGroup = new ArrayList<Integer>();
		}
		chipReconstructBoundary = Integer.parseInt(element.getAttribute("chipReconstructBoundary") == null
				|| element.getAttribute("chipReconstructBoundary").length() == 0 ? "0"
						: element.getAttribute("chipReconstructBoundary")); // 芯片重构高低段分界
		chipReconstructStep = Integer.parseInt(element.getAttribute("chipReconstructStep") == null
				|| element.getAttribute("chipReconstructStep").length() == 0 ? "0"
						: element.getAttribute("chipReconstructStep")); // 芯片重构概率步长
		String mainChipAffixNumberString = element.getAttribute("mainChipAffixNumber"); // 主要芯片词缀数量
		if (mainChipAffixNumberString != null && mainChipAffixNumberString.length() > 0) {
			String[] mainChipAffixNumberStrings = mainChipAffixNumberString.split("\\|");
			mainChipAffixNumber = new ArrayList<Entry<Integer, Integer>>(mainChipAffixNumberStrings.length);
			for (int i = 0; i < mainChipAffixNumberStrings.length; i++) {
				String[] split = mainChipAffixNumberStrings[i].split(":", 2);
				mainChipAffixNumber.add(new Entry<Integer, Integer>() {
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
				});
			}
		} else {
			mainChipAffixNumber = new ArrayList<Entry<Integer, Integer>>();
		}
		String secondaryChipAffixNumberString = element.getAttribute("secondaryChipAffixNumber"); // 次要芯片词缀数量
		if (secondaryChipAffixNumberString != null && secondaryChipAffixNumberString.length() > 0) {
			String[] secondaryChipAffixNumberStrings = secondaryChipAffixNumberString.split("\\|");
			secondaryChipAffixNumber = new ArrayList<Entry<Integer, Integer>>(secondaryChipAffixNumberStrings.length);
			for (int i = 0; i < secondaryChipAffixNumberStrings.length; i++) {
				String[] split = secondaryChipAffixNumberStrings[i].split(":", 2);
				secondaryChipAffixNumber.add(new Entry<Integer, Integer>() {
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
				});
			}
		} else {
			secondaryChipAffixNumber = new ArrayList<Entry<Integer, Integer>>();
		}
		routineTrainingRewardNum = Integer.parseInt(element.getAttribute("routineTrainingRewardNum") == null
				|| element.getAttribute("routineTrainingRewardNum").length() == 0 ? "0"
						: element.getAttribute("routineTrainingRewardNum")); // 进阶训练奖励次数
		routineTrainingRewardNumMax = Integer.parseInt(element.getAttribute("routineTrainingRewardNumMax") == null
				|| element.getAttribute("routineTrainingRewardNumMax").length() == 0 ? "0"
						: element.getAttribute("routineTrainingRewardNumMax")); // 进阶训练奖励次数上限
		randomEventInterval = Integer.parseInt(element.getAttribute("randomEventInterval") == null
				|| element.getAttribute("randomEventInterval").length() == 0 ? "0"
						: element.getAttribute("randomEventInterval")); // 随机事件事件间隔
		randomEventNumMax = Integer.parseInt(element.getAttribute("randomEventNumMax") == null
				|| element.getAttribute("randomEventNumMax").length() == 0 ? "0"
						: element.getAttribute("randomEventNumMax")); // 随机事件上限
		randomEventTimer = Integer.parseInt(element.getAttribute("randomEventTimer") == null
				|| element.getAttribute("randomEventTimer").length() == 0 ? "0"
						: element.getAttribute("randomEventTimer")); // 随机事件存在时间
		npcRatio = Integer.parseInt(
				element.getAttribute("npcRatio") == null || element.getAttribute("npcRatio").length() == 0 ? "0"
						: element.getAttribute("npcRatio")); // 商店NPC好感度
		battleChapterEventDotNum = Integer.parseInt(element.getAttribute("battleChapterEventDotNum") == null
				|| element.getAttribute("battleChapterEventDotNum").length() == 0 ? "0"
						: element.getAttribute("battleChapterEventDotNum")); // 主线突发事件点数量
		String groupConstString = element.getAttribute("groupConst"); // 创群加群常量
		if (groupConstString != null && groupConstString.length() > 0) {
			String[] groupConstStrings = groupConstString.split("\\|");
			groupConst = new ArrayList<Integer>(groupConstStrings.length);
			for (int i = 0; i < groupConstStrings.length; i++) {
				Integer temp = Integer.parseInt(groupConstStrings[i]);
				groupConst.add(temp);
			}
		} else {
			groupConst = new ArrayList<Integer>();
		}
		teamFormationNumMax = Integer.parseInt(element.getAttribute("teamFormationNumMax") == null
				|| element.getAttribute("teamFormationNumMax").length() == 0 ? "0"
						: element.getAttribute("teamFormationNumMax")); // 阵型数量
		decomposeReturnProportion = Integer.parseInt(element.getAttribute("decomposeReturnProportion") == null
				|| element.getAttribute("decomposeReturnProportion").length() == 0 ? "0"
						: element.getAttribute("decomposeReturnProportion")); // 分解返还比例
		initialRole = Integer.parseInt(
				element.getAttribute("initialRole") == null || element.getAttribute("initialRole").length() == 0 ? "0"
						: element.getAttribute("initialRole")); // 初始看板娘角色ID
		String initialRolesString = element.getAttribute("initialRoles"); // 初始角色
		if (initialRolesString != null && initialRolesString.length() > 0) {
			String[] initialRolesStrings = initialRolesString.split("\\|");
			initialRoles = new ArrayList<Integer>(initialRolesStrings.length);
			for (int i = 0; i < initialRolesStrings.length; i++) {
				Integer temp = Integer.parseInt(initialRolesStrings[i]);
				initialRoles.add(temp);
			}
		} else {
			initialRoles = new ArrayList<Integer>();
		}
		spiritReply = Integer.parseInt(
				element.getAttribute("spiritReply") == null || element.getAttribute("spiritReply").length() == 0 ? "0"
						: element.getAttribute("spiritReply")); // 体力恢复
		String spiritBuyString = element.getAttribute("spiritBuy"); // 体力购买
		if (spiritBuyString != null && spiritBuyString.length() > 0) {
			String[] spiritBuyStrings = spiritBuyString.split("\\|");
			spiritBuy = new ArrayList<Entry<Integer, Integer>>(spiritBuyStrings.length);
			for (int i = 0; i < spiritBuyStrings.length; i++) {
				String[] split = spiritBuyStrings[i].split(":", 2);
				spiritBuy.add(new Entry<Integer, Integer>() {
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
				});
			}
		} else {
			spiritBuy = new ArrayList<Entry<Integer, Integer>>();
		}
		String spiritLimitString = element.getAttribute("spiritLimit"); // 体力上限
		if (spiritLimitString != null && spiritLimitString.length() > 0) {
			String[] spiritLimitStrings = spiritLimitString.split("\\|");
			spiritLimit = new ArrayList<Entry<Integer, Integer>>(spiritLimitStrings.length);
			for (int i = 0; i < spiritLimitStrings.length; i++) {
				String[] split = spiritLimitStrings[i].split(":", 2);
				spiritLimit.add(new Entry<Integer, Integer>() {
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
				});
			}
		} else {
			spiritLimit = new ArrayList<Entry<Integer, Integer>>();
		}
		spiritReceive = Integer.parseInt(
				element.getAttribute("spiritReceive") == null || element.getAttribute("spiritReceive").length() == 0
						? "0"
						: element.getAttribute("spiritReceive")); // 体力领取
		String spiritReceiveTimeString = element.getAttribute("spiritReceiveTime"); // 体力领取时间段
		if (spiritReceiveTimeString != null && spiritReceiveTimeString.length() > 0) {
			String[] spiritReceiveTimeStrings = spiritReceiveTimeString.split("\\|");
			spiritReceiveTime = new ArrayList<String>(spiritReceiveTimeStrings.length);
			for (int i = 0; i < spiritReceiveTimeStrings.length; i++) {
				String temp = spiritReceiveTimeStrings[i];
				spiritReceiveTime.add(temp);
			}
		} else {
			spiritReceiveTime = new ArrayList<String>();
		}
		spiritBuyItem = Integer.parseInt(
				element.getAttribute("spiritBuyItem") == null || element.getAttribute("spiritBuyItem").length() == 0
						? "0"
						: element.getAttribute("spiritBuyItem")); // 体力购买道具
		mailValidity = Integer.parseInt(
				element.getAttribute("mailValidity") == null || element.getAttribute("mailValidity").length() == 0 ? "0"
						: element.getAttribute("mailValidity")); // 邮件有效期
		mailDeliveryValidity = Integer.parseInt(element.getAttribute("mailDeliveryValidity") == null
				|| element.getAttribute("mailDeliveryValidity").length() == 0 ? "0"
						: element.getAttribute("mailDeliveryValidity")); // 邮件发送有效期
		mailMax = Integer
				.parseInt(element.getAttribute("mailMax") == null || element.getAttribute("mailMax").length() == 0 ? "0"
						: element.getAttribute("mailMax")); // 邮件最大数量
		exploreSanDefault = Integer.parseInt(element.getAttribute("exploreSanDefault") == null
				|| element.getAttribute("exploreSanDefault").length() == 0 ? "0"
						: element.getAttribute("exploreSanDefault")); // 预置SAN值
		exploreSanCostPerStep = Integer.parseInt(element.getAttribute("exploreSanCostPerStep") == null
				|| element.getAttribute("exploreSanCostPerStep").length() == 0 ? "0"
						: element.getAttribute("exploreSanCostPerStep")); // 预置SAN每步消耗值
		exploreSanMax = Integer.parseInt(
				element.getAttribute("exploreSanMax") == null || element.getAttribute("exploreSanMax").length() == 0
						? "0"
						: element.getAttribute("exploreSanMax")); // 最大SAN值
		exploreAttributePerSan = Float.parseFloat(element.getAttribute("exploreAttributePerSan") == null
				|| element.getAttribute("exploreAttributePerSan").length() == 0 ? "0"
						: element.getAttribute("exploreAttributePerSan")); // SAN的属性影响值
		explorePollutionLimit = Integer.parseInt(element.getAttribute("explorePollutionLimit") == null
				|| element.getAttribute("explorePollutionLimit").length() == 0 ? "0"
						: element.getAttribute("explorePollutionLimit")); // 污染值界限
		explorePollutionSafeHouse = Integer.parseInt(element.getAttribute("explorePollutionSafeHouse") == null
				|| element.getAttribute("explorePollutionSafeHouse").length() == 0 ? "0"
						: element.getAttribute("explorePollutionSafeHouse")); // 进入安全屋降低污染值
		explorePollutionPerStep = Integer.parseInt(element.getAttribute("explorePollutionPerStep") == null
				|| element.getAttribute("explorePollutionPerStep").length() == 0 ? "0"
						: element.getAttribute("explorePollutionPerStep")); // 有怪物时每步污染值
		explorePollutionZone = Integer.parseInt(element.getAttribute("explorePollutionZone") == null
				|| element.getAttribute("explorePollutionZone").length() == 0 ? "0"
						: element.getAttribute("explorePollutionZone")); // 污染区内每步污染值
		explorePollutionPerMoster = Integer.parseInt(element.getAttribute("explorePollutionPerMoster") == null
				|| element.getAttribute("explorePollutionPerMoster").length() == 0 ? "0"
						: element.getAttribute("explorePollutionPerMoster")); // 近距离有怪物时污染值
		explorePollutionMosterMax = Integer.parseInt(element.getAttribute("explorePollutionMosterMax") == null
				|| element.getAttribute("explorePollutionMosterMax").length() == 0 ? "0"
						: element.getAttribute("explorePollutionMosterMax")); // 近距离有怪物时上限值
		exploreLanternSan = Integer.parseInt(element.getAttribute("exploreLanternSan") == null
				|| element.getAttribute("exploreLanternSan").length() == 0 ? "0"
						: element.getAttribute("exploreLanternSan")); // 灯塔的SAN恢复值
		exploreSafeHouseSan = Integer.parseInt(element.getAttribute("exploreSafeHouseSan") == null
				|| element.getAttribute("exploreSafeHouseSan").length() == 0 ? "0"
						: element.getAttribute("exploreSafeHouseSan")); // 安全屋的SAN恢复值
		exploreLanternBattery = Integer.parseInt(element.getAttribute("exploreLanternBattery") == null
				|| element.getAttribute("exploreLanternBattery").length() == 0 ? "0"
						: element.getAttribute("exploreLanternBattery")); // 每次电池消耗数量
		exploreViewDefault = Integer.parseInt(element.getAttribute("exploreViewDefault") == null
				|| element.getAttribute("exploreViewDefault").length() == 0 ? "0"
						: element.getAttribute("exploreViewDefault")); // 预置视野值
		exploreViewCostPerArea = Float.parseFloat(element.getAttribute("exploreViewCostPerArea") == null
				|| element.getAttribute("exploreViewCostPerArea").length() == 0 ? "0"
						: element.getAttribute("exploreViewCostPerArea")); // 视野每区域衰减值
		exploreLanternRewardView = Float.parseFloat(element.getAttribute("exploreLanternRewardView") == null
				|| element.getAttribute("exploreLanternRewardView").length() == 0 ? "0"
						: element.getAttribute("exploreLanternRewardView")); // 灯塔奖励视野值
		exploreViewMax = Integer.parseInt(
				element.getAttribute("exploreViewMax") == null || element.getAttribute("exploreViewMax").length() == 0
						? "0"
						: element.getAttribute("exploreViewMax")); // 实际最大视野值
		exploreViewMin = Integer.parseInt(
				element.getAttribute("exploreViewMin") == null || element.getAttribute("exploreViewMin").length() == 0
						? "0"
						: element.getAttribute("exploreViewMin")); // 实际最小视野值
		explorePunishView = Integer.parseInt(element.getAttribute("explorePunishView") == null
				|| element.getAttribute("explorePunishView").length() == 0 ? "0"
						: element.getAttribute("explorePunishView")); // 受惩罚的视野值
		explorePunishRate = Integer.parseInt(element.getAttribute("explorePunishRate") == null
				|| element.getAttribute("explorePunishRate").length() == 0 ? "0"
						: element.getAttribute("explorePunishRate")); // 每步受惩罚的概率
		explorePriceIncreasePerBuy = Integer.parseInt(element.getAttribute("explorePriceIncreasePerBuy") == null
				|| element.getAttribute("explorePriceIncreasePerBuy").length() == 0 ? "0"
						: element.getAttribute("explorePriceIncreasePerBuy")); // 每次购买的价格增长值
		exploreCollecterMax = Integer.parseInt(element.getAttribute("exploreCollecterMax") == null
				|| element.getAttribute("exploreCollecterMax").length() == 0 ? "0"
						: element.getAttribute("exploreCollecterMax")); // 收集器最大容量
		exploreCollecterHealPerTime = Integer.parseInt(element.getAttribute("exploreCollecterHealPerTime") == null
				|| element.getAttribute("exploreCollecterHealPerTime").length() == 0 ? "0"
						: element.getAttribute("exploreCollecterHealPerTime")); // 收集器每次恢复生命
		exploreEquipmentBindingQuality = Integer.parseInt(element.getAttribute("exploreEquipmentBindingQuality") == null
				|| element.getAttribute("exploreEquipmentBindingQuality").length() == 0 ? "0"
						: element.getAttribute("exploreEquipmentBindingQuality")); // 魂契开始绑定的品质
		exploreEquipmentEquipMaxNum = Integer.parseInt(element.getAttribute("exploreEquipmentEquipMaxNum") == null
				|| element.getAttribute("exploreEquipmentEquipMaxNum").length() == 0 ? "0"
						: element.getAttribute("exploreEquipmentEquipMaxNum")); // 魂契装备最大数量
		exploreTeamId = Integer.parseInt(
				element.getAttribute("exploreTeamId") == null || element.getAttribute("exploreTeamId").length() == 0
						? "0"
						: element.getAttribute("exploreTeamId")); // 探索阵容ID
		exploreSafeHouseRandonReward = Integer.parseInt(element.getAttribute("exploreSafeHouseRandonReward") == null
				|| element.getAttribute("exploreSafeHouseRandonReward").length() == 0 ? "0"
						: element.getAttribute("exploreSafeHouseRandonReward")); // 安全屋的随机奖励
		String singularityProbabilityString = element.getAttribute("singularityProbability"); // 奇点概率权重
		if (singularityProbabilityString != null && singularityProbabilityString.length() > 0) {
			String[] singularityProbabilityStrings = singularityProbabilityString.split("\\|");
			singularityProbability = new ArrayList<Integer>(singularityProbabilityStrings.length);
			for (int i = 0; i < singularityProbabilityStrings.length; i++) {
				Integer temp = Integer.parseInt(singularityProbabilityStrings[i]);
				singularityProbability.add(temp);
			}
		} else {
			singularityProbability = new ArrayList<Integer>();
		}
		drawNoviceLimit = Integer.parseInt(
				element.getAttribute("drawNoviceLimit") == null || element.getAttribute("drawNoviceLimit").length() == 0
						? "0"
						: element.getAttribute("drawNoviceLimit")); // 新手卡池上限次数
		mainlineMonsterDistance = Integer.parseInt(element.getAttribute("mainlineMonsterDistance") == null
				|| element.getAttribute("mainlineMonsterDistance").length() == 0 ? "0"
						: element.getAttribute("mainlineMonsterDistance")); // 主线小怪之间最小间距
		mainlinePlayerMoveDistanceMax = Integer.parseInt(element.getAttribute("mainlinePlayerMoveDistanceMax") == null
				|| element.getAttribute("mainlinePlayerMoveDistanceMax").length() == 0 ? "0"
						: element.getAttribute("mainlinePlayerMoveDistanceMax")); // 主线世界玩家移动最大距离（一次位置同步）
		String initItemsString = element.getAttribute("initItems"); // 初始化道具
		if (initItemsString != null && initItemsString.length() > 0) {
			String[] initItemsStrings = initItemsString.split("\\|"); 
			initItems = new ArrayList<Entry<Integer, Integer>>(initItemsStrings.length);
			for (int i = 0; i < initItemsStrings.length; i++) {
				String[] split = initItemsStrings[i].split(":", 2);
				initItems.add(new Entry<Integer, Integer>() {
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
				});
			}
		} else {
			initItems = new ArrayList<Entry<Integer, Integer>>();
		}
		String initBuildingsString = element.getAttribute("initBuildings"); // 初始化建筑
		if (initBuildingsString != null && initBuildingsString.length() > 0) {
			String[] initBuildingsStrings = initBuildingsString.split("\\|");
			initBuildings = new ArrayList<Entry<Integer, Integer>>(initBuildingsStrings.length);
			for (int i = 0; i < initBuildingsStrings.length; i++) {
				String[] split = initBuildingsStrings[i].split(":", 2);
				initBuildings.add(new Entry<Integer, Integer>() {
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
				});
			}
		} else {
			initBuildings = new ArrayList<Entry<Integer, Integer>>();
		}
		String initExploreLineupString = element.getAttribute("initExploreLineup"); // 初始队伍
		if (initExploreLineupString != null && initExploreLineupString.length() > 0) {
			String[] initExploreLineupStrings = initExploreLineupString.split("\\|");
			initExploreLineup = new ArrayList<Entry<Integer, Integer>>(initExploreLineupStrings.length);
			for (int i = 0; i < initExploreLineupStrings.length; i++) {
				String[] split = initExploreLineupStrings[i].split(":", 2);
				initExploreLineup.add(new Entry<Integer, Integer>() {
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
				});
			}
		} else {
			initExploreLineup = new ArrayList<Entry<Integer, Integer>>();
		}
		initExploreChapter = Integer.parseInt(element.getAttribute("initExploreChapter") == null
				|| element.getAttribute("initExploreChapter").length() == 0 ? "0"
						: element.getAttribute("initExploreChapter")); // 初始化章节（序章）
		String initMapString = element.getAttribute("initMap"); // 初始化地图|初始化坐标
		if (initMapString != null && initMapString.length() > 0) {
			String[] initMapStrings = initMapString.split("\\|");
			initMap = new ArrayList<Integer>(initMapStrings.length);
			for (int i = 0; i < initMapStrings.length; i++) {
				Integer temp = Integer.parseInt(initMapStrings[i]);
				initMap.add(temp);
			}
		} else {
			initMap = new ArrayList<Integer>();
		}
		lineupRoleMax = Integer.parseInt(
				element.getAttribute("lineupRoleMax") == null || element.getAttribute("lineupRoleMax").length() == 0
						? "0"
						: element.getAttribute("lineupRoleMax")); // 编队中保存队伍的最大人数
		climbingTowerEachLayerBattleCount = Integer
				.parseInt(element.getAttribute("climbingTowerEachLayerBattleCount") == null
						|| element.getAttribute("climbingTowerEachLayerBattleCount").length() == 0 ? "0"
								: element.getAttribute("climbingTowerEachLayerBattleCount")); // 爬塔每一层战场数量
		String firstExploreRoleString = element.getAttribute("firstExploreRole"); // 首次进入探索的角色
		if (firstExploreRoleString != null && firstExploreRoleString.length() > 0) {
			String[] firstExploreRoleStrings = firstExploreRoleString.split("\\|");
			firstExploreRole = new ArrayList<Integer>(firstExploreRoleStrings.length);
			for (int i = 0; i < firstExploreRoleStrings.length; i++) {
				Integer temp = Integer.parseInt(firstExploreRoleStrings[i]);
				firstExploreRole.add(temp);
			}
		} else {
			firstExploreRole = new ArrayList<Integer>();
		}
		supplyNumbers = Integer.parseInt(
				element.getAttribute("supplyNumbers") == null || element.getAttribute("supplyNumbers").length() == 0
						? "0"
						: element.getAttribute("supplyNumbers")); // 初始补给量
		airdropSupplyNumbers = Integer.parseInt(element.getAttribute("airdropSupplyNumbers") == null
				|| element.getAttribute("airdropSupplyNumbers").length() == 0 ? "0"
						: element.getAttribute("airdropSupplyNumbers")); // 空投初始补给量
		naturalRecovery = Integer.parseInt(
				element.getAttribute("naturalRecovery") == null || element.getAttribute("naturalRecovery").length() == 0
						? "0"
						: element.getAttribute("naturalRecovery")); // 自然回复值（有补给时可恢复）
		String equipmentAffixProbabilityString = element.getAttribute("equipmentAffixProbability"); // 装备词缀数量随机
		if (equipmentAffixProbabilityString != null && equipmentAffixProbabilityString.length() > 0) {
			String[] equipmentAffixProbabilityStrings = equipmentAffixProbabilityString.split("\\|");
			equipmentAffixProbability = new ArrayList<Integer>(equipmentAffixProbabilityStrings.length);
			for (int i = 0; i < equipmentAffixProbabilityStrings.length; i++) {
				Integer temp = Integer.parseInt(equipmentAffixProbabilityStrings[i]);
				equipmentAffixProbability.add(temp);
			}
		} else {
			equipmentAffixProbability = new ArrayList<Integer>();
		}
		levelMonsterExpBaseValue = Integer.parseInt(element.getAttribute("levelMonsterExpBaseValue") == null
				|| element.getAttribute("levelMonsterExpBaseValue").length() == 0 ? "0"
						: element.getAttribute("levelMonsterExpBaseValue")); // 等级单只怪物经验基础值
		coinRegionalSettlementBaseValue = Integer
				.parseInt(element.getAttribute("coinRegionalSettlementBaseValue") == null
						|| element.getAttribute("coinRegionalSettlementBaseValue").length() == 0 ? "0"
								: element.getAttribute("coinRegionalSettlementBaseValue")); // 金币区域结算基础值
		String crystallizeNumbersSectionString = element.getAttribute("crystallizeNumbersSection"); // 晶体产出结晶数量浮动区间
		if (crystallizeNumbersSectionString != null && crystallizeNumbersSectionString.length() > 0) {
			String[] crystallizeNumbersSectionStrings = crystallizeNumbersSectionString.split("\\|");
			crystallizeNumbersSection = new ArrayList<Integer>(crystallizeNumbersSectionStrings.length);
			for (int i = 0; i < crystallizeNumbersSectionStrings.length; i++) {
				Integer temp = Integer.parseInt(crystallizeNumbersSectionStrings[i]);
				crystallizeNumbersSection.add(temp);
			}
		} else {
			crystallizeNumbersSection = new ArrayList<Integer>();
		}
		String titaniumSectionString = element.getAttribute("titaniumSection"); // 钛合金怪物产出浮动区间
		if (titaniumSectionString != null && titaniumSectionString.length() > 0) {
			String[] titaniumSectionStrings = titaniumSectionString.split("\\|");
			titaniumSection = new ArrayList<Integer>(titaniumSectionStrings.length);
			for (int i = 0; i < titaniumSectionStrings.length; i++) {
				Integer temp = Integer.parseInt(titaniumSectionStrings[i]);
				titaniumSection.add(temp);
			}
		} else {
			titaniumSection = new ArrayList<Integer>();
		}
		String grapheneSectionString = element.getAttribute("grapheneSection"); // 石墨烯怪物产出浮动区间
		if (grapheneSectionString != null && grapheneSectionString.length() > 0) {
			String[] grapheneSectionStrings = grapheneSectionString.split("\\|");
			grapheneSection = new ArrayList<Integer>(grapheneSectionStrings.length);
			for (int i = 0; i < grapheneSectionStrings.length; i++) {
				Integer temp = Integer.parseInt(grapheneSectionStrings[i]);
				grapheneSection.add(temp);
			}
		} else {
			grapheneSection = new ArrayList<Integer>();
		}
		String carbonFibreSectionString = element.getAttribute("carbonFibreSection"); // 碳纤维怪物产出浮动区间
		if (carbonFibreSectionString != null && carbonFibreSectionString.length() > 0) {
			String[] carbonFibreSectionStrings = carbonFibreSectionString.split("\\|");
			carbonFibreSection = new ArrayList<Integer>(carbonFibreSectionStrings.length);
			for (int i = 0; i < carbonFibreSectionStrings.length; i++) {
				Integer temp = Integer.parseInt(carbonFibreSectionStrings[i]);
				carbonFibreSection.add(temp);
			}
		} else {
			carbonFibreSection = new ArrayList<Integer>();
		}
		String zygoteSectionString = element.getAttribute("zygoteSection"); // 寄合质怪物产出浮动区间
		if (zygoteSectionString != null && zygoteSectionString.length() > 0) {
			String[] zygoteSectionStrings = zygoteSectionString.split("\\|");
			zygoteSection = new ArrayList<Integer>(zygoteSectionStrings.length);
			for (int i = 0; i < zygoteSectionStrings.length; i++) {
				Integer temp = Integer.parseInt(zygoteSectionStrings[i]);
				zygoteSection.add(temp);
			}
		} else {
			zygoteSection = new ArrayList<Integer>();
		}
		String heteroderivativeSectionString = element.getAttribute("heteroderivativeSection"); // 异导物怪物产出浮动区间
		if (heteroderivativeSectionString != null && heteroderivativeSectionString.length() > 0) {
			String[] heteroderivativeSectionStrings = heteroderivativeSectionString.split("\\|");
			heteroderivativeSection = new ArrayList<Integer>(heteroderivativeSectionStrings.length);
			for (int i = 0; i < heteroderivativeSectionStrings.length; i++) {
				Integer temp = Integer.parseInt(heteroderivativeSectionStrings[i]);
				heteroderivativeSection.add(temp);
			}
		} else {
			heteroderivativeSection = new ArrayList<Integer>();
		}
		relicMaximum = Integer.parseInt(
				element.getAttribute("relicMaximum") == null || element.getAttribute("relicMaximum").length() == 0 ? "0"
						: element.getAttribute("relicMaximum")); // 遗迹派遣最大人数
		relicMinimal = Integer.parseInt(
				element.getAttribute("relicMinimal") == null || element.getAttribute("relicMinimal").length() == 0 ? "0"
						: element.getAttribute("relicMinimal")); // 遗迹派遣队伍最少保留人数
		String relicDominanceCoefficientString = element.getAttribute("relicDominanceCoefficient"); // 派遣时职业系数
		if (relicDominanceCoefficientString != null && relicDominanceCoefficientString.length() > 0) {
			String[] relicDominanceCoefficientStrings = relicDominanceCoefficientString.split("\\|");
			relicDominanceCoefficient = new ArrayList<Float>(relicDominanceCoefficientStrings.length);
			for (int i = 0; i < relicDominanceCoefficientStrings.length; i++) {
				Float temp = Float.parseFloat(relicDominanceCoefficientStrings[i]);
				relicDominanceCoefficient.add(temp);
			}
		} else {
			relicDominanceCoefficient = new ArrayList<Float>();
		}
		String relicEventProbabilityString = element.getAttribute("relicEventProbability"); // 事件默认发生概率
		if (relicEventProbabilityString != null && relicEventProbabilityString.length() > 0) {
			String[] relicEventProbabilityStrings = relicEventProbabilityString.split("\\|");
			relicEventProbability = new ArrayList<Integer>(relicEventProbabilityStrings.length);
			for (int i = 0; i < relicEventProbabilityStrings.length; i++) {
				Integer temp = Integer.parseInt(relicEventProbabilityStrings[i]);
				relicEventProbability.add(temp);
			}
		} else {
			relicEventProbability = new ArrayList<Integer>();
		}
		String relicTimeConsumingString = element.getAttribute("relicTimeConsuming"); // 遗迹耗时系数
		if (relicTimeConsumingString != null && relicTimeConsumingString.length() > 0) {
			String[] relicTimeConsumingStrings = relicTimeConsumingString.split("\\|");
			relicTimeConsuming = new ArrayList<Integer>(relicTimeConsumingStrings.length);
			for (int i = 0; i < relicTimeConsumingStrings.length; i++) {
				Integer temp = Integer.parseInt(relicTimeConsumingStrings[i]);
				relicTimeConsuming.add(temp);
			}
		} else {
			relicTimeConsuming = new ArrayList<Integer>();
		}
		String relicRewardString = element.getAttribute("relicReward"); // 遗迹奖励份数
		if (relicRewardString != null && relicRewardString.length() > 0) {
			String[] relicRewardStrings = relicRewardString.split("\\|");
			relicReward = new ArrayList<Integer>(relicRewardStrings.length);
			for (int i = 0; i < relicRewardStrings.length; i++) {
				Integer temp = Integer.parseInt(relicRewardStrings[i]);
				relicReward.add(temp);
			}
		} else {
			relicReward = new ArrayList<Integer>();
		}
		String mapMonsterStrengthWeightString = element.getAttribute("mapMonsterStrengthWeight"); // 地图类型强度权重
		if (mapMonsterStrengthWeightString != null && mapMonsterStrengthWeightString.length() > 0) {
			String[] mapMonsterStrengthWeightStrings = mapMonsterStrengthWeightString.split("\\|");
			mapMonsterStrengthWeight = new HashMap<Integer, Integer>(mapMonsterStrengthWeightStrings.length);
			for (int i = 0; i < mapMonsterStrengthWeightStrings.length; i++) {
				String[] split = mapMonsterStrengthWeightStrings[i].split(":", 2);
				Integer key = Integer.parseInt(split[0]);
				Integer value = Integer.parseInt(split[1]);
				mapMonsterStrengthWeight.put(key, value);
			}
		} else {
			mapMonsterStrengthWeight = new HashMap<Integer, Integer>();
		}
		String mainEventScoreRatioString = element.getAttribute("mainEventScoreRatio"); // 主城事件得分系数
		if (mainEventScoreRatioString != null && mainEventScoreRatioString.length() > 0) {
			String[] mainEventScoreRatioStrings = mainEventScoreRatioString.split("\\|");
			mainEventScoreRatio = new ArrayList<Float>(mainEventScoreRatioStrings.length);
			for (int i = 0; i < mainEventScoreRatioStrings.length; i++) {
				Float temp = Float.parseFloat(mainEventScoreRatioStrings[i]);
				mainEventScoreRatio.add(temp);
			}
		} else {
			mainEventScoreRatio = new ArrayList<Float>();
		}
		String mainEventScoreMaxString = element.getAttribute("mainEventScoreMax"); // 主城事件得分上限
		if (mainEventScoreMaxString != null && mainEventScoreMaxString.length() > 0) {
			String[] mainEventScoreMaxStrings = mainEventScoreMaxString.split("\\|");
			mainEventScoreMax = new ArrayList<Float>(mainEventScoreMaxStrings.length);
			for (int i = 0; i < mainEventScoreMaxStrings.length; i++) {
				Float temp = Float.parseFloat(mainEventScoreMaxStrings[i]);
				mainEventScoreMax.add(temp);
			}
		} else {
			mainEventScoreMax = new ArrayList<Float>();
		}
		String mainEventScoreString = element.getAttribute("mainEventScore"); // 主城事件得分
		if (mainEventScoreString != null && mainEventScoreString.length() > 0) {
			String[] mainEventScoreStrings = mainEventScoreString.split("\\|");
			mainEventScore = new ArrayList<Float>(mainEventScoreStrings.length);
			for (int i = 0; i < mainEventScoreStrings.length; i++) {
				Float temp = Float.parseFloat(mainEventScoreStrings[i]);
				mainEventScore.add(temp);
			}
		} else {
			mainEventScore = new ArrayList<Float>();
		}
		mainCityExplore = Integer.parseInt(
				element.getAttribute("mainCityExplore") == null || element.getAttribute("mainCityExplore").length() == 0
						? "0"
						: element.getAttribute("mainCityExplore")); // 主城事件中的探索度计算参数
		String mainCityBattleString = element.getAttribute("mainCityBattle"); // 主城事件中的战斗得分计算参数
		if (mainCityBattleString != null && mainCityBattleString.length() > 0) {
			String[] mainCityBattleStrings = mainCityBattleString.split("\\|");
			mainCityBattle = new ArrayList<Float>(mainCityBattleStrings.length);
			for (int i = 0; i < mainCityBattleStrings.length; i++) {
				Float temp = Float.parseFloat(mainCityBattleStrings[i]);
				mainCityBattle.add(temp);
			}
		} else {
			mainCityBattle = new ArrayList<Float>();
		}
		String mainCityCollectString = element.getAttribute("mainCityCollect"); // 主城事件中的收集得分计算参数
		if (mainCityCollectString != null && mainCityCollectString.length() > 0) {
			String[] mainCityCollectStrings = mainCityCollectString.split("\\|");
			mainCityCollect = new ArrayList<Float>(mainCityCollectStrings.length);
			for (int i = 0; i < mainCityCollectStrings.length; i++) {
				Float temp = Float.parseFloat(mainCityCollectStrings[i]);
				mainCityCollect.add(temp);
			}
		} else {
			mainCityCollect = new ArrayList<Float>();
		}
		finishEvent = Integer.parseInt(
				element.getAttribute("finishEvent") == null || element.getAttribute("finishEvent").length() == 0 ? "0"
						: element.getAttribute("finishEvent")); // 主城事件完成一个任务事件加分数量
		finishCopy = Integer.parseInt(
				element.getAttribute("finishCopy") == null || element.getAttribute("finishCopy").length() == 0 ? "0"
						: element.getAttribute("finishCopy")); // 主城事件完成一个副本加分数量
		injuryMember = Integer.parseInt(
				element.getAttribute("injuryMember") == null || element.getAttribute("injuryMember").length() == 0 ? "0"
						: element.getAttribute("injuryMember")); // 主城事件重伤一名队员扣分数量
		dieMember = Integer.parseInt(
				element.getAttribute("dieMember") == null || element.getAttribute("dieMember").length() == 0 ? "0"
						: element.getAttribute("dieMember")); // 主城事件死亡一名队员扣分数量
		collectTreasureChest = Integer.parseInt(element.getAttribute("collectTreasureChest") == null
				|| element.getAttribute("collectTreasureChest").length() == 0 ? "0"
						: element.getAttribute("collectTreasureChest")); // 主城事件收集一次性宝箱
		killStalker = Integer.parseInt(
				element.getAttribute("killStalker") == null || element.getAttribute("killStalker").length() == 0 ? "0"
						: element.getAttribute("killStalker")); // 主城事件击杀追猎者
		String mainCitySectionValueString = element.getAttribute("mainCitySectionValue"); // 主城事件区间值
		if (mainCitySectionValueString != null && mainCitySectionValueString.length() > 0) {
			String[] mainCitySectionValueStrings = mainCitySectionValueString.split("\\|");
			mainCitySectionValue = new ArrayList<Integer>(mainCitySectionValueStrings.length);
			for (int i = 0; i < mainCitySectionValueStrings.length; i++) {
				Integer temp = Integer.parseInt(mainCitySectionValueStrings[i]);
				mainCitySectionValue.add(temp);
			}
		} else {
			mainCitySectionValue = new ArrayList<Integer>();
		}
		deathPenalty = Integer.parseInt(
				element.getAttribute("deathPenalty") == null || element.getAttribute("deathPenalty").length() == 0 ? "0"
						: element.getAttribute("deathPenalty")); // 镜像死亡惩罚效果
		String uvaRewardString = element.getAttribute("uvaReward"); // 无人机获得奖励内容
		if (uvaRewardString != null && uvaRewardString.length() > 0) {
			String[] uvaRewardStrings = uvaRewardString.split("\\|");
			uvaReward = new ArrayList<Entry<Integer, Integer>>(uvaRewardStrings.length);
			for (int i = 0; i < uvaRewardStrings.length; i++) {
				String[] split = uvaRewardStrings[i].split(":", 2);
				uvaReward.add(new Entry<Integer, Integer>() {
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
				});
			}
		} else {
			uvaReward = new ArrayList<Entry<Integer, Integer>>();
		}
		uvaInterval = Integer.parseInt(
				element.getAttribute("uvaInterval") == null || element.getAttribute("uvaInterval").length() == 0 ? "0"
						: element.getAttribute("uvaInterval")); // 无人机领取奖励间隔时长
		cardGroupLmint = Integer.parseInt(
				element.getAttribute("cardGroupLmint") == null || element.getAttribute("cardGroupLmint").length() == 0
						? "0"
						: element.getAttribute("cardGroupLmint")); // 策略卡组上限
		cardGroupLength = Integer.parseInt(
				element.getAttribute("cardGroupLength") == null || element.getAttribute("cardGroupLength").length() == 0
						? "0"
						: element.getAttribute("cardGroupLength")); // 自定义卡组名称长度
		String initStrategyCardsString = element.getAttribute("initStrategyCards"); // 初始化策略卡
		if (initStrategyCardsString != null && initStrategyCardsString.length() > 0) {
			String[] initStrategyCardsStrings = initStrategyCardsString.split("\\|");
			initStrategyCards = new ArrayList<Integer>(initStrategyCardsStrings.length);
			for (int i = 0; i < initStrategyCardsStrings.length; i++) {
				Integer temp = Integer.parseInt(initStrategyCardsStrings[i]);
				initStrategyCards.add(temp);
			}
		} else {
			initStrategyCards = new ArrayList<Integer>();
		}
		String initialCardGroupString = element.getAttribute("initialCardGroup"); // 默认初始卡组
		if (initialCardGroupString != null && initialCardGroupString.length() > 0) {
			String[] initialCardGroupStrings = initialCardGroupString.split("\\|");
			initialCardGroup = new ArrayList<Integer>(initialCardGroupStrings.length);
			for (int i = 0; i < initialCardGroupStrings.length; i++) {
				Integer temp = Integer.parseInt(initialCardGroupStrings[i]);
				initialCardGroup.add(temp);
			}
		} else {
			initialCardGroup = new ArrayList<Integer>();
		}
		cardGroupCost = Integer.parseInt(
				element.getAttribute("cardGroupCost") == null || element.getAttribute("cardGroupCost").length() == 0
						? "0"
						: element.getAttribute("cardGroupCost")); // 每套卡组cost值
		strategyPointSection = Integer.parseInt(element.getAttribute("strategyPointSection") == null
				|| element.getAttribute("strategyPointSection").length() == 0 ? "0"
						: element.getAttribute("strategyPointSection")); // 策略点最大最小区间
		effectiveInteractive = Integer.parseInt(element.getAttribute("effectiveInteractive") == null
				|| element.getAttribute("effectiveInteractive").length() == 0 ? "0"
						: element.getAttribute("effectiveInteractive")); // 有效交互
		killLittleMonster = Integer.parseInt(element.getAttribute("killLittleMonster") == null
				|| element.getAttribute("killLittleMonster").length() == 0 ? "0"
						: element.getAttribute("killLittleMonster")); // 杀死小怪
		killEliteMonster = Integer.parseInt(element.getAttribute("killEliteMonster") == null
				|| element.getAttribute("killEliteMonster").length() == 0 ? "0"
						: element.getAttribute("killEliteMonster")); // 杀死精英
		killSpecialMonster = Integer.parseInt(element.getAttribute("killSpecialMonster") == null
				|| element.getAttribute("killSpecialMonster").length() == 0 ? "0"
						: element.getAttribute("killSpecialMonster")); // 杀死特殊怪
		finishGoal = Integer.parseInt(
				element.getAttribute("finishGoal") == null || element.getAttribute("finishGoal").length() == 0 ? "0"
						: element.getAttribute("finishGoal")); // 完成区域任务（第二阶段的目标）
		String strategyCardCostString = element.getAttribute("strategyCardCost"); // 局间洗牌消耗
		if (strategyCardCostString != null && strategyCardCostString.length() > 0) {
			String[] strategyCardCostStrings = strategyCardCostString.split("\\|");
			strategyCardCost = new ArrayList<Integer>(strategyCardCostStrings.length);
			for (int i = 0; i < strategyCardCostStrings.length; i++) {
				Integer temp = Integer.parseInt(strategyCardCostStrings[i]);
				strategyCardCost.add(temp);
			}
		} else {
			strategyCardCost = new ArrayList<Integer>();
		}
		strategyCardProbability = Float.parseFloat(element.getAttribute("strategyCardProbability") == null
				|| element.getAttribute("strategyCardProbability").length() == 0 ? "0"
						: element.getAttribute("strategyCardProbability")); // 探索结算时策略卡出现的概率系数
		String medicineCostString = element.getAttribute("medicineCost"); // 局间医疗仓消耗
		if (medicineCostString != null && medicineCostString.length() > 0) {
			String[] medicineCostStrings = medicineCostString.split("\\|");
			medicineCost = new ArrayList<Integer>(medicineCostStrings.length);
			for (int i = 0; i < medicineCostStrings.length; i++) {
				Integer temp = Integer.parseInt(medicineCostStrings[i]);
				medicineCost.add(temp);
			}
		} else {
			medicineCost = new ArrayList<Integer>();
		}
		favorLimit = Integer.parseInt(
				element.getAttribute("favorLimit") == null || element.getAttribute("favorLimit").length() == 0 ? "0"
						: element.getAttribute("favorLimit")); // 好感度最大值
		giftCardsCostLimit = Integer.parseInt(element.getAttribute("giftCardsCostLimit") == null
				|| element.getAttribute("giftCardsCostLimit").length() == 0 ? "0"
						: element.getAttribute("giftCardsCostLimit")); // 局间礼物卡的COST上限
		String standardEquipmentCostString = element.getAttribute("standardEquipmentCost"); // 制式装备额外消耗
		if (standardEquipmentCostString != null && standardEquipmentCostString.length() > 0) {
			String[] standardEquipmentCostStrings = standardEquipmentCostString.split("\\|");
			standardEquipmentCost = new ArrayList<Integer>(standardEquipmentCostStrings.length);
			for (int i = 0; i < standardEquipmentCostStrings.length; i++) {
				Integer temp = Integer.parseInt(standardEquipmentCostStrings[i]);
				standardEquipmentCost.add(temp);
			}
		} else {
			standardEquipmentCost = new ArrayList<Integer>();
		}
		String exploreEquipmentCostString = element.getAttribute("exploreEquipmentCost"); // 探索装备额外消耗
		if (exploreEquipmentCostString != null && exploreEquipmentCostString.length() > 0) {
			String[] exploreEquipmentCostStrings = exploreEquipmentCostString.split("\\|");
			exploreEquipmentCost = new ArrayList<Integer>(exploreEquipmentCostStrings.length);
			for (int i = 0; i < exploreEquipmentCostStrings.length; i++) {
				Integer temp = Integer.parseInt(exploreEquipmentCostStrings[i]);
				exploreEquipmentCost.add(temp);
			}
		} else {
			exploreEquipmentCost = new ArrayList<Integer>();
		}
		String bossAppearCountDownString = element.getAttribute("bossAppearCountDown"); // 第二阶段区域boss的出现倒计时参数
		if (bossAppearCountDownString != null && bossAppearCountDownString.length() > 0) {
			String[] bossAppearCountDownStrings = bossAppearCountDownString.split("\\|");
			bossAppearCountDown = new ArrayList<Integer>(bossAppearCountDownStrings.length);
			for (int i = 0; i < bossAppearCountDownStrings.length; i++) {
				Integer temp = Integer.parseInt(bossAppearCountDownStrings[i]);
				bossAppearCountDown.add(temp);
			}
		} else {
			bossAppearCountDown = new ArrayList<Integer>();
		}
		genSpritbodyProbability = Integer.parseInt(element.getAttribute("genSpritbodyProbability") == null
				|| element.getAttribute("genSpritbodyProbability").length() == 0 ? "0"
						: element.getAttribute("genSpritbodyProbability")); // 游荡者死后生成精神体的概率
		monsterRebornRound = Integer.parseInt(element.getAttribute("monsterRebornRound") == null
				|| element.getAttribute("monsterRebornRound").length() == 0 ? "0"
						: element.getAttribute("monsterRebornRound")); // 怪物重生回合
		stalkerProbability = Integer.parseInt(element.getAttribute("stalkerProbability") == null
				|| element.getAttribute("stalkerProbability").length() == 0 ? "0"
						: element.getAttribute("stalkerProbability")); // 追猎者出现的概率
		exceedRoundGenStalker = Integer.parseInt(element.getAttribute("exceedRoundGenStalker") == null
				|| element.getAttribute("exceedRoundGenStalker").length() == 0 ? "0"
						: element.getAttribute("exceedRoundGenStalker")); // 追猎者超回合必出
		switchInWorldGenWandererProbability = Integer
				.parseInt(element.getAttribute("switchInWorldGenWandererProbability") == null
						|| element.getAttribute("switchInWorldGenWandererProbability").length() == 0 ? "0"
								: element.getAttribute("switchInWorldGenWandererProbability")); // 游荡者出现概率
		genWandererRangeOutside = Integer.parseInt(element.getAttribute("genWandererRangeOutside") == null
				|| element.getAttribute("genWandererRangeOutside").length() == 0 ? "0"
						: element.getAttribute("genWandererRangeOutside")); // 游荡者出现在玩家多少范围外
		genWandererRangeInside = Integer.parseInt(element.getAttribute("genWandererRangeInside") == null
				|| element.getAttribute("genWandererRangeInside").length() == 0 ? "0"
						: element.getAttribute("genWandererRangeInside")); // 游荡者出现在玩家多少范围内
		mapWandererNumnberLimit = Integer.parseInt(element.getAttribute("mapWandererNumnberLimit") == null
				|| element.getAttribute("mapWandererNumnberLimit").length() == 0 ? "0"
						: element.getAttribute("mapWandererNumnberLimit")); // 单张地图游荡者数量上限
		mappingFieldCostCharging = Integer.parseInt(element.getAttribute("mappingFieldCostCharging") == null
				|| element.getAttribute("mappingFieldCostCharging").length() == 0 ? "0"
						: element.getAttribute("mappingFieldCostCharging")); // 进入映射域每回合消耗充能数量
		ChargingLimit = Integer.parseInt(
				element.getAttribute("ChargingLimit") == null || element.getAttribute("ChargingLimit").length() == 0
						? "0"
						: element.getAttribute("ChargingLimit")); // 表世界充能最大上限
		ChargingMin = Integer.parseInt(
				element.getAttribute("ChargingMin") == null || element.getAttribute("ChargingMin").length() == 0 ? "0"
						: element.getAttribute("ChargingMin")); // 进入映射域最小充能值
		HiggsGenerate = Integer.parseInt(
				element.getAttribute("HiggsGenerate") == null || element.getAttribute("HiggsGenerate").length() == 0
						? "0"
						: element.getAttribute("HiggsGenerate")); // 希格斯水晶单张地图生成数量
		HiggsTotal = Integer.parseInt(
				element.getAttribute("HiggsTotal") == null || element.getAttribute("HiggsTotal").length() == 0 ? "0"
						: element.getAttribute("HiggsTotal")); // 希格斯水晶共计需要数量
		String RoleUnlockSkillPromotionLevelString = element.getAttribute("RoleUnlockSkillPromotionLevel"); // 角色解锁技能的晋升等级
		if (RoleUnlockSkillPromotionLevelString != null && RoleUnlockSkillPromotionLevelString.length() > 0) {
			String[] RoleUnlockSkillPromotionLevelStrings = RoleUnlockSkillPromotionLevelString.split("\\|");
			RoleUnlockSkillPromotionLevel = new ArrayList<Integer>(RoleUnlockSkillPromotionLevelStrings.length);
			for (int i = 0; i < RoleUnlockSkillPromotionLevelStrings.length; i++) {
				Integer temp = Integer.parseInt(RoleUnlockSkillPromotionLevelStrings[i]);
				RoleUnlockSkillPromotionLevel.add(temp);
			}
		} else {
			RoleUnlockSkillPromotionLevel = new ArrayList<Integer>();
		}
		String EquipUnlockBuffPromotionLevelString = element.getAttribute("EquipUnlockBuffPromotionLevel"); // 固定装备解锁词缀的晋升等级
		if (EquipUnlockBuffPromotionLevelString != null && EquipUnlockBuffPromotionLevelString.length() > 0) {
			String[] EquipUnlockBuffPromotionLevelStrings = EquipUnlockBuffPromotionLevelString.split("\\|");
			EquipUnlockBuffPromotionLevel = new ArrayList<Integer>(EquipUnlockBuffPromotionLevelStrings.length);
			for (int i = 0; i < EquipUnlockBuffPromotionLevelStrings.length; i++) {
				Integer temp = Integer.parseInt(EquipUnlockBuffPromotionLevelStrings[i]);
				EquipUnlockBuffPromotionLevel.add(temp);
			}
		} else {
			EquipUnlockBuffPromotionLevel = new ArrayList<Integer>();
		}
		TraumaBuffId = Integer.parseInt(
				element.getAttribute("TraumaBuffId") == null || element.getAttribute("TraumaBuffId").length() == 0 ? "0"
						: element.getAttribute("TraumaBuffId")); // 添加创伤效果
		String AddBreakDownString = element.getAttribute("AddBreakDown"); // 添加崩溃效果
		if (AddBreakDownString != null && AddBreakDownString.length() > 0) {
			String[] AddBreakDownStrings = AddBreakDownString.split("\\|");
			AddBreakDown = new ArrayList<Integer>(AddBreakDownStrings.length);
			for (int i = 0; i < AddBreakDownStrings.length; i++) {
				Integer temp = Integer.parseInt(AddBreakDownStrings[i]);
				AddBreakDown.add(temp);
			}
		} else {
			AddBreakDown = new ArrayList<Integer>();
		}
		String battleSanChangeBuffListString = element.getAttribute("battleSanChangeBuffList"); // 战斗增减san值buff列表
		if (battleSanChangeBuffListString != null && battleSanChangeBuffListString.length() > 0) {
			String[] battleSanChangeBuffListStrings = battleSanChangeBuffListString.split("\\|");
			battleSanChangeBuffList = new ArrayList<Integer>(battleSanChangeBuffListStrings.length);
			for (int i = 0; i < battleSanChangeBuffListStrings.length; i++) {
				Integer temp = Integer.parseInt(battleSanChangeBuffListStrings[i]);
				battleSanChangeBuffList.add(temp);
			}
		} else {
			battleSanChangeBuffList = new ArrayList<Integer>();
		}
	}
	@Override
	public void load() {

		try {
			ClassLoader classLoader = Thread.currentThread().getClass().getClassLoader();
			if (classLoader == null) {
				classLoader = OldGlobalConst.class.getClassLoader();
			}
			Document document = XmlUtils.load(classLoader.getResourceAsStream("xml/" + xmlFileName + ".xml"));
			Element[] list = XmlUtils.getChildrenByName(document.getDocumentElement(), xmlFileName);
			for (Element e : list) {
				init(e);
			}
		} catch (Exception e) {
			e.printStackTrace(); 
		}

	}

	@Override
	public String name() {
		return xmlFileName;
	}
}
