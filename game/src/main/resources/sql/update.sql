ALTER TABLE `t_gm_mail`
    ADD COLUMN `send_name`  varchar(255) NULL COMMENT '发送邮件的人 GM账号' AFTER `approval_timer`;



CREATE TABLE `t_invite` (
                            `id` bigint NOT NULL AUTO_INCREMENT,
                            `player_id` bigint NOT NULL COMMENT '邀请者的角色id',
                            `dst_pid` bigint NOT NULL COMMENT '被邀请者玩家id',
                            PRIMARY KEY (`id`),
                            KEY `player_id` (`player_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;



