# Party 游戏服务器 Windows 本地最新部署与运维指南

> **环境更新说明**：本文档针对最新路径结构（工程位于 `D:\work-all\game\`、MySQL 8.0 默认端口 3306、独立中间件及 Windows Terminal 多页签一键启停）进行重新整理与校准。

---

## 目录
1. [系统环境与最新路径一览](#一系统环境与最新路径一览)
2. [基础中间件与 Windows Terminal 多页签启动](#二基础中间件与-windows-terminal-多页签启动)
   - [2.1 中间件目录与独立运行脚本](#21-中间件目录与独立运行脚本)
   - [2.2 Windows Terminal 多页签一键脚本 (`start_middleware_wt.bat`)](#22-windows-terminal-多页签一键脚本-start_middleware_wtbat)
   - [2.3 中间件一键停止 (`stop_middleware.bat`)](#23-中间件一键停止-stop_middlewarebat)
3. [数据库与配置服务校准](#三数据库与配置服务校准)
   - [3.1 MySQL 8.0 配置](#31-mysql-80-配置)
   - [3.2 系统 hosts 配置](#32-系统-hosts-配置)
   - [3.3 ZooKeeper 节点注册](#33-zookeeper-节点注册)
4. [服务端编译与资源同步流程](#四服务端编译与资源同步流程)
   - [4.1 协议生成解耦与 PbProtocol 生成](#41-协议生成解耦与-pbprotocol-生成)
   - [4.2 数表导出与资源拷贝](#42-数表导出与资源拷贝)
   - [4.3 全工程 Maven 打包](#43-全工程-maven-打包)
5. [游戏服务启动 (GameServer、CrossServer 与 LoginServer)](#五游戏服务启动-gameservercrossserver-与-loginserver)
   - [5.1 启动参数详解](#51-启动参数详解)
   - [5.2 启动脚本](#52-启动脚本)
   - [5.3 GM Web 控制台访问与配置 (`GmWebConsoleServer`)](#53-gm-web-控制台访问与配置-gmwebconsoleserver)
6. [服务状态验证](#六服务状态验证)

---

## 一、系统环境与最新路径一览

| 组件 / 服务 | 最新路径 / 端口 | 说明 |
| :--- | :--- | :--- |
| **JDK 版本** | **JDK 21** (`D:\Java\jdk-21`) | 默认环境变量可保留 JDK 8，启动脚本内独立切换 JDK 21 |
| **Maven** | `D:\maven` | 阿里云镜像，本地仓库 `D:\maven\repository` |
| **MySQL** | **MySQL 8.0** (`127.0.0.1:3306`) | 用户名/密码：`root` / `root` |
| **中间件根目录** | `D:\work-all\game\app` | 包含 apollo, redis, zkui, zookeeper |
| **项目源码根目录** | `D:\work-all\game\server` | 游戏服务核心 Maven 工程 |
| **数表与配置目录** | `D:\work-all\game\configs\Server` | 策划数表与配置元数据（对应 `metafolder`） |

---

## 二、基础中间件与 Windows Terminal 多页签启动

在 `D:\work-all\game\app\` 目录下，已将 **apollo、redis、zkui、zookeeper** 四个单机服务做成独立可执行脚本，并通过 Windows Terminal (`wt.exe`) 实现**单个窗口、4个独立页签**同时运行。

### 2.1 中间件目录与独立运行脚本

1. **ZooKeeper (端口 2181)**
   - 目录：`D:\work-all\game\app\zookeeper\zookeeper1`
   - 单机配置文件：`conf\zoo_win.cfg`
   - 独立运行脚本：`D:\work-all\game\app\zookeeper\run_zookeeper.bat`
2. **Redis (端口 6379)**
   - 目录：`D:\work-all\game\app\redis`
   - 独立运行脚本：`D:\work-all\game\app\redis\run_redis.bat`
   - 运行程序：`bin\redis-server.exe redis.conf`（单机无密码）
3. **Apollo ConfigService (端口 9888)**
   - 目录：`D:\work-all\game\app\apollo\configservice`
   - 独立运行脚本：`D:\work-all\game\app\apollo\run_apollo.bat`
   - 启动命令：`java -Dserver.port=9888 -Dspring.profiles.active=github -jar apollo-configservice.jar`
   - 健康检查：`http://localhost:9888/health` 返回 `{"status":"UP"}`
4. **ZKUI (Web 端口 9990)**
   - 目录：`D:\work-all\game\app\zkui`
   - 独立运行脚本：`D:\work-all\game\app\zkui\run_zkui.bat`
   - 登录地址：`http://localhost:9990`，默认账号/密码：`admin` / `manager`

### 2.2 Windows Terminal 多页签一键脚本 (`start_middleware_wt.bat`)
脚本位置：`D:\work-all\game\app\start_middleware_wt.bat`
双击或命令行运行后，会自动唤起 Windows Terminal 并创建 4 个 Tab 页：
```bat
start wt.exe --title "ZooKeeper" -d "%BASE%\zookeeper" cmd.exe /k "run_zookeeper.bat" ; nt --title "Redis" -d "%BASE%\redis" cmd.exe /k "run_redis.bat" ; nt --title "Apollo" -d "%BASE%\apollo" cmd.exe /k "run_apollo.bat" ; nt --title "ZKUI" -d "%BASE%\zkui" cmd.exe /k "run_zkui.bat"
```

### 2.3 中间件一键停止 (`stop_middleware.bat`)
脚本位置：`D:\work-all\game\app\stop_middleware.bat`
自动查找并关闭监听 2181、9888、9990、6379 端口的进程。

---

## 三、数据库与配置服务校准

### 3.1 MySQL 8.0 配置
- 确认运行在 3306 端口，包含如下 4 个核心数据库：
  - `apolloconfigdb`
  - `apolloportaldb`
  - `game`
  - `passport`
- Apollo 配置（`apolloconfigdb.Item`）已全部校准为 3306 端口与账号 `root`/`root`。

### 3.2 系统 hosts 配置
`C:\Windows\System32\drivers\etc\hosts` 需包含：
```hosts
127.0.0.1 test
```
使各服务通过 `http://test:9888` 访问 Apollo。

### 3.3 ZooKeeper 节点注册
Vert.x 客户端在 ZK 的根命名空间为 `io.vertx`。已初始化 `game_test` 节点：
- 节点路径：`/io.vertx/server/login/game/nodes/game_test`
- 节点数据：
  ```json
  {"serverId":"game_test","name":"测试服","status":1,"ip":"127.0.0.1","port":7011,"serverOpenTime":"2024-11-18 00:00:01","version":"1.0.1","weight":1}
  ```

---

## 四、服务端编译与资源同步流程

### 4.1 协议生成解耦与 PbProtocol 生成
1. 运行 `D:\work-all\game\server\protocol.bat`：
   - 脚本默认将 `workspace` 与 `metafolder` 均设为当前仓库根目录（`%REPO_ROOT%`，**不会自动指向 `configs\Server`**）；如需让 `metafolder` 指向外部配置目录，请在运行前通过环境变量显式覆盖：
     ```cmd
     set metafolder=D:\work-all\game\configs\Server
     .\protocol.bat
     ```
2. 该脚本自动调用 `protoc.exe` 将 `protocol/src/main/proto/*.proto` 编译至 `protocol/src/main/java`，并通过 Velocity 模板生成完整的 `PbProtocol.java`。

### 4.2 数表导出与资源拷贝
数表类与 XML 配置源自 `D:\work-all\game\configs\Server\output`，官方同步脚本为 `game/tool/copy.bat`（需设置 `metafolder` 与 `workspace`）：
- **Java 配置代码**：
  - `output/java/entity/` -> `protocol/src/main/java/cn/game/protocol/generated/config/`
  - `output/java/enum/` -> `protocol/src/main/java/cn/game/protocol/generated/enume/`（受 `game/tool/exclude.txt` 过滤）
  - `output/java/init/` -> `protocol/src/main/java/cn/game/protocol/generated/helper/`
  - `output/java/manager/` -> `protocol/src/main/java/cn/game/protocol/generated/manager/`
- **XML 数表**：
  - `copy.bat` 默认将全量 `output/xml/data/*.xml` 同步至 `game/src/main/resources/xml/`，将 `GlobalConst.xml` 同步至 `login/src/main/resources/xml/`（若 `copytotarget=1` 还会覆盖 `target/classes/xml/`；当前工作树中 `protocol/src/main/resources/xml/` 与 `login/src/main/resources/xml/` 亦保留历史全量副本，核对生效表时以运行 classpath 为准）。

### 4.3 全工程 Maven 打包
在 `D:\work-all\game\server` 执行：
```cmd
mvn package -DskipTests
```
全工程 6 个子模块（`util`、`protocol`、`core`、`game`、`login`、`simulationclient`）编译成功，并将所有依赖 jar 包自动复制到各模块 `target/lib/` 目录下。

---

## 五、游戏服务启动 (GameServer、CrossServer 与 LoginServer)

### 5.1 启动参数详解
各进程使用的 JVM 参数：
```cmd
-Dapp.id=game (或 cross / login)
-Denv=dev
-Dapollo.cluster=SYQ
-Ddev_meta=http://test:9888
-DdefaultNetworkIp=127.0.0.1
--add-opens=java.base/java.lang=ALL-UNNAMED
--add-opens=java.base/java.util=ALL-UNNAMED
-Dio.netty.tryReflectionSetAccessible=true
-Dfile.encoding=utf-8
-Dserver.run.mode=test
-Djdk.attach.allowAttachSelf=true
-Dzookeeper.clientCnxnSocket=org.apache.zookeeper.ClientCnxnSocketNetty
-Dworkspace=D:\work-all\game\server
-Dmetafolder=D:\work-all\game\configs\Server
```
> **注意**：
> 1. Apollo 中若使用 `-Denv=dev`，客户端会自动通过 `dev_meta=http://test:9888` 从远端拉取最新配置；若传 `-Denv=local`，Apollo 会默认走纯本地文件模式，导致因缺少本地缓存报错。`run_cross.bat` 默认使用 `APOLLO_META=http://127.0.0.1:9888`（同时传 `-Dapollo.configService` 与 `-Ddev_meta`），避免受本地 `hosts` 影响。
> 2. Windows 宿主机上若存在多个虚拟网卡（如 VMware/VPN），需显式指定 `-DdefaultNetworkIp=127.0.0.1`，避免 Vert.x 集群网卡绑定失败。
> 3. **CrossServer 归属**：`CrossServer` 主类 `cn.game.games.net.cross.CrossServer` 位于 `game/` 模块中，联调公会（`Guild`）与跨服活动（`CrossActivity`）时必须连同 `GameServer` 一并启动，确保同一 ZooKeeper/Redis 集群可见。

### 5.2 启动脚本
自动化联调优先使用 `run_server.ps1 -Action Prepare` 生成独立快照，再按 [运行目标证据](../.ai-workspace/context/runtime-verification.md) 启动、核验与停止。默认 Apollo 为回环 9888 / SYQ；Start 需要确认测试数据范围，不会按主类模糊匹配结束已有服务。下列旧手工入口不自动提供严格运行身份。

已在 `D:\work-all\game\server\` 目录下创建便捷启动脚本：
1. **独立启动**：
   - **GameServer**：双击 `run_game.bat`（唯一 ID：`game_test`，WebSocket 端口：`7011`，内置 GM Web 控制台默认端口：`18080`）
   - **CrossServer**：双击 `run_cross.bat`（唯一 ID：`cross_test`，工作目录 `game/`，承载公会与跨服活动 RPC）
   - **LoginServer**：双击 `run_login.bat`（唯一 ID：`login_test`，HTTP 端口：`9390`）
2. **Windows Terminal 多页签一键启动**：
   - 双击 `run_servers_wt.bat`，会在同一终端窗口内以两个独立 Tab 启动 `GameServer` 和 `LoginServer`（如需联调公会/跨服活动，再单独启动 `run_cross.bat`）。
3. **一键停止**：
   - 双击 `stop_servers.bat`（同时匹配并停止 `game_test`、`cross_test`、`login_test` 三个本地测试进程）。

### 5.3 GM Web 控制台访问与配置 (`GmWebConsoleServer`)
`GameServer` 内置基于 Vert.x Web 的可视化 GM 与研发工作台（`GmWebConsoleServer`），集成 GM 指令自省、动态 Protobuf 发包、Trace/Diff 观测及模块双轨验收：
- **默认访问地址**：`http://127.0.0.1:18080/gm-console`
- **默认启用策略**：非生产模式（`-Dserver.run.mode=test` / `dev`）下**默认自动启动且免 Token**；生产模式（`PRODUCTION`）默认关闭且强制 Fail-Closed，严禁在生产环境无鉴权开放。
- **配置键（JVM `-D` / Apollo / 环境变量优先级以 `GmSecurityPolicy` 为准）**：
  - `gm.http.enabled` / `GM_HTTP_ENABLED`：是否启用（非生产默认 `true`，生产默认 `false`）
  - `gm.http.port` / `GM_HTTP_PORT`：监听端口（默认 `18080`；**禁止**配置为 `17011`，该端口保留给测试运行身份接口 `/runtime-identity`）
  - `gm.http.host` / `GM_HTTP_HOST`：绑定网卡地址
  - `gm.http.requireAuth` / `GM_HTTP_REQUIRE_AUTH`：是否强制 Token 鉴权（生产默认 `true`）
  - `gm.http.opsToken`（或 `gm.http.token`） / `GM_HTTP_OPS_TOKEN`：鉴权 Token（通过请求头 `X-GM-Token` 或 `Authorization` 传递）
- **外部热重载面板目录**：`server/gm-console/panels/*.html`（如 `quick-test-panel.html`），修改 HTML 后刷新页面即时生效，详见 [dev-recipes.md](../.ai-workspace/context/dev-recipes.md) 第 5 节。

---

## 六、服务状态验证

以下端口监听仅表示对应入口已建立，不能证明完整登录、协议场景或所连版本正确。完整联调需执行实际场景并核对运行身份：
- `2181`：ZooKeeper 注册中心
- `6379`：Redis 数据缓存
- `9888`：Apollo 配置中心
- `9990`：ZKUI 页面
- `7011`：GameServer WebSocket 通信端口
- `9390`：LoginServer 业务 HTTP 接口端口
- `18080`：GameServer 内置 GM Web 控制台 HTTP 端口（`http://127.0.0.1:18080/gm-console`，非生产默认开启）
- `17011`：GameServer 测试模式运行身份探针端口（`/runtime-identity`，仅显式开启 `game.runtimeIdentity.enabled=true` 时监听）
- **CrossServer 节点状态**：不对外开放客户端端口，通过 ZooKeeper / Vert.x 集群与 GM 工作台顶部集群拓扑状态徽章确认 `cross_test` 在线。
