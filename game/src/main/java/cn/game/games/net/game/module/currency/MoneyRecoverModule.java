package cn.game.games.net.game.module.currency;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;

import cn.game.games.core.BasePlayerModule;
import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.PlayerEvent;
import cn.game.games.net.game.helper.PlayerHelper;
import cn.game.protocol.generated.config.AssetRestoreConfig;
import cn.game.protocol.generated.enume.Asset;
import cn.game.protocol.generated.enume.WelfareTypeEnum;
import cn.game.protocol.generated.manager.AssetRestoreManager;
import cn.game.protocol.manual.OpType;
import cn.game.protocol.protobuf.PlayerMsg.PlayerAllInfo.Builder;

/**    
 * 一些按照时间恢复的资源，例如各种体力,注意创建角色时候，初始体力至少需要配置0，才会启动恢复任务。 
 * 2024年3月19日 上午11:23:20
 * @author SYQ
 */
public class MoneyRecoverModule extends BasePlayerModule {
	private static EventTypeEnum[] events = new EventTypeEnum[] { EventTypeEnum.PLAYER_CREATE, EventTypeEnum.LoginFinish,
			EventTypeEnum.ResourceRemove, EventTypeEnum.ResourceAdd, EventTypeEnum.GetItem, EventTypeEnum.CostItem };

	@JsonIgnore
	private Map<Integer, Long> timerTask = new HashMap<Integer, Long>();

	/** 资源恢复时间 */
	private Map<Integer, Long> idUpdateTimeMap = new HashMap<Integer, Long>();

	@Override
	public void handleEvent(PlayerEvent event) {
		switch (event.getType()) {
		case PLAYER_CREATE: {
			break;
		}
		case LoginFinish: {
			Set<Integer> idsSet = idUpdateTimeMap.keySet();
			Iterator<Integer> iterator = idsSet.iterator();
			while (iterator.hasNext()) {
				Integer id = (Integer) iterator.next();
				if (!player.getCurrencyModule().has(id)) {
					iterator.remove();
				}
			}

			for (Integer id : idsSet) {
				long updateTime = idUpdateTimeMap.get(id);
				AssetRestoreConfig recoveryConfig = AssetRestoreManager.instance().get(id);
				int interval = recoveryConfig.interval * 60 * 1000;
				if (isRecoverMax(id)) {
					continue;
				}
				int max = getRecoverMax(id);
				long recoveryTimes = (System.currentTimeMillis() - updateTime) / interval;
				if (recoveryTimes > 0) {
					long newValue = Math.min(player.getCurrencyModule().getCount(id) + recoveryTimes, max);
					player.getCurrencyModule().setCount(id, newValue);
					idUpdateTimeMap.put(id, updateTime + recoveryTimes * interval);
				} else {
					startRecoveryTask(id, (int) (interval - (System.currentTimeMillis() - updateTime)));
				}
			}
			startAllRecoveryTask();
			break;
		}
		case ResourceAdd: {
			// 活动开始，增加新资源，增加恢复任务
			// 值满，取消定时任务， 如果消耗了值，变成不满了，则需要重新启动恢复任务。或者先不处理，做个上限的判断
			int id = event.getIntParameter(0);
			startRecoveryTask(id, 0);
			break;
		}
		case ResourceRemove: {
			int id = event.getIntParameter(0);
			Long timer = timerTask.remove(id);
			if (timer != null) {
				player.cancelTimer(timer);
			}
			break;
		}
		case GetItem: {
			int id = event.getIntParameter(0);
			AssetRestoreConfig assetRestoreConfig = AssetRestoreManager.instance().getNullable(id);
			if (assetRestoreConfig == null) {
				return;
			}
			// 满了停止。
			if (isRecoverMax(id)) {
				stopRecoveryTask(id);
			}
			break;
		}
		case CostItem: {
			int id = event.getIntParameter(0);
			AssetRestoreConfig assetRestoreConfig = AssetRestoreManager.instance().getNullable(id);
			if (assetRestoreConfig == null) {
				return;
			}
			// 不满启动。
			if (!isRecoverMax(id)) {
				if (!timerTask.containsKey(id)) {
					startRecoveryTask(id, 0);
				}
			}
			break;
		}
		default:
			throw new IllegalArgumentException("Unexpected value: " + event.getType());
		}
	}

	private void startRecoveryTask(int id, int nextUpdataTime) {
		AssetRestoreConfig recoveryConfig = AssetRestoreManager.instance().getNullable(id);
		if (recoveryConfig != null) {
			if (nextUpdataTime == 0) {
				idUpdateTimeMap.put(id, System.currentTimeMillis());
				long timer = player.setPeriodicTask(recoveryConfig.interval * 60 * 1000, r -> {
					timeToRecovery(id);
				});
				timerTask.put(id, timer);

			} else {
				long timer1 = player.setTimerTask(nextUpdataTime, l -> {
					timeToRecovery(id);
					long timer = player.setPeriodicTask(recoveryConfig.interval * 60 * 1000, r -> {
						timeToRecovery(id);
					});
					timerTask.put(id, timer);
				});
				timerTask.put(id, timer1);
			}

		}
	}

	private void timeToRecovery(int id) {
		// 是否到达上限
		if (isRecoverMax(id)) {
			return;
		}
		idUpdateTimeMap.put(id, System.currentTimeMillis());
		PlayerHelper.addResources(player, id, 1, OpType.TimerRecovery);
	}

	private void stopRecoveryTask(int id) {
		AssetRestoreConfig recoveryConfig = AssetRestoreManager.instance().getNullable(id);
		if (recoveryConfig != null) {
			Long timer = timerTask.remove(id);
			if (timer != null) {
				player.cancelTimer(timer);
			}
		}
	}
//	Asset.playerEnergy.ID

	/**
	 * 某值恢复满还需要多久
	 * @return 时间戳 毫秒
	 */
	public long getOfflineRecoveryTimer(Asset asset) {
		long curEnergy = player.getCurrencyModule().get(asset);
		AssetRestoreConfig recoveryConfig = AssetRestoreManager.instance().getNullable(asset.ID);
		if (recoveryConfig == null){
			return 0L;
		}
		int recoverMax = getRecoverMax(asset.ID);
		if (curEnergy >= recoverMax)
			return 0L;
		return (recoverMax - curEnergy) * recoveryConfig.interval * 60 * 1000L;
	}

	private void startAllRecoveryTask() {
		Collection<AssetRestoreConfig> list = AssetRestoreManager.instance().list();
		for (AssetRestoreConfig moneyRecoveryConfig : list) {
			if (player.getCurrencyModule().has(moneyRecoveryConfig.ID)) {
				if (idUpdateTimeMap.containsKey(moneyRecoveryConfig.ID)) {
					continue;
				}
				if (timerTask.containsKey(moneyRecoveryConfig.ID)) {
					continue;
				}
				startRecoveryTask(moneyRecoveryConfig.ID, 0);
			}
		}
	}

	/** 
	 * 获取自然恢复的资源最大值
	 * @param id
	 * @return
	 */
	public int getRecoverMax(int id) {
		AssetRestoreConfig assetRestoreConfig = AssetRestoreManager.instance().get(id);
		int max = assetRestoreConfig.maxShow;
//		if (assetRestoreConfig.maxType == 1) {
//			// 此处需要加月卡体力
//			MonthCardModule module = player.getModule(MonthCardModule.class);
//			if (module.hasMonthCard()) {
//				max += assetRestoreConfig.maxValue;
//			}
//		}
		if (id == Asset.playerEnergy.ID) {
			max += player.getWelfareValue(WelfareTypeEnum.PlayerEnergy);
		}
		max *= assetRestoreConfig.maxMultiple;
		return max;
	}

	/** 
	 * 是否恢复到最大了
	 * @param id
	 * @return
	 */
	private boolean isRecoverMax(int id) {

		return player.getCurrencyModule().getCount(id) >= getRecoverMax(id);
	}

	@Override
	public void buildPlayerAllInfo(Builder builder) {
		builder.putAllAssetRecover(idUpdateTimeMap);
	}

	public Map<Integer, Long> getIdUpdateTimeMap() {
		return idUpdateTimeMap;
	}
	@Override
	protected int getInitOrder() {
		return INIT_PRIORITY_LOW;
	}

	@Override
	public EventTypeEnum[] getEventTypes() {
		return events;
	}
}
