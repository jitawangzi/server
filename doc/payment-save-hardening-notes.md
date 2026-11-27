# 玩家保存、支付加固与请求计时专项技术档案（S2B～S4）

> **定位**：本档案承接从 [01_系统架构设计全景文档.md](01_系统架构设计全景文档.md) 与 [02_开发流程与研发规范手册.md](02_开发流程与研发规范手册.md) 拆出的 S2B～S4 专项加固批次细则（保存协调器、五分钟写保护、支付履约与同库持久去重、离线证据/封存/清理预检/启用预检/历史盘点 CLI、数据库超时边界及有界请求计时基线）。
> **阅读方式**：日常了解架构全景与研发流程直接读 `doc/01` 与 `doc/02` 精简摘要；仅在排查或修改保存协调器、支付履约、离线运维 CLI 或请求计时指标时查阅本档案。跨进程强杀恢复契约另见 [payment-process-recovery.md](payment-process-recovery.md)。

## 目录

- [一、架构、保存协调、支付契约与计时细则](#一架构保存协调支付契约与计时细则摘自原-doc01)
  - [9.2.1 S2B：同玩家保存保序与结果不明](#921-s2b同玩家保存保序与结果不明)
  - [9.2.2 S2C：五分钟风险预算与写入保护](#922-s2c五分钟风险预算与写入保护)
  - [9.2.3 S3A：可支付订单发布门槛](#923-s3a可支付订单发布门槛)
  - [9.2.4 S3A：发货实例归属与离线收尾](#924-s3a发货实例归属与离线收尾)
  - [9.2.5 S3A：充值奖励完成契约](#925-s3a充值奖励完成契约)
  - [9.2.6 S3A：重复通知与单调回执](#926-s3a重复通知与单调回执)
  - [S3B：证据保留与有界离线核对](#s3b证据保留与有界离线核对)
  - [S3C：订单容量观测](#s3c订单容量观测s3c-payment-capacity-monitoringt2)
  - [S3C：单订单可信证据封存](#s3c单订单可信证据封存s3c-payment-trusted-archivet2)
  - [S3D：Game 同库持久履约去重](#s3dgame-同库持久履约去重s3d-payment-durable-dedupt2)
  - [S3D：逐单只读安全清理预检](#s3d逐单只读安全清理预检s3d-payment-safe-cleanup-gatet2)
  - [S3E：持久去重启用结构预检](#s3e持久去重启用结构预检s3e-payment-enable-preflightt2)
  - [Task4：数据库等待与未知结果边界](#task4数据库等待与未知结果边界s3e-payment-database-timeout-boundariest2)
  - [Task5：有界只读历史订单盘点](#task5有界只读历史订单盘点s3e-payment-legacy-inventoryt2)
  - [S4：有界请求计时与 10ms 基线](#s4有界请求计时与10ms基线)
- [二、专项运维命令、部署顺序与回归验收细则](#二专项运维命令部署顺序与回归验收细则摘自原-doc02)
  - [S2C 玩家变更与保存风险约束](#s2c-玩家变更与保存风险约束)
  - [S2 存储层真实数据库验收](#s2-存储层真实数据库验收)
  - [S3A 支付回复 / 推单 / 发货 / 奖励 / 重复通知验收](#s3a-前置支付结果消费与异步回复)
  - [S3B 证据保留与只读核对验收](#s3b-证据保留与只读核对验收)
  - [S3C 订单容量观测、修复与可信封存运维验收](#s3c-订单容量观测验收与独立复跑)
  - [S3D 持久去重部署、回滚与逐单清理预检](#s3d-持久去重部署回滚与验收)
  - [S3E 启用结构预检与隔离验收](#s3e-启用结构预检与隔离验收)
  - [Task4 数据库超时验收与操作限制](#task4-数据库超时验收与操作限制)
  - [Task5 历史订单只读盘点操作与验收](#task5历史订单只读盘点操作与验收)
  - [S4 请求计时诊断与可复现基线](#s4请求计时诊断与可复现基线)

---

## 一、架构、保存协调、支付契约与计时细则（摘自原 doc/01）

### 9.2.1 S2B：同玩家保存保序与结果不明

创建与单表周期/登出/充值/离线修改入口统一进入 `PlayerSaveCoordinator`。每玩家一个实际写入在途，最多 16 个后继快照 FIFO 排队，所有快照均不合并。每次请求具有独立代次、捕获时点和结果；队列满明确失败并保留已观察的未保存状态。默认等待 30 秒，等待超时只结束该等待视图，不取消写入、不释放写入权；已超时的排队请求仍执行。不同玩家可独立推进。

`PlayerSnapshotWriter` 在独立虚拟线程使用现有 `SqlSessionFactory` 打开 `SIMPLE`、非自动提交的专用会话，JDBC 工厂经 Mapper 与显式 `commit(true)` 确认后报告成功；SpringManaged 工厂先由同数据源 `DataSourceTransactionManager` 开启并绑定事务，必须等 `TransactionTemplate` 返回后才确认提交，完成通知不回投玩家邮箱。要求 JDBC 或 SpringManaged 事务工厂，不能加入外部事务；配置不符合时在写入前明确失败。零行或非一行更新须回滚成功后报告未写入。进入 Mapper 后的异常保守进入 UNKNOWN；关闭连接或请求回滚不能证明代理/数据库端不会晚提交。原独立表路径未改成跨表事务。

UNKNOWN 保留原请求和后继队列，拒绝新提交，不自动重投或放行。受信服务端恢复接口 `reconcile(coordinatorId, playerId, generation, resolution, evidence)` 原子校验本实例身份、当前 UNKNOWN 和代次；UNKNOWN 仅由实际写入器退出后的结果建立。运维必须先排除数据库及 ShardingSphere 后端旧命令/事务继续提交，再核对 COMMITTED/NOT_COMMITTED，并记录操作者、数据库目标、事件编号、终止证明及结果证明。该记录是外部受信裁决，不是程序自动证明；单次 SELECT、关闭客户端连接或非空说明字符串都不足以解除 UNKNOWN。不能可靠核对时继续保留，不提供强制恢复开关。核对不改写已返回的失败/超时；NOT_COMMITTED 不自动重投原请求，已排队的新快照可继续。

`status(playerId)` 返回确认代次、确认时间、快照覆盖时点、在途/UNKNOWN、排队数、等待超时与已观察脏状态。`earliestUnsavedChangeAt=null` 表示业务变更起点未知；`riskFirstObservedSnapshotAt` 是当前风险期间首次观察到快照的保守起点，部分确认后仍可指向已确认的早期快照，不能称当前最早未确认快照；无法推知部分确认后剩余业务变更起点时 `earliestUnsavedChangeAt` 恢复 null。`markDirty` 是后续完整业务埋点入口，本批未覆盖全部业务变更，不能据此证明严格五分钟风险上界。`PlayerManager.getDisconnectedSaveRisks()` 查询已断线但仍驻留的保存风险对象，不依赖在线连接列表。

PlayerManager 登记/删除与通道状态协调：旧对象驻留时拒绝覆盖；在途、排队、UNKNOWN 或已观察未保存状态时拒绝删除。异步清理携带原 Player 身份，防止旧回调删除新对象。健康删除后释放通道记录，实例内代次不复用。状态仅驻留本进程，不是持久化写入日志或跨节点接管凭证；进程故障/节点接管仍由后续 S4 完善。既有 logout 期间重连复用同一 Player 的连接/退出代次窗口尚未解决，不能把对象身份保护宣称为完整会话生命周期保护。压测直接建号、停服维护删档等不属于在线快照通道，禁止与业务保存并行运行。

S2B 单元验收使用真实保存装配、真实邮箱及专用事务写入器的受控会话替身，覆盖超时晚完成、UNKNOWN、核对代次、队列满、对象保留、零行更新和 commit 边界。未启动服务或连接真实数据库；线上代理事务行为、故障连接和冷加载仍需后续集成验收。

### 9.2.2 S2C：五分钟风险预算与写入保护

聚合单表模式的普通业务仍可先返回、后持久化。风险预算固定为 **300 秒**，独立巡检每秒运行：未确认变化达到 240 秒尝试新快照，270 秒告警，300 秒拒绝新的业务写作用域；UNKNOWN 立即保护。该策略提前留出 60 秒处理余量，不能把它等同于数据库故障、停机或长时间在途操作下仍绝不丢超过五分钟的硬保证。已开始的业务不会被强行中断；真实数据库冷读/故障验收及延迟负载验收另行执行。

`PlayerSaveCoordinator.Status` 区分捕获开始、实际提交确认、最近尝试，以及最早未覆盖变化、风险年龄、告警/保护、连续失败数与下次重试时间。纯注册只登记权威对象，不制造提交或风险；加载后的初始化hook按可能变更保守跟踪，即使最终没有实际修改也可能触发保存和保护。运行时以 DateUtil 墙钟为原点并用单调计时推进，避免校时回拨冻结风险。业务入口采用**保守变化作用域**，并非逐字段脏跟踪：可能写入的请求即使最后没有实际变更也可能触发保存；纯心跳豁免。不在每请求上序列化全部玩家数据。

已接入网络实际执行、两类登录/重连、加载后的业务初始化hook、玩家业务定时器、addTask、在线/离线 modifyPlayer、活动调度、驻留玩家 RPC，以及公会、下单、匹配等已识别的异步写回调。迟到回调须重新入玩家邮箱并验证原对象；进入作用域前检查预算。版本区段按捕获/已接受快照边界保留，确认 A 只删除 A 覆盖的变化；A 捕获后 B 变化或作用域仍活动时，不会误清脏。失败快照不再引用的区段合并，日志空间受队列容量约束。

独立保存巡检不依赖登录timer或连接，驻留断线玩家也参与；每玩家最多一个待派发自动任务，全局最多 128 个未结束自动任务。实际捕获在玩家邮箱执行，事务完成独立推进。临时且明确未写入的拒绝/建连错误最多重试 3 次，按 1/2/4 秒退避（等待期限仍是 S2B 的 30 秒）；配置、零影响行、捕获错误等停止自动重试，等待显式修复保存。等待超时不代表事务终止，不会启动第二笔在途写入；UNKNOWN 不自动重试，仍走 S2B 的受信核对。实际完成序号防止旧等待失败污染后来的提交成功。

写保护保留 Player 和未确认数据，保存/核对/跨服与GM退出保存通道仍可执行。一次性业务timer若在开始前因保护被拒绝，按原对象保留最多64个延期动作，恢复后重新进玩家邮箱执行；取消全部timer会取消未开始动作，已执行失败的业务不重放，超过容量明确报错。离线充值先关闭同步业务作用域，再捕获保存和按原对象清理，避免成功保存后人为制造新脏版本。保护只在风险已被确认完整覆盖、无在途/排队/UNKNOWN/活动作用域时解除；旧快照成功不单独解锁。独立表模式保留原定时保存，不以主表确认声明其跨表持久性。充值跨系统幂等、关停排空、跨节点接管仍属后续阶段。

可控时钟和真实网络分派/邮箱的回归见 `PlayerSaveRiskTest` 与 `save-risk-regression-expected.json`：预算边界、冷加载、部分确认、作用域跨快照、连续失败、重试上限、迟到超时、UNKNOWN、断线维护和拒绝回包。事务使用受控替身；**未连接实际数据库，不作为 S2-C3 冷读取证**。

### 9.2.3 S3A：可支付订单发布门槛

现金充值在代金券与测试免支付分支之后进入 `PaymentOrderPublisher`。仅聚合表模式允许向 Login 创单；捕获原玩家、连接、会话、请求序号及复制后的参数，RPC 返回后重新进入玩家邮箱。先拒绝无效或重复订单，再在写保护作用域内加入 `PlayerModule.payItems`，关闭作用域后调用统一保存通道；只有本次保存返回玩家提交回执才注册临时付款回调并尝试发送可支付订单。

保存失败、超时或 UNKNOWN 不推单；保留已经加入的关联，不自动重放创单或推单。超时后的迟到提交不能改变已返回的失败结果。同步发送拒绝或异常按订单及同一 Promise 精确移除本次回调，防止后续发货对已完成 false 的承诺重复完成。重复订单不覆盖原记录或回调。推单只表示网络写入被同步接受，不证明客户端收到；`Player.pay` 此时仍未完成，必须等待实际付款回调。

关联、保存捕获和发布前均核对权威对象、原连接、会话及退出状态。订单推送及失败响应固定原请求序号和缓存；其他旧购买消费者的错误响应尚未全部具备这一保证。`AllRecharge1` 下实际价格为一元，Login 请求、推送和持久关联的 SDK 商品 ID 统一为 `1007`，修正此前后两处仍使用原配置商品 ID 的不一致。

保存等待在该玩家邮箱内，SDK 发货任务只能随后执行；实际数据库确认来自独立线程，不依赖同一邮箱。该玩家后续请求可能等待约 30 秒，不能将本路径纳入已经达到 10ms 的承诺。保存前早到的发货、Login 已创单但 Game 关联尚未保存的崩溃窗口、完整奖励幂等和最终成功持久化仍待后续 S3A。

`PaymentOrderPublisherTest` 覆盖真实邮箱与保存协调器的时序、异常、身份及请求归属；实际 `Player.pay` 参数接线测试只替代外部事件总线。`PlayerSaveDatabaseIT.payableOrderPublicationColdReloadsAssociation` 经同一发布器、生产快照和 Writer，在 JDBC/SpringManaged 两种事务下推单后重建新 Player，核对完整关联和临时回调未落库。不等于真实支付平台或完整发货链路验收。

### 9.2.4 S3A：发货实例归属与离线收尾

`ServerHandler.ship` 保持异步离线加载；同一 Handler 内同玩家的并发发货共用正在初始化的加载 Future，不消费提前登记但尚未完成初始化的实例。加载内部需要回投玩家邮箱，因此不能在同一邮箱同步等待加载。`PlayerHelper` 将成功登记后的初始化失败包装为携带原实例的 `OfflinePlayerInitializationFailure`，保留原原因；登记前竞争失败保持原异常。本 Handler 只隔离失败中携带且仍驻留的原实例，不能按玩家 ID 误伤抢先登录的后继对象，也不能把删除在途记录理解为初始化成功；失败不直接清除对象。隔离采用弱实例键，旧实例安全释放后不延长其生命周期，新实例不继承该标记。隔离仅是本 Handler 的保守保护，不代替全局加载、重启或跨节点初始化状态管理。

实际发货任务在邮箱中重新核对权威 Player、活跃和退出状态，并根据当前在线状态选择原在线 callback 或离线 `offlinePay`。在线登录未完成时拒绝；离线加载沿用 `islogining=true` 的历史默认值，只有没有真实通道的临时连接可按离线对象处理。`online=false` 却已经换成真实连接属于矛盾状态，拒绝本次发货/保存确认，不改写全局在线标志。

业务作用域关闭后，在同一个玩家任务内等待统一保存通道，只接受属于该玩家的本次提交回执，确认前后均检查原实例。失败、等待超时、UNKNOWN 或身份失效只向本次 Login RPC 回失败，保留风险对象；不再给重连后的客户端发送旧发货通用错误。清理同样留在邮箱，只有仍属原实例、原临时连接、离线且未退出时才尝试安全删除。提交确认后的发送或清理异常独立记录，不能追加相反的第二次响应。独立表缺乏统一订单/奖励回执，进入业务前拒绝。

本批不把历史 `finish` 升级为可信发奖证明，重复 finish 订单仍按原契约拒绝。奖励与保存完成契约见下一节；十天日龄删除证据风险已由 S3B 保守保留策略阻断；可信归档/容量治理、孤儿订单恢复及跨节点接管仍待后续。保存可能占用同玩家邮箱约 30 秒，不代表达到 10ms。

`PaymentDeliveryLifecycleTest` 直接调用实际 ship/find/process/persist/cleanup，使用真实邮箱、callback、Charge 分派、离线 PayType 分支及保存协调器；仅替代加载/事务 IO 和最终活动奖励依赖。真实库用例 `offlineDeliveryCommitsBeforeReplyAndCleanup` 在 JDBC/SpringManaged 两种事务下拦住实际 commit，确认提交前零回复/零清理，再用新 Session、新 Player 核对订单及测试奖励标记。该标记验证保存生命周期，不等于所有商品奖励或真实平台发货已经验收。

### 9.2.5 S3A：充值奖励完成契约

依据任务 `s3a-payment-completion-contract`，现金支付把三个结果分开：`Player.pay` 的付款承诺、`PaymentCompletion` 的完整奖励执行、`PlayerSaveCoordinator.Receipt` 的玩家快照提交。订单发布后绑定的 Boolean Promise 不再直接触发发奖；`execPayCallback` 必须找到显式奖励步骤，并在原发货邮箱等待其实际结果。缺失完整绑定是明确失败，不能退回离线发奖。没有在线回调时才沿用对应 `PayType.offlinePay`。

本次必要步骤是下表的购买变更及其直接奖励、同步 `Charge` 分派（包括 VIP 经验与累充处理），以及这些调用显式返回的异步结果。任何新增异步奖励必须纳入奖励 Future，不能返回“已入队”作为完成；禁止等待需要同一玩家邮箱后继任务的 Future。月卡充值邮件直接复用原 Player 的 `MailModule.sendOnline`，等待实际插入 Future，成功后才推进领取时间；常规跨天邮件保持既有行为。资源变化/邮件新增推送仍是增量通知，不代表最终充值成功。

| 注册消费位置 | 必要购买及奖励步骤 | 离线关联 |
|---|---|---|
| ShopHandler 的 XianShiLiBao / fundPassBuy / recharge / buyShopItem / buyMonthCard / buyChapterPacks | 原模块购买状态、原配置奖励、商店次数及 BuyItems；月卡另等邮件插入 | 复用相同 PayType；ShopItem 新订单附 shopId、count，保持购买倍率 |
| ActivityHandler 的 singleChargeBuy / westLuckyBuy / dayGiftBuy / fundPassSignBuy | 首充解锁、西游礼包、每日单件/打包发奖、基金签到解锁及原响应内容 | 每日礼包复用 buy/packageBuy；基金签到记录 activityId；西游礼包使用 WestLucky(13)，避免误走 FirstCharge |
| ActivityJQB.asyncReceive | 原随机奖励与原 checkRefreshActivity，刷新移入奖励步骤 | 原 ActivityJQB 离线路径 |
| LimitedTimeGiftModule.buy | 原次数变化、礼包移除及奖励；离线免支付结果显式为 true | 实际等待 buy(id,false) 返回，不再拆箱 null |
| VipHandler.buyVipGift | 原已购列表及原 VIP 礼包随机奖励 | 原 VipGift 离线路径 |

发货在任何奖励前写 `completionState=EXECUTING`；全部奖励及同步 Charge 正常返回后才写 `REWARDS_COMPLETED` 和 `finish`。关闭变更作用域后进入同一保存协调器。只有本次提交回执确认且原实例仍有效，才完成在线购买结果并回复 Login 成功；业务回包统一捕获原连接/序号。离线补单没有原奖励响应对象，不能伪造在线购买结果，原临时承诺明确失败，Login 仍可在本次保存确认后成功。

奖励、Charge 同步异常或异步失败均保留 `REVIEW_REQUIRED`，不自动回滚部分资源、不 reset finish、不回退到其他发奖路径。历史状态非空或仅 finish 的重复通知仍拒绝；本批新凭证的去重规则见 9.2.6。失败状态纳入现有 PayItem JSON；本次不增加数据库。旧 JSON 缺省 null；缺少离线数量/活动定位参数的旧订单保守拒绝，不推测购买策略，也不将旧 FirstCharge 订单猜成西游礼包。保存失败、超时和 UNKNOWN 保留原对象及既有保存保护；晚提交不补发成功。失败后仅能核对实际资源、事件、邮件及保存代次再安排专门恢复，不提供本批自动解除 REVIEW_REQUIRED 的 API。

证明范围：真实发货入口、真实奖励绑定与玩家保存协调器的受控测试，三类实际购买 Handler 到发货的接线，以及月卡实际离线购买到邮件结果的等待。JSON 往返只证明字段兼容，不是冷读验收；月卡邮件 IO 在本轮测试中受控。本轮未连接真实数据库/充值平台，不证明跨重启恰好一次、全平台对账、跨节点接管、全项目任意异步事件或网络送达。进程在阻断状态保存前崩溃的恢复窗口仍属于后续孤儿订单治理，不能用内存状态宣称已解决。

### 9.2.6 S3A：重复通知与单调回执

本批依据用户明确要求实现任务 `s3a-payment-idempotency`（T2）。完整奖励、同步 Charge 与日志正常完成后，`PayItem` 创建 version=1 的 `completionProof`，绑定 playerId/orderId/sdkOrderId、金额、购买类型、商品和附加参数。凭证与奖励、finish 一同写入原玩家聚合快照，不另起一次保存。新生成的内存凭证由不序列化的等待位锁住；仅原保存回执确认且原权威实例仍有效后启用。失败、UNKNOWN、等待超时及迟到提交不解锁内存凭证、不重发奖励。可信数据库冷读得到的同版本、同身份、同参数凭证可以直接成功回复本次 Login RPC，不再执行奖励、Charge、邮件或保存。JSON 往返不是提交证据，只有现有受信存储加载链可提供冷读来源。

历史 finish、REWARDS_COMPLETED、无凭证或未知版本一律不能推断成功；REVIEW_REQUIRED 不解除。旧 JSON 缺省无凭证，仍可读取。基线字段模式回归实际验证旧 Jackson 拒绝 completionProof；本次属于不兼容持久格式更新，必须停止旧 Game 写入器、排空在途支付及保存后整体更新，不支持新旧 Game 混跑。新版本写入凭证后禁止直接回滚旧 JAR，故障应停写并前向修复；不自动剥离字段、reset finish 或重发奖励。该测试验证字段读取限制，不代表已执行真实集群部署/回滚。十天清理的证据删除风险由 S3B 保守保留策略阻断；跨节点写入与重启前部分副作用窗口仍未解决。SDK 单号冲突、玩家或购买参数变化均拒绝。

Login 的畅游、微信发货推送、iOS 回执和 GM 补单统一通过 `mergePaymentReceipt` 条件 SQL 合并。交付只允许 false → true；失败不更新为 false，也不整行回写旧订单。第三方单号/交易号冲突及玩家/金额不匹配不得更新，零行写入须读取并核对目标终态。成功必须来自 Game 提交结果，Login 合并完成后才响应平台。畅游取消提前 status=1；先检查玩家、账号、精确金额和已绑定订单号，验单返回 state=200/data=0 后才发货，Game 和 Login 确认后才执行平台订单更新。

上轮将内部 itemId 与 gameGoodsId 强制相等、固定数量为 1 并拒绝缺少 debug_text 的历史订单，缺少契约依据，已移除。仓内 ChangYouPaymentNotification 示例的 gameGoodsId 是 com.cy.gyxy.yuanbao1；Game 创单传入内部数字 SKU，不能假设两者相等。本批保留原 SDK 验单链、玩家/账号、精确金额和已绑定平台单号冲突校验；旧 Login 订单仍经 Game 核对，历史 Game finish 无凭证仍拒绝去重。

金额转换沿用原实现的元→分语义并改为精确比较；state=200/data=0、回调 status=1/0 均源自既有代码，不等于正式平台保证。SDK 的验单请求只含 receipt；pushInfo.gameOrderId 的 MD5 校验不含秘密，不能证明其与已验平台订单的可信绑定。仓内缺少正式商品映射/数量/金额单位、验单与本地订单绑定、更新正文成功码与重试契约，必须由平台资料明确；本批不放宽真实性校验，也不以猜测新增会误拒合法订单的限制，不宣称跨平台支付真实性或全链恰好一次已验收。orderUpdate 的 Future 成功只表明请求返回正文；未证明业务接收成功，更新失败不能回退本地交付。上述缺口需独立审核及业务决策，本批可独立交付的是本地持久凭证与 Login 状态单调性。

Login 真实库验收使用 PaymentOrderDatabaseIT 和独立清单，先通过 JDBC 元数据检查现有表结构，仅 INSERT 唯一随机主键专用订单，不创建业务账号；回执 callback 遵守真实 JSON 列类型。每次合并与冷读使用新会话，覆盖 8 路旧快照并发成功/失败、迟到失败、重复成功、两种 MySQL 影响行数配置、身份/金额/单号冲突及未知支付状态。SQL 与零行核对同时绑定 third_uid，只允许已知支付状态 1/2；未知状态继续待核对。精确条件 DELETE 并冷查确认夹具清理。

当前配置来自 Apollo：Game 使用 game/default/jdbc；run_login.bat 的 app.id=login 及 Login Spring jdbc namespace 支持 Login 使用 login/default/jdbc。game 配置下缺少订单表时不能造表或冒称验收；改用当前 Login 配置验证。仅测试进程临时私有 Properties 设置 S2_DB_CONFIG，正确转义并在 finally 删除；不修改 Apollo、不记录配置值。

本批测试覆盖真实 Game/畅游处理链和精确 fixture 的数据库冷读，实际执行结果与限制以本任务 development-report.json 为准。原请求连接/序号、同玩家邮箱、权威实例/重连保护和保存失败驻留机制继续使用既有路径。没有全局事务、自动对账或孤儿补偿，不启动生产服务或调用真实充值平台。

本轮关联修复任务 `s3a-payment-review-fixes`：GM 对非数字第三方单号直接走第三方查询，拒绝空请求、未知支付状态、无效订单/玩家/金额，同步及异步异常均结束原 HTTP。Login 的 PayOrder 没有 itemId 字段，不伪造商品约束；实际购买参数由 Game 的持久 PayItem 前置校验。四入口均以创单绑定的 thirdOrderId 作为 SDK 身份，null/blank 统一空串；微信 mchOrderNo/transactionId 不冒充 thirdOrderId，iOS prepayId 与 transactionId 不是同一种标识。

微信/iOS 验证原账号、金额、支付状态及已有流水后，先用条件 SQL 绑定支付事实（delivered=false），再请求 Game；SQL 同时检查 mch_order_no 冲突，避免并发不同流水先发奖后才拒绝。支付事实的提前记录不代表交付，Game 原快照提交和最终 Login 合并成功后才响应成功；失败不回退终态。iOS SDK 配置延迟到实际验签/创单时初始化，测试加载处理链不下载证书。

微信创单的 goodsPrice 原价写入本地 price，回调核对 GoodsInfo.OrigPrice 的分值；ActualPrice 必填且非负，允许优惠后实付低于原价。不乘 Quantity、不套用畅游元转分。单位依据[腾讯官方小游戏支付参数](https://intl.cloud.tencent.com/jp/document/product/1219/69511)，该文档是 SAS 同名字段契约；本项目当前 buyQuantity 固定 1；正式商品/优惠活动、非微信渠道缺省 WeChatPayInfo 的适用范围仍须正式平台资料验证，不宣称本轮已经真实平台验收。当前入口沿用已有 WeChatPayInfo 处理范围。

PayItem 与 CompletionProof 局部忽略未知 JSON 字段，未知凭证版本仍拒绝成功去重；全局 JSON 策略不变。此兼容仅作用于升级后的读取器，不修复已发行旧 JAR；前述停旧写入器、整体升级及禁止直接回滚约束继续适用。本轮真实 HTTP 接口代理测试调用 GM/微信 handle 与 iOS 完整生产处理链，仅替换验签、存储、RPC/审计边界，不调用外部支付平台。

### S3B：证据保留与有界离线核对

本批任务 `s3b-payment-evidence-retention`（T2）以人工明确需求为依据。实际清理入口 `PlayerModule.initFromDbAfter` 不再按 finish 和十天日龄删除 PayItem。当前无可信归档或平台终态证明，全部订单保守保留，包括普通未完成记录、历史 finish 无凭证、version=1 凭证、未知版本、EXECUTING、REVIEW_REQUIRED 与结果不明记录。历史 finish 不追认为成功，也不清理后重发。本批不改变持久 JSON 格式或数据库结构，继续遵守停旧写入器、排空后整体升级、新凭证写入后禁止直接回滚旧 JAR 的限制。

`PaymentEvidenceCli` 是显式本地运维入口，一次只接受一个正 playerId/orderId（最多十九位且必须在 long 范围内），不支持分页、列表扫描或任意 SQL。分别按 Game 玩家主键及 Login 订单主键读一行；模块 JSON 限 4 MiB，连接/socket/SQL 各限两秒，CLI 总等待限七秒。只用普通 JSON 树读取现有类型包装，不加载类、不构造 Player、不经过登录/初始化/加载 hook，因而不触发任务、邮件、奖励、保存或清理。未知字段可读取，未知版本、损坏 JSON、无记录、字段异常、查询错误/超时分别保守报告。

返回最小化分类：Game 关联、历史 finish、阶段、凭证版本/一致性；Login 支付/交付值、玩家/订单/金额（Game 元与 Login 分）和绑定平台单号是否匹配。金额/身份冲突不能忽略；不输出原始单号、凭证正文、receipt、完整玩家快照或账号。每次库读取起止时间可见，source 固定为 OFFLINE_STORAGE_NON_ATOMIC；在途及仅内存状态均 NOT_OBSERVABLE，事务终止和 UNKNOWN 解除均 NOT_PROVEN，canResend 固定 false。一致凭证只表示当前持久快照字段一致，不能证明跨库原子终态、在线健康或平台真实性。

当前已具备可查证据与清理保护；未实现自动补偿、孤儿重放、跨节点接管或全平台真实性对账。保留成本随每玩家订单数线性增长，每次全量快照与恢复均承担该成本，没有自动淘汰或硬数量限额；S3C 增加容量观测，CLI 的 4 MiB 限制只限制诊断，不限制保存。S3C 第一阶段补充下述可验证本地封存回执；外置持久去重索引、平台终态证明和安全删除仍未实现，原 `payment-evidence-capacity-and-archive` 待办整体保持 PENDING。

### S3C：订单容量观测（s3c-payment-capacity-monitoring，T2）

本任务仅实现容量监控。`PlayerDataSnapshot.capture` 在所属玩家串行队列内复用一次完整 modules 序列化结果，读取 `PlayerModule.retainedPaymentOrderCount()` 的 HashMap.size，并单次遍历字符串计算准确 UTF-8 字节数；不另行序列化、不扫描订单集合、不分配等大 UTF-8 数组。原开发环境 `ByteHelp.estimateUtf8Bytes` 只是字符数乘四的上界估算，不能作为实际字节证据。字节口径为完整带类型 `modules` 字段，包含其他模块及元数据，不能解释为订单区域单独占用或数据库物理磁盘大小。

封存标量随本次 Mapper 参数传递，持久 JSON/表结构不增加字段。只有 coordinator 收到真实 COMMITTED 后，才在锁外的通知线程发出容量指标与告警；NOT_WRITTEN、UNKNOWN、等待超时和构建快照均不冒充落库。`status(playerId)` 的 `confirmedPaymentOrders`、`confirmedModulesUtf8Bytes` 与最近确认 generation/confirmedSnapshotAt 对齐，未确认或探针失败为 null；后续保存失败不把旧确认值当成新快照。人工受信 reconcile 的确认同样更新诊断标量，但不重写原 UNKNOWN 请求的失败回执。在线对象释放后用离线主键诊断，不额外维护无界玩家指标表。

复用 Vertx 的默认 Micrometer registry，两个固定无标签 DistributionSummary 为 `payment.capacity.committed.orders` 和 `payment.capacity.committed.modules.bytes`，记录有界异步出口实际处理的已确认快照样本，不是全库订单总量、完整提交次数或唯一玩家统计；registry 未就绪时跳过指标，容量日志仍可用。部署是否收集 JMX/Prometheus 仍以实际设施为准。玩家 ID 仅存在有界限频状态和告警日志，不作为指标标签；日志只包含 ID、固定维度/级别、数值与阈值，不包含 SDK 单号、receipt、账号或正文。

JVM 启动属性统一前缀 `payment.capacity.`，首次确认保存时读取一次；调整需重启进程。以下均为运营初始预算，未作吞吐/尾延迟实测，不代表 10ms SLA。等于阈值即触发，数量与字节独立分级，窗口内从 warn 升级 critical 仍受同维度限频。

| 属性后缀 | 默认值 | 约束 |
|---|---:|---|
| `orders.warn` / `orders.critical` | 1000 / 5000 | 正整数且 warn < critical |
| `modulesBytes.warn` / `modulesBytes.critical` | 1048576 / 4194304 | UTF-8 字节，正整数且 warn < critical |
| `alertIntervalMillis` | 300000 | 1000～86400000 毫秒 |
| `trackedPlayers` | 8192 | 1～65536 个玩家限频槽 |
| `alertsPerInterval` | 128 | 1～1024 条进程级窗口告警预算 |

缺省使用默认；任一无效、倒置或溢出配置整组回退默认并输出脱敏警告，不能用零值禁用。时钟可注入，契约为单调经过毫秒；生产与保存协调器既有做法一致，以 System.nanoTime 差值派生 elapsed，告警及故障日志限频均不使用墙钟，墙钟快进再回归不会冻结窗口。日志时间戳仍由既有 DateUtil/日志设施负责，不改变全局时间。每玩家每维度每窗口最多一次告警，进程总输出也有上限；表满只回收已到期的最旧名额，未到期则抑制新玩家告警，不以轮换驱逐绕过限频。达到全局预算同样抑制，故日志是有界抽样，不承诺逐玩家告警必达；保存确认标量仍逐次保留在 status，按主键 status/CLI 可定向核查。保存回执先完成，再向进程共享后台出口提交标量；出口最多一个工作线程、一个排队样本，繁忙时丢弃观测，不能解释为可靠遥测。生产指标、容量告警和注入 observer 均在该边界内，不占用保存通知线程，也不阻碍后继写入。短锁只更新标量和名额，日志及指标均在锁外执行。监控探针/出口异常由另一进程共享故障出口异步记录脱敏日志，同样最多一个工作线程、一个排队故障、繁忙丢弃，以单调时长每进程五分钟限频。故障日志自身抛错时在最终边界终止递归报告；阻塞日志最多占用该有界资源，不能影响快照封存、保存成功/失败、UNKNOWN 或写保护。

人工处置：warn 时按玩家主键检查订单数及完整 modules 字节趋势、复核预算配置、测量保存与恢复开销；critical 时增加存储/备份余量并由运维评估扩容、冷恢复演练及可信归档方案。两级均继续保留全部证据和已支付履约，不删除、截断、覆盖旧证据，不按容量拒绝已支付发货，不自动重放或解除 UNKNOWN。容量阶段本身不提供归档、去重或终态证明；后续S3C/S3D分别交付可信封存及同库持久去重，平台独立终态和安全清理仍待后续验收，原归档待办保持PENDING。

`PaymentEvidenceCli` 仍仅按 Game 玩家主键与 Login 订单主键读取；同一 Game SELECT 返回 OCTET_LENGTH(CONVERT(modules USING utf8mb4)) 和有条件正文，显式计算 UTF-8 字节而不假设数据库列编码。超过 4 MiB 时只回传 `modulesUtf8Bytes`、`FULL_MODULES_UTF8`、`diagnosticLimitBytes`、`INPUT_LIMIT` 与 `retainedOrders=NOT_READ_INPUT_LIMIT`，不制造大占位字符串、不加载正文、不全表扫描。未超限时从既有树解析补充 retainedOrders；这不授予补发/解锁/归档权限，跨库仍非原子观察。4 MiB 是诊断正文上限，与保存和容量 critical 预算分别独立。

### S3C：单订单可信证据封存（s3c-payment-trusted-archive，T2）

显式本地 `PaymentArchiveCli` / `payment-archive.ps1` 增加 archive、verify；原 PaymentEvidenceCli 两参数诊断保持兼容。一次只采集一个显式 playerId/orderId，复用既有主键 SELECT、4 MiB modules、两秒连接/socket/SQL和七秒总采集等待。采集线程只读；超时取消后也不能在后台写归档。普通树解析拒绝重复键、尾随文档、超过64层及异常数字，不解析类型包装的类名、不构造 Player、不运行登录、奖励、保存或清理 hook。只提取指定 PayItem（全部购买参数、完成凭证或风险状态）及 Login 最小关联字段，不保留完整玩家/其他订单/账号/receipt。

自包含 `payment-<playerId>-<orderId>.json` 同时包含正文与认证回执，根目录由调用方显式给定绝对路径，文件名不来自归档内容。V1 信封包含 schemaVersion=1、algorithm=HMAC-SHA256、keyId、content、mac；content 绑定玩家/订单、全部 PayItem、最小 Login、两次读取起止时间/固定来源、可重算 assessment 和所有业务限制。V1 规范化以 UTF-8 编码，对象键按 Java String 排序、数组保序、整数十进制；拒绝浮点、超 long 数字及未知信封版本/额外信封字段。HMAC 使用 JCA HmacSHA256，域前缀 `S3C-PAYMENT-ARCHIVE-V1\n` 后接除 mac 外的全部规范信封，mac 用 MessageDigest.isEqual 比较。keyId 是外部32字节 key 的 SHA-256 指纹，不能作为独立认证；没有 key 不降级为摘要校验。verify 必须显式提供期望玩家/订单，即便 MAC 正确，错单也不接受。

独立32字节随机 key 文件必须在归档根之外，由受控运维单独保管；不内嵌、不输出、不写仓库、无硬编码默认值。根、key、正文及锁文件必须为当前执行身份私有且是常规路径。创建时设置私有权限，不修改已有 ACL；已有共享权限直接失败。逐级拒绝符号链接、junction/reparse 重定向和点段；Windows还拒绝UNC网络路径与NTFS替代数据流。当前身份从原生令牌获取，不信任可覆盖的JVM user.name。此边界不抵御相同权限的恶意管理员、目录竞态替换或密钥泄漏。备份/迁移需分别保护归档和 key；轮换后保留原 key 来验证历史，不能重新签名覆盖原观察。stdout 仅固定脱敏状态，不输出 SDK 单号、账号、正文、凭据、key 或路径。

每个根使用 OS 文件锁立即 tryLock，不等待、不自动接管；竞争失败显式返回失败。正文 CREATE_NEW、完整写入、force(true)、关闭、新通道冷读，再核对 MAC/内容/显式身份后才成功。单文件避免“回执成功但正文不存在”的多文件事务。精确相同完整观察内容可 VERIFIED_EXISTING；新读取时间也是内容，重复 archive 的新观察若不同则冲突，不能把旧回执冒充新观察，已有证据应使用 verify。任意内容/key冲突或半写/截断产物保留且拒绝覆盖，人工先调查和独立保全，不自动修补或重签。磁盘满、写/force/读失败或读回不符本轮均失败；force失败后可能已有完整文件，后续显式 verify 只证明当前可读完整性，不改写原失败结果。FileChannel.force依赖文件系统/设备实现，不提供父目录 fsync、断电持久性保证、跨节点锁或跨库事务原子性。

所有成功回执仍为 OFFLINE_STORAGE_NON_ATOMIC，platformAuthenticity/transactionResolution 固定 NOT_PROVEN，canDelete=false、canResend=false。历史 finish、未知 proof version 和执行中记录只能作为 RISK_ONLY 风险证据，不能追认为履约完成；未知PayItem/凭证字段拒绝按旧schema封存，避免默默承载新语义或额外隐私；Game V1 一致也只是本地持久字段一致，Login payState/isDeliver 不构成独立平台终态证明。读取错误/超限/损坏/重复键及玩家、订单、金额、已知 SDK 关联冲突不产生可接受回执。源 PayItem 和当前持久去重依据全部保留，不删除/淘汰、不压缩改写聚合快照、不补发、不解除 UNKNOWN、不跨节点接管。S3C本身没有减少在线快照容量；后续S3D提供同库持久去重，平台独立终态和安全清理仍待后续验收。

### S3D：Game 同库持久履约去重（s3d-payment-durable-dedup，T2）

本批依据已明确的“归档后持久去重”需求，只增加履约凭证与原子保存，不实现热 PayItem 清理。验收点为：同库事务原子提交/回滚，持久占位拦截同单并发，重复通知零奖励/Charge/邮件/保存副作用，无热订单的真实数据库冷加载仍可去重，故障及迟到提交保守阻断，默认未迁移部署保持旧功能。平台支付/退款合同、价格验证、原五分钟保存预算不变。

生产表名固定为 `t_payment_fulfillment_claim`、`t_payment_fulfillment_receipt`，DDL 位于 `game/src/main/resources/db/migration/payment-durable-dedup-v1.sql`，不自动执行。两表和 `t_player_data` 必须为 InnoDB 基础表；启用路径每次冷查询及占位建连均检查完整V1结构契约（见S3E），无成功缓存。order_id 为主键、sdk_order_id 独立完整唯一且区分大小写；单号最多191个Java字符，空值/空白拒绝。现有消息只有游戏订单、玩家与SDK单号，没有平台命名空间；全局SDK重号保守冲突，不承诺多平台重号可正常履约，不能据此扩展平台合同。

占位表只 INSERT：保存版本1的完整购买身份（player/order/sdk、rmb元、payType/payId、sdkGoodsId、保序paySubIds、addId/addCount）和随机token。只有本次成功提交占位的请求才能发奖，不借用查询到的token，不按时间、租约、保存代次或重启回收占位。相同key无论同内容重入还是不同参数竞争，失败者均不得执行奖励。占位提交丢失确认也不能继续；该占位只能人工调查，不能自动补偿或解除。

完整奖励及Charge成功后，将原PayItem V1完成证明的完整正文、SHA-256和占位身份封存到本次保存附件。`PlayerDataSnapshot` 核对该正文与本次热订单一致；附件不进入模块JSON、不全扫历史PayItem。`PlayerSnapshotWriter` 先在原玩家事务UPDATE聚合，再通过同一连接INSERT不可覆盖完成凭证，两步统一commit。JDBC和SpringManaged均使用真实事务边界；不复用普通DAO的等待超时包装。INSERT失败、唯一键冲突、影响行数异常或提交UNKNOWN不能回成功；不先回包后异步补索引。完成表无UPDATE/DELETE/upsert，普通业务账号仅需新表SELECT/INSERT。摘要只检测正文变化，可信性来自Game数据库写权限、占位来源和事务边界，不来自本地HMAC归档。

启用后重复通知先核对当前原对象是否被隔离，再从Game新连接读取两表。完成版本、身份、全部购买参数、token、原V1证明及摘要均匹配才回幂等成功；热PayItem存在时额外比对其全部购买参数，热记录不存在时依据独立完成凭证验证。该路径不访问归档目录、不发奖/Charge/邮件、不保存或修改付款事实。不存在只表示“无凭证”，仍必须有合法未执行热订单、原权威对象、写保护通过以及新占位提交才能开始首次履约；缺热订单、占位未完成、旧finish/V1无新凭证、未知版本、正文损坏、错身份、查询失败或依赖缺失均拒绝并需人工复核。

一旦占位后产生部分副作用、捕获失败、凭证事务失败或保存等待超时，原对象进入永久 `PAYMENT_FROZEN`（真实结果不明时仍显示UNKNOWN），取消未开始的排队快照，阻断普通保存、自动重试、玩家变化和释放。真实在途事务仍允许结束；迟到commit不能改写原失败，也不能自动解冻对象。此隔离不被普通UNKNOWN核对自动解除，需停服保全、排除旧事务、冷核对占位/完成凭证/聚合及外部副作用后由受信运维制定恢复；本批不开放自动恢复写入口。

唯一键和持久占位防止同订单重复执行，但不建立完整跨节点玩家权威或外部邮件事务：不同节点仍可能持有旧的整份玩家快照，独立表/外部副作用也不受Game聚合事务回滚。此部署必须停止旧写入器并确保每玩家单节点写入；不承诺跨节点接管或整体奖励exactly-once。所有热订单继续保留，`canDelete=false`。未来安全清理仍须平台独立终态证明、跨节点权威/旧事务终止与旧版本回写保护、可信归档保全和逐单数据库冷查；原capacity-and-archive整体待办保持PENDING。

历史V1不自动升级、不全库扫描/静默回填。现有 `PaymentEvidenceCli` 的显式player/order只读冷查可提供资格线索：完整V1和Game/Login身份一致是必要条件，单独HMAC、finish或Login状态不提供授权；还需排除旧事务与跨节点写入，并将凭证补建和已完成奖励快照绑定同一Game事务。当前没有补建写入口，不能用新发货路径重放历史奖励来制造凭证。

### S3D：逐单只读安全清理预检（s3d-payment-safe-cleanup-gate，T2）

本任务在当前可信归档及Game持久claim/receipt基础上，只实现显式单玩家、单订单的本地证据就绪报告，没有实现删除热订单或凭证，没有扫描、调度、批量查询、自动回填/恢复或生产开关。现有容量及归档整体待办保持PENDING，不声称减少在线快照容量；所有归档产物、密钥、claim/receipt行和热订单继续保留。

`PaymentCleanupCli` 每次从显式绝对根和根外私有32字节key重新读取真实V1归档字节，复用原HMAC、ACL、无链接路径、4MiB、重复键/尾随JSON/深度及精确schema边界。先前VERIFIED状态、文件名或SHA不构成信任。只解析普通JSON树和固定PayItem数据对象，不从归档反射加载Player/Module，不初始化登录/恢复hook；不调用业务callback、奖励、Charge、邮件、保存或数据库写入。

冷读复用Game的`t_player_data.modules`主键有界读取和Login最小字段；新的两条Game只读SELECT按order/sdk键查claim/receipt，LIMIT 3识别歧义，每条2秒，LONGTEXT在服务端限制4MiB；连接及socket沿用2秒，CLI共享数据库总预算12秒，实际调用及清理退出后报告（边界见Task4）。生产表名固定，无表名覆盖参数；测试表接缝仅包内且随机后缀受严格校验。复用`PaymentDedupStore`原身份、V1完成正文及摘要校验，而清理预检额外要求receipt完整正文（含finishTime）与认证归档完全相等，当前热项的全部身份/购买参数/证明和Login也必须与归档一致。当前Player聚合中的其他业务字段只读取，不进入报告。

读取失败/超时/缺表、损坏、未知版本、身份或参数冲突、无claim/receipt、只有CLAIMED、旧finish、UNKNOWN/执行中/风险归档、缺失冷记录以及当前内容变化均给出具体localBlockers，`localEvidenceReady=false`。热项不存在单独报告`ALREADY_NOT_PRESENT_OBSERVED`和`HOT_ITEM_ALREADY_NOT_PRESENT_NO_DELETION_PERFORMED`；这是已经不存在的观察，不是可删除或清理成功。

全部局部核对一致可以报告`localEvidenceReady=true`，但status仍为`BLOCKED_EXTERNAL_PROOFS`，`canDelete=false`且`canResend=false`。固定外部阻断为`PLATFORM_TERMINAL_NOT_PROVEN`和`OLD_WRITERS_AND_UNKNOWN_NOT_FENCED`，对应未来任务必须接入的独立认证平台终态来源、可信旧写入者/旧事务隔离及UNKNOWN排除证明。当前receipt或Login payState/isDeliver均不能替代这些证明。多连接只读观察不是原子快照，存在检查与使用之间的TOCTOU，未来删除设计还需原子重新校验；不能把本报告当作任何写入授权。

### S3E：持久去重启用结构预检（s3e-payment-enable-preflight，T2）

本次只实现结构防错与只读观察。`PaymentDedupSchema` 是发货与运维预检共用的 V1 契约，权威DDL仍是 `payment-durable-dedup-v1.sql`。`PaymentDedupStore.lookup` 和 `claim` 的新连接均完整检查：缺表/元数据不可读或不兼容抛错，实际Handler在占位和奖励前拒绝。检查不缓存，先前lookup成功不能免除claim再次检查。默认开关仍OFF，未启用路径不检查新表。

三表必须为InnoDB基础表；玩家表这里只验证事务引擎，不宣称审核了全部玩家Mapper结构。claim精确字段为有符号BIGINT order_id/player_id、VARCHAR(191) utf8mb4/utf8mb4_bin sdk_order_id、MEDIUMTEXT utf8mb4/utf8mb4_bin intent及CHAR(36) ascii/ascii_bin token。receipt额外要求有符号INT version、MEDIUMTEXT utf8mb4/utf8mb4_bin proof及CHAR(64) ascii/ascii_bin proof_sha256。全部字段NOT NULL、无显式默认、无EXTRA或生成表达式；拒绝所有额外字段（包含当前可能可写的nullable/default额外列），避免默许未经设计的新持久语义。字段顺序不影响显式列名INSERT；整数旧式显示宽度只影响展示，忽略宽度但不忽略UNSIGNED/ZEROFILL。除此不放宽类型、长度、容量或排序规则。

每表必须有完整单列order_id主键和完整单列sdk_order_id唯一BTREE，唯一索引名不影响语义；逐列读取SUB_PART/SEQ_IN_INDEX，不用GROUP_CONCAT推断。前缀/复合唯一键、额外唯一键、CHECK/FOREIGN KEY以及可见触发器均拒绝；普通非唯一索引仅允许引用已知列的BTREE，不允许函数/隐藏生成表达式或FULLTEXT/SPATIAL等其他索引类型，避免表达式在INSERT时产生额外失败。该规则不推断所有DDL变更等价或所有数据库版本兼容。

每条固定information_schema SELECT绑定单一表名，超时2秒，LIMIT及maxRows=129；128行上限以额外一行判超限，不给截断结果签名。报告包含contractVersion=1、固定角色/字段/预期规则及SHA256观察签名；原始默认值/表达式、表归属注释、连接值、玩家和订单正文不进入诊断。签名只绑定本次读到的元数据，不是跨查询原子快照，不证明并发DDL不存在、权限完整可见或发布安全。运维须确保元数据可见性（尤其TRIGGER权限）并在部署窗口排除DDL；读取器无法发现权限不可见的触发器。每次lookup/claim多次元数据查询有实际开销，本次未做缓存优化。

`PaymentEnablementCli` / `payment-enable-preflight.ps1`无业务参数，固定生产表，仅SELECT元数据，不读订单或玩家数据、不迁移、不修改Apollo/JVM开关、不回填/补发/解冻/删除。私有Game连接与socket超时各2秒，整条诊断共享12秒数据库截止点（包括全部schema SELECT），实际JDBC调用及连接清理退出后返回，不再用取消守护Future替代数据库操作终止。成功结构观察也为BLOCKED_EXTERNAL_PREREQUISITES，canEnable/canDelete/canResend始终false；平台全局SDK命名空间、旧写入器/UNKNOWN隔离、历史凭证覆盖始终列为外部阻断，schema签名不能替代。全局平台终态/旧事务排除等原清理前提也未建立。

检查后至claim/奖励/receipt事务仍有并发DDL窗口；部署应停受理、隔离旧写入器并冻结DDL。不能因预检通过直接在线启用。DDL/连接/权限/SQL mode/容量或外部副作用仍可能造成晚期故障；原同事务聚合+receipt提交、失败/UNKNOWN永久隔离及不自动重放语义继续适用。未证明数据库权限、部署fencing或历史完成订单迁移完成，生产命名新表不存在仍是预期默认关闭状态。

### Task4：数据库等待与未知结果边界（s3e-payment-database-timeout-boundaries，T2）

实际Game启动由`GameServer`的`SpringApolloLoader`加载`dataserver.xml,gameserver.xml`，远程配置为`SqlSessionFactoryBean`、`HikariConfig/HikariDataSource`，连接获取配置60000ms；本地Login Druid及替代`DynamicDataSource`不证明当前Game接线。当前依赖HikariCP 6.2.1、Connector/J 8.0.26、MyBatis 3.5.10/mybatis-spring 2.0.7。本任务只读核对Apollo，不更改远程设置、不启动服务或启用持久去重。

`PaymentDatabaseBoundary`用于专用玩家保存和持久lookup/claim。默认本地参数`payment.db.acquireMillis=65000`（覆盖池60000+一次validationTimeout默认5000）、`networkMillis=5000`、`queryMillis=5000`、`totalMillis=75000`；参数必须为有限正整数、total>acquire且不超过300000ms，不合法则在Mapper前失败。只接受已初始化的精确Hikari实现、经驱动URL验证的单主机DriverManagerDataSource，或明确声明获取/废弃能力的隔离适配器；未知路由/池和不支持网络设置的驱动不被当作已支持。Hikari获取及存活校验配置不能超过本地获取预算，不为一次调用修改共享池。未初始化池必须由启动流程先完成初始化，不能把首次同步初始化的耗时伪装为connectionTimeout。

借用后核对并设置connection networkTimeout，语句执行前按同一单调截止点设置剩余网络/查询预算，结果集推进和commit/rollback也消耗此截止点；schema的多条SELECT与receipt SELECT/INSERT没有新预算。JDBC查询超时单位为秒向上取整，毫秒级剩余预算另由网络超时约束。Connector/J连接私有`queryTimeoutKillsConnection=true`使查询超时强制关闭该连接，避免取消时另起KILL QUERY建连；正常归还恢复原值及networkTimeout/autocommit/readOnly/isolation并逐项核对，失败或未结束事务直接物理废弃。失败连接不得靠恢复autocommit=true隐式提交；Hikari摘除后不再关闭已摘除代理触发错误恢复。连接级设置只属于独占借用期。

JDBC路由将预算连接传给原工厂，Spring路由预先按原数据源身份绑定ConnectionHolder，再用同一数据源的事务管理器/原工厂；聚合与完成凭证仍用同一连接原子提交。Mapper前获取/能力校验失败为NOT_WRITTEN；进入Mapper后任何SQL/提交异常或迟到确认超出截止点保持UNKNOWN，协调器继续隔离后继。真实提交已确认后的恢复/关闭失败不抹除COMMITTED，但连接仍需废弃。等待者10ms或30秒超时不取消JDBC，不提前释放真实在途写权；数据库/代理端事务终止仍需受信运维证明。

Evidence/Archive/Cleanup/Enablement的生产读取器共享整条12秒数据库截止点，私有connect/socket及每语句上限2秒，不重写URL或改SSL等安全选项。本机Connector/J的Properties优先于URL；额外验证URL显式超时，拒绝无限/超预算值、重连、非单主机和自定义连接路径。生产CLI同步等待真实调用/清理；只有测试注入任意读取器的兼容等待入口可先报告超时，此时显式readerExited=false，不宣称停止。

预算不是硬实时上限：Hikari物理建连属于池自身工作线程及生命周期，本任务不改变其connect/socket配置；现有Apollo未显式配置物理connect/socket上限，运维须独立补证并配置，不把池借用超时称为物理建连取消。DNS、TLS/认证多次读写、JVM暂停、OS调度、驱动缺陷/自定义代理停顿可使真实退出晚于名义总预算；不会以Future取消提前报告事务结束。网络/查询超时、socket关闭及冷读结果都不能单独证明服务端已回滚或旧事务不会晚提交。没有DDL缓存、性能优化、历史盘点、自动重放、解冻、生产DDL/开关或平台调用。

### Task5：有界只读历史订单盘点（s3e-payment-legacy-inventory，T2）

`PaymentLegacyInventoryCli` / `payment-legacy-inventory.ps1` 仅接受显式 `playerId:orderId` 逗号清单、pageSize和cursor；没有默认范围、玩家发现、范围查询或全库扫描。最多100个不同订单、每页1–10项，正long溢出、重复订单（包括跨玩家重复）、多余参数均拒绝。按玩家和订单数值排序，初始cursor为`-`，后续使用报告nextCursor（版本域+排序清单SHA-256及下一输入位置）；清单改变拒绝游标，排序重排不改变范围。SHA只绑定输入，无认证/业务授权意义。scopeExhausted只说明显式输入位置耗尽，不是历史数据库已盘点；各页可重复观察，无跨页一致性。

固定按Game玩家主键读取原始modules、Login订单主键读取最小字段；不创建Player/PlayerModule，不解析包装类名、不调用登录/恢复、奖励、通知、Charge或保存hook。严格JSON树复用归档解析器（重复键、尾随、深度64、数字长度32），完成项复用清理的精确字段和PaymentDedupStore V1验证；持久claim/receipt复用原严格观察，receipt未知未来版本保守标记。历史finish无V1、未完成/pending/UNKNOWN、身份或金额/SDK冲突、未来版本、有效V1热完成但新表中无行、有效当前claim+receipt、仅claim、损坏/不可读、缺依赖分别计数。没有生产新表是默认OFF的正常部署可能性，报告MISSING_DEPENDENCY，绝不解释为空凭证；只在两表查询均成功且无匹配行时报告无新凭证。缺热订单不从receipt或归档推断；本工具不接收归档文件（若未来扩展，必须复用实际HMAC认证，不能只查摘要）。

配置由S2_DB_CONFIG显式绝对路径提供，复用归档的当前执行身份owner/私有ACL、无链接路径及64KiB读取上限；不自动读取Apollo。复用Task4的真实JDBC预算/URL解析/只读连接惯例：整轮单调12秒（含配置、解析、连接及清理），每次获取/网络/查询2秒、最多30个连接/40条应用SELECT每页；modules每项4MiB，claim intent/receipt intent/proof各4MiB，Login SDK最多764 UTF-8字节（超长不加载正文、读取失败）。这些是有限合作式等待及正文预算，不能承诺DNS/驱动/OS暂停下的硬实时截止，更不证明服务端事务终止。输出仅固定聚合分类/原因/覆盖和游标（<12000字节），不含平台单号、玩家JSON、凭据或明细文件；明确输入本身可留在本机进程命令行，操作员须使用受控终端。

`observed`为本页已完整分类的输入，counts之和等于observed。`attempted`含已经启动但失败的那一项；unreadableAttempts仅计失败项，dependencyOpenFailed单列配置/读取器创建失败。任一连接/查询/预算故障立即停止，保留前项计数，nextCursor停在失败输入，后项不读取；无法读取不冒充空页或缺行。已到末尾的游标返回成功空页且不创建读取器。退出码0=本页完成（可有风险分类，不能认定可处理），1=参数非法，2=部分读取/配置/连接/超时失败。跨Game/Login及claim/receipt读取存在TOCTOU，无原子观察或旧写入者/UNKNOWN隔离证明。

所有报告canDelete/canResend/canBackfill恒false；“候选”仅意为needs manual evidence review。下一步需独立认证的平台终态、可信旧写入器及UNKNOWN排除证据，以及另行设计/授权的原子重验证与写入方案。没有回填、重发、删除、退款、解冻、DDL或开关/Apollo修改入口。本轮只验收新建随机所有权表/专用fixture，不执行真实历史或业务账号盘点；生产历史分布和平台契约未验收。

### S4：有界请求计时与10ms基线

`TimingMetrics` 以固定 Stage × Route × Outcome 单元记录单调纳秒耗时，默认关闭；显式 JVM 属性 `-Dgame.timing.enabled=true` 开启。复用 `ExecutionMonitor.collectStatistics().timings` 提供拉取快照，不假定监控后端已安装，也不自动写 Apollo。没有每玩家、订单、SDK、trace-id 或任意协议名/地址标签。REQUEST 仅分 HEARTBEAT、LOGIN、OTHER；其他阶段归 ALL。固定364单元、每单元32桶，无遥测队列、线程、外部 observer 或逐请求日志。业务写入使用 tryLock，快照/并发采样争用时丢观测，记录全局及对应行 dropped；不会等待慢快照读取者。快照读取是诊断操作，须放在监控/运维线程。时钟故障只累计 failures，刻意不在完成链输出可能阻塞的故障日志。

| Stage | 实际起止与结果口径 |
|---|---|
| REQUEST | IdVirtualThreadProcessor 完成既有入口去重后、提交前，到同连接/原seq、请求号+1或通用错误协议的首次写入Future完成。成功还要求协议errorCode=0；发送同步异常/失活为FAILURE。未执行拒绝在拒绝确定时记REJECTED，其耗时不是拒绝回包写完；同步dispatch返回且未发起匹配发送为NO_RESPONSE |
| MAILBOX_QUEUE | TaskWrapper创建至本次markStarted；每次重试重新起点，包含实际退避和重排队。未开始被拒绝记REJECTED |
| EXECUTION | 每次task.execute调用至返回/抛错，包含业务内DB/RPC同步等待，排除结果Promise监听器和错误策略。与既有MailboxStats包含结果回调/策略的诊断时长不同 |
| TASK_ACTUAL / TASK_WAIT | 实际提交生命周期（含重试）至真实结果；以及execute入口至原等待Future结果。等待超时记TIMEOUT，不取消/放行实际任务；等待超时后真实完成另记LATE_SUCCESS/LATE_FAILURE，不与SUCCESS相加当请求数 |
| DB_WORKER_QUEUE / DB_WORKER / DB_WAIT | VxHolder执行前的worker调度等待、实际Callable执行、原timeout等待视图。实际结果与超时/迟到完成分开；保留DAO原错误/超时与线程模型 |
| CONNECTION / SQL | 仅PaymentDatabaseBoundary支持的专用支付/聚合保存路径：实际getConnection调用，以及实际Statement execute*/commit/rollback调用。SQL不含prepare、预算设置、结果集后续遍历和连接恢复；通用DAO未拆分连接/SQL，以DB_WORKER观测其整体工作 |
| TRANSACTION / SNAPSHOT | PlayerSnapshotWriter.execute的实际边界（含获取、Mapper、commit/rollback及清理），仅实际committed为SUCCESS；以及PlayerDataSnapshot中唯一完整modules JSON序列化调用。未改变参数隔离、JSON格式、保存保序、UNKNOWN与持久去重 |
| RPC_WAIT | RpcClient的请求模式，含传输及统一响应解码；同步/异步失败保留原原因，超时单独分类。广播不制造等待样本；传输超时不能证明远端实际退出，不制造远端迟到完成 |

REQUEST不含socket读取、此前解码、客户端网络或确认收包；异常响应/拒绝有独立结果。仅关联上述相邻协议号约定，特殊响应号不猜测关联；同seq的其他推送被排除。线程上下文只在同步dispatch期间持有连接引用与seq，finally恢复/清理；匹配发送发起后，原写入Future回调只捕获计时，不捕获玩家对象。脱离作用域的异步发送、另一个processor/HTTP入口不自动传播，因此NO_RESPONSE表示此观测边界未见匹配发送，不表示业务失败。尚未完成的写入不产生终态样本。没有引入新的trace ID或业务幂等身份。

每行count是已记录的终态样本分母，dropped是该行争用丢样数，over10ms直接比较原始纳秒且严格大于10,000,000；比例为over10ms/count（count=0时无比例），不能把丢样算成达标。maxNanos为已记录真实最大值；p50/p95/p99UpperMicros是1us、2us、4us…2^30us桶的最近秩上界近似值，溢出桶为Long.MAX_VALUE，空行分位数为0。活跃快照逐单元读取并非全局原子；排空后每阶段count+dropped可与实际次数精确核对。竞争丢样可能有偏，不能把直方图当全流量精确分布。reset替换窗口，已开始Span仍属于旧窗口；跨重置的不同阶段按各自开始时窗口归属，不强行凑成同一请求。旧ExecutionMonitor全局任务字段修正为当前保留邮箱累计值之和，重复读取不再重复累加；邮箱回收后这些旧字段会下降，不是进程终身累计。

有限基准 `RequestTimingBenchmark` 在新JVM运行：真实TaskExecutorService、IdVirtualThreadProcessor、BaseHandler/GameBaseHandler、PlayerHandler.heartbeat、Protobuf解析及GameClient二进制响应构造，只有分发注册及WebSocket传输为本地替身。每次新建独占GameClient/seq=1，只发一次合法空心跳，使用固定进程内实体键，不登录、不加载实际账号、不触碰共享用户；单实体热点与按生产者分散实体分别测试1/4/16并发，队列容量64，虚拟线程消费者、1/4/16生产线程，无DB池。固定seed=84017；每矩阵配置128预热、512测量，OFF/ON各一轮，共24窗口/7680次，预热1536与测量6144分开。所有原始纳秒、精确最近秩分位数、>10ms分子/分母、错误数、排空时长、直方图与丢样均保存；每阶段count+dropped核对真实完成数，校验执行类字节前后哈希及资源退出。每操作最多等待5秒、窗口等待30秒，子JVM总90秒，失败强制回收子进程；正常关闭还必须确认消费者与生产线程退出。原有服务close仍沿用既有等待契约。

首次开发测量环境：JDK21.0.9+7-LTS-338、HotSpot64、Windows10.0 amd64、Intel64 Family6 Model151 Stepping5、8逻辑处理器。开启观测测量窗口的p99为0.052–0.597ms，六窗均0/512超过10ms；16并发热点的排队p95桶上界256us，执行p95桶上界16us，显示该场景队列占比更高。分散并发出现观测争用丢样，完整原始基准样本仍逐次保留。此结果仅是一次进程内空心跳基线，非已部署Game/Login、认证、真实网络或DB/RPC/快照E2E；相关DB/RPC/快照样本明确为0，不能推断支付/保存或所有请求10ms达标。OFF/ON固定顺序且宿主未校准，不宣称仪表开销百分比或性能提升。真实DB拆分另由本轮独有fixture保存、SQL/连接/事务观测与冷读验证，不冒称请求负载基准。平台延迟、GC和部署实例环境仍需后续有归属测试环境测量，不能为满足10ms削弱持久化。

---

## 二、专项运维命令、部署顺序与回归验收细则（摘自原 doc/02）

### S2C 玩家变更与保存风险约束

新增聚合玩家业务应在既有 `GameBaseHandler`、Player 业务 timer、`PlayerHelper.addTask/modifyPlayer` 入口执行；自行创建的异步回调应使用 `PlayerWriteGuard.submit/submitValue`，携带原 Player，在实际执行时重新核对身份及写保护。同步模块操作可在已有作用域内执行。不要把整个外部 Future 视为同步作用域，也不要在远程回调线程直接写玩家字段；需要同步等待时须核对不等待同一邮箱内的后继任务。

保护错误不是业务成功；请求应传播失败，协议拒绝使用原请求序号，不返回虚假的充值或保存成功。心跳与恢复保存豁免，但业务性定时任务仍必须经过保护。手工保存必须通过 PlayerHelper 统一捕获/提交；等待超时不重投，UNKNOWN 不用一次查询自动解锁。自动重试耗尽或永久失败先排查原因，再显式保存；仅最新风险完整确认且无未决写入才能解除保护。

验收应覆盖真实业务分派的“排队时健康、执行时已超预算”以及迟到回调、异常关闭作用域、旧快照成功不能清除新变化。使用 `save-risk-regression-expected.json` 的明确用例清单。模拟事务通过只证明本地边界，不代替现有数据库上隔离账号的冷读和故障注入；新功能若加入旁路写入，须补相应入口测试。

### S2 存储层真实数据库验收

`PlayerSaveDatabaseIT` 显式选择执行，不随普通 `*Test` 单测连接数据库。先设置环境变量 `S2_DB_CONFIG` 指向本机 JDBC Properties 文件（键为 `dburl/dbuser/dbpass/driverClassName`，不要把凭据提交仓库或写入日志），再运行：

```powershell
pwsh -NoProfile -File ./.ai-workspace/scripts/run-java-tests.ps1 -Project game -Test 'PlayerSaveDatabaseIT,PlayerTimerCancellationTest' -ExpectedTestsFile ./game/src/test/resources/save-database-expected.json
```

在已登记的任务中，仍用 `Task Run` 包装上述命令。配置缺失应失败，不把跳过当通过。测试只插入带唯一 `runId/playerId/uid` 的 `t_player_data` 夹具，不创建表、不使用业务账号、不启动 Game/Login。成功后按主键、UID、accountId 和 deviceId 同时匹配删除；失败保留行及登记日志，确认测试进程、底层写入结束后再按这些精确标识处理，禁止按名称前缀批量清理。

五项数据库用例分别在 JDBC 与 SpringManaged 事务下验证：生产 Mapper/Writer 保存后新会话冷读并重建 Player；真实 commit 前阻塞时等待超时不让后继快照提前进入写入器；真实 commit 已返回后注入回执异常，保持 UNKNOWN 并在实际结束和冷读核对后显式恢复；可支付订单经生产发布流程确认保存后，在新 Player 中恢复完整关联且不恢复临时 callback；实际离线发货提交确认后才回复和清理，再冷读订单及测试奖励标记。回执异常是受控提交故障，不等于真实网络断线、数据库宕机或进程崩溃演练。定时器用例覆盖排队后取消、取消全部，以及已因保护延期后的单次取消与未取消对照。

这些夹具不等于正式注册账号。注册/登录/重连全链路、真实节点故障与接管、共享负载下五分钟预算和 10ms 性能目标，仍须使用专用注册账号另行验收。

### S3A 前置：支付结果消费与异步回复

每日礼包（含打包购买）、限时礼包、基金签到购买只在 `Boolean.TRUE` 支付结果下调用原购买/奖励逻辑。`false`、`null` 和失败 Future 都不能发奖或回复成功；支付仍未决时不提前发送成功。每日礼包和基金的无具体原因失败沿用 `unknown`，限时礼包沿用 `xian_shi_li_bao_buy_fail`。异常保留明确业务错误码，并通过派生 Future 捕获后续购买回调异常，记录原始异常；这不代表部分发奖已回滚。

这三个入口在调用支付前创建业务局部的 `PaymentReply`，捕获原连接和当次请求序号。异步成功、失败及响应缓存统一归属原请求，不能取回复时已经被心跳或新请求更新的序号，也不改发到重连后的新连接。本批已推广到下文列出的现金购买消费点，仍不等于全项目异步协议已完成审计。

`PaymentFailureResponseTest` 直接调用实际 Handler，使用受控支付结果和奖励模块；针对性运行指定 `payment-responses-expected.json`。测试覆盖普通/打包礼包、未决/失败/成功/null、支付及发奖异常、业务错误码、响应类型与奖励字段、连接替换、请求序号推进以及真实 GameClient 缓存重放。不启动真实充值平台，不宣称已验证奖励持久化。

本批分离发奖执行与保存后的最终成功；后续 S3A 仍需完成跨服务孤儿订单恢复、重复回调幂等成功确认、Login 订单单调状态及平台回执契约核对。当前修复不能替代这些持久化与恢复验收。

### S3A 可支付订单发布验收

现金订单必须复用 `PaymentOrderPublisher`：先保存 `PayItem` 关联，再尝试推单，且推单成功不能提前完成支付为 true。只支持聚合表；独立表必须在 Login 创单前拒绝。超时、UNKNOWN、捕获异常均保留已有关联但不推单，迟到成功不自动重推。同步发送失败必须精确清除本次 callback，不能删除 PayItem 或其他订单的 callback。

针对性验收使用 `PaymentOrderPublisherTest` 和 `payment-order-publication-expected.json`，覆盖 RPC 无效/null/失败、保存未决/失败/UNKNOWN/超时晚提交、真实邮箱发货排队、重复订单、原对象/连接/session/退出状态、序号缓存、复制参数、独立表拒绝与同步发送失败。实际 `Player.pay` 的普通/一元充值接线以外部事件总线替身核对 RPC，发布组件测试核对推送与持久字段；不把该替身称为 Login 服务验收。一元充值 SKU 统一为 `1007`，回退本提交将恢复旧的不一致行为。

执行上方真实库命令同时验收第四项发布后关联冷读，现有数据库只使用精确隔离的夹具行。完整 SDK 发货、重启后的订单恢复、最终奖励只发一次及所有消费者请求归属尚未在本批完成。保存等待期间同玩家请求排队，需另行做性能基线；不能将 10ms 目标写成已达成。

### S3A 发货归属与离线收尾验收

发货必须在实际邮箱任务内校验原对象身份；不能在保存的外部完成回调按旧 online 值清理玩家。离线加载内部也需要邮箱，只能异步加载完成后再投递业务；保存可以在邮箱等待，但不能等待同一邮箱中的后继任务。离线历史默认 login 标志不代表实际在线登录，须结合临时连接与加载结果判断；重连后真实连接即使尚未更新 online 标志，也不能作为可清理的离线临时对象。

已观察到加载失败的驻留实例保持本 Handler 发货隔离，依据必须来自 `OfflinePlayerInitializationFailure` 携带的原对象；登记前失败不标记当前同 ID 玩家。核对初始化失败原因、保存风险和当前归属后，才能通过既有安全生命周期释放并重新加载；不得直接删除隔离记录、清空玩家或用新对象覆盖旧对象绕过风险。该标记不是跨进程恢复证明，也不能说明其他加载入口已经统一治理。

使用 `PaymentDeliveryLifecycleTest` 与 `payment-delivery-lifecycle-expected.json` 验证实际入口、正常离线默认标志、初始化在途/失败残留、排队/保存期间归属变化、同对象换连接、提交前零回复、UNKNOWN/超时保留、提交后发送/清理异常不双回复，以及 Charge 异常后的保守拒绝。真实库清单包含第五项提交门槛与冷读，使用专用行及测试奖励标记；不代替正式账号和真实平台的完整履约验收。本批建立下述奖励完成契约；本批仅对新版本完成凭证开放幂等成功，不能只凭 finish 放行。

### S3A 奖励完成与最终成功验收

验收顺序固定为：完整奖励 Future 明确完成 → 同步 Charge 正常返回 → finish/REWARDS_COMPLETED → 关闭业务作用域 → 同一保存协调器确认 → 原请求购买成功及 Login 成功。`PaymentCompletion` 的奖励 Supplier 不包含最终成功响应。异步步骤必须返回真实业务结果，不能把 Future 创建、任务受理或网络发送受理作为发奖完成。现行注册消费清单、旧订单兼容和保守恢复边界见 01 文档 9.2.5；不改变各入口奖励数量和协议消息格式。

`PaymentCompletionTest` 覆盖异步延迟、同步/异步失败与部分副作用、Charge 异常、未迁移 Boolean 回调拒绝、离线异常、保存失败/UNKNOWN/超时与迟到提交、奖励等待期间权威替换、阻断 JSON 及旧 finish、月卡实际离线购买等待邮件插入。`PaymentFailureResponseTest.actualHandlersWaitForShipmentRewardsAndCommit` 把每日礼包、限时礼包、基金签到的真实 Handler 注册连接到真实 ServerHandler 发货分支，提交前同时核对购买与 Login 零成功。其他注册点已迁移并编译，未宣称逐商品配置/所有实际奖励模块全覆盖。

精确方法清单为 `game/src/test/resources/payment-completion-expected.json`，包含本次新增方法及原支付/生命周期/保存风险回归目标。普通执行命令如下，仍用同一任务的 Task Run 包装，批次为 compile-tests 与 payment-completion：

```powershell
mvn -B '-DskipTests' test-compile
pwsh -NoProfile -File ./.ai-workspace/scripts/run-java-tests.ps1 -Project game -Test 'PaymentCompletionTest,PaymentFailureResponseTest,PaymentDeliveryLifecycleTest,PaymentOrderPublisherTest,PlayerSaveRiskTest' -ExpectedTestsFile ./game/src/test/resources/payment-completion-expected.json
```

在受限环境中若 POM 的 Git 元数据 exec 插件读取提交号受阻，可在验证子进程的 `MAVEN_OPTS` 附加 `-Dexec.skip=true` 仅跳过该插件，仍执行真实 reactor 编译及 Surefire，用例与新 XML 检查不跳过，不修改 Git 信任或项目配置。

部分执行失败只隔离待核对，不假设奖励可回滚。核对前禁止清空 completionState、reset finish、删除驻留 Player 或重新发送礼包；后续恢复需单独限定订单、实际副作用及保存/邮件终态。未确认保存的最终购买响应不发送成功；资源/邮件推送仍可能在失败之前已产生。本轮不启动业务服务、不连接真实数据库，不复用上一批数据库/审核通过作为本批证据；真实平台、跨重启/跨节点、十天清理、全平台对账与完整业务环境另验。

### S3A 重复通知验收与平台边界

任务 `s3a-payment-idempotency` 的 Game 清单为 `game/src/test/resources/payment-idempotency-expected.json`，逐项保留上一批支付、生命周期及保存风险回归并新增凭证测试。Login 必须另跑 `login/src/test/resources/payment-idempotency-expected.json`，Game reactor 不覆盖 Login 用例。真实库必须显式运行 `PlayerSaveDatabaseIT#completedPaymentColdReloadDeduplicatesWithoutReplaying` 与离线发货回归，精确清单为 `payment-idempotency-database-expected.json`；从当前 Apollo game/default/jdbc 获取配置后设置测试进程 S2_DB_CONFIG，使用唯一行 INSERT 和身份限定 DELETE，不触及业务账号。未配置或连接失败必须记未完成，不能复用旧库证据。

```powershell
# 下列命令均由同一任务 Task Run 包装；仅跳过既有 Git 元数据 exec 插件。
$env:MAVEN_OPTS += ' -Dexec.skip=true'
& ./.ai-workspace/scripts/run-java-tests.ps1 -Project game -Test 'PaymentIdempotencyTest,PaymentCompletionTest,PaymentFailureResponseTest,PaymentDeliveryLifecycleTest,PaymentOrderPublisherTest,PlayerSaveRiskTest' -ExpectedTestsFile ./game/src/test/resources/payment-idempotency-expected.json
& ./.ai-workspace/scripts/run-java-tests.ps1 -Project login -Test PaymentNotificationTest -ExpectedTestsFile ./login/src/test/resources/payment-idempotency-expected.json
& ./.ai-workspace/scripts/run-java-tests.ps1 -Project game -Test 'PlayerSaveDatabaseIT#completedPaymentColdReloadDeduplicatesWithoutReplaying+offlineDeliveryCommitsBeforeReplyAndCleanup' -ExpectedTestsFile ./game/src/test/resources/payment-idempotency-database-expected.json
# Login 使用当前 login/default/jdbc 的私有临时配置；不沿用 Game 数据库。
& ./.ai-workspace/scripts/run-java-tests.ps1 -Project login -Test PaymentOrderDatabaseIT -ExpectedTestsFile ./login/src/test/resources/payment-idempotency-database-expected.json
```

验收包含提交前零成功、并发及成功后重投只执行一次业务、身份/商品/金额不匹配、历史 finish、异常及保存 FAILED/UNKNOWN/timeout 的保守处理、新协调器和新玩家从真实数据库恢复后无副作用去重。冷读 IT 只证明本批隔离玩家聚合快照及受控奖励计数，不代表真实邮件平台、全部 SKU、跨节点或任意故障下恰好一次。

畅游字符串 SKU 与内部 itemId 的对应、固定数量及历史 debug_text 不是可证明的约束，已移除上轮猜测限制，回归验证历史 Login 行仍走原验单及 Game 链。身份、精确金额和已绑定单号检查继续保留。正式商品/金额单位、验单与本地订单绑定、平台更新正文及重试语义仍缺契约；MD5 不含秘密，不是可信绑定证据，不能声称真实平台全链已验收。微信/iOS 的其他外部字段和完整支付环境仍需独立平台验收；本批不启动业务服务或进行真实支付。

Login 真实库独立清单逐方法要求并发/迟到失败、冲突及未知支付状态；回执使用真实 JSON 列合法夹具，双影响行数配置均需执行，条件删除后冷查为空。若无对应订单表则失败，不创建表或改用模拟数据库掩盖。本批凭证新增字段经基线模式验证旧 Jackson 不可读，按不兼容格式更新停机排空旧 Game 写入器，写入后只允许前向修复，不支持旧 JAR 直接回滚或新旧混跑。该边界不声称已执行集群停机演练。

S3A 审查修复验证：关联任务 `s3a-payment-review-fixes` 保留原任务与全部历史记录。GM/微信实际 handle 和 iOS 共用生产处理链的回归纳入 Login 逐方法 `payment-idempotency-expected.json`；覆盖同步异常结束 HTTP、非数字单号、空 SDK 身份、状态/金额/流水冲突、并发乱序及异步失败。PayItem 未知字段与未知版本同时验证；Login 真实库冲突断言增加 `mch_order_no`。本轮证据均需重新执行并留证，不能复用原任务通过记录。

### S3B 证据保留与只读核对验收

本批新增 `payment-evidence.ps1 -PlayerId <正整数> -OrderId <正整数>`，从当前 game/core/protocol/util 编译目录及 game/target/lib 读取依赖；需要先完成编译和部署依赖，不会自动启动生产服务。S2_DB_CONFIG 指向本地受限 Java Properties 文件：Game 使用 dburl/dbuser/dbpass，Login 使用 login.dburl/login.dbuser/login.dbpass；配置由受控运维提供，禁止进入仓库、日志或结果。测试从 Apollo 的 game/default/jdbc 与 login/default/jdbc 仅读 configurations，正确 Properties 转义、限制文件 ACL，仅测试进程设置环境变量，finally 删除临时文件。CLI 不查询 Apollo，不新增 HTTP/协议或数据库 DDL，运维应使用已有 OS/数据库权限控制并优先只读账号。

清单 `game/src/test/resources/payment-evidence-expected.json` 逐方法覆盖本批实际初始化清理和 CLI 树读、十天边界、旧/新/未知凭证、冲突、异常字段、损坏 JSON、无记录、输入及大小限制、查询失败/超时、重复执行只读性，并保留既有支付/保存回归。`payment-evidence-database-expected.json` 要求唯一 fixture 的真实持久化→新会话冷读→实际初始化清理→CLI 两库核对重复两次→完整 modules 不变→精确清理后冷查为空，并保留实际发货/重启去重回归。Login 本批没有修改，不以 Game reactor 声称覆盖 Login 行为；CLI 对 Login 的查询只读且不读取 receipt。

验收批次为 `compile-tests`、`payment-evidence`、`payment-database`。受限环境下仅允许在验证子进程 `MAVEN_OPTS` 附加 `-Dexec.skip=true` 跳过 Git 元数据 exec 插件，真实编译与测试不可跳过。定向测试通过 `.ai-workspace/scripts/run-java-tests.ps1` 显式 Surefire 3.5.2 执行，要求本轮 XML 与精确方法清单；真实库仅唯一 fixture，禁止业务账号/平台请求/启动服务，结果以本轮新证据为准，不复用历史记录。

### S3C 订单容量观测验收与独立复跑

任务 `s3c-payment-capacity-monitoring`（T2）只完成每玩家保留订单数、完整 modules UTF-8 字节、可配置预算和有界限频告警，以及只读诊断容量信息；配置键、默认值、等值触发、无效回退、指标口径和人工处置见本文 S3C 架构章节。归档、安全删除、持久去重索引和平台终态证明仍待另行任务，不能把原 `payment-evidence-capacity-and-archive` 全部关闭。

独立入口：`pwsh -NoProfile -File ./.ai-workspace/specs/features/ServerArchitectureAndAgentReadiness/tasks/s3c-payment-capacity-monitoring/evidence/run-review-tests.ps1`。默认 All 运行六模块真实 test-compile，再运行 Game/Login 定向测试；`-Mode Compile` 与 `-Mode Test` 可由 Task Run 分别登记 compile-tests/payment-capacity。Test 同样执行 test-compile，不使用旧 class 代替编译。只跳过 Git 元数据 exec 插件，显式开启测试并固定 Surefire 3.5.2。

每次由当前执行者在 `.ai-workspace/.runtime/payment-capacity-review/<随机ID>` 创建自己的目录，Windows ACL 仅授予该执行者，不依赖其他用户/旧执行者的私有目录，也不修改既有 ACL。完整日志、fresh XML 副本及 gate.json 保留于该目录；控制台输出短脱敏摘要，任何失败保持非零退出。运行前删除明确选中类的旧 XML，逐个要求新报告；按完整类名#方法名核对 expected，全部方法恰好执行一次，suite/testcase 计数一致且无 failed/error/skipped，额外/漏跑/计数不符均失败。源码和清单指纹随本轮 gate 保存，用于审核对齐同一快照。

新 `payment-capacity-expected.json` 复用原 payment-evidence 方法和保存快照回归，并加入本任务监控及诊断用例；旧 expected 不改。必须实际运行 PaymentEvidenceTest、PaymentIdempotencyTest、PaymentCompletionTest、PaymentFailureResponseTest、PaymentDeliveryLifecycleTest、PaymentOrderPublisherTest、PlayerSaveRiskTest、PlayerSaveSnapshotTest，并复用 Login payment-idempotency-expected.json 的 13 个 PaymentNotificationTest 方法。覆盖多字节/代理对 UTF-8、阈值相等与超过、独立维度、重复/回拨/并发预留、锁外告警、玩家轮换与全局输出有界、真实 Micrometer 固定基数、只读重复诊断和大小边界、大订单集原样封存、NOT_WRITTEN/UNKNOWN 不误报提交，以及探针/指标/告警异常不改变保存回执。

本任务未改数据库写入器、Mapper、支付履约逻辑或持久 JSON 格式；SQL 仍为按玩家主键的单行只读 SELECT，在原尺寸投影中显式 CONVERT(modules USING utf8mb4) 后再 OCTET_LENGTH，不假设列编码，不生成超限占位正文。容量测试使用真实序列化、状态机、MyBatis 参数绑定回归、受控事务结果与 JDBC 只读查询替身，无需数据库配置、DDL、业务数据或平台请求。没有重跑旧 DB IT，不宣称本批新增真实库冷读、数据库编码/磁盘大小、集群行为、平台验单或负载 SLA 验收；原真实库证据保留在来源任务。本任务的容量 critical 不是保存硬限额，绝不以诊断 INPUT_LIMIT 或容量预警拒绝已支付履约。

### S3C 审查修复验收（s3c-payment-capacity-review-fixes，T2）

本任务关联已 DONE 的容量监控任务，仅修复慢 observer 对保存回执的影响、墙钟跳变导致的限频静默，以及故障日志隔离边界。来源任务的报告、runner、expected 和验证历史保留；当前源码 expected 扩展新增六个方法。回执先完成，生产指标/告警与注入 observer 使用同一有界后台出口（单线程、一个排队样本，繁忙丢弃）；故障日志另有相同上界，日志自身失败不递归。确认容量仍按代次保留在 status，不改变 UNKNOWN、保存推进、幂等或已支付履约。

复跑使用新任务 `evidence/run-review-tests.ps1`，基于原 runner，每次在 `.ai-workspace/.runtime/payment-capacity-review-fixes/<随机ID>` 创建当前身份独占目录，Maven 堆上限 1024 MiB、测试 JVM 512 MiB。六模块真实 test-compile 后，按当前逐方法清单运行完整容量/支付/保存回归及 Login 13 方法，核对 fresh XML；失败、跳过、额外或遗漏均非零。新增闩锁覆盖 observer/metrics/真实生产告警日志阻塞时回执已完成、多代次及跨玩家仍推进、被丢弃任务与线程/队列上界、释放及异常后资源恢复；真实探针失败时故障日志阻塞/抛错也不能影响封存或保存。告警与故障限频按生产相同的单调经过毫秒契约注入，覆盖墙钟快进/回归、精确 elapsed 边界、满表回收及并发预留。本批不连接数据库/平台，不改数据库，不复跑旧 DB IT，不声称业务环境或负载 SLA 已验收。

等待辅助方法以 `nanoTime - start` 计算超时，不比较绝对截止读数；首次观察到就绪后直接返回，避免共享执行器状态在重复读取间变化导致即时误报。`s3c-payment-capacity-await-fix` 在既有回归方法中补充单次就绪、纳秒计数回绕和超时边界断言，保留原 140 方法清单与独立复跑入口。

### S3C 可信封存命令、恢复与独立验收

任务 `s3c-payment-trusted-archive`（T2）依据本次明确授权，只交付本地单玩家单订单封存与验证，原诊断兼容。先编译当前六模块并准备依赖，再显式调用（以下路径/ID由操作者替换）：

```powershell
./payment-archive.ps1 -Operation archive -PlayerId <正玩家ID> -OrderId <正订单ID> -OutputRoot <绝对私有归档根> -KeyFile <根外私有32字节key文件>
./payment-archive.ps1 -Operation verify -PlayerId <正玩家ID> -OrderId <正订单ID> -OutputRoot <同一归档根> -KeyFile <原始key文件>
```

archive 的连接配置仍仅从受限 S2_DB_CONFIG 读取，生产操作应使用只读账号；verify 不连接数据库、不需要 S2_DB_CONFIG。明确提供所有参数，无默认 key、扫描、分页或自动批量归档。根/文件名、V1 HMAC、key备份/轮换和信任边界见架构文档本批小节。归档根可由工具新建，父级必须已存在；已有根/key必须私有，不能通过修改他人ACL取得可读权限。独立 key 丢失后没有可信降级验证；相同权限管理员或 key 泄漏不在防护范围。

SEALED_AND_VERIFIED 表示新文件经 force 和冷读验证，VERIFIED 表示显式 verify 当前文件，VERIFIED_EXISTING 仅表示精确相同完整观察已存在；失败返回 FAILED_RETAIN_SOURCE / NOT_VERIFIED 和非零退出。摘要不包含原单号、正文、账号、凭据或key。新采集时间不同属于新内容；已有文件不覆盖，需 verify 历史观察。竞争可在原持有者退出后重试，但不得删除锁文件来绕过有效文件锁。半写、截断、冲突、force/冷读失败均应保留产物和原失败证据，由人工保全并调查；禁止盲目覆盖、自动重签或把旧内容当新成功。

部署只增加离线工具，不改 Apollo/schema/业务保存格式，也不启动线上清理；可停用新增 CLI，保留 key 和文件继续原证据保留。已有 PayItem 完成凭证的旧 JAR 回滚禁令仍有效，不能用本地归档代替源持久去重记录。平台真实性、事务终态、删除和补发权限仍未证明，所有回执 canDelete/canResend 固定 false。

独立执行入口为当前任务 `evidence/run-review-tests.ps1`，默认 All：真实六模块 test-compile、完整既有140方法（Game127/Login13）加15个归档单测，再运行Game旧三方法/新增两库端到端及Login四方法真实DB。Compile/Test/Database可分别由 Task Run 登记；只跳过 Git 元数据插件，不跳过编译或测试。fresh XML逐方法 expected 必须完整一致、无失败/错误/skip，额外或遗漏均失败。日志和XML保留本轮当前身份私有随机runtime，stdout摘要小于12000字符，旧证据不覆盖。子进程 JAVA_TOOL_OPTIONS 使用正确 file:/// URI；敏感日志不输出到摘要。

数据库阶段仅从 `http://test:9888/configs/game/default/jdbc` 和 `http://test:9888/configs/login/default/jdbc` 获取本轮配置，每字符按Java Properties Unicode转义，仅子进程S2_DB_CONFIG使用，finally删除凭据。每轮当前身份创建唯一私有runtime，测试key/归档仅临时夹具。新增端到端仅INSERT本轮唯一Game/Login fixture，真实只读采集→归档force/冷读→verify→新连接核对两库源行不变→带归属条件精确删除→fresh session冷查为空；不DDL、不改业务账号、不调用支付。清理证明来自本轮成功断言及日志实际计数，不复用旧五次清理。DB不可用真实失败，不用mock替代。

两库配置中无前缀键属于 Game，`login.*` 属于 Login；Login 独立回归优先读取 `login.*`，仅旧单库配置兼容无前缀键，不能将 Game 库上的执行计为 Login 验收。

归档单测独立核对磁盘字节和 JCA MAC，覆盖签名/内容/身份/结论篡改、缺 key/错 key/未知 version、错期望单号、重复键/超深/超大、同内容幂等与冲突不覆写、线程及进程锁竞争、半写/截断、write/force/read 故障、源数据保留、风险无删除/补发权限、真实 junction 路径边界与新 JVM stdout 脱敏。截至 S3C 仅容量观测及可验证封存完成；后续 S3D 交付同库持久去重及隔离 DB 验收，原 `payment-evidence-capacity-and-archive` 的平台独立终态和安全清理仍未完成，整体待办保持 PENDING。

### S3D 持久去重部署、回滚与验收

启动开关为 JVM `-Dgame.paymentDurableDedup=true`，默认false，`Config.paymentDurableDedupEnabled` 启动时读取一次，不是Apollo在线切换项。关闭时不解析新数据库表，保持现有V1热订单支付行为；缺表且未启用不会让默认部署的全部支付失败。启用后任一新表缺失、引擎/唯一约束不符或查询不可用均拒绝，不降级到旧V1或归档；热记录清理始终禁止。

生产发布必须按以下顺序执行：

1. 停止支付受理及所有Game旧写入器，等待或人工排除数据库在途旧事务，保全玩家快照与支付证据。确认每玩家只有一个节点持写权，业务池配置有界连接/读写超时；不能只凭进程退出推断数据库事务已结束。
2. 运维在目标Game库审查并执行 `game/src/main/resources/db/migration/payment-durable-dedup-v1.sql`；应用不自动DDL，本任务也不应用生产DDL。核对两个新表及`t_player_data`均为InnoDB，两张新表分别有order主键与SDK唯一键。生产支付账号对新表仅SELECT/INSERT，不授予覆盖/清理权限。表已存在时先审查真实结构，不盲目重复执行或DROP重建。
3. 停服更新六模块对应JAR，以显式JVM开关启动，定向验证新订单占位→奖励→同事务凭证/快照commit→回包，以及重复通知零副作用；既有V1无独立凭证的重试将保守拒绝，不应通过补发“修复”。本次不调用真实支付平台，平台验收仍待正式合同资料。
4. 保持全部PayItem和平台证据、归档key/文件与两张表；不得开启清理。对长期仅占位、UNKNOWN或PAYMENT_FROZEN订单告警并人工保全，不按超时接管/自动补偿/自动解冻。

迁移尚未启用、没有任何新占位时可保持开关关闭使用当前V1功能；只要已有新占位，就不能直接关开关或回滚到忽略该凭证的JAR，因为部分奖励/迟到提交可能存在。回滚前须停服排除旧事务，逐单核对并制定兼容恢复方案；未来如果移出热PayItem，忽略新表的旧JAR更不能安全运行。PayItem持久字段沿用当前HEAD的V1格式，但早于V1的JAR仍可能因新字段拒绝反序列化；格式能读不等于新去重语义可回滚。两张表永不作为自动回滚清理目标。

新任务的独立入口为 `tasks/s3d-payment-durable-dedup/evidence/run-review-tests.ps1`（相对本功能规格目录），默认All，保留Compile/Test/Database分组。真实编译六模块；Game既有142方法+新增4方法、Login既有13方法；DB保留Game4/Login4方法并新增14个Game方法。逐方法expected清单仅扩展新文件，不删旧目标；freshXML核对完整集合、每项恰好一次，失败/错误/skip/遗漏或额外项均非零。摘要不超过12000字符，分组列出实际Game/Login/DB计数和清理证据；不以BUILD SUCCESS代替方法验证。

DB配置每轮只从Apollo的Game/Login实时只读端点获取，不读取旧缓存；当前执行身份创建随机私有runtime，配置逐字符JavaProperties转义，finally删除；子JVM日志使用file:/// URI。只为本轮在Game库创建`s3d_claim_<32位随机十六进制>`和`s3d_receipt_<同后缀>`，结构直接来源迁移SQL，标识符严格校验，生产固定表名不受业务输入控制。每张测试表有本轮comment身份，只有实际创建并冷查确认该身份后才DROP。先等待本轮写线程退出，再按精确id/uid/account/device条件清理自己的Game行，旧回归按原精确条件清理Login行；新连接查玩家为空、information_schema查两测试表为空，保存清理计数。绝不对现有业务表应用生产DDL或清理其他账号/表。

新增真实库覆盖 JDBC/Spring 原子提交与回滚、凭证 INSERT 失败/唯一键冲突导致聚合不提交、同单/SDK 不同参数竞争、重复通知、真实移走测试热记录后的冷加载、错身份/各购买字段/版本/摘要、缺表/缺唯一约束/查询故障、保存超时后迟到 commit、真实 commit 后确认丢失、部分奖励隔离和离线原清理顺序。仅测试 fixture 模拟未来无热记录，不增加生产清理能力；独立验收应重新运行本任务 `All` 入口并核对全量通过。

### S3D 逐单只读预检操作与验收

在六模块当前类完成编译后，显式提供`S2_DB_CONFIG`受限本地JavaProperties文件（Game键dburl/dbuser/dbpass/driverClassName，Login加login.前缀），运行：

```powershell
./payment-cleanup-preflight.ps1 -PlayerId <正long> -OrderId <正long> -ArchiveRoot <绝对私有归档根> -KeyFile <根外私有32字节key>
```

没有默认玩家、订单、key或扫描范围，不支持force、assume-terminal、自动恢复或backfill。CLI只输出固定安全状态/原因码/所需下一步证据，不输出玩家或订单ID、SDK单号、购买正文、连接/路径/key/凭据。退出码1为INVALID_INPUT；2为局部一致但缺外部证明（BLOCKED_EXTERNAL_PROOFS）；3为局部证据阻断，包括认证/配置/读取故障（BLOCKED_LOCAL_EVIDENCE）。没有退出0或“已清理”结果，localEvidenceReady不能用作删除/补发条件。ARCHIVE_AUTHENTICATION_OR_FORMAT_FAILED先核对归档/key/私有权限并保全原产物；CURRENT_*或DURABLE_*按具体冲突调查当前冷记录及事务，不自动改写；外部阻断需要未来接入可信平台终态与写入者隔离/UNKNOWN排除证据，不能由布尔开关代替。

新任务独立入口为 `tasks/s3d-payment-safe-cleanup-gate/evidence/run-review-tests.ps1`，默认 All，也可按 Compile/Test/Database 分组定位失败。完整保留当前 181 方法（Game 146、Login 13、DB Game 18/Login 4），新增 10 个定向单测和 3 个真实库方法。Fresh JUnit 逐方法完整集合恰好一次，失败/错误/skip/缺失/额外项均失败；摘要小于 12000 字符，完整日志和报告留本轮私有 runtime，失败尝试保留。输入快照覆盖六模块源码/资源/POM、相关文档和 runner，验收时应重跑同一 `All` 入口并核对输入快照未变。

每次DB阶段从`http://test:9888/configs/game/default/jdbc`及`/configs/login/default/jdbc`只读获取新配置，私有随机目录、逐字符JavaProperties Unicode转义，finally删除并核对配置不存在。新测试每个Case精确创建一Game/一Login行、随机带本轮comment的两表；真实JDBC/Spring聚合和receipt事务commit后，以认证归档加实际CLI/reader预检，冷查Game/Login/claim/receipt全部字段和归档字节前后相等。故障仅修改本轮fixture，缺表仅DROP实际创建且comment再次冷查匹配的精确测试表。清理等待写线程退出，精确身份删除fixture并新连接查行/表不存在，销毁测试key/归档目录。测试产物销毁不代表生产归档/凭证可以删除。

`fixture-plan.json`按每个实际DB方法的构造位置和循环结构记录Case数量及对应冷空标记，runner按方法清单求和，不猜测固定合计：旧Game归档4方法中两方法各遍历JDBC/Spring，两方法各1组；旧durable14方法中三方法各2组，其余各1组；旧Login4方法中affectedRows循环一方法2组，其余各1组；本批CLI完成方法遍历JDBC/Spring为2组，损坏方法和缺失/风险方法各1组。新夹具还核对OPENED/COLD_EMPTY随机后缀逐一对应且唯一，验证精确清理和配置删除。

本批只部署离线只读工具，生产durable开关仍默认OFF，迁移SQL保持opt-in且不执行生产DDL，不启动线上服务。可停用新CLI作为回退；不回滚已有durable去重语义，不删除热项/归档/claim/receipt。缺失可信平台终态及旧写入者隔离证明仍是后续独立任务，容量治理/归档整体不能宣称完成。

### S3E 启用结构预检与隔离验收

六模块当前版本编译后，还须准备Game运行依赖：使用正常发布包的对应game/target/lib，或在根目录显式执行 `mvn -B -pl game '-DskipTests' dependency:copy-dependencies`。test-compile本身不会生成该目录；本轮编译的game/core/protocol/util类目录在classpath先于依赖JAR。运维提供私有临时Java Properties（Game dburl/dbuser/dbpass/driverClassName）并设置S2_DB_CONFIG，执行 `./payment-enable-preflight.ps1`。缺少当前类或依赖目录时脚本返回BLOCKED_RUNTIME/退出3并提示准备依赖；存在目录不证明JAR版本或配置正确。工具不自动编译/下载依赖、不自动从Apollo取配置、不接受force/表名/开关参数，只读固定Game三表元数据；没有生产DDL或激活入口。日志配置通过file:/// URI加载。stdout只含版本、元数据观察签名、固定局部原因和外部阻断，不输出连接/正文/原始默认值。

退出1表示非法参数，退出3表示结构/配置/读取/总超时阻断，退出2表示局部结构就绪但外部前提仍未证明；无退出0，不可把localSchemaReady或signature作为自动启用条件。按角色/字段/REQUIRE规则与权威V1 DDL核对，不自动ALTER/DROP修复。字段精确契约、允许的显示宽度/字段顺序/非唯一索引容差、并发DDL窗口和元数据权限可见性边界见架构S3E。部署时须独立确认平台SDK全局命名空间、每玩家单写与旧事务隔离、历史凭证覆盖，并冻结DDL；不自动迁移历史凭证、不重放历史奖励、不解冻UNKNOWN。默认仍OFF。

本任务验证入口为 `tasks/s3e-payment-enable-preflight/evidence/run-review-tests.ps1`（相对本功能规格目录），All/Compile/Test/Database模式。沿用S3D逐方法fresh JUnit及精确夹具冷查门禁，保留原194方法并加入schema/CLI故障单测及真实DDL变体。Compile执行六模块clean test-compile；Test/Database显式执行Surefire3.5.2并要求完整expected集合各一次、无skip/失败/额外/缺失。All在同轮完成全部步骤；私有runtime保存原始日志、XML、输入前后哈希、gate.json，stdout为小于12000字符的摘要。失败轮次保留，不以其他成功覆盖。

Database 每轮从 Apollo Game/Login 只读 HTTP 端点 `-NoProxy` 获取新配置；只在当前身份专有 ACL 目录写逐字符 Unicode 转义 Properties，finally 删除。保留旧真实双库验收，新 schema 测试只操作唯一玩家以及随机 32hex 后缀、实际创建集合+持久 comment 双重确认的测试表。新连接冷查两表无 claim/receipt、玩家聚合无变更来断言错误 schema 在占位/奖励/Charge/保存前拒绝；finally 先等写线程结束，再精确删除本轮行并确认所有本轮表不存在。玩家非事务引擎用自有影子表测试，不 ALTER 生产 `t_player_data`；生产新表不执行 DDL。独立验收需重跑本任务 `All` 入口并核对源码与输入快照哈希一致。

### Task4 数据库超时验收与操作限制

专用支付/保存入口的预算、数据源支持范围、Spring绑定、状态恢复和UNKNOWN分类见架构文档Task4；禁止对一次请求修改共享Hikari/MyBatis配置。生产Game的Hikari60000ms获取、默认5000ms验证合计纳入默认65000ms acquire预算；网络/查询5000ms、总75000ms，均为本地`payment.db.*Millis`参数。物理建连、DNS/认证及平台停顿的限制必须如实报告；当前Apollo没有显式物理connect/socket上限，此任务未写远程配置。若要求进程/OS级硬截止，需要独立部署隔离方案，不能用取消线程伪造完成。

本轮期望清单`payment-database-timeout-expected.json`和`payment-database-timeout-database-expected.json`在完整既有213方法上新增连接能力、预算/URL优先级、恢复异常、隐式提交防护、schema共用预算，以及实际Hikari饱和、实际MySQL行锁socket超时、慢SQL查询超时；JDBC和Spring分别验证确认丢失、迟到真实commit与恢复/废弃。提交确认丢失、迟到屏障和驱动不支持/恢复失败是实际自有fixture连接外的故障注入，不冒称真实断网。真实socket/查询计时分类与实测毫秒单独保留在私有日志。

新任务evidence/run-review-tests.ps1支持Compile/Test/Database/All，fresh六模块clean test-compile、逐方法无缺失/额外/重复/跳过、双库实际回归；所有源码/资源/POM及期望清单和runner前后hash必须一致。每轮新取Apollo jdbc，当前身份ACL私有目录、Java Properties每字符转义、Log4j file:/// URI、finally删除S2_DB_CONFIG；不打印配置。新表仅随机32hex后缀及所有权注释；按精确主键/owner清理后新连接冷查为空并确认表不存在，保留失败证据，不删热订单/归档/claim/receipt。原194及Task3的19方法不删减，旧runner不弱化。

独立验收需运行本任务 `run-review-tests.ps1 -Mode All` 并核对 `source/spec/input` 哈希一致；如需后续修复应登记新的关联任务，提交时按仓库区分并排除 `.ai-workspace/.runtime/` 产物。

### Task5：历史订单只读盘点操作与验收

仅在已有单独明确业务清单授权时执行。先把Game/Login的dburl/dbuser/dbpass/driverClassName写入当前原生执行身份私有、无链接的绝对路径Properties文件；Login键加login.。配置仅通过S2_DB_CONFIG指定，不写入仓库，不输出值，字符按Java Properties规则转义，使用后finally删除。工具不会自行获取远程配置。示例参数仅说明格式：`./payment-legacy-inventory.ps1 -Pairs '41:91,42:92' -PageSize 1 -Cursor '-'`；示例不授予这些账号的查询权限。保留聚合报告nextCursor，再以同一清单继续；清单重排允许，增减或更换输入须从`-`开始。无默认清单、无全库选项、无自定义表名/SQL/归档输入或明细导出。

报告PAGE_COMPLETE/退出0只说明本页全部输入已分类，PARTIAL_READ/退出2须保留此前分类并从报告nextCursor重试，INVALID_INPUT/退出1未连接数据库。scopeExhausted是输入位置覆盖，不等于完整历史覆盖；observed不包括未读到的失败项，unreadableAttempts/dependencyOpenFailed分别说明行尝试和打开依赖失败；缺行/缺依赖分类可完成观察，查询错误不等于缺行。每页最多10项（清单最多100项），整轮12秒合作式预算；2秒连接/网络/查询，正文4MiB/字段、Login单号764字节；慢库可能只能完成部分页，JDBC退出及清理后才返回，操作系统/驱动/DNS不具备硬实时保证。多库/多连接/多页不是原子快照，禁止据此自动生成操作资格。

分类、严格解析和无授权语义见架构Task5段。先核对Game/Login身份、完整V1或历史风险及依赖，再由人工收集独立平台终态、旧写入者/UNKNOWN隔离、实际可信证据；需要归档时使用原payment-archive的HMAC流程。即使完整当前receipt/claim，canDelete/canResend/canBackfill仍恒false；禁止自动回填/重放/删除/退款/解冻。本任务没有执行真实业务清单，只对本轮新建Game/Login随机归属表验收。

Task5 独立验收入口：`pwsh -NoProfile -File ./.ai-workspace/specs/features/ServerArchitectureAndAgentReadiness/tasks/s3e-payment-legacy-inventory/evidence/run-review-tests.ps1 -Mode All`。Compile 为六模块 fresh clean test-compile；Test 保留既有 181 Game + 13 Login 并加入 13 个新单元方法；Database 保留既有 37 Game + 4 Login 并加入 7 个真实两库方法（总 255）。逐方法 expected 严格核对一一执行、无额外/缺失/重复/skip；新 fixture 只创建本轮 32hex 表，核对 comment 后清理并新连接查不存在，源全字段字节前后相等，实际子 JVM 验证退出码和 Player/PlayerModule 不初始化。每轮 fresh Apollo 只读配置仅供隔离验收，私有日志/配置 finally 删除/输入哈希稳定检查沿用 Task4 安全方法；原失败证据保留，不能以旧成功替代，提交时排除 `.ai-workspace/.runtime/` 产物。

### S4：请求计时诊断与可复现基线

默认不开启，受控实例使用本地JVM参数 `-Dgame.timing.enabled=true`；无需Apollo变更、共享池变更或监控服务。通过现有 `TaskExecutorService.getMonitor().collectStatistics().timings` 或 `TimingMetrics.current().snapshot()` 在监控线程拉取，保留stage/route/outcome/count/dropped/over10ms/maxNanos与p50/p95/p99UpperMicros及窗口enabled/dropped/failures。精确计数、桶上界近似分位数、争用丢样偏差、重置、相邻协议响应和异步边界限制见架构S4。诊断读取者可慢，但生产记录者只tryLock丢样；不把快照拉取加到每个业务请求。`TimingMetrics.reset(boolean)` 是本地新窗口操作，不清除在途业务；采集全量可比阶段时先自然排空自有负载，不暂停共享业务玩家。

独立验收门禁入口：

```powershell
pwsh -NoProfile -File ./.ai-workspace/specs/features/ServerArchitectureAndAgentReadiness/tasks/s4-request-timing-baseline/evidence/run-review-tests.ps1 -Mode All
```

All执行六模块fresh clean test-compile、完整原255逐方法服务端回归（Game194/Login13/DatabaseGame44/DatabaseLogin4）、六个core历史清单去重并将类目标展开成精确方法、新计时用例、新真实保存计时冷读及新JVM基准。Compile/Test/Database/Benchmark和开发定向Metrics可分别定位；每次使用新私有ACL目录，不复用成功XML或配置。JUnit实际方法必须各一次，无重复/缺失/额外/skip；不默认跑无关Redis/网络集成测试。测试条件仅判定真实结果、次数、分位数计算、资源回收与输入稳定，不以宿主延迟达标率作随机门禁。stdout小于12000字符，原始日志/XML/完整benchmark.json/gate.json留私有runtime；失败证据保留。同批次范围扩展须以Reason登记。

Database仍每轮从Game/Login Apollo readonly端点-NoProxy新取jdbc；每字符Java Properties Unicode转义，S2_DB_CONFIG只指本轮当前身份专有目录中的文件，finally删除并报告configRemoved。保留旧支付两库所有回归与冷空计划；新增计时库验收复用独有玩家主键、生产模块序列化/Writer/Mapper，真实提交后新会话核对正文，精确归属条件清理并冷查为空。新增方法不建表；旧测试只创建本轮随机32hex后缀和所有权注释表，按原守卫验证后回收。生产命名新表缺失为默认关闭状态，绝不创建/启用，不删历史热单、归档、claim/receipt、不补偿/回填/解冻或调用平台。

Benchmark模式执行 `RequestTimingBenchmarkTest.freshJvmProducesCompleteRecomputableMatrix`，它启动 `java -Xmx512m -cp <本轮测试classpath> cn.game.core.execute.RequestTimingBenchmark <本轮私有绝对路径>/benchmark.json`。报告包含JDK/VM/OS/CPU、执行类哈希、seed、路由/载荷/实体分布/池大小、预热与测量各窗口、完整原始纳秒、exact nearest-rank p50/p95/p99、>10ms比例、错误数和清理状态；gate同时记录全部源码/资源/POM/脚本输入前后哈希、sourceHash和当前六模块构建输出buildHash。两种哈希范围不同，不能互换。原始样本包含Protobuf解析、独占客户端夹具建立、邮箱及响应验证和调用线程唤醒；生产REQUEST钩子从已解码入口起算到写入Future完成，数值不要求相同或可相加。

measurementScope固定为IN_PROCESS_REAL_MAILBOX_PROTOBUF_GAMECLIENT_SYNTHETIC_TRANSPORT。传输替身不测真实WebSocket/内核往返，未运行authenticated deployed Game/Login，不加载业务用户；DB/RPC/快照工作不在空心跳基准内，报告这些样本为0。完整部署基线需要实际加载新代码的隔离实例、归属明确的测试账号及网络/依赖环境；本次不启动共享实例或对实际用户压测。OFF/ON仅保存观测，没有校准因果开销或速度提升；热点队列和分散并发遥测争用是本次可观察限制，保存/支付路径仍可能占邮箱数十秒，不能宣称普遍10ms。

独立验收需重跑上述 `All` 门禁并核对源码、规格与输入快照哈希一致；如需后续修复应新建关联任务，提交时按所属仓库分别提交并排除 `.ai-workspace/.runtime/` 等运行态产物。