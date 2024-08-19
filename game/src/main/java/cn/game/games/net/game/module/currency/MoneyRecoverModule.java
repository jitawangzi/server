package cn.game.games.net.game.module.currency;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonIgnore;

import cn.game.games.cache.entity.PlayerIds;
import cn.game.games.core.BasePlayerModule;
import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.GameEvent;
import cn.game.games.net.game.helper.PlayerHelper;
import cn.game.games.net.game.module.player.IdConstant;
import cn.game.protocol.generated.config.AssetRestoreConfig;
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
	private static EventTypeEnum[] events = new EventTypeEnum[] { EventTypeEnum.PLAYER_CREATE,
			EventTypeEnum.LoginFinish,
			EventTypeEnum.ResourceRemove, EventTypeEnum.ResourceAdd };

	@JsonIgnore
	private Map<Integer, Long> timerTask = new HashMap<Integer, Long>();
	
	@Override
	public void handleEvent(GameEvent event) {
		switch (event.getType()) {
		case PLAYER_CREATE: {
			break;
		}
		case LoginFinish: {

			Set<Integer> idsSet = player.getPlayerModule().getIdsSet(IdConstant.MONEY_RECOVERY);
			idsSet.stream().filter(id -> !player.getCurrencyModule().has(id)).collect(Collectors.toList())
					.forEach(id -> player.getPlayerModule().removeId(IdConstant.MONEY_RECOVERY, id));

			for (Integer id : idsSet) {
				PlayerIds ids = player.getPlayerModule().getIds(IdConstant.MONEY_RECOVERY, id);
				AssetRestoreConfig recoveryConfig = AssetRestoreManager.instance().get(id);
				int interval = recoveryConfig.interval * 60 * 1000;
				if (isRecoverMax(id)) {
					continue;
				}
				int max = getRecoverMax(id);
				long recoveryTimes = (System.currentTimeMillis() - ids.getUpdateTime()) / interval;
				if (recoveryTimes > 0) {
					long newValue = Math.min(player.getCurrencyModule().getCount(id) + recoveryTimes, max);
					player.getCurrencyModule().setCount(id, newValue);
					ids.setUpdateTime(ids.getUpdateTime() + recoveryTimes * interval);
					ids.update();
				}
			}
			startAllRecoveryTask();
			break;
		}
		case ResourceAdd: {
			// 活动开始，增加新资源，增加恢复任务
			// 值满，取消定时任务， 如果消耗了值，变成不满了，则需要重新启动恢复任务。或者先不处理，做个上限的判断
			int id = event.getIntParameter(0);
			startRecoveryTask(id);
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
		default:
			throw new IllegalArgumentException("Unexpected value: " + event.getType());
		}
	}

	private void startRecoveryTask(int id) {
		AssetRestoreConfig recoveryConfig = AssetRestoreManager.instance().getNullable(id);
		if (recoveryConfig != null) {
			long timer = player.setPeriodicTask(recoveryConfig.interval * 60 * 1000, r -> {
				// 是否到达上限
				if (isRecoverMax(id)) {
					return;
				}
				player.getPlayerModule().updateTime(IdConstant.MONEY_RECOVERY, id);
				PlayerHelper.addResources(player, id, 1, OpType.TimerRecovery);
			});
			timerTask.put(id, timer);
		}
	}

	private void startAllRecoveryTask() {
		Collection<AssetRestoreConfig> list = AssetRestoreManager.instance().list();
		for (AssetRestoreConfig moneyRecoveryConfig : list) {
			if (player.getCurrencyModule().has(moneyRecoveryConfig.ID)) {
				startRecoveryTask(moneyRecoveryConfig.ID);
			}
		}
	}

	/** 
	 * 获取自然恢复的资源最大值
	 * @param id
	 * @return
	 */
	private int getRecoverMax(int id) {
		AssetRestoreConfig assetRestoreConfig = AssetRestoreManager.instance().get(id);
		int max = assetRestoreConfig.maxShow;
//		if (assetRestoreConfig.maxType == 1) {
//			// 此处需要加月卡体力
//			MonthCardModule module = player.getModule(MonthCardModule.class);
//			if (module.hasMonthCard()) {
//				max += assetRestoreConfig.maxValue;
//			}
//		}
		max += player.getWelfareValue(WelfareTypeEnum.PlayerEnergy);
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
	public Class<?>[] defaultDbMapperClass() {
		return null;
	}

	@Override
	public void initFromDbAfter() {

	};
	@Override
	public void buildPlayerAllInfo(Builder builder) {

		Collection<PlayerIds> ids = player.getPlayerModule().getIds(IdConstant.MONEY_RECOVERY);
		for (PlayerIds playerIds : ids) {
			builder.putAssetRecover(playerIds.getConfigId(), (int) (playerIds.getUpdateTime() / 1000));
		}
	}

	@Override
	protected int getInitOrder() {
		return Integer.MAX_VALUE;
	}

	@Override
	public EventTypeEnum[] getEventTypes() {
		return events;
	}
}
