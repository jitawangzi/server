# SKILL: Run Tests and Triage Failures (运行测试并分类失败)

## 技能标识 (Skill ID)
SKILL_ID: 31_run_tests_and_triage
VERSION: 0.2
STAGE: TEST (测试)

## 意图 (Intent)
运行测试套件，捕获失败，并分类根本原因以进行有效修复。

## 必需上下文包 (必须加载)
- .claude/context/client-test.md (用于理解测试日志格式)
- features/<feature>/06_design_contract.md

## 输入
- features/<feature>/05_test_plan.md
- features/<feature>/06_design_contract.md
- 测试执行输出 (控制台日志)
- 仓库状态

## 输出
- features/<feature>/11_test_run_report.md

## 允许的修改 (硬性约束)
- 允许：
  - features/<feature>/11_test_run_report.md
  - 仅当分类为 TEST 时：允许修改测试类文件以修复测试 bug。
- 禁止：
  - **严禁修改业务逻辑代码 (src/main/...)**。

## 分类类别与下一步行动 (Triage Categories)
- **ENV**: 环境问题。行动：检查配置或环境。
- **TEST**: 测试代码 bug。行动：QA 角色立即根据 `rule-fix-failures.md` 修复测试代码。
- **IMPL**: 业务逻辑 bug。行动：记录详细 Bug Report，移交给开发角色修复。
- **CONTRACT**: 契约/设计问题。行动：联系设计师审核并更新设计文档。

## 故障报告规范 (Bug Report Section)
如果是 IMPL 或 CONTRACT 故障，报告必须包含：
1. **复现命令**: PowerShell 运行指令。
2. **失败描述**: 哪个协议/方法失败了。
3. **输入参数**: 发送请求的具体数据。
4. **预期结果**: 设计文档中的预期。
5. **实际结果**: 服务器返回的错误码或错误数据 (Json Diff)。
6. **日志线索**: 提取服务器日志中的 Exception 或关键字。

## 验收 / 完成标准
- 报告包含：
  - 运行的命令
  - 失败测试列表
  - 每个失败的分类
  - 下一步行动 (接下来运行哪个技能)