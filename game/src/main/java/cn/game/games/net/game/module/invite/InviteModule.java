package cn.game.games.net.game.module.invite;

import static cn.game.games.core.event.EventTypeEnum.PLAYER_CREATE;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

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
    /**
     * 邀请到的目标列表 和 等级信息
     */
    @JsonIgnore
    Map<Long,Integer> targetLvMap = new HashMap<>();

	/** 邀请到的目标列表  */
	@JsonIgnore
	List<Long> targetPlayers = new ArrayList<>();
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
        return new EventTypeEnum[]{PLAYER_CREATE};
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
            }
        }
    }

    @Override
    protected void initFromDb(ListIterator<?> iterator) {
        List<Invite> inviteList = (List<Invite>) iterator.next();
        List<Long> targetPids = new ArrayList<>();
        inviteList.forEach(invite -> {targetPids.add(invite.getDstPid());});
        meargeInvitePid(targetPids,null);

		for (Invite invite : inviteList) {
			targetPlayers.add(invite.getDstPid());
		}
    }

    @Override
    public boolean alwaysStoreDataInStandaloneTable() {
        return true;
    }

    CompletionStage<Void> refreshInviteDataLv() {
        CompletableFuture<Void> voidFuture = new CompletableFuture();
        List<Long> targetPids = new ArrayList<>(targetLvMap.keySet());
        meargeInvitePid(targetPids,voidFuture);
        return voidFuture;
    }

    private void meargeInvitePid(List<Long> targetPids,  CompletableFuture<Void> voidFuture) {
        Map<Long,Integer> targetMap = new HashMap<>();
        PlayerManager.getInstance().batchGetSimplePlayerListFromRedisAsync(targetPids).onSuccess(targetPlayerList ->{
            targetPlayerList.forEach(simplePlayer -> {
                targetMap.put(simplePlayer.id,simplePlayer.level);
            });
            this.targetLvMap = targetMap;
            if (voidFuture != null){
                voidFuture.complete(null);
            }
        }).onFailure(e ->{
            e.printStackTrace();
            if (voidFuture != null){
                voidFuture.complete(null);
            }
        });
    }

	/** 
	 * 获取邀请到的目标玩家id和等级
	 * @return
	 */
	public Future<Map<Long, Integer>> getTargetLvMap() {
		return PlayerManager.getInstance()
				.batchGetSimplePlayerListFromRedisAsync(targetPlayers)
				.map(list -> list.stream().collect(Collectors.toMap(player -> player.id, player -> player.level)));
	}

    @Override
    public Class<?>[] defaultDbMapperClass() {
        return new Class<?>[] { InviteMapper.class };
    }
}
