# AI.md

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

**Helper / Manager（工具层,工具生成）** 包括 `PlayerHelper`（资源增删、消息推送）和 `xxxManager`（静态配置读取，通常为单例）。

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

如果仅需检查而不扣除，或不希望抛出异常，使用 `PlayerHelper.isEnough(...)`。

---

## 4. 协议 Handler 编写模板

    private void handleProcess(NetClient client, Object message) {
        XxxRequest req = (XxxRequest) message;  // ---- 会由代码生成器生成
	// 声明回包   ---- 会由代码生成器生成
        XxxResponse.Builder resp = XxxResponse.newBuilder();
        
        // 1. 获取玩家
        Player player = PlayerManager.getInstance().getPlayer(client.getPlayerId());// ---- 会由代码生成器生成
        
        // 2. 基础校验（参数、前置条件）
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

通过 `player.getModule(XxxModule.class)` 或 `player.getXxxModule()` 获取数据。默认情况下无需手动 `save()` 或 `update()`，框架会定时全量保存 Module 数据。例外情况是当 Module 声明了 `alwaysStoreDataInStandaloneTable()`（如邮件系统），则需通过生成的 Mapper/DAO 进行 `insert/update`。

事件监听通过实现 `getEventTypes()` 和 `handleEvent(PlayerEvent event)` 完成。事件处理仍在玩家线程中，是同步安全的。

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

事件类型在 `EventTypeEnum` 中预先定义，新需求可能需要手动添加。常用事件包括：

**玩家生命周期事件**：`PLAYER_INIT`（创建新玩家，优先级高）、`PLAYER_CREATE`（创建新玩家）、`LoginStart`（开始登录）、`LoginFinish`（登录完成，一般用这个）、`Relogin`（客户端重新登录，内存数据还在）、`Reconnect`（重连，通常不需要处理）、`LoginSuccess`（任何登录都会触发，不推荐使用）。

**时间事件**：`NewDay5`（早5点跨天）、`NewDay`（晚12点跨天）、`NewWeek`（跨周）、`NewMonth`（跨月）。

**业务事件**：`Charge`（充值，参数为充值数量 RMB）、`FuncOpen`（功能开启）、`ResourceAdd`（新增资源）、`ResourceRemove`（移除资源）、`LevelUp`（升级，参数为经验和等级）、`Level`（通关关卡，参数为关卡id、回合数、剩余人数）等。

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