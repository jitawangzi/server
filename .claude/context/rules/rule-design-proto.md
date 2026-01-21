# SKILL: Design Protocol (.proto) + Message/ID Table (设计协议 + 消息/ID 表)

## 技能标识 (Skill ID)
SKILL_ID: 11_design_proto
VERSION: 0.1
STAGE: DESIGN (设计)

## 意图 (Intent)
根据业务规格和协议规则设计或更新协议定义。
此技能是唯一应该重点参考协议规则文档的地方。

## 必需上下文包 (必须加载)
- .claude/context/proto-rules.md (协议规则、ID 分配规则、命名规则)

## 输入 (运行前必须存在)
- specs/features/<feature>/01_server_rules.md
- 现有的 .proto 文件 (仓库中)

## 输出 (必须创建/更新)
选择一项 (团队偏好)：
A) 更新仓库中实际的 .proto (如果工作流允许，建议首选)，和/或
B) 生成协议提案文档：
   - specs/features/<feature>/03_protocol_design.md (proto 代码片段 + ID 表)

## 允许的修改 (硬性约束)
- 允许修改：
  - 属于此功能/服务的 .proto 文件,以及可能需要的一些通用的proto
  - specs/features/<feature>/03_protocol_design.md (创建/更新)
- 禁止：
  - 任何无关的 .proto
  - 任何服务端代码
  - 任何客户端代码/测试

## 输出要求 (Output Requirements)
- 明确的请求/响应消息定义
- 遵循 context/proto-rules.md 的字段编号和命名
- 错误码映射 (如果协议携带错误)
- 消息 ID / 端点 (如果你的协议使用它们)
- 向后兼容性说明

## 验收 / 完成标准
- 协议匹配所有需要线路变更的业务规则。
- 没有禁止的破坏性变更 (除非明确批准)。
- 所有 ID/字段均唯一且一致。

---
