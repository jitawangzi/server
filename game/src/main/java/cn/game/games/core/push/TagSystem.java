package cn.game.games.core.push;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class TagSystem {
    private Map<List<String>, Set<Long>> tagToPlayers;
    private Map<Long, Set<List<String>>> playerToTags;

    public TagSystem() {
        this.tagToPlayers = new ConcurrentHashMap<>();
        this.playerToTags = new ConcurrentHashMap<>();
    }

	public void addPlayerTags(long playerId, String... tags) {
        List<String> tagList = Arrays.asList(tags);
        tagToPlayers.computeIfAbsent(tagList, k -> ConcurrentHashMap.newKeySet()).add(playerId);
        playerToTags.computeIfAbsent(playerId, k -> ConcurrentHashMap.newKeySet()).add(tagList);
    }

	public void delPlayerTags(long playerId, String... tags) {
		if (tags.length == 0) {
			// 如果没有传入标签，删除该玩家的所有标签
			Set<List<String>> allTagLists = playerToTags.remove(playerId);
			if (allTagLists != null) {
				for (List<String> tagList : allTagLists) {
					tagToPlayers.computeIfPresent(tagList, (k, players) -> {
						players.remove(playerId);
						return players.isEmpty() ? null : players;
					});
				}
			}
		} else {
			// 如果传入了特定标签，只删除这些标签
			List<String> tagList = Arrays.asList(tags);

			tagToPlayers.computeIfPresent(tagList, (k, players) -> {
				players.remove(playerId);
				return players.isEmpty() ? null : players;
			});

			playerToTags.computeIfPresent(playerId, (k, tagLists) -> {
				tagLists.remove(tagList);
				return tagLists.isEmpty() ? null : tagLists;
			});
		}
	}

    public Set<Long> getPlayersByTags(String... tags) {
        return tagToPlayers.getOrDefault(Arrays.asList(tags), Collections.emptySet());
    }

	public Set<Long> getAllPlayers() {
//		return new HashSet<>(playerToTags.keySet());
		return playerToTags.keySet();
	}
}