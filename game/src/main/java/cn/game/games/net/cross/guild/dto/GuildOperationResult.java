package cn.game.games.net.cross.guild.dto;

import cn.game.protocol.manual.ErrorMsgEnum;

/**
 * 公会操作结果
 */
public class GuildOperationResult<T> {
    private boolean success;
	private ErrorMsgEnum errorCode;
    private String errorMessage;
    private T data;

    public static <T> GuildOperationResult<T> success(T data) {
        GuildOperationResult<T> result = new GuildOperationResult<>();
        result.success = true;
        result.data = data;
        return result;
    }

	public static <T> GuildOperationResult<T> failure(ErrorMsgEnum errorCode, String errorMessage) {
        GuildOperationResult<T> result = new GuildOperationResult<>();
        result.success = false;
        result.errorCode = errorCode;
        result.errorMessage = errorMessage;
        return result;
    }

    // Getters and setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

	public ErrorMsgEnum getErrorCode() {
        return errorCode;
    }

	public void setErrorCode(ErrorMsgEnum errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}

