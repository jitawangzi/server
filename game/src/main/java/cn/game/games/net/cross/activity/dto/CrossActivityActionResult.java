package cn.game.games.net.cross.activity.dto;

import java.io.Serializable;

/**
 * 跨服活动玩家行为执行结果 DTO
 * 
 * @author shuyongqiang
 */
public class CrossActivityActionResult implements Serializable {
	private static final long serialVersionUID = 1L;

	private boolean success;
	private int errorCode;
	private String errorMsg;
	private String resultPayloadJson;

	public CrossActivityActionResult() {
	}

	public CrossActivityActionResult(boolean success, int errorCode, String errorMsg, String resultPayloadJson) {
		this.success = success;
		this.errorCode = errorCode;
		this.errorMsg = errorMsg;
		this.resultPayloadJson = resultPayloadJson;
	}

	public static CrossActivityActionResult success(String resultPayloadJson) {
		return new CrossActivityActionResult(true, 0, null, resultPayloadJson);
	}

	public static CrossActivityActionResult fail(int errorCode, String errorMsg) {
		return new CrossActivityActionResult(false, errorCode, errorMsg, null);
	}

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public int getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(int errorCode) {
		this.errorCode = errorCode;
	}

	public String getErrorMsg() {
		return errorMsg;
	}

	public void setErrorMsg(String errorMsg) {
		this.errorMsg = errorMsg;
	}

	public String getResultPayloadJson() {
		return resultPayloadJson;
	}

	public void setResultPayloadJson(String resultPayloadJson) {
		this.resultPayloadJson = resultPayloadJson;
	}
}
