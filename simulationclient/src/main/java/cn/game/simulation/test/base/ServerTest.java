package cn.game.simulation.test.base;

import com.google.protobuf.Message;

import cn.game.simulation.client.Client;
import cn.game.simulation.test.jmeter.util.JmeterUtil;
import cn.game.util.log.LoggerManager;

public abstract class ServerTest {

	static {
		try {
      System.setProperty("user.dir", "D:\\Party\\server\\server\\simulationclient");
			LoggerManager.init();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public abstract Message getMessage(Client client);

	public String getHexStringMessage(Client client) {
		Message message = getMessage(client);
		return JmeterUtil.toHexString(message);
	};

}
