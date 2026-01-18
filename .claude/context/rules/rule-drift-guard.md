# SKILL: Drift Guard (Contract Compliance Check) (漂移防护 [契约合规检查])

## 技能标识 (Skill ID)
SKILL_ID: 22_drift_guard
VERSION: 0.2
STAGE: DEV or TEST (gate) (开发或测试 [关卡])

## 意图 (Intent)
检测实施变更与冻结的设计契约之间的漂移 (drift)。

## 必需上下文包 (必须加载)
- _common/ctx/ctx_dev.md (如果在测试后运行，则为 ctx_test.md)

## 输入
- features/<feature>/05_design_contract.md
- 当前 diff / git 工作树中的已更改文件
- 协议文件 (.proto) (如果相关)

## 输出
- features/<feature>/07_drift_report.md

## 允许的修改 (硬性约束)
- 允许：
  - features/<feature>/07_drift_report.md
- 禁止：
  - 任何代码/proto 更改 (此技能仅作分析/报告)

## 检查 (必须包含在报告中)
1) 范围检查 (Scope check)：
   - 是否修改了任何禁止的文件？
2) 接口检查 (Interface check)：
   - 协议/消息/错误码是否与契约相比发生了变化？
3) 行为检查 (Behavior check)：
   - 核心流程是否按规定实施？
4) 不变量检查 (Invariants check)：
   - 是否有任何可能违反不变量的地方？
5) 风险总结 (Risk summary)：
   - 高/中/低 + 原因
6) 行动 (Actions)：
   - 如果存在漂移：列出所需的具体契约更新或需要回滚的代码更改。

## 验收 / 完成标准
- 07_drift_report.md 已存在并明确说明 PASS/FAIL (通过/失败)。
- 如果为 FAIL，则包含具体的补救计划。

---

## PROMPT (copy/paste)
你是一个执行 Agent。

阅读：
- 相关的上下文包 (_common/ctx/ctx_dev.md 或 _common/ctx/ctx_test.md)
- features/<feature>/05_design_contract.md
- 当前 git diff / 已更改文件列表
- 相关 .proto 文件

任务：
创建 features/<feature>/07_drift_report.md，检查实施是否偏离了契约。

约束：
- 不要修改代码或 proto。
- 明确：PASS/FAIL (通过/失败)，并提供逐项证据。

交付：
- 完整的 07_drift_report.md 内容
