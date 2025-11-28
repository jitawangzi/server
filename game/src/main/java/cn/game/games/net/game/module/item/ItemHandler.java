package cn.game.games.net.game.module.item;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import cn.game.core.exception.LogicException;
import cn.game.core.net.client.NetClient;
import cn.game.games.cache.entity.Player;
import cn.game.games.net.game.handler.GameBaseHandler;
import cn.game.games.net.game.helper.PlayerHelper;
import cn.game.games.net.game.manager.GameConstants;
import cn.game.games.net.game.manager.PlayerManager;
import cn.game.protocol.generated.config.ItemConfig;
import cn.game.protocol.generated.enume.EntryEffectEnum;
import cn.game.protocol.generated.manager.ItemManager;
import cn.game.protocol.manual.ErrorMsgEnum;
import cn.game.protocol.manual.OpType;
import cn.game.protocol.protobuf.ItemMsg;
import cn.game.protocol.protobuf.ItemMsg.ItemSellRequest_0b000007;
import cn.game.protocol.protobuf.ItemMsg.ItemSellResponse_0b000008;
import cn.game.protocol.protobuf.ItemMsg.ItemUseInfo;
import cn.game.protocol.protobuf.PbProtocol;
import cn.game.protocol.protobuf.RewardMsg.RewardInfo;
import cn.game.util.GameUtil;
import cn.game.util.Rnd;

/**
 * 道具处理器
 */
@Component
public class ItemHandler extends GameBaseHandler {

    @Override
    protected int getModule() {
        return 0x0b;
    }

    @Override
    protected void inititialize() {
        putInvoker(PbProtocol.ItemUseRequest_0b000003, this::use);
        putInvoker(PbProtocol.ItemSellRequest_0b000007, this::sell);
    }

    protected void use(NetClient client, Object message) {
        ItemMsg.ItemUseRequest_0b000003 req = (ItemMsg.ItemUseRequest_0b000003) message;
        ItemMsg.ItemUseResponse_0b000004.Builder resp = ItemMsg.ItemUseResponse_0b000004.newBuilder();
		List<ItemUseInfo> itemUseList = req.getItemUseList();
		Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());

		for (ItemUseInfo itemUseInfo : itemUseList) {
			player.checkClientRequestCount(itemUseInfo.getCount());
			if (!PlayerHelper.isEnough(player, itemUseInfo.getId(), itemUseInfo.getCount())) {
				client.sendProtocol(resp, ErrorMsgEnum.resource_not_enough.getId());
				return;
			}
		}
		for (ItemUseInfo itemUseInfo : itemUseList) {
			int id = itemUseInfo.getId();
			int count = itemUseInfo.getCount();
			int param = itemUseInfo.getParam();
			ItemConfig item = ItemManager.instance().getNullable(id);
			if (item == null) {
				client.sendProtocol(resp, ErrorMsgEnum.config_data_not_found.getId());
				return;
			}
			if (item.ItemType == 13) {
				// 手操卡有概率不消耗
				int entryEffectValue = player.getAttrModule().getEntryEffectValue(EntryEffectEnum.ReleaseFree); 
				if (!Rnd.hit(entryEffectValue)) {
					PlayerHelper.delResources(player, id,count, OpType.ItemOpen);
				}
			}else {
				PlayerHelper.delResources(player, id, count, OpType.ItemOpen);
			}
			ItemUse itemUse = ItemUse.valueOf(item.ItemType);
			if (itemUse == null) {
				throw new LogicException(1, "item  use not implement: " + item.ItemType);
			}
			List<RewardInfo> rewards = itemUse.use(player, id, count, param);
			resp.addAllReward(rewards);
		}
        client.sendProtocol(resp);
    }

    private void sell(NetClient client, Object message) {
        ItemSellRequest_0b000007 req = (ItemSellRequest_0b000007) message;
        List<Integer> idList = req.getIdList();
        List<Integer> countList = req.getCountList();
        ItemSellResponse_0b000008 defaultInstance = ItemSellResponse_0b000008.getDefaultInstance();
        Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());
		int[] transformIdAndCount = GameUtil.transformIdAndCount(idList, countList); 
		PlayerHelper.delResources(player, transformIdAndCount, OpType.ItemSell);
		List<RewardInfo> rewards = new ArrayList<>();
		for (int i = 0; i < idList.size(); i++) {
			int id = idList.get(i);
			int count = countList.get(i);
			player.checkClientRequestCount(count);
			ItemConfig item = ItemManager.instance().getNullable(id);
			if (item == null) {
				client.sendProtocol(defaultInstance, ErrorMsgEnum.config_data_not_found.getId());
				return;
			}
			if (item.Decompose.length == 0) {
				continue; 
			}
			if (item.Decompose.length > 2) {
				throw new IllegalArgumentException("Decompose length > 2 : " + item.Decompose.length);
			}
			
			List<RewardInfo> resources = PlayerHelper.addResources(player, item.Decompose[0], item.Decompose[1] * count, OpType.ItemSell);
			rewards.addAll(resources);
		}

        ItemSellResponse_0b000008.Builder resp = ItemSellResponse_0b000008.newBuilder();
		resp.addAllReward(rewards);

        client.sendProtocol(resp.build());
    }
}
