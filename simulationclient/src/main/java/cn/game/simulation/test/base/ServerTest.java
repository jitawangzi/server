package cn.game.simulation.test.base;

import java.util.List;

import com.google.protobuf.Message;

import cn.game.protocol.generated.enume.ActivityTypeEnum;
import cn.game.simulation.client.Client;
import cn.game.simulation.client.ServerTestContext;
import cn.game.simulation.test.jmeter.util.JmeterUtil;
import cn.game.util.Rnd;
import cn.game.util.log.LoggerManager;

public abstract class ServerTest<TRequest extends Message, TResponse extends Message> {

	static {
		try {
			LoggerManager.init();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

    /**
     * 构建调试用的固定请求。
     * <p>
     * 场景：人工手动测试、快速验证协议连通性。
     * 特点：参数写死(Hardcoded)，强制返回非空消息。
     */
	public abstract Message buildDebugRequest(Client client);

    /**
     * 尝试构建模拟真实场景的请求。
     * <p>
     * 场景：压力测试机器人、自动化回归测试。
     * 特点：根据 Client 当前状态(等级、背包、任务等)、静态数表等动态填充参数。
     *      如果当前状态不符合业务逻辑(如等级不足)，则返回 null (模拟玩家此时不会点击该按钮)。
     */
	public abstract Message tryBuildSimulationRequest(Client client);

	 /**
     * 校验响应数据的正确性,这里默认服务器已经成功返回消息，并且消息没有错误
     * <p>
     * 作用：检查服务器返回的 response 中的数据值是否符合预期。
     * 检查逻辑：
     *      1. 如果某字段必须有值，那么是否有值？
     *      2. 如果字段有值，那么这个值是否在合理范围内？
     *
     * @param client   当前客户端对象（包含最新状态）
     * @param request  刚才发送的请求消息（用于上下文对比）必定不是null
     * @param response 服务器返回的响应消息
     * @return 错误描述，如果为null或者空字符串表示没有错误
     */
    public abstract String verifyResponse(Client client, TRequest requestMessage, TResponse responseMessage);
	
	public void start() throws Exception {
		ServerTestContext.init();
		Client client = new Client(ServerTestContext.passportUsername, ServerTestContext.pwd, ServerTestContext.serverId,
				ServerTestContext.version);
		ServerTestContext.send(client, () -> buildDebugRequest(client));
	}

}
