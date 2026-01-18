# Testing Framework & Guidelines

## 1. 模拟客户端框架 (Simulation Client)
本项目的测试不使用传统的单元测试，而是使用**模拟客户端 (`Simulation Client`)** 进行黑盒/集成测试。

- **基类**: 所有测试类必须继承 `ClientBaseScenarioTest`。
- **位置**: `src/test/java/...`
- **核心逻辑**: 重写 `executeProcess()` 方法，模拟玩家行为。

## 2. 常用测试 API
在 `executeProcess()` 中使用：

- **`sendAndWait(Message request)`**: 发送请求并阻塞等待响应。
  - *自动推断*: 框架会自动根据 Request ID + 1 推断 Response ID 并进行匹配。
  - *返回值*: 返回接收到的 Response 对象。
- **`synchronizeDataFromServer()`**: 主动请求刷新 `PlayerAllInfo`。
- **`getPlayerAllInfo()`**: 获取当前客户端缓存的玩家核心数据。

## 3. 测试代码模板

```java
public class ClientGuildTest extends ClientBaseScenarioTest {
    public static void main(String[] args) { new ClientGuildTest().start(); }

    @Override
    protected void executeProcess() throws Exception {
        // 1. 创建公会
        GuildCreateRequest req = GuildCreateRequest.newBuilder().setName("MyGuild").build();
        GuildCreateResponse resp = (GuildCreateResponse) sendAndWait(req);
        
        // 2. 验证结果
        if (resp.getGuildId() == 0) {
            throw new RuntimeException("Guild create failed");
        }
        
        // 3. 验证数据同步
        synchronizeDataFromServer();
        if (getPlayerAllInfo().getGuildId() != resp.getGuildId()) {
             throw new RuntimeException("Guild ID mismatch");
        }
    }
}
```

## 4. 运行方式
不要直接在 IDE 运行 JUnit。使用提供的 PowerShell 脚本：
- **启动服务器**: `powershell .\run_server.ps1`
- **运行测试**: `powershell .\run_client.ps1 <完整类名>`

## 5. 对抗性测试要求
- **边界值**: 必须测试字符串为空、最大长度、特殊字符。
- **资源不足**: 必须测试在资源为 0 或不足时的行为（应收到错误码，而不是 Exception）。
- **重复请求**: 测试连续发送两次相同请求（幂等性）。
