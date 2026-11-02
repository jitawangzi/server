ALTER TABLE game.t_player_data DROP INDEX uid;
ALTER TABLE game.t_player_data ADD UNIQUE KEY uk_uid_serverid (uid, server_id);
ALTER TABLE t_player_data MODIFY COLUMN modules MEDIUMBLOB NULL COMMENT '玩家全模块数据(CBOR序列化+Snappy压缩)';
