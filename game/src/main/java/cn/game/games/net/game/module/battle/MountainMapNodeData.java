package cn.game.games.net.game.module.battle;

import java.util.ArrayList;
import java.util.List;

public class MountainMapNodeData {
    /**
     * 节点id
     */
    private int nodeId;
    /**
     * 节点状态  0 未打 1 已打 2
     */
    private int nodeStatus;
    /**
     * 节点类型
     */
    private int nodeType;
    /**
     * 怪物id
     */
    private int monsterId;
    /**
     * 等级参数
     */
    private int levelpro;
    /**
     * 事件参数
     */
    private int eventId;
    /**
     * 商店参数
     */
    private List<Integer> shopId =new ArrayList<>();

    public int getNodeId() {
        return nodeId;
    }

    public void setNodeId(int nodeId) {
        this.nodeId = nodeId;
    }

    public int getNodeStatus() {
        return nodeStatus;
    }

    public void setNodeStatus(int nodeStatus) {
        this.nodeStatus = nodeStatus;
    }

    public int getNodeType() {
        return nodeType;
    }

    public void setNodeType(int nodeType) {
        this.nodeType = nodeType;
    }

    public int getMonsterId() {
        return monsterId;
    }

    public void setMonsterId(int monsterId) {
        this.monsterId = monsterId;
    }

    public int getLevelpro() {
        return levelpro;
    }

    public void setLevelpro(int levelpro) {
        this.levelpro = levelpro;
    }

    public int getEventId() {
        return eventId;
    }

    public void setEventId(int eventId) {
        this.eventId = eventId;
    }



    public void clear()
    {
        nodeStatus=0;
        nodeType=0;
        monsterId=0;
        levelpro=0;
        eventId=0;
        shopId.clear();
    }

    public List<Integer> getShopId() {
        return shopId;
    }

    public void setShopId(List<Integer> shopId) {
        this.shopId = shopId;
    }
}
