package cn.game.games.net.game.gm.command;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.stereotype.Component;

import cn.game.core.base.ServerContext;
import cn.game.core.event.ServerEventTypeEnum;
import cn.game.core.exception.LogicException;
import cn.game.core.net.client.LogoutType;
import cn.game.games.cache.entity.Chapter;
import cn.game.games.cache.entity.Player;
import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.net.cross.guild.service.GuildServiceInterface;
import cn.game.games.net.game.gm.AbstractGm;
import cn.game.games.net.game.helper.PlayerHelper;
import cn.game.games.net.game.helper.ServerHelper;
import cn.game.games.net.game.helper.TestHelper;
import cn.game.games.net.game.manager.GameClientManager;
import cn.game.games.net.game.manager.PlayerManager;
import cn.game.games.net.game.module.battle.BattleModule;
import cn.game.games.net.game.module.develop.DevelopModule;
import cn.game.protocol.generated.config.BattleConfig;
import cn.game.protocol.generated.manager.BattleManager;
import cn.game.protocol.manual.ErrorMsgEnum;
import cn.game.protocol.manual.OpType;
import cn.game.protocol.protobuf.PlayerMsg.PlayerResetPush_01100016;
import cn.game.util.DateUtil;
import cn.game.util.LinuxTimeShift;
import cn.game.util.LinuxTimeShift.PreviewResult;
import cn.game.util.ServerType;

@Component
public class PlayerGm extends AbstractGm {

    @Override
    public void init() {
        register("tdlv", this::tdlv);
        register("playerquit", this::playerquit);
        register("playerdel", this::playerdel);
        register("huanfu", this::huanfu);
        register("newday", this::newday);
        register("gamenewday", this::gamenewday);
        register("guildnewday", this::guildnewday);
        register("super", this::superCmd);
        register("init", this::initCmd);
        register("time", this::time);
    }

    private void tdlv(Player player, String[] params) {
        int p1 = getInt(params, 1);
        if (p1 == 0) {
            throw new LogicException(ErrorMsgEnum.gm_cmd_param.ID);
        }
        DevelopModule developModule = player.getDevelopModule();
        developModule.setHeavenlyDaoLevel(p1);
    }

    private void playerquit(Player player, String[] params) {
        // 把某人退出
        if (params.length == 1) {
            // 退出所有人
            GameClientManager.getInstance().logoutAll(LogoutType.GMTestRequest);
        } else {
            // 退出某人
            TestHelper.logoutPlayer(getLong(params, 1), LogoutType.GMTestRequest);
        }
    }

    private void playerdel(Player player, String[] params) {
        long p1 = getLong(params, 1);
        long delId = p1 == 0 ? player.getPlayerId() : p1;
        if (delId == 0) {
            throw new LogicException(ErrorMsgEnum.gm_cmd_param.ID);
        }
        // 将某人删档
        TestHelper.deletePlayerData(delId);
    }

    private void huanfu(Player player, String[] params) {
        String serverId = getString(params, 1);
        TestHelper.transferServer(player.getPlayerId(), serverId);
    }

    private void newday(Player player, String[] params) {
        int nowDay = DateUtil.getDay();
        player.getData().setRefreshDay(nowDay - 1);
        PlayerHelper.refreshDay(player);
        // 通知客户端跨天了， 使用登陆来刷新所有数据。
        PlayerHelper.sendProtocol(player.getPlayerId(), PlayerResetPush_01100016.getDefaultInstance());
    }

    private void gamenewday(Player player, String[] params) {
        ServerContext.getInstance().fireEvent(ServerEventTypeEnum.NewDay);
        int nowDay = DateUtil.getDay();
        PlayerManager.getInstance().getAllPlayer().values().forEach(p -> {
            p.getData().setRefreshDay(nowDay - 1);
            PlayerHelper.refreshDay(p);
        });
    }

    private void guildnewday(Player player, String[] params) {
        List<GuildServiceInterface> allServerInterface = ServerHelper.getAllServerInterface(ServerType.Cross, GuildServiceInterface.class); 
        for (GuildServiceInterface guildServiceInterface : allServerInterface) {
            guildServiceInterface.testGuildNewDay() ; 
        }
    }

    private void superCmd(Player player, String[] params) {
        PlayerHelper.addResources(player, 100001, 1000000, OpType.Test);
        PlayerHelper.addResources(player, 100007, 100000, OpType.Test);
        PlayerHelper.addResources(player, 100201, 100000, OpType.Test);
        PlayerHelper.addResources(player, 101003, 1000, OpType.Test);
        PlayerHelper.addResources(player, 709056, 100000, OpType.Test);

        PlayerHelper.addResources(player, 709061, 5, OpType.Test);
        PlayerHelper.addResources(player, 709062, 5, OpType.Test);
        PlayerHelper.addResources(player, 709063, 5, OpType.Test);
        PlayerHelper.addResources(player, 709064, 5, OpType.Test);
        PlayerHelper.addResources(player, 709065, 5, OpType.Test);

        PlayerHelper.addResources(player, 1500106, 5, OpType.Test);
        PlayerHelper.addResources(player, 1500206, 5, OpType.Test);
        PlayerHelper.addResources(player, 1500306, 5, OpType.Test);
        PlayerHelper.addResources(player, 1500406, 5, OpType.Test);
        PlayerHelper.addResources(player, 1500606, 5, OpType.Test);

        PlayerHelper.addResources(player, 355003, 5, OpType.Test);
        PlayerHelper.addResources(player, 355004, 5, OpType.Test);
        PlayerHelper.addResources(player, 355005, 5, OpType.Test);
        PlayerHelper.addResources(player, 332003, 5, OpType.Test);
        PlayerHelper.addResources(player, 332004, 5, OpType.Test);
        TestHelper.addItems(player, 3000, 0);
        PlayerHelper.addResources(player, 203001, 2000, OpType.Test);

        // 跳过新手引导
        player.getPlayerModule().getGuideMap().put(6, 99);
        player.getPlayerModule().getGuideMap().put(7, 99);
        player.getPlayerModule().getGuideMap().put(8, 99);
        player.getPlayerModule().getGuideMap().put(9, 99);

        BattleModule battleModule = player.getBattleModule();
        battleModule.setMainBattleHighest(110025);
        BattleConfig battleConfig = BattleManager.instance().getNullable(110025);
        while (battleConfig != null) {
            battleModule.addChapter(battleConfig.ID);
            Chapter chapter = battleModule.getChapter(battleConfig.ID);
            chapter.setBattleTime(30);
            chapter.setPass(true);
            battleConfig = BattleManager.instance().getNullable(battleConfig.preBattle);
        }
    }

    private void initCmd(Player player, String[] params) {
        TestHelper.setMaxCurrency(player, OpType.Test);
    }

    private void time(Player player, String[] params) {
        if (ServerContext.getInstance().getRunMode().isProduction()) {
            throw new LogicException(ErrorMsgEnum.production_gm_not_allow.ID) ;
        }
        
        String[] timeArgs=  new String[] { getString(params, 1) }; 
        LocalDateTime now = LocalDateTime.now(); 
        PreviewResult previewPlannedTime = LinuxTimeShift.previewPlannedTime(timeArgs); 
        if (previewPlannedTime!=null) {
            if (previewPlannedTime.crossedDay) {
                PlayerManager.getInstance().getAllPlayer().values().forEach(p -> {
                    p.getData().setOfflineTime(DateUtil.currentTimeMillis());
                });
            }
        }
        LinuxTimeShift.main(timeArgs); 
        
        LocalDateTime nowDateTime = LocalDateTime.now();
        if (previewPlannedTime!=null) {
            if (previewPlannedTime.crossedDay) {
                PlayerManager.getInstance().getAllPlayer().values().forEach(p -> {
                    PlayerHelper.refresh(p);
                });
            }
        }
        PlayerManager.getInstance().getAllPlayer().values().forEach(p -> {
            p.fireAndHandleEvent(EventTypeEnum.SystemTimeChange);
        });
        ServerContext.getInstance().fireEvent(ServerEventTypeEnum.SystemTimeChange);
        if (nowDateTime.isAfter(now) && DateUtil.diff(nowDateTime, now, ChronoUnit.WEEKS) > 0) {
            ServerContext.getInstance().fireEvent(ServerEventTypeEnum.NewWeek);
            PlayerManager.getInstance().getAllPlayer().values().forEach(p -> {
                p.fireAndHandleEvent(EventTypeEnum.NewWeek);
            });
        }else if (nowDateTime.isAfter(now) && DateUtil.diff(nowDateTime, now, ChronoUnit.MONTHS) > 0) {
            ServerContext.getInstance().fireEvent(ServerEventTypeEnum.NewMonth);
            PlayerManager.getInstance().getAllPlayer().values().forEach(p -> {
                p.fireAndHandleEvent(EventTypeEnum.NewMonth);
            });
        }else if (nowDateTime.isAfter(now) && DateUtil.diff(nowDateTime, now, ChronoUnit.DAYS) > 0) {
            ServerContext.getInstance().fireEvent(ServerEventTypeEnum.NewDay);
            PlayerManager.getInstance().getAllPlayer().values().forEach(p -> {
                p.fireAndHandleEvent(EventTypeEnum.NewDay);
            });
        }
    }
}
