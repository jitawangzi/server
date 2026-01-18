# 项目概览 (Project Summary)

## 1. 项目背景
本项目是一个基于 Java (JDK 21) 的高性能游戏服务端。核心目标是提供**安全**、**一致**与**高效**的开发体验。

## 2. 核心目录结构
- **`game/`**: 主游戏服（包含 `Handler` 协议入口、`Module` 业务逻辑、配置管理）。
- **`protocol/`**: 存放 Protobuf 定义与生成产物。
- **`core/` & `util/`**: 通用基础能力 (`AsyncUtils`, `DateUtil`, `Rnd`)。
- **`simulationclient/`**: 模拟客户端，用于功能测试和压测。

## 3. 关键开发原则
- **线程模型**: 基于 `objectId` (playerId) 分配的 JDK21 虚拟线程模型。同一玩家的逻辑串行执行，无需锁。
- **异步转同步**: 严禁使用 `CompletableFuture` 回调链，必须使用 `AsyncUtils.await(...)` 将异步转同步，以保持代码线性逻辑。
- **持久化**: 大多数 Module 数据由框架定时全量保存，无需手动 `save()`。特殊情况（如邮件）使用 `DbEntity` 接口手动操作。
- **错误处理**: 业务逻辑错误推荐使用 `player.fail(ErrorMsgEnum.xxx)` 抛出异常，由框架统一处理。

## 4. 常用工具 (API Whitelist)
- **时间**: 必须用 `DateUtil.currentTimeMillis()`，禁用 `System`。
- **随机**: 必须用 `Rnd` 类。
- **日志**: 使用 `log.info/error`，禁用 `System.out.println`。