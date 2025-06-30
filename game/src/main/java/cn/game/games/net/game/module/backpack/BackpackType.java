package cn.game.games.net.game.module.backpack;

/**
 * 背包类型枚举
 */
public enum BackpackType {
    EQUIPMENT(1, "装备背包"),
    MATERIAL(2, "材料背包"),
    QUEST(3, "任务背包");

    private int code;
    private String desc;

    BackpackType(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}

