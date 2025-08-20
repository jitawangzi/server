package cn.game.games.net.game.module.rank;

import cn.game.games.net.cross.guild.SimpleGuild;

public class GuildRank {

	private RankEntry rankEntry;
	private SimpleGuild guild;

	public GuildRank(RankEntry rankEntry, SimpleGuild guild) {
		this.rankEntry = rankEntry;
		this.guild = guild;
	}
	public RankEntry getRankEntry() {
		return rankEntry;
	}

	public void setRankEntry(RankEntry rankEntry) {
		this.rankEntry = rankEntry;
	}
	public SimpleGuild getGuild() {
		return guild;
	}
	public void setGuild(SimpleGuild guild) {
		this.guild = guild;
	}

}
