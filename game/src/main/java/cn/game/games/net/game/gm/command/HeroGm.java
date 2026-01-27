package cn.game.games.net.game.gm.command;

import java.util.List;
import org.springframework.stereotype.Component;
import cn.game.games.cache.entity.Player;
import cn.game.games.net.game.gm.AbstractGm;
import cn.game.games.net.game.helper.PlayerHelper;
import cn.game.protocol.protobuf.RewardMsg.RewardInfo;
import cn.game.protocol.protobuf.RewardMsg.RewardPush_55000501;
import cn.game.protocol.manual.OpType;

@Component
public class HeroGm extends AbstractGm {
    @Override
    public void init() {
        register("hero", this::hero);
        register("herolv", this::herolv);
        register("qingshen", this::qingshen);
    }

    private void hero(Player player, String[] params) {
        int p1 = getInt(params, 1);
        int p2 = getInt(params, 2);
        List<RewardInfo> resources = PlayerHelper.addResources(player, p1, p2, OpType.Test);
        PlayerHelper.sendProtocol(player.getPlayerId(), RewardPush_55000501.newBuilder().addAllRewards(resources).build());
    }

    private void herolv(Player player, String[] params) {
        int p1 = getInt(params, 1);
        int p2 = getInt(params, 2);
        int size = player.getHeroModule().list().size();
        for (int i = 0; i < size; i++) {
            var hero = player.getHeroModule().list().stream().toList().get(i);
            if (hero.getConfigId() == p1) {
                hero.setLevel(p2);
            }
        }
    }
    
    private void qingshen(Player player, String[] params) {
         player.getDrawModule().getHeroRecruit().refresh();
    }
}
