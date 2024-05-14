//package cn.game.games.net.game.handler;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import org.springframework.stereotype.Component;
//
//import cn.game.core.net.client.NetClient;
//import cn.game.core.net.socket.handler.BaseHandler;
//import cn.game.games.cache.base.PlayerCacheFactory;
//import cn.game.games.cache.entity.Player;
//import cn.game.games.net.game.helper.PlayerHelper;
//import cn.game.games.net.game.manager.PlayerManager;
//import cn.game.games.net.game.module.award.RewardItem;
//import cn.game.games.net.game.module.item.ItemModule;
//import cn.game.games.net.game.module.item.ItemUse;
//import cn.game.games.util.PbBuilder;
//import cn.game.protocol.generated.config.OldItemConfig;
//import cn.game.protocol.generated.enume.ResourceEnum;
//import cn.game.protocol.generated.manager.OldItemManager;
//import cn.game.protocol.manual.ErrorMsgEnum;
//import cn.game.protocol.protobuf.ItemMsg;
//import cn.game.protocol.protobuf.PbProtocol;
//import cn.game.protocol.protobuf.RewardMsg.RewardInfo;
//
///**
// * 道具处理器
// */
//
//@Component
//public class ItemHandler extends BaseHandler {
//
//	@Override
//	protected int getModule() {
//		return 0x0b;
//	}
//
//	@Override
//	protected void inititialize() {
//
//		putInvoker(PbProtocol.DeprecatedItemSaleRequest_0b000001, this::sale);
//		putInvoker(PbProtocol.DeprecatedItemUseRequest_0b000003, this::use);
//		putInvoker(PbProtocol.DecomposeRequest_0b000005, this::decompose);
//	}
//
//	/**
//	* 出售
//	* @param client
//	* @param message
//	*/
//
//	private void sale(NetClient client, Object message) {
//		ItemMsg.DeprecatedItemSaleRequest_0b000001 req = (ItemMsg.DeprecatedItemSaleRequest_0b000001) message;
//		ItemMsg.DeprecatedItemSaleResponse_0b000002 resp = ItemMsg.DeprecatedItemSaleResponse_0b000002
//				.getDefaultInstance();
//		int id = req.getId();
//		int num = req.getNum();
//		OldItemConfig itemConfig = OldItemManager.getInstance().getItemConfig(id);
//		if (itemConfig == null) {
//			client.sendProtocol(resp, ErrorMsgEnum.config_data_not_found.getId());
//			return;
//		}
//		ItemModule itemModule = PlayerCacheFactory.getCache(client.getPlayerId(), ItemModule.class);
//		// if (!itemModule.map().containsKey(id)) {
//		// client.sendProtocol(resp, ErrorMsgEnum.player_data_not_found.getId());
//		// return ;
//		// }
//		// if (!itemConfig.getSell()) {
//		// client.sendProtocol(resp, ErrorMsgEnum.not_sale.getId());
//		// return ;
//		// }
//		// //扣减数量
//		// itemModule.delRepositoryItem(id, num);
//		// 获得金币
//		PlayerHelper.addResources(client.getPlayerId(), ResourceEnum.Coin.getId(), num * itemConfig.getPrice());
//
//		client.sendProtocol(resp);
//
//	}
//
//	private void decompose(NetClient client, Object message) {
//			ItemMsg.DecomposeRequest_0b000005 req = (ItemMsg.DecomposeRequest_0b000005) message;
//			ItemMsg.DecomposeResponse_0b000006.Builder resp = ItemMsg.DecomposeResponse_0b000006.newBuilder();
//			List<String> idList = req.getUidList();
//			int type = req.getType();
//			long playerId = client.getPlayerId(); Player player = PlayerManager.getInstance().getPlayer(playerId);
//			
//			List<RewardInfo> rewards = new ArrayList<>();
//			for (String id : idList) {
//				long uid = Long.parseLong(id);
//				if (type == GoodsTypeEnum.Chip.getId()) {
//					ChipOp chipOp = PlayerCacheFactory.getCache(client.getPlayerId(), ChipOp.class);
//					if (!chipOp.checkLock(playerId, uid)) {
//						client.sendProtocol(resp, ErrorMsgEnum.unlock.getId());
//						return;
//					}
//					List<RewardItem> rewardItems = chipOp.decompose(playerId, uid);
//					if (rewardItems == null) {
//						client.sendProtocol(resp, ErrorMsgEnum.resource_not_enough.getId());
//						return;
//					}
//					rewards.addAll(rewardItems);
//				} else 
//			}
//			
//			resp.addAllReward(rewards);
//			client.sendProtocol(resp);
//			
//			}
//
//	protected void use(NetClient client, Object message) {
//
//		ItemMsg.DeprecatedItemUseRequest_0b000003 req = (ItemMsg.DeprecatedItemUseRequest_0b000003) message;
//		ItemMsg.DeprecatedItemUseResponse_0b000004.Builder resp = ItemMsg.DeprecatedItemUseResponse_0b000004
//				.newBuilder();
//
//		int id = req.getId();
//		// long target = StringUtils.isEmpty(req.getTarget()) ? 0 :
//		// Long.parseLong(req.getTarget());
//		int count = req.getCount();
//
//		OldItemConfig item = OldItemManager.getInstance().getItemConfig(id);
//		if (item == null) {
//			client.sendProtocol(resp, ErrorMsgEnum.config_data_not_found.getId());
//			return;
//		}
//		ItemModule itemModule = PlayerCacheFactory.getCache(client.getPlayerId(), ItemModule.class);
//		long itemCount = itemModule.getCount(id);
//		if (itemCount < count) {
//			client.sendProtocol(resp, ErrorMsgEnum.resource_not_enough.getId());
//			return;
//		}
//		if (!item.getUse()) {
//			client.sendProtocol(resp, ErrorMsgEnum.not_use.getId());
//			return;
//		}
//
//		Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
//
//		ItemUse itemUse = ItemUse.valueOf(item.getType());
//		List<RewardItem> addEffects = new ArrayList<>();
//		if (itemUse != null) {
//			addEffects = itemUse.use(player, id, count, item.getEffect());
//		}
//
//		if (addEffects.isEmpty()) {
//			client.sendProtocol(resp, ErrorMsgEnum.not_use.getId());
//			return;
//		}
//
//		itemModule.del(id, count);
//		resp.addAllReward(PbBuilder.buildRewardInfo(addEffects));
//
//		client.sendProtocol(resp);
//	}
//
//}
