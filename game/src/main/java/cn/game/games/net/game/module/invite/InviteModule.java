package cn.game.games.net.game.module.invite;

import cn.game.core.cache.RedisLocalCache;
import cn.game.games.cache.entity.Invite;
import cn.game.games.core.BasePlayerModule;
import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.GameEvent;
import cn.game.games.core.log.GameLogger;
import cn.game.games.net.data.mapper.InviteMapper;
import cn.game.games.net.data.mapper.MailMapper;
import cn.game.games.net.game.helper.PlayerHelper;
import cn.game.games.net.game.manager.PlayerManager;
import cn.game.games.util.DAO;
import cn.game.protocol.protobuf.PlayerMsg;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static cn.game.games.core.event.EventTypeEnum.LoginSuccess;
import static cn.game.games.core.event.EventTypeEnum.PLAYER_CREATE;

/**
 * @ClassName InviteModule
 *
 * @description: 邀请模块
 * @author: ly
 * @create: 2024-12-24 16:54 @Version 1.0
 */
public class InviteModule extends BasePlayerModule {
    /**
     * 邀请到的目标列表 和 等级信息
     */
    @JsonIgnore
    Map<Long,Integer> targetLvMap = new HashMap<>();
    /**
     * 领取过奖励的任务下标
     */
    List<Integer> rewardIndexList = new ArrayList<>();
    @Override
    public void buildPlayerAllInfo(PlayerMsg.PlayerAllInfo.Builder builder) {

    }

    public void saveInviteData(long invitePid){
        Invite invite = new Invite();
        invite.setPlayerId(invitePid);
        invite.setDstPid(playerId);
        DAO.insert(invite).onSuccess((h)->{
            GameLogger.invite(player,invitePid);
        });

    }


    @Override
    public EventTypeEnum[] getEventTypes() {
        return new EventTypeEnum[]{PLAYER_CREATE, LoginSuccess};
    }

    @Override
    public void handleEvent(GameEvent event) {
        switch (event.getType()){
            case PLAYER_CREATE -> {
                long invitePid = event.getLong(0);
                if (invitePid <= 0){
                    return;
                }
                if (invitePid == playerId){
                    return;
                }
                saveInviteData(invitePid);
                break;
            }
            case LoginSuccess -> {
                refreshInviteDataLv();
                break;
            }

        }
    }

    CompletionStage<Void> refreshInviteDataLv() {
        CompletableFuture<Void> voidFuture = new CompletableFuture();
        DAO.execute(InviteMapper.class,"selectByIndexPid",playerId).onSuccess(result ->{
            List<Invite> list = (List<Invite>) result;
            List<Long> targetPids = new ArrayList<>();
            list.forEach(data ->{
                targetPids.add(data.getDstPid());
            });
            Map<Long,Integer> targetMap = new HashMap<>();
            PlayerManager.getInstance().batchGetSimplePlayerListFromRedisAsync(targetPids).onSuccess(targetPlayerList ->{
                targetPlayerList.forEach(simplePlayer -> {
                    targetMap.put(simplePlayer.id,simplePlayer.level);
                });
                this.targetLvMap = targetMap;
                voidFuture.complete(null);
            }).onFailure(e ->{
                e.printStackTrace();
            });
        }).onFailure(e ->{
            e.printStackTrace();
        });
        return voidFuture;
    }

    @Override
    public Class<?>[] defaultDbMapperClass() {
        return new Class<?>[] { InviteMapper.class };
    }
}
