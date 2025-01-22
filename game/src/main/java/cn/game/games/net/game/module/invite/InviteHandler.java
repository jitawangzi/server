package cn.game.games.net.game.module.invite;

import java.util.ArrayList;
import java.util.List;

import cn.game.core.net.client.NetClient;
import cn.game.games.cache.entity.Player;
import cn.game.games.net.game.helper.PlayerHelper;
import cn.game.games.net.game.manager.PlayerManager;
import cn.game.protocol.generated.config.InviteConfig;
import cn.game.protocol.generated.manager.InviteManager;
import cn.game.protocol.manual.ErrorMsgEnum;
import cn.game.protocol.manual.OpType;
import cn.game.protocol.protobuf.QuestMsg;

/**
 * @ClassName InviteHandler
 *
 * @description:
 * @author: ly
 * @create: 2024-12-24 18:19 @Version 1.0
 */
public class InviteHandler {
    public static void list(NetClient client, Object message) {
        QuestMsg.InviteTaskListRequest_20000041 req = (QuestMsg.InviteTaskListRequest_20000041) message;
        long playerId = client.getPlayerId();
        Player player = PlayerManager.getInstance().getPlayer(playerId);
        InviteModule module = player.getInviteModule();
        module.notifyInviteTaskList();
    }

    public static void rewardInviteTask(NetClient client, Object o) {
        QuestMsg.RewardInviteTaskRequest_20000043 req = (QuestMsg.RewardInviteTaskRequest_20000043) o;
        QuestMsg.RewardInviteTaskResponse_20000044.Builder res = QuestMsg.RewardInviteTaskResponse_20000044.newBuilder();
        long playerId = client.getPlayerId();
        Player player = PlayerManager.getInstance().getPlayer(playerId);
        InviteModule module = player.getInviteModule();
        List<InviteConfig> rewardConfigList = new ArrayList<>();
        for (int rewardId  : req.getIndexListList()){
            if (module.rewardIndexList.contains(rewardId)){
                client.sendProtocol(res, ErrorMsgEnum.repeat_request.getId());
                return;
            }
            InviteConfig config = InviteManager.instance().get(rewardId);
            int needNum = config.Condition[0];
            int needLv = config.Condition[1];
            int num = module.getFinishLvCount(needLv);
            if (num < needNum){
                client.sendProtocol(res, ErrorMsgEnum.repeat_request.getId());
                return;
            }
            rewardConfigList.add(config);
        }
        rewardConfigList.forEach(rewardConfig->{
            res.addAllDrops(PlayerHelper.addResources(player,rewardConfig.Reward, OpType.inviteReward));
            module.rewardIndexList.add(rewardConfig.ID);
        });
        res.addAllIndexList(req.getIndexListList());
        client.sendProtocol(res);
    }
}
