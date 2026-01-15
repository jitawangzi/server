---

# 模拟客户端测试指南

本文档旨在指导开发者如何使用模拟客户端工程（Simulation Client）进行游戏服务器的功能测试。通过模拟客户端，我们可以启动虚拟玩家，发送指定功能的协议与服务器交互，并处理服务器的返回数据。

> **⚠️ 前置阅读**
>
> 在使用本工程前，请确保已阅读并理解 [protocol.md](protocol.md)，以掌握协议定义的基础知识。

---

## 1. 核心概念：Client 对象

`cn.game.simulation.client.Client` 是模拟客户端的关键类。
该类代表了一个虚拟玩家对象，维护着该玩家的核心游戏数据状态。通常情况下，测试人员只需了解其作用，无需修改此类代码。

### 1.1 核心字段
以下是 `Client` 类中用于支持逻辑判断和协议构造的常用字段：

| 字段名 | 类型 | 描述 |
| :--- | :--- | :--- |
| `playerAllInfo` | `PlayerAllInfo` | **玩家核心数据**。<br>对应 `PlayerMsg.proto` 中的 `PlayerAllInfo`，通常在登录后返回并自动保存。 |
| `dataMap` | `Map<String, Object>` | **通用数据容器**。<br>可自定义 Key（如协议名），用于灵活保存测试过程中的临时上下文数据。 |

### 1.2 常用方法

获取客户端玩家缓存的核心游戏数据：

```java
public PlayerAllInfo getPlayerAllInfo() {
    return playerAllInfo;
}
```

---

## 2. 功能测试开发流程

测试某个具体功能时，请遵循以下流程新建测试类。

### 2.1 新建测试类
1.  **继承父类**：新建的测试类必须继承 `ClientBaseScenarioTest`。
2.  **命名规范**：`Client[功能名]Test`（例如邮件功能：`ClientMailTest`）。
3.  **存放目录**：需与 `ClientBaseScenarioTest` 保持在同一目录。

### 2.2 代码示例
以邮件功能测试为例：

```java
public class ClientMailTest extends ClientBaseScenarioTest {

    public static void main(String[] args) {
        // 启动测试
        new ClientMailTest().start();
    }

    /**
     * 在此处实现具体的测试逻辑
     *
     * 详细规则参考父类 executeProcess() 方法注释,这里只简要说明
     *
     * 时序性：功能通常涉及多个协议，AI需根据业务逻辑编排发送顺序。
     * 示例：副本玩法需先发送“请求副本数据”，获取数据缓存后，再发送“领取奖励”。
    * 状态依赖：后一个请求的参数可能依赖前一个请求的返回数据；若前置条件不满足（如已领取过奖励），则跳过这个请求，继续下一个。
    * 执行方式*：确定顺序后，按步骤逐一启动测试。
     */
    @Override
    protected void executeProcess() throws Exception {
        // 1. 发送请求
        // 2. 验证返回
        // 3. 逻辑断言
    }
}
```


### 2.3 常用 API
在 `executeProcess` 方法中，你可以使用以下父类提供的方法：

*   **同步发送消息**：
    `sendAndWait(Message request)` —— 发送消息并阻塞等待服务器返回,需要根据协议规则推断返回类型
*   **同步玩家数据**：
    `synchronizeDataFromServer()` —— 请求服务器刷新当前玩家的最新核心数据 (`playerAllInfo`)。

---

## 3. 编译与运行

### 3.1 编译工程及启动服务器
这个脚本先会自动进行完整的 Maven 工程编译。
启动/重启服务器。
在编写完代码后，准备测试前，需要先执行这个脚本，进行代码编译，重新启动服务器。 

```powershell
powershell .\run_server.ps1
```

### 3.2 运行测试

使用 PowerShell 脚本执行测试：

```powershell
powershell .\run_client.ps1 <完整类名>
```

### 3.3 结果检查与排错

执行脚本后，请根据控制台输出判断结果：

*   ✅ **TEST PASSED**
    *   **含义**：任务完成，测试通过。

*   ❌ **COMPILATION FAILED**
    *   **行动**：阅读上方 Maven 错误日志，修复 Java 语法错误后重试。

*   ❌ **TEST FAILED**
    *   **行动**：阅读控制台打印的 Java 运行时日志，根据错误类型分析：
        *   **Timeout (超时)**：检查服务器是否正在运行，或代码逻辑是否被某些操作阻塞。
        *   **Assertion Error (断言错误)**：检查模拟数据（如道具数量、前置条件）是否与服务器逻辑预期不符。

> **重试规则**：对于同一个错误，允许自主修正代码并重试最多 **5次**。如果依然失败，请停止操作并报告日志分析结果。

---

## 4. 其他注意事项

1.  **请求机制**：
    客户端是同步阻塞模型。每次只能向服务器发送一个请求，在该请求收到回应（或超时）之前，不会发送新的请求。

2.  **账号管理**：
    *   **默认行为**：每次测试默认使用一个新的随机账号注册,服务器默认会给账号添加足够的资源，开启所有功能等，方便测试。 
    *   **固定账号**：如果需要保持使用同一个账号进行连续测试，请修改 `env.properties` 文件：

    ```properties
    # env.properties
    # 设置用户名时使用固定账号，不设置则每次重新注册
    user.name=19952
    ```