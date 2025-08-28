package cn.game.games.net.game.module.ginseng;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import cn.game.games.core.BasePlayerModule;
import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.PlayerEvent;
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
	
	/** 选择手操卡id */
	private List<Integer> handCardList = new ArrayList<>();


	/** 下一次产生果实的时间 */
	private int nextFruitTime;

	@JsonIgnore
	private long fruitTimer;

	@Override
	public EventTypeEnum[] getEventTypes() {
		return events;
	}

	@Override
	public void handleEvent(PlayerEvent event) {
		switch (event.getType()) {

		case NewDay: {
			waterTimes = 0;
			insecticidesTimes = 0;
			break;
		}
		case FuncOpen: {
			InitialUI func = event.getParameter(0);
			if (func == InitialUI.RSGTree) {
				player.getPlayerModule().initLevel(Asset.RSGTreeExp);
				hangUpStartTime = DateUtil.currentTimeSeconds();
				hangUpRewardCalcTime = hangUpStartTime;

				startFruitTask();
				startBugTask();

			}
			break;
		}
		}
	}

	@Override
	public void onLogin() {
		if (player.isFuncOpen(InitialUI.RSGTree)){
			startFruitTask();
			startBugTask();
		}
	}

	public void startFruitTask() {

		int level = player.getLevel(Asset.RSGTreeExp);
		if (level == 0) {
			return;
		}
		// 根据当前果实数，计算刷新果实的时间
		int remaningSeconds = calcRemaningSeconds();
		// 剩余刷新秒数
		RSGTreeLvConfig rsgTreeLvConfig = RSGTreeLvManager.instance().get(level);
		int nextFruitTime = DateUtil.currentTimeSeconds() + remaningSeconds;
		boolean changeTimer = false;
		if (this.nextFruitTime > 0) {
			if (nextFruitTime < this.nextFruitTime) {
				if (fruitTimer > 0) {
					player.cancelTimer(fruitTimer);
				}
				this.nextFruitTime = nextFruitTime;
				changeTimer = true;
			}
		} else {
			this.nextFruitTime = nextFruitTime;
			changeTimer = true;
		}
		if (changeTimer || fruitTimer == 0) {
			int delay = this.nextFruitTime - DateUtil.currentTimeSeconds();
			if (delay <= 0) {
				newFruit(rsgTreeLvConfig,this.nextFruitTime);
				this.nextFruitTime += calcRemaningSeconds();
				startFruitTask();
			}else {
				fruitTimer = player.setTimerTask(delay * 1000L, r -> {
					newFruit(rsgTreeLvConfig,DateUtil.currentTimeSeconds());
					this.fruitTimer = 0 ;
					this.nextFruitTime = 0;
					startFruitTask();
				});
			}
		}
	}

	private int calcRemaningSeconds(){
		int fruitCount = fruitMap.size();
		int[] apearTime	= GlobalConst.RSGTreeFruitApearTime[fruitCount];
		return Rnd.randomInRange(apearTime);
	}
	private void newFruit(RSGTreeLvConfig rsgTreeLvConfig,int createTime) {
		if (fruitMap.size() >= rsgTreeLvConfig.Num) {
			return;
		}
		for (int i = 0; i < rsgTreeLvConfig.Num; i++) {
			if (!fruitMap.hasValue(i)) {
				// 如果没有果实，则添加一个果实
//				if (Rnd.hit(rsgTreeLvConfig.RefreshWeight)) {
				fruitMap.add(i, createTime + rsgTreeLvConfig.MellowTime);
				break;
//				}
			}
		}
	}

	private void startBugTask() {
//		int level = player.getLevel(Asset.RSGTreeExp);
//		RSGTreeLvConfig rsgTreeLvConfig = RSGTreeLvManager.instance().get(level);
		int level = player.getLevel(Asset.RSGTreeExp);
		if (level == 0) {
			return;
		}

		player.setPeriodicTask(GlobalConst.RSGTreeRefreshInterval * 1000, r -> {
			if (bugs >= GlobalConst.RSGTreeBugMax) {
				return;
			}
			if ( getInsecticidesEndTime()  > DateUtil.currentTimeSeconds()) {
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
		int hangUpSeconds =  DateUtil.currentTimeSeconds() - hangUpStartTime;
		if (hangUpSeconds>= GlobalConst.RSGTreeAwardMaxTime) {
			hangUpSeconds = GlobalConst.RSGTreeAwardMaxTime;
		}
		builder.setHangUpSeconds(hangUpSeconds);
		builder.setInsecticidesEndRemainingSeconds(
				 getInsecticidesEndTime()  - DateUtil.currentTimeSeconds() > 0 ?  getInsecticidesEndTime()  - DateUtil.currentTimeSeconds() : 0);
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
		// 体力以前存到BattleModule里了
		builder.setEnergyFruitCount(player.getBattleModule().getStoreStaminas().size()) ; 

		return builder.build();
	}
	// 计算挂机奖励
	public int calcHangUpReward() {
		int hangUpSeconds =  DateUtil.currentTimeSeconds() - hangUpRewardCalcTime;
		if (hangUpSeconds>= GlobalConst.RSGTreeAwardMaxTime) {
			hangUpSeconds = GlobalConst.RSGTreeAwardMaxTime;
		}
		int minutes =  hangUpSeconds/60 ;
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
		builder.addAllHandCardIds(handCardList); 
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
		if (insecticidesEndTime < DateUtil.currentTimeSeconds()) {
			insecticidesEndTime = 0;
		}
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

	public List<Integer> getHandCardList() {
		return handCardList;
	}

}
