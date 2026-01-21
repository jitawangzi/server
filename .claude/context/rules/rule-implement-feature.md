# SKILL: Implement Feature From Design Contract (依据设计契约实施功能)

## 技能标识 (Skill ID)
SKILL_ID: 20_implement_feature_from_contract
VERSION: 0.2
STAGE: DEV (开发)

## 意图 (Intent)
严格根据设计契约实施服务端功能，保持最小范围且不重新设计。

## 必需上下文包 (必须加载)
- .claude/context/ai-coding.md

## 输入 (运行前必须存在)
- .claude/context/ai-coding.md
- specs/features/<feature>/06_design_contract.md
- 协议定义 (.proto) (仓库中已实施的)

## 输出 (必须创建/更新)
- 此功能所需的服务端代码变更,直接创建或者修改已有的项目文件。 
- (可选) 开发笔记：specs/features/<feature>/07_dev_report.md

## 允许的修改 (硬性约束)
- 允许：
  - 仅限 06_design_contract.md 中“允许修改范围”一节列出的文件
- 禁止：
  - 任何协议更改 (除非契约明确允许)
  - 任何无关的重构
  - 任何契约中未描述的行为更改

## 执行检查清单
1) 阅读  ai-coding.md + 06_design_contract.md。
2) 提取：
   - 要实施的端点/处理程序
   - 错误码映射
   - 不变量
   - 允许的文件/模块
3) 实施最小差异 (minimal diff)。
4) 根据 ai-coding.md 添加/调整日志和指标。
5) 运行构建/静态检查。
6) 编写 07_dev_report.md：
   - 变更的文件
   - 已实施的契约项目 (复选框列表)
   - 如何验证

## 验收 / 完成标准
- 构建成功。
- 无契约漂移 (No contract drift)。
- 仅修改了允许的文件。

