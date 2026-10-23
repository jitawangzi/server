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
5. [游戏服务启动 (GameServer 与 LoginServer)](#五游戏服务启动-gameserver-与-loginserver)
   - [5.1 启动参数详解](#51-启动参数详解)
   - [5.2 脚本与 Windows Terminal 启动](#52-脚本与-windows-terminal-启动)
6. [服务状态排查与常见问题 (FAQ)](#六服务状态排查与常见问题-faq)

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
   脚本内部自动注入：
   ```cmd
   -Dworkspace="D:\work-all\game\server"
   -Dmetafolder="D:\work-all\game\configs\Server"
   ```
2. 该脚本自动调用 `protoc.exe` 将 `protocol/src/main/proto/*.proto` 编译至 `protocol/src/main/java`，并通过 Velocity 模板生成完整的 `PbProtocol.java`。

### 4.2 数表导出与资源拷贝
数表类与 XML 配置源自 `D:\work-all\game\configs\Server\output`：
- **Java 配置代码**：
  - `output/java/entity/` -> `protocol/src/main/java/cn/game/protocol/generated/config/`
  - `output/java/enum/` -> `protocol/src/main/java/cn/game/protocol/generated/enume/`
  - `output/java/init/` -> `protocol/src/main/java/cn/game/protocol/generated/helper/`
  - `output/java/manager/` -> `protocol/src/main/java/cn/game/protocol/generated/manager/`
- **XML 数表**：
  - `output/xml/data/*.xml` -> 同步至 `protocol/src/main/resources/xml/`、`game/src/main/resources/xml/` 及 `login/src/main/resources/xml/`。

### 4.3 全工程 Maven 打包
在 `D:\work-all\game\server` 执行：
```cmd
mvn package -DskipTests
```
全工程 7 个子模块编译成功，并将所有依赖 jar 包自动复制到各模块 `target/lib/` 目录下。

---

## 五、游戏服务启动 (GameServer 与 LoginServer)

### 5.1 启动参数详解
各进程使用的 JVM 参数：
```cmd
-Dapp.id=game (或 login)
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
> 1. Apollo 中若使用 `-Denv=dev`，客户端会自动通过 `dev_meta=http://test:9888` 从远端拉取最新配置；若传 `-Denv=local`，Apollo 会默认走纯本地文件模式，导致因缺少本地缓存报错。
> 2. Windows 宿主机上若存在多个虚拟网卡（如 VMware/VPN），需显式指定 `-DdefaultNetworkIp=127.0.0.1`，避免 Vert.x 集群网卡绑定失败。

### 5.2 启动脚本
已在 `D:\work-all\game\server\` 目录下创建便捷启动脚本：
1. **独立启动**：
   - GameServer：双击 `run_game.bat`（唯一 ID：`game_test`，WebSocket 端口：7011）
   - LoginServer：双击 `run_login.bat`（唯一 ID：`login_test`，HTTP 端口：9390）
2. **Windows Terminal 多页签一键启动**：
   - 双击 `run_servers_wt.bat`，会在同一终端窗口内以两个独立 Tab 启动 GameServer 和 LoginServer。
3. **一键停止**：
   - 双击 `stop_servers.bat`。

---

## 六、服务状态验证

运行状态下，以下端口正常监听即表示整个系统已完整跑通：
- `2181`：ZooKeeper 注册中心
- `6379`：Redis 数据缓存
- `9888`：Apollo 配置中心
- `9990`：ZKUI 页面
- `7011`：GameServer WebSocket 通信端口
- `9390`：LoginServer 业务 HTTP 接口端口
