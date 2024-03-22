package cn.game.core.async;
/**
 * 跨线程的异步任务
 */
public interface AsyncTask {
	
	/** 初始化 */
	public static final int STAGE_INITAILZED = 0;
	/** doStart完成 */
	public static final int STAGE_START_DONE = 1;
	/** doIo完成 */
	public static final int STAGE_IO_DONE = 2;
	/** doStop完成,也可用于中止执行 */
	public static final int STAGE_STOP_DONE = 3;

	/**
	 * 操作开始时的操作,在当前线程中执行
	 * 
	 * @return
	 */
	public int doStart();

	/**
	 * 运行在另一个线程，如果要访问主线程共享对象，注意只读，竞态条件等
	 * 
	 * @return
	 */
	public int doIo();

	/**
	 * 异步操作结束后执行的操作,一般在初始线程中执行
	 * 
	 * @return
	 */
	public int doStop();

}
