package cn.game.games.net.game.exception;

public class LogicException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	private int errorCode;

	public LogicException() {
	}

	public LogicException(int errorCode) {
		this.errorCode = errorCode;
	}

	public int getErrorCode() {
		return errorCode;
	}

}
