ALTER TABLE `t_gm_mail`
    ADD COLUMN `send_name`  varchar(255) NULL COMMENT '发送邮件的人 GM账号' AFTER `approval_timer`;

