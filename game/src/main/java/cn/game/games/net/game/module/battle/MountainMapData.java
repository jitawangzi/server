package cn.game.games.net.game.module.battle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MountainMapData {
    /**
     * 地图数据
     */
   private Map<Integer, List<MountainMapNodeData>> mapData = new HashMap<>();
   /**
    * 当前节点id
    */
    private int curNodeId;
    /**
     * 成功
     */
    private boolean success;
     /**
     * 积分
     */
    private int score;
    /**
     * 奖励
     */
    private Map<Integer, Integer> scoreReward=new HashMap<>();
    /**
     * 血量
     */
    private float hp;
    /**
     * 结束时间
     */
    private int endTime;
    /**
     *  buff列表
     */
    private  List<Integer> buffBag=new ArrayList<>();


    public Map<Integer, List<MountainMapNodeData>> getMapData() {
        return mapData;
    }

    public void setMapData(Map<Integer, List<MountainMapNodeData>> mapData) {
        this.mapData = mapData;
    }

    public int getEndTime() {
        return endTime;
    }

    public void setEndTime(int endTime) {
        this.endTime = endTime;
    }

    public float getHp() {
        return hp;
    }

    public void setHp(float hp) {
        this.hp = hp;
    }

    public Map<Integer, Integer> getScoreReward() {
        return scoreReward;
    }

    public void setScoreReward(Map<Integer, Integer> scoreReward) {
        this.scoreReward = scoreReward;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public int getCurNodeId() {
        return curNodeId;
    }

    public void setCurNodeId(int curNodeId) {
        this.curNodeId = curNodeId;
    }
}
