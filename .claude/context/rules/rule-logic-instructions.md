# SKILL: Design Logic Instructions (Implementation-Ready Pseudocode) (设计逻辑指令 [可实施的伪代码])

## 技能标识 (Skill ID)
SKILL_ID: 12_design_logic_instructions
VERSION: 0.1
STAGE: DESIGN (设计)

## 意图 (Intent)
生成与现有架构和 API 边界一致的、可实施的逐步逻辑指令。

## 必需上下文包 (必须加载)
- _common/ctx/ctx_design.md

## 输入 (运行前必须存在)
- features/<feature>/02_biz_spec.md
- features/<feature>/03_protocol_design.md 或相关 .proto (如果协议已更新)
- _common/project_context.md
- _common/context_brief.md

## 输出 (必须创建/更新)
- features/<feature>/03_logic_instructions.md
- (可选) features/<feature>/03_design_full.md (链接到逻辑指令)

## 允许的修改 (硬性约束)
- 允许：
  - features/<feature>/03_logic_instructions.md
  - features/<feature>/03_design_full.md (仅链接/部分)
- 禁止：
  - 任何代码或 proto 更改

## 输出要求 (03_logic_instructions.md)
对于每个端点/处理程序 (endpoint/handler)：
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

---

## PROMPT (copy/paste)
你是一个执行 Agent。

阅读：
- _common/ctx/ctx_design.md
- _common/project_context.md
- _common/context_brief.md
- features/<feature>/02_biz_spec.md
- 协议定义 (03_protocol_design.md 或 .proto)

任务：
创建 features/<feature>/03_logic_instructions.md，包含每个处理程序/端点的可实施逐步逻辑。

约束：
- 不要更改任何代码或 proto。
- 使用 project_context.md 中的现有模块边界；如果缺失，列出“所需的新助手/API”。

交付：
- 03_logic_instructions.md 的完整内容
- 风险/模糊点的简短列表 (如果有)
