-- 仅交付脚本，不由应用自动执行。停服并终止全部旧写入器后，由运维在 Game 库审查执行。
-- 两表只追加，不运行 UPDATE/DELETE/REPLACE/ON DUPLICATE KEY UPDATE；生产账号只授予 SELECT、INSERT。
-- order_id 为游戏全局订单 ID；sdk_order_id 按当前既有单一平台命名空间唯一，区分大小写。
-- 不具备平台单号全局唯一契约的多平台部署不能启用，须先将平台命名空间纳入协议及唯一键。
CREATE TABLE t_payment_fulfillment_claim (
    order_id BIGINT NOT NULL,
    sdk_order_id VARCHAR(191) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
    player_id BIGINT NOT NULL,
    intent MEDIUMTEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
    token CHAR(36) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
    PRIMARY KEY (order_id),
    UNIQUE KEY uk_payment_claim_sdk (sdk_order_id)
) ENGINE=InnoDB;

CREATE TABLE t_payment_fulfillment_receipt (
    order_id BIGINT NOT NULL,
    sdk_order_id VARCHAR(191) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
    player_id BIGINT NOT NULL,
    version INT NOT NULL,
    intent MEDIUMTEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
    token CHAR(36) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
    proof MEDIUMTEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
    proof_sha256 CHAR(64) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
    PRIMARY KEY (order_id),
    UNIQUE KEY uk_payment_receipt_sdk (sdk_order_id)
) ENGINE=InnoDB;
