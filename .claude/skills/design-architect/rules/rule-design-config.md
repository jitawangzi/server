# SKILL: Data Modeling (Schema & Config) (数据建模：存储与配置)

## 技能标识 (Skill ID)
SKILL_ID: 12_design_data
VERSION: 0.2
STAGE: DESIGN (设计)

## 意图 (Intent)
根据业务规则，设计服务端的数据持久化方案（DB Schema）和静态配置表结构（Config Structure）。

## 必需上下文包 (必须加载)
- .claude/context/config-rules.md (静态表设计规则)
- (推荐) 现有的 Database Schema 定义（如果存在，用于参考风格）

## 输入 (运行前必须存在)
- .claude/specs/features/<feature>/01_server_rules.md

## 输出 (必须创建/更新)
数据模型设计文档：
   - .claude/specs/features/<feature>/02_data_model.md
   - (可选) .claude/specs/features/<feature>/<feature>.xlsx (如果涉及新配置表，生成初始 Excel)

## 核心任务 (Core Tasks)

### 1. Rule Compliance Verification (规则合规性验证) - CRITICAL
在开始设计前，必须显式读取 `.claude/context/config-rules.md` 中的 **"7. 常用设计模式 (Design Patterns)"** 和 **"5. Excel表结构"** 章节。
你必须在输出文档的开头，列出一个 **Self-Check List**，确认你的设计符合以下模式：
*   **Tiered Costs (7.1)**: 是否使用了 `int[][]` 而非新建表？
*   **Naming (5.1)**: Sheet名是否包含 `#注释`？
*   **Global Rules (2.2.A)**: 是否使用了 `GlobalConst`？
*   **Enums (2.2.B)**: 是否定义了必要的枚举？如果定义了，是否符合 **(5.3 枚举表结构规则)**？ (ID, name, desc 是必选列)
*   **Time Types (3.1)**: 是否使用了 `LocalDateTime`？

### 2. Persistence Strategy (持久化策略)
必须明确该功能的数据存储方式：
- **In-Player Blob (默认推荐)**: 
    - 适用于：大多数依附于玩家的私有数据（如：任务进度、背包、技能等级）。
    - 方案：直接定义在 Player 对象或其子模块（Component）中，随玩家整体序列化。无需独立建表。
- **Independent Table (独立表)**:
    - 适用于：
        - 全局数据（排行榜、全服活动状态）。
        - 离线交互数据（离线邮件、好友申请、竞技场防守阵容）。
        - 巨大的日志/流水数据。
        - 必须通过 SQL 复杂查询的数据（工会搜索）。
    - 方案：设计专门的数据库表（MySQL/MongoDB Collection）。

### 2. Database Schema Design (若需独立表)
如果选择了 **Independent Table**，必须定义：
- **Table Name**: 符合项目命名规范（如 `t_guild`, `t_mail`）。
- **Columns**: 字段名、类型（int, bigint, varchar, blob）、是否可空、默认值。
- **Keys**: 主键（PK）、索引（Index/Unique）。
- **Usage**: 简述该表在业务中的读写频率和方式。

### 3. Static Config Design (配置表设计)
- **Table Name**: 定义配置表的文件名（如 `ShopConfig`, `ItemConfig`）。
- **Structure**: 字段名、类型、解释。
- **Indexing**: 服务端加载时的索引方式（Map Key, List, Grouping）。
- **Constants**: 全局常量定义（如：每日恢复上限、初始价格参数）。

## 验收 / 完成标准
- **策略明确**: 每个业务状态（在 `01_server_rules.md` 中定义的）都必须有明确的归属（Player Blob 或 Independent Table）。
- **结构完整**: 静态配置涵盖所有非动态参数。
- **规范一致**: 命名和类型风格与项目现有代码一致。