package cn.game.games.net.game.gm.acceptance;

/**
 * 模块验收单条用例执行状态
 *
 * @author shuyongqiang
 */
public enum AcceptanceCaseStatus {
    /** 执行通过，断言与状态一致 */
    PASSED,
    /** 造数阶段异常或失败（测试夹具故障，不计入业务缺陷） */
    PROVISION_FAILED,
    /** 造数命令回包成功但前置自检未生效（夹具缺陷） */
    FIXTURE_DEFECT,
    /** 被测业务协议返回错误码或服务端抛出未捕获异常（业务或配表缺陷） */
    SUT_FAILURE,
    /** 业务协议执行成功但后续状态或资源流水断言失败（逻辑缺陷） */
    ASSERTION_FAILED;

    /**
     * 判断当前状态是否属于测试成功
     *
     * @return true表示通过
     */
    public boolean isPassed() {
        return this == PASSED;
    }

    /**
     * 判断当前状态是否属于测试脚手架故障（造数或夹具自检缺陷）
     *
     * @return true表示属于夹具问题
     */
    public boolean isFixtureIssue() {
        return this == PROVISION_FAILED || this == FIXTURE_DEFECT;
    }

    /**
     * 判断当前状态是否属于核心业务或配置缺陷
     *
     * @return true表示业务或配表缺陷
     */
    public boolean isSutDefect() {
        return this == SUT_FAILURE || this == ASSERTION_FAILED;
    }
}
