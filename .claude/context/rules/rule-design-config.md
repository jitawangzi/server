# SKILL: Design Config (Excel 或者java静态数据结构) (静态数据表结构、常量 设计)

## 技能标识 (Skill ID)
SKILL_ID: 12_design_config
VERSION: 0.1
STAGE: DESIGN (设计)

## 意图 (Intent)
根据业务规则设计静态数据配置表结构。

## 必需上下文包 (必须加载)
- .claude/context/config-rules.md (静态表设计规则)

## 输入 (运行前必须存在)
- .claude/specs/features/<feature>/01_server_rules.md

## 输出 (必须创建/更新)
配置表提案文档：
   - .claude/specs/features/<feature>/02_config_design.md (静态配置表结构、 索引定义,新增常量)
   - .claude/specs/features/<feature>/<feature>.xlsx (第一次创建配置表时，同时生成xlsx，方便人类使用，以后不用更新维护)
## 允许的修改 (硬性约束)
- 允许修改：
  - .claude/specs/features/<feature>/<feature>.xlsx
  - .claude/specs/features/<feature>/02_config_design.md (创建/更新)
- 禁止：
  - 任何无关的 .proto
  - 任何服务端代码
  - 任何客户端代码/测试

## 输出要求 (Output Requirements)
- 明确的配置表结构（字段名，字段类型）
- 数据表读取方式，例如是list类型的，还是map类型的，map类型的，需不需要增加索引。 
- 

## 验收 / 完成标准
- 设计的配置表结构，要涵盖所有业务规则。
- 没有禁止的破坏性变更 (除非明确批准)。

---
