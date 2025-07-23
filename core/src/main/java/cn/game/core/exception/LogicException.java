package cn.game.core.exception;

/**    
 * 逻辑异常，一般是服务器主动抛出的，设置错误码，代表某种错误。 
 * 2024年10月28日 11:56:19
 * @author SYQ
 */
public class LogicException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	private int errorCode;
	private String errorMessage;

	public LogicException() {
		super(null, null, false, false);
	}

	public LogicException(int errorCode) {
		super(errorCode + "", null, false, false);
		this.errorCode = errorCode;
	}
	public LogicException(int errorCode, String errorMessage) {
		super(errorCode + "", null, false, false);
		this.errorCode = errorCode;
		this.errorMessage = errorMessage;
	}

	public int getErrorCode() {
		return errorCode;
	}

	public String getErrorMessage() {
		return errorMessage;
	}
	
}
