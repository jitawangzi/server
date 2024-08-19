package cn.game.games.net.game.exception;

public class LogicException extends Throwable {

	private static final long serialVersionUID = 1L;
	private int errorCode;

	public LogicException() {
	}

	public LogicException(int errorCode) {
		super(errorCode + "", null, false, false);
		this.errorCode = errorCode;
	}

	public int getErrorCode() {
		return errorCode;
	}
}
