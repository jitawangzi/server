# AI 辅助开发指南 (AI-Assisted Development Guide)

本指南详细说明了如何利用 `.claude/skills` 下的 AI 专家系统进行高效、规范的游戏服务端开发。

## 核心哲学 (Core Philosophy)
**"文档驱动一切 (Documentation Driven Everything)"**
1.  **01_server_rules.md** 是业务逻辑的唯一真理。
2.  **06_design_contract.md** 是技术实现的唯一真理。
3.  代码只是契约的投影。**严禁跳过文档直接修改代码**（Bug 修复除外，但必须伴随测试）。

---

## 场景一：新功能开发 (New Feature)
**适用**：从 0 到 1 开发一个完整的新系统（如：宠物系统、公会系统）。
**模式**：标准瀑布流 (Waterfall)。

### 步骤清单
1.  **准备策划稿**: 将策划提供的 `.docx` 或 `.doc` 放入 `.claude/specs/features/<FeatureName>/`。
2.  **Step 1: 需求分析**
    *   **指令**: `/activate requirement-analyst`
    *   **Prompt**: "请解析 <FeatureName> 下的策划文档，生成初始版本的业务规则书。"
    *   **产出**: `01_server_rules.md`
3.  **Step 2: 系统设计 (迭代)**
    *   **指令**: `/activate design-architect`
    *   **Prompt**: "请根据 01 规则书，开始进行 <FeatureName> 的技术设计。请按步骤输出。"
    *   **交互**: 你需要依次确认 `02 (Data)`, `03 (Proto)`, `04 (Logic)`, `05 (Test)`，直到生成最终契约 `06`。
4.  **Step 3: 代码实现**
    *   **指令**: `/activate implementation-engine`
    *   **Prompt**: "设计契约 06 已确认，请实现 <FeatureName> 的功能代码。"
5.  **Step 4: 质量验收**
    *   **指令**: `/activate quality-assurance`
    *   **Prompt**: "请为 <FeatureName> 编写测试用例并执行验收。"

---

## 场景二：功能维护与迭代 (Agile Maintenance)
**适用**：功能已上线，策划修改规则（如：调整次数限制逻辑、新增小功能），或你需要快速同步变更。
**模式**：敏捷快速通道 (Fast-Track)。

### 步骤清单
1.  **更新源头**: 将策划修改后的新 `.docx` 覆盖或放入功能目录。
2.  **Step 1: 智能增量更新**
    *   **指令**: `/activate requirement-analyst`
    *   **Prompt**: "策划更新了 <FeatureName> 的文档。请对比当前的 `01_server_rules.md`，执行增量更新。注意：忽略纯数值调整，只同步逻辑变更。"
    *   **关键点**: AI 会保留你之前的人工备注，只修改变化的规则。
3.  **Step 2: 快速同步实现**
    *   **指令**: `/activate feature-maintainer`
    *   **Prompt**: "<FeatureName> 的规则书已更新。请分析变更影响，并同步修改契约和代码。"
    *   **流程**:
        1.  AI 分析 `01` vs `06` 的差异。
        2.  AI 输出 **影响面分析 (Impact Analysis)**（改了哪些协议、哪些逻辑）。
        3.  **人工确认**。
        4.  AI 自动修改代码并运行测试。

---

## 场景三：Bug 修复 (Bug Fix & Triage)
**适用**：测试阶段或线上发现 Bug，需要修复。
**原则**：代码错误直接修，设计缺陷先改文档。

### 步骤清单
1.  **准备 Bug 列表**:
    在功能目录下创建（或编辑）`BUGS.md`，格式如下：
    ```markdown
    # Bug List
    1. 宠物满级后还能吃药水，导致报错 (Expected: 提示已满级)。
    2. 商店购买后金币UI没刷新。
    3. 每日任务重置时间不对。
    ```
2.  **执行批量修复**
    *   **指令**: `/activate implementation-engine`
    *   **Prompt**: "请读取 <FeatureName>/BUGS.md，对列表中的问题进行判责和修复。"
3.  **AI 处理逻辑 (自动)**
    *   **判责 (Triage)**: AI 会查阅 `01` 和 `06` 文档。
        *   **DESIGN_FLAW**: 如果文档没定义或定义错误 -> **拒绝修复**，提示你去改文档（走场景二）。
        *   **CLIENT_ISSUE**: 如果服务端协议正确 -> **拒绝修复**，标记为客户端问题。
        *   **IMPL_FAILURE**: 如果代码逻辑错了 -> **执行修复**。
    *   **回归测试**: 对于修复的 Bug，AI 会强制运行或编写一个测试用例，证明它从 Fail 变成了 Pass。
4.  **查看报告**: AI 会输出最终的修复报告。

---

## 技能速查表 (Skill Cheat Sheet)

| Skill Name | 主要用途 | 典型指令 |
| :--- | :--- | :--- |
| **requirement-analyst** | **文档生成/更新**。解析 Docx，生成/同步 `01_server_rules.md`。 | "增量更新规则书，忽略数值变化" |
| **design-architect** | **系统设计**。生成 `02` ~ `06` 全套设计文档。 | "开始设计流程" |
| **feature-maintainer** | **敏捷迭代**。跳过中间设计，直接从 `01` 同步到代码。 | "规则已变，请同步代码" |
| **implementation-engine**| **代码实现/修Bug**。写代码，或批量修 Bug。 | "实现功能" / "修复 BUGS.md" |
| **quality-assurance** | **测试验证**。写测试，跑测试，查日志。 | "进行回归测试" |

## 常见问题 (FAQ)

**Q: 策划只改了数值（比如经验表），需要走流程吗？**
A: 不需要。如果只是配置表里的数字变了，直接用 Excel 工具重新导表即可。只有当**逻辑**变了（比如“经验计算公式”变了），才需要走 **场景二**。

**Q: 我可以直接改 Java 代码吗？**
A: 除非是紧急的热修复（Hotfix），否则**不建议**。直接改代码会导致代码与 `06_design_contract.md` 产生“漂移”。下次 AI 工作时可能会误覆盖你的修改。建议尽量通过 `feature-maintainer` 来应用变更。

**Q: AI 修复 Bug 失败了怎么办？**
A: 此时请人工介入。修改完代码后，记得运行一下 AI 生成的那个测试用例，确保它通过。
