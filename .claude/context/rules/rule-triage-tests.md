# SKILL: Run Tests and Triage Failures (运行测试并分类失败)

## 技能标识 (Skill ID)
SKILL_ID: 31_run_tests_and_triage
VERSION: 0.1
STAGE: TEST (测试)

## 意图 (Intent)
运行测试套件，捕获失败，并分类根本原因以进行有效修复。

## 必需上下文包 (必须加载)
- _common/ctx/ctx_test.md

## 输入
- features/<feature>/04_test_plan.md
- features/<feature>/05_design_contract.md
- 测试执行输出 (控制台日志)
- 仓库状态

## 输出
- features/<feature>/09_test_run_report.md

## 允许的修改 (硬性约束)
- 允许：
  - features/<feature>/09_test_run_report.md
- 禁止：
  - 任何代码更改

## 分类类别 (用于报告) (Triage Categories)
- ENV: 环境/基础设施/配置问题
- TEST: 测试 bug 或不稳定的测试 (flaky test)
- IMPL: 实施 bug
- CONTRACT: 契约/设计不匹配或规格缺失
- PROTO: 协议不匹配
- DATA: 测试数据/Fixtures 问题

## 验收 / 完成标准
- 报告包含：
  - 运行的命令
  - 失败测试列表
  - 每个失败的分类
  - 下一步行动 (接下来运行哪个技能)

---

## PROMPT (copy/paste)
你是一个执行 Agent。

阅读：
- _common/ctx/ctx_test.md
- features/<feature>/04_test_plan.md
- features/<feature>/05_design_contract.md

任务：
运行所需的测试 (根据项目约定) 并创建 features/<feature>/09_test_run_report.md。

约束：
- 不要修改代码。
- 将每个失败分类为：ENV/TEST/IMPL/CONTRACT/PROTO/DATA 之一，并建议下一步行动。

交付：
- 完整的 09_test_run_report.md 内容
