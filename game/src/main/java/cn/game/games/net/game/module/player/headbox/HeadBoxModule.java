package cn.game.games.net.game.module.player.headbox;

import cn.game.games.core.GoodsModule;
import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.GameEvent;
import cn.game.games.net.game.module.player.IdConstant;
import cn.game.protocol.manual.GoodsTypeEnum;
import cn.game.protocol.manual.OpType;
import cn.game.protocol.protobuf.PlayerMsg.PlayerAllInfo.Builder;
import cn.game.protocol.protobuf.RewardMsg.RewardInfo;

/**    
 * 这里只是为了可以给头像框奖励
 * 2024年8月22日 下午4:16:56
 * @author SYQ
 */
public class HeadBoxModule extends GoodsModule<HeadBox, HeadBox> {
	private static EventTypeEnum[] events = new EventTypeEnum[] { EventTypeEnum.PLAYER_CREATE };
	@Override
	public EventTypeEnum[] getEventTypes() {
		return events;
	}

	@Override
	public void handleEvent(GameEvent event) {

		switch (event.getType()) {

		case PLAYER_CREATE: {
			break;
		}
		}

	}

	@Override
	public long getCount(int configId) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Object add(int configId, int count, OpType opType) {
		player.getPlayerModule().addId(IdConstant.HEAD_BOX, configId);
		return new HeadBox(configId, count);
	}

	@Override
	public void checkConfig(int id) {

	}

	@Override
	public HeadBox newInstance() {
		return new HeadBox();
	}

	@Override
	public RewardInfo toRewardInfo(HeadBox reward) {
		return RewardInfo.newBuilder().setHead(reward.getConfigId()).build();
	}

	@Override
	public boolean del(int configId, long count, OpType... args) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean del(long uid, OpType... args) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public HeadBox get(int configId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HeadBox get(long uid) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public GoodsTypeEnum getGoodsTypeEnum() {
		return GoodsTypeEnum.HeadBox;
	}

	@Override
	public void initAddCache(HeadBox item) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addCacheStackable(HeadBox item) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addCacheNoStackable(HeadBox item) {
		// TODO Auto-generated method stub

	}

	@Override
	public void removeCache(HeadBox item) {
		// TODO Auto-generated method stub

	}

	@Override
	public void buildPlayerAllInfo(Builder builder) {

	}
}
