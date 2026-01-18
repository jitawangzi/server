# SKILL: Run Static Checks / Build Verification (运行静态检查 / 构建验证)

## 技能标识 (Skill ID)
SKILL_ID: 21_static_checks
VERSION: 0.1
STAGE: DEV (开发)

## 意图 (Intent)
验证功能变更集的编译/Lint/格式化/单元冒烟测试。

## 必需上下文包 (必须加载)
- _common/ctx/ctx_dev.md

## 输入
- 包含功能实施变更的仓库工作树
- features/<feature>/05_design_contract.md (获取验收命令)

## 输出
- features/<feature>/06_dev_report.md (追加验证部分)
- (可选) features/<feature>/06_build_log_excerpt.txt

## 允许的修改 (硬性约束)
- 允许：
  - features/<feature>/06_dev_report.md
  - features/<feature>/06_build_log_excerpt.txt
- 禁止：
  - 任何代码变更 (此技能仅作验证)

## 执行检查清单
1) 从 05_design_contract.md 读取验收命令。
2) 运行：
   - 构建/编译
   - Lint/格式化 (如果适用)
   - 最小单元测试/冒烟测试
3) 捕获失败信息：
   - 运行的命令
   - 退出代码
   - 最小相关日志
4) 在 06_dev_report.md 中总结。

## 验收 / 完成标准
- 所有必需检查通过，或已记录失败及其复现步骤。

---

## PROMPT (copy/paste)
你是一个执行 Agent。

阅读：
- _common/ctx/ctx_dev.md
- features/<feature>/05_design_contract.md

任务：
运行契约要求的构建/静态检查并记录结果。

约束：
- 不要修改代码。
- 仅将结果写入 features/<feature>/06_dev_report.md (追加一个“验证”部分)。
- 如果任何命令失败，包含最小日志和可能的根本原因假设。

交付：
- 执行的命令 + 结果
- 更新后的 06_dev_report.md 内容
