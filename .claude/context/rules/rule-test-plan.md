# SKILL: Create Test Plan (创建测试计划) (05_test_plan.md)

## 技能标识 (Skill ID)
SKILL_ID: 13_plan_tests
VERSION: 0.1
STAGE: DESIGN (设计)

## 意图 (Intent)
创建一个完整的测试计划，使 Agent 无需进一步解释即可实施。

## 必需上下文包 (必须加载)
- context/client-test.md

## 输入 (运行前必须存在)
- features/<feature>/01_server_rules.md
- 协议定义 (.proto 或 03_protocol_design.md)

## 输出 (必须创建/更新)
- features/<feature>/05_test_plan.md

## 允许的修改 (硬性约束)
- 允许：
  - features/<feature>/05_test_plan.md
- 禁止：
  - 任何代码/proto 更改

## 输出要求 (04_test_plan.md)
- 测试环境假设 (Fixtures, 种子数据)
- 测试矩阵涵盖：
  - 快乐路径 (Happy path)
  - 每一条业务规则
  - 每一个错误码 / 故障模式
  - 边界情况 (Boundaries)
  - 幂等性 (如果适用)
  - 并发性 (如果适用)
- 对于每个测试用例：
  - 名称/ID
  - 前置条件
  - 请求
  - 预期响应 (包括错误码)
  - 预期状态变更
  - 清理 (如果需要)

## 验收 / 完成标准
- 每条编号的业务规则至少有一个测试。
- 每个错误码至少有一个测试。
- 测试用例无需猜测即可实施。

