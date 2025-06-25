package cn.game.core.execute;

/**
 * 队列满异常，当任务队列已满时抛出
 */
public class QueueFullException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public QueueFullException(String message) {
        super(message);
    }
}

