# Protocol Definition Rules (Proto3)

## 1. 基础规范
- **语法**: 必须使用 `syntax = "proto3";`。
- **包名**: 统一使用 `package cn.game.protocol.protobuf;`。
- **Java配置**:
  - `option java_package = "cn.game.protocol.protobuf";`
  - `option java_outer_classname = "XxxMsg";` (如 `PetMsg`)

## 2. 消息命名规范
格式: `[ModuleName][Action][Type]_[MsgID]`
示例: `PetCompositeRequest_19000001`

- **ModuleName**: 模块名，首字母大写 (如 `Pet`, `Shop`)。
- **Action**: 操作动作 (如 `Composite`, `Buy`)。
- **Type**: 消息类型。
  - `Request`: 客户端请求
  - `Response`: 服务器响应
  - `Push`: 服务器主动推送
- **MsgID**: 全局唯一的协议 ID。

## 3. 协议 ID 规则 (16进制 -> 10进制)
结构: `[High 8 bit: ModuleID] [Low 24 bit: MessageID]`

- **Module ID**: 每个功能模块分配唯一的 ID (如 Pet=19)。
- **Message ID**: 模块内递增。
- **配对规则**: `Response ID` = `Request ID + 1`。
  - `Request`: `19000001`
  - `Response`: `19000002`
- **Push ID**: 使用独立的 ID 段，避免与 Request/Response 冲突。

## 4. 代码生成注解 (Annotations)
在 `.proto` 文件头部必须包含以下注释，用于指导代码生成器：

```protobuf
//@HandlerPackage cn.game.games.net.game.module.pet
//@ClientHandlerPackage cn.game.simulation.client.handler
//@Function Pet
//@MessageModule 19

package cn.game.protocol.protobuf;
```

- `HandlerPackage`: 生成的服务端 Handler 路径。
- `MessageModule`: 模块 ID (整数)。

## 5. 数据同步策略
- **PlayerAllInfo**: 包含核心高频数据，登录时全量下发。
- **资源变动**:
  - **隐式同步**: 资源扣除不需要在 Response 中返回，服务器会自动下发资源更新 Push。
  - **显式同步**: **获得**新物品（特别是带属性的实例物品）**必须**在 Response 中显式返回 `RewardInfo`。
