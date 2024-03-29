package cn.game.games.net.game.module.currency;

import java.util.ListIterator;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.game.games.core.GoodsModule;
import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.GameEvent;
import cn.game.games.net.game.helper.ItemHelper;
import cn.game.protocol.generated.config.ExpConfig;
import cn.game.protocol.generated.enume.GoodsTypeEnum;
import cn.game.protocol.generated.enume.Money;
import cn.game.protocol.generated.manager.UserUpgradeManager;
import cn.game.protocol.manual.ResourceConsumeEnum;
import cn.game.protocol.protobuf.BaseMsg.CurrencyInfo;
import cn.game.protocol.protobuf.BaseMsg.ResourceInfo;
import cn.game.protocol.protobuf.PlayerMsg.PlayerAllInfo.Builder;
import cn.game.protocol.protobuf.RewardMsg.RewardInfo;
import cn.game.util.MapWrapper;

public class CurrencyModule extends GoodsModule<Currency, Currency> {
	private static final Logger levellog = LoggerFactory.getLogger("levelLog");

	@Override
	public EventTypeEnum[] getEventTypes() {
		return null;
	}

	@Override
	public void handleEvent(GameEvent event) {
	}

	@Override
	public long getCount(int configId) {
		return player.getCurrencyMap().getValue(configId);
	}

	@Override
	public Currency add(int configId, int count) {

		if (count < 0) {
			return new Currency(configId, 0);
		}
		ItemHelper.checkConfig(configId);
		Money money = Money.get(configId);
		if (money.Type == 2) {
			addExp(configId, count);
		} else {
			player.getCurrencyMap().add(configId, count);
		}
		return new Currency(configId, count);
	}

	@Override
	public boolean del(int configId, int count, ResourceConsumeEnum... args) {

		if (count <= 0) {
			return true;
		}
		return player.getCurrencyMap().del(configId, count);
	}

	@Override
	public Currency get(int configId) {
		return new Currency(configId, getCount(configId));
	}

	@Override
	public GoodsTypeEnum getGoodsTypeEnum() {
		return GoodsTypeEnum.Resource;
	}

	@Override
	public Class<?>[] defaultDbMapperClass() {
		return null;
	}

	@Override
	protected void initFromDb(ListIterator<?> iterator) {

	}

	@Override
	public Currency newInstance() {
		return new Currency();
	}

	@Override
	public RewardInfo toRewardInfo(Currency reward) {
		return RewardInfo.newBuilder()
				.setResource(ResourceInfo.newBuilder().setId(reward.getConfigId()).setCount(reward.getCount())).build();

	}

	@Override
	public void buildPlayerAllInfo(Builder builder) {
		Map<Integer, Long> currencyMap = player.getCurrencyMap().getMap();
		currencyMap.forEach((k, v) -> {
			builder.addCurrencys(CurrencyInfo.newBuilder().setId(k).setCount(9223372036854775800L));
		});
	}

	@Override
	public void initAddCache(Currency item) {
		// TODO Auto-generated method stub

	}

	@Override
	public void removeCache(Currency item) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean del(long uid, ResourceConsumeEnum... args) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Currency get(long uid) {
		throw new UnsupportedOperationException();
	}

	public void addExp(int id, int count) {
		long curExp = player.getCurrency(id) + count;

		MapWrapper levelsMap = player.getLevelMap();

		ExpConfig expConfig = getExpConfig(id, (int) levelsMap.getValue(id));
		ExpConfig nextExpConfig = getExpConfig(id, (int) (levelsMap.getValue(id) + 1));
		while (curExp >= expConfig.experience && nextExpConfig != null) {
			curExp -= expConfig.experience;
			levelsMap.add(id, 1);

			player.handleEvent(new GameEvent(EventTypeEnum.LevelUp, player, id, levelsMap.getValue(id)));

			expConfig = getExpConfig(id, (int) levelsMap.getValue(id));
			nextExpConfig = getExpConfig(id, (int) (levelsMap.getValue(id) + 1));

			levellog.info("opType[levelUp]playerId[{}]exp[{}]newLevel[{}]", player.getData().getPlayerId(), id,
					levelsMap.getValue(id));
		}
		if (curExp > expConfig.experience) {
			curExp = expConfig.experience;
		}
		player.setCurrency(id, curExp);
	}

	public ExpConfig getExpConfig(int id, int level) {
		if (id == Money.playerExp.ID) {
			return UserUpgradeManager.instance().getNullable(level);
		}
		throw new IllegalArgumentException("没有实现的经验id： " + id);
	}

	@Override
	public void addCacheStackable(Currency item) {
		
	}

	@Override
	public void addCacheNoStackable(Currency item) {
		
	}
}
