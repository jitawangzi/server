package cn.game.core.net.transport;

import java.io.Serializable;

/**
 * @Description 表示远程调用的结果
 * @date 2020年9月11日 下午3:05:04
 * @author SYQ
 */
public class Result implements Serializable {

	/**  */
	private static final long serialVersionUID = 5526246354859249315L;
	private Object result;

	public Result() {
	}

	public Result(Object result) {
		this.result = result;
	}

	public Object getResult() {
		return result;
	}

	public void setResult(Object result) {
		this.result = result;
	}

}
