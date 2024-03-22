package cn.game.games.net.game.module.item;

import java.util.List;
import java.util.ListIterator;

import cn.game.games.cache.entity.Equip;
import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.GameEvent;
import cn.game.games.net.data.mapper.EquipMapper;
import cn.game.protocol.generated.enume.GoodsTypeEnum;
import cn.game.protocol.protobuf.BaseMsg.EquipInfo;
import cn.game.protocol.protobuf.PlayerMsg.PlayerAllInfo.Builder;
import cn.game.protocol.protobuf.RewardMsg.RewardInfo;

/**    
 * 这里通常处理不能重叠的那些东西
 * @date 2024年2月19日 上午10:55:53
 * @author SYQ
 */
public class EquipModule extends AbstractItemNoStackModule<Equip> {
	private static EventTypeEnum[] events = new EventTypeEnum[] { EventTypeEnum.PLAYER_CREATE, EventTypeEnum.NewDay };


	@Override
	public EventTypeEnum[] getEventTypes() {
		return events;
	}

	@Override
	public void handleEvent(GameEvent event) {
		switch (event.getType()) {
		}
	}

	@Override
	public Class<?>[] defaultDbMapperClass() {
		return new Class<?>[] { EquipMapper.class };
	}

	@Override
	protected void initFromDb(ListIterator<?> iterator) {
		List<Equip> list = (List<Equip>) iterator.next();
		for (Equip equip : list) {
			initAddCache(equip);
		}
	}

	@Override
	public GoodsTypeEnum getGoodsTypeEnum() {
		return GoodsTypeEnum.Equipment;
	}

	@Override
	public RewardInfo toRewardInfo(Equip equip) {
		return RewardInfo.newBuilder().setEquip(EquipInfo.newBuilder().setId(equip.getConfigId())).build();
	}

	@Override
	public Equip newInstance() {
		return new Equip();
	}

	@Override
	public void buildPlayerAllInfo(Builder builder) {
		// TODO Auto-generated method stub

	}

}
