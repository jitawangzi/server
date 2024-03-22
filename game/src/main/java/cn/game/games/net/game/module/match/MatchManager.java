package cn.game.games.net.game.module.match;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

import cn.game.games.cache.entity.Player;

public class MatchManager {

	private static MatchManager instace = new MatchManager();

	private MatchManager() {
	};

	private ConcurrentMap<Long, MatchRoom> rooms = new ConcurrentHashMap<>();
	private AtomicLong roomId = new AtomicLong(1);

	public static MatchManager getInstance() {

		return instace;
	}

	public void newRoom(Player player) {

		MatchRoom room = new MatchRoom(player, roomId.getAndIncrement());
		rooms.put(room.getId(), room);
//		player.getData().setMatchRoomId(room.getId());

	}

	public Collection<Player> match(Player player) {

		for (MatchRoom matchRoom : rooms.values()) {
			if (matchRoom.matchPlayer(player)) {
//				player.getData().setMatchRoomId(matchRoom.getId());
				return matchRoom.getPlayers();
			}
		}

		newRoom(player);

		return null;

	}

	public void finish(Player player, int score) {

//		MatchRoom room = this.rooms.get(player.getData().getMatchRoomId());
//		if (room != null) {
//
//			MatchRaceScoreResponse_07000015.Builder builder = MatchRaceScoreResponse_07000015.newBuilder();
//			builder.setScore(score);
//			builder.setId(player.getData().getId());
//
//			room.broadcast(builder, player.getData().getId());
//
//			boolean addFinishId = room.addFinishId(player);
//
//			if (addFinishId) {
//				room.clear();
//			}
//
//			player.getData().setMatchRoomId(0);
//		}

	}

	public void ready(Player player) {

//		MatchRoom matchRoom = rooms.get(player.getData().getMatchRoomId());
//		boolean ready = matchRoom.ready(player);
//		matchRoom.broadcast(MatchPlayerReadyResponse_07000010.getDefaultInstance(), player.getData().getId());
//
//		if (ready) {
//		}

	}

	public void exit(Player player) {
//		MatchRoom matchRoom = rooms.get(player.getData().getMatchRoomId());
//		if (matchRoom != null) {
//			Player removePlayer = matchRoom.removePlayer(player.getData().getId());
//			if (removePlayer != null) {
//
//				MatchPlayerExitResponse_07000007.Builder builder = MatchPlayerExitResponse_07000007.newBuilder();
//				builder.setId(player.getData().getId());
//				matchRoom.broadcast(builder.build());
//
//			}
//		}

	}

}
