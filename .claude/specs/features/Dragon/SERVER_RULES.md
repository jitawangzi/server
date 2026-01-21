# Server Rules: Dragon Vein System (龙脉系统)

## A. Feature Overview (功能概述)
龙脉系统是一个中期付费/活跃养成线。
*   **开启条件**: 玩家等级达到巅峰1级（371级）。
*   **核心资源**: 龙魂 (Dragon Soul) - 一种不进背包的虚拟货币，主要通过击杀BOSS获得。
*   **核心玩法**: 
    1.  **节点升级**: 消耗龙魂点亮节点，获得单项基础属性。
    2.  **等级加成 (Milestone)**: 达到特定阶数/级数目标，解锁额外属性及**百分比加成**。

## B. Business State Definition (业务状态定义)
服务端需要为每个玩家维护以下**持久化状态**：
1.  **DragonVeinOrder (当前阶数)**: 默认为1阶。
2.  **DragonVeinLevel (当前级数)**: 默认为0级。每阶包含8个节点。
3.  **DragonSoul (龙魂数量)**: 玩家当前拥有的龙魂数值（非物品ID）。
4.  *(无需额外存储)*: “等级加成”的状态无需单独存储，因为它完全由 Order/Level 状态推导（例如：若 Order >= 24, Level >= 1，则自动激活对应的里程碑加成）。

## C. Dynamic Data Requirements (动态数据需求)
仅列出服务端必须下发、客户端不可推导的数据：
1.  **dragon_soul**: 当前拥有的龙魂数值。
2.  **order**: 当前龙脉阶数。
3.  **level**: 当前龙脉级数。
4.  **milestone_next_target**: (可选优化) 虽然客户端可算，但为了方便显示“当前目标/下级目标”，服务端协议中可包含“下一级里程碑所需的阶/级”信息的结构，或者让客户端完全读表。鉴于 Skill 规范，建议**不包含**，由客户端读表。

## D. Actions & Rules (操作与规则详情)

### 1. Upgrade Dragon Vein (提升龙脉)
*   **Pre-conditions (前置条件)**:
    *   功能已开启 (Level >= 371)。
    *   未达到满阶满级。
*   **Cost (消耗)**:
    *   消耗 `DragonSoul`（数量由配置表根据当前 Order/Level 决定）。
*   **Success Effect (成功结果)**:
    *   扣除对应龙魂。
    *   `level` + 1。
    *   **单点属性生效**: 当前点亮的节点属性立即生效（通常是单条属性，如“破甲+113”）。
    *   **里程碑检查 (Milestone Check)**:
        *   检查新的 `Order/Level` 是否达到了配置表中的“等级加成”目标（如 24阶1级）。
        *   **If Reached**: 激活额外的固定属性（如生命+190096）和 **百分比加成**（如龙脉加成+48%）。
    *   **自动进阶逻辑**: 如果 `level` 达到该阶上限（8级），且再次升级（或点亮第8个后自动进阶？需确认），则 `order` + 1，`level` 重置为 0。
    *   **属性重算**: 触发总属性计算。公式: `FinalAttr = (BaseAttr + MilestoneFixedAttr) * (1 + MilestonePercentBonus)`。
*   **Failure (失败处理)**:
    *   龙魂不足：提示错误。
    *   已满级：提示错误。

### 2. Dragon Soul Acquisition (龙魂产出 - BOSS掉落)
*   **Trigger (触发时机)**:
    *   世界BOSS、BOSS之家、蛮荒禁地、神兽岛内等级 ≥ 400级的BOSS死亡时。
*   **Drop Rules (分配规则)**:
    *   **判定对象**: 拥有BOSS归属权的队伍或个人。
    *   **Rule 1 (等级压制惩罚)**:
        *   检查归属者中是否存在玩家等级 ≥ BOSS掉落产出等级。
        *   **If 存在等级压制**:
            *   检查该压制玩家是否有“剩余击杀次数”（疲劳度）。
            *   **If 有次数**: 该BOSS **不产出** 龙魂 (防止高等级玩家抢低级BOSS资源)。
            *   **If 无次数**: 该BOSS **产出** 龙魂 (允许蹭资源)。
        *   **If 不存在等级压制** (所有人都 < 掉落等级):
            *   该BOSS **产出** 龙魂。
    *   **Distribution (具体分配)**:
        *   若产出龙魂，总量由BOSS ID配置决定。
        *   **平均分配**: 总量 / 归属者人数，**向上取整**。
        *   直接增加玩家 `DragonSoul` 数值，并通过系统提示通知。

## E. Global Rules & Cron (全局规则与定时任务)
无特定定时任务。

## F. Questions (待确认问题)
