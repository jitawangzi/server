package cn.game.games.net.game.module.battle;

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
    private float levelpro;
    /**
     * 事件参数
     */
    private float eventId;
    /**
     * 商店参数
     */
    private float shopId;

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

    public float getLevelpro() {
        return levelpro;
    }

    public void setLevelpro(float levelpro) {
        this.levelpro = levelpro;
    }

    public float getEventId() {
        return eventId;
    }

    public void setEventId(float eventId) {
        this.eventId = eventId;
    }

    public float getShopId() {
        return shopId;
    }

    public void setShopId(float shopId) {
        this.shopId = shopId;
    }

    public void clear()
    {
        nodeStatus=0;
        nodeType=0;
        monsterId=0;
        levelpro=0;
        eventId=0;
        shopId=0;
    }
}
