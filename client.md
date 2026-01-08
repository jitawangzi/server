
---

# 模拟客户端工程使用指南

本文档旨在指导开发者如何使用模拟客户端工程（Simulation Client）进行游戏服务器的功能测试与压力测试。通过模拟客户端，我们可以启动虚拟玩家，发送指定协议与服务器交互，并处理服务器的返回数据。

> **前置阅读**：在使用本工程前，请确保已阅读并理解 [protocol.md](protocol.md)，以掌握协议定义的基础知识。

## 1. 核心机制与代码生成

工程的核心依赖于 `PbProtocolGenerator` 工具。当我们在 `.proto` 文件中定义好协议后，执行该生成器，会在服务端和客户端工程中分别生成对应的 Java 类。

### 1.1 协议定义示例
以 `PetMsg.proto` 为例，定义碎片合成宠物的请求与响应：

```protobuf
// 碎片合成宠物请求
message PetCompositeRequest_19000001 {
  int32 id = 1; // Pet表id
}

// 碎片合成宠物响应
message PetCompositeResponse_19000002 {
}
```

### 1.2 服务端 Handler (自动生成)
生成器会在服务端生成 `PetHandler`，用于处理来自客户端的请求：

```java
@Component
public class PetHandler extends GameBaseHandler {
    @Override
    protected int getModule() { return 0x19; }

    @Override
    protected void inititialize() {
        // 绑定请求协议与处理方法
        putInvoker(PbProtocol.PetCompositeRequest_19000001, this::composite);
    }

    private void composite(NetClient client, Object message) {
        PetCompositeRequest_19000001 req = (PetCompositeRequest_19000001) message;
        // ... 业务逻辑 ...
        
        // 发送响应给客户端
        PetCompositeResponse_19000002 response = PetCompositeResponse_19000002.getDefaultInstance();
        client.sendProtocol(response);
    }
}
```

### 1.3 客户端 Handler (自动生成)
同时，在模拟客户端工程中，会生成 `[Client]PetHandler`。**这是我们需要关注的重点**，它定义了模拟客户端收到服务器响应后的处理逻辑。

```java
@Component
public class ClientPetHandler extends BaseHandler {
    @Override
    protected int getModule() { return 0x19; }

    @Override
    protected void inititialize() {
        // 绑定响应协议与处理方法
        putInvoker(PbProtocol.PetCompositeResponse_19000002, this::composite);
    }

    private void composite(NetClient netClient, Object message) {
        PetCompositeResponse_19000002 resp = (PetCompositeResponse_19000002) message;
        
        // 强转为模拟客户端对象，用于访问客户端状态
        Client client = (Client) netClient;
        
        // TODO: 在此处编写收到服务器返回后的逻辑，例如更新本地数据
    }
}
```

---

## 2. Client 对象详解

在客户端 Handler 中，`Client client = (Client) netClient;` 是关键。`Client` 类代表了一个虚拟玩家对象，它不仅负责发送协议，还维护着该玩家的游戏数据状态。

### 2.1 核心字段
以下是 `Client` 类中维护的常用数据，用于支持后续的逻辑判断和协议构造：

| 字段名 | 类型 | 描述 |
| :--- | :--- | :--- |
| `playerAllInfo` | `PlayerAllInfo` | **玩家全量数据**。对应 `PlayerMsg.proto`，通常在登录后返回并保存。 |
| `friendsList` | `List<FriendInfo>` | 好友列表。 |
| `blackList` | `List<String>` | 黑名单列表。 |
| `shopItemMap` | `Map<Integer, List<ShopItemProto>>` | **商店数据缓存**。Key: 商店ID, Value: 商品列表。 |
| `guildMember` | `GuildMemberInfo` | 公会成员数据。 |
| `mailsList` | `List<MailInfo>` | 邮件列表。 |
| `dataMap` | `Map<String, Object>` | **通用数据容器**。可自定义 Key（如协议名），用于灵活保存临时数据。 |
| `lastSendMessage` | `Message` | **最后一次客户端向服务器请求的消息**。这个特别重要，用来记录客户端最后一次向服务器发送的协议。客户端收到服务器的返回协议时，可以知道当时发送的请求，具体都发送了哪些数据。 |

### 2.2 数据流转示例（商店系统）
为了让模拟客户端能够执行复杂的交互（如“先获取列表，再购买商品”），我们需要在 Handler 中保存服务器返回的数据。

**Proto 定义 (`ShopMsg.proto`)：**
```protobuf
// 查看商店列表
message ShopItemListRequest_15000001 { uint32 shopId = 1; }
message ShopItemListResponse_15000002 { repeated ShopItemProto items = 1; }

// 购买商品
message ShopItemBuyRequest_15000003 { 
    uint32 shopId = 1; 
    uint32 itemId = 2; 
    uint32 count = 3; 
}
// 购买商品返回
message ShopItemBuyResponse_15000004{
  repeated RewardInfo rewards = 1;  // 获得的具体物品
}

```

**客户端逻辑 (`ClientShopHandler`)：**
当收到商品列表返回时，将其保存到 `client.shopItemMap` 中，以便后续测试类使用这些数据进行“购买”操作。

```java
@Component
public class ClientShopHandler extends GameBaseHandler {
    // ...省略部分代码...

    private void itemList(NetClient netClient, Object message) {
        ShopItemListResponse_15000002 resp = (ShopItemListResponse_15000002) message;
        List<ShopItemProto> itemsList = resp.getItemsList();
        Client client = (Client) netClient;

        // 如果商品列表不为空
        if (!itemsList.isEmpty()) {
            int shopId = ((ShopItemListRequest_15000001) client.lastSendMessage).getShopId();
            // 将数据缓存到 Client 对象中
            client.shopItemMap.put(shopId, itemsList);
        }
    }
}
```

---

## 3. 编写测试与压测用例 (xxxTest 类)

对于每一个请求协议（Request），生成器会自动生成对应的 `xxxTest` 类（例如 `ShopItemBuyRequest_15000003Test`）。我们需要在此类中填充代码，构建发送给服务器的消息体。

该类主要包含两个核心方法，分别对应不同的使用场景。

### 3.1 `getMessage(Client client)`
*   **用途**：**功能自测 / 本地调试**。
*   **特点**：直接运行该 Test 类的 `main` 方法时调用。
*   **实现建议**：通常使用硬编码（Hardcode）数据，或者从 `client` 对象中获取简单的上下文数据。此时一般不加载服务器配置表。

```java
@Override
public Message getMessage(Client client) {
    var builder = ShopItemBuyRequest_15000003.newBuilder();
    
    // 自测时，明确指定购买商店 ID 21 中的商品 2101
    builder.setShopId(21);
    builder.setItemId(2101);
    builder.setCount(55);

    return builder.build();
}
```

### 3.2 `getMessagePressure(Client client)`
*   **用途**：**压力测试 / 自动化机器人**。
*   **特点**：在压测框架运行时调用。可以使用服务器配置表数据。
*   **实现建议**：
    1.  **逻辑闭环**：结合 `Client` 中缓存的数据（如 `shopItemMap`）和配置表，模拟真实的玩家行为。
    2.  **避免报错**：必须构造合法的请求，避免因数据错误导致服务器抛出 Error，干扰压测结果。
    3.  **支持空转**：如果当前条件不满足（例如还没有获取到商店列表），应返回 `null`，此时客户端不会发送任何请求。

```java
@Override
public Message getMessagePressure(Client client) {
    var builder = ShopItemBuyRequest_15000003.newBuilder();

    // 1. 检查是否有缓存的商店数据 (前提是之前已发送过 ShopItemListRequest)
    Map<Integer, List<ShopItemProto>> shopItemMap = client.shopItemMap; 
    if (shopItemMap.isEmpty()) {
        return null; // 数据不足，不发送请求
    }

    // 2. 随机选择一个已加载的商店
    Integer randomShopId = Rnd.randomElement(shopItemMap.keySet()); 
    List<ShopItemProto> items = shopItemMap.get(randomShopId); 
    
    // 3. 随机选择该商店内的一个商品
    if (items == null || items.isEmpty()) {
        return null;
    }
    ShopItemProto randomItem = Rnd.randomElement(items);

    // 4. 构建请求
    builder.setShopId(randomShopId);
    builder.setItemId(randomItem.getItemId()); 
    builder.setCount(1); // 购买1个
    
    return builder.build();
}
```

### 3.3 运行方式

每个生成的 Test 类都包含一个 `main` 方法，可直接右键运行以启动单次测试：

```java
public static void main(String args[]) throws Exception {
    ShopItemBuyRequest_15000003Test instance = new ShopItemBuyRequest_15000003Test();
    instance.start(); // 启动客户端并发送 getMessage() 构造的协议
}
```

---

## 其他
1.  客户端每次只能向服务器发送一个请求，在这个请求没有回应之前，不会向服务器发送新的请求。 
2.  客户端初始化时，会读取env.properties文件，初始化相关配置，需要关注的是 
# 不设置用户名时，每次都重新注册。
user.name=19952
当 user.name 设置了值时，则每次客户端登录时，都会使用同一个账号登录，如果不设置user.name时，则每次都使用新的账号登录。 
如果测试有先后依赖关系的协议时，一般需要设置一个固定的账号。 

## 总结

1.  **定义协议**：在 `.proto` 文件中定义。
2.  **生成代码**：运行 `PbProtocolGenerator` 生成 Handler 和 Test 类。
3.  **处理响应（可选）**：如果后续请求依赖前置数据，需在 `ClientxxxHandler` 中将服务器返回的数据保存到 `Client` 对象中。
4.  **编写测试**：
    *   **自测**：修改 `xxxTest` 类的 `getMessage` 方法，使用指定数据。
    *   **压测**：修改 `xxxTest` 类的 `getMessagePressure` 方法，结合 `Client` 缓存数据与配置表，编写健壮的随机逻辑。