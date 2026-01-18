# SKILL: Fix Failures (Contract-First When Needed) (修复失败 [必要时优先更新契约])

## 技能标识 (Skill ID)
SKILL_ID: 32_fix_with_contract_update
VERSION: 0.2
STAGE: TEST (fix loop) (测试 [修复循环])

## 意图 (Intent)
应用最小的修复以使测试通过，同时防止无声的契约漂移 (silent contract drift)。
如果必须更改外部可见行为，请先更新 05_design_contract.md。

## 必需上下文包 (必须加载)
- _common/ctx/ctx_test.md

## 输入
- features/<feature>/05_design_contract.md
- features/<feature>/09_test_run_report.md (或原始失败日志)
- 相关代码和测试

## 输出
- 代码/测试修复 (最小化)
- 更新的 features/<feature>/05_design_contract.md (如果行为发生变化)
- features/<feature>/10_fix_report.md

## 允许的修改 (硬性约束)
协调员必须指定以下模式之一：

模式 A (仅实施修复 - MODE A):
- 允许：
  - 契约范围内的生产代码文件
  - 测试
- 禁止：
  - 协议更改
  - 契约更改

模式 B (允许更新契约 - MODE B):
- 允许：
  - features/<feature>/05_design_contract.md
  - 范围内的生产代码
  - 测试
- 禁止：
  - 协议更改 (除非明确授权)

## 执行检查清单
1) 识别失败的测试和根本原因类别。
2) 决定 模式 A vs 模式 B：
   - 如果修复更改了契约未描述的行为 => 模式 B，先更新契约。
3) 实施最小补丁。
4) 重新运行失败的测试。
5) 编写 10_fix_report.md：
   - 根本原因
   - 补丁摘要
   - 契约更改 (如果有)
   - 验证结果

## 验收 / 完成标准
- 失败的测试通过。
- 漂移防护 (Drift guard) 将通过 (建议在此之后运行技能 22)。

---

## PROMPT (copy/paste)
你是一个执行 Agent。

阅读：
- _common/ctx/ctx_test.md
- features/<feature>/05_design_contract.md
- features/<feature>/09_test_run_report.md (和/或原始日志)

任务：
以最小的更改修复失败。

协调员提供的约束 (填写)：
- 模式 (MODE): A 或 B
- 允许的文件/目录: <列表>
- 禁止: <列表>
- 要运行的测试: <命令或测试名称>

硬性规则：
- 如果你更改了外部可见行为，请先更新 05_design_contract.md (模式 B)。
- 保持差异最小化；不要重构。

交付：
- 确切的差异 (diffs)/编辑
- 更新后的 05_design_contract.md (如果为模式 B)
- features/<feature>/10_fix_report.md，总结根本原因和验证
