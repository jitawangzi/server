package cn.game.games.net.game.module.invite;

import static cn.game.games.core.event.EventTypeEnum.PLAYER_CREATE;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import cn.game.core.net.vertx.VxHolder;
import cn.game.games.net.game.helper.PlayerHelper;
import cn.game.protocol.generated.config.InviteConfig;
import cn.game.protocol.generated.manager.InviteManager;
import cn.game.protocol.protobuf.QuestMsg;
import cn.game.protocol.protobuf.ServerMsg;
import cn.game.util.ServerType;
import com.fasterxml.jackson.annotation.JsonIgnore;

import cn.game.games.cache.entity.Invite;
import cn.game.games.core.BasePlayerModule;
import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.GameEvent;
import cn.game.games.core.log.GameLogger;
import cn.game.games.net.data.mapper.InviteMapper;
import cn.game.games.net.game.manager.PlayerManager;
import cn.game.games.util.DAO;
import cn.game.protocol.protobuf.PlayerMsg;
import io.vertx.core.Future;

/**
 * @ClassName InviteModule
 *
 * @description: 邀请模块
 * @author: ly
 * @create: 2024-12-24 16:54 @Version 1.0
 */
public class InviteModule extends BasePlayerModule {



    /**我邀请的人的等级 key  我邀请的人角色id，val 等级*/
    Map<Long, Integer> targetLvMap = new ConcurrentHashMap<>();

    /**
     * 领取过奖励的任务下标
     */
    List<Integer> rewardIndexList = new ArrayList<>();

    /**
     * 邀请我的人
     */
    long invitePid;
    @Override
    public void buildPlayerAllInfo(PlayerMsg.PlayerAllInfo.Builder builder) {

    }

    public void saveInviteData(long invitePid){
        this.invitePid = invitePid;
        Invite invite = new Invite();
        invite.setPlayerId(invitePid);
        invite.setDstPid(playerId);
        DAO.insert(invite).onSuccess((h)->{
            GameLogger.invite(player,invitePid);
            notifyLvUpToInvitePlayer();
        });
    }

    private void notifyLvUpToInvitePlayer() {
        if (this.invitePid == 0){
            return;
        }
        VxHolder.executeBlockingWithTimeout(()->{
            String inviteServerId = PlayerManager.getInstance().getServerId(invitePid);
            if (inviteServerId != null){
                ServerMsg.NotifyInviteBindAndLvUpRequest_7d000041.Builder req = ServerMsg.NotifyInviteBindAndLvUpRequest_7d000041.newBuilder();
                req.setPid(invitePid);
                req.setTargetPid(playerId);
                req.setLv(player.getLevel());
                log.info(String.format("notifyLvUpToInvitePlayer req:%s",req));
//                PlayerHelper.sendToRemotePlayer(invitePid,inviteServerId,req.build());
                VxHolder.broadcastRemoteServer(ServerType.Game,req.build());
            }
            return null;
        });
    }

    public void updateTargetLv(long targetPid, int lv){
        int curLv = targetLvMap.getOrDefault(targetPid,1);
        if (curLv > lv){
            return;
        }
        targetLvMap.put(targetPid,lv);
    }

    public void checkRedHot() {
        boolean hasRedhot = false;
    for (InviteConfig config : InviteManager.instance().list()){
        if (rewardIndexList.contains(config.ID)){
            continue;
        }
        int needNum = config.Condition[0];
        int needLv = config.Condition[1];
        if (getFinishLvCount(needLv) >= needNum){
            hasRedhot = true;
            break;
        }
    }
      if (hasRedhot) {
            notifyInviteTaskList();
        }

    }

    @Override
    public EventTypeEnum[] getEventTypes() {
        return new EventTypeEnum[]{PLAYER_CREATE, EventTypeEnum.LoginSuccess,EventTypeEnum.LevelUp};
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

            case LevelUp -> notifyLvUpToInvitePlayer();
        }
    }

    @Override
    protected void initFromDb(ListIterator<?> iterator) {
        List<Invite> inviteList = (List<Invite>) iterator.next();
        List<Long> targetPids = new ArrayList<>();
        inviteList.forEach(invite -> {targetPids.add(invite.getDstPid());});
        getTargetLvMap(targetPids).onSuccess(lvMap ->{
            lvMap.forEach( (targetPid ,lv) ->{
                updateTargetLv(targetPid,lv);
            });
            checkRedHot();
        }).onFailure(e ->{e.printStackTrace();});
    }

    @Override
    public boolean alwaysStoreDataInStandaloneTable() {
        return true;
    }


	/** 
	 * 获取邀请到的目标玩家id和等级
	 * @return
	 */
	private Future<Map<Long, Integer>> getTargetLvMap(List<Long> targetPlayers) {
		return PlayerManager.getInstance()
				.batchGetSimplePlayerListFromRedisAsync(targetPlayers)
				.map(list -> list.stream().collect(Collectors.toMap(player -> player.id, player -> player.level)));
	}

    public Map<Long, Integer> getTargetLvMap() {
        return targetLvMap;
    }

    @Override
    public Class<?>[] defaultDbMapperClass() {
        return new Class<?>[] { InviteMapper.class };
    }

    public void notifyInviteTaskList() {
        QuestMsg.InviteTaskListResponse_20000042.Builder res = QuestMsg.InviteTaskListResponse_20000042.newBuilder();
        InviteManager.instance().list().forEach(inviteConfig -> {
            QuestMsg.InviteTask.Builder taskBuilder = QuestMsg.InviteTask.newBuilder().setIndex(inviteConfig.ID);
            int needNum = inviteConfig.Condition[0];
            int needLv = inviteConfig.Condition[1];
            int num = (int) targetLvMap.values().stream().filter(lv -> lv >= needLv).count();
            taskBuilder.setNum(num >= needNum ? needNum : num);
            res.addTaskList(taskBuilder);
        });
        res.addAllRewardIndexList(rewardIndexList);
        player.getGameClient().sendProtocol(res.build());
    }

    public int getFinishLvCount(int needLv) {
        return (int)targetLvMap.values().stream().filter(lv -> lv >= needLv).count();
    }
}
