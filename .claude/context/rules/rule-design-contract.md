# SKILL: Compile Design Contract (编译设计契约) (05_design_contract.md)

## 技能标识 (Skill ID)
SKILL_ID: 14_compile_design_contract
VERSION: 0.2
STAGE: DESIGN (stage gate into DEV/TEST) (设计 [进入开发/测试的关卡])

## 意图 (Intent)
冻结一份简短的、权威的“契约”，后续阶段将加载此契约，而不是重新阅读冗长的讨论或规则文档。

## 必需上下文包 (必须加载)
- _common/ctx/ctx_design.md

## 输入 (运行前必须存在)
- features/<feature>/02_biz_spec.md
- features/<feature>/03_logic_instructions.md
- 协议定义 (.proto 或 03_protocol_design.md)
- features/<feature>/04_test_plan.md
- _common/context_brief.md

## 输出 (必须创建/更新)
- features/<feature>/05_design_contract.md

## 允许的修改 (硬性约束)
- 允许：
  - features/<feature>/05_design_contract.md
- 禁止：
  - 任何代码更改
  - 任何 proto 更改 (此技能仅总结/冻结)

## 输出要求 (05_design_contract.md 必须简短：1-2 页)
1) 范围 (Scope)
   - 范围内 / 范围外
2) 外部接口索引 (External Interface Index)
   - 涉及的端点/消息
   - 消息 ID (如果适用)
   - 关键请求/响应字段
3) 错误码 (Error Codes)
   - 表格：条件 -> 错误码
4) 核心流程 (Core Flows) (指令式，而非散文)
   - 每个端点的项目符号/编号步骤
5) 不变量 (Invariants) (必须保持)
   - 状态、资源、幂等性、并发规则
6) 允许修改范围 (Allowed Modification Scope)
   - 开发/测试阶段允许触及的模块/文件
7) 验收检查清单 (Acceptance Checklist)
   - 构建命令
   - 需要通过的测试 (来自测试计划)

## 验收 / 完成标准
- 内容控制在 1-2 页内 (首选简洁的表格)。
- 开发/测试阶段仅凭此契约 + 代码库 + 测试计划即可执行。

---

## PROMPT (copy/paste)
你是一个执行 Agent。

阅读：
- _common/ctx/ctx_design.md
- _common/context_brief.md
- features/<feature>/02_biz_spec.md
- features/<feature>/03_logic_instructions.md
- 协议定义
- features/<feature>/04_test_plan.md

任务：
创建/更新 features/<feature>/05_design_contract.md 作为一份简短的 1-2 页权威契约，供后续开发/测试阶段使用。

约束：
- 不要修改代码或 proto。
- 优先使用表格和编号步骤。

交付：
- 05_design_contract.md 的完整内容
- 在开发开始前必须由人类做出的任何剩余决策列表 (如果有)
