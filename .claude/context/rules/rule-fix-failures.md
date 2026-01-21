# SKILL: Fix Failures (Contract-First When Needed) (修复失败 [必要时优先更新契约])

## 技能标识 (Skill ID)
SKILL_ID: 32_fix_with_contract_update
VERSION: 0.2
STAGE: TEST (fix loop) (测试 [修复循环])

## 角色职责 (Role Responsibilities)
- **QA 角色**: 仅在 `09_test_run_report.md` 分类为 **TEST** 时，使用此规则修复测试代码。
- **开发角色**: 在分类为 **IMPL** 时，使用此规则根据 Bug Report 修复业务逻辑。

## 意图 (Intent)
应用最小的修复以使测试通过，同时保持设计与代码的一致性。
如果修复过程发现设计契约 (`03_design` 系列) 存在错误，必须同步更新文档。

## 执行检查清单
1) 识别故障分类 (来自 `09_test_run_report.md`)。
2) 如果是开发角色修复 IMPL Bug：
   - 分析 QA 提供的输入/预期/实际对比。
   - 在代码中增加必要的日志或断点定位（如果需要）。
3) 决定 模式 A vs 模式 B：
   - 模式 A：修复代码实现，使其符合设计。
   - 模式 B：代码实现是对的，但设计写错了。必须先修改 `03_design` 系列文档。
4) 实施最小补丁。
5) 验证修复：
   - 开发角色：重新执行编译/启动。
   - QA 角色：重新运行失败的测试用例。

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
