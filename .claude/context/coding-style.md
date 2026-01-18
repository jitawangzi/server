# Java 编码规范 (For Implementation Engine)

## 1. Proto 交互规范
- **DTO 转换**: 禁止在 `Controller` 或 `Service` 层直接返回 `Proto` 对象。必须创建对应的 `POJO` (DTO)，并使用 `Converter` 将 `Proto` 对象转换为 `POJO`。
- **原因**: 解耦内部领域模型与外部接口协议，防止 Proto 变更污染业务逻辑。

## 2. 异常处理规范
- **业务异常**: 所有业务逻辑错误（如“金币不足”、“等级不够”）必须抛出 `BaseBizException` 或其子类。
- **推荐写法**: 使用 `player.fail(ErrorMsgEnum.xxx)`，它会自动抛出异常并中断流程，由最上层 Handler 捕获并返回错误码给客户端。
- **禁止写法**: 禁止在业务代码中捕获 `BaseBizException` 后“吞掉”异常，除非是为了做特殊的补偿逻辑。
- **日志记录**: 捕获未知异常时，日志必须包含 `exception.getMessage()` 和完整的堆栈信息。

## 3. 核心 API 使用规范
- **时间获取**:
  - 禁止: `System.currentTimeMillis()`
  - 必须: `DateUtil.currentTimeMillis()` 或 `DateUtil.currentTimeSeconds()`
- **随机数**:
  - 必须: `cn.game.util.Rnd` 工具类
- **日志输出**:
  - 禁止: `System.out.println`
  - 必须: `log.info()`, `log.error()`, `log.debug()`
  - 要求: 尽量在日志中包含 `playerId` 以便追踪。

## 4. 资源操作规范 (PlayerHelper)
- **增加资源**: 使用 `PlayerHelper.addResources(player, itemId, count, OpType.XXX, true)`。
  - 参数说明: `notify=true` 会自动将奖励放入 `Response` 推送给客户端。
- **扣除资源**: 使用 `PlayerHelper.delResources(player, costId, costNum, OpType.XXX)`。
  - 特性: 如果资源不足，该方法会自动抛出异常，无需手动 `if (count < need)` 检查。
- **检查资源**: 仅检查不扣除时，使用 `PlayerHelper.isEnough(...)`。

## 5. 注释规范
- **JavaDoc**: 所有 `public` 方法必须编写 JavaDoc，说明参数约束、返回值和可能抛出的异常。
- **行内注释**: 避免“显而易见”的注释（如 `i++ // i加1`），注释应解释“为什么这么做”而非“做了什么”。
