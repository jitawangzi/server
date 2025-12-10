ALTER TABLE game.t_player_data DROP INDEX uid;
ALTER TABLE game.t_player_data ADD UNIQUE KEY uk_uid_serverid (uid, server_id);