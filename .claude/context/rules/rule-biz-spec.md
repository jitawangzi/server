# SKILL: Derive Business Spec (推导业务规格) (02_biz_spec.md)

## 技能标识 (Skill ID)
SKILL_ID: 10_derive_biz_spec
VERSION: 0.1
STAGE: DESIGN (设计)

## 意图 (Intent)
将原始需求转换为明确的业务规格说明书：
范围、规则、边界情况、不变量和验收标准。

## 必需上下文包 (必须加载)
- _common/ctx/ctx_design.md

## 输入 (运行前必须存在)
- features/<feature>/01_req.md
- _common/context_brief.md
- _common/project_context.md (架构 + API 边界)

## 输出 (必须创建/更新)
- features/<feature>/02_biz_spec.md

## 允许的修改 (硬性约束)
- 允许修改：
  - features/<feature>/02_biz_spec.md (创建/更新)
- 禁止修改：
  - 任何代码
  - 任何 .proto 文件
  - 任何其他功能文档 (除非协调员明确授权)

## 输出格式 (02_biz_spec.md 必须包含)
1) 概览 (Overview) (1 段)
2) 范围 (Scope)
   - 范围内 (In scope)
   - 范围外 (Out of scope)
3) 实体与术语 (Entities & Terms) (词汇表)
4) 业务规则 (Business Rules) (编号，可测试)
5) 边界情况 / 故障情况 (Edge Cases / Failure Cases) (明确列出)
6) 错误码 / 面向用户的错误 (Error Codes) (如果适用)
7) 非功能性约束 (Non-functional constraints) (性能、安全性、幂等性、并发性)
8) 验收标准 (Acceptance Criteria) (检查清单)

## 验收 / 完成标准
- 每条规则都是可测试且不矛盾的。
- 01_req.md 中的所有模糊部分都已解析为明确的陈述
  或列为未决问题 (Open Questions)。

---

## PROMPT (copy/paste)
你是一个在仓库中的执行 Agent。

阅读：
- _common/ctx/ctx_design.md
- _common/context_brief.md
- _common/project_context.md
- features/<feature>/01_req.md

任务：
遵循“输出格式”创建/更新 features/<feature>/02_biz_spec.md。

约束：
- 不要更改代码或 proto 文件。
- 优先使用明确的编号规则和边界情况。
- 如果需求模糊，请在末尾添加“未决问题 (Open Questions)”部分。

交付：
- 02_biz_spec.md 的完整内容
- 未决问题的简短列表 (如果有)
