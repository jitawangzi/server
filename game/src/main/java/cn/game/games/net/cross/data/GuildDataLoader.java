package cn.game.games.net.cross.data;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import cn.game.core.cache.id.DistributedObjectType;
import cn.game.core.db.GenericDataLoader;
import cn.game.games.cache.entity.GuildData;
import cn.game.games.net.cross.guild.GuildManager;
import cn.game.games.net.data.mapper.GuildDataMapper;

@Component
public class GuildDataLoader implements GenericDataLoader<GuildData, Long> {

	@Autowired
	private GuildDataMapper guildMapper;

	@Override
	public List<GuildData> getBatch(Long lastId, int limit) {
		return guildMapper.getBatchCursor(lastId, limit);
	}

	@Override
	public void processData(List<GuildData> data) {
		GuildManager.getInstance().loadGuildList(data);
	}

	@Override
	public Long getLastIdOfBatch(Long lastId, int limit) {
		return guildMapper.getLastIdOfBatch(lastId, limit);
	}

	@Override
	public DistributedObjectType getDistributedObjectType() {
		return DistributedObjectType.GUILD;
	}

	@Override
	public List<Long> getBatchIdCursor(Long lastId, int limit) {
		return guildMapper.getBatchIdCursor(lastId, limit);
	}

	@Override
	public GuildData load(Long id) {
		return guildMapper.selectByPrimaryKey(id);
	}

}