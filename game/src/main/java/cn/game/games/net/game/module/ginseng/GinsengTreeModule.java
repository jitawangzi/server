package cn.game.games.net.game.module.ginseng;

import java.util.ArrayList;
import java.util.List;

import cn.game.games.core.BasePlayerModule;
import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.GameEvent;
import cn.game.games.net.game.helper.PlayerHelper;
import cn.game.games.net.game.module.award.Goods;
import cn.game.protocol.generated.config.GlobalConst;
import cn.game.protocol.generated.config.RSGTreeLvConfig;
import cn.game.protocol.generated.enume.Asset;
import cn.game.protocol.generated.enume.InitialUI;
import cn.game.protocol.generated.manager.RSGTreeLvManager;
import cn.game.protocol.protobuf.GinsengTreeMsg;
import cn.game.protocol.protobuf.GinsengTreeMsg.GinsengTreeInfo;
import cn.game.protocol.protobuf.PlayerMsg.PlayerAllInfo.Builder;
import cn.game.util.DateUtil;
import cn.game.util.IntMapWrapper;
import cn.game.util.Rnd;

public class GinsengTreeModule extends BasePlayerModule {
	private static EventTypeEnum[] events = new EventTypeEnum[] { EventTypeEnum.NewDay, EventTypeEnum.FuncOpen };

	/** 果实数据： key: 实际有果实的位置，从0开始;  value: 实际成熟时间 */
	private IntMapWrapper fruitMap = new IntMapWrapper();

	/** 有几条虫子 */
	private int bugs;

	/** 每天杀虫剂购买/使用 次数 */
	private int insecticidesTimes;

	/** 杀虫剂结束时间 */
	private int insecticidesEndTime;

	/** 每天浇水次数 */
	private int waterTimes;

	/** 开始挂机时间 */
	private int hangUpStartTime;

	/** 上次挂机奖励计算时间 */
	private int hangUpRewardCalcTime;

	/** 挂机的随机奖励部分，key 道具或资源id，value 数量 */
	private IntMapWrapper hangUpRandomRewardMap = new IntMapWrapper();
	private List<Integer> heroIdList = new ArrayList<>();

	@Override
	public EventTypeEnum[] getEventTypes() {
		return events;
	}

	@Override
	public void handleEvent(GameEvent event) {
		switch (event.getType()) {

		case NewDay: {
			waterTimes = 0;
			insecticidesTimes = 0;
			break;
		}
		case FuncOpen: {
			InitialUI func = event.getParameter(0);
			if (func == InitialUI.HangingUpp) {
				player.getPlayerModule().initLevel(Asset.RSGTreeExp);
				hangUpStartTime = DateUtil.currentTimeSeconds();
				hangUpRewardCalcTime = hangUpStartTime;
			}
			break;
		}
		}
	}

	@Override
	public void onLogin() {
		int level = player.getLevel(Asset.RSGTreeExp);
		if (level > 0) {
			startFruitTask();
			startBugTask();
		}
	}

	private void startFruitTask() {

		int level = player.getLevel(Asset.RSGTreeExp);
		RSGTreeLvConfig rsgTreeLvConfig = RSGTreeLvManager.instance().get(level);
		player.setPeriodicTask(rsgTreeLvConfig.RefreshTime * 1000, r -> {
			if (fruitMap.size() >= rsgTreeLvConfig.Num) {
				return;
			}
			for (int i = 0; i < rsgTreeLvConfig.Num; i++) {
				if (!fruitMap.hasValue(i)) {
					// 如果没有果实，则添加一个果实
					if (Rnd.hit(rsgTreeLvConfig.RefreshWeight)) {
						fruitMap.add(i, DateUtil.currentTimeSeconds() + rsgTreeLvConfig.MellowTime);
					}
				}
			}
		});
	}

	private void startBugTask() {
//		int level = player.getLevel(Asset.RSGTreeExp);
//		RSGTreeLvConfig rsgTreeLvConfig = RSGTreeLvManager.instance().get(level);
		player.setPeriodicTask(GlobalConst.RSGTreeRefreshInterval * 1000, r -> {
			if (bugs >= GlobalConst.RSGTreeBugMax) {
				return;
			}
			if (insecticidesEndTime > DateUtil.currentTimeSeconds()) {
				return;
			}
			if (Rnd.hit(GlobalConst.RSGTreeRefreshWeight)) {
				bugs++;
			}
		});
	}

	public GinsengTreeInfo buildGinsengTreeInfo() {
		cn.game.protocol.protobuf.GinsengTreeMsg.GinsengTreeInfo.Builder builder = GinsengTreeMsg.GinsengTreeInfo.newBuilder();
		builder.setLevel(player.getLevel(Asset.RSGTreeExp));
		builder.setExp((int) player.getCurrencyModule().get(Asset.RSGTreeExp));
		builder.setBugs(bugs);
		builder.setHangUpSeconds(DateUtil.currentTimeSeconds() - hangUpStartTime);
		builder.setInsecticidesEndRemainingSeconds(
				insecticidesEndTime - DateUtil.currentTimeSeconds() > 0 ? insecticidesEndTime - DateUtil.currentTimeSeconds() : 0);
		builder.setInsecticidesTimes(insecticidesTimes);
		builder.setWaterTimes(waterTimes);
		fruitMap.getMap().forEach((k, v) -> {
			int time = v - DateUtil.currentTimeSeconds();
			builder.putFruitMap(k, time > 0 ? time : 0);
		});
		// 计算挂机奖励
		calcHangUpReward();

		builder.putAllHangUpRandomRewardMap(hangUpRandomRewardMap.getMap());
		builder.addAllHeroIdList(heroIdList);

		return builder.build();
	}
	// 计算挂机奖励
	public int calcHangUpReward() {
		int minutes =  (DateUtil.currentTimeSeconds() - hangUpRewardCalcTime)/60 ; 
		if (minutes <= 0) {
			return 0;
		}
		int level = player.getLevel(Asset.RSGTreeExp);
		RSGTreeLvConfig rsgTreeLvConfig = RSGTreeLvManager.instance().get(level);
		// 每次挂机奖励
		int randomReward = rsgTreeLvConfig.RandomID;

		for (int i = 0; i < minutes; i++) {
			List<Goods> randomReward2 = PlayerHelper.randomReward(randomReward);
			for (Goods goods : randomReward2) {
				hangUpRandomRewardMap.add(goods.getId(), goods.getCount());
			}
		}
		hangUpRewardCalcTime += minutes * 60;
		return minutes;
	}
	public void fertilization() {
		fruitMap.reduceAllValues(GlobalConst.RSGTreeFertilizerAcce);
	}

	@Override
	public void buildPlayerAllInfo(Builder builder) {

	}

	public IntMapWrapper getFruitMap() {
		return fruitMap;
	}

	public void setFruitMap(IntMapWrapper fruitMap) {
		this.fruitMap = fruitMap;
	}

	public int getBugs() {
		return bugs;
	}

	public void setBugs(int bugs) {
		this.bugs = bugs;
	}

	public int getInsecticidesTimes() {
		return insecticidesTimes;
	}

	public void setInsecticidesTimes(int insecticidesTimes) {
		this.insecticidesTimes = insecticidesTimes;
	}

	public int getInsecticidesEndTime() {
		return insecticidesEndTime;
	}

	public void setInsecticidesEndTime(int insecticidesEndTime) {
		this.insecticidesEndTime = insecticidesEndTime;
	}

	public int getWaterTimes() {
		return waterTimes;
	}

	public void setWaterTimes(int waterTimes) {
		this.waterTimes = waterTimes;
	}

	public int getHangUpStartTime() {
		return hangUpStartTime;
	}

	public void setHangUpStartTime(int hangUpStartTime) {
		this.hangUpStartTime = hangUpStartTime;
	}

	public IntMapWrapper getHangUpRandomRewardMap() {
		return hangUpRandomRewardMap;
	}

	public void setHangUpRandomRewardMap(IntMapWrapper hangUpRandomRewardMap) {
		this.hangUpRandomRewardMap = hangUpRandomRewardMap;
	}

	public int getHangUpRewardCalcTime() {
		return hangUpRewardCalcTime;
	}

	public void setHangUpRewardCalcTime(int hangUpRewardCalcTime) {
		this.hangUpRewardCalcTime = hangUpRewardCalcTime;
	}

	public List<Integer> getHeroIdList() {
		return heroIdList;
	}

}
