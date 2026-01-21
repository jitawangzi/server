 
***

# 静态数据配置指南 (Configuration Guide)

为了实现**逻辑与数据分离**的设计目标，游戏中的静态数据（策划数值）独立保存在外部文件中。程序启动时，会读取这些文件并解析为内存中的对象，供全进程读取使用。

## 1. 核心理念与命名规范

### 1.1 设计原则
*   **源文件**：策划人员维护 `.xlsx` (Excel) 文件。
*   **运行时**：程序实际读取的是由工具转化后的 `.xml` 数据文件。
*   **代码映射**：工具自动生成对应的 Java `Config` 类和 `Manager` 管理类。

### 1.2 命名规范
为了明确区分“静态配置”与“运行时逻辑对象”，采用以下命名习惯：
*   **运行时逻辑对象**：通常直接命名（如 `Quest`，代表玩家身上接取的一个具体任务实例）。
*   **静态配置对象**：添加 `Config` 后缀（如 `QuestConfig`，代表策划配置的任务模板数据）。

---

## 2. 配置表定义

### 2.1 类结构示例
以任务模块为例，`Quest.xlsx` 定义了任务的静态属性。生成器会将其解析为 `QuestConfig` 类，每行 Excel 数据对应一个对象实例。

```java
public class QuestConfig {
    public final int ID;              // 任务ID (主键)
    public final int Type;            // 任务类型，对应 QuestTypeEnum
    public final int Condition;       // 任务完成条件，对应 Condition 表ID
    public final int Reward;          // 任务奖励ID (Drop表ID)
    public final int Group;           // 任务分组 (不同类型的任务分组互斥)
    public final int[] OpenQuests;    // 完成时开启的新任务ID列表
    public final boolean IsDeleteOnFinish; // 完成后是否从列表中移除
    // ... 其他字段
}
```

### 2.2 特殊配置表
除了常规的业务数据表，还有两类特殊的配置：

#### A. 全局常量表 (GlobalConst)
用于存储全服通用的常量配置，非列表结构，直接通过静态字段访问。
```java
public class GlobalConst extends ResourceListener {
    public volatile static int UpHeroID;          // 强制下阵英雄ID
    public volatile static int[] CreateUID;       // UID创建时的参数配置
    public volatile static int PayVIPExp;         // 1元人民币兑换的VIP经验值
    public volatile static int[][] PlayerName;    // 修改名字消耗配置
}
```

#### B. 枚举定义表
用于将 Excel 中的状态定义映射为 Java 枚举，便于代码引用。
```java
public enum QuestTypeEnum {
    Daily(1, "Daily", "日常任务"),
    Weekly(2, "Weekly", "周常任务"),
    Achievement(3, "Achievement", "成就任务");
    // ...
}
```
> *注：`GlobalConst` 和枚举类通常由人工协助定义，AI 编写逻辑时可直接使用。*

---

## 3. 数据类型支持

配置表生成工具支持将 Excel 单元格内容解析为以下强类型数据：

### 3.1 基础类型
*   **数值型**：`byte`, `short`, `int`, `long`, `float`, `double`
*   **字符/布尔**：`String`, `boolean`
*   **时间日期**：`Date`, `LocalDateTime`, `CronExpression` (Cron表达式)
*   **枚举**：自定义的 Enum 类型

### 3.2 集合类型
*   **数组**：支持一维和二维数组。
    *   示例：`int[]` (例如 `1,2,3`), `int[][]` (例如 `1,1;2,2`), `LocalDateTime[]` 等。
*   **映射 (Map)**：
    *   示例：`Map<Integer, Integer>`, `Map<Byte, Integer>` 等。

---

## 4. 数据读取方式 (Manager)

每个配置表都会生成一个对应的单例 `Manager` 类（如 `QuestManager`）。
**规则**：Excel 的第一列必须是数字类型的 ID，作为默认的主键索引。
一般默认情况下，是把excel中的表数据，读取出来，组织成一个java 的map类型的结构， key为id或者索引，一般按照id或者索引读数据。 
还有一种称之为list类型的表结构，按照行顺序，读取出来一个java 中list结构的数据来保存，有序，并且随机行数据，一般如果不需要按照id或者索引取数据，
可以设置为list类型，数据也更紧凑。 

### 4.1 常用读取接口

```java
// 1. 按 ID 获取 (不存在时抛出异常，适用于逻辑上必须存在的配置)
QuestConfig config = QuestManager.instance().get(id);

// 2. 按 ID 获取 (允许返回 null，适用于尝试性查找)
QuestConfig config = QuestManager.instance().getNullable(id);

// 3. 获取全量数据列表
Collection<QuestConfig> allQuests = QuestManager.instance().list();

// 4. 获取全量数据列表，假设这个表是使用list方式存储的
List<QuestGroupConfig> allGroupQuests = QuestGroupManager.instance().list();

// 5. 获取原始索引 Map
Map<Integer, QuestConfig> allMap = QuestManager.instance().getMap();


```

### 4.2 索引查询
如果在配置中定义了索引字段（例如按 `Type` 字段索引，同时支持多字段联合索引，唯一索引），工具会生成额外的查询方法：

```java
// 按类型获取任务列表
List<QuestConfig> dailyQuests = QuestManager.instance().getTypeList(QuestTypeEnum.Daily.getId());

// 获取按类型分组的 Map
Map<Integer, List<QuestConfig>> typeMap = QuestManager.instance().getTypes();
```

> **注意**：Manager 类及其方法完全由工具生成，**禁止手动修改**。

---


## 5. Excel表结构

### 5.1 文件名规则
一般设计为.xlsx文件，文件名可以为功能名，一个.xlsx文件可以包含多个静态配置表，也就是多个sheet,sheet名规则为[配置表名]#[中文注释名]。
例如公会功能Guild.xlsx,第一个sheet为GuildBasic#公会基础信息 ，第二个sheet为GuildPermissions#公会权限，而实际通过工具生成java类时，
会生成GuildBasicConfig，GuildPermissionsConfig，增加Config后缀。 

### 5.2 表结构规则
表的结构，一共需要4行，第5行开始为数据区。 
第一行表示这个字段是客户端读取还是服务端读取，或者是都读，c/s/cs
第二行为英文字段名， 第一列的字段名始终固定叫ID,其他字段使用驼峰命名规则。 
第三行为数据类型，例如 int、int[]  等。 
第四行为中文注释说明。 
---


## 6. 开发工作流与维护

### 6.1 数据维护流程
1.  **策划编辑**：策划人员在独立的 Git 仓库中维护 `.xlsx` 源文件。
2.  **工具解析**：
    *   执行解析工具，读取 Excel。
    *   生成 Java 代码（`Config` 类, `Manager` 类, `Enum` 类, `GlobalConst` 类）。
    *   生成数据文件（`.xml`），供程序运行时读取。

### 6.2 同步至工程 (Sync)
*   **操作脚本**：`update_copy.bat`。
*   **作用**：将解析工具生成的代码和 XML 数据复制到服务器/客户端的工程目录下。
*   **操作建议**：
    该步骤建议由**人工手动执行**，而非 CI/CD 自动触发。
    *   *原因*：避免策划频繁修改或不稳定的中间数据实时同步到开发环境，导致程序运行出错。开发人员应在确认数据环境稳定后，主动执行脚本同步数据。

***
