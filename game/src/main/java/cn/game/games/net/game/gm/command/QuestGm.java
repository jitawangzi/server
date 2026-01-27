package cn.game.games.net.game.gm.command;

import java.util.List;
import org.springframework.stereotype.Component;
import cn.game.games.cache.entity.Player;
import cn.game.games.net.game.gm.AbstractGm;
import cn.game.games.net.game.helper.PlayerHelper;
import cn.game.games.net.game.module.quest.QuestModule;
import cn.game.protocol.protobuf.RewardMsg.RewardInfo;
import cn.game.protocol.protobuf.RewardMsg.RewardPush_55000501;
import cn.game.protocol.generated.manager.QuestManager;

@Component
public class QuestGm extends AbstractGm {
    @Override
    public void init() {
        register("quest", this::quest);
    }

    private void quest(Player player, String[] params) {
        int p1 = getInt(params, 1);
        QuestManager.instance().get(p1);
        // 完成某个任务
        QuestModule module = player.getQuestModule();
        List<RewardInfo> items = module.finish(p1, 0);
        PlayerHelper.sendProtocol(player.getPlayerId(), RewardPush_55000501.newBuilder().addAllRewards(items).build());
    }
}
