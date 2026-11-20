package cn.game.games.net.game.gm.acceptance;

/**
 * 模块自动化验收运行模式
 *
 * @author shuyongqiang
 */
public enum AcceptanceRunMode {
    /** 仅运行功能契约与边界测试用例 */
    CONTRACT,
    /** 仅运行多态分支与全量配表扫描 */
    MATRIX_SCAN,
    /** 双轨全量运行 */
    ALL;

    /**
     * 解析运行模式字符串，默认为 ALL
     *
     * @param modeStr 模式字符串
     * @return 对应的运行模式
     */
    public static AcceptanceRunMode parse(String modeStr) {
        if (modeStr == null || modeStr.isBlank()) {
            return ALL;
        }
        for (AcceptanceRunMode mode : values()) {
            if (mode.name().equalsIgnoreCase(modeStr.trim())) {
                return mode;
            }
        }
        return ALL;
    }
}
