package cn.game.games.net.game.gm.command;

import java.util.List;

import org.springframework.stereotype.Component;

import cn.game.games.cache.entity.Player;
import cn.game.games.net.game.gm.AbstractGm;
import cn.game.games.net.game.helper.PlayerHelper;
import cn.game.games.net.game.helper.ServerHelper;
import cn.game.games.net.game.helper.TestHelper;
import cn.game.games.net.game.remote.GameServerInterface;
import cn.game.protocol.protobuf.RewardMsg.RewardInfo;
import cn.game.protocol.protobuf.RewardMsg.RewardPush_55000501;

@Component
public class ItemGm extends AbstractGm {

    @Override
    public void init() {
        register("item", this::item);
        register("itemdel", this::itemdel);
    }

    private void item(Player player, String[] params) {
        int p1 = getInt(params, 1);
        int p2 = getInt(params, 2);
        List<RewardInfo> items = TestHelper.addItems(player, p1, p2);
        PlayerHelper.sendProtocol(player.getPlayerId(), RewardPush_55000501.newBuilder().addAllRewards(items).build());
    }

    private void itemdel(Player player, String[] params) {
        long pid = getLong(params, 1); 
        int p2 = getInt(params, 2);
        int p3 = getInt(params, 3);
        
        if (pid == 0) {
            pid = player.getPlayerId(); 
        }
        GameServerInterface playerProxy = ServerHelper.getPlayerProxy(pid); 
        playerProxy.delResources(pid, p2, p3); 
    }
}
