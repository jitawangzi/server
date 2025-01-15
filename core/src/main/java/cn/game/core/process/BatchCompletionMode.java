package cn.game.core.process;

public enum BatchCompletionMode {
	/** 所有任务完成才算完成 */
	ALL_COMPLETE,
	/** 任意任务完成即可 */
	ANY_COMPLETE,
	/** 大部分完成即可 */
	MAJORITY_COMPLETE
}
