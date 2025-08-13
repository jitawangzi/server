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
     * 积分最大值
     */
    private int scoreMax;
    /**
     * 奖励
     */
    private Map<Integer, Integer> scoreReward=new HashMap<>();
    /**
     * 血量
     */
    private int hp;
    /**
     * 结束时间
     */
    private int endTime;
    /**
     * 刷新次数(剩余)
     */
    private int refreshNum;
    /**
     * 排行榜id
     */
    private int rankId;
     /**
     * 等级系数
     */
    private int levelPro;
    /**
     *  buff列表
     */

    private Map<Integer,Integer> buffBag = new HashMap<>();

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

    public int getHp() {
        return hp;
    }

    public void setHp(int hp) {
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

    public int getRefreshNum() {
        return refreshNum;
    }

    public void setRefreshNum(int refreshNum) {
        this.refreshNum = refreshNum;
    }

    public int getScoreMax() {
        return scoreMax;
    }

    public void setScoreMax(int scoreMax) {
        this.scoreMax = scoreMax;
    }




    public int getRankId() {
        return rankId;
    }

    public void setRankId(int rankId) {
        this.rankId = rankId;
    }

    public Map<Integer, Integer> getBuffBag() {
        return buffBag;
    }

    public void setBuffBag(Map<Integer, Integer> buffBag) {
        this.buffBag = buffBag;
    }

    public int getLevelPro() {
        return levelPro;
    }

    public void setLevelPro(int levelPro) {
        this.levelPro = levelPro;
    }
}
