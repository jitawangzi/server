package cn.game.core.exception;

import cn.game.core.task.BatchProcessResult;

/**    
 * 批量处理的异常
 * 2024年11月8日 10:44:08
 * @author SYQ
 */
public class BatchProcessException extends RuntimeException {
	private static final long serialVersionUID = -3743651000596835414L;
	private final BatchProcessResult result;

	public BatchProcessException(String message, Throwable cause, BatchProcessResult result) {
		super(message, cause);
		this.result = result;
	}

	public BatchProcessResult getResult() {
		return result;
	}
}