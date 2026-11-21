# 游戏服务器工程总览（全景导航）

> **读者**：服务端研发/维护人员 & AI Coding Agent（首次进入本目录请先读本页）。  
> **原则**：本文件只提供**工程组成、多仓边界、修改红线与文档路由**，不重复抄写架构、规范或流程细节；所有细则以各层唯一真源（SSOT）为准。

---

## 1. 项目整体组成（双轨结构）

本目录 `server/` 由 **游戏服务端源码** 与 **AI 辅助研发体系** 两大部分叠加组成，外部还依赖两个平级配置/生成器目录：

```text
../configs/                       # [外部] 策划配表真源与导出产物（Excel / XML / 生成代码源）
../dic_generate/                  # [外部] 配表生成器源码及《配置表AI编码规范.md》

server/                           # [宿主主仓 server.git]
├── pom.xml                       # JDK 21 六模块聚合工程（默认 skipTests=true）
├── util/                         # 公共基础工具（时间/随机/配置/日志）
├── protocol/                     # [独立 Git: protocol.git] Protobuf 真源与生成代码、配置生成类
├── core/                         # 基础设施（虚拟线程邮箱、网络/RPC、缓存、Vert.x 集群）
├── game/                         # 主业务服（GameServer + CrossServer、Handler/Module、GM、验收平台）
├── login/                        # 登录服（HTTP 登录、区服列表、账号与订单入口）
├── simulationclient/             # 模拟客户端（协议场景测试、白盒测试 SDK、压测）
├── gm-console/                   # GM Web 控制台外部热重载面板（panels/）
├── doc/                          # 面向研发与运维的系统架构、研发手册、本地部署与专项验收文档
├── tools/                        # 工具 JAR 与 AI-SOP 版本锁（tools/ai-sop/ai-sop.lock.json）
│
├── .ai-sop/                      # [Git Submodule: agent-sop.git] 通用 AI 开发工作流引擎（严禁宿主手改）
├── AGENTS.md / ANTIGRAVITY.md    # [投影生成] AI 公共与各工具入口指令（严禁手改）
├── CLAUDE.md / GEMINI.md         # [投影生成] Claude / Legacy Gemini 入口指令（严禁手改）
├── ai-sop.ps1                    # [投影生成] SOP CLI 统一入口（严禁手改）
├── .agents/ .claude/ .codex/     # [投影生成] 各 AI 客户端 Hook 与 Skill 投影目录（严禁手改）
├── .cursor/ .github/ .pi/        # [投影生成] 其余 AI 客户端 Hook/扩展投影（严禁手改）
│
└── .ai-workspace/                # [独立 Git: ai-workspace.git] 本服领域上下文、自建 Skill 与校验脚本
    ├── context/                  # AI 编码规范、业务模式、API 白名单、验证 Profile 与排障手册
    ├── skills/                   # 本服自建 Skill 真源（如 module-acceptance-tester，安装后投影至 .agents/.claude）
    ├── scripts/                  # 本服工作区校验、JUnit 报告门禁、本地运行时与回归脚本
    ├── workflows/                # 并行开发、发布恢复与迁移记录
    └── specs/                    # [独立 Git: game_specs.git] 本服功能设计文档、任务记录与验证证据
```

---

## 2. 多仓库归属与修改红线

提交代码或文档前，必须确认文件所属的真实 Git 仓库，**不能在宿主根执行一次 `git add` 就以为所有目录都已提交**：

| 路径 | 归属仓库 / 跟踪状态 | 能否直接修改 | 说明 |
|---|---|---|---|
| `server/` 业务代码、`doc/`、`README.md` | `server.git`（宿主主仓跟踪） | 可修改 | 宿主已忽略 `.ai-workspace/`、`protocol/` 等独立仓 |
| `protocol/` | `protocol.git`（独立仓库） | 改 `.proto` 后由 `protocol.bat` 生成 | 单独检查 `git -C protocol status` 与提交 |
| `.ai-sop/`、`tools/ai-sop/` | `agent-sop.git`（Git Submodule + 锁文件，`server.git` 跟踪） | **禁止在宿主直接修改** | 通用 SOP 引擎，由 `tools/ai-sop/ai-sop.lock.json` 锁版本 |
| `ANTIGRAVITY.md`、`GEMINI.md`、`ai-sop.ps1` | 投影生成（**由 `server.git` 跟踪**） | **禁止手工编辑** | 随 SOP 升版由 `install-ai-sop.ps1` 重新投影，变更需随 `server.git` 提交 |
| `AGENTS.md`、`CLAUDE.md`、`.agents/`、`.claude/`、`.codex/`、`.cursor/`、`.github/`、`.pi/` | 投影生成（**由 `.gitignore` 忽略**） | **禁止手工编辑** | 本机安装器投影生成；自建 Skill 请改 `.ai-workspace/skills/` 后重投（历史遗留的少量 `.claude/skills/*` 若仍被跟踪亦不得手改） |
| `.ai-workspace/`（不含 `specs/`） | `ai-workspace.git`（独立仓库） | 可修改 | 存放本服领域知识、验证配置、自建 Skill 真源与脚本；提交使用其独立 Git / `commit.cmd` |
| `.ai-workspace/specs/` | `game_specs.git`（独立仓库） | 可修改（状态由引擎写入） | 仅保存**本服功能/平台**的设计文档、任务与 `.artifacts/`；外层 `.ai-workspace/.gitignore` 隔离 |

---

## 3. 文档导航地图（人 + AI 共用）

### 3.1 我是人类研发 / 维护人员
| 需求 | 目标文档（唯一真源） |
|---|---|
| 了解系统拓扑、节点职责、邮箱线程、存储与容错边界 | [doc/01_系统架构设计全景文档.md](doc/01_系统架构设计全景文档.md) |
| 了解完整研发流程、配表/协议/业务落地与编码约束 | [doc/02_开发流程与研发规范手册.md](doc/02_开发流程与研发规范手册.md) |
| 搭建 Windows 本地环境（MySQL/Redis/ZK/Apollo）与启停服务 | [doc/deployment_guide.md](doc/deployment_guide.md) |
| 新人图文快速上手配置文档（**仅面向开发者人工阅读，AI 无需加载**） | [doc/Party项目服务端快速上手配置文档.docx](doc/Party项目服务端快速上手配置文档.docx) |
| 排查线上/本地 Bug、查看日志与使用 GM Web 工作台 | [.ai-workspace/context/debugging-playbook.md](.ai-workspace/context/debugging-playbook.md) |
| 查看充值跨进程与崩溃恢复专项验收标准 | [doc/payment-process-recovery.md](doc/payment-process-recovery.md) |
| 查看配表结构与生成器规则（外部平级仓库） | [../dic_generate/配置表AI编码规范.md](../dic_generate/配置表AI编码规范.md) |
| 了解 `.ai-workspace` 恢复与多仓同步方式 | [.ai-workspace/README.md](.ai-workspace/README.md) |

### 3.2 我是 AI Agent（冷启动阅读顺序）
1. **流程入口与红线**：先遵循 [AGENTS.md](AGENTS.md)（Antigravity 同步看 [ANTIGRAVITY.md](ANTIGRAVITY.md)，Claude Code 同步看 [CLAUDE.md](CLAUDE.md)）。
   - 纯问答 / 只读审查：直接按需读文档与代码，**不启动 Task**。
   - 准备修改代码/配置/文档：按 `AGENTS.md` 执行 `Task Discover` → `Start/Resume`（默认 T2）。
2. **领域知识总索引**：加载 [.ai-workspace/context/README.md](.ai-workspace/context/README.md)（包含 4 篇“始终加载”精简核心文档 + 全量“按需加载”路由表，含 `doc/` 与 `debugging-playbook.md`）。
3. **本服功能设计与任务**：查阅 [.ai-workspace/specs/README.md](.ai-workspace/specs/README.md) 定位 `.ai-workspace/specs/features/<FeatureName>/`（仅收录本服自有功能与平台基建）。

---

## 4. 高频命令速查（均在 `server/` 根目录执行）

```powershell
# 1. Maven 编译（根 POM 默认 skipTests=true，构建成功不代表执行了测试）
mvn -B '-DskipTests' compile
mvn -B '-DskipTests' test-compile

# 2. SOP 安装与环境诊断
pwsh -NoProfile -File ./ai-sop.ps1 Verify
pwsh -NoProfile -File ./ai-sop.ps1 Doctor

# 3. 任务发现与验证 Profile（开发修改前先 Discover -> Start/Resume）
pwsh -NoProfile -File ./ai-sop.ps1 Task -Operation Discover
pwsh -NoProfile -File ./ai-sop.ps1 Task -Operation Run -Feature <Feature> -TaskId <TaskId> -Agent <Agent> -Profile compile
pwsh -NoProfile -File ./ai-sop.ps1 Task -Operation Run -Feature <Feature> -TaskId <TaskId> -Agent <Agent> -Profile workspace-contract

# 4. 协议生成 / API 白名单导出 / 模拟客户端
.\protocol.bat
.\export_api.bat
pwsh -NoProfile -File ./run_client.ps1 -TestClass <完整测试类名>
```

> 更多验证 Profile、定向 JUnit 脚本、运行实例联调与多仓同步命令，见 [.ai-workspace/context/project-tooling.md](.ai-workspace/context/project-tooling.md)。
