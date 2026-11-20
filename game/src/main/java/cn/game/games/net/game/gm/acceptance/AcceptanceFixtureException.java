package cn.game.games.net.game.gm.acceptance;

/**
 * 模块验收造数与测试夹具专用异常
 *
 * @author shuyongqiang
 */
public class AcceptanceFixtureException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /** 是否属于造数失败（true表示造数报错，false表示夹具自检未达标） */
    private final boolean provisionFailed;

    /**
     * 构造夹具异常
     *
     * @param message 错误信息
     * @param provisionFailed true表示造数失败，false表示夹具自检缺陷
     */
    public AcceptanceFixtureException(String message, boolean provisionFailed) {
        super(message);
        this.provisionFailed = provisionFailed;
    }

    /**
     * 构造夹具异常（参数顺序兼容）
     *
     * @param provisionFailed true表示造数失败，false表示夹具自检缺陷
     * @param message 错误信息
     */
    public AcceptanceFixtureException(boolean provisionFailed, String message) {
        this(message, provisionFailed);
    }

    /**
     * 构造带原因的夹具异常
     *
     * @param message 错误信息
     * @param cause 根本原因
     * @param provisionFailed true表示造数失败，false表示夹具自检缺陷
     */
    public AcceptanceFixtureException(String message, Throwable cause, boolean provisionFailed) {
        super(message, cause);
        this.provisionFailed = provisionFailed;
    }

    /**
     * 获取是否为造数失败
     *
     * @return true表示造数失败
     */
    public boolean isProvisionFailed() {
        return provisionFailed;
    }
}
