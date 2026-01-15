# 游戏服务端新功能开发 SOP (Standard Operating Procedure)

本文档规定了 AI 协同人类开发者进行功能开发的标准流程。流程核心目标是：**重设计（AI 生成 + 人工把关），轻执行（AI 全自动编码）**。

---

## 0. 工作区准备 (Workspace Setup)

开始新功能前，请依据功能复杂度选择目录结构：

### 目录结构规范
* **_common/**: 存放本项目所有公共规范 (`AI.md`, `sop.md`, `project_context.md` 等)。
* **features/**: 存放具体功能开发文档，按 `编号_功能名` 命名。

### 模式选择
1.  **标准模式 (Standard Mode)**：适用于复杂系统（如公会、战斗、多模块交互）。需拆分设计文档。
2.  **快速模式 (Fast Mode)**：适用于简单功能（如签到、CRUD、简单活动）。设计文档合并。

---

## 1. 需求分析 (Requirement Analysis)

* **输入**：`features/xxx/01_req.md` (原始策划需求/草稿)。
* **执行**：AI 阅读原始需求，结合 `project_context.md` 理解项目背景。
* **输出**：`features/xxx/02_biz_spec.md` (**业务规则文档**)。
    * 将需求转化为纯文本的逻辑描述。
    * 明确“做什么”和“不做什么”。
    * **人工动作**：确认规则无误。

---

## 2. 技术方案设计 (Technical Design) [核心环节]

在此阶段，AI 需产出**指令级**的设计方案。这些文档将作为后续编码的“绝对法律”。

### 2.1 设计产出物 (根据模式选择)

#### A. 标准模式 (Standard Mode)
请分别生成以下文件：
1.  **`03_a_config.md` (静态数据)**: 定义 Excel 结构、GlobalConst 常量。
2.  **`03_b_protocol.md` (协议定义)**: 定义 Protobuf 消息结构。
3.  **`03_c_logic.md` (逻辑指令)**: 核心业务流程的伪代码/指令。

#### B. 快速模式 (Fast Mode)
请生成单文件：
1.  **`03_design_full.md`**: 包含配置、协议和逻辑指令的所有内容。

### 2.2 设计原则 (Design Principles)

#### A. 静态数据设计
* 引用 `config.md` 规范。
* 明确 Config 类的字段类型及索引方式（ID索引or 类型索引,或者都有，默认会生成ID索引），需不需要修改已有Config 结构。
* 明确 是否需要在GlobalConst类中新增哪些常量。

#### B. 协议设计 (Server-First)
* **服务端逻辑最简原则**：协议设计优先服务于服务端逻辑的原子性和便利性，而非 UI 展示。
* **UI 数据解耦**：UI 需展示的复杂数据，尽量通过 `PlayerAllInfo` 或通用查询同步，避免在业务响应中携带无关状态。
* **输出要求**：直接输出 `.proto` 代码块，包含完整的 Message ID 和注释。

#### C. 逻辑设计 (指令化描述)
* **严禁**使用模糊的自然语言（如“检查一下钱够不够”）。
* **必须**使用**指令化伪代码**，明确调用哪个 Manager 或 Helper。
* **示例**：
    > **Function**: `upgrade()`
    > 1. **Check**: `PlayerHelper.checkResource(player, 1001, cost)`
    > 2. **Action**: `PlayerHelper.delResources(player, 1001, cost, OpType.Upgrade)` (Allow exception throw)
    > 3. **Logic**: `data.setLevel(lv + 1)`
    > 4. **Response**: Send `UpgradeResponse`

---

## 3. 测试方案预演 (Test Planning)

* **时机**：在技术设计阶段同步完成。
* **输出**：`04_test_plan.md`。
* **内容**：
    * 列出关键测试路径（Happy Path）。
    * 列出异常分支（Exception Case）。
    * **自检**：如果发现某个逻辑无法编写客户端测试用例，请返回修改协议设计。

---

## 4. 人工评审与定稿 (Human Review & Sign-off)

> **⚠️ 关键节点**：此步骤未完成前，严禁进入编码阶段。

* **执行者**：人类开发者。
* **审查重点**：
    1.  **协议合理性**：是否为了 UI 牺牲了服务端性能？是否有安全漏洞？
    2.  **逻辑完备性**：`03_c_logic` 中的指令是否正确引用了 API？流程是否闭环？
* **动作**：
    * 直接修改 AI 生成的 Markdown 文档。
    * 确认无误后，通知 AI：“设计已锁定，开始编码”。

---

## 5. 自动化编码 (Auto Coding)

* **输入**：已定稿的 `03_design` 系列文档 + `AI.md` (编码规范)。
* **执行**：AI 扮演 **Coder** 角色。
* **原则**：
    * **严格执行**：完全照搬设计文档中的逻辑指令，不进行“发挥”。
    * **引用白名单**：正确导入 `project_context.md` 中的 API。
    * **工具生成的类不处理**：工具生成的类不创建也不修改，Java Config、Manager 、Handler类。
* **产出**：
    * `.proto` 文件。
    *  Module 及相关类 / Enum 代码。

---

## 6. 自动化测试生成 (Auto Testing)
* 引用 `client.md` 流程及规范。
* **输入**：`client.md` + `03_b_protocol.md` + `04_test_plan.md`。
* **执行**：AI 扮演 **QA** 角色。
* **任务**：
    * 编写 `ClientXxxTest.java`。
    * 覆盖测试计划中的所有 Case。
    * 利用 `sendAndWait` 验证 ErrorCode 和 Response。

---

## 7. 验收与迭代 (Verify & Iterate)
* 引用 `client.md` 流程及规范。
  **编译与启动、测试等流程**：参考文档。
4.  **修复**：如果测试失败，AI 需分析日志，并**同时更新**代码和和设计文档，保持文档与代码一致。
