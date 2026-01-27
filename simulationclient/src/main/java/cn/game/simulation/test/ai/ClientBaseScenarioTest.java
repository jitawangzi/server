package cn.game.simulation.test.ai;

import cn.game.protocol.protobuf.TestMsg.TestGmCmdRequest_6f000001;
import com.google.protobuf.Message;

import cn.game.core.exception.LogicException;
import cn.game.core.net.vertx.VxHolder;
import cn.game.core.util.ExceptionHelper;
import cn.game.protocol.generated.helper.ManagerHelper;
import cn.game.protocol.protobuf.PlayerMsg.PlayerLoginRequest_01000001;
import cn.game.simulation.client.Client;
import cn.game.simulation.client.ServerTestContext;
import cn.game.util.log.LoggerManager;

/**    
 * 客户端测试基类
 * 默认情况下每次使用新账号登录
 * 如果需要固定账号，配置env.properties 的user.name
 * 2026年1月9日 21:17:26
 */
public abstract class ClientBaseScenarioTest {
    
    /** 
    * 客户端实例，缓存了部分登录服务器返回的核心数据 
    * @link Client#getPlayerAllInfo()
    * 高频使用、可能被多模块引用的重要数据（如等级、货币、道具、关卡进度、核心养成等）
    */
    protected Client client;

    /** 
     * 发送请求并阻塞等待结果
     * @param <T>
     * @param request
     * @return 强转后的返回消息实例,需要根据协议规则推断返回类型
     * @throws RuntimeException，如果没有异常，表示一个成功的网络消息返回
     */
    protected <T> T sendAndWait(Message request) {
    	try {
			return client.sendProtocolAndWait(request);
		} catch (Exception e) {
			LogicException cause = ExceptionHelper.findCause(e, LogicException.class);
			if (cause != null) {
				throw new RuntimeException("请求协议:"+request.getClass().getSimpleName() + " 返回错误，错误码:"+cause.getErrorCode(),e.getCause()) ; 
			} else {
				throw new RuntimeException("请求协议:"+request.getClass().getSimpleName() + " 返回错误",e.getCause()) ; 
			}
		} 
    }

    /** 
     * 发送 GM 指令并等待结果
     * @param cmd GM 指令内容，例如 "item 100001 10"
     * @return GM 指令响应包
     */
    protected Message sendGmCmd(String cmd) {
        TestGmCmdRequest_6f000001 gmReq = TestGmCmdRequest_6f000001.newBuilder()
                .setCmd(cmd)
                .build();
        return sendAndWait(gmReq);
    }
    /** 
     * 从服务器同步最新的数据
     * {@link Client#getPlayerAllInfo()} 会被更新为最新数据
     * 作用：
     * 1、可以用来刷新客户端本地缓存为服务器端最新的数据
     * 2、通过查看服务器最新数据，可以验证经过某些行为后，
     * 服务器的数据是否符合预期
     * 
     * 相当于玩家重新登录服务器或者重连服务器
     * 可以在功能测试的模拟代码中酌情使用
     * @throws RuntimeException 如果同步失败则抛出异常
     */
    protected void synchronizeDataFromServer() {
    	try {
    		client.sendProtocolAndWait(PlayerLoginRequest_01000001.newBuilder().setSessionId(client.getSessionId()).setReconnect(true).setPlayerId(client.getPlayerId()+"").build());
    	} catch (Exception e) {
    		LogicException cause = ExceptionHelper.findCause(e, LogicException.class);
    		if (cause != null) {
    			throw new RuntimeException("同步服务端数据协议返回错误，错误码:"+cause.getErrorCode(),e.getCause()) ; 
    		} else {
    			throw new RuntimeException("同步服务端数据协议返回错误",e.getCause()) ; 
    		}
    	} 
    }
    
    /**
     * 抽象测试逻辑实现，需要实现具体的测试逻辑
     *
     * 通用模式说明（Simulation + Validation Pattern）——写在父类注释中以便复用：
     *
     * 目标/契约：
     * - 目的：在模拟客户端环境下验证一条或一组协议的端到端行为（请求、服务器副作用、后续状态），
     *   而不是仅凭单次请求的返回认为成功；任何违反预期的数据应抛出异常使测试失败。
     * - 输入：测试前需准备好被测场景数据（通过 GM、管理脚本或数据库 fixture），确保测试幂等与可控。
     * - 输出：在断言失败时抛出运行时异常（RuntimeException）以终止测试并标记失败。
     *
     * 推荐流程（步骤化）：
     * 1) 准备/确认测试数据（可在外部用 GM 或直接写 DB 完成）。
     * 2) 发起读取型请求（例如列表请求）获取初始快照并校验基本字段（非空、范围、逻辑一致性）。
     *    - helper：实现类似 validateXxx(Response, context) 的校验函数用于复用。
     * 3) 对有副作用的操作（例如查看/领取/删除）采取“先选取目标 -> 发送请求 -> 重新读取 -> 验证目标状态变更”的模式。
     *    - 优先对单个目标（便于定位）进行操作；如果无匹配目标，再使用“一键”/批量接口。
     * 4) 在每次副作用后重新请求并校验服务器最终状态（而非仅检查操作返回），保证服务器端的确发生了预期变化。
     *
     * 校验点示例（视协议而定）：
     * - 列表返回：uid 非空且唯一、时间字段合法、附件/子项字段合法；
     * - 单项变更：操作后对应对象的状态字段发生预期变化（例如 see/receive 标志）；
     * - 奖励/附件：当期望返回奖励时，检查 rewardsCount>0，并可进一步比对奖励明细与附件对应关系。
     *
     * 断言与失败处理：
     * - 在发现不符合预期的数据时，应抛出带有上下文的 RuntimeException 或特定测试异常，便于快速定位；
     * - 避免吞掉异常或仅记录日志，测试应在断言失败时立即失败。
     *
     * 可用辅助约定（建议在基类或测试工具中提供）：
     * - sendAndWait(Message request)：发送并阻塞等待 protobuf 响应；
     * - validateXxx(...)：对响应做基本结构与字段校验；
     * - retryUntil(fetch, check, maxTry, sleepMs)：重试获取与校验的工具。
     *
     * 例：实现邮件类操作的伪流程
     * - list = sendAndWait(MailListRequest)
     * - validate(list)
     * - if exists unread: sendAndWait(MailSeeRequest(uid)) else sendAndWait(MailSeeRequest.getDefaultInstance())
     * - reList = retryUntil(() -> sendAndWait(MailListRequest), r -> checkSeeChanged(r, uid), 3, 200)
     * - assert see changed
     * - similar for receive/delete
     *
     * 总结：把“发送请求 -> 验证返回结构 -> 对有副作用的操作再次读取并验证最终状态”的流程固化为模版，
     * 任何新的 executeProcess 实现都应遵循此模板并复用基类提供的通用工具函数。
     *
     * @throws Exception 测试执行期间抛出异常表示失败
     */
    protected abstract void executeProcess() throws Exception;
    
	/** 
	 * 测试程序启动入口
	 * @throws Exception
	 */
	public void start(){
		try {
			LoggerManager.init();
			ManagerHelper.init();

			ServerTestContext.init();
			run();
            System.out.println(">>> 测试流程结束，结果：PASS");
            System.exit(0); // 成功退出
		} catch (Exception e) {
            System.err.println(">>> 测试流程失败: " + e.getMessage());
            e.printStackTrace();
            System.exit(1); // 失败退出
        }
	}
	
    /** 
     * 
     * @throws Exception
     */
    private void run() throws Exception {

        // 1. 自动登录流程
    	client = new Client(ServerTestContext.passportUsername, ServerTestContext.pwd, ServerTestContext.serverId,
				ServerTestContext.version);

		client.loginPassportProto(ServerTestContext.loginServerUrl);
		client.loginGateway(ServerTestContext.gateServerIp, ServerTestContext.gateServerPort);

		client.waitInit();
        System.out.println("登录成功，开始执行测试流程...");

        // 2. 执行具体的测试逻辑 (由子类/AI实现)
        executeProcess();
    
    }
}