# SKILL: Design Logic Instructions (Implementation-Ready Pseudocode) (设计逻辑指令 [可实施的伪代码])

## 技能标识 (Skill ID)
SKILL_ID: 12_design_logic_instructions
VERSION: 0.1
STAGE: DESIGN (设计)

## 意图 (Intent)
生成与现有架构和 API 边界一致的、可实施的逐步逻辑指令。

## 必需上下文包 (必须加载)
- .claude/context/ai-coding.md

## 输入 (运行前必须存在)
- specs/features/<feature>/01_server_rules.md
- specs/features/<feature>/02_config_design.md
- specs/features/<feature>/03_protocol_design.md
- .claude/context/ai-coding.md
- .claude/context/api-whitelist.md

## 输出 (必须创建/更新)
- specs/features/<feature>/04_logic_instructions.md
需包含类的设计，以及一些接口和方法的定义，及简单实现。 

## 约束：
- 使用 .claude/context/api-whitelist.md；如果缺失，列出“所需的新助手/API”。
- 风险/模糊点的简短列表 (如果有)

## 逻辑设计 (指令化描述)
* **严禁**使用模糊的自然语言（如“检查一下钱够不够”）。
* **必须**使用**指令化伪代码**，明确调用哪个 Manager 或 Helper。
* **示例**：
    > **Function**: `upgrade()`
    > 1. **Check**: `PlayerHelper.checkResource(player, 1001, cost)`
    > 2. **Action**: `PlayerHelper.delResources(player, 1001, cost, OpType.Upgrade)` (Allow exception throw)
    > 3. **Logic**: `data.setLevel(lv + 1)`
    > 4. **Response**: Send `UpgradeResponse`

---

## 允许的修改 (硬性约束)
- 允许：
  - specs/features/<feature>/04_logic_instructions.md
- 禁止：
  - 任何代码或 proto 更改

## 输出要求 (04_logic_instructions.md)
对于每个协议/处理程序 (proto/handler)：
- 前置条件和验证
- 授权检查
- 数据加载顺序
- 核心状态转换 (明确)
- 错误处理 (何时返回哪个错误码)
- 幂等性策略 (如果需要)
- 并发/锁定策略 (如果需要)
- 副作用 (事件/日志/指标)
- 后置条件 (不变量)

包含：
- "禁止事项 (Do NOT do)" 列表 (此功能的常见陷阱)

## 验收 / 完成标准
- 每一条业务规则都由至少一个步骤实现。
- 没有任何步骤需要批准边界之外的 API；如果需要，列出所需的“新 API”。
