package cn.game.simulation.test.base;

import java.util.List;

import com.google.protobuf.Message;

import cn.game.protocol.generated.enume.ActivityTypeEnum;
import cn.game.simulation.client.Client;
import cn.game.simulation.client.ServerTestContext;
import cn.game.simulation.test.jmeter.util.JmeterUtil;
import cn.game.util.Rnd;
import cn.game.util.log.LoggerManager;

public abstract class ServerTest {

	static {
		try {
			LoggerManager.init();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/** 
	 * 生成协议数据
	 * @param client
	 * @return
	 */
	public abstract Message getMessage(Client client);

	/** 
	 * 压测模式下的协议数据生成
	 * @param client
	 * @return 
	 */
	public abstract Message getMessagePressure(Client client);

	public String getHexStringMessage(Client client) {
		Message message = getMessage(client);
		return JmeterUtil.toHexString(message);
	}

	public void start() throws Exception {
		ServerTestContext.init();
		Client client = new Client(ServerTestContext.passportUsername, ServerTestContext.pwd, ServerTestContext.serverId,
				ServerTestContext.version);
		ServerTestContext.send(client, () -> getMessage(client));
	}

}
