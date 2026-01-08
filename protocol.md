***

# 通讯协议规范 (Protocol Specification)

本文档旨在规范客户端与服务端之间的通讯协议设计，基于 Google Protobuf 3 (`proto3`) 定义。

## 1. 基础规范

### 1.1 文件定义
*   **文件粒度**：以“功能模块”为单位划分 `.proto` 文件（例如：宠物系统、商店系统）。
*   **命名格式**：`[模块名]Msg.proto`。
    *   *示例*：宠物系统协议文件命名为 `PetMsg.proto`。
*   **包名定义**：所有协议文件统一使用以下包名：
    ```protobuf
    package cn.game.protocol.protobuf;
    ```
*   **依赖管理**：
    *   每个功能模块文件（如 `ShopMsg.proto`）可独立存在。
    *   公共数据结构定义在 `BaseMsg.proto` 或 `RewardMsg.proto` 中。
    *   功能模块可 `import` 基础模块，但**禁止**功能模块之间循环引用。

### 1.2 消息命名
消息（Message）名需遵循以下语义格式：
`[模块名][行为][类型]_[协议ID]`

| 组成部分 | 说明 | 示例 |
| :--- | :--- | :--- |
| **模块名** | 功能的英文名称（首字母大写） | `Pet` |
| **行为** | 该协议的具体操作动作 | `Composite` |
| **类型** | `Request` (请求) / `Response` (响应) / `Push` (推送) | `Request` |
| **协议ID** | 唯一标识符（详见下方 ID 规则） | `19000001` |

**完整示例**：
```protobuf
// 碎片合成宠物请求
message PetCompositeRequest_19000001 { ... }
```

---

## 2. 协议 ID 规则

协议 ID 为全局唯一的 16 进制数字，结构如下：
*   **高 8 位**：模块 ID（Module ID），同一模块下的所有协议此位必须相同。
*   **低 24 位**：消息 ID，用于区分同一模块下的不同协议。

### 2.1 请求与响应 (Request/Response)
遵循 `Request ID + 1 = Response ID` 的配对规则，便于通过 ID 自动关联请求与返回。
*   **Request**：由客户端发起。
*   **Response**：由服务器返回。

### 2.2 服务器推送 (Push)
表示服务器主动下发的消息（如聊天推送、踢人下线），无需客户端请求。
*   **ID 规则**：
    *   不得与 Request/Response 的 ID 冲突。
    *   应避免 `PushID` 与 `PushID - 1` 覆盖现有的 Request/Response ID 范围。
    *   *建议*：使用较高的低位 ID 区段来专门定义 Push 消息。

### 2.3 ID 定义示例
```protobuf
// [Request] 碎片合成宠物
message PetCompositeRequest_19000001 {
    int32 id = 1; // Pet表配置ID
}

// [Response] 合成结果（ID = 请求ID + 1）
message PetCompositeResponse_19000002 {
}

// [Push] 玩家退出/被踢推送
message PlayerLogoutPush_01100030 {
}
```

---

## 3. 代码生成配置 (Annotations)

在 `.proto` 文件中，通过注释形式配置参数，供代码生成器 (`PbProtocolGenerator`) 使用。

### 3.1 配置项说明

| 标签 | 说明 | 必选性 | 示例 |
| :--- | :--- | :--- | :--- |
| `HandlerPackage` | 生成的服务端 Handler 类所在的包路径。 | **必选** | `cn.game.games.net.game.module.pet` |
| `MessageModule` | 模块的数字编号（对应 ID 高 8 位）。 | **必选** | `19` |
| `ClientHandlerPackage` | 生成的客户端测试类 Handler 包路径。 | *可选* | `cn.game.simulation.client.handler` |
| `Function` | 对应功能的枚举名（InitialUI），用于默认的功能开启校验。 | *可选* | `SoulPets` |

### 3.2 配置示例
```protobuf
//@HandlerPackage cn.game.games.net.game.module.develop.pet
//@ClientHandlerPackage cn.game.simulation.client.handler
//@Function SoulPets
//@MessageModule 19

package cn.game.protocol.protobuf;
...
```

---

## 4. 开发工作流

### 4.1 代码生成
1.  修改或新增 `.proto` 文件。
2.  运行 `PbProtocolGenerator` 工具。
3.  **产出物**：
    *   **服务端**：生成 `PbProtocol` 类及对应的 Handler 桩代码。
    *   **客户端**：生成测试辅助类，命名规则为 `[Request协议名]Test`（例如 `PetCompositeRequest_19000001Test`）。

### 4.2 客户端测试
生成的 Test 类包含 `getMessage(Client client)` 方法。开发者可直接在该方法中为协议赋值并运行，工具会自动发送请求至服务端，便于接口联调。

> **常见问题排查**：
> 若新增协议后运行生成器提示 `message id not found`，通常是因为缓存或索引未更新，**重新执行一次**生成器即可解决。

---

## 5. 通讯模式与数据同步设计

### 5.1 玩家数据分类
玩家数据在架构上分为两类，采用不同的同步策略：

#### A. 核心基础数据 (PlayerAllInfo)
*   **定义**：高频使用、可能被多模块引用的重要数据（如等级、货币、道具、关卡进度、核心养成等）。
*   **同步机制**：
    *   **登录时**：服务器通过 `PlayerLoginResponse` 将全量核心数据下发。
    *   **维护**：客户端需缓存并实时维护这部分数据。
    *   **作用**：客户端利用本地缓存进行预先合法性检查（Pre-check）或UI展示。
*   **协议示例**：
    ```protobuf
    message PlayerLoginResponse_01000002 {
        PlayerAllInfo info = 1;     // 用户全量核心数据
        bool reconnect = 2;         // 是否断线重连
        string time = 3;            // 服务器时间戳
    }
    ```
*   **PlayerAllInfo中新增数据字段**：
需要在对应的xxxModule逻辑模块，复写buildPlayerAllInfo方法并填充数据，例如ItemModule: 
    ```	@Override
	public void buildPlayerAllInfo(PlayerAllInfo.Builder builder) {
		for (Item item : list()) {
			builder.addItems(item.toItemInfo());
		}
	}
    ```

#### B. 周边功能数据
*   **定义**：独立性强、低频使用或仅在特定界面展示的数据（如活动详情、排行榜、商店列表、邮件）。
*   **同步机制**：
    *   **按需拉取**：不随登录下发，仅在打开对应功能界面时请求服务器获取。
    *   **独立性**：通常不被其他模块引用。

### 5.2 状态变更与物品同步
当发生业务操作（如消耗碎片合成宠物）时，数据同步遵循以下模式：

#### 1. 资源扣除（通用处理）
*   **机制**：服务器执行扣除逻辑后，会主动下发通用的“资源/道具变化协议”。
*   **客户端处理**：监听通用协议并更新本地缓存。
*   **协议设计**：具体的业务响应协议（如 `PetCompositeResponse`）**不需要**包含扣除的道具信息。

#### 2. 资源获得（显式携带）
*   **机制**：新获得的物品（特别是如宠物实例、装备实例等包含服务端生成属性的对象），无法由客户端简单构造。
*   **协议设计**：必须在业务响应协议中显式包含获得的物品数据。
*   **示例**：
    ```protobuf
    // 领取任务奖励请求
    message QuestReceiveRequest_20000004 {
        repeated int32 ids = 1; // 任务ID列表
    }

    // 领取任务奖励响应
    message QuestReceiveResponse_20000005 {
        repeated RewardInfo rewards = 1; // 必须明确返回获得的奖励详情
    }
    ```

***
