# SKILL: Generate/Update Tests From Test Plan (根据测试计划生成/更新测试)

## 技能标识 (Skill ID)
SKILL_ID: 30_generate_tests_from_plan
VERSION: 0.2
STAGE: TEST (测试)

## 意图 (Intent)
完全按照测试计划中的规定实施测试，使用最终协议和客户端约定。

## 必需上下文包 (必须加载)
- context/client-test.md

## 输入 (必须存在)
- features/<feature>/05_test_plan.md
- features/<feature>/06_design_contract.md
- 仓库中的最终 .proto 文件
- 仓库中已实施的服务端代码

## 输出
- 客户端测试java class
- (可选) features/<feature>/10_test_impl_report.md

## 允许的修改 (硬性约束)
- 允许：
  - 仅测试文件 (ClientBaseScenarioTest的子类)
  - features/<feature>/10_test_impl_report.md
- 禁止：
  - 生产服务端代码更改 (除非协调员明确说明这是修复周期)
  - 协议更改

## 执行检查清单
1) 阅读 client-test.md + 05_test_plan.md。
2) 逐个实施测试用例；保持可追溯性映射。
3) 运行测试。
4) 在 10_test_impl_report.md 中记录结果和任何缺失。

## 验收 / 完成标准
- 测试可编译并运行。
- 所有计划的测试用例都已实施，并最终没有错误。
