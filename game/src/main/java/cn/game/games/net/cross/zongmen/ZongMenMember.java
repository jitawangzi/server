package cn.game.games.net.cross.zongmen;

import cn.game.protocol.protobuf.ZongMenMsg;

import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName ZongMenMember
 *
 * @description: 宗门成员
 * @author: ly
 * @create: 2025-02-05 18:30 @Version 1.0
 */
public class ZongMenMember implements ZongMenConstants.ZongMenEventHandler {

    long playerId;
    /**玩家战斗力 */
    int power;
    /** 加入时间戳*/
    long joinTime;
    /**贡献值 */
    int contribution;
    /**累计贡献值 */
    int totalContribution;
    /**职位 */
    int position;
    /** 领取过的宗门活跃度奖励  */
    List<Integer> rewardLivenessIndexList  = new ArrayList<>();

    public ZongMenMember() {
    }
    public ZongMenMember(long playerId, int power,int position) {
        this.playerId = playerId;
        this.power = power;
        setJoinTime(System.currentTimeMillis());
        setContribution(0);
        setPosition(position);
    }

    public long getPlayerId() {
        return playerId;
    }

    public void setPlayerId(long playerId) {
        this.playerId = playerId;
    }

    public int getPower() {
        return power;
    }

    public void setPower(int power) {
        this.power = power;
    }

    public long getJoinTime() {
        return joinTime;
    }

    public void setJoinTime(long joinTime) {
        this.joinTime = joinTime;
    }

    public int getContribution() {
        return contribution;
    }

    public void setContribution(int contribution) {
        this.contribution = contribution;
    }

    public int getTotalContribution() {
        return totalContribution;
    }

    public void setTotalContribution(int totalContribution) {
        this.totalContribution = totalContribution;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public void addcontribution(int contribution){
        this.contribution += contribution;
        this.totalContribution += contribution;
    }

    public List<Integer> getRewardLivenessIndexList() {
        return rewardLivenessIndexList;
    }

    public void setRewardLivenessIndexList(List<Integer> rewardLivenessIndexList) {
        this.rewardLivenessIndexList = rewardLivenessIndexList;
    }

    public ZongMenMsg.ZongMenMemberProto.Builder toProto(){
        ZongMenMsg.ZongMenMemberProto.Builder builder = ZongMenMsg.ZongMenMemberProto.newBuilder();
        builder.setJoinTime((int) (joinTime/1000L));
        builder.setPid(playerId);
        builder.setPosition(position);
        builder.setTodayContribute(contribution);
        builder.setTodayContribute(totalContribution);
        return builder;
    }

    @Override
    public ZongMenConstants.ZongMenEvenType[] getRegisterEvent() {
        return new ZongMenConstants.ZongMenEvenType[]{ZongMenConstants.ZongMenEvenType.CROSS_DAY};
    }

    @Override
    public void handleEventType(ZongMenConstants.ZongMenEvenType type, ZongMenInfo info, Object... params) {
        switch (type){
            case CROSS_DAY ->{
                //每日重置 贡献度
                this.totalContribution = 0;
            }
        }
    }
}
