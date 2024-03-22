package cn.game.games.net.game.remote;

import java.io.Serializable;

/**
 * @Description 服务器状态
 * @date 2021年2月25日 下午2:44:03
 * @author SYQ
 */
public class ServerStatus implements Serializable {

	/**  */
	private static final long serialVersionUID = -2603877919582450787L;
	private int online;

	public int getOnline() {
		return online;
	}

	public void setOnline(int online) {
		this.online = online;
	}

}
