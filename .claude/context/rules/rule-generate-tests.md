# SKILL: Generate/Update Tests From Test Plan (根据测试计划生成/更新测试)

## 技能标识 (Skill ID)
SKILL_ID: 30_generate_tests_from_plan
VERSION: 0.2
STAGE: TEST (测试)

## 意图 (Intent)
完全按照测试计划中的规定实施测试，使用最终协议和客户端约定。

## 必需上下文包 (必须加载)
- _common/ctx/ctx_test.md

## 输入 (必须存在)
- _common/client.md
- _common/context_brief.md
- features/<feature>/04_test_plan.md
- features/<feature>/05_design_contract.md
- 仓库中的最终 .proto 文件
- 仓库中已实施的服务端代码

## 输出
- 客户端/服务端测试文件 (根据你的项目约定)
- (可选) features/<feature>/08_test_impl_report.md

## 允许的修改 (硬性约束)
- 允许：
  - 仅测试文件 (由协调员提供的明确列表或目录模式)
  - features/<feature>/08_test_impl_report.md
- 禁止：
  - 生产服务端代码更改 (除非协调员明确说明这是修复周期)
  - 协议更改

## 执行检查清单
1) 阅读 ctx_test + client.md + 测试计划。
2) 逐个实施测试用例；保持可追溯性映射。
3) 运行测试 (或提供命令)。
4) 在 08_test_impl_report.md 中记录结果和任何缺失。

## 验收 / 完成标准
- 所有计划的测试用例都已实施。
- 测试可编译并运行 (或有明确的运行说明)。

---

## PROMPT (copy/paste)
你是一个执行 Agent。

阅读：
- _common/ctx/ctx_test.md
- _common/client.md
- _common/context_brief.md
- features/<feature>/04_test_plan.md
- features/<feature>/05_design_contract.md
- 最终 .proto 文件

任务：
根据 features/<feature>/04_test_plan.md 实施测试。

约束：
- 仅修改测试文件 (经协调员允许) 并可选地编写 features/<feature>/08_test_impl_report.md。
- 在此技能中不要更改生产代码或协议。

交付：
- 测试的确切文件更改
- 可追溯性列表：TestCaseID -> 测试方法/类
- 如何运行测试 + 结果 (如果可用)
