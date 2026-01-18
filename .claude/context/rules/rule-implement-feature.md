# SKILL: Implement Feature From Design Contract (依据设计契约实施功能)

## 技能标识 (Skill ID)
SKILL_ID: 20_implement_feature_from_contract
VERSION: 0.2
STAGE: DEV (开发)

## 意图 (Intent)
严格根据设计契约实施服务端功能，保持最小范围且不重新设计。

## 必需上下文包 (必须加载)
- _common/ctx/ctx_dev.md

## 输入 (运行前必须存在)
- _common/AI.md
- _common/context_brief.md
- features/<feature>/05_design_contract.md
- (可选) features/<feature>/03_design_full.md (仅相关部分)
- 协议定义 (.proto) (仓库中已实施的)

## 输出 (必须创建/更新)
- 此功能所需的服务端代码变更
- (可选) 开发笔记：features/<feature>/06_dev_report.md

## 允许的修改 (硬性约束)
- 允许：
  - 仅限 05_design_contract.md 中“允许修改范围”一节列出的文件
- 禁止：
  - 任何协议更改 (除非契约明确允许)
  - 任何无关的重构
  - 任何契约中未描述的行为更改

## 执行检查清单
1) 阅读 ctx_dev + AI.md + context_brief + 05_design_contract。
2) 提取：
   - 要实施的端点/处理程序
   - 错误码映射
   - 不变量
   - 允许的文件/模块
3) 实施最小差异 (minimal diff)。
4) 根据 AI.md 添加/调整日志和指标。
5) 运行构建/静态检查 (或提供命令)。
6) 编写 06_dev_report.md：
   - 变更的文件
   - 已实施的契约项目 (复选框列表)
   - 如何验证

## 验收 / 完成标准
- 构建成功。
- 无契约漂移 (No contract drift)。
- 仅修改了允许的文件。

---

## PROMPT (copy/paste)
你是一个在仓库中的执行 Agent。

阅读：
- _common/ctx/ctx_dev.md
- _common/AI.md
- _common/context_brief.md
- features/<feature>/05_design_contract.md
- 契约引用的相关 .proto 和现有代码模块

任务：
严格根据 05_design_contract.md 实施此功能。

硬性约束：
- 仅修改契约明确允许的文件。
- 除非契约明确授权，否则不要更改协议。
- 如果发现契约不完整/不正确，请停止并首先提议最小的契约更新。

交付：
- 简短计划 (最多 15 行)
- 你将应用的准确差异 (diffs)/编辑
- 如何验证 (命令) + 结果 (如果你能运行它们)
- 创建/更新 features/<feature>/06_dev_report.md，总结变更和契约合规性
