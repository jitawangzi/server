package cn.game.games.net.cross.activity;

/**
 * 跨服活动路由缺失异常
 * 
 * @author shuyongqiang
 */
public class CrossActivityRouteMissingException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	private final long activityUid;
	private final int errorCode;

	public CrossActivityRouteMissingException(long activityUid, int errorCode) {
		super("Cross activity route missing for uid=" + activityUid + ", errorCode=" + errorCode);
		this.activityUid = activityUid;
		this.errorCode = errorCode;
	}

	public long getActivityUid() {
		return activityUid;
	}

	public int getErrorCode() {
		return errorCode;
	}
}
