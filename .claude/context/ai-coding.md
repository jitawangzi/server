# ai-coding.md

> **面向对象**：在本项目中协助编写/修改游戏业务功能的 AI 助手
> **核心目标**：在不深入底层框架细节的情况下，**安全且一致**地补全 `Handler` 方法体与 `Module` 逻辑，严格遵守线程模型、持久化机制与错误处理约定。
> **适用范围**：`core/`, `game/`, `login/`, `protocol/`, `simulationclient/`, `util/`

---

## 1. 总体架构与开发入口

### 1.1 仓库模块职责

**`game/`** 是主游戏服（核心业务），包含 `*Handler`（协议入口）、`*Module`（业务逻辑）、配置管理和 DB 访问。**`protocol/`** 存放 Protobuf 协议定义与生成产物。**`core/` 与 `util/`** 提供通用基础能力，如 `AsyncUtils`、`DateUtil`、`Rnd` 等。**`simulationclient/`** 用于压测和回归测试。

### 1.2 业务开发模式

**Handler（协议层）** 继承 `GameBaseHandler`，使用 `@Component` 注解标记为单例。AI 的任务是补全 `initialize()` 中的协议注册，并实现处理方法（Invoker）。Handler 的职责是解析参数、获取 Player、校验、调用 Module/Helper、返回 Response。

**Module（逻辑层）** 由 `Player` 持有，职责是维护玩家状态、状态机流转、事件处理。大部分 Module 数据由框架定时全量持久化，内存修改即视为保存。

**Helper（逻辑层）** 这个是可选的，封装一些模块级别的静态方法，方便跨模块调用。 例如 `PlayerHelper`（资源增删、消息推送） `TestHelper`（测试代码，可以用在gm指令中）

**Manager（工具层,工具生成）** 包括 `xxxManager`（静态配置读取，单例）。

---

## 2. 线程模型与并发契约（⭐ 最高优先级）

### 2.1 核心模型：ID 分队列 + JDK21 虚拟线程

框架按 `objectId`（通常是 `playerId`）将任务分配到固定队列，每个队列由 JDK21 虚拟线程串行消费。这意味着对同一玩家的代码是串行的，无需加锁，可直接修改 Player/Module 字段。但 Handler/Manager 是全局单例，必须保证线程安全（只读或使用并发容器）。

### 2.2 跨线程回投

当代码运行在非玩家队列（如 HTTP 回调、Quartz 线程）时，严禁直接修改玩家数据，必须回投到玩家队列：

    ServerContext.getInstance().getProcessor().process(playerId, () -> {
        // 此时已在玩家虚拟线程中，可安全读写 Player/Module
        return null;
    });

### 2.3 异步编程：同步化写法

由于运行在虚拟线程中，禁止使用 `CompletableFuture` 回调链（如 `.thenAccept`）直接修改玩家数据。统一使用 `AsyncUtils.await(...)` 将异步转同步：

    RFuture<Long> future = redissonClient.getAtomicLong("key").incrementAndGetAsync();
    long value = AsyncUtils.await(future); // 虚拟线程挂起，不阻塞物理线程
    // 继续执行业务逻辑...

### 2.4 定时任务

禁止直接使用 Quartz 或 ScheduledExecutorService 修改玩家数据。必须使用 `Player#setTimerTask(...)`（一次性）或 `Player#setPeriodicTask(...)`（周期性），这些 API 保证回调在玩家队列中执行。

### 2.5 默认业务逻辑线程

通常处理玩家请求时，也就是Handler类的逻辑入口，已经让玩家处在正常的队列中了，默认就是单线程并且线程安全的，通常不需要考虑并发模型。 

### 2.6 禁止手动初始化线程
普通业务中，禁止直接声明新的线程或者线程池，应该使用已有的线程模型。 

---

## 3. 资源与道具操作规范（PlayerHelper）

### 3.1 增加资源

使用 `PlayerHelper.addResources` 增加资源并处理相关逻辑（如任务触发）。该方法返回 `List<RewardInfo>`（Protobuf 类型），必须将其放入 Response 返回给客户端用于展示。参数需指定 `OpType`（操作类型枚举）和 `notify`（是否推送）。

    List<RewardInfo> rewards = PlayerHelper.addResources(player, itemId, count, OpType.GM, true);
    respBuilder.addAllRewards(rewards);

### 3.2 扣除资源

使用 `PlayerHelper.delResources` 扣除资源。如果资源不足，方法内部会直接抛出异常。编码时不需要在外部判断 `if (count < need)`，也不需要 `try-catch`（除非有特殊逻辑），让异常冒泡中断流程即可：

    // 直接调用，若不足会自动抛错并返回错误码给客户端
    PlayerHelper.delResources(player, costId, costNum, OpType.LevelUp);

### 3.3 检查资源

如果仅需检查而不扣除，使用 `PlayerHelper.isEnough(...)`。

---

## 4. 协议 Handler 编写模板

    private void handleProcess(NetClient client, Object message) {
        XxxRequest req = (XxxRequest) message;  // ---- 会由代码生成器生成
	// 声明回包   ---- 会由代码生成器生成
        XxxResponse.Builder resp = XxxResponse.newBuilder();
        
        // 1. 获取玩家
        Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());// ---- 会由代码生成器生成
        
        // 2. 基础校验（参数合法性、前置条件(如果有的化)）
        // 方式A：业务逻辑错误，推荐用 fail 抛出，由框架统一处理,会返回一个通用的错误协议给客户端
        if (req.getCount() <= 0) {
            player.fail(ErrorMsgEnum.param_error);
        }
        // 方式B：如果协议约定需要回包带错误码
        // client.sendProtocol(resp, ErrorMsgEnum.param_error.getId()); return;

        // 3. 获取模块（两种方式均可，优先使用 getModule）
         PetModule module = player.getModule(PetModule.class);
	// 或者  PetModule module = player.getPetModule();  如果方法存在
        // 4. 核心逻辑（调用 Module）
        module.upgradePet(req.getPetId());

        // 设置返回值...
	resp.setXXX

	// 返回客户端返回包
        client.sendProtocol(resp.build());
    }

---

## 5. 错误处理约定

**`player.fail(ErrorMsgEnum.xxx)`** 是推荐的默认方式，它会抛出 `LogicException`，中断流程，框架自动捕获并发送错误码给客户端。

**`client.sendProtocol(resp, errorId)`** 仅当客户端协议明确要求"即使失败也要收到完整 Response 包（含错误码字段）"时使用。

---

## 6. 模块（Module）与持久化

通过 `player.getModule(XxxModule.class)` 或 `player.getXxxModule()` 获取数据。默认情况下无需手动 `save()` 或 `update()`，框架会定时全量保存 Module 数据。例外情况是当 Module 声明了 `alwaysStoreDataInStandaloneTable()`（如邮件系统），则需通过Mybatis-Generator生成的java类（实现了DbEntity接口）， 使用DbEntity接口的`insert/update`等方法进行数据库操作。

事件监听通过 `getEventTypes()` 方法注册需要监听的事件类型。  

通过`player.fireAndHandleEvent(EventTypeEnum eventType, Object... params)` 发起事件并处理，事件处理仍在当前线程中，是线程安全的。

---

## 7. 消息推送

非响应式推送（即 Server 主动通知 Client）使用 `PlayerHelper.sendProtocol(long playerId, Object message)`，内部会自动查找 `GameClient` 并发送。

---

## 8. 静态数据配置表

### 8.1 配置表结构

Excel 静态数据配置表定义功能的静态数据，每行代表一条记录。以任务模块为例，`Quest.xlsx` 定义了任务结构，工具会将其解析为 `QuestConfig` 类：

    public class QuestConfig {
        public final int ID;              // 任务ID
        public final int Type;            // 任务类型，对应 QuestTypeEnum
        public final int Condition;       // 任务完成条件，对应 Condition 表ID
        public final int Reward;          // 任务奖励ID，掉落id
        public final int Group;           // 分组，不同类型的任务分组不能相同
        public final int[] OpenQuests;    // 完成时开启的新任务id
        public final boolean IsDeleteOnFinish; // 完成时是否删除
        // ...
    }

### 8.2 配置读取方式

每个配置表都有对应的 Manager 类用于数据读取。第一列始终是数字类型的 ID，用于按 ID 获取数据。

    // 按 ID 获取，不存在时抛出异常
    QuestConfig config = QuestManager.instance().get(id);

    // 按 ID 获取，允许返回 null
    QuestConfig config = QuestManager.instance().getNullable(id);

    // 获取所有数据
    Collection<QuestConfig> allQuests = QuestManager.instance().list();

    // 按索引字段获取列表（如果配置了索引）
    List<QuestConfig> dailyQuests = QuestManager.instance().getTypeList(QuestTypeEnum.Daily.getId());

    // 获取原始索引 Map
    Map<Integer, List<QuestConfig>> typeMap = QuestManager.instance().getTypes();

这些 Manager 类和方法由工具生成，不应手动修改。

### 8.3 特殊配置类

**常量表** 用于存储全局常量：

    public class GlobalConst extends ResourceListener {
        public volatile static int UpHeroID;          // 强制下阵霸波奔
        public volatile static int[] CreateUID;       // UID创建取值参数
        public volatile static int PayVIPExp;         // 充值1人民币兑换的VIP经验
        public volatile static int[][] PlayerName;    // 修改名字花费
        // ...
    }

**枚举类型表** 用于定义枚举常量：

    public enum QuestTypeEnum {
        Daily(1, "Daily", "日常任务"),
        Weekly(2, "Weekly", "周常任务"),
        Achievement(3, "Achievement", "成就任务"),
        // ...
    }

这些类在 AI 编写代码前会由人工生成并提供。

---
## 9. 事件系统

游戏事件系统主要分为两个层级：系统级事件与玩家级事件。实际开发中，绝大多数业务逻辑围绕**玩家事件**展开。

### 9.1 事件分类

* **系统级事件 (`ServerEvent`)**
    * **作用范围**：全局，影响整个服务器系统。
    * **典型场景**：修改系统时间、服务器状态变更等。
* **玩家级事件 (`PlayerEvent`)**
    * **作用范围**：仅在当前玩家对象范围内触发和处理。
    * **定义位置**：所有类型均在 `EventTypeEnum` 枚举中预先定义。如有新需求，需手动在该枚举中添加。

---

### 9.2 常用玩家事件详解

以下是 `EventTypeEnum` 中常用的事件类型分类：

#### A. 玩家生命周期事件

| 事件枚举 | 说明 | 备注 |
| :--- | :--- | :--- |
| **`PLAYER_INIT`** | 创建新玩家 | 优先级高，用于初始化核心数据 |
| **`PLAYER_CREATE`** | 创建新玩家 | 常规创建流程 |
| **`LoginFinish`** | **登录完成** | **最常用**，一般数据加载完毕后处理逻辑放在此处 |

#### B. 时间触发事件
用于处理定时刷新、周期性重置的逻辑。

* **`NewDay`**：午夜 0:00 跨天。
* **`NewWeek`**：跨周事件。
* **`NewMonth`**：跨月事件。

#### C. 业务行为事件
当玩家进行特定游戏行为时触发。

* **`Charge`**：充值。（参数：充值金额 RMB）
* **`FuncOpen`**：功能开启。
* **`GetItem`**：获得资源、道具等。（参数，id，value）
* **`LevelUp`**：角色升级。（参数：经验值、等级）
* **`Level`**：通关关卡。（参数：关卡 ID、回合数、剩余人数）

---

### 9.3 开发使用指南

#### 1. 定义事件
在 `EventTypeEnum` 中添加新事件时，**必须**添加详细注释，明确参数规范。

> **注释规范**：明确说明参数的数量、顺序以及每个参数的物理含义。

#### 2. 发起事件 (Fire)
根据业务场景，传入对应的事件类型及参数。

```java
// 方法签名
player.fireAndHandleEvent(EventTypeEnum eventType, Object... params);
```
// 调用示例
// 假设 GetItem 事件定义为：参数0=道具id, 参数1=道具数量
player.fireAndHandleEvent(EventTypeEnum.GetItem, id, count);

#### 3. 监听事件 (Fire)
在功能模块中实现 handleEvent 方法，通过 switch-case 结构处理特定事件。
```
@Override
public void handleEvent(PlayerEvent event) {
    switch (event.getType()) {
        case GetItem: {
            // 根据定义好的顺序获取参数
            int itemId = event.getParameter(0);
            int itemCount = event.getParameter(1);
            
            // TODO: 执行获得物品后的业务逻辑
            break;
        }
        case LevelUp: {
            // 处理升级逻辑
            break;
        }
        default:
            break;
    }
}

```

---

## 10. 工具类使用规范

### 10.1 时间相关

禁止直接使用 `System.currentTimeMillis()`，统一使用 `cn.game.util.DateUtil` 中的方法：

    DateUtil.currentTimeSeconds()  // 当前时间戳（秒）
    DateUtil.currentTimeMillis()   // 当前时间戳（毫秒）
    // 其他时间相关便捷方法也在 DateUtil 中

### 10.2 随机相关

随机相关方法统一使用 `cn.game.util.Rnd` 类。

### 10.3 日志规范

严禁使用 `System.out.println` 或 `e.printStackTrace()`。Module 和 Handler 基类已声明：

    protected transient Logger log = LoggerFactory.getLogger(this.getClass());

直接使用 `log.info`、`log.error`、`log.debug` 即可。如果某些类没有声明 Logger，可以手动补全。日志级别规范：业务流程用 INFO，错误用 ERROR，调试用 DEBUG。记录日志时需要包含 playerId。

---

## 11. AI 编码自检清单

在生成代码前，请检查以下要点：

- **线程安全**：是否在 Handler/Module 中直接 `new Thread`？（禁止）
- **异步处理**：是否使用了 `AsyncUtils.await` 而不是回调链？（必须）
- **资源扣除**：是否依赖 `delResources` 的自动异常抛出机制？（推荐）
- **回包**：成功路径是否调用了 `client.sendProtocol`？
- **OpType**：涉及资源变动时，是否使用了正确的 `OpType` 枚举？
- **时间获取**：是否使用 `DateUtil` 而非 `System.currentTimeMillis()`？
- **日志输出**：是否使用 `log` 而非 `System.out.println`？

---

## 12. 暂不涉及的内容

全局/社交系统处理（Global/Guild/Rank）暂时较少，先由人工开发。其并发模型与玩家系统相同，都是通过 ID 分配到不同队列实现串行化。