package cn.game.games.net.game.gm.acceptance;

import java.util.List;

/**
 * 模块级双轨自动化验收套件接口。
 * 遵循“功能契约验收 (CONTRACT)”与“多态分支 × 配表全集扫描 (MATRIX_SCAN)”双轨设计。
 *
 * @author shuyongqiang
 */
public interface ModuleAcceptanceSuite {

    /**
     * 获取被测模块号（如 0x0b 道具模块，0x15 商店模块）
     *
     * @return 模块号数值
     */
    int getModuleId();

    /**
     * 获取模块唯一英文标识符（如 "item", "shop"）
     *
     * @return 模块标识符
     */
    String getModuleKey();

    /**
     * 获取模块中文显示名称（如 "道具使用与出售分解", "全服商店、黑市、月卡与礼包"）
     *
     * @return 中文标题
     */
    String getTitle();

    /**
     * 获取本模块声明覆盖的多态业务分支键集合
     *
     * @return 多态分支键列表
     */
    List<String> getDeclaredBranches();

    /**
     * 获取本模块关联扫描的正式策划配表名称列表
     *
     * @return 配表名称列表
     */
    List<String> getRelatedTables();

    /**
     * 执行轨道一：功能契约与状态机边界用例
     *
     * @param ctx 验收执行上下文
     * @throws Exception 执行异常
     */
    void runContractCases(AcceptanceContext ctx) throws Exception;

    /**
     * 执行轨道二：多态策略分支 × 全量配表矩阵扫描用例
     *
     * @param ctx 验收执行上下文
     * @throws Exception 执行异常
     */
    void runMatrixScan(AcceptanceContext ctx) throws Exception;
}
