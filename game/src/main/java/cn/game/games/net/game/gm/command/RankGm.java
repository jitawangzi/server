package cn.game.games.net.game.gm.command;

import org.springframework.stereotype.Component;
import cn.game.games.cache.entity.Player;
import cn.game.games.net.game.gm.AbstractGm;
import cn.game.games.net.game.module.rank.RankService;
import cn.game.protocol.generated.enume.RankType;

@Component
public class RankGm extends AbstractGm {
    @Override
    public void init() {
        register("rank", this::rank);
        register("rankds", this::rankds);
        register("rankreward", this::rankreward);
    }

    private void rank(Player player, String[] params) {
        int p1 = getInt(params, 1);
        int p2 = getInt(params, 2);
        RankService.getInstance().setScoreAsync(player.getServerId(), RankType.get(p1), player.getPlayerId(), p2);
    }
    
    private void rankds(Player player, String[] params) {
        int p1 = getInt(params, 1);
        RankService.getInstance().setScoreAsync(player.getServerId(), RankType.DaShengLeiTaiSeason, player.getPlayerId(), p1);
        RankService.getInstance().setScoreAsync(player.getServerId(), RankType.DaShengLeiTaiDay, player.getPlayerId(),p1);
    }

    private void rankreward(Player player, String[] params) {
        int p1 = getInt(params, 1);
        RankService.getInstance().rewardGm(p1);
    }
}
