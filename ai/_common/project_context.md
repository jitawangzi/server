# 项目全景与 API 白名单 (Project Context)

> **用途**：供 AI 在设计和编码阶段引用。
> **原则**：只列出稳定、公共、可供调用的 API，不包含具体业务逻辑实现。
> **补充说明**：由于api可能经常变化，并且有多个同方法名的重载方法，所以可能只精确到类级别，这里没有API不代表不存在，实际使用时，可能需要先定位到类，然后查看相关方法。 

## 1. 核心包结构
- `cn.game.games.module`: 业务模块 (Modules)
- `cn.game.games.net.game.module`: 协议 Handler
- `cn.game.util`: 通用工具类

## 2. API 白名单 (API Whitelist)

### 2.1 数据管理 (Managers)
*AI 在编写逻辑时，应优先调用以下单例获取配置数据：*
- `ItemManager.instance()`: 物品/道具配置 (get, getNullable)
- `PetManager.instance()`: 宠物系统配置
- `QuestManager.instance()`: 任务配置
- `ShopManager.instance()`: 商店配置
- ... (根据项目实际情况添加)

### 2.2 玩家操作辅助 (Helpers)
- `PlayerHelper.addResources(player, id, count, opType, notify)`: 通用发奖
- `PlayerHelper.delResources(player, id, count, opType)`: 通用扣除 (自动抛异常)
- `PlayerHelper.checkResource(player, id, count)`: 资源检查
- `PlayerHelper.sendProtocol(player, msg)`: 推送消息

### 2.3 工具类 (Utils)
- `DateUtil`: 时间操作 (禁止使用 System.currentTimeMillis)
- `Rnd`: 随机数生成
- `AsyncUtils`: 异步转同步控制

## 3. 现有核心模块参考
*如果新功能需要交互，可参考以下 Module 类名：*
- `PetModule`: 宠物数据
- `ItemModule`: 背包数据
- `QuestModule`: 任务数据