package cn.game.games.net.game.module.rank;

import cn.game.games.core.SimplePlayer;

public class PlayerRank {

	private RankEntry rankEntry;
	private SimplePlayer player;

	public PlayerRank(RankEntry rankEntry, SimplePlayer player) {
		this.rankEntry = rankEntry;
		this.player = player;
	}
	public RankEntry getRankEntry() {
		return rankEntry;
	}

	public void setRankEntry(RankEntry rankEntry) {
		this.rankEntry = rankEntry;
	}

	public SimplePlayer getPlayer() {
		return player;
	}

	public void setPlayer(SimplePlayer player) {
		this.player = player;
	}

}
