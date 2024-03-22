package cn.game.core.net.steam;

import cn.game.core.net.vertx.VxHolder;
import io.vertx.codegen.annotations.Nullable;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;

public class SteamAPI {

	/** steamweb api host, needs key */
	private static final String HOST = "partner.steam-api.com";
	public static final String KEY = "8157C7C239EC859582FD3ED14D1A1BC3";
	public static final int APP_ID = 2337470;

    public static class IClientStats_1046930 {

		/** 
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void ReportEvent(Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/IClientStats_1046930/ReportEvent/v1";
			VxHolder.request(HttpMethod.POST, requestURI, null, successHandler, failedHandler);
		}
	}
    public static class ICSGOPlayers_730 {

		/** 
		 * @param steamid -- The SteamID of the user
		 * @param steamidkey -- Authentication obtained from the SteamID
		 * @param knowncode -- Previously known match sharing code obtained from the SteamID
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GetNextMatchSharingCode(long steamid,String steamidkey,String knowncode,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/ICSGOPlayers_730/GetNextMatchSharingCode/v1";
			JsonObject param = new JsonObject();
			param.put("steamid", steamid);
			param.put("steamidkey", steamidkey);
			param.put("knowncode", knowncode);
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param steamid -- The steam ID
		 * @param coin -- The coin
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GetPlayerProfileCoin(long steamid,String coin,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/ICSGOPlayers_730/GetPlayerProfileCoin/v1";
			JsonObject param = new JsonObject();
			param.put("steamid", steamid);
			param.put("coin", coin);
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}
	}
    public static class ICSGOServers_730 {

		/** 
		 * @param interval -- What recent interval is requested, possible values: day, week, month
		 * @param gamemode -- What game mode is requested, possible values: competitive, casual
		 * @param mapgroup -- What maps are requested, possible values: operation
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GetGameMapsPlaytime(String interval,String gamemode,String mapgroup,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/ICSGOServers_730/GetGameMapsPlaytime/v1";
			JsonObject param = new JsonObject();
			param.put("interval", interval);
			param.put("gamemode", gamemode);
			param.put("mapgroup", mapgroup);
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GetGameServersStatus(Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/ICSGOServers_730/GetGameServersStatus/v1";
			JsonObject param = new JsonObject();
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}
	}
    public static class ICSGOTournaments_730 {

		/** 
		 * @param event -- The event ID
		 * @param steamid -- The SteamID of the user inventory
		 * @param steamidkey -- Authentication obtained from the SteamID
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GetTournamentFantasyLineup(int event,long steamid,String steamidkey,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/ICSGOTournaments_730/GetTournamentFantasyLineup/v1";
			JsonObject param = new JsonObject();
			param.put("event", event);
			param.put("steamid", steamid);
			param.put("steamidkey", steamidkey);
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param event -- The event ID
		 * @param steamid -- The SteamID of the user inventory
		 * @param steamidkey -- Authentication obtained from the SteamID
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GetTournamentItems(int event,long steamid,String steamidkey,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/ICSGOTournaments_730/GetTournamentItems/v1";
			JsonObject param = new JsonObject();
			param.put("event", event);
			param.put("steamid", steamid);
			param.put("steamidkey", steamidkey);
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param event -- The event ID
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GetTournamentLayout(int event,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/ICSGOTournaments_730/GetTournamentLayout/v1";
			JsonObject param = new JsonObject();
			param.put("event", event);
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param event -- The event ID
		 * @param steamid -- The SteamID of the user inventory
		 * @param steamidkey -- Authentication obtained from the SteamID
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GetTournamentPredictions(int event,long steamid,String steamidkey,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/ICSGOTournaments_730/GetTournamentPredictions/v1";
			JsonObject param = new JsonObject();
			param.put("event", event);
			param.put("steamid", steamid);
			param.put("steamidkey", steamidkey);
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param event -- The event ID
		 * @param steamid -- The SteamID of the user inventory
		 * @param steamidkey -- Authentication obtained from the SteamID
		 * @param sectionid -- Event section id
		 * @param pickid0 -- PickID to select for the slot
		 * @param itemid0 -- ItemID to lock in for the pick
		 * @param pickid1 -- PickID to select for the slot
		 * @param itemid1 -- ItemID to lock in for the pick
		 * @param pickid2 -- PickID to select for the slot
		 * @param itemid2 -- ItemID to lock in for the pick
		 * @param pickid3 -- PickID to select for the slot
		 * @param itemid3 -- ItemID to lock in for the pick
		 * @param pickid4 -- PickID to select for the slot
		 * @param itemid4 -- ItemID to lock in for the pick
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void UploadTournamentFantasyLineup(int event,long steamid,String steamidkey,int sectionid,int pickid0,long itemid0,int pickid1,long itemid1,int pickid2,long itemid2,int pickid3,long itemid3,int pickid4,long itemid4,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/ICSGOTournaments_730/UploadTournamentFantasyLineup/v1";
			JsonObject param = new JsonObject();
			param.put("event", event);
			param.put("steamid", steamid);
			param.put("steamidkey", steamidkey);
			param.put("sectionid", sectionid);
			param.put("pickid0", pickid0);
			param.put("itemid0", itemid0);
			param.put("pickid1", pickid1);
			param.put("itemid1", itemid1);
			param.put("pickid2", pickid2);
			param.put("itemid2", itemid2);
			param.put("pickid3", pickid3);
			param.put("itemid3", itemid3);
			param.put("pickid4", pickid4);
			param.put("itemid4", itemid4);
			VxHolder.request(HttpMethod.POST, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param event -- The event ID
		 * @param steamid -- The SteamID of the user inventory
		 * @param steamidkey -- Authentication obtained from the SteamID
		 * @param sectionid -- Event section id
		 * @param groupid -- Event group id
		 * @param index -- Index in group
		 * @param pickid -- Pick ID to select
		 * @param itemid -- ItemID to lock in for the pick
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void UploadTournamentPredictions(int event,long steamid,String steamidkey,int sectionid,int groupid,int index,int pickid,long itemid,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/ICSGOTournaments_730/UploadTournamentPredictions/v1";
			JsonObject param = new JsonObject();
			param.put("event", event);
			param.put("steamid", steamid);
			param.put("steamidkey", steamidkey);
			param.put("sectionid", sectionid);
			param.put("groupid", groupid);
			param.put("index", index);
			param.put("pickid", pickid);
			param.put("itemid", itemid);
			VxHolder.request(HttpMethod.POST, requestURI, param, successHandler, failedHandler);
		}
	}
    public static class IDOTA2Fantasy_205790 {

		/** 
		 * @param FantasyLeagueID -- The fantasy league ID
		 * @param StartTime -- An optional filter for minimum timestamp
		 * @param EndTime -- An optional filter for maximum timestamp
		 * @param MatchID -- An optional filter for a specific match
		 * @param SeriesID -- An optional filter for a specific series
		 * @param PlayerAccountID -- An optional filter for a specific player
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GetFantasyPlayerStats(int FantasyLeagueID,@Nullable int StartTime,@Nullable int EndTime,@Nullable long MatchID,@Nullable int SeriesID,@Nullable int PlayerAccountID,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/IDOTA2Fantasy_205790/GetFantasyPlayerStats/v1";
			JsonObject param = new JsonObject();
			param.put("FantasyLeagueID", FantasyLeagueID);
			if(StartTime != 0)
				param.put("StartTime", StartTime);
			if(EndTime != 0)
				param.put("EndTime", EndTime);
			if(MatchID != 0)
				param.put("MatchID", MatchID);
			if(SeriesID != 0)
				param.put("SeriesID", SeriesID);
			if(PlayerAccountID != 0)
				param.put("PlayerAccountID", PlayerAccountID);
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param accountid -- The account ID to look up
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GetPlayerOfficialInfo(int accountid,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/IDOTA2Fantasy_205790/GetPlayerOfficialInfo/v1";
			JsonObject param = new JsonObject();
			param.put("accountid", accountid);
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GetProPlayerList(Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/IDOTA2Fantasy_205790/GetProPlayerList/v1";
			JsonObject param = new JsonObject();
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}
	}
    public static class IDOTA2MatchStats_205790 {

		/** 
		 * @param server_steam_id -- 
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GetRealtimeStats(long server_steam_id,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/IDOTA2MatchStats_205790/GetRealtimeStats/v1";
			JsonObject param = new JsonObject();
			param.put("server_steam_id", server_steam_id);
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}
	}
    public static class IDOTA2MatchStats_570 {

		/** 
		 * @param server_steam_id -- 
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GetRealtimeStats(long server_steam_id,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/IDOTA2MatchStats_570/GetRealtimeStats/v1";
			JsonObject param = new JsonObject();
			param.put("server_steam_id", server_steam_id);
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}
	}
    public static class IDOTA2Match_205790 {

		/** 
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GetLeagueListing(Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/IDOTA2Match_205790/GetLeagueListing/v1";
			JsonObject param = new JsonObject();
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param league_id -- Only show matches of the specified league id
		 * @param match_id -- Only show matches of the specified match id
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GetLiveLeagueGames(@Nullable int league_id,@Nullable long match_id,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/IDOTA2Match_205790/GetLiveLeagueGames/v1";
			JsonObject param = new JsonObject();
			if(league_id != 0)
				param.put("league_id", league_id);
			if(match_id != 0)
				param.put("match_id", match_id);
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param match_id -- Match id
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GetMatchDetails(long match_id,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/IDOTA2Match_205790/GetMatchDetails/v1";
			JsonObject param = new JsonObject();
			param.put("match_id", match_id);
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param hero_id -- The ID of the hero that must be in the matches being queried
		 * @param game_mode -- Which game mode to return matches for
		 * @param skill -- The average skill range of the match, these can be [1-3] with lower numbers being lower skill. Ignored if an account ID is specified
		 * @param min_players -- Minimum number of human players that must be in a match for it to be returned
		 * @param account_id -- An account ID to get matches from. This will fail if the user has their match history hidden
		 * @param league_id -- The league ID to return games from
		 * @param start_at_match_id -- The minimum match ID to start from
		 * @param matches_requested -- The number of requested matches to return
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GetMatchHistory(@Nullable int hero_id,@Nullable int game_mode,@Nullable int skill,@Nullable String min_players,@Nullable String account_id,@Nullable String league_id,@Nullable long start_at_match_id,@Nullable String matches_requested,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/IDOTA2Match_205790/GetMatchHistory/v1";
			JsonObject param = new JsonObject();
			if(hero_id != 0)
				param.put("hero_id", hero_id);
			if(game_mode != 0)
				param.put("game_mode", game_mode);
			if(skill != 0)
				param.put("skill", skill);
			if(min_players != null)
				param.put("min_players", min_players);
			if(account_id != null)
				param.put("account_id", account_id);
			if(league_id != null)
				param.put("league_id", league_id);
			if(start_at_match_id != 0)
				param.put("start_at_match_id", start_at_match_id);
			if(matches_requested != null)
				param.put("matches_requested", matches_requested);
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param start_at_match_seq_num -- 
		 * @param matches_requested -- 
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GetMatchHistoryBySequenceNum(@Nullable long start_at_match_seq_num,@Nullable int matches_requested,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/IDOTA2Match_205790/GetMatchHistoryBySequenceNum/v1";
			JsonObject param = new JsonObject();
			if(start_at_match_seq_num != 0)
				param.put("start_at_match_seq_num", start_at_match_seq_num);
			if(matches_requested != 0)
				param.put("matches_requested", matches_requested);
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param start_at_team_id -- 
		 * @param teams_requested -- 
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GetTeamInfoByTeamID(@Nullable long start_at_team_id,@Nullable int teams_requested,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/IDOTA2Match_205790/GetTeamInfoByTeamID/v1";
			JsonObject param = new JsonObject();
			if(start_at_team_id != 0)
				param.put("start_at_team_id", start_at_team_id);
			if(teams_requested != 0)
				param.put("teams_requested", teams_requested);
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param partner -- Which partner's games to use.
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GetTopLiveEventGame(int partner,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/IDOTA2Match_205790/GetTopLiveEventGame/v1";
			JsonObject param = new JsonObject();
			param.put("partner", partner);
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param partner -- Which partner's games to use.
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GetTopLiveGame(int partner,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/IDOTA2Match_205790/GetTopLiveGame/v1";
			JsonObject param = new JsonObject();
			param.put("partner", partner);
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param partner -- Which partner's games to use.
		 * @param home_division -- Prefer matches from this division.
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GetTopWeekendTourneyGames(int partner,@Nullable int home_division,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/IDOTA2Match_205790/GetTopWeekendTourneyGames/v1";
			JsonObject param = new JsonObject();
			param.put("partner", partner);
			if(home_division != 0)
				param.put("home_division", home_division);
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param account_id -- 
		 * @param league_id -- 
		 * @param hero_id -- 
		 * @param time_frame -- 
		 * @param match_id -- 
		 * @param phase_id -- 
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GetTournamentPlayerStats(String account_id,@Nullable String league_id,@Nullable String hero_id,@Nullable String time_frame,@Nullable long match_id,@Nullable int phase_id,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/IDOTA2Match_205790/GetTournamentPlayerStats/v1";
			JsonObject param = new JsonObject();
			param.put("account_id", account_id);
			if(league_id != null)
				param.put("league_id", league_id);
			if(hero_id != null)
				param.put("hero_id", hero_id);
			if(time_frame != null)
				param.put("time_frame", time_frame);
			if(match_id != 0)
				param.put("match_id", match_id);
			if(phase_id != 0)
				param.put("phase_id", phase_id);
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param account_id -- 
		 * @param league_id -- 
		 * @param hero_id -- 
		 * @param time_frame -- 
		 * @param match_id -- 
		 * @param phase_id -- 
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GetTournamentPlayerStatsV2(String account_id,@Nullable String league_id,@Nullable String hero_id,@Nullable String time_frame,@Nullable long match_id,@Nullable int phase_id,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/IDOTA2Match_205790/GetTournamentPlayerStats/v2";
			JsonObject param = new JsonObject();
			param.put("account_id", account_id);
			if(league_id != null)
				param.put("league_id", league_id);
			if(hero_id != null)
				param.put("hero_id", hero_id);
			if(time_frame != null)
				param.put("time_frame", time_frame);
			if(match_id != 0)
				param.put("match_id", match_id);
			if(phase_id != 0)
				param.put("phase_id", phase_id);
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}
	}
    public static class IDOTA2Match_570 {

		/** 
		 * @param league_id -- Only show matches of the specified league id
		 * @param match_id -- Only show matches of the specified match id
		 * @param dpc -- Only show matches that are part of the DPC
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GetLiveLeagueGames(@Nullable int league_id,@Nullable long match_id,@Nullable boolean dpc,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/IDOTA2Match_570/GetLiveLeagueGames/v1";
			JsonObject param = new JsonObject();
			if(league_id != 0)
				param.put("league_id", league_id);
			if(match_id != 0)
				param.put("match_id", match_id);
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param match_id -- Match id
		 * @param include_persona_names -- Include persona names as part of the response
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GetMatchDetails(long match_id,@Nullable boolean include_persona_names,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/IDOTA2Match_570/GetMatchDetails/v1";
			JsonObject param = new JsonObject();
			param.put("match_id", match_id);
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param hero_id -- The ID of the hero that must be in the matches being queried
		 * @param game_mode -- Which game mode to return matches for
		 * @param skill -- The average skill range of the match, these can be [1-3] with lower numbers being lower skill. Ignored if an account ID is specified
		 * @param min_players -- Minimum number of human players that must be in a match for it to be returned
		 * @param account_id -- An account ID to get matches from. This will fail if the user has their match history hidden
		 * @param league_id -- The league ID to return games from
		 * @param start_at_match_id -- The minimum match ID to start from
		 * @param matches_requested -- The number of requested matches to return
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GetMatchHistory(@Nullable int hero_id,@Nullable int game_mode,@Nullable int skill,@Nullable String min_players,@Nullable String account_id,@Nullable String league_id,@Nullable long start_at_match_id,@Nullable String matches_requested,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/IDOTA2Match_570/GetMatchHistory/v1";
			JsonObject param = new JsonObject();
			if(hero_id != 0)
				param.put("hero_id", hero_id);
			if(game_mode != 0)
				param.put("game_mode", game_mode);
			if(skill != 0)
				param.put("skill", skill);
			if(min_players != null)
				param.put("min_players", min_players);
			if(account_id != null)
				param.put("account_id", account_id);
			if(league_id != null)
				param.put("league_id", league_id);
			if(start_at_match_id != 0)
				param.put("start_at_match_id", start_at_match_id);
			if(matches_requested != null)
				param.put("matches_requested", matches_requested);
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param start_at_match_seq_num -- 
		 * @param matches_requested -- 
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GetMatchHistoryBySequenceNum(@Nullable long start_at_match_seq_num,@Nullable int matches_requested,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/IDOTA2Match_570/GetMatchHistoryBySequenceNum/v1";
			JsonObject param = new JsonObject();
			if(start_at_match_seq_num != 0)
				param.put("start_at_match_seq_num", start_at_match_seq_num);
			if(matches_requested != 0)
				param.put("matches_requested", matches_requested);
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param start_at_team_id -- 
		 * @param teams_requested -- 
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GetTeamInfoByTeamID(@Nullable long start_at_team_id,@Nullable int teams_requested,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/IDOTA2Match_570/GetTeamInfoByTeamID/v1";
			JsonObject param = new JsonObject();
			if(start_at_team_id != 0)
				param.put("start_at_team_id", start_at_team_id);
			if(teams_requested != 0)
				param.put("teams_requested", teams_requested);
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param partner -- Which partner's games to use.
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GetTopLiveEventGame(int partner,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/IDOTA2Match_570/GetTopLiveEventGame/v1";
			JsonObject param = new JsonObject();
			param.put("partner", partner);
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param partner -- Which partner's games to use.
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GetTopLiveGame(int partner,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/IDOTA2Match_570/GetTopLiveGame/v1";
			JsonObject param = new JsonObject();
			param.put("partner", partner);
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param partner -- Which partner's games to use.
		 * @param home_division -- Prefer matches from this division.
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GetTopWeekendTourneyGames(int partner,@Nullable int home_division,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/IDOTA2Match_570/GetTopWeekendTourneyGames/v1";
			JsonObject param = new JsonObject();
			param.put("partner", partner);
			if(home_division != 0)
				param.put("home_division", home_division);
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param account_id -- 
		 * @param league_id -- 
		 * @param hero_id -- 
		 * @param time_frame -- 
		 * @param match_id -- 
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GetTournamentPlayerStats(String account_id,@Nullable String league_id,@Nullable String hero_id,@Nullable String time_frame,@Nullable long match_id,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/IDOTA2Match_570/GetTournamentPlayerStats/v1";
			JsonObject param = new JsonObject();
			param.put("account_id", account_id);
			if(league_id != null)
				param.put("league_id", league_id);
			if(hero_id != null)
				param.put("hero_id", hero_id);
			if(time_frame != null)
				param.put("time_frame", time_frame);
			if(match_id != 0)
				param.put("match_id", match_id);
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param account_id -- 
		 * @param league_id -- 
		 * @param hero_id -- 
		 * @param time_frame -- 
		 * @param match_id -- 
		 * @param phase_id -- 
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GetTournamentPlayerStatsV2(String account_id,@Nullable String league_id,@Nullable String hero_id,@Nullable String time_frame,@Nullable long match_id,@Nullable int phase_id,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/IDOTA2Match_570/GetTournamentPlayerStats/v2";
			JsonObject param = new JsonObject();
			param.put("account_id", account_id);
			if(league_id != null)
				param.put("league_id", league_id);
			if(hero_id != null)
				param.put("hero_id", hero_id);
			if(time_frame != null)
				param.put("time_frame", time_frame);
			if(match_id != 0)
				param.put("match_id", match_id);
			if(phase_id != 0)
				param.put("phase_id", phase_id);
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}
	}
    public static class IDOTA2StreamSystem_205790 {

		/** 
		 * @param broadcaster_steam_id -- 64-bit Steam ID of the broadcaster
		 * @param league_id -- LeagueID to use if we aren't in a lobby
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GetBroadcasterInfo(long broadcaster_steam_id,@Nullable int league_id,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/IDOTA2StreamSystem_205790/GetBroadcasterInfo/v1";
			JsonObject param = new JsonObject();
			param.put("broadcaster_steam_id", broadcaster_steam_id);
			if(league_id != 0)
				param.put("league_id", league_id);
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}
	}
    public static class IDOTA2StreamSystem_570 {

		/** 
		 * @param broadcaster_steam_id -- 64-bit Steam ID of the broadcaster
		 * @param league_id -- LeagueID to use if we aren't in a lobby
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GetBroadcasterInfo(long broadcaster_steam_id,@Nullable int league_id,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/IDOTA2StreamSystem_570/GetBroadcasterInfo/v1";
			JsonObject param = new JsonObject();
			param.put("broadcaster_steam_id", broadcaster_steam_id);
			if(league_id != 0)
				param.put("league_id", league_id);
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}
	}
    public static class IDOTA2Ticket_205790 {

		/** 
		 * @param BadgeID -- The Badge ID
		 * @param ValidBadgeType1 -- Valid Badge Type 1
		 * @param ValidBadgeType2 -- Valid Badge Type 2
		 * @param ValidBadgeType3 -- Valid Badge Type 3
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void ClaimBadgeReward(String BadgeID,int ValidBadgeType1,int ValidBadgeType2,int ValidBadgeType3,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/IDOTA2Ticket_205790/ClaimBadgeReward/v1";
			JsonObject param = new JsonObject();
			param.put("BadgeID", BadgeID);
			param.put("ValidBadgeType1", ValidBadgeType1);
			param.put("ValidBadgeType2", ValidBadgeType2);
			param.put("ValidBadgeType3", ValidBadgeType3);
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param BadgeID -- The badge ID
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GetSteamIDForBadgeID(String BadgeID,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/IDOTA2Ticket_205790/GetSteamIDForBadgeID/v1";
			JsonObject param = new JsonObject();
			param.put("BadgeID", BadgeID);
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param steamid -- The 64-bit Steam ID
		 * @param BadgeType -- Badge Type
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void SetSteamAccountPurchased(long steamid,int BadgeType,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/IDOTA2Ticket_205790/SetSteamAccountPurchased/v1";
			JsonObject param = new JsonObject();
			param.put("steamid", steamid);
			param.put("BadgeType", BadgeType);
			VxHolder.request(HttpMethod.POST, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param steamid -- The 64-bit Steam ID
		 * @param ValidBadgeType1 -- Valid Badge Type 1
		 * @param ValidBadgeType2 -- Valid Badge Type 2
		 * @param ValidBadgeType3 -- Valid Badge Type 3
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void SteamAccountValidForBadgeType(long steamid,int ValidBadgeType1,int ValidBadgeType2,int ValidBadgeType3,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/IDOTA2Ticket_205790/SteamAccountValidForBadgeType/v1";
			JsonObject param = new JsonObject();
			param.put("steamid", steamid);
			param.put("ValidBadgeType1", ValidBadgeType1);
			param.put("ValidBadgeType2", ValidBadgeType2);
			param.put("ValidBadgeType3", ValidBadgeType3);
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}
	}
    public static class IDOTA2Ticket_570 {

		/** 
		 * @param BadgeID -- The badge ID
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GetSteamIDForBadgeID(String BadgeID,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/IDOTA2Ticket_570/GetSteamIDForBadgeID/v1";
			JsonObject param = new JsonObject();
			param.put("BadgeID", BadgeID);
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param steamid -- The 64-bit Steam ID
		 * @param BadgeType -- Badge Type
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void SetSteamAccountPurchased(long steamid,int BadgeType,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/IDOTA2Ticket_570/SetSteamAccountPurchased/v1";
			JsonObject param = new JsonObject();
			param.put("steamid", steamid);
			param.put("BadgeType", BadgeType);
			VxHolder.request(HttpMethod.POST, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param steamid -- The 64-bit Steam ID
		 * @param ValidBadgeType1 -- Valid Badge Type 1
		 * @param ValidBadgeType2 -- Valid Badge Type 2
		 * @param ValidBadgeType3 -- Valid Badge Type 3
		 * @param ValidBadgeType4 -- Valid Badge Type 4
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void SteamAccountValidForBadgeType(long steamid,int ValidBadgeType1,int ValidBadgeType2,int ValidBadgeType3,@Nullable int ValidBadgeType4,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/IDOTA2Ticket_570/SteamAccountValidForBadgeType/v1";
			JsonObject param = new JsonObject();
			param.put("steamid", steamid);
			param.put("ValidBadgeType1", ValidBadgeType1);
			param.put("ValidBadgeType2", ValidBadgeType2);
			param.put("ValidBadgeType3", ValidBadgeType3);
			if(ValidBadgeType4 != 0)
				param.put("ValidBadgeType4", ValidBadgeType4);
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}
	}
    public static class IEconDOTA2_205790 {

		/** 
		 * @param eventid -- The League ID of the compendium you're looking for.
		 * @param accountid -- The account ID to look up.
		 * @param language -- The language to provide hero names in.
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GetEventStatsForAccount(int eventid,int accountid,@Nullable String language,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/IEconDOTA2_205790/GetEventStatsForAccount/v1";
			JsonObject param = new JsonObject();
			param.put("eventid", eventid);
			param.put("accountid", accountid);
			if(language != null)
				param.put("language", language);
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param language -- The language to provide item names in.
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GetGameItems(@Nullable String language,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/IEconDOTA2_205790/GetGameItems/v1";
			JsonObject param = new JsonObject();
			if(language != null)
				param.put("language", language);
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param language -- The language to provide hero names in.
		 * @param itemizedonly -- Return a list of itemized heroes only.
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GetHeroes(@Nullable String language,@Nullable boolean itemizedonly,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/IEconDOTA2_205790/GetHeroes/v1";
			JsonObject param = new JsonObject();
			if(language != null)
				param.put("language", language);
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param iconname -- The item icon name to get the CDN path of
		 * @param icontype -- The type of image you want. 0 = normal, 1 = large, 2 = ingame
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GetItemIconPath(String iconname,@Nullable int icontype,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/IEconDOTA2_205790/GetItemIconPath/v1";
			JsonObject param = new JsonObject();
			param.put("iconname", iconname);
			if(icontype != 0)
				param.put("icontype", icontype);
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param language -- The language to provide rarity names in.
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GetRarities(@Nullable String language,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/IEconDOTA2_205790/GetRarities/v1";
			JsonObject param = new JsonObject();
			if(language != null)
				param.put("language", language);
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param leagueid -- The ID of the league to get the prize pool of
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GetTournamentPrizePool(@Nullable int leagueid,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/IEconDOTA2_205790/GetTournamentPrizePool/v1";
			JsonObject param = new JsonObject();
			if(leagueid != 0)
				param.put("leagueid", leagueid);
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}
	}
    public static class IEconDOTA2_570 {

		/** 
		 * @param eventid -- The Event ID of the event you're looking for.
		 * @param accountid -- The account ID to look up.
		 * @param language -- The language to provide hero names in.
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GetEventStatsForAccount(int eventid,int accountid,@Nullable String language,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/IEconDOTA2_570/GetEventStatsForAccount/v1";
			JsonObject param = new JsonObject();
			param.put("eventid", eventid);
			param.put("accountid", accountid);
			if(language != null)
				param.put("language", language);
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param language -- The language to provide item names in.
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GetGameItems(@Nullable String language,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/IEconDOTA2_570/GetGameItems/v1";
			JsonObject param = new JsonObject();
			if(language != null)
				param.put("language", language);
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param language -- The language to provide hero names in.
		 * @param itemizedonly -- Return a list of itemized heroes only.
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GetHeroes(@Nullable String language,@Nullable boolean itemizedonly,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/IEconDOTA2_570/GetHeroes/v1";
			JsonObject param = new JsonObject();
			if(language != null)
				param.put("language", language);
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param itemdef -- The item definition to get creator information for.
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GetItemCreators(int itemdef,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/IEconDOTA2_570/GetItemCreators/v1";
			JsonObject param = new JsonObject();
			param.put("itemdef", itemdef);
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param itemdef -- The item definition to get published file ids for.
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GetItemWorkshopPublishedFileIDs(int itemdef,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/IEconDOTA2_570/GetItemWorkshopPublishedFileIDs/v1";
			JsonObject param = new JsonObject();
			param.put("itemdef", itemdef);
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param language -- The language to provide rarity names in.
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GetRarities(@Nullable String language,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/IEconDOTA2_570/GetRarities/v1";
			JsonObject param = new JsonObject();
			if(language != null)
				param.put("language", language);
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param leagueid -- The ID of the league to get the prize pool of
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GetTournamentPrizePool(@Nullable int leagueid,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/IEconDOTA2_570/GetTournamentPrizePool/v1";
			JsonObject param = new JsonObject();
			if(leagueid != 0)
				param.put("leagueid", leagueid);
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}
	}
    public static class IEconItems_1046930 {

		/** 
		 * @param steamid -- The Steam ID to fetch items for
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GetPlayerItems(long steamid,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/IEconItems_1046930/GetPlayerItems/v1";
			JsonObject param = new JsonObject();
			param.put("steamid", steamid);
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}
	}
    public static class IEconItems_1269260 {

		/** 
		 * @param steamid -- The Steam ID to fetch items for
		 * @param class_id -- Return items equipped for this class id
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GetEquippedPlayerItems(long steamid,int class_id,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/IEconItems_1269260/GetEquippedPlayerItems/v1";
			JsonObject param = new JsonObject();
			param.put("steamid", steamid);
			param.put("class_id", class_id);
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}
	}
    public static class IEconItems_205790 {

		/** 
		 * @param steamid -- The Steam ID to fetch items for
		 * @param class_id -- Return items equipped for this class id
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GetEquippedPlayerItems(long steamid,int class_id,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/IEconItems_205790/GetEquippedPlayerItems/v1";
			JsonObject param = new JsonObject();
			param.put("steamid", steamid);
			param.put("class_id", class_id);
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param steamid -- The Steam ID to fetch items for
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GetPlayerItems(long steamid,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/IEconItems_205790/GetPlayerItems/v1";
			JsonObject param = new JsonObject();
			param.put("steamid", steamid);
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GetSchemaURL(Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/IEconItems_205790/GetSchemaURL/v1";
			JsonObject param = new JsonObject();
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param language -- The language to results in.
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GetStoreMetaData(@Nullable String language,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/IEconItems_205790/GetStoreMetaData/v1";
			JsonObject param = new JsonObject();
			if(language != null)
				param.put("language", language);
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}
	}
    public static class IEconItems_221540 {

		/** 
		 * @param steamid -- The Steam ID to fetch items for
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GetPlayerItems(long steamid,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/IEconItems_221540/GetPlayerItems/v1";
			JsonObject param = new JsonObject();
			param.put("steamid", steamid);
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}
	}
    public static class IEconItems_238460 {

		/** 
		 * @param steamid -- The Steam ID to fetch items for
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GetPlayerItems(long steamid,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/IEconItems_238460/GetPlayerItems/v1";
			JsonObject param = new JsonObject();
			param.put("steamid", steamid);
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}
	}
    public static class IEconItems_440 {

		/** 
		 * @param steamid -- The Steam ID to fetch items for
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GetPlayerItems(long steamid,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/IEconItems_440/GetPlayerItems/v1";
			JsonObject param = new JsonObject();
			param.put("steamid", steamid);
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param language -- The language to return the names in. Defaults to returning string keys.
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GetSchema(@Nullable String language,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/IEconItems_440/GetSchema/v1";
			JsonObject param = new JsonObject();
			if(language != null)
				param.put("language", language);
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param language -- The language to return the names in. Defaults to returning string keys.
		 * @param start -- The first item id to return. Defaults to 0. Response will indicate next value to query if applicable.
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GetSchemaItems(@Nullable String language,@Nullable int start,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/IEconItems_440/GetSchemaItems/v1";
			JsonObject param = new JsonObject();
			if(language != null)
				param.put("language", language);
			if(start != 0)
				param.put("start", start);
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param language -- The language to return the names in. Defaults to returning string keys.
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GetSchemaOverview(@Nullable String language,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/IEconItems_440/GetSchemaOverview/v1";
			JsonObject param = new JsonObject();
			if(language != null)
				param.put("language", language);
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GetSchemaURL(Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/IEconItems_440/GetSchemaURL/v1";
			JsonObject param = new JsonObject();
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param language -- The language to results in.
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GetStoreMetaData(@Nullable String language,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/IEconItems_440/GetStoreMetaData/v1";
			JsonObject param = new JsonObject();
			if(language != null)
				param.put("language", language);
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GetStoreStatus(Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/IEconItems_440/GetStoreStatus/v1";
			JsonObject param = new JsonObject();
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}
	}
    public static class IEconItems_570 {

		/** 
		 * @param steamid -- The Steam ID to fetch items for
		 * @param class_id -- Return items equipped for this class id
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GetEquippedPlayerItems(long steamid,int class_id,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/IEconItems_570/GetEquippedPlayerItems/v1";
			JsonObject param = new JsonObject();
			param.put("steamid", steamid);
			param.put("class_id", class_id);
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param steamid -- The Steam ID to fetch items for
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GetPlayerItems(long steamid,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/IEconItems_570/GetPlayerItems/v1";
			JsonObject param = new JsonObject();
			param.put("steamid", steamid);
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param language -- The language to results in.
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GetStoreMetaData(@Nullable String language,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/IEconItems_570/GetStoreMetaData/v1";
			JsonObject param = new JsonObject();
			if(language != null)
				param.put("language", language);
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}
	}
    public static class IEconItems_583950 {

		/** 
		 * @param steamid -- The Steam ID to fetch items for
		 * @param class_id -- Return items equipped for this class id
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GetEquippedPlayerItems(long steamid,int class_id,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/IEconItems_583950/GetEquippedPlayerItems/v1";
			JsonObject param = new JsonObject();
			param.put("steamid", steamid);
			param.put("class_id", class_id);
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}
	}
    public static class IEconItems_620 {

		/** 
		 * @param steamid -- The Steam ID to fetch items for
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GetPlayerItems(long steamid,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/IEconItems_620/GetPlayerItems/v1";
			JsonObject param = new JsonObject();
			param.put("steamid", steamid);
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param language -- The language to return the names in. Defaults to returning string keys.
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GetSchema(@Nullable String language,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/IEconItems_620/GetSchema/v1";
			JsonObject param = new JsonObject();
			if(language != null)
				param.put("language", language);
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}
	}
    public static class IEconItems_730 {

		/** 
		 * @param steamid -- The Steam ID to fetch items for
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GetPlayerItems(long steamid,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/IEconItems_730/GetPlayerItems/v1";
			JsonObject param = new JsonObject();
			param.put("steamid", steamid);
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param language -- The language to return the names in. Defaults to returning string keys.
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GetSchemaV2(@Nullable String language,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/IEconItems_730/GetSchema/v2";
			JsonObject param = new JsonObject();
			if(language != null)
				param.put("language", language);
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GetSchemaURLV2(Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/IEconItems_730/GetSchemaURL/v2";
			JsonObject param = new JsonObject();
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param language -- The language to results in.
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GetStoreMetaData(@Nullable String language,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/IEconItems_730/GetStoreMetaData/v1";
			JsonObject param = new JsonObject();
			if(language != null)
				param.put("language", language);
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}
	}
    public static class IGameInventory {

		/** 
		 * @param appid -- appid of game
		 * @param steamid -- The steam ID of the account to operate on
		 * @param command -- The command to run on that asset
		 * @param contextid -- The context to fetch history for
		 * @param arguments -- The arguments that were provided with the command in the first place
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GetHistoryCommandDetails(int appid,long steamid,String command,long contextid,String arguments,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/IGameInventory/GetHistoryCommandDetails/v1";
			JsonObject param = new JsonObject();
			param.put("appid", appid);
			param.put("steamid", steamid);
			param.put("command", command);
			param.put("contextid", contextid);
			param.put("arguments", arguments);
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param appid -- appid of game
		 * @param steamid -- The Steam ID to fetch history for
		 * @param contextid -- The context to fetch history for
		 * @param starttime -- Start time of the history range to collect
		 * @param endtime -- End time of the history range to collect
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GetUserHistory(int appid,long steamid,long contextid,int starttime,int endtime,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/IGameInventory/GetUserHistory/v1";
			JsonObject param = new JsonObject();
			param.put("appid", appid);
			param.put("steamid", steamid);
			param.put("contextid", contextid);
			param.put("starttime", starttime);
			param.put("endtime", endtime);
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param appid -- appid of game
		 * @param steamid -- The asset ID to operate on
		 * @param contextid -- The context to fetch history for
		 * @param actorid -- A unique 32 bit ID for the support person executing the command
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void HistoryExecuteCommands(int appid,long steamid,long contextid,int actorid,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/IGameInventory/HistoryExecuteCommands/v1";
			JsonObject param = new JsonObject();
			param.put("appid", appid);
			param.put("steamid", steamid);
			param.put("contextid", contextid);
			param.put("actorid", actorid);
			VxHolder.request(HttpMethod.POST, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param appid -- appid of game
		 * @param assetid -- The asset ID to operate on
		 * @param contextid -- The context to fetch history for
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void SupportGetAssetHistory(int appid,long assetid,long contextid,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/IGameInventory/SupportGetAssetHistory/v1";
			JsonObject param = new JsonObject();
			param.put("appid", appid);
			param.put("assetid", assetid);
			param.put("contextid", contextid);
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}
	}
    public static class IGCVersion_1046930 {

		/** 
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GetClientVersion(Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/IGCVersion_1046930/GetClientVersion/v1";
			JsonObject param = new JsonObject();
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GetServerVersion(Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/IGCVersion_1046930/GetServerVersion/v1";
			JsonObject param = new JsonObject();
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}
	}
    public static class IGCVersion_1269260 {

		/** 
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GetClientVersion(Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/IGCVersion_1269260/GetClientVersion/v1";
			JsonObject param = new JsonObject();
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GetServerVersion(Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/IGCVersion_1269260/GetServerVersion/v1";
			JsonObject param = new JsonObject();
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}
	}
    public static class IGCVersion_205790 {

		/** 
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GetClientVersion(Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/IGCVersion_205790/GetClientVersion/v1";
			JsonObject param = new JsonObject();
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GetServerVersion(Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/IGCVersion_205790/GetServerVersion/v1";
			JsonObject param = new JsonObject();
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}
	}
    public static class IGCVersion_440 {

		/** 
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GetClientVersion(Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/IGCVersion_440/GetClientVersion/v1";
			JsonObject param = new JsonObject();
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GetServerVersion(Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/IGCVersion_440/GetServerVersion/v1";
			JsonObject param = new JsonObject();
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}
	}
    public static class IGCVersion_570 {

		/** 
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GetClientVersion(Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/IGCVersion_570/GetClientVersion/v1";
			JsonObject param = new JsonObject();
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GetServerVersion(Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/IGCVersion_570/GetServerVersion/v1";
			JsonObject param = new JsonObject();
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}
	}
    public static class IGCVersion_583950 {

		/** 
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GetClientVersion(Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/IGCVersion_583950/GetClientVersion/v1";
			JsonObject param = new JsonObject();
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GetServerVersion(Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/IGCVersion_583950/GetServerVersion/v1";
			JsonObject param = new JsonObject();
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}
	}
    public static class IGCVersion_730 {

		/** 
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GetServerVersion(Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/IGCVersion_730/GetServerVersion/v1";
			JsonObject param = new JsonObject();
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}
	}
    public static class IPortal2Leaderboards_620 {

		/** 
		 * @param leaderboardName -- The leaderboard name to fetch data for.
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GetBucketizedData(String leaderboardName,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/IPortal2Leaderboards_620/GetBucketizedData/v1";
			JsonObject param = new JsonObject();
			param.put("leaderboardName", leaderboardName);
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}
	}
    public static class ISteamApps {

		/** 
		 * @param appid -- AppID of game
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GetAppBetas(int appid,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/ISteamApps/GetAppBetas/v1";
			JsonObject param = new JsonObject();
			param.put("appid", appid);
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param appid -- AppID of game
		 * @param count -- # of builds to retrieve (default 10)
		 * @param depot_details -- True if we want the info on the depots in each build.  False if we don't need that info.  Defaults to true.
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GetAppBuilds(int appid,@Nullable int count,@Nullable boolean depot_details,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/ISteamApps/GetAppBuilds/v1";
			JsonObject param = new JsonObject();
			param.put("appid", appid);
			if(count != 0)
				param.put("count", count);
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param appid -- AppID of depot
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GetAppDepotVersions(int appid,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/ISteamApps/GetAppDepotVersions/v1";
			JsonObject param = new JsonObject();
			param.put("appid", appid);
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GetAppList(Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/ISteamApps/GetAppList/v1";
			JsonObject param = new JsonObject();
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GetAppListV2(Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/ISteamApps/GetAppList/v2";
			JsonObject param = new JsonObject();
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param appid -- AppID of game
		 * @param timebegin -- Time range begin
		 * @param timeend -- Time range end
		 * @param includereports -- include reports that were not bans
		 * @param includebans -- include reports that were bans
		 * @param reportidmin -- minimum report id
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GetCheatingReports(int appid,int timebegin,int timeend,boolean includereports,boolean includebans,@Nullable long reportidmin,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/ISteamApps/GetCheatingReports/v1";
			JsonObject param = new JsonObject();
			param.put("appid", appid);
			param.put("timebegin", timebegin);
			param.put("timeend", timeend);
			param.put("includereports", includereports);
			param.put("includebans", includebans);
			if(reportidmin != 0)
				param.put("reportidmin", reportidmin);
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param key -- access key
		 * @param type_filter -- Filter app results by type. Can be comman separated, eg: games,dlc
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GetPartnerAppListForWebAPIKey(String key,@Nullable String type_filter,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/ISteamApps/GetPartnerAppListForWebAPIKey/v1";
			JsonObject param = new JsonObject();
			param.put("key", key);
			if(type_filter != null)
				param.put("type_filter", type_filter);
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param appid -- AppID of game
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GetPlayersBanned(int appid,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/ISteamApps/GetPlayersBanned/v1";
			JsonObject param = new JsonObject();
			param.put("appid", appid);
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param appid -- AppID of game
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GetSDRConfig(int appid,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/ISteamApps/GetSDRConfig/v1";
			JsonObject param = new JsonObject();
			param.put("appid", appid);
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param appid -- AppID of game
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GetSDRConfigV2(int appid,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/ISteamApps/GetSDRConfig/v2";
			JsonObject param = new JsonObject();
			param.put("appid", appid);
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param filter -- Query filter string
		 * @param limit -- Limit number of servers in the response
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GetServerList(@Nullable String filter,@Nullable int limit,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/ISteamApps/GetServerList/v1";
			JsonObject param = new JsonObject();
			if(filter != null)
				param.put("filter", filter);
			if(limit != 0)
				param.put("limit", limit);
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param addr -- IP or IP:queryport to list
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GetServersAtAddress(String addr,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/ISteamApps/GetServersAtAddress/v1";
			JsonObject param = new JsonObject();
			param.put("addr", addr);
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param appid -- AppID of game
		 * @param buildid -- BuildID
		 * @param betakey -- beta key, required. Use public for default branch
		 * @param description -- optional description for this build
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void SetAppBuildLive(int appid,int buildid,String betakey,@Nullable String description,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/ISteamApps/SetAppBuildLive/v1";
			JsonObject param = new JsonObject();
			param.put("appid", appid);
			param.put("buildid", buildid);
			param.put("betakey", betakey);
			if(description != null)
				param.put("description", description);
			VxHolder.request(HttpMethod.POST, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param appid -- AppID of game
		 * @param version -- The installed version of the game
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void UpToDateCheck(int appid,int version,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/ISteamApps/UpToDateCheck/v1";
			JsonObject param = new JsonObject();
			param.put("appid", appid);
			param.put("version", version);
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}
	}
    public static class ISteamBroadcast {

		/** 
		 * @param steamid -- Steam ID of the broadcaster
		 * @param sessionid -- Broadcast Session ID
		 * @param token -- Viewer token
		 * @param stream -- video stream representation watching
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void ViewerHeartbeat(long steamid,long sessionid,long token,@Nullable int stream,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/ISteamBroadcast/ViewerHeartbeat/v1";
			JsonObject param = new JsonObject();
			param.put("steamid", steamid);
			param.put("sessionid", sessionid);
			param.put("token", token);
			if(stream != 0)
				param.put("stream", stream);
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}
	}
    public static class ISteamCDN {

		/** 
		 * @param key -- access key
		 * @param cdnname -- Steam name of CDN property
		 * @param allowedipblocks -- comma-separated list of allowed IP address blocks in CIDR format - blank for not used
		 * @param allowedasns -- comma-separated list of allowed client network AS numbers - blank for not used
		 * @param allowedipcountries -- comma-separated list of allowed client IP country codes in ISO 3166-1 format - blank for not used
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void SetClientFilters(String key,String cdnname,@Nullable String allowedipblocks,@Nullable String allowedasns,@Nullable String allowedipcountries,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/ISteamCDN/SetClientFilters/v1";
			JsonObject param = new JsonObject();
			param.put("key", key);
			param.put("cdnname", cdnname);
			if(allowedipblocks != null)
				param.put("allowedipblocks", allowedipblocks);
			if(allowedasns != null)
				param.put("allowedasns", allowedasns);
			if(allowedipcountries != null)
				param.put("allowedipcountries", allowedipcountries);
			VxHolder.request(HttpMethod.POST, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param key -- access key
		 * @param cdnname -- Steam name of CDN property
		 * @param mbps_sent -- Outgoing network traffic in Mbps
		 * @param mbps_recv -- Incoming network traffic in Mbps
		 * @param cpu_percent -- Percent CPU load
		 * @param cache_hit_percent -- Percent cache hits
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void SetPerformanceStats(String key,String cdnname,@Nullable int mbps_sent,@Nullable int mbps_recv,@Nullable int cpu_percent,@Nullable int cache_hit_percent,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/ISteamCDN/SetPerformanceStats/v1";
			JsonObject param = new JsonObject();
			param.put("key", key);
			param.put("cdnname", cdnname);
			if(mbps_sent != 0)
				param.put("mbps_sent", mbps_sent);
			if(mbps_recv != 0)
				param.put("mbps_recv", mbps_recv);
			if(cpu_percent != 0)
				param.put("cpu_percent", cpu_percent);
			if(cache_hit_percent != 0)
				param.put("cache_hit_percent", cache_hit_percent);
			VxHolder.request(HttpMethod.POST, requestURI, param, successHandler, failedHandler);
		}
	}
    public static class ISteamCommunity {

		/** 
		 * @param key -- access key
		 * @param steamidActor -- SteamID of user doing the reporting
		 * @param steamidTarget -- SteamID of the entity being accused of abuse
		 * @param appid -- AppID to check for ownership
		 * @param abuseType -- Abuse type code (see EAbuseReportType enum)
		 * @param contentType -- Content type code (see ECommunityContentType enum)
		 * @param description -- Narrative from user
		 * @param gid -- GID of related record (depends on content type)
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void ReportAbuse(String key,long steamidActor,long steamidTarget,int appid,int abuseType,int contentType,String description,@Nullable long gid,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/ISteamCommunity/ReportAbuse/v1";
			JsonObject param = new JsonObject();
			param.put("key", key);
			param.put("steamidActor", steamidActor);
			param.put("steamidTarget", steamidTarget);
			param.put("appid", appid);
			param.put("abuseType", abuseType);
			param.put("contentType", contentType);
			param.put("description", description);
			if(gid != 0)
				param.put("gid", gid);
			VxHolder.request(HttpMethod.POST, requestURI, param, successHandler, failedHandler);
		}
	}
    public static class ISteamDirectory {

		/** 
		 * @param cellid -- Client's Steam cell ID
		 * @param maxcount -- Max number of servers to return
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GetCMList(int cellid,@Nullable int maxcount,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/ISteamDirectory/GetCMList/v1";
			JsonObject param = new JsonObject();
			param.put("cellid", cellid);
			if(maxcount != 0)
				param.put("maxcount", maxcount);
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param cellid -- Client's Steam cell ID, uses IP location if blank
		 * @param cmtype -- Optional CM type filter
		 * @param realm -- Optional Steam Realm filter
		 * @param maxcount -- Max number of servers to return
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GetCMListForConnect(@Nullable int cellid,@Nullable String cmtype,@Nullable String realm,@Nullable int maxcount,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/ISteamDirectory/GetCMListForConnect/v1";
			JsonObject param = new JsonObject();
			if(cellid != 0)
				param.put("cellid", cellid);
			if(cmtype != null)
				param.put("cmtype", cmtype);
			if(realm != null)
				param.put("realm", realm);
			if(maxcount != 0)
				param.put("maxcount", maxcount);
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GetSteamPipeDomains(Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/ISteamDirectory/GetSteamPipeDomains/v1";
			JsonObject param = new JsonObject();
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}
	}
    public static class ISteamEconomy {

		/** 
		 * @param appid -- That the key is associated with. Must be a steam economy app.
		 * @param steamid -- SteamID of user attempting to initiate a trade
		 * @param targetid -- SteamID of user that is the target of the trade invitation
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void CanTrade(int appid,long steamid,long targetid,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/ISteamEconomy/CanTrade/v1";
			JsonObject param = new JsonObject();
			param.put("appid", appid);
			param.put("steamid", steamid);
			param.put("targetid", targetid);
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param appid -- The app ID the user is buying assets for
		 * @param steamid -- SteamID of the user making a purchase
		 * @param txnid -- The transaction ID
		 * @param language -- The local language for the user
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void FinalizeAssetTransaction(int appid,long steamid,String txnid,String language,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/ISteamEconomy/FinalizeAssetTransaction/v1";
			JsonObject param = new JsonObject();
			param.put("appid", appid);
			param.put("steamid", steamid);
			param.put("txnid", txnid);
			param.put("language", language);
			VxHolder.request(HttpMethod.POST, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param appid -- Must be a steam economy app.
		 * @param language -- The user's local language
		 * @param class_count -- Number of classes requested. Must be at least one.
		 * @param classid0 -- Class ID of the nth class.
		 * @param instanceid0 -- Instance ID of the nth class.
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GetAssetClassInfo(int appid,@Nullable String language,int class_count,long classid0,@Nullable long instanceid0,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/ISteamEconomy/GetAssetClassInfo/v1";
			JsonObject param = new JsonObject();
			param.put("appid", appid);
			if(language != null)
				param.put("language", language);
			param.put("class_count", class_count);
			param.put("classid0", classid0);
			if(instanceid0 != 0)
				param.put("instanceid0", instanceid0);
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param appid -- Must be a steam economy app.
		 * @param currency -- The currency to filter for
		 * @param language -- The user's local language
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GetAssetPrices(int appid,@Nullable String currency,@Nullable String language,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/ISteamEconomy/GetAssetPrices/v1";
			JsonObject param = new JsonObject();
			param.put("appid", appid);
			if(currency != null)
				param.put("currency", currency);
			if(language != null)
				param.put("language", language);
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param steamid -- SteamID of user
		 * @param appid -- The app to get exported items from.
		 * @param contextid -- The context in the app to get exported items from.
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GetExportedAssetsForUser(long steamid,int appid,long contextid,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/ISteamEconomy/GetExportedAssetsForUser/v1";
			JsonObject param = new JsonObject();
			param.put("steamid", steamid);
			param.put("appid", appid);
			param.put("contextid", contextid);
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param appid -- Must be a steam economy app.
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GetMarketPrices(int appid,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/ISteamEconomy/GetMarketPrices/v1";
			JsonObject param = new JsonObject();
			param.put("appid", appid);
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param appid -- The app ID the user is buying assets for
		 * @param steamid -- SteamID of user making a purchase
		 * @param assetid0 -- The ID of the first asset the user is buying - there must be at least one
		 * @param assetquantity0 -- The quantity of assetid0's the the user is buying
		 * @param currency -- The local currency for the user
		 * @param language -- The local language for the user
		 * @param ipaddress -- The user's IP address
		 * @param referrer -- The referring URL
		 * @param clientauth -- If true (default is false), the authorization will appear in the user's steam client overlay, rather than as a web page - useful for stores that are embedded in products.
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void StartAssetTransaction(int appid,long steamid,String assetid0,int assetquantity0,String currency,String language,String ipaddress,@Nullable String referrer,@Nullable boolean clientauth,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/ISteamEconomy/StartAssetTransaction/v1";
			JsonObject param = new JsonObject();
			param.put("appid", appid);
			param.put("steamid", steamid);
			param.put("assetid0", assetid0);
			param.put("assetquantity0", assetquantity0);
			param.put("currency", currency);
			param.put("language", language);
			param.put("ipaddress", ipaddress);
			if(referrer != null)
				param.put("referrer", referrer);
			VxHolder.request(HttpMethod.POST, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param appid -- That the key is associated with. Must be a steam economy app.
		 * @param partya -- SteamID of first user in the trade
		 * @param partyb -- SteamID of second user in the trade
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void StartTrade(int appid,long partya,long partyb,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/ISteamEconomy/StartTrade/v1";
			JsonObject param = new JsonObject();
			param.put("appid", appid);
			param.put("partya", partya);
			param.put("partyb", partyb);
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}
	}
    public static class ISteamGameServerStats {

		/** 
		 * @param key -- access key
		 * @param gameid -- game id to get stats for, if not a mod, it's safe to use appid here
		 * @param appid -- appID of the game
		 * @param rangestart -- range start date/time (Format: YYYY-MM-DD HH:MM:SS, seattle local time
		 * @param rangeend -- range end date/time (Format: YYYY-MM-DD HH:MM:SS, seattle local time
		 * @param maxresults -- Max number of results to return (up to 1000)
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GetGameServerPlayerStatsForGame(String key,long gameid,int appid,String rangestart,String rangeend,@Nullable int maxresults,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/ISteamGameServerStats/GetGameServerPlayerStatsForGame/v1";
			JsonObject param = new JsonObject();
			param.put("key", key);
			param.put("gameid", gameid);
			param.put("appid", appid);
			param.put("rangestart", rangestart);
			param.put("rangeend", rangeend);
			if(maxresults != 0)
				param.put("maxresults", maxresults);
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}
	}
    public static class ISteamLeaderboards {

		/** 
		 * @param appid -- appid of game
		 * @param name -- name of the leaderboard to delete
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void DeleteLeaderboard(int appid,String name,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/ISteamLeaderboards/DeleteLeaderboard/v1";
			JsonObject param = new JsonObject();
			param.put("appid", appid);
			param.put("name", name);
			VxHolder.request(HttpMethod.POST, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param appid -- appid of game
		 * @param leaderboardid -- numeric ID of the target leaderboard. Can be retrieved from GetLeaderboardsForGame
		 * @param steamid -- steamID to set the score for
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void DeleteLeaderboardScore(int appid,long leaderboardid,long steamid,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/ISteamLeaderboards/DeleteLeaderboardScore/v1";
			JsonObject param = new JsonObject();
			param.put("appid", appid);
			param.put("leaderboardid", leaderboardid);
			param.put("steamid", steamid);
			VxHolder.request(HttpMethod.POST, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param appid -- appid of game
		 * @param name -- name of the leaderboard to create
		 * @param sortmethod -- sort method to use for this leaderboard (defaults to Ascending)
		 * @param displaytype -- display type for this leaderboard (defaults to Numeric)
		 * @param createifnotfound -- if this is true the leaderboard will be created if it doesn't exist. Defaults to true.
		 * @param onlytrustedwrites -- if this is true the leaderboard scores cannot be set by clients, and can only be set by publisher via SetLeaderboardScore WebAPI. Defaults to false.
		 * @param onlyfriendsreads -- if this is true the leaderboard scores can only be read for friends by clients, scores can always be read by publisher. Defaults to false.
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void FindOrCreateLeaderboard(int appid,String name,@Nullable String sortmethod,@Nullable String displaytype,@Nullable boolean createifnotfound,@Nullable boolean onlytrustedwrites,@Nullable boolean onlyfriendsreads,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/ISteamLeaderboards/FindOrCreateLeaderboard/v1";
			JsonObject param = new JsonObject();
			param.put("appid", appid);
			param.put("name", name);
			if(sortmethod != null)
				param.put("sortmethod", sortmethod);
			if(displaytype != null)
				param.put("displaytype", displaytype);
			VxHolder.request(HttpMethod.POST, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param appid -- appid of game
		 * @param name -- name of the leaderboard to create
		 * @param sortmethod -- sort method to use for this leaderboard (defaults to Ascending)
		 * @param displaytype -- display type for this leaderboard (defaults to Numeric)
		 * @param createifnotfound -- if this is true the leaderboard will be created if it doesn't exist. Defaults to true.
		 * @param onlytrustedwrites -- if this is true the leaderboard scores cannot be set by clients, and can only be set by publisher via SetLeaderboardScore WebAPI. Defaults to false.
		 * @param onlyfriendsreads -- if this is true the leaderboard scores can only be read for friends by clients, scores can always be read by publisher. Defaults to false.
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void FindOrCreateLeaderboardV2(int appid,String name,@Nullable String sortmethod,@Nullable String displaytype,@Nullable boolean createifnotfound,@Nullable boolean onlytrustedwrites,@Nullable boolean onlyfriendsreads,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/ISteamLeaderboards/FindOrCreateLeaderboard/v2";
			JsonObject param = new JsonObject();
			param.put("appid", appid);
			param.put("name", name);
			if(sortmethod != null)
				param.put("sortmethod", sortmethod);
			if(displaytype != null)
				param.put("displaytype", displaytype);
			VxHolder.request(HttpMethod.POST, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param key -- access key
		 * @param appid -- appid of game
		 * @param rangestart -- range start or 0
		 * @param rangeend -- range end or max LB entries
		 * @param steamid -- SteamID used for friend & around user requests
		 * @param leaderboardid -- ID of the leaderboard to view
		 * @param datarequest -- type of request: RequestGlobal, RequestAroundUser, RequestFriends
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GetLeaderboardEntries(String key,int appid,int rangestart,int rangeend,@Nullable long steamid,int leaderboardid,int datarequest,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/ISteamLeaderboards/GetLeaderboardEntries/v1";
			JsonObject param = new JsonObject();
			param.put("key", key);
			param.put("appid", appid);
			param.put("rangestart", rangestart);
			param.put("rangeend", rangeend);
			if(steamid != 0)
				param.put("steamid", steamid);
			param.put("leaderboardid", leaderboardid);
			param.put("datarequest", datarequest);
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param key -- access key
		 * @param appid -- appid of game
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GetLeaderboardsForGame(String key,int appid,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/ISteamLeaderboards/GetLeaderboardsForGame/v1";
			JsonObject param = new JsonObject();
			param.put("key", key);
			param.put("appid", appid);
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param key -- access key
		 * @param appid -- appid of game
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GetLeaderboardsForGameV2(String key,int appid,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/ISteamLeaderboards/GetLeaderboardsForGame/v2";
			JsonObject param = new JsonObject();
			param.put("key", key);
			param.put("appid", appid);
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param appid -- appid of game
		 * @param leaderboardid -- numeric ID of the target leaderboard. Can be retrieved from GetLeaderboardsForGame
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void ResetLeaderboard(int appid,int leaderboardid,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/ISteamLeaderboards/ResetLeaderboard/v1";
			JsonObject param = new JsonObject();
			param.put("appid", appid);
			param.put("leaderboardid", leaderboardid);
			VxHolder.request(HttpMethod.POST, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param appid -- appid of game
		 * @param leaderboardid -- numeric ID of the target leaderboard. Can be retrieved from GetLeaderboardsForGame
		 * @param steamid -- steamID to set the score for
		 * @param score -- the score to set for this user
		 * @param scoremethod -- update method to use. Can be "KeepBest" or "ForceUpdate"
		 * @param details -- game-specific details for how the score was earned. Up to 256 bytes.
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void SetLeaderboardScore(int appid,int leaderboardid,long steamid,int score,String scoremethod,@Nullable byte[] details,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/ISteamLeaderboards/SetLeaderboardScore/v1";
			JsonObject param = new JsonObject();
			param.put("appid", appid);
			param.put("leaderboardid", leaderboardid);
			param.put("steamid", steamid);
			param.put("score", score);
			param.put("scoremethod", scoremethod);
			if(details != null)
				param.put("details", details);
			VxHolder.request(HttpMethod.POST, requestURI, param, successHandler, failedHandler);
		}
	}
    public static class ISteamMicroTxn {

		/** 
		 * @param steamid -- SteamID of user with the agreement
		 * @param agreementid -- ID of agreement
		 * @param appid -- AppID of game
		 * @param nextprocessdate -- Date for next process
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void AdjustAgreement(long steamid,long agreementid,int appid,String nextprocessdate,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/ISteamMicroTxn/AdjustAgreement/v1";
			JsonObject param = new JsonObject();
			param.put("steamid", steamid);
			param.put("agreementid", agreementid);
			param.put("appid", appid);
			param.put("nextprocessdate", nextprocessdate);
			VxHolder.request(HttpMethod.POST, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param steamid -- SteamID of user with the agreement
		 * @param agreementid -- ID of agreement
		 * @param appid -- AppID of game
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void CancelAgreement(long steamid,long agreementid,int appid,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/ISteamMicroTxn/CancelAgreement/v1";
			JsonObject param = new JsonObject();
			param.put("steamid", steamid);
			param.put("agreementid", agreementid);
			param.put("appid", appid);
			VxHolder.request(HttpMethod.POST, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param orderid -- 3rd party ID for transaction
		 * @param appid -- AppID of game this transaction is for
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void FinalizeTxn(long orderid,int appid,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/ISteamMicroTxn/FinalizeTxn/v1";
			JsonObject param = new JsonObject();
			param.put("orderid", orderid);
			param.put("appid", appid);
			VxHolder.request(HttpMethod.POST, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param orderid -- 3rd party ID for transaction
		 * @param appid -- AppID of game this transaction is for
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void FinalizeTxnV2(long orderid,int appid,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/ISteamMicroTxn/FinalizeTxn/v2";
			JsonObject param = new JsonObject();
			param.put("orderid", orderid);
			param.put("appid", appid);
			VxHolder.request(HttpMethod.POST, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param appid -- AppID of game this transaction is for
		 * @param type -- Report type (GAMESALES, STEAMSTORE, SETTLEMENT)
		 * @param time -- Beginning time to start report from (RFC 3339 UTC format)
		 * @param maxresults -- Max number of results to return (up to 1000)
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GetReport(int appid,@Nullable String type,String time,@Nullable int maxresults,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/ISteamMicroTxn/GetReport/v1";
			JsonObject param = new JsonObject();
			param.put("appid", appid);
			if(type != null)
				param.put("type", type);
			param.put("time", time);
			if(maxresults != 0)
				param.put("maxresults", maxresults);
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param appid -- AppID of game this transaction is for
		 * @param type -- Report type (GAMESALES, STEAMSTORE, SETTLEMENT)
		 * @param time -- Beginning time to start report from (RFC 3339 UTC format)
		 * @param maxresults -- Max number of results to return (up to 1000)
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GetReportV2(int appid,@Nullable String type,String time,@Nullable int maxresults,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/ISteamMicroTxn/GetReport/v2";
			JsonObject param = new JsonObject();
			param.put("appid", appid);
			if(type != null)
				param.put("type", type);
			param.put("time", time);
			if(maxresults != 0)
				param.put("maxresults", maxresults);
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param appid -- AppID of game this transaction is for
		 * @param type -- Report type (GAMESALES, STEAMSTORE, SETTLEMENT)
		 * @param time -- Beginning time to start report from (RFC 3339 UTC format)
		 * @param maxresults -- Max number of results to return (up to 1000)
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GetReportV3(int appid,@Nullable String type,String time,@Nullable int maxresults,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/ISteamMicroTxn/GetReport/v3";
			JsonObject param = new JsonObject();
			param.put("appid", appid);
			if(type != null)
				param.put("type", type);
			param.put("time", time);
			if(maxresults != 0)
				param.put("maxresults", maxresults);
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param appid -- AppID of game this transaction is for
		 * @param type -- Report type (GAMESALES, STEAMSTORE, SETTLEMENT)
		 * @param time -- Beginning time to start report from (RFC 3339 UTC format)
		 * @param maxresults -- Max number of results to return (up to 1000)
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GetReportV4(int appid,@Nullable String type,String time,@Nullable int maxresults,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/ISteamMicroTxn/GetReport/v4";
			JsonObject param = new JsonObject();
			param.put("appid", appid);
			if(type != null)
				param.put("type", type);
			param.put("time", time);
			if(maxresults != 0)
				param.put("maxresults", maxresults);
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param appid -- AppID of game this transaction is for
		 * @param type -- Report type (GAMESALES, STEAMSTORE, SETTLEMENT)
		 * @param time -- Beginning time to start report from (RFC 3339 UTC format)
		 * @param maxresults -- Max number of results to return (up to 1000)
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GetReportV5(int appid,@Nullable String type,String time,@Nullable int maxresults,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/ISteamMicroTxn/GetReport/v5";
			JsonObject param = new JsonObject();
			param.put("appid", appid);
			if(type != null)
				param.put("type", type);
			param.put("time", time);
			if(maxresults != 0)
				param.put("maxresults", maxresults);
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param steamid -- SteamID of user making purchase
		 * @param appid -- AppID of game
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GetUserAgreementInfo(long steamid,int appid,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/ISteamMicroTxn/GetUserAgreementInfo/v1";
			JsonObject param = new JsonObject();
			param.put("steamid", steamid);
			param.put("appid", appid);
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param steamid -- SteamID of user making purchase
		 * @param ipaddress -- ip address of user in string format (xxx.xxx.xxx.xxx). Only required if usersession=web
		 * @param appid -- AppID of game this transaction is for
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GetUserInfo(@Nullable long steamid,@Nullable String ipaddress,@Nullable int appid,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/ISteamMicroTxn/GetUserInfo/v1";
			JsonObject param = new JsonObject();
			if(steamid != 0)
				param.put("steamid", steamid);
			if(ipaddress != null)
				param.put("ipaddress", ipaddress);
			if(appid != 0)
				param.put("appid", appid);
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param steamid -- SteamID of user making purchase
		 * @param ipaddress -- ip address of user in string format (xxx.xxx.xxx.xxx). Only required if usersession=web
		 * @param appid -- AppID of game this transaction is for
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GetUserInfoV2(@Nullable long steamid,@Nullable String ipaddress,@Nullable int appid,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/ISteamMicroTxn/GetUserInfo/v2";
			JsonObject param = new JsonObject();
			if(steamid != 0)
				param.put("steamid", steamid);
			if(ipaddress != null)
				param.put("ipaddress", ipaddress);
			if(appid != 0)
				param.put("appid", appid);
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param orderid -- 3rd party ID for transaction
		 * @param steamid -- SteamID of user making purchase
		 * @param appid -- AppID of game this transaction is for
		 * @param itemcount -- Number of items in cart
		 * @param language -- ISO 639-1 language code of description
		 * @param currency -- ISO 4217 currency code
		 * @param usersession -- session where user will authorize the transaction. client or web (defaults to client)
		 * @param ipaddress -- ip address of user in string format (xxx.xxx.xxx.xxx). Only required if usersession=web
		 * @param itemid -- 3rd party ID for item
		 * @param qty -- Quantity of this item
		 * @param amount -- Total cost (in cents) of item(s)
		 * @param description -- Description of item
		 * @param category -- Optional category grouping for item
		 * @param associated_bundle -- Optional bundleid of associated bundle
		 * @param billingtype -- Optional recurring billing type
		 * @param startdate -- Optional start date for recurring billing
		 * @param enddate -- Optional end date for recurring billing
		 * @param period -- Optional period for recurring billing
		 * @param frequency -- Optional frequency for recurring billing
		 * @param recurringamt -- Optional recurring billing amount
		 * @param bundlecount -- Number of bundles in cart
		 * @param bundleid -- 3rd party ID of the bundle. This shares the same ID space as 3rd party items.
		 * @param bundle_qty -- Quantity of this bundle
		 * @param bundle_desc -- Description of bundle
		 * @param bundle_category -- Optional category grouping for bundle
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void InitTxn(long orderid,long steamid,int appid,int itemcount,String language,String currency,@Nullable String usersession,@Nullable String ipaddress,int[] itemid,int[] qty,int[] amount,String[] description,@Nullable String[] category,@Nullable int[] associated_bundle,@Nullable String[] billingtype,@Nullable String[] startdate,@Nullable String[] enddate,@Nullable String[] period,@Nullable int[] frequency,@Nullable String[] recurringamt,@Nullable int bundlecount,@Nullable int[] bundleid,@Nullable int[] bundle_qty,@Nullable String[] bundle_desc,@Nullable String[] bundle_category,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/ISteamMicroTxn/InitTxn/v1";
			JsonObject param = new JsonObject();
			param.put("orderid", orderid);
			param.put("steamid", steamid);
			param.put("appid", appid);
			param.put("itemcount", itemcount);
			param.put("language", language);
			param.put("currency", currency);
			if(usersession != null)
				param.put("usersession", usersession);
			if(ipaddress != null)
				param.put("ipaddress", ipaddress);
			if (itemid != null) {
				for (int i = 0; i < itemid.length; i++) {
					param.put("itemid["+i+"]", itemid[i]);
				}
			}
			if (qty != null) {
				for (int i = 0; i < qty.length; i++) {
					param.put("qty["+i+"]", qty[i]);
				}
			}
			if (amount != null) {
				for (int i = 0; i < amount.length; i++) {
					param.put("amount["+i+"]", amount[i]);
				}
			}
			if (description != null) {
				for (int i = 0; i < description.length; i++) {
					param.put("description["+i+"]", description[i]);
				}
			}
			if (category != null) {
				for (int i = 0; i < category.length; i++) {
					param.put("category["+i+"]", category[i]);
				}
			}
			if (associated_bundle != null) {
				for (int i = 0; i < associated_bundle.length; i++) {
					param.put("associated_bundle["+i+"]", associated_bundle[i]);
				}
			}
			if (billingtype != null) {
				for (int i = 0; i < billingtype.length; i++) {
					param.put("billingtype["+i+"]", billingtype[i]);
				}
			}
			if (startdate != null) {
				for (int i = 0; i < startdate.length; i++) {
					param.put("startdate["+i+"]", startdate[i]);
				}
			}
			if (enddate != null) {
				for (int i = 0; i < enddate.length; i++) {
					param.put("enddate["+i+"]", enddate[i]);
				}
			}
			if (period != null) {
				for (int i = 0; i < period.length; i++) {
					param.put("period["+i+"]", period[i]);
				}
			}
			if (frequency != null) {
				for (int i = 0; i < frequency.length; i++) {
					param.put("frequency["+i+"]", frequency[i]);
				}
			}
			if (recurringamt != null) {
				for (int i = 0; i < recurringamt.length; i++) {
					param.put("recurringamt["+i+"]", recurringamt[i]);
				}
			}
			if(bundlecount != 0)
				param.put("bundlecount", bundlecount);
			if (bundleid != null) {
				for (int i = 0; i < bundleid.length; i++) {
					param.put("bundleid["+i+"]", bundleid[i]);
				}
			}
			if (bundle_qty != null) {
				for (int i = 0; i < bundle_qty.length; i++) {
					param.put("bundle_qty["+i+"]", bundle_qty[i]);
				}
			}
			if (bundle_desc != null) {
				for (int i = 0; i < bundle_desc.length; i++) {
					param.put("bundle_desc["+i+"]", bundle_desc[i]);
				}
			}
			if (bundle_category != null) {
				for (int i = 0; i < bundle_category.length; i++) {
					param.put("bundle_category["+i+"]", bundle_category[i]);
				}
			}
			VxHolder.request(HttpMethod.POST, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param orderid -- 3rd party ID for transaction
		 * @param steamid -- SteamID of user making purchase
		 * @param appid -- AppID of game this transaction is for
		 * @param itemcount -- Number of items in cart
		 * @param language -- ISO 639-1 language code of description
		 * @param currency -- ISO 4217 currency code
		 * @param usersession -- session where user will authorize the transaction. client or web (defaults to client)
		 * @param ipaddress -- ip address of user in string format (xxx.xxx.xxx.xxx). Only required if usersession=web
		 * @param itemid -- 3rd party ID for item
		 * @param qty -- Quantity of this item
		 * @param amount -- Total cost (in cents) of item(s)
		 * @param description -- Description of item
		 * @param category -- Optional category grouping for item
		 * @param associated_bundle -- Optional bundleid of associated bundle
		 * @param billingtype -- Optional recurring billing type
		 * @param startdate -- Optional start date for recurring billing
		 * @param enddate -- Optional end date for recurring billing
		 * @param period -- Optional period for recurring billing
		 * @param frequency -- Optional frequency for recurring billing
		 * @param recurringamt -- Optional recurring billing amount
		 * @param bundlecount -- Number of bundles in cart
		 * @param bundleid -- 3rd party ID of the bundle. This shares the same ID space as 3rd party items.
		 * @param bundle_qty -- Quantity of this bundle
		 * @param bundle_desc -- Description of bundle
		 * @param bundle_category -- Optional category grouping for bundle
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void InitTxnV2(long orderid,long steamid,int appid,int itemcount,String language,String currency,@Nullable String usersession,@Nullable String ipaddress,int[] itemid,int[] qty,int[] amount,String[] description,@Nullable String[] category,@Nullable int[] associated_bundle,@Nullable String[] billingtype,@Nullable String[] startdate,@Nullable String[] enddate,@Nullable String[] period,@Nullable int[] frequency,@Nullable String[] recurringamt,@Nullable int bundlecount,@Nullable int[] bundleid,@Nullable int[] bundle_qty,@Nullable String[] bundle_desc,@Nullable String[] bundle_category,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/ISteamMicroTxn/InitTxn/v2";
			JsonObject param = new JsonObject();
			param.put("orderid", orderid);
			param.put("steamid", steamid);
			param.put("appid", appid);
			param.put("itemcount", itemcount);
			param.put("language", language);
			param.put("currency", currency);
			if(usersession != null)
				param.put("usersession", usersession);
			if(ipaddress != null)
				param.put("ipaddress", ipaddress);
			if (itemid != null) {
				for (int i = 0; i < itemid.length; i++) {
					param.put("itemid["+i+"]", itemid[i]);
				}
			}
			if (qty != null) {
				for (int i = 0; i < qty.length; i++) {
					param.put("qty["+i+"]", qty[i]);
				}
			}
			if (amount != null) {
				for (int i = 0; i < amount.length; i++) {
					param.put("amount["+i+"]", amount[i]);
				}
			}
			if (description != null) {
				for (int i = 0; i < description.length; i++) {
					param.put("description["+i+"]", description[i]);
				}
			}
			if (category != null) {
				for (int i = 0; i < category.length; i++) {
					param.put("category["+i+"]", category[i]);
				}
			}
			if (associated_bundle != null) {
				for (int i = 0; i < associated_bundle.length; i++) {
					param.put("associated_bundle["+i+"]", associated_bundle[i]);
				}
			}
			if (billingtype != null) {
				for (int i = 0; i < billingtype.length; i++) {
					param.put("billingtype["+i+"]", billingtype[i]);
				}
			}
			if (startdate != null) {
				for (int i = 0; i < startdate.length; i++) {
					param.put("startdate["+i+"]", startdate[i]);
				}
			}
			if (enddate != null) {
				for (int i = 0; i < enddate.length; i++) {
					param.put("enddate["+i+"]", enddate[i]);
				}
			}
			if (period != null) {
				for (int i = 0; i < period.length; i++) {
					param.put("period["+i+"]", period[i]);
				}
			}
			if (frequency != null) {
				for (int i = 0; i < frequency.length; i++) {
					param.put("frequency["+i+"]", frequency[i]);
				}
			}
			if (recurringamt != null) {
				for (int i = 0; i < recurringamt.length; i++) {
					param.put("recurringamt["+i+"]", recurringamt[i]);
				}
			}
			if(bundlecount != 0)
				param.put("bundlecount", bundlecount);
			if (bundleid != null) {
				for (int i = 0; i < bundleid.length; i++) {
					param.put("bundleid["+i+"]", bundleid[i]);
				}
			}
			if (bundle_qty != null) {
				for (int i = 0; i < bundle_qty.length; i++) {
					param.put("bundle_qty["+i+"]", bundle_qty[i]);
				}
			}
			if (bundle_desc != null) {
				for (int i = 0; i < bundle_desc.length; i++) {
					param.put("bundle_desc["+i+"]", bundle_desc[i]);
				}
			}
			if (bundle_category != null) {
				for (int i = 0; i < bundle_category.length; i++) {
					param.put("bundle_category["+i+"]", bundle_category[i]);
				}
			}
			VxHolder.request(HttpMethod.POST, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param orderid -- 3rd party ID for transaction
		 * @param steamid -- SteamID of user making purchase
		 * @param appid -- AppID of game this transaction is for
		 * @param itemcount -- Number of items in cart
		 * @param language -- ISO 639-1 language code of description
		 * @param currency -- ISO 4217 currency code
		 * @param usersession -- session where user will authorize the transaction. client or web (defaults to client)
		 * @param ipaddress -- ip address of user in string format (xxx.xxx.xxx.xxx). Only required if usersession=web
		 * @param itemid -- 3rd party ID for item
		 * @param qty -- Quantity of this item
		 * @param amount -- Total cost (in cents) of item(s)
		 * @param description -- Description of item
		 * @param category -- Optional category grouping for item
		 * @param associated_bundle -- Optional bundleid of associated bundle
		 * @param billingtype -- Optional recurring billing type
		 * @param startdate -- Optional start date for recurring billing
		 * @param enddate -- Optional end date for recurring billing
		 * @param period -- Optional period for recurring billing
		 * @param frequency -- Optional frequency for recurring billing
		 * @param recurringamt -- Optional recurring billing amount
		 * @param bundlecount -- Number of bundles in cart
		 * @param bundleid -- 3rd party ID of the bundle. This shares the same ID space as 3rd party items.
		 * @param bundle_qty -- Quantity of this bundle
		 * @param bundle_desc -- Description of bundle
		 * @param bundle_category -- Optional category grouping for bundle
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void InitTxnV3(long orderid,long steamid,int appid,int itemcount,String language,String currency,@Nullable String usersession,@Nullable String ipaddress,int[] itemid,int[] qty,int[] amount,String[] description,@Nullable String[] category,@Nullable int[] associated_bundle,@Nullable String[] billingtype,@Nullable String[] startdate,@Nullable String[] enddate,@Nullable String[] period,@Nullable int[] frequency,@Nullable String[] recurringamt,@Nullable int bundlecount,@Nullable int[] bundleid,@Nullable int[] bundle_qty,@Nullable String[] bundle_desc,@Nullable String[] bundle_category,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/ISteamMicroTxn/InitTxn/v3";
			JsonObject param = new JsonObject();
			param.put("orderid", orderid);
			param.put("steamid", steamid);
			param.put("appid", appid);
			param.put("itemcount", itemcount);
			param.put("language", language);
			param.put("currency", currency);
			if(usersession != null)
				param.put("usersession", usersession);
			if(ipaddress != null)
				param.put("ipaddress", ipaddress);
			if (itemid != null) {
				for (int i = 0; i < itemid.length; i++) {
					param.put("itemid["+i+"]", itemid[i]);
				}
			}
			if (qty != null) {
				for (int i = 0; i < qty.length; i++) {
					param.put("qty["+i+"]", qty[i]);
				}
			}
			if (amount != null) {
				for (int i = 0; i < amount.length; i++) {
					param.put("amount["+i+"]", amount[i]);
				}
			}
			if (description != null) {
				for (int i = 0; i < description.length; i++) {
					param.put("description["+i+"]", description[i]);
				}
			}
			if (category != null) {
				for (int i = 0; i < category.length; i++) {
					param.put("category["+i+"]", category[i]);
				}
			}
			if (associated_bundle != null) {
				for (int i = 0; i < associated_bundle.length; i++) {
					param.put("associated_bundle["+i+"]", associated_bundle[i]);
				}
			}
			if (billingtype != null) {
				for (int i = 0; i < billingtype.length; i++) {
					param.put("billingtype["+i+"]", billingtype[i]);
				}
			}
			if (startdate != null) {
				for (int i = 0; i < startdate.length; i++) {
					param.put("startdate["+i+"]", startdate[i]);
				}
			}
			if (enddate != null) {
				for (int i = 0; i < enddate.length; i++) {
					param.put("enddate["+i+"]", enddate[i]);
				}
			}
			if (period != null) {
				for (int i = 0; i < period.length; i++) {
					param.put("period["+i+"]", period[i]);
				}
			}
			if (frequency != null) {
				for (int i = 0; i < frequency.length; i++) {
					param.put("frequency["+i+"]", frequency[i]);
				}
			}
			if (recurringamt != null) {
				for (int i = 0; i < recurringamt.length; i++) {
					param.put("recurringamt["+i+"]", recurringamt[i]);
				}
			}
			if(bundlecount != 0)
				param.put("bundlecount", bundlecount);
			if (bundleid != null) {
				for (int i = 0; i < bundleid.length; i++) {
					param.put("bundleid["+i+"]", bundleid[i]);
				}
			}
			if (bundle_qty != null) {
				for (int i = 0; i < bundle_qty.length; i++) {
					param.put("bundle_qty["+i+"]", bundle_qty[i]);
				}
			}
			if (bundle_desc != null) {
				for (int i = 0; i < bundle_desc.length; i++) {
					param.put("bundle_desc["+i+"]", bundle_desc[i]);
				}
			}
			if (bundle_category != null) {
				for (int i = 0; i < bundle_category.length; i++) {
					param.put("bundle_category["+i+"]", bundle_category[i]);
				}
			}
			VxHolder.request(HttpMethod.POST, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param orderid -- 3rd party ID for transaction
		 * @param steamid -- SteamID of user with the agreement
		 * @param agreementid -- ID of agreement
		 * @param appid -- AppID of game
		 * @param amount -- Total cost (in cents) to charge
		 * @param currency -- ISO 4217 currency code
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void ProcessAgreement(long orderid,long steamid,long agreementid,int appid,int amount,String currency,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/ISteamMicroTxn/ProcessAgreement/v1";
			JsonObject param = new JsonObject();
			param.put("orderid", orderid);
			param.put("steamid", steamid);
			param.put("agreementid", agreementid);
			param.put("appid", appid);
			param.put("amount", amount);
			param.put("currency", currency);
			VxHolder.request(HttpMethod.POST, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param appid -- AppID of game this transaction is for
		 * @param orderid -- 3rd party ID for transaction
		 * @param transid -- Steam transaction ID
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void QueryTxn(int appid,@Nullable long orderid,@Nullable long transid,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/ISteamMicroTxn/QueryTxn/v1";
			JsonObject param = new JsonObject();
			param.put("appid", appid);
			if(orderid != 0)
				param.put("orderid", orderid);
			if(transid != 0)
				param.put("transid", transid);
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param appid -- AppID of game this transaction is for
		 * @param orderid -- 3rd party ID for transaction
		 * @param transid -- Steam transaction ID
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void QueryTxnV2(int appid,@Nullable long orderid,@Nullable long transid,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/ISteamMicroTxn/QueryTxn/v2";
			JsonObject param = new JsonObject();
			param.put("appid", appid);
			if(orderid != 0)
				param.put("orderid", orderid);
			if(transid != 0)
				param.put("transid", transid);
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param appid -- AppID of game this transaction is for
		 * @param orderid -- 3rd party ID for transaction
		 * @param transid -- Steam transaction ID
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void QueryTxnV3(int appid,@Nullable long orderid,@Nullable long transid,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/ISteamMicroTxn/QueryTxn/v3";
			JsonObject param = new JsonObject();
			param.put("appid", appid);
			if(orderid != 0)
				param.put("orderid", orderid);
			if(transid != 0)
				param.put("transid", transid);
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param orderid -- 3rd party ID for transaction
		 * @param appid -- AppID of game this transaction is for
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void RefundTxn(long orderid,int appid,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/ISteamMicroTxn/RefundTxn/v1";
			JsonObject param = new JsonObject();
			param.put("orderid", orderid);
			param.put("appid", appid);
			VxHolder.request(HttpMethod.POST, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param orderid -- 3rd party ID for transaction
		 * @param appid -- AppID of game this transaction is for
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void RefundTxnV2(long orderid,int appid,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/ISteamMicroTxn/RefundTxn/v2";
			JsonObject param = new JsonObject();
			param.put("orderid", orderid);
			param.put("appid", appid);
			VxHolder.request(HttpMethod.POST, requestURI, param, successHandler, failedHandler);
		}
	}
    public static class ISteamMicroTxnSandbox {

		/** 
		 * @param steamid -- SteamID of user with the agreement
		 * @param agreementid -- ID of agreement
		 * @param appid -- AppID of game
		 * @param nextprocessdate -- Date for next process
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void AdjustAgreement(long steamid,long agreementid,int appid,String nextprocessdate,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/ISteamMicroTxnSandbox/AdjustAgreement/v1";
			JsonObject param = new JsonObject();
			param.put("steamid", steamid);
			param.put("agreementid", agreementid);
			param.put("appid", appid);
			param.put("nextprocessdate", nextprocessdate);
			VxHolder.request(HttpMethod.POST, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param steamid -- SteamID of user with the agreement
		 * @param agreementid -- ID of agreement
		 * @param appid -- AppID of game
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void CancelAgreement(long steamid,long agreementid,int appid,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/ISteamMicroTxnSandbox/CancelAgreement/v1";
			JsonObject param = new JsonObject();
			param.put("steamid", steamid);
			param.put("agreementid", agreementid);
			param.put("appid", appid);
			VxHolder.request(HttpMethod.POST, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param orderid -- 3rd party ID for transaction
		 * @param appid -- AppID of game this transaction is for
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void FinalizeTxn(long orderid,int appid,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/ISteamMicroTxnSandbox/FinalizeTxn/v1";
			JsonObject param = new JsonObject();
			param.put("orderid", orderid);
			param.put("appid", appid);
			VxHolder.request(HttpMethod.POST, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param orderid -- 3rd party ID for transaction
		 * @param appid -- AppID of game this transaction is for
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void FinalizeTxnV2(long orderid,int appid,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/ISteamMicroTxnSandbox/FinalizeTxn/v2";
			JsonObject param = new JsonObject();
			param.put("orderid", orderid);
			param.put("appid", appid);
			VxHolder.request(HttpMethod.POST, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param appid -- AppID of game this transaction is for
		 * @param type -- Report type (GAMESALES, STEAMSTORE, SETTLEMENT)
		 * @param time -- Beginning time to start report from (RFC 3339 UTC format)
		 * @param maxresults -- Max number of results to return (up to 1000)
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GetReport(int appid,@Nullable String type,String time,@Nullable int maxresults,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/ISteamMicroTxnSandbox/GetReport/v1";
			JsonObject param = new JsonObject();
			param.put("appid", appid);
			if(type != null)
				param.put("type", type);
			param.put("time", time);
			if(maxresults != 0)
				param.put("maxresults", maxresults);
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param appid -- AppID of game this transaction is for
		 * @param type -- Report type (GAMESALES, STEAMSTORE, SETTLEMENT)
		 * @param time -- Beginning time to start report from (RFC 3339 UTC format)
		 * @param maxresults -- Max number of results to return (up to 1000)
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GetReportV2(int appid,@Nullable String type,String time,@Nullable int maxresults,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/ISteamMicroTxnSandbox/GetReport/v2";
			JsonObject param = new JsonObject();
			param.put("appid", appid);
			if(type != null)
				param.put("type", type);
			param.put("time", time);
			if(maxresults != 0)
				param.put("maxresults", maxresults);
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param appid -- AppID of game this transaction is for
		 * @param type -- Report type (GAMESALES, STEAMSTORE, SETTLEMENT)
		 * @param time -- Beginning time to start report from (RFC 3339 UTC format)
		 * @param maxresults -- Max number of results to return (up to 1000)
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GetReportV3(int appid,@Nullable String type,String time,@Nullable int maxresults,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/ISteamMicroTxnSandbox/GetReport/v3";
			JsonObject param = new JsonObject();
			param.put("appid", appid);
			if(type != null)
				param.put("type", type);
			param.put("time", time);
			if(maxresults != 0)
				param.put("maxresults", maxresults);
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param appid -- AppID of game this transaction is for
		 * @param type -- Report type (GAMESALES, STEAMSTORE, SETTLEMENT)
		 * @param time -- Beginning time to start report from (RFC 3339 UTC format)
		 * @param maxresults -- Max number of results to return (up to 1000)
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GetReportV4(int appid,@Nullable String type,String time,@Nullable int maxresults,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/ISteamMicroTxnSandbox/GetReport/v4";
			JsonObject param = new JsonObject();
			param.put("appid", appid);
			if(type != null)
				param.put("type", type);
			param.put("time", time);
			if(maxresults != 0)
				param.put("maxresults", maxresults);
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param appid -- AppID of game this transaction is for
		 * @param type -- Report type (GAMESALES, STEAMSTORE, SETTLEMENT)
		 * @param time -- Beginning time to start report from (RFC 3339 UTC format)
		 * @param maxresults -- Max number of results to return (up to 1000)
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GetReportV5(int appid,@Nullable String type,String time,@Nullable int maxresults,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/ISteamMicroTxnSandbox/GetReport/v5";
			JsonObject param = new JsonObject();
			param.put("appid", appid);
			if(type != null)
				param.put("type", type);
			param.put("time", time);
			if(maxresults != 0)
				param.put("maxresults", maxresults);
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param steamid -- SteamID of user making purchase
		 * @param appid -- AppID of game
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GetUserAgreementInfo(long steamid,int appid,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/ISteamMicroTxnSandbox/GetUserAgreementInfo/v1";
			JsonObject param = new JsonObject();
			param.put("steamid", steamid);
			param.put("appid", appid);
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param steamid -- SteamID of user making purchase
		 * @param ipaddress -- ip address of user in string format (xxx.xxx.xxx.xxx). Only required if usersession=web
		 * @param appid -- AppID of game this transaction is for
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GetUserInfo(@Nullable long steamid,@Nullable String ipaddress,@Nullable int appid,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/ISteamMicroTxnSandbox/GetUserInfo/v1";
			JsonObject param = new JsonObject();
			if(steamid != 0)
				param.put("steamid", steamid);
			if(ipaddress != null)
				param.put("ipaddress", ipaddress);
			if(appid != 0)
				param.put("appid", appid);
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param steamid -- SteamID of user making purchase
		 * @param ipaddress -- ip address of user in string format (xxx.xxx.xxx.xxx). Only required if usersession=web
		 * @param appid -- AppID of game this transaction is for
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GetUserInfoV2(@Nullable long steamid,@Nullable String ipaddress,@Nullable int appid,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/ISteamMicroTxnSandbox/GetUserInfo/v2";
			JsonObject param = new JsonObject();
			if(steamid != 0)
				param.put("steamid", steamid);
			if(ipaddress != null)
				param.put("ipaddress", ipaddress);
			if(appid != 0)
				param.put("appid", appid);
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param orderid -- 3rd party ID for transaction
		 * @param steamid -- SteamID of user making purchase
		 * @param appid -- AppID of game this transaction is for
		 * @param itemcount -- Number of items in cart
		 * @param language -- ISO 639-1 language code of description
		 * @param currency -- ISO 4217 currency code
		 * @param itemid -- 3rd party ID for item
		 * @param qty -- Quantity of this item
		 * @param amount -- Total cost (in cents) of item(s)
		 * @param description -- Description of item
		 * @param category -- Optional category grouping for item
		 * @param billingtype -- Optional recurring billing type
		 * @param startdate -- Optional start date for recurring billing
		 * @param enddate -- Optional start date for recurring billing
		 * @param period -- Optional period for recurring billing
		 * @param frequency -- Optional frequency for recurring billing
		 * @param recurringamt -- Optional recurring billing amount
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void InitTxn(long orderid,long steamid,int appid,int itemcount,String language,String currency,int[] itemid,int[] qty,int[] amount,String[] description,@Nullable String[] category,@Nullable String[] billingtype,@Nullable String[] startdate,@Nullable String[] enddate,@Nullable String[] period,@Nullable int[] frequency,@Nullable String[] recurringamt,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/ISteamMicroTxnSandbox/InitTxn/v1";
			JsonObject param = new JsonObject();
			param.put("orderid", orderid);
			param.put("steamid", steamid);
			param.put("appid", appid);
			param.put("itemcount", itemcount);
			param.put("language", language);
			param.put("currency", currency);
			if (itemid != null) {
				for (int i = 0; i < itemid.length; i++) {
					param.put("itemid["+i+"]", itemid[i]);
				}
			}
			if (qty != null) {
				for (int i = 0; i < qty.length; i++) {
					param.put("qty["+i+"]", qty[i]);
				}
			}
			if (amount != null) {
				for (int i = 0; i < amount.length; i++) {
					param.put("amount["+i+"]", amount[i]);
				}
			}
			if (description != null) {
				for (int i = 0; i < description.length; i++) {
					param.put("description["+i+"]", description[i]);
				}
			}
			if (category != null) {
				for (int i = 0; i < category.length; i++) {
					param.put("category["+i+"]", category[i]);
				}
			}
			if (billingtype != null) {
				for (int i = 0; i < billingtype.length; i++) {
					param.put("billingtype["+i+"]", billingtype[i]);
				}
			}
			if (startdate != null) {
				for (int i = 0; i < startdate.length; i++) {
					param.put("startdate["+i+"]", startdate[i]);
				}
			}
			if (enddate != null) {
				for (int i = 0; i < enddate.length; i++) {
					param.put("enddate["+i+"]", enddate[i]);
				}
			}
			if (period != null) {
				for (int i = 0; i < period.length; i++) {
					param.put("period["+i+"]", period[i]);
				}
			}
			if (frequency != null) {
				for (int i = 0; i < frequency.length; i++) {
					param.put("frequency["+i+"]", frequency[i]);
				}
			}
			if (recurringamt != null) {
				for (int i = 0; i < recurringamt.length; i++) {
					param.put("recurringamt["+i+"]", recurringamt[i]);
				}
			}
			VxHolder.request(HttpMethod.POST, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param orderid -- 3rd party ID for transaction
		 * @param steamid -- SteamID of user making purchase
		 * @param appid -- AppID of game this transaction is for
		 * @param itemcount -- Number of items in cart
		 * @param language -- ISO 639-1 language code of description
		 * @param currency -- ISO 4217 currency code
		 * @param itemid -- 3rd party ID for item
		 * @param qty -- Quantity of this item
		 * @param amount -- Total cost (in cents) of item(s)
		 * @param description -- Description of item
		 * @param category -- Optional category grouping for item
		 * @param billingtype -- Optional recurring billing type
		 * @param startdate -- Optional start date for recurring billing
		 * @param enddate -- Optional end date for recurring billing
		 * @param period -- Optional period for recurring billing
		 * @param frequency -- Optional frequency for recurring billing
		 * @param recurringamt -- Optional recurring billing amount
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void InitTxnV2(long orderid,long steamid,int appid,int itemcount,String language,String currency,int[] itemid,int[] qty,int[] amount,String[] description,@Nullable String[] category,@Nullable String[] billingtype,@Nullable String[] startdate,@Nullable String[] enddate,@Nullable String[] period,@Nullable int[] frequency,@Nullable String[] recurringamt,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/ISteamMicroTxnSandbox/InitTxn/v2";
			JsonObject param = new JsonObject();
			param.put("orderid", orderid);
			param.put("steamid", steamid);
			param.put("appid", appid);
			param.put("itemcount", itemcount);
			param.put("language", language);
			param.put("currency", currency);
			if (itemid != null) {
				for (int i = 0; i < itemid.length; i++) {
					param.put("itemid["+i+"]", itemid[i]);
				}
			}
			if (qty != null) {
				for (int i = 0; i < qty.length; i++) {
					param.put("qty["+i+"]", qty[i]);
				}
			}
			if (amount != null) {
				for (int i = 0; i < amount.length; i++) {
					param.put("amount["+i+"]", amount[i]);
				}
			}
			if (description != null) {
				for (int i = 0; i < description.length; i++) {
					param.put("description["+i+"]", description[i]);
				}
			}
			if (category != null) {
				for (int i = 0; i < category.length; i++) {
					param.put("category["+i+"]", category[i]);
				}
			}
			if (billingtype != null) {
				for (int i = 0; i < billingtype.length; i++) {
					param.put("billingtype["+i+"]", billingtype[i]);
				}
			}
			if (startdate != null) {
				for (int i = 0; i < startdate.length; i++) {
					param.put("startdate["+i+"]", startdate[i]);
				}
			}
			if (enddate != null) {
				for (int i = 0; i < enddate.length; i++) {
					param.put("enddate["+i+"]", enddate[i]);
				}
			}
			if (period != null) {
				for (int i = 0; i < period.length; i++) {
					param.put("period["+i+"]", period[i]);
				}
			}
			if (frequency != null) {
				for (int i = 0; i < frequency.length; i++) {
					param.put("frequency["+i+"]", frequency[i]);
				}
			}
			if (recurringamt != null) {
				for (int i = 0; i < recurringamt.length; i++) {
					param.put("recurringamt["+i+"]", recurringamt[i]);
				}
			}
			VxHolder.request(HttpMethod.POST, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param orderid -- 3rd party ID for transaction
		 * @param steamid -- SteamID of user making purchase
		 * @param appid -- AppID of game this transaction is for
		 * @param itemcount -- Number of items in cart
		 * @param language -- ISO 639-1 language code of description
		 * @param currency -- ISO 4217 currency code
		 * @param itemid -- 3rd party ID for item
		 * @param qty -- Quantity of this item
		 * @param amount -- Total cost (in cents) of item(s)
		 * @param description -- Description of item
		 * @param category -- Optional category grouping for item
		 * @param billingtype -- Optional recurring billing type
		 * @param startdate -- Optional start date for recurring billing
		 * @param enddate -- Optional end date for recurring billing
		 * @param period -- Optional period for recurring billing
		 * @param frequency -- Optional frequency for recurring billing
		 * @param recurringamt -- Optional recurring billing amount
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void InitTxnV3(long orderid,long steamid,int appid,int itemcount,String language,String currency,int[] itemid,int[] qty,int[] amount,String[] description,@Nullable String[] category,@Nullable String[] billingtype,@Nullable String[] startdate,@Nullable String[] enddate,@Nullable String[] period,@Nullable int[] frequency,@Nullable String[] recurringamt,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/ISteamMicroTxnSandbox/InitTxn/v3";
			JsonObject param = new JsonObject();
			param.put("orderid", orderid);
			param.put("steamid", steamid);
			param.put("appid", appid);
			param.put("itemcount", itemcount);
			param.put("language", language);
			param.put("currency", currency);
			if (itemid != null) {
				for (int i = 0; i < itemid.length; i++) {
					param.put("itemid["+i+"]", itemid[i]);
				}
			}
			if (qty != null) {
				for (int i = 0; i < qty.length; i++) {
					param.put("qty["+i+"]", qty[i]);
				}
			}
			if (amount != null) {
				for (int i = 0; i < amount.length; i++) {
					param.put("amount["+i+"]", amount[i]);
				}
			}
			if (description != null) {
				for (int i = 0; i < description.length; i++) {
					param.put("description["+i+"]", description[i]);
				}
			}
			if (category != null) {
				for (int i = 0; i < category.length; i++) {
					param.put("category["+i+"]", category[i]);
				}
			}
			if (billingtype != null) {
				for (int i = 0; i < billingtype.length; i++) {
					param.put("billingtype["+i+"]", billingtype[i]);
				}
			}
			if (startdate != null) {
				for (int i = 0; i < startdate.length; i++) {
					param.put("startdate["+i+"]", startdate[i]);
				}
			}
			if (enddate != null) {
				for (int i = 0; i < enddate.length; i++) {
					param.put("enddate["+i+"]", enddate[i]);
				}
			}
			if (period != null) {
				for (int i = 0; i < period.length; i++) {
					param.put("period["+i+"]", period[i]);
				}
			}
			if (frequency != null) {
				for (int i = 0; i < frequency.length; i++) {
					param.put("frequency["+i+"]", frequency[i]);
				}
			}
			if (recurringamt != null) {
				for (int i = 0; i < recurringamt.length; i++) {
					param.put("recurringamt["+i+"]", recurringamt[i]);
				}
			}
			VxHolder.request(HttpMethod.POST, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param steamid -- SteamID of user with the agreement
		 * @param agreementid -- ID of agreement
		 * @param appid -- AppID of game
		 * @param amount -- Total cost (in cents) to charge
		 * @param currency -- ISO 4217 currency code
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void ProcessAgreement(long steamid,long agreementid,int appid,int amount,String currency,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/ISteamMicroTxnSandbox/ProcessAgreement/v1";
			JsonObject param = new JsonObject();
			param.put("steamid", steamid);
			param.put("agreementid", agreementid);
			param.put("appid", appid);
			param.put("amount", amount);
			param.put("currency", currency);
			VxHolder.request(HttpMethod.POST, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param appid -- AppID of game this transaction is for
		 * @param orderid -- 3rd party ID for transaction
		 * @param transid -- Steam transaction ID
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void QueryTxn(int appid,@Nullable long orderid,@Nullable long transid,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/ISteamMicroTxnSandbox/QueryTxn/v1";
			JsonObject param = new JsonObject();
			param.put("appid", appid);
			if(orderid != 0)
				param.put("orderid", orderid);
			if(transid != 0)
				param.put("transid", transid);
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param appid -- AppID of game this transaction is for
		 * @param orderid -- 3rd party ID for transaction
		 * @param transid -- Steam transaction ID
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void QueryTxnV2(int appid,@Nullable long orderid,@Nullable long transid,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/ISteamMicroTxnSandbox/QueryTxn/v2";
			JsonObject param = new JsonObject();
			param.put("appid", appid);
			if(orderid != 0)
				param.put("orderid", orderid);
			if(transid != 0)
				param.put("transid", transid);
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param orderid -- 3rd party ID for transaction
		 * @param appid -- AppID of game this transaction is for
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void RefundTxn(long orderid,int appid,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/ISteamMicroTxnSandbox/RefundTxn/v1";
			JsonObject param = new JsonObject();
			param.put("orderid", orderid);
			param.put("appid", appid);
			VxHolder.request(HttpMethod.POST, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param orderid -- 3rd party ID for transaction
		 * @param appid -- AppID of game this transaction is for
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void RefundTxnV2(long orderid,int appid,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/ISteamMicroTxnSandbox/RefundTxn/v2";
			JsonObject param = new JsonObject();
			param.put("orderid", orderid);
			param.put("appid", appid);
			VxHolder.request(HttpMethod.POST, requestURI, param, successHandler, failedHandler);
		}
	}
    public static class ISteamNews {

		/** 
		 * @param appid -- AppID to retrieve news for
		 * @param maxlength -- Maximum length for the content to return, if this is 0 the full content is returned, if it's less then a blurb is generated to fit.
		 * @param enddate -- Retrieve posts earlier than this date (unix epoch timestamp)
		 * @param count -- # of posts to retrieve (default 20)
		 * @param tags -- Comma-separated list of tags to filter by (e.g. 'patchnodes')
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GetNewsForApp(int appid,@Nullable int maxlength,@Nullable int enddate,@Nullable int count,@Nullable String tags,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/ISteamNews/GetNewsForApp/v1";
			JsonObject param = new JsonObject();
			param.put("appid", appid);
			if(maxlength != 0)
				param.put("maxlength", maxlength);
			if(enddate != 0)
				param.put("enddate", enddate);
			if(count != 0)
				param.put("count", count);
			if(tags != null)
				param.put("tags", tags);
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param appid -- AppID to retrieve news for
		 * @param maxlength -- Maximum length for the content to return, if this is 0 the full content is returned, if it's less then a blurb is generated to fit.
		 * @param enddate -- Retrieve posts earlier than this date (unix epoch timestamp)
		 * @param count -- # of posts to retrieve (default 20)
		 * @param feeds -- Comma-separated list of feed names to return news for
		 * @param tags -- Comma-separated list of tags to filter by (e.g. 'patchnodes')
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GetNewsForAppV2(int appid,@Nullable int maxlength,@Nullable int enddate,@Nullable int count,@Nullable String feeds,@Nullable String tags,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/ISteamNews/GetNewsForApp/v2";
			JsonObject param = new JsonObject();
			param.put("appid", appid);
			if(maxlength != 0)
				param.put("maxlength", maxlength);
			if(enddate != 0)
				param.put("enddate", enddate);
			if(count != 0)
				param.put("count", count);
			if(feeds != null)
				param.put("feeds", feeds);
			if(tags != null)
				param.put("tags", tags);
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param key -- access key
		 * @param appid -- AppID to retrieve news for
		 * @param maxlength -- Maximum length for the content to return, if this is 0 the full content is returned, if it's less then a blurb is generated to fit.
		 * @param enddate -- Retrieve posts earlier than this date (unix epoch timestamp)
		 * @param count -- # of posts to retrieve (default 20)
		 * @param tags -- Comma-separated list of tags to filter by (e.g. 'patchnodes')
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GetNewsForAppAuthed(String key,int appid,@Nullable int maxlength,@Nullable int enddate,@Nullable int count,@Nullable String tags,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/ISteamNews/GetNewsForAppAuthed/v1";
			JsonObject param = new JsonObject();
			param.put("key", key);
			param.put("appid", appid);
			if(maxlength != 0)
				param.put("maxlength", maxlength);
			if(enddate != 0)
				param.put("enddate", enddate);
			if(count != 0)
				param.put("count", count);
			if(tags != null)
				param.put("tags", tags);
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param key -- access key
		 * @param appid -- AppID to retrieve news for
		 * @param maxlength -- Maximum length for the content to return, if this is 0 the full content is returned, if it's less then a blurb is generated to fit.
		 * @param enddate -- Retrieve posts earlier than this date (unix epoch timestamp)
		 * @param count -- # of posts to retrieve (default 20)
		 * @param feeds -- Comma-seperated list of feed names to return news for
		 * @param tags -- Comma-separated list of tags to filter by (e.g. 'patchnodes')
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GetNewsForAppAuthedV2(String key,int appid,@Nullable int maxlength,@Nullable int enddate,@Nullable int count,@Nullable String feeds,@Nullable String tags,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/ISteamNews/GetNewsForAppAuthed/v2";
			JsonObject param = new JsonObject();
			param.put("key", key);
			param.put("appid", appid);
			if(maxlength != 0)
				param.put("maxlength", maxlength);
			if(enddate != 0)
				param.put("enddate", enddate);
			if(count != 0)
				param.put("count", count);
			if(feeds != null)
				param.put("feeds", feeds);
			if(tags != null)
				param.put("tags", tags);
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}
	}
    public static class ISteamPublishedItemSearch {

		/** 
		 * @param steamid -- SteamID of user
		 * @param appid -- appID of product
		 * @param startidx -- Starting index in the result set (0 based)
		 * @param count -- Number Requested
		 * @param tagcount -- Number of Tags Specified
		 * @param usertagcount -- Number of User specific tags requested
		 * @param hasappadminaccess -- Whether the user making the request is an admin for the app and can see private files
		 * @param fileType -- EPublishedFileInfoMatchingFileType, defaults to k_PFI_MatchingFileType_Items
		 * @param tag -- Tag to filter result set
		 * @param usertag -- A user specific tag
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void RankedByPublicationOrder(long steamid,int appid,int startidx,int count,int tagcount,int usertagcount,@Nullable boolean hasappadminaccess,@Nullable int fileType,@Nullable String[] tag,@Nullable String[] usertag,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/ISteamPublishedItemSearch/RankedByPublicationOrder/v1";
			JsonObject param = new JsonObject();
			param.put("steamid", steamid);
			param.put("appid", appid);
			param.put("startidx", startidx);
			param.put("count", count);
			param.put("tagcount", tagcount);
			param.put("usertagcount", usertagcount);
			if(fileType != 0)
				param.put("fileType", fileType);
			if (tag != null) {
				for (int i = 0; i < tag.length; i++) {
					param.put("tag["+i+"]", tag[i]);
				}
			}
			if (usertag != null) {
				for (int i = 0; i < usertag.length; i++) {
					param.put("usertag["+i+"]", usertag[i]);
				}
			}
			VxHolder.request(HttpMethod.POST, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param steamid -- SteamID of user
		 * @param appid -- appID of product
		 * @param startidx -- Starting index in the result set (0 based)
		 * @param count -- Number Requested
		 * @param tagcount -- Number of Tags Specified
		 * @param usertagcount -- Number of User specific tags requested
		 * @param hasappadminaccess -- Whether the user making the request is an admin for the app and can see private files
		 * @param fileType -- EPublishedFileInfoMatchingFileType, defaults to k_PFI_MatchingFileType_Items
		 * @param days -- [1,7] number of days for the trend period, including today
		 * @param tag -- Tag to filter result set
		 * @param usertag -- A user specific tag
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void RankedByTrend(long steamid,int appid,int startidx,int count,int tagcount,int usertagcount,@Nullable boolean hasappadminaccess,@Nullable int fileType,@Nullable int days,@Nullable String[] tag,@Nullable String[] usertag,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/ISteamPublishedItemSearch/RankedByTrend/v1";
			JsonObject param = new JsonObject();
			param.put("steamid", steamid);
			param.put("appid", appid);
			param.put("startidx", startidx);
			param.put("count", count);
			param.put("tagcount", tagcount);
			param.put("usertagcount", usertagcount);
			if(fileType != 0)
				param.put("fileType", fileType);
			if(days != 0)
				param.put("days", days);
			if (tag != null) {
				for (int i = 0; i < tag.length; i++) {
					param.put("tag["+i+"]", tag[i]);
				}
			}
			if (usertag != null) {
				for (int i = 0; i < usertag.length; i++) {
					param.put("usertag["+i+"]", usertag[i]);
				}
			}
			VxHolder.request(HttpMethod.POST, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param steamid -- SteamID of user
		 * @param appid -- appID of product
		 * @param startidx -- Starting index in the result set (0 based)
		 * @param count -- Number Requested
		 * @param tagcount -- Number of Tags Specified
		 * @param usertagcount -- Number of User specific tags requested
		 * @param hasappadminaccess -- Whether the user making the request is an admin for the app and can see private files
		 * @param fileType -- EPublishedFileInfoMatchingFileType, defaults to k_PFI_MatchingFileType_Items
		 * @param tag -- Tag to filter result set
		 * @param usertag -- A user specific tag
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void RankedByVote(long steamid,int appid,int startidx,int count,int tagcount,int usertagcount,@Nullable boolean hasappadminaccess,@Nullable int fileType,@Nullable String[] tag,@Nullable String[] usertag,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/ISteamPublishedItemSearch/RankedByVote/v1";
			JsonObject param = new JsonObject();
			param.put("steamid", steamid);
			param.put("appid", appid);
			param.put("startidx", startidx);
			param.put("count", count);
			param.put("tagcount", tagcount);
			param.put("usertagcount", usertagcount);
			if(fileType != 0)
				param.put("fileType", fileType);
			if (tag != null) {
				for (int i = 0; i < tag.length; i++) {
					param.put("tag["+i+"]", tag[i]);
				}
			}
			if (usertag != null) {
				for (int i = 0; i < usertag.length; i++) {
					param.put("usertag["+i+"]", usertag[i]);
				}
			}
			VxHolder.request(HttpMethod.POST, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param steamid -- SteamID of user
		 * @param appid -- appID relevant to all subsequent tags
		 * @param tagcount -- Number of Tags Specified
		 * @param usertagcount -- Number of User specific tags requested
		 * @param hasappadminaccess -- Whether the user making the request is an admin for the app and can see private files
		 * @param fileType -- EPublishedFileInfoMatchingFileType, defaults to k_PFI_MatchingFileType_Items
		 * @param tag -- Tag to filter result set
		 * @param usertag -- A user specific tag
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void ResultSetSummary(long steamid,long appid,int tagcount,int usertagcount,@Nullable boolean hasappadminaccess,@Nullable int fileType,@Nullable String[] tag,@Nullable String[] usertag,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/ISteamPublishedItemSearch/ResultSetSummary/v1";
			JsonObject param = new JsonObject();
			param.put("steamid", steamid);
			param.put("appid", appid);
			param.put("tagcount", tagcount);
			param.put("usertagcount", usertagcount);
			if(fileType != 0)
				param.put("fileType", fileType);
			if (tag != null) {
				for (int i = 0; i < tag.length; i++) {
					param.put("tag["+i+"]", tag[i]);
				}
			}
			if (usertag != null) {
				for (int i = 0; i < usertag.length; i++) {
					param.put("usertag["+i+"]", usertag[i]);
				}
			}
			VxHolder.request(HttpMethod.POST, requestURI, param, successHandler, failedHandler);
		}
	}
    public static class ISteamPublishedItemVoting {

		/** 
		 * @param steamid -- Steam ID of user
		 * @param appid -- appID of product
		 * @param count -- Count of how many items we are querying
		 * @param publishedfileid -- The Published File ID who's vote details are required
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void ItemVoteSummary(long steamid,int appid,int count,@Nullable long[] publishedfileid,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/ISteamPublishedItemVoting/ItemVoteSummary/v1";
			JsonObject param = new JsonObject();
			param.put("steamid", steamid);
			param.put("appid", appid);
			param.put("count", count);
			if (publishedfileid != null) {
				for (int i = 0; i < publishedfileid.length; i++) {
					param.put("publishedfileid["+i+"]", publishedfileid[i]);
				}
			}
			VxHolder.request(HttpMethod.POST, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param steamid -- Steam ID of user
		 * @param count -- Count of how many items we are querying
		 * @param publishedfileid -- A Specific Published Item
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void UserVoteSummary(long steamid,int count,@Nullable long[] publishedfileid,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/ISteamPublishedItemVoting/UserVoteSummary/v1";
			JsonObject param = new JsonObject();
			param.put("steamid", steamid);
			param.put("count", count);
			if (publishedfileid != null) {
				for (int i = 0; i < publishedfileid.length; i++) {
					param.put("publishedfileid["+i+"]", publishedfileid[i]);
				}
			}
			VxHolder.request(HttpMethod.POST, requestURI, param, successHandler, failedHandler);
		}
	}
    public static class ISteamRemoteStorage {

		/** 
		 * @param steamid -- SteamID of user
		 * @param appid -- appID of product
		 * @param listtype -- EUCMListType
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void EnumerateUserSubscribedFiles(long steamid,int appid,@Nullable int listtype,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/ISteamRemoteStorage/EnumerateUserSubscribedFiles/v1";
			JsonObject param = new JsonObject();
			param.put("steamid", steamid);
			param.put("appid", appid);
			if(listtype != 0)
				param.put("listtype", listtype);
			VxHolder.request(HttpMethod.POST, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param collectioncount -- Number of collections being requested
		 * @param publishedfileids -- collection ids to get the details for
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GetCollectionDetails(int collectioncount,long[] publishedfileids,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/ISteamRemoteStorage/GetCollectionDetails/v1";
			JsonObject param = new JsonObject();
			param.put("collectioncount", collectioncount);
			if (publishedfileids != null) {
				for (int i = 0; i < publishedfileids.length; i++) {
					param.put("publishedfileids["+i+"]", publishedfileids[i]);
				}
			}
			VxHolder.request(HttpMethod.POST, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param itemcount -- Number of items being requested
		 * @param publishedfileids -- published file id to look up
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GetPublishedFileDetails(int itemcount,long[] publishedfileids,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/ISteamRemoteStorage/GetPublishedFileDetails/v1";
			JsonObject param = new JsonObject();
			param.put("itemcount", itemcount);
			if (publishedfileids != null) {
				for (int i = 0; i < publishedfileids.length; i++) {
					param.put("publishedfileids["+i+"]", publishedfileids[i]);
				}
			}
			VxHolder.request(HttpMethod.POST, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param steamid -- If specified, only returns details if the file is owned by the SteamID specified
		 * @param ugcid -- ID of UGC file to get info for
		 * @param appid -- appID of product
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GetUGCFileDetails(@Nullable long steamid,long ugcid,int appid,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/ISteamRemoteStorage/GetUGCFileDetails/v1";
			JsonObject param = new JsonObject();
			if(steamid != 0)
				param.put("steamid", steamid);
			param.put("ugcid", ugcid);
			param.put("appid", appid);
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param steamid -- SteamID of user
		 * @param ugcid -- ID of UGC file whose bits are being fiddled with
		 * @param appid -- appID of product to change updating state for
		 * @param used -- New state of flag
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void SetUGCUsedByGC(long steamid,long ugcid,int appid,boolean used,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/ISteamRemoteStorage/SetUGCUsedByGC/v1";
			JsonObject param = new JsonObject();
			param.put("steamid", steamid);
			param.put("ugcid", ugcid);
			param.put("appid", appid);
			param.put("used", used);
			VxHolder.request(HttpMethod.POST, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param steamid -- SteamID of user
		 * @param appid -- appID of product
		 * @param publishedfileid -- published file id to subscribe to
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void SubscribePublishedFile(long steamid,int appid,long publishedfileid,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/ISteamRemoteStorage/SubscribePublishedFile/v1";
			JsonObject param = new JsonObject();
			param.put("steamid", steamid);
			param.put("appid", appid);
			param.put("publishedfileid", publishedfileid);
			VxHolder.request(HttpMethod.POST, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param steamid -- SteamID of user
		 * @param appid -- appID of product
		 * @param publishedfileid -- published file id to unsubscribe from
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void UnsubscribePublishedFile(long steamid,int appid,long publishedfileid,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/ISteamRemoteStorage/UnsubscribePublishedFile/v1";
			JsonObject param = new JsonObject();
			param.put("steamid", steamid);
			param.put("appid", appid);
			param.put("publishedfileid", publishedfileid);
			VxHolder.request(HttpMethod.POST, requestURI, param, successHandler, failedHandler);
		}
	}
    public static class ISteamSpecialSurvey {

		/** 
		 * @param key -- access key
		 * @param appid -- appid of game
		 * @param surveyid -- ID of the survey being taken
		 * @param steamid -- SteamID of the user taking the survey
		 * @param token -- Survey identity verification token for the user
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void CheckUserStatus(String key,int appid,int surveyid,long steamid,String token,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/ISteamSpecialSurvey/CheckUserStatus/v1";
			JsonObject param = new JsonObject();
			param.put("key", key);
			param.put("appid", appid);
			param.put("surveyid", surveyid);
			param.put("steamid", steamid);
			param.put("token", token);
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param key -- access key
		 * @param appid -- appid of game
		 * @param surveyid -- ID of the survey being taken
		 * @param steamid -- SteamID of the user taking the survey
		 * @param token -- Survey identity verification token for the user
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void SetUserFinished(String key,int appid,int surveyid,long steamid,String token,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/ISteamSpecialSurvey/SetUserFinished/v1";
			JsonObject param = new JsonObject();
			param.put("key", key);
			param.put("appid", appid);
			param.put("surveyid", surveyid);
			param.put("steamid", steamid);
			param.put("token", token);
			VxHolder.request(HttpMethod.POST, requestURI, param, successHandler, failedHandler);
		}
	}
    public static class ISteamUser {

		/** 
		 * @param key -- access key
		 * @param steamid -- SteamID of user
		 * @param appid -- AppID to check for ownership
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void CheckAppOwnership(String key,long steamid,int appid,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/ISteamUser/CheckAppOwnership/v1";
			JsonObject param = new JsonObject();
			param.put("key", key);
			param.put("steamid", steamid);
			param.put("appid", appid);
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param key -- access key
		 * @param steamid -- SteamID of user
		 * @param appid -- AppID to check for ownership
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void CheckAppOwnershipV2(String key,long steamid,int appid,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/ISteamUser/CheckAppOwnership/v2";
			JsonObject param = new JsonObject();
			param.put("key", key);
			param.put("steamid", steamid);
			param.put("appid", appid);
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param key -- access key
		 * @param steamid -- SteamID of user
		 * @param appids -- Comma-delimited list of appids (max: 100)
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GetAppPriceInfo(String key,long steamid,String appids,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/ISteamUser/GetAppPriceInfo/v1";
			JsonObject param = new JsonObject();
			param.put("key", key);
			param.put("steamid", steamid);
			param.put("appids", appids);
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param key -- access key
		 * @param steamid -- SteamID of user
		 * @param relationship -- relationship type (ex: friend)
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GetFriendList(String key,long steamid,@Nullable String relationship,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/ISteamUser/GetFriendList/v1";
			JsonObject param = new JsonObject();
			param.put("key", key);
			param.put("steamid", steamid);
			if(relationship != null)
				param.put("relationship", relationship);
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param key -- access key
		 * @param steamids -- Comma-delimited list of SteamIDs
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GetPlayerBans(String key,String steamids,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/ISteamUser/GetPlayerBans/v1";
			JsonObject param = new JsonObject();
			param.put("key", key);
			param.put("steamids", steamids);
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param key -- access key
		 * @param steamids -- Comma-delimited list of SteamIDs
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GetPlayerSummaries(String key,String steamids,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/ISteamUser/GetPlayerSummaries/v1";
			JsonObject param = new JsonObject();
			param.put("key", key);
			param.put("steamids", steamids);
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param key -- access key
		 * @param steamids -- Comma-delimited list of SteamIDs (max: 100)
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GetPlayerSummariesV2(String key,String steamids,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/ISteamUser/GetPlayerSummaries/v2";
			JsonObject param = new JsonObject();
			param.put("key", key);
			param.put("steamids", steamids);
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param key -- access key
		 * @param steamid -- SteamID of user
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GetPublisherAppOwnership(String key,long steamid,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/ISteamUser/GetPublisherAppOwnership/v1";
			JsonObject param = new JsonObject();
			param.put("key", key);
			param.put("steamid", steamid);
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param key -- access key
		 * @param steamid -- SteamID of user
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GetPublisherAppOwnershipV2(String key,long steamid,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/ISteamUser/GetPublisherAppOwnership/v2";
			JsonObject param = new JsonObject();
			param.put("key", key);
			param.put("steamid", steamid);
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param key -- access key
		 * @param steamid -- SteamID of user
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GetPublisherAppOwnershipV3(String key,long steamid,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/ISteamUser/GetPublisherAppOwnership/v3";
			JsonObject param = new JsonObject();
			param.put("key", key);
			param.put("steamid", steamid);
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param key -- access key
		 * @param packagerowversion -- The unsigned 64-bit row version to read package changes from. The row version of data read up to will be returned for use in future calls.
		 * @param cdkeyrowversion -- The unsigned 64-bit row version to read CD Key changes from. The row version of data read up to will be returned for use in future calls.
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GetPublisherAppOwnershipChanges(String key,String packagerowversion,String cdkeyrowversion,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/ISteamUser/GetPublisherAppOwnershipChanges/v1";
			JsonObject param = new JsonObject();
			param.put("key", key);
			param.put("packagerowversion", packagerowversion);
			param.put("cdkeyrowversion", cdkeyrowversion);
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param key -- access key
		 * @param steamid -- SteamID of user
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GetUserGroupList(String key,long steamid,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/ISteamUser/GetUserGroupList/v1";
			JsonObject param = new JsonObject();
			param.put("key", key);
			param.put("steamid", steamid);
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param key -- access key
		 * @param steamid -- SteamID of user
		 * @param packageid -- PackageID to grant
		 * @param ipaddress -- ip address of user in string format (xxx.xxx.xxx.xxx).
		 * @param thirdpartykey -- Optionally associate third party key during grant. 'thirdpartyappid' will have to be set.
		 * @param thirdpartyappid -- Has to be set if 'thirdpartykey' is set. The appid associated with the 'thirdpartykey'.
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GrantPackage(String key,long steamid,int packageid,@Nullable String ipaddress,@Nullable String thirdpartykey,@Nullable int thirdpartyappid,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/ISteamUser/GrantPackage/v1";
			JsonObject param = new JsonObject();
			param.put("key", key);
			param.put("steamid", steamid);
			param.put("packageid", packageid);
			if(ipaddress != null)
				param.put("ipaddress", ipaddress);
			if(thirdpartykey != null)
				param.put("thirdpartykey", thirdpartykey);
			if(thirdpartyappid != 0)
				param.put("thirdpartyappid", thirdpartyappid);
			VxHolder.request(HttpMethod.POST, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param key -- access key
		 * @param steamid -- SteamID of user
		 * @param packageid -- PackageID to grant
		 * @param ipaddress -- ip address of user in string format (xxx.xxx.xxx.xxx).
		 * @param thirdpartykey -- Optionally associate third party key during grant. 'thirdpartyappid' will have to be set.
		 * @param thirdpartyappid -- Has to be set if 'thirdpartykey' is set. The appid associated with the 'thirdpartykey'.
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GrantPackageV2(String key,long steamid,int packageid,@Nullable String ipaddress,@Nullable String thirdpartykey,@Nullable int thirdpartyappid,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/ISteamUser/GrantPackage/v2";
			JsonObject param = new JsonObject();
			param.put("key", key);
			param.put("steamid", steamid);
			param.put("packageid", packageid);
			if(ipaddress != null)
				param.put("ipaddress", ipaddress);
			if(thirdpartykey != null)
				param.put("thirdpartykey", thirdpartykey);
			if(thirdpartyappid != 0)
				param.put("thirdpartyappid", thirdpartyappid);
			VxHolder.request(HttpMethod.POST, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param key -- access key
		 * @param steamid -- SteamID of user
		 * @param packageid -- PackageID to grant
		 * @param ipaddress -- ip address of user in string format (xxx.xxx.xxx.xxx).
		 * @param thirdpartykey -- Optionally associate third party key during grant. 'thirdpartyappid' will have to be set.
		 * @param thirdpartyappid -- Has to be set if 'thirdpartykey' is set. The appid associated with the 'thirdpartykey'.
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GrantPackageV3(String key,long steamid,int packageid,@Nullable String ipaddress,@Nullable String thirdpartykey,@Nullable int thirdpartyappid,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/ISteamUser/GrantPackage/v3";
			JsonObject param = new JsonObject();
			param.put("key", key);
			param.put("steamid", steamid);
			param.put("packageid", packageid);
			if(ipaddress != null)
				param.put("ipaddress", ipaddress);
			if(thirdpartykey != null)
				param.put("thirdpartykey", thirdpartykey);
			if(thirdpartyappid != 0)
				param.put("thirdpartyappid", thirdpartyappid);
			VxHolder.request(HttpMethod.POST, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param key -- access key
		 * @param vanityurl -- The vanity URL to get a SteamID for
		 * @param url_type -- The type of vanity URL. 1 (default): Individual profile, 2: Group, 3: Official game group
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void ResolveVanityURL(String key,String vanityurl,@Nullable int url_type,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/ISteamUser/ResolveVanityURL/v1";
			JsonObject param = new JsonObject();
			param.put("key", key);
			param.put("vanityurl", vanityurl);
			if(url_type != 0)
				param.put("url_type", url_type);
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param key -- access key
		 * @param steamid -- SteamID of user
		 * @param packageid -- PackageID to grant
		 * @param revokereason -- Reason for why to revoke
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void RevokePackage(String key,long steamid,int packageid,String revokereason,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/ISteamUser/RevokePackage/v1";
			JsonObject param = new JsonObject();
			param.put("key", key);
			param.put("steamid", steamid);
			param.put("packageid", packageid);
			param.put("revokereason", revokereason);
			VxHolder.request(HttpMethod.POST, requestURI, param, successHandler, failedHandler);
		}
	}
    public static class ISteamUserAuth {

		/** 
		 * @param steamid -- Should be the users steamid, unencrypted.
		 * @param sessionkey -- Should be a 32 byte random blob of data, which is then encrypted with RSA using the Steam system's public key.  Randomness is important here for security.
		 * @param encrypted_loginkey -- Should be the users hashed loginkey, AES encrypted with the sessionkey.
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void AuthenticateUser(long steamid,byte[] sessionkey,byte[] encrypted_loginkey,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/ISteamUserAuth/AuthenticateUser/v1";
			JsonObject param = new JsonObject();
			param.put("steamid", steamid);
			param.put("sessionkey", sessionkey);
			param.put("encrypted_loginkey", encrypted_loginkey);
			VxHolder.request(HttpMethod.POST, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param key -- access key
		 * @param appid -- appid of game
		 * @param ticket -- Ticket from GetAuthSessionTicket.
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void AuthenticateUserTicket(String key,int appid,String ticket,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/ISteamUserAuth/AuthenticateUserTicket/v1";
			JsonObject param = new JsonObject();
			param.put("key", key);
			param.put("appid", appid);
			param.put("ticket", ticket);
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}
	}
    public static class ISteamUserOAuth {

		/** 
		 * @param access_token -- OAuth2 token for which to return details
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GetTokenDetails(String access_token,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/ISteamUserOAuth/GetTokenDetails/v1";
			JsonObject param = new JsonObject();
			param.put("access_token", access_token);
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}
	}
    public static class ISteamUserStats {

		/** 
		 * @param gameid -- GameID to retrieve the achievement percentages for
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GetGlobalAchievementPercentagesForApp(long gameid,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/ISteamUserStats/GetGlobalAchievementPercentagesForApp/v1";
			JsonObject param = new JsonObject();
			param.put("gameid", gameid);
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param gameid -- GameID to retrieve the achievement percentages for
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GetGlobalAchievementPercentagesForAppV2(long gameid,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/ISteamUserStats/GetGlobalAchievementPercentagesForApp/v2";
			JsonObject param = new JsonObject();
			param.put("gameid", gameid);
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param appid -- AppID that we're getting global stats for
		 * @param count -- Number of stats get data for
		 * @param name -- Names of stat to get data for
		 * @param startdate -- Start date for daily totals (unix epoch timestamp)
		 * @param enddate -- End date for daily totals (unix epoch timestamp)
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GetGlobalStatsForGame(int appid,int count,String[] name,@Nullable int startdate,@Nullable int enddate,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/ISteamUserStats/GetGlobalStatsForGame/v1";
			JsonObject param = new JsonObject();
			param.put("appid", appid);
			param.put("count", count);
			if (name != null) {
				for (int i = 0; i < name.length; i++) {
					param.put("name["+i+"]", name[i]);
				}
			}
			if(startdate != 0)
				param.put("startdate", startdate);
			if(enddate != 0)
				param.put("enddate", enddate);
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param appid -- AppID that we're getting user count for
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GetNumberOfCurrentPlayers(int appid,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/ISteamUserStats/GetNumberOfCurrentPlayers/v1";
			JsonObject param = new JsonObject();
			param.put("appid", appid);
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param key -- access key
		 * @param steamid -- SteamID of user
		 * @param appid -- AppID to get achievements for
		 * @param l -- Language to return strings for
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GetPlayerAchievements(String key,long steamid,int appid,@Nullable String l,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/ISteamUserStats/GetPlayerAchievements/v1";
			JsonObject param = new JsonObject();
			param.put("key", key);
			param.put("steamid", steamid);
			param.put("appid", appid);
			if(l != null)
				param.put("l", l);
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param key -- access key
		 * @param appid -- appid of game
		 * @param l -- localized langauge to return (english, french, etc.)
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GetSchemaForGame(String key,int appid,@Nullable String l,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/ISteamUserStats/GetSchemaForGame/v1";
			JsonObject param = new JsonObject();
			param.put("key", key);
			param.put("appid", appid);
			if(l != null)
				param.put("l", l);
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param key -- access key
		 * @param appid -- appid of game
		 * @param l -- localized language to return (english, french, etc.)
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GetSchemaForGameV2(String key,int appid,@Nullable String l,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/ISteamUserStats/GetSchemaForGame/v2";
			JsonObject param = new JsonObject();
			param.put("key", key);
			param.put("appid", appid);
			if(l != null)
				param.put("l", l);
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param key -- access key
		 * @param steamid -- SteamID of user
		 * @param appid -- appid of game
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GetUserStatsForGame(String key,long steamid,int appid,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/ISteamUserStats/GetUserStatsForGame/v1";
			JsonObject param = new JsonObject();
			param.put("key", key);
			param.put("steamid", steamid);
			param.put("appid", appid);
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param key -- access key
		 * @param steamid -- SteamID of user
		 * @param appid -- appid of game
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GetUserStatsForGameV2(String key,long steamid,int appid,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/ISteamUserStats/GetUserStatsForGame/v2";
			JsonObject param = new JsonObject();
			param.put("key", key);
			param.put("steamid", steamid);
			param.put("appid", appid);
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param key -- access key
		 * @param steamid -- SteamID of user
		 * @param appid -- appid of game
		 * @param count -- Number of stats and achievements to set a value for (name/value param pairs)
		 * @param name -- Name of stat or achievement to set
		 * @param value -- Value to set
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void SetUserStatsForGame(String key,long steamid,int appid,int count,String[] name,int[] value,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/ISteamUserStats/SetUserStatsForGame/v1";
			JsonObject param = new JsonObject();
			param.put("key", key);
			param.put("steamid", steamid);
			param.put("appid", appid);
			param.put("count", count);
			if (name != null) {
				for (int i = 0; i < name.length; i++) {
					param.put("name["+i+"]", name[i]);
				}
			}
			if (value != null) {
				for (int i = 0; i < value.length; i++) {
					param.put("value["+i+"]", value[i]);
				}
			}
			VxHolder.request(HttpMethod.POST, requestURI, param, successHandler, failedHandler);
		}
	}
    public static class ISteamWebAPIUtil {

		/** 
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GetServerInfo(Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/ISteamWebAPIUtil/GetServerInfo/v1";
			JsonObject param = new JsonObject();
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param key -- access key
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GetSupportedAPIList(@Nullable String key,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/ISteamWebAPIUtil/GetSupportedAPIList/v1";
			JsonObject param = new JsonObject();
			if(key != null)
				param.put("key", key);
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}
	}
    public static class ISteamWebUserPresenceOAuth {

		/** 
		 * @param steamid -- Steam ID of the user
		 * @param umqid -- UMQ Session ID
		 * @param message -- Message that was last known to the user
		 * @param pollid -- Caller-specific poll id
		 * @param sectimeout -- Long-poll timeout in seconds
		 * @param secidletime -- How many seconds is client considering itself idle, e.g. screen is off
		 * @param use_accountids -- Boolean, 0 (default): return steamid_from in output, 1: return accountid_from
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void PollStatus(String steamid,long umqid,int message,@Nullable int pollid,@Nullable int sectimeout,@Nullable int secidletime,@Nullable int use_accountids,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/ISteamWebUserPresenceOAuth/PollStatus/v1";
			JsonObject param = new JsonObject();
			param.put("steamid", steamid);
			param.put("umqid", umqid);
			param.put("message", message);
			if(pollid != 0)
				param.put("pollid", pollid);
			if(sectimeout != 0)
				param.put("sectimeout", sectimeout);
			if(secidletime != 0)
				param.put("secidletime", secidletime);
			if(use_accountids != 0)
				param.put("use_accountids", use_accountids);
			VxHolder.request(HttpMethod.POST, requestURI, param, successHandler, failedHandler);
		}
	}
    public static class ISteamWorkshop {

		/** 
		 * @param appid -- AppID of game this transaction is for
		 * @param itemcount -- Number of items to associate
		 * @param publishedfileid -- the workshop published file id
		 * @param gameitemid -- 3rd party ID for item
		 * @param revenuepercentage -- Percentage of revenue the owners of the workshop item will get from the sale of the item [0.0, 100.0].  For bundle-like items, send over an entry for each item in the bundle (gameitemid = bundle id) with different publishedfileids and with the revenue percentage pre-split amongst the items in the bundle (i.e. 30% / 10 items in the bundle)
		 * @param gameitemdescription -- Game's description of the game item
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void AssociateWorkshopItems(int appid,int itemcount,@Nullable long[] publishedfileid,@Nullable int[] gameitemid,@Nullable float[] revenuepercentage,@Nullable String[] gameitemdescription,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/ISteamWorkshop/AssociateWorkshopItems/v1";
			JsonObject param = new JsonObject();
			param.put("appid", appid);
			param.put("itemcount", itemcount);
			if (publishedfileid != null) {
				for (int i = 0; i < publishedfileid.length; i++) {
					param.put("publishedfileid["+i+"]", publishedfileid[i]);
				}
			}
			if (gameitemid != null) {
				for (int i = 0; i < gameitemid.length; i++) {
					param.put("gameitemid["+i+"]", gameitemid[i]);
				}
			}
			if (revenuepercentage != null) {
				for (int i = 0; i < revenuepercentage.length; i++) {
					param.put("revenuepercentage["+i+"]", revenuepercentage[i]);
				}
			}
			if (gameitemdescription != null) {
				for (int i = 0; i < gameitemdescription.length; i++) {
					param.put("gameitemdescription["+i+"]", gameitemdescription[i]);
				}
			}
			VxHolder.request(HttpMethod.POST, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param appid -- AppID of game this transaction is for
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GetContributors(int appid,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/ISteamWorkshop/GetContributors/v1";
			JsonObject param = new JsonObject();
			param.put("appid", appid);
			VxHolder.request(HttpMethod.POST, requestURI, param, successHandler, failedHandler);
		}
	}
    public static class ITFItems_440 {

		/** 
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GetGoldenWrenches(Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/ITFItems_440/GetGoldenWrenches/v1";
			JsonObject param = new JsonObject();
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GetGoldenWrenchesV2(Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/ITFItems_440/GetGoldenWrenches/v2";
			JsonObject param = new JsonObject();
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}
	}
    public static class ITFPromos_205790 {

		/** 
		 * @param steamid -- The Steam ID to fetch items for
		 * @param promoid -- The promo ID to grant an item for
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GetItemID(long steamid,int promoid,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/ITFPromos_205790/GetItemID/v1";
			JsonObject param = new JsonObject();
			param.put("steamid", steamid);
			param.put("promoid", promoid);
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param steamid -- The Steam ID to fetch items for
		 * @param promoid -- The promo ID to grant an item for
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GrantItem(long steamid,int promoid,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/ITFPromos_205790/GrantItem/v1";
			JsonObject param = new JsonObject();
			param.put("steamid", steamid);
			param.put("promoid", promoid);
			VxHolder.request(HttpMethod.POST, requestURI, param, successHandler, failedHandler);
		}
	}
    public static class ITFPromos_440 {

		/** 
		 * @param steamid -- The Steam ID to fetch items for
		 * @param promoid -- The promo ID to grant an item for
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GetItemID(long steamid,int promoid,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/ITFPromos_440/GetItemID/v1";
			JsonObject param = new JsonObject();
			param.put("steamid", steamid);
			param.put("promoid", promoid);
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param steamid -- The Steam ID to fetch items for
		 * @param promoid -- The promo ID to grant an item for
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GrantItem(long steamid,int promoid,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/ITFPromos_440/GrantItem/v1";
			JsonObject param = new JsonObject();
			param.put("steamid", steamid);
			param.put("promoid", promoid);
			VxHolder.request(HttpMethod.POST, requestURI, param, successHandler, failedHandler);
		}
	}
    public static class ITFPromos_620 {

		/** 
		 * @param steamid -- The Steam ID to fetch items for
		 * @param PromoID -- The promo ID to grant an item for
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GetItemID(long steamid,int PromoID,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/ITFPromos_620/GetItemID/v1";
			JsonObject param = new JsonObject();
			param.put("steamid", steamid);
			param.put("PromoID", PromoID);
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param steamid -- The Steam ID to fetch items for
		 * @param PromoID -- The promo ID to grant an item for
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GrantItem(long steamid,int PromoID,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/ITFPromos_620/GrantItem/v1";
			JsonObject param = new JsonObject();
			param.put("steamid", steamid);
			param.put("PromoID", PromoID);
			VxHolder.request(HttpMethod.POST, requestURI, param, successHandler, failedHandler);
		}
	}
    public static class ITFSystem_440 {

		/** 
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GetWorldStatus(Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/ITFSystem_440/GetWorldStatus/v1";
			JsonObject param = new JsonObject();
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}
	}
    public static class IGameServersService {

		/** 
		 * @param key -- Access key
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GetAccountList(String key,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/IGameServersService/GetAccountList/v1";
			JsonObject param = new JsonObject();
			param.put("key", key);
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param key -- Access key
		 * @param appid -- The app to use the account for
		 * @param memo -- The memo to set on the new account
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void CreateAccount(String key,int appid,String memo,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/IGameServersService/CreateAccount/v1";
			JsonObject param = new JsonObject();
			param.put("key", key);
			param.put("appid", appid);
			param.put("memo", memo);
			VxHolder.request(HttpMethod.POST, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param key -- Access key
		 * @param steamid -- The SteamID of the game server to set the memo on
		 * @param memo -- The memo to set on the new account
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void SetMemo(String key,long steamid,String memo,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/IGameServersService/SetMemo/v1";
			JsonObject param = new JsonObject();
			param.put("key", key);
			param.put("steamid", steamid);
			param.put("memo", memo);
			VxHolder.request(HttpMethod.POST, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param key -- Access key
		 * @param steamid -- The SteamID of the game server to reset the login token of
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void ResetLoginToken(String key,long steamid,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/IGameServersService/ResetLoginToken/v1";
			JsonObject param = new JsonObject();
			param.put("key", key);
			param.put("steamid", steamid);
			VxHolder.request(HttpMethod.POST, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param key -- Access key
		 * @param steamid -- The SteamID of the game server account to delete
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void DeleteAccount(String key,long steamid,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/IGameServersService/DeleteAccount/v1";
			JsonObject param = new JsonObject();
			param.put("key", key);
			param.put("steamid", steamid);
			VxHolder.request(HttpMethod.POST, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param key -- Access key
		 * @param steamid -- The SteamID of the game server to get info on
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GetAccountPublicInfo(String key,long steamid,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/IGameServersService/GetAccountPublicInfo/v1";
			JsonObject param = new JsonObject();
			param.put("key", key);
			param.put("steamid", steamid);
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param key -- Access key
		 * @param login_token -- Login token to query
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void QueryLoginToken(String key,String login_token,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/IGameServersService/QueryLoginToken/v1";
			JsonObject param = new JsonObject();
			param.put("key", key);
			param.put("login_token", login_token);
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param key -- Access key
		 * @param steamid
		 * @param banned
		 * @param ban_seconds
		 * @param appid
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void SetBanStatus(String key,long steamid,boolean banned,int ban_seconds,int appid,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/IGameServersService/SetBanStatus/v1";
			JsonObject param = new JsonObject();
			param.put("key", key);
			param.put("steamid", steamid);
			param.put("banned", banned);
			param.put("ban_seconds", ban_seconds);
			param.put("appid", appid);
			VxHolder.request(HttpMethod.POST, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param key -- Access key
		 * @param server_ips
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GetServerSteamIDsByIP(String key,String server_ips,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/IGameServersService/GetServerSteamIDsByIP/v1";
			JsonObject param = new JsonObject();
			param.put("key", key);
			param.put("server_ips", server_ips);
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param key -- Access key
		 * @param server_steamids
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GetServerIPsBySteamID(String key,long server_steamids,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/IGameServersService/GetServerIPsBySteamID/v1";
			JsonObject param = new JsonObject();
			param.put("key", key);
			param.put("server_steamids", server_steamids);
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param key -- Access key
		 * @param fake_ip -- FakeIP of server to query.
		 * @param fake_port -- Fake port of server to query.
		 * @param app_id -- AppID to use.  Each AppID has its own FakeIP address.
		 * @param query_type -- What type of query?
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void QueryByFakeIP(String key,int fake_ip,int fake_port,int app_id,int query_type,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/IGameServersService/QueryByFakeIP/v1";
			JsonObject param = new JsonObject();
			param.put("key", key);
			param.put("fake_ip", fake_ip);
			param.put("fake_port", fake_port);
			param.put("app_id", app_id);
			param.put("query_type", query_type);
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}
	}
    public static class IPlayerService {

		/** 
		 * @param key -- Access key
		 * @param steamid -- The player we're asking about
		 * @param appid_playing -- The game player is currently playing
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void IsPlayingSharedGame(String key,long steamid,int appid_playing,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/IPlayerService/IsPlayingSharedGame/v1";
			JsonObject param = new JsonObject();
			param.put("key", key);
			param.put("steamid", steamid);
			param.put("appid_playing", appid_playing);
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param steamid
		 * @param ticket
		 * @param play_sessions
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void RecordOfflinePlaytime(long steamid,String ticket,JsonObject play_sessions,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/IPlayerService/RecordOfflinePlaytime/v1";
			JsonObject param = new JsonObject();
			param.put("steamid", steamid);
			param.put("ticket", ticket);
			param.put("play_sessions", play_sessions);
			VxHolder.request(HttpMethod.POST, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param key -- Access key
		 * @param steamid -- The player we're asking about
		 * @param count -- The number of games to return (0/unset: all)
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GetRecentlyPlayedGames(String key,long steamid,int count,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/IPlayerService/GetRecentlyPlayedGames/v1";
			JsonObject param = new JsonObject();
			param.put("key", key);
			param.put("steamid", steamid);
			param.put("count", count);
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param key -- Access key
		 * @param steamid -- The player we're asking about
		 * @param include_appinfo -- true if we want additional details (name, icon) about each game
		 * @param include_played_free_games -- Free games are excluded by default.  If this is set, free games the user has played will be returned.
		 * @param appids_filter -- if set, restricts result set to the passed in apps
		 * @param include_free_sub -- Some games are in the free sub, which are excluded by default.
		 * @param skip_unvetted_apps -- if set, skip unvetted store apps
		 * @param language -- Will return appinfo in this language
		 * @param include_extended_appinfo -- true if we want even more details (capsule, sortas, and capabilities) about each game.  include_appinfo must also be true.
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GetOwnedGames(String key,long steamid,boolean include_appinfo,boolean include_played_free_games,int appids_filter,boolean include_free_sub,@Nullable boolean skip_unvetted_apps,String language,boolean include_extended_appinfo,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/IPlayerService/GetOwnedGames/v1";
			JsonObject param = new JsonObject();
			param.put("key", key);
			param.put("steamid", steamid);
			param.put("include_appinfo", include_appinfo);
			param.put("include_played_free_games", include_played_free_games);
			param.put("appids_filter", appids_filter);
			param.put("include_free_sub", include_free_sub);
			param.put("language", language);
			param.put("include_extended_appinfo", include_extended_appinfo);
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param key -- Access key
		 * @param steamid -- The player we're asking about
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GetSteamLevel(String key,long steamid,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/IPlayerService/GetSteamLevel/v1";
			JsonObject param = new JsonObject();
			param.put("key", key);
			param.put("steamid", steamid);
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param key -- Access key
		 * @param steamid -- The player we're asking about
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GetBadges(String key,long steamid,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/IPlayerService/GetBadges/v1";
			JsonObject param = new JsonObject();
			param.put("key", key);
			param.put("steamid", steamid);
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param key -- Access key
		 * @param steamid -- The player we're asking about
		 * @param badgeid -- The badge we're asking about
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GetCommunityBadgeProgress(String key,long steamid,int badgeid,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/IPlayerService/GetCommunityBadgeProgress/v1";
			JsonObject param = new JsonObject();
			param.put("key", key);
			param.put("steamid", steamid);
			param.put("badgeid", badgeid);
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}
	}
    public static class IBroadcastService {

		/** 
		 * @param key -- Access key
		 * @param appid
		 * @param steamid
		 * @param broadcast_id
		 * @param frame_data
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void PostGameDataFrame(String key,int appid,long steamid,long broadcast_id,String frame_data,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/IBroadcastService/PostGameDataFrame/v1";
			JsonObject param = new JsonObject();
			param.put("key", key);
			param.put("appid", appid);
			param.put("steamid", steamid);
			param.put("broadcast_id", broadcast_id);
			param.put("frame_data", frame_data);
			VxHolder.request(HttpMethod.POST, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param appid -- AppID of the game being broadcasted
		 * @param steamid -- Broadcasters SteamID
		 * @param rtmp_token -- Valid RTMP token for the Broadcaster
		 * @param frame_data -- game data frame expressing current state of game (string, zipped, whatever)
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void PostGameDataFrameRTMP(int appid,long steamid,String rtmp_token,String frame_data,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/IBroadcastService/PostGameDataFrameRTMP/v1";
			JsonObject param = new JsonObject();
			param.put("appid", appid);
			param.put("steamid", steamid);
			param.put("rtmp_token", rtmp_token);
			param.put("frame_data", frame_data);
			VxHolder.request(HttpMethod.POST, requestURI, param, successHandler, failedHandler);
		}
	}
    public static class IChatRoomService {

		/** 
		 * @param key -- Access key
		 * @param steamid_owner
		 * @param appid
		 * @param name
		 * @param room_type
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void CreateAppChatRoomGroup(String key,long steamid_owner,int appid,String name,int room_type,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/IChatRoomService/CreateAppChatRoomGroup/v1";
			JsonObject param = new JsonObject();
			param.put("key", key);
			param.put("steamid_owner", steamid_owner);
			param.put("appid", appid);
			param.put("name", name);
			param.put("room_type", room_type);
			VxHolder.request(HttpMethod.POST, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param key -- Access key
		 * @param chat_group_id
		 * @param steamids
		 * @param appid
		 * @param suppress_log_messages
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void AppAddUsersToGroup(String key,long chat_group_id,long steamids,int appid,boolean suppress_log_messages,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/IChatRoomService/AppAddUsersToGroup/v1";
			JsonObject param = new JsonObject();
			param.put("key", key);
			param.put("chat_group_id", chat_group_id);
			param.put("steamids", steamids);
			param.put("appid", appid);
			param.put("suppress_log_messages", suppress_log_messages);
			VxHolder.request(HttpMethod.POST, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param key -- Access key
		 * @param appid
		 * @param chat_group_id
		 * @param message
		 * @param loc_token
		 * @param params
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void AppPostSystemMessageToGroup(String key,int appid,long chat_group_id,String message,String loc_token,JsonObject params,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/IChatRoomService/AppPostSystemMessageToGroup/v1";
			JsonObject param = new JsonObject();
			param.put("key", key);
			param.put("appid", appid);
			param.put("chat_group_id", chat_group_id);
			param.put("message", message);
			param.put("loc_token", loc_token);
			param.put("params", params);
			VxHolder.request(HttpMethod.POST, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param key -- Access key
		 * @param chat_group_id
		 * @param steamid_targets
		 * @param kick_expiration
		 * @param appid
		 * @param steamid_kick_actor
		 * @param suppress_log_messages
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void AppRemoveUsersFromGroup(String key,long chat_group_id,long steamid_targets,int kick_expiration,int appid,long steamid_kick_actor,boolean suppress_log_messages,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/IChatRoomService/AppRemoveUsersFromGroup/v1";
			JsonObject param = new JsonObject();
			param.put("key", key);
			param.put("chat_group_id", chat_group_id);
			param.put("steamid_targets", steamid_targets);
			param.put("kick_expiration", kick_expiration);
			param.put("appid", appid);
			param.put("steamid_kick_actor", steamid_kick_actor);
			param.put("suppress_log_messages", suppress_log_messages);
			VxHolder.request(HttpMethod.POST, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param key -- Access key
		 * @param chat_group_id
		 * @param appid
		 * @param name
		 * @param avatar_ugc_id
		 * @param allow_user_invites
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void SetAppChatRoomConfig(String key,long chat_group_id,int appid,String name,long avatar_ugc_id,boolean allow_user_invites,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/IChatRoomService/SetAppChatRoomConfig/v1";
			JsonObject param = new JsonObject();
			param.put("key", key);
			param.put("chat_group_id", chat_group_id);
			param.put("appid", appid);
			param.put("name", name);
			param.put("avatar_ugc_id", avatar_ugc_id);
			param.put("allow_user_invites", allow_user_invites);
			VxHolder.request(HttpMethod.POST, requestURI, param, successHandler, failedHandler);
		}
	}
    public static class IContentFilteringService {

		/** 
		 * @param key -- Access key
		 * @param appid -- AppID that is asking to having filtering performed.
		 * @param language -- In which language should filtering be performed. If empty, no profanity filtering will be performed.
		 * @param legal_filtering_country -- If set to an ISO 3166-1 Alpha-2 country code that requires legal filtering, that legal filtering will be performed.
		 * @param raw_strings -- The list of strings to be filtered.
		 * @param is_name -- True if the strings are names instead of chat text
		 * @param steamid -- The SteamID of the person viewing the text
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void FilterStrings(String key,int appid,String language,String legal_filtering_country,String raw_strings,boolean is_name,long steamid,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/IContentFilteringService/FilterStrings/v1";
			JsonObject param = new JsonObject();
			param.put("key", key);
			param.put("appid", appid);
			param.put("language", language);
			param.put("legal_filtering_country", legal_filtering_country);
			param.put("raw_strings", raw_strings);
			param.put("is_name", is_name);
			param.put("steamid", steamid);
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}
	}
    public static class IContentServerConfigService {

		/** 
		 * @param key -- Access key
		 * @param cache_id -- Unique ID number
		 * @param cache_key -- Valid current cache API key
		 * @param change_notes -- Notes
		 * @param allowed_ip_blocks -- comma-separated list of allowed IP address blocks in CIDR format - blank to clear unfilter
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void SetSteamCacheClientFilters(String key,int cache_id,String cache_key,String change_notes,String allowed_ip_blocks,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/IContentServerConfigService/SetSteamCacheClientFilters/v1";
			JsonObject param = new JsonObject();
			param.put("key", key);
			param.put("cache_id", cache_id);
			param.put("cache_key", cache_key);
			param.put("change_notes", change_notes);
			param.put("allowed_ip_blocks", allowed_ip_blocks);
			VxHolder.request(HttpMethod.POST, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param key -- Access key
		 * @param cache_id -- Unique ID number
		 * @param cache_key -- Valid current cache API key
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GetSteamCacheNodeParams(String key,int cache_id,String cache_key,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/IContentServerConfigService/GetSteamCacheNodeParams/v1";
			JsonObject param = new JsonObject();
			param.put("key", key);
			param.put("cache_id", cache_id);
			param.put("cache_key", cache_key);
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param key -- Access key
		 * @param cache_id -- Unique ID number
		 * @param cache_key -- Valid current cache API key
		 * @param mbps_sent -- Outgoing network traffic in Mbps
		 * @param mbps_recv -- Incoming network traffic in Mbps
		 * @param cpu_percent -- Percent CPU load
		 * @param cache_hit_percent -- Percent cache hits
		 * @param num_connected_ips -- Number of unique connected IP addresses
		 * @param upstream_egress_utilization -- What is the percent utilization of the busiest datacenter egress link?
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void SetSteamCachePerformanceStats(String key,int cache_id,String cache_key,int mbps_sent,int mbps_recv,int cpu_percent,int cache_hit_percent,int num_connected_ips,int upstream_egress_utilization,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/IContentServerConfigService/SetSteamCachePerformanceStats/v1";
			JsonObject param = new JsonObject();
			param.put("key", key);
			param.put("cache_id", cache_id);
			param.put("cache_key", cache_key);
			param.put("mbps_sent", mbps_sent);
			param.put("mbps_recv", mbps_recv);
			param.put("cpu_percent", cpu_percent);
			param.put("cache_hit_percent", cache_hit_percent);
			param.put("num_connected_ips", num_connected_ips);
			param.put("upstream_egress_utilization", upstream_egress_utilization);
			VxHolder.request(HttpMethod.POST, requestURI, param, successHandler, failedHandler);
		}
	}
    public static class IContentServerDirectoryService {

		/** 
		 * @param cell_id -- client Cell ID
		 * @param max_servers -- max servers in response list
		 * @param ip_override -- client IP address
		 * @param launcher_type -- launcher type
		 * @param ipv6_public -- client public ipv6 address if it knows it
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GetServersForSteamPipe(int cell_id,@Nullable int max_servers,@Nullable String ip_override,@Nullable int launcher_type,@Nullable String ipv6_public,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/IContentServerDirectoryService/GetServersForSteamPipe/v1";
			JsonObject param = new JsonObject();
			param.put("cell_id", cell_id);
			if(max_servers != 0)
				param.put("max_servers", max_servers);
			if(ip_override != null)
				param.put("ip_override", ip_override);
			if(launcher_type != 0)
				param.put("launcher_type", launcher_type);
			if(ipv6_public != null)
				param.put("ipv6_public", ipv6_public);
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param cached_signature
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GetClientUpdateHosts(String cached_signature,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/IContentServerDirectoryService/GetClientUpdateHosts/v1";
			JsonObject param = new JsonObject();
			param.put("cached_signature", cached_signature);
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param appid
		 * @param depotid
		 * @param source_manifestid
		 * @param target_manifestid
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GetDepotPatchInfo(int appid,int depotid,long source_manifestid,long target_manifestid,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/IContentServerDirectoryService/GetDepotPatchInfo/v1";
			JsonObject param = new JsonObject();
			param.put("appid", appid);
			param.put("depotid", depotid);
			param.put("source_manifestid", source_manifestid);
			param.put("target_manifestid", target_manifestid);
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}
	}
    public static class IOnlinePlayService {

		/** 
		 * @param key -- Access key
		 * @param steamid -- Steam ID 1 of request
		 * @param steamid2 -- Steam ID 2 of request
		 * @param appid -- App ID of request
		 * @param time_range_begin -- unix time range begin to check
		 * @param time_range_end -- unix time range end to check
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GetCoPlayStatus(String key,long steamid,long steamid2,int appid,int time_range_begin,int time_range_end,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/IOnlinePlayService/GetCoPlayStatus/v1";
			JsonObject param = new JsonObject();
			param.put("key", key);
			param.put("steamid", steamid);
			param.put("steamid2", steamid2);
			param.put("appid", appid);
			param.put("time_range_begin", time_range_begin);
			param.put("time_range_end", time_range_end);
			VxHolder.request(HttpMethod.POST, requestURI, param, successHandler, failedHandler);
		}
	}
    public static class IPublishedFileService {

		/** 
		 * @param key -- Access key
		 * @param appid
		 * @param matching_file_type -- EPublishedFileInfoMatchingFileType
		 * @param tags -- Include files that have all the tags or any of the tags if match_all_tags is set to false.
		 * @param match_all_tags -- If true, then files must have all the tags specified.  If false, then must have at least one of the tags specified.
		 * @param excluded_tags -- Exclude any files that have any of these tags.
		 * @param desired_queue_size -- Desired number of items in the voting queue.  May be clamped by the server
		 * @param desired_revision -- Filter to items that have data for the specified revision.
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void RefreshVotingQueue(String key,int appid,int matching_file_type,String tags,@Nullable boolean match_all_tags,String excluded_tags,int desired_queue_size,@Nullable int desired_revision,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/IPublishedFileService/RefreshVotingQueue/v1";
			JsonObject param = new JsonObject();
			param.put("key", key);
			param.put("appid", appid);
			param.put("matching_file_type", matching_file_type);
			param.put("tags", tags);
			param.put("excluded_tags", excluded_tags);
			param.put("desired_queue_size", desired_queue_size);
			if(desired_revision != 0)
				param.put("desired_revision", desired_revision);
			VxHolder.request(HttpMethod.POST, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param publishedfileids
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GetUserVoteSummary(long publishedfileids,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/IPublishedFileService/GetUserVoteSummary/v1";
			JsonObject param = new JsonObject();
			param.put("publishedfileids", publishedfileids);
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param key -- Access key
		 * @param query_type -- enumeration EPublishedFileQueryType in clientenums.h
		 * @param page -- Current page
		 * @param cursor -- Cursor to paginate through the results (set to '*' for the first request).  Prefer this over using the page parameter, as it will allow you to do deep pagination.  When used, the page parameter will be ignored.
		 * @param numperpage -- (Optional) The number of results, per page to return.
		 * @param creator_appid -- App that created the files
		 * @param appid -- App that consumes the files
		 * @param requiredtags -- Tags to match on. See match_all_tags parameter below
		 * @param excludedtags -- (Optional) Tags that must NOT be present on a published file to satisfy the query.
		 * @param match_all_tags -- If true, then items must have all the tags specified, otherwise they must have at least one of the tags.
		 * @param required_flags -- Required flags that must be set on any returned items
		 * @param omitted_flags -- Flags that must not be set on any returned items
		 * @param search_text -- Text to match in the item's title or description
		 * @param filetype -- EPublishedFileInfoMatchingFileType
		 * @param child_publishedfileid -- Find all items that reference the given item.
		 * @param days -- If query_type is k_PublishedFileQueryType_RankedByTrend, then this is the number of days to get votes for [1,7].
		 * @param include_recent_votes_only -- If query_type is k_PublishedFileQueryType_RankedByTrend, then limit result set just to items that have votes within the day range given
		 * @param cache_max_age_seconds -- Allow stale data to be returned for the specified number of seconds.
		 * @param language -- Language to search in and also what gets returned. Defaults to English.
		 * @param required_kv_tags -- Required key-value tags to match on.
		 * @param taggroups -- (Optional) At least one of the tags must be present on a published file to satisfy the query.
		 * @param date_range_created -- (Optional) Filter to items created within this range.
		 * @param date_range_updated -- (Optional) Filter to items updated within this range.
		 * @param totalonly -- (Optional) If true, only return the total number of files that satisfy this query.
		 * @param ids_only -- (Optional) If true, only return the published file ids of files that satisfy this query.
		 * @param return_vote_data -- Return vote data
		 * @param return_tags -- Return tags in the file details
		 * @param return_kv_tags -- Return key-value tags in the file details
		 * @param return_previews -- Return preview image and video details in the file details
		 * @param return_children -- Return child item ids in the file details
		 * @param return_short_description -- Populate the short_description field instead of file_description
		 * @param return_for_sale_data -- Return pricing information, if applicable
		 * @param return_metadata -- Populate the metadata
		 * @param return_playtime_stats -- Return playtime stats for the specified number of days before today.
		 * @param return_details -- By default, if none of the other 'return_*' fields are set, only some voting details are returned. Set this to true to return the default set of details.
		 * @param strip_description_bbcode -- Strips BBCode from descriptions.
		 * @param desired_revision -- Return the data for the specified revision.
		 * @param return_reactions -- If true, then reactions to items will be returned.
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void QueryFiles(String key,int query_type,int page,String cursor,@Nullable int numperpage,int creator_appid,int appid,String requiredtags,String excludedtags,@Nullable boolean match_all_tags,String required_flags,String omitted_flags,String search_text,int filetype,long child_publishedfileid,int days,boolean include_recent_votes_only,@Nullable int cache_max_age_seconds,@Nullable int language,JsonObject required_kv_tags,JsonObject taggroups,JsonObject date_range_created,JsonObject date_range_updated,boolean totalonly,boolean ids_only,boolean return_vote_data,boolean return_tags,boolean return_kv_tags,boolean return_previews,boolean return_children,boolean return_short_description,boolean return_for_sale_data,@Nullable boolean return_metadata,int return_playtime_stats,boolean return_details,boolean strip_description_bbcode,@Nullable int desired_revision,@Nullable boolean return_reactions,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/IPublishedFileService/QueryFiles/v1";
			JsonObject param = new JsonObject();
			param.put("key", key);
			param.put("query_type", query_type);
			param.put("page", page);
			param.put("cursor", cursor);
			if(numperpage != 0)
				param.put("numperpage", numperpage);
			param.put("creator_appid", creator_appid);
			param.put("appid", appid);
			param.put("requiredtags", requiredtags);
			param.put("excludedtags", excludedtags);
			param.put("required_flags", required_flags);
			param.put("omitted_flags", omitted_flags);
			param.put("search_text", search_text);
			param.put("filetype", filetype);
			param.put("child_publishedfileid", child_publishedfileid);
			param.put("days", days);
			param.put("include_recent_votes_only", include_recent_votes_only);
			if(cache_max_age_seconds != 0)
				param.put("cache_max_age_seconds", cache_max_age_seconds);
			if(language != 0)
				param.put("language", language);
			param.put("required_kv_tags", required_kv_tags);
			param.put("taggroups", taggroups);
			param.put("date_range_created", date_range_created);
			param.put("date_range_updated", date_range_updated);
			param.put("totalonly", totalonly);
			param.put("ids_only", ids_only);
			param.put("return_vote_data", return_vote_data);
			param.put("return_tags", return_tags);
			param.put("return_kv_tags", return_kv_tags);
			param.put("return_previews", return_previews);
			param.put("return_children", return_children);
			param.put("return_short_description", return_short_description);
			param.put("return_for_sale_data", return_for_sale_data);
			param.put("return_playtime_stats", return_playtime_stats);
			param.put("return_details", return_details);
			param.put("strip_description_bbcode", strip_description_bbcode);
			if(desired_revision != 0)
				param.put("desired_revision", desired_revision);
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param key -- Access key
		 * @param publishedfileid
		 * @param vote_up
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void Vote(String key,long publishedfileid,boolean vote_up,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/IPublishedFileService/Vote/v1";
			JsonObject param = new JsonObject();
			param.put("key", key);
			param.put("publishedfileid", publishedfileid);
			param.put("vote_up", vote_up);
			VxHolder.request(HttpMethod.POST, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param key -- Access key
		 * @param publishedfileid
		 * @param appid
		 * @param banned
		 * @param reason
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void UpdateBanStatus(String key,long publishedfileid,int appid,boolean banned,String reason,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/IPublishedFileService/UpdateBanStatus/v1";
			JsonObject param = new JsonObject();
			param.put("key", key);
			param.put("publishedfileid", publishedfileid);
			param.put("appid", appid);
			param.put("banned", banned);
			param.put("reason", reason);
			VxHolder.request(HttpMethod.POST, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param key -- Access key
		 * @param publishedfileid
		 * @param appid
		 * @param incompatible
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void UpdateIncompatibleStatus(String key,long publishedfileid,int appid,boolean incompatible,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/IPublishedFileService/UpdateIncompatibleStatus/v1";
			JsonObject param = new JsonObject();
			param.put("key", key);
			param.put("publishedfileid", publishedfileid);
			param.put("appid", appid);
			param.put("incompatible", incompatible);
			VxHolder.request(HttpMethod.POST, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param key -- Access key
		 * @param publishedfileid
		 * @param tags_to_add
		 * @param tags_to_remove
		 * @param string_tags_to_remove_by_key
		 * @param int_tags_to_remove_by_key
		 * @param appid
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void UpdateKeyValueTags(String key,long publishedfileid,JsonObject tags_to_add,JsonObject tags_to_remove,String string_tags_to_remove_by_key,String int_tags_to_remove_by_key,int appid,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/IPublishedFileService/UpdateKeyValueTags/v1";
			JsonObject param = new JsonObject();
			param.put("key", key);
			param.put("publishedfileid", publishedfileid);
			param.put("tags_to_add", tags_to_add);
			param.put("tags_to_remove", tags_to_remove);
			param.put("string_tags_to_remove_by_key", string_tags_to_remove_by_key);
			param.put("int_tags_to_remove_by_key", int_tags_to_remove_by_key);
			param.put("appid", appid);
			VxHolder.request(HttpMethod.POST, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param key -- Access key
		 * @param publishedfileid
		 * @param for_table_of_contents
		 * @param specific_sectionid
		 * @param desired_revision -- Return the data for the specified revision.
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GetSubSectionData(String key,long publishedfileid,boolean for_table_of_contents,long specific_sectionid,@Nullable int desired_revision,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/IPublishedFileService/GetSubSectionData/v1";
			JsonObject param = new JsonObject();
			param.put("key", key);
			param.put("publishedfileid", publishedfileid);
			param.put("for_table_of_contents", for_table_of_contents);
			param.put("specific_sectionid", specific_sectionid);
			if(desired_revision != 0)
				param.put("desired_revision", desired_revision);
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param key -- Access key
		 * @param publishedfileids -- Set of published file Ids to retrieve details for.
		 * @param includetags -- If true, return tag information in the returned details.
		 * @param includeadditionalpreviews -- If true, return preview information in the returned details.
		 * @param includechildren -- If true, return children in the returned details.
		 * @param includekvtags -- If true, return key value tags in the returned details.
		 * @param includevotes -- If true, return vote data in the returned details.
		 * @param short_description -- If true, return a short description instead of the full description.
		 * @param includeforsaledata -- If true, return pricing data, if applicable.
		 * @param includemetadata -- If true, populate the metadata field.
		 * @param language -- Specifies the localized text to return. Defaults to English.
		 * @param return_playtime_stats -- Return playtime stats for the specified number of days before today.
		 * @param appid
		 * @param strip_description_bbcode -- Strips BBCode from descriptions.
		 * @param desired_revision -- Return the data for the specified revision.
		 * @param includereactions -- If true, then reactions to items will be returned.
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GetDetails(String key,long publishedfileids,boolean includetags,boolean includeadditionalpreviews,boolean includechildren,boolean includekvtags,boolean includevotes,boolean short_description,boolean includeforsaledata,boolean includemetadata,@Nullable int language,int return_playtime_stats,int appid,boolean strip_description_bbcode,@Nullable int desired_revision,@Nullable boolean includereactions,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/IPublishedFileService/GetDetails/v1";
			JsonObject param = new JsonObject();
			param.put("key", key);
			param.put("publishedfileids", publishedfileids);
			param.put("includetags", includetags);
			param.put("includeadditionalpreviews", includeadditionalpreviews);
			param.put("includechildren", includechildren);
			param.put("includekvtags", includekvtags);
			param.put("includevotes", includevotes);
			param.put("short_description", short_description);
			param.put("includeforsaledata", includeforsaledata);
			param.put("includemetadata", includemetadata);
			if(language != 0)
				param.put("language", language);
			param.put("return_playtime_stats", return_playtime_stats);
			param.put("appid", appid);
			param.put("strip_description_bbcode", strip_description_bbcode);
			if(desired_revision != 0)
				param.put("desired_revision", desired_revision);
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param key -- Access key
		 * @param steamid -- Steam ID of the user whose files are being requested.
		 * @param appid -- App Id of the app that the files were published to.
		 * @param page -- (Optional) Starting page for results.
		 * @param numperpage -- (Optional) The number of results, per page to return.
		 * @param type -- (Optional) Type of files to be returned.
		 * @param sortmethod -- (Optional) Sorting method to use on returned values.
		 * @param privacy -- (optional) Filter by privacy settings.
		 * @param requiredtags -- (Optional) Tags that must be present on a published file to satisfy the query.
		 * @param excludedtags -- (Optional) Tags that must NOT be present on a published file to satisfy the query.
		 * @param required_kv_tags -- Required key-value tags to match on.
		 * @param filetype -- (Optional) File type to match files to.
		 * @param creator_appid -- App Id of the app that published the files, only matched if specified.
		 * @param match_cloud_filename -- Match this cloud filename if specified.
		 * @param cache_max_age_seconds -- Allow stale data to be returned for the specified number of seconds.
		 * @param language -- Specifies the localized text to return. Defaults to English.
		 * @param taggroups -- (Optional) At least one of the tags must be present on a published file to satisfy the query.
		 * @param totalonly -- (Optional) If true, only return the total number of files that satisfy this query.
		 * @param ids_only -- (Optional) If true, only return the published file ids of files that satisfy this query.
		 * @param return_vote_data -- Return vote data
		 * @param return_tags -- Return tags in the file details
		 * @param return_kv_tags -- Return key-value tags in the file details
		 * @param return_previews -- Return preview image and video details in the file details
		 * @param return_children -- Return child item ids in the file details
		 * @param return_short_description -- Populate the short_description field instead of file_description
		 * @param return_for_sale_data -- Return pricing information, if applicable
		 * @param return_metadata -- Populate the metadata field
		 * @param return_playtime_stats -- Return playtime stats for the specified number of days before today.
		 * @param strip_description_bbcode -- Strips BBCode from descriptions.
		 * @param return_reactions -- If true, then reactions to items will be returned.
		 * @param startindex_override -- Backwards compatible for the client.
		 * @param desired_revision -- Return the data for the specified revision.
		 * @param return_apps -- Return list of apps the items belong to
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GetUserFiles(String key,long steamid,int appid,@Nullable int page,@Nullable int numperpage,@Nullable String type,@Nullable String sortmethod,int privacy,String requiredtags,String excludedtags,JsonObject required_kv_tags,int filetype,int creator_appid,String match_cloud_filename,@Nullable int cache_max_age_seconds,@Nullable int language,JsonObject taggroups,boolean totalonly,boolean ids_only,@Nullable boolean return_vote_data,boolean return_tags,@Nullable boolean return_kv_tags,boolean return_previews,boolean return_children,@Nullable boolean return_short_description,boolean return_for_sale_data,@Nullable boolean return_metadata,int return_playtime_stats,boolean strip_description_bbcode,@Nullable boolean return_reactions,int startindex_override,@Nullable int desired_revision,boolean return_apps,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/IPublishedFileService/GetUserFiles/v1";
			JsonObject param = new JsonObject();
			param.put("key", key);
			param.put("steamid", steamid);
			param.put("appid", appid);
			if(page != 0)
				param.put("page", page);
			if(numperpage != 0)
				param.put("numperpage", numperpage);
			if(type != null)
				param.put("type", type);
			if(sortmethod != null)
				param.put("sortmethod", sortmethod);
			param.put("privacy", privacy);
			param.put("requiredtags", requiredtags);
			param.put("excludedtags", excludedtags);
			param.put("required_kv_tags", required_kv_tags);
			param.put("filetype", filetype);
			param.put("creator_appid", creator_appid);
			param.put("match_cloud_filename", match_cloud_filename);
			if(cache_max_age_seconds != 0)
				param.put("cache_max_age_seconds", cache_max_age_seconds);
			if(language != 0)
				param.put("language", language);
			param.put("taggroups", taggroups);
			param.put("totalonly", totalonly);
			param.put("ids_only", ids_only);
			param.put("return_tags", return_tags);
			param.put("return_previews", return_previews);
			param.put("return_children", return_children);
			param.put("return_for_sale_data", return_for_sale_data);
			param.put("return_playtime_stats", return_playtime_stats);
			param.put("strip_description_bbcode", strip_description_bbcode);
			param.put("startindex_override", startindex_override);
			if(desired_revision != 0)
				param.put("desired_revision", desired_revision);
			param.put("return_apps", return_apps);
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param key -- Access key
		 * @param steamid -- Steam ID of the user whose files are being requested.
		 * @param appid -- App Id of the app that the files were published to.
		 * @param page -- (Optional) Starting page for results.
		 * @param numperpage -- (Optional) The number of results, per page to return.
		 * @param type -- (Optional) Type of files to be returned.
		 * @param sortmethod -- (Optional) Sorting method to use on returned values.
		 * @param privacy -- (optional) Filter by privacy settings.
		 * @param requiredtags -- (Optional) Tags that must be present on a published file to satisfy the query.
		 * @param excludedtags -- (Optional) Tags that must NOT be present on a published file to satisfy the query.
		 * @param required_kv_tags -- Required key-value tags to match on.
		 * @param filetype -- (Optional) File type to match files to.
		 * @param creator_appid -- App Id of the app that published the files, only matched if specified.
		 * @param match_cloud_filename -- Match this cloud filename if specified.
		 * @param cache_max_age_seconds -- Allow stale data to be returned for the specified number of seconds.
		 * @param language -- Specifies the localized text to return. Defaults to English.
		 * @param taggroups -- (Optional) At least one of the tags must be present on a published file to satisfy the query.
		 * @param totalonly -- (Optional) If true, only return the total number of files that satisfy this query.
		 * @param ids_only -- (Optional) If true, only return the published file ids of files that satisfy this query.
		 * @param return_vote_data -- Return vote data
		 * @param return_tags -- Return tags in the file details
		 * @param return_kv_tags -- Return key-value tags in the file details
		 * @param return_previews -- Return preview image and video details in the file details
		 * @param return_children -- Return child item ids in the file details
		 * @param return_short_description -- Populate the short_description field instead of file_description
		 * @param return_for_sale_data -- Return pricing information, if applicable
		 * @param return_metadata -- Populate the metadata field
		 * @param return_playtime_stats -- Return playtime stats for the specified number of days before today.
		 * @param strip_description_bbcode -- Strips BBCode from descriptions.
		 * @param return_reactions -- If true, then reactions to items will be returned.
		 * @param startindex_override -- Backwards compatible for the client.
		 * @param desired_revision -- Return the data for the specified revision.
		 * @param return_apps -- Return list of apps the items belong to
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GetUserFileCount(String key,long steamid,int appid,@Nullable int page,@Nullable int numperpage,@Nullable String type,@Nullable String sortmethod,int privacy,String requiredtags,String excludedtags,JsonObject required_kv_tags,int filetype,int creator_appid,String match_cloud_filename,@Nullable int cache_max_age_seconds,@Nullable int language,JsonObject taggroups,boolean totalonly,boolean ids_only,@Nullable boolean return_vote_data,boolean return_tags,@Nullable boolean return_kv_tags,boolean return_previews,boolean return_children,@Nullable boolean return_short_description,boolean return_for_sale_data,@Nullable boolean return_metadata,int return_playtime_stats,boolean strip_description_bbcode,@Nullable boolean return_reactions,int startindex_override,@Nullable int desired_revision,boolean return_apps,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/IPublishedFileService/GetUserFileCount/v1";
			JsonObject param = new JsonObject();
			param.put("key", key);
			param.put("steamid", steamid);
			param.put("appid", appid);
			if(page != 0)
				param.put("page", page);
			if(numperpage != 0)
				param.put("numperpage", numperpage);
			if(type != null)
				param.put("type", type);
			if(sortmethod != null)
				param.put("sortmethod", sortmethod);
			param.put("privacy", privacy);
			param.put("requiredtags", requiredtags);
			param.put("excludedtags", excludedtags);
			param.put("required_kv_tags", required_kv_tags);
			param.put("filetype", filetype);
			param.put("creator_appid", creator_appid);
			param.put("match_cloud_filename", match_cloud_filename);
			if(cache_max_age_seconds != 0)
				param.put("cache_max_age_seconds", cache_max_age_seconds);
			if(language != 0)
				param.put("language", language);
			param.put("taggroups", taggroups);
			param.put("totalonly", totalonly);
			param.put("ids_only", ids_only);
			param.put("return_tags", return_tags);
			param.put("return_previews", return_previews);
			param.put("return_children", return_children);
			param.put("return_for_sale_data", return_for_sale_data);
			param.put("return_playtime_stats", return_playtime_stats);
			param.put("strip_description_bbcode", strip_description_bbcode);
			param.put("startindex_override", startindex_override);
			if(desired_revision != 0)
				param.put("desired_revision", desired_revision);
			param.put("return_apps", return_apps);
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param key -- Access key
		 * @param publishedfileid
		 * @param appid
		 * @param metadata
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void SetDeveloperMetadata(String key,long publishedfileid,int appid,String metadata,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/IPublishedFileService/SetDeveloperMetadata/v1";
			JsonObject param = new JsonObject();
			param.put("key", key);
			param.put("publishedfileid", publishedfileid);
			param.put("appid", appid);
			param.put("metadata", metadata);
			VxHolder.request(HttpMethod.POST, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param key -- Access key
		 * @param publishedfileid
		 * @param appid
		 * @param add_tags
		 * @param remove_tags
		 * @param language
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void UpdateTags(String key,long publishedfileid,int appid,String add_tags,String remove_tags,int language,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/IPublishedFileService/UpdateTags/v1";
			JsonObject param = new JsonObject();
			param.put("key", key);
			param.put("publishedfileid", publishedfileid);
			param.put("appid", appid);
			param.put("add_tags", add_tags);
			param.put("remove_tags", remove_tags);
			param.put("language", language);
			VxHolder.request(HttpMethod.POST, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param key -- Access key
		 * @param steamid
		 * @param appid
		 * @param expiration_time
		 * @param reason
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void UpdateAppUGCBan(String key,long steamid,int appid,int expiration_time,String reason,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/IPublishedFileService/UpdateAppUGCBan/v1";
			JsonObject param = new JsonObject();
			param.put("key", key);
			param.put("steamid", steamid);
			param.put("appid", appid);
			param.put("expiration_time", expiration_time);
			param.put("reason", reason);
			VxHolder.request(HttpMethod.POST, requestURI, param, successHandler, failedHandler);
		}
	}
    public static class IEconService {

		/** 
		 * @param key -- Access key
		 * @param max_trades -- The number of trades to return information for
		 * @param start_after_time -- The time of the last trade shown on the previous page of results, or the time of the first trade if navigating back
		 * @param start_after_tradeid -- The tradeid shown on the previous page of results, or the ID of the first trade if navigating back
		 * @param navigating_back -- The user wants the previous page of results, so return the previous max_trades trades before the start time and ID
		 * @param get_descriptions -- If set, the item display data for the items included in the returned trades will also be returned
		 * @param language -- The language to use when loading item display data
		 * @param include_failed
		 * @param include_total -- If set, the total number of trades the account has participated in will be included in the response
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GetTradeHistory(String key,int max_trades,int start_after_time,long start_after_tradeid,boolean navigating_back,boolean get_descriptions,String language,boolean include_failed,boolean include_total,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/IEconService/GetTradeHistory/v1";
			JsonObject param = new JsonObject();
			param.put("key", key);
			param.put("max_trades", max_trades);
			param.put("start_after_time", start_after_time);
			param.put("start_after_tradeid", start_after_tradeid);
			param.put("navigating_back", navigating_back);
			param.put("get_descriptions", get_descriptions);
			param.put("language", language);
			param.put("include_failed", include_failed);
			param.put("include_total", include_total);
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param key -- Access key
		 * @param tradeid
		 * @param get_descriptions -- If set, the item display data for the items included in the returned trades will also be returned
		 * @param language -- The language to use when loading item display data
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GetTradeStatus(String key,long tradeid,boolean get_descriptions,String language,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/IEconService/GetTradeStatus/v1";
			JsonObject param = new JsonObject();
			param.put("key", key);
			param.put("tradeid", tradeid);
			param.put("get_descriptions", get_descriptions);
			param.put("language", language);
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param key -- Access key
		 * @param steamid -- User to clear cache for.
		 * @param appid -- App to clear cache for.
		 * @param contextid -- Context to clear cache for.
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void FlushInventoryCache(String key,long steamid,int appid,long contextid,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/IEconService/FlushInventoryCache/v1";
			JsonObject param = new JsonObject();
			param.put("key", key);
			param.put("steamid", steamid);
			param.put("appid", appid);
			param.put("contextid", contextid);
			VxHolder.request(HttpMethod.POST, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param key -- Access key
		 * @param appid
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void FlushAssetAppearanceCache(String key,int appid,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/IEconService/FlushAssetAppearanceCache/v1";
			JsonObject param = new JsonObject();
			param.put("key", key);
			param.put("appid", appid);
			VxHolder.request(HttpMethod.POST, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param key -- Access key
		 * @param appid
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void FlushContextCache(String key,int appid,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/IEconService/FlushContextCache/v1";
			JsonObject param = new JsonObject();
			param.put("key", key);
			param.put("appid", appid);
			VxHolder.request(HttpMethod.POST, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param key -- Access key
		 * @param get_sent_offers -- Request the list of sent offers.
		 * @param get_received_offers -- Request the list of received offers.
		 * @param get_descriptions -- If set, the item display data for the items included in the returned trade offers will also be returned. If one or more descriptions can't be retrieved, then your request will fail.
		 * @param language -- The language to use when loading item display data.
		 * @param active_only -- Indicates we should only return offers which are still active, or offers that have changed in state since the time_historical_cutoff
		 * @param historical_only -- Indicates we should only return offers which are not active.
		 * @param time_historical_cutoff -- When active_only is set, offers updated since this time will also be returned
		 * @param cursor -- Cursor aka start index
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GetTradeOffers(String key,boolean get_sent_offers,boolean get_received_offers,boolean get_descriptions,String language,boolean active_only,boolean historical_only,int time_historical_cutoff,@Nullable int cursor,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/IEconService/GetTradeOffers/v1";
			JsonObject param = new JsonObject();
			param.put("key", key);
			param.put("get_sent_offers", get_sent_offers);
			param.put("get_received_offers", get_received_offers);
			param.put("get_descriptions", get_descriptions);
			param.put("language", language);
			param.put("active_only", active_only);
			param.put("historical_only", historical_only);
			param.put("time_historical_cutoff", time_historical_cutoff);
			if(cursor != 0)
				param.put("cursor", cursor);
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param key -- Access key
		 * @param tradeofferid
		 * @param language
		 * @param get_descriptions -- If set, the item display data for the items included in the returned trade offers will also be returned. If one or more descriptions can't be retrieved, then your request will fail.
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GetTradeOffer(String key,long tradeofferid,String language,boolean get_descriptions,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/IEconService/GetTradeOffer/v1";
			JsonObject param = new JsonObject();
			param.put("key", key);
			param.put("tradeofferid", tradeofferid);
			param.put("language", language);
			param.put("get_descriptions", get_descriptions);
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param key -- Access key
		 * @param time_last_visit -- The time the user last visited.  If not passed, will use the time the user last visited the trade offer page.
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GetTradeOffersSummary(String key,int time_last_visit,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/IEconService/GetTradeOffersSummary/v1";
			JsonObject param = new JsonObject();
			param.put("key", key);
			param.put("time_last_visit", time_last_visit);
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param key -- Access key
		 * @param steamid_target -- User you are trading with
		 * @param trade_offer_access_token -- A special token that allows for trade offers from non-friends.
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GetTradeHoldDurations(String key,long steamid_target,String trade_offer_access_token,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/IEconService/GetTradeHoldDurations/v1";
			JsonObject param = new JsonObject();
			param.put("key", key);
			param.put("steamid_target", steamid_target);
			param.put("trade_offer_access_token", trade_offer_access_token);
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}
	}
    public static class IGameNotificationsService {

		/** 
		 * @param key -- Access key
		 * @param appid -- The appid to create the session for.
		 * @param context -- Game-specified context value the game can used to associate the session with some object on their backend.
		 * @param title -- The title of the session to be displayed within each user's list of sessions.
		 * @param users -- The initial state of all users in the session.
		 * @param steamid -- (Optional) steamid to make the request on behalf of -- if specified, the user must be in the session and all users being added to the session must be friends with the user.
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void CreateSession(String key,int appid,long context,JsonObject title,JsonObject users,long steamid,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/IGameNotificationsService/CreateSession/v1";
			JsonObject param = new JsonObject();
			param.put("key", key);
			param.put("appid", appid);
			param.put("context", context);
			param.put("title", title);
			param.put("users", users);
			param.put("steamid", steamid);
			VxHolder.request(HttpMethod.POST, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param appid -- The appid to create the session for.
		 * @param context -- Game-specified context value the game can used to associate the session with some object on their backend.
		 * @param title -- The title of the session to be displayed within each user's list of sessions.
		 * @param users -- The initial state of all users in the session.
		 * @param steamid -- (Optional) steamid to make the request on behalf of -- if specified, the user must be in the session and all users being added to the session must be friends with the user.
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void UserCreateSession(int appid,long context,JsonObject title,JsonObject users,long steamid,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/IGameNotificationsService/UserCreateSession/v1";
			JsonObject param = new JsonObject();
			param.put("appid", appid);
			param.put("context", context);
			param.put("title", title);
			param.put("users", users);
			param.put("steamid", steamid);
			VxHolder.request(HttpMethod.POST, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param key -- Access key
		 * @param sessionid -- The sessionid to update.
		 * @param appid -- The appid of the session to update.
		 * @param title -- (Optional) The new title of the session.  If not specified, the title will not be changed.
		 * @param users -- (Optional) A list of users whose state will be updated to reflect the given state. If the users are not already in the session, they will be added to it.
		 * @param steamid -- (Optional) steamid to make the request on behalf of -- if specified, the user must be in the session and all users being added to the session must be friends with the user.
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void UpdateSession(String key,long sessionid,int appid,JsonObject title,JsonObject users,long steamid,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/IGameNotificationsService/UpdateSession/v1";
			JsonObject param = new JsonObject();
			param.put("key", key);
			param.put("sessionid", sessionid);
			param.put("appid", appid);
			param.put("title", title);
			param.put("users", users);
			param.put("steamid", steamid);
			VxHolder.request(HttpMethod.POST, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param sessionid -- The sessionid to update.
		 * @param appid -- The appid of the session to update.
		 * @param title -- (Optional) The new title of the session.  If not specified, the title will not be changed.
		 * @param users -- (Optional) A list of users whose state will be updated to reflect the given state. If the users are not already in the session, they will be added to it.
		 * @param steamid -- (Optional) steamid to make the request on behalf of -- if specified, the user must be in the session and all users being added to the session must be friends with the user.
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void UserUpdateSession(long sessionid,int appid,JsonObject title,JsonObject users,long steamid,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/IGameNotificationsService/UserUpdateSession/v1";
			JsonObject param = new JsonObject();
			param.put("sessionid", sessionid);
			param.put("appid", appid);
			param.put("title", title);
			param.put("users", users);
			param.put("steamid", steamid);
			VxHolder.request(HttpMethod.POST, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param key -- Access key
		 * @param appid -- The sessionid to request details for. Optional. If not specified, all the user's sessions will be returned.
		 * @param steamid -- The user whose sessions are to be enumerated.
		 * @param include_all_user_messages -- (Optional) Boolean determining whether the message for all users should be included. Defaults to false.
		 * @param include_auth_user_message -- (Optional) Boolean determining whether the message for the authenticated user should be included. Defaults to false.
		 * @param language -- (Optional) Language to localize the text in.
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void EnumerateSessionsForApp(String key,int appid,long steamid,boolean include_all_user_messages,boolean include_auth_user_message,String language,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/IGameNotificationsService/EnumerateSessionsForApp/v1";
			JsonObject param = new JsonObject();
			param.put("key", key);
			param.put("appid", appid);
			param.put("steamid", steamid);
			param.put("include_all_user_messages", include_all_user_messages);
			param.put("include_auth_user_message", include_auth_user_message);
			param.put("language", language);
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param key -- Access key
		 * @param sessions
		 * @param appid -- The appid for the sessions.
		 * @param language -- Language to localize the text in.
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GetSessionDetailsForApp(String key,JsonObject sessions,int appid,String language,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/IGameNotificationsService/GetSessionDetailsForApp/v1";
			JsonObject param = new JsonObject();
			param.put("key", key);
			param.put("sessions", sessions);
			param.put("appid", appid);
			param.put("language", language);
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param key -- Access key
		 * @param steamid -- The steamid to request notifications for.
		 * @param appid -- The appid to request notifications for.
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void RequestNotifications(String key,long steamid,int appid,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/IGameNotificationsService/RequestNotifications/v1";
			JsonObject param = new JsonObject();
			param.put("key", key);
			param.put("steamid", steamid);
			param.put("appid", appid);
			VxHolder.request(HttpMethod.POST, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param key -- Access key
		 * @param sessionid -- The sessionid to delete.
		 * @param appid -- The appid of the session to delete.
		 * @param steamid -- (Optional) steamid to make the request on behalf of -- if specified, the user must be in the session.
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void DeleteSession(String key,long sessionid,int appid,long steamid,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/IGameNotificationsService/DeleteSession/v1";
			JsonObject param = new JsonObject();
			param.put("key", key);
			param.put("sessionid", sessionid);
			param.put("appid", appid);
			param.put("steamid", steamid);
			VxHolder.request(HttpMethod.POST, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param sessionid -- The sessionid to delete.
		 * @param appid -- The appid of the session to delete.
		 * @param steamid -- (Optional) steamid to make the request on behalf of -- if specified, the user must be in the session.
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void UserDeleteSession(long sessionid,int appid,long steamid,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/IGameNotificationsService/UserDeleteSession/v1";
			JsonObject param = new JsonObject();
			param.put("sessionid", sessionid);
			param.put("appid", appid);
			param.put("steamid", steamid);
			VxHolder.request(HttpMethod.POST, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param key -- Access key
		 * @param sessionid -- The sessionid to delete.
		 * @param appid -- The appid of the session to delete.
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void DeleteSessionBatch(String key,long sessionid,int appid,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/IGameNotificationsService/DeleteSessionBatch/v1";
			JsonObject param = new JsonObject();
			param.put("key", key);
			param.put("sessionid", sessionid);
			param.put("appid", appid);
			VxHolder.request(HttpMethod.POST, requestURI, param, successHandler, failedHandler);
		}
	}
    public static class IInventoryService {

		/** 
		 * @param key -- Access key
		 * @param appid
		 * @param itemdefid
		 * @param itempropsjson
		 * @param steamid
		 * @param notify -- Should notify the user that the item was added to their Steam Inventory.
		 * @param requestid
		 * @param trade_restriction -- If true, apply the default trade and market restriction times to this item.
		 * @param is_purchase -- If set, treat requestid as a txnid and create this item as a result of user microtransaction purchase.
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void AddItem(String key,int appid,long itemdefid,String itempropsjson,long steamid,boolean notify,long requestid,boolean trade_restriction,@Nullable boolean is_purchase,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/IInventoryService/AddItem/v1";
			JsonObject param = new JsonObject();
			param.put("key", key);
			param.put("appid", appid);
			param.put("itemdefid", itemdefid);
			param.put("itempropsjson", itempropsjson);
			param.put("steamid", steamid);
			param.put("notify", notify);
			param.put("requestid", requestid);
			param.put("trade_restriction", trade_restriction);
			VxHolder.request(HttpMethod.POST, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param key -- Access key
		 * @param appid
		 * @param itemdefid
		 * @param itempropsjson
		 * @param steamid
		 * @param notify -- Should notify the user that the item was added to their Steam Inventory.
		 * @param requestid
		 * @param trade_restriction -- If true, apply the default trade and market restriction times to this item.
		 * @param is_purchase -- If set, treat requestid as a txnid and create this item as a result of user microtransaction purchase.
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void AddPromoItem(String key,int appid,long itemdefid,String itempropsjson,long steamid,boolean notify,long requestid,boolean trade_restriction,@Nullable boolean is_purchase,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/IInventoryService/AddPromoItem/v1";
			JsonObject param = new JsonObject();
			param.put("key", key);
			param.put("appid", appid);
			param.put("itemdefid", itemdefid);
			param.put("itempropsjson", itempropsjson);
			param.put("steamid", steamid);
			param.put("notify", notify);
			param.put("requestid", requestid);
			param.put("trade_restriction", trade_restriction);
			VxHolder.request(HttpMethod.POST, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param key -- Access key
		 * @param appid
		 * @param steamid
		 * @param materialsitemid
		 * @param materialsquantity
		 * @param outputitemdefid
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void ExchangeItem(String key,int appid,long steamid,long materialsitemid,int materialsquantity,long outputitemdefid,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/IInventoryService/ExchangeItem/v1";
			JsonObject param = new JsonObject();
			param.put("key", key);
			param.put("appid", appid);
			param.put("steamid", steamid);
			param.put("materialsitemid", materialsitemid);
			param.put("materialsquantity", materialsquantity);
			param.put("outputitemdefid", outputitemdefid);
			VxHolder.request(HttpMethod.POST, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param key -- Access key
		 * @param appid
		 * @param steamid
		 * @param updates
		 * @param timestamp
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void ModifyItems(String key,int appid,long steamid,JsonObject updates,int timestamp,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/IInventoryService/ModifyItems/v1";
			JsonObject param = new JsonObject();
			param.put("key", key);
			param.put("appid", appid);
			param.put("steamid", steamid);
			param.put("updates", updates);
			param.put("timestamp", timestamp);
			VxHolder.request(HttpMethod.POST, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param key -- Access key
		 * @param appid
		 * @param steamid
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GetInventory(String key,int appid,long steamid,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/IInventoryService/GetInventory/v1";
			JsonObject param = new JsonObject();
			param.put("key", key);
			param.put("appid", appid);
			param.put("steamid", steamid);
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param key -- Access key
		 * @param appid
		 * @param modifiedsince
		 * @param itemdefids
		 * @param workshopids
		 * @param cache_max_age_seconds -- Allow stale data to be returned for the specified number of seconds.
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GetItemDefs(String key,int appid,String modifiedsince,long itemdefids,long workshopids,@Nullable int cache_max_age_seconds,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/IInventoryService/GetItemDefs/v1";
			JsonObject param = new JsonObject();
			param.put("key", key);
			param.put("appid", appid);
			param.put("modifiedsince", modifiedsince);
			param.put("itemdefids", itemdefids);
			param.put("workshopids", workshopids);
			if(cache_max_age_seconds != 0)
				param.put("cache_max_age_seconds", cache_max_age_seconds);
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param key -- Access key
		 * @param appid
		 * @param itemid
		 * @param quantity
		 * @param steamid
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void SplitItemStack(String key,int appid,long itemid,int quantity,long steamid,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/IInventoryService/SplitItemStack/v1";
			JsonObject param = new JsonObject();
			param.put("key", key);
			param.put("appid", appid);
			param.put("itemid", itemid);
			param.put("quantity", quantity);
			param.put("steamid", steamid);
			VxHolder.request(HttpMethod.POST, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param key -- Access key
		 * @param appid
		 * @param fromitemid
		 * @param destitemid
		 * @param quantity
		 * @param steamid
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void CombineItemStacks(String key,int appid,long fromitemid,long destitemid,int quantity,long steamid,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/IInventoryService/CombineItemStacks/v1";
			JsonObject param = new JsonObject();
			param.put("key", key);
			param.put("appid", appid);
			param.put("fromitemid", fromitemid);
			param.put("destitemid", destitemid);
			param.put("quantity", quantity);
			param.put("steamid", steamid);
			VxHolder.request(HttpMethod.POST, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param key -- Access key
		 * @param ecurrency
		 * @param currency_code -- Standard short code of the requested currency (preferred)
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GetPriceSheet(String key,int ecurrency,String currency_code,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/IInventoryService/GetPriceSheet/v1";
			JsonObject param = new JsonObject();
			param.put("key", key);
			param.put("ecurrency", ecurrency);
			param.put("currency_code", currency_code);
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param key -- Access key
		 * @param appid
		 * @param steamid
		 * @param itemdefid
		 * @param force
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void Consolidate(String key,int appid,long steamid,long itemdefid,@Nullable boolean force,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/IInventoryService/Consolidate/v1";
			JsonObject param = new JsonObject();
			param.put("key", key);
			param.put("appid", appid);
			param.put("steamid", steamid);
			param.put("itemdefid", itemdefid);
			VxHolder.request(HttpMethod.POST, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param key -- Access key
		 * @param appid
		 * @param steamid
		 * @param force
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void ConsolidateAll(String key,int appid,long steamid,boolean force,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/IInventoryService/ConsolidateAll/v1";
			JsonObject param = new JsonObject();
			param.put("key", key);
			param.put("appid", appid);
			param.put("steamid", steamid);
			param.put("force", force);
			VxHolder.request(HttpMethod.POST, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param key -- Access key
		 * @param appid
		 * @param steamid
		 * @param itemdefid
		 * @param force
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GetQuantity(String key,int appid,long steamid,long itemdefid,@Nullable boolean force,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/IInventoryService/GetQuantity/v1";
			JsonObject param = new JsonObject();
			param.put("key", key);
			param.put("appid", appid);
			param.put("steamid", steamid);
			param.put("itemdefid", itemdefid);
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param key -- Access key
		 * @param appid
		 * @param max_results
		 * @param start_highwater
		 * @param start_timestamp
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GetAddItemHistory(String key,int appid,@Nullable int max_results,long start_highwater,int start_timestamp,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/IInventoryService/GetAddItemHistory/v1";
			JsonObject param = new JsonObject();
			param.put("key", key);
			param.put("appid", appid);
			if(max_results != 0)
				param.put("max_results", max_results);
			param.put("start_highwater", start_highwater);
			param.put("start_timestamp", start_timestamp);
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}
	}
    public static class IEconMarketService {

		/** 
		 * @param key -- Access key
		 * @param steamid -- The SteamID of the user to check
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GetMarketEligibility(String key,long steamid,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/IEconMarketService/GetMarketEligibility/v1";
			JsonObject param = new JsonObject();
			param.put("key", key);
			param.put("steamid", steamid);
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param key -- Access key
		 * @param appid -- The app making the request
		 * @param steamid -- The SteamID of the user whose listings should be canceled
		 * @param synchronous -- Whether or not to wait until all listings have been canceled before returning the response
		 * @param vac -- This was in response to a VAC ban?
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void CancelAppListingsForUser(String key,int appid,long steamid,boolean synchronous,boolean vac,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/IEconMarketService/CancelAppListingsForUser/v1";
			JsonObject param = new JsonObject();
			param.put("key", key);
			param.put("appid", appid);
			param.put("steamid", steamid);
			param.put("synchronous", synchronous);
			param.put("vac", vac);
			VxHolder.request(HttpMethod.POST, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param key -- Access key
		 * @param appid -- The app that's asking. Must match the app of the listing and must belong to the publisher group that owns the API key making the request
		 * @param listingid -- The identifier of the listing to get information for
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GetAssetID(String key,int appid,long listingid,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/IEconMarketService/GetAssetID/v1";
			JsonObject param = new JsonObject();
			param.put("key", key);
			param.put("appid", appid);
			param.put("listingid", listingid);
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param key -- Access key
		 * @param appid -- The app the item belongs to
		 * @param class_name -- Asset class property names
		 * @param class_value -- Asset class property value
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void LearnItem(String key,int appid,String class_name,String class_value,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/IEconMarketService/LearnItem/v1";
			JsonObject param = new JsonObject();
			param.put("key", key);
			param.put("appid", appid);
			param.put("class_name", class_name);
			param.put("class_value", class_value);
			VxHolder.request(HttpMethod.POST, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param key -- Access key
		 * @param language -- The language to use in item descriptions
		 * @param rows -- Number of rows per page
		 * @param start -- The result number to start at
		 * @param filter_appid -- If present, the app ID to limit results to
		 * @param ecurrency -- If present, prices returned will be represented in this currency
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GetPopular(String key,String language,@Nullable int rows,int start,int filter_appid,int ecurrency,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/IEconMarketService/GetPopular/v1";
			JsonObject param = new JsonObject();
			param.put("key", key);
			param.put("language", language);
			if(rows != 0)
				param.put("rows", rows);
			param.put("start", start);
			param.put("filter_appid", filter_appid);
			param.put("ecurrency", ecurrency);
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}
	}
    public static class ILobbyMatchmakingService {

		/** 
		 * @param key -- Access key
		 * @param appid
		 * @param max_members
		 * @param lobby_type
		 * @param lobby_name
		 * @param steamid_invited_members
		 * @param lobby_metadata
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void CreateLobby(String key,int appid,int max_members,int lobby_type,String lobby_name,long steamid_invited_members,JsonObject lobby_metadata,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/ILobbyMatchmakingService/CreateLobby/v1";
			JsonObject param = new JsonObject();
			param.put("key", key);
			param.put("appid", appid);
			param.put("max_members", max_members);
			param.put("lobby_type", lobby_type);
			param.put("lobby_name", lobby_name);
			param.put("steamid_invited_members", steamid_invited_members);
			param.put("lobby_metadata", lobby_metadata);
			VxHolder.request(HttpMethod.POST, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param key -- Access key
		 * @param appid
		 * @param steamid_lobby
		 * @param steamid_to_remove
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void RemoveUserFromLobby(String key,int appid,long steamid_lobby,long steamid_to_remove,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/ILobbyMatchmakingService/RemoveUserFromLobby/v1";
			JsonObject param = new JsonObject();
			param.put("key", key);
			param.put("appid", appid);
			param.put("steamid_lobby", steamid_lobby);
			param.put("steamid_to_remove", steamid_to_remove);
			VxHolder.request(HttpMethod.POST, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param key -- Access key
		 * @param appid
		 * @param steamid_lobby
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GetLobbyData(String key,int appid,long steamid_lobby,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/ILobbyMatchmakingService/GetLobbyData/v1";
			JsonObject param = new JsonObject();
			param.put("key", key);
			param.put("appid", appid);
			param.put("steamid_lobby", steamid_lobby);
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}
	}
    public static class IProductInfoService {

		/** 
		 * @param key -- Access key
		 * @param appid
		 * @param languages
		 * @param steamid
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void SetRichPresenceLocalization(String key,int appid,JsonObject languages,long steamid,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/IProductInfoService/SetRichPresenceLocalization/v1";
			JsonObject param = new JsonObject();
			param.put("key", key);
			param.put("appid", appid);
			param.put("languages", languages);
			param.put("steamid", steamid);
			VxHolder.request(HttpMethod.POST, requestURI, param, successHandler, failedHandler);
		}
	}
    public static class IQuestService {

		/** 
		 * @param key -- Access key
		 * @param steamid
		 * @param appid
		 * @param match_item_type
		 * @param match_item_class
		 * @param prefix_item_name
		 * @param attributes
		 * @param note
		 * @param broadcast_channel_id
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void PublisherAddCommunityItemsToPlayer(String key,long steamid,int appid,int match_item_type,int match_item_class,String prefix_item_name,JsonObject attributes,String note,long broadcast_channel_id,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/IQuestService/PublisherAddCommunityItemsToPlayer/v1";
			JsonObject param = new JsonObject();
			param.put("key", key);
			param.put("steamid", steamid);
			param.put("appid", appid);
			param.put("match_item_type", match_item_type);
			param.put("match_item_class", match_item_class);
			param.put("prefix_item_name", prefix_item_name);
			param.put("attributes", attributes);
			param.put("note", note);
			param.put("broadcast_channel_id", broadcast_channel_id);
			VxHolder.request(HttpMethod.POST, requestURI, param, successHandler, failedHandler);
		}
	}
    public static class IStoreService {

		/** 
		 * @param key -- Access key
		 * @param if_modified_since -- Return only items that have been modified since this date.
		 * @param have_description_language -- Return only items that have a description in this language.
		 * @param include_games -- Include games (defaults to enabled)
		 * @param include_dlc -- Include DLC
		 * @param include_software -- Include software items
		 * @param include_videos -- Include videos and series
		 * @param include_hardware -- Include hardware
		 * @param last_appid -- For continuations, this is the last appid returned from the previous call.
		 * @param max_results -- Number of results to return at a time.  Default 10k, max 50k.
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GetAppList(String key,@Nullable int if_modified_since,@Nullable String have_description_language,@Nullable boolean include_games,@Nullable boolean include_dlc,@Nullable boolean include_software,@Nullable boolean include_videos,@Nullable boolean include_hardware,@Nullable int last_appid,@Nullable int max_results,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/IStoreService/GetAppList/v1";
			JsonObject param = new JsonObject();
			param.put("key", key);
			if(if_modified_since != 0)
				param.put("if_modified_since", if_modified_since);
			if(have_description_language != null)
				param.put("have_description_language", have_description_language);
			if(last_appid != 0)
				param.put("last_appid", last_appid);
			if(max_results != 0)
				param.put("max_results", max_results);
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}
	}
    public static class IHelpRequestLogsService {

		/** 
		 * @param appid
		 * @param log_type
		 * @param version_string
		 * @param log_contents
		 * @param request_id
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void UploadUserApplicationLog(int appid,String log_type,String version_string,String log_contents,long request_id,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/IHelpRequestLogsService/UploadUserApplicationLog/v1";
			JsonObject param = new JsonObject();
			param.put("appid", appid);
			param.put("log_type", log_type);
			param.put("version_string", version_string);
			param.put("log_contents", log_contents);
			param.put("request_id", request_id);
			VxHolder.request(HttpMethod.POST, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param appid
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GetApplicationLogDemand(int appid,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/IHelpRequestLogsService/GetApplicationLogDemand/v1";
			JsonObject param = new JsonObject();
			param.put("appid", appid);
			VxHolder.request(HttpMethod.POST, requestURI, param, successHandler, failedHandler);
		}
	}
    public static class ITestExternalPrivilegeService {

		/** 
		 * @param key -- Access key
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void CallPublisherKey(String key,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/ITestExternalPrivilegeService/CallPublisherKey/v1";
			JsonObject param = new JsonObject();
			param.put("key", key);
			VxHolder.request(HttpMethod.POST, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param key -- Access key
		 * @param appid
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void CallPublisherKeyOwnsApp(String key,int appid,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/ITestExternalPrivilegeService/CallPublisherKeyOwnsApp/v1";
			JsonObject param = new JsonObject();
			param.put("key", key);
			param.put("appid", appid);
			VxHolder.request(HttpMethod.POST, requestURI, param, successHandler, failedHandler);
		}
	}
    public static class ICheatReportingService {

		/** 
		 * @param key -- Access key
		 * @param steamid -- steamid of the user who is reported as cheating.
		 * @param appid -- The appid.
		 * @param steamidreporter -- (Optional) steamid of the user or game server who is reporting the cheating.
		 * @param appdata -- (Optional) App specific data about the cheating.
		 * @param heuristic -- (Optional) extra information about the source of the cheating - was it a heuristic.
		 * @param detection -- (Optional) extra information about the source of the cheating - was it a detection.
		 * @param playerreport -- (Optional) extra information about the source of the cheating - was it a player report.
		 * @param noreportid -- (Optional) dont return report id
		 * @param gamemode -- (Optional) extra information about state of game - was it a specific type of game play (0 = generic)
		 * @param suspicionstarttime -- (Optional) extra information indicating how far back the game thinks is interesting for this user
		 * @param severity -- (Optional) level of severity of bad action being reported
		 * @param matchid -- (Optional) matchid to identify the game instance
		 * @param cheating_type -- (Optional) app specific data about the type of cheating
		 * @param appdata2 -- (Optional) App specific data about the cheating.
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void ReportPlayerCheating(String key,long steamid,int appid,long steamidreporter,long appdata,boolean heuristic,boolean detection,boolean playerreport,boolean noreportid,int gamemode,int suspicionstarttime,int severity,long matchid,long cheating_type,long appdata2,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/ICheatReportingService/ReportPlayerCheating/v1";
			JsonObject param = new JsonObject();
			param.put("key", key);
			param.put("steamid", steamid);
			param.put("appid", appid);
			param.put("steamidreporter", steamidreporter);
			param.put("appdata", appdata);
			param.put("heuristic", heuristic);
			param.put("detection", detection);
			param.put("playerreport", playerreport);
			param.put("noreportid", noreportid);
			param.put("gamemode", gamemode);
			param.put("suspicionstarttime", suspicionstarttime);
			param.put("severity", severity);
			param.put("matchid", matchid);
			param.put("cheating_type", cheating_type);
			param.put("appdata2", appdata2);
			VxHolder.request(HttpMethod.POST, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param key -- Access key
		 * @param steamid -- steamid of the user who is reported as cheating.
		 * @param appid -- The appid.
		 * @param reportid -- The reportid originally used to report cheating.
		 * @param cheatdescription -- Text describing cheating infraction.
		 * @param duration -- Ban duration requested in seconds.
		 * @param delayban -- Delay the ban according to default ban delay rules.
		 * @param flags -- Additional information about the ban request.
		 * @param invisible_ban -- The ban will be recorded but not be visible or deny access to secure servers.
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void RequestPlayerGameBan(String key,long steamid,int appid,long reportid,String cheatdescription,int duration,boolean delayban,int flags,boolean invisible_ban,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/ICheatReportingService/RequestPlayerGameBan/v1";
			JsonObject param = new JsonObject();
			param.put("key", key);
			param.put("steamid", steamid);
			param.put("appid", appid);
			param.put("reportid", reportid);
			param.put("cheatdescription", cheatdescription);
			param.put("duration", duration);
			param.put("delayban", delayban);
			param.put("flags", flags);
			param.put("invisible_ban", invisible_ban);
			VxHolder.request(HttpMethod.POST, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param key -- Access key
		 * @param steamid -- steamid of the user who is reported as cheating.
		 * @param appid -- The appid.
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void RemovePlayerGameBan(String key,long steamid,int appid,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/ICheatReportingService/RemovePlayerGameBan/v1";
			JsonObject param = new JsonObject();
			param.put("key", key);
			param.put("steamid", steamid);
			param.put("appid", appid);
			VxHolder.request(HttpMethod.POST, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param key -- Access key
		 * @param appid -- The appid.
		 * @param timeend -- The beginning of the time range .
		 * @param timebegin -- The end of the time range.
		 * @param reportidmin -- Minimum reportID to include
		 * @param includereports -- (Optional) Include reports.
		 * @param includebans -- (Optional) Include ban requests.
		 * @param steamid -- (Optional) Query just for this steamid.
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GetCheatingReports(String key,int appid,int timeend,int timebegin,long reportidmin,boolean includereports,boolean includebans,long steamid,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/ICheatReportingService/GetCheatingReports/v1";
			JsonObject param = new JsonObject();
			param.put("key", key);
			param.put("appid", appid);
			param.put("timeend", timeend);
			param.put("timebegin", timebegin);
			param.put("reportidmin", reportidmin);
			param.put("includereports", includereports);
			param.put("includebans", includebans);
			param.put("steamid", steamid);
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param key -- Access key
		 * @param steamid -- steamid of the user.
		 * @param appid -- The appid the user is playing.
		 * @param session_id -- session id
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void RequestVacStatusForUser(String key,long steamid,int appid,long session_id,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/ICheatReportingService/RequestVacStatusForUser/v1";
			JsonObject param = new JsonObject();
			param.put("key", key);
			param.put("steamid", steamid);
			param.put("appid", appid);
			param.put("session_id", session_id);
			VxHolder.request(HttpMethod.POST, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param key -- Access key
		 * @param steamid -- steamid of the user.
		 * @param appid -- The appid the user is playing.
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void StartSecureMultiplayerSession(String key,long steamid,int appid,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/ICheatReportingService/StartSecureMultiplayerSession/v1";
			JsonObject param = new JsonObject();
			param.put("key", key);
			param.put("steamid", steamid);
			param.put("appid", appid);
			VxHolder.request(HttpMethod.POST, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param key -- Access key
		 * @param steamid -- steamid of the user.
		 * @param appid -- The appid the user is playing.
		 * @param session_id -- session id
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void EndSecureMultiplayerSession(String key,long steamid,int appid,long session_id,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/ICheatReportingService/EndSecureMultiplayerSession/v1";
			JsonObject param = new JsonObject();
			param.put("key", key);
			param.put("steamid", steamid);
			param.put("appid", appid);
			param.put("session_id", session_id);
			VxHolder.request(HttpMethod.POST, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param key -- Access key
		 * @param steamid -- steamid of the user running and reporting the cheat.
		 * @param appid -- The appid.
		 * @param pathandfilename -- path and file name of the cheat executable.
		 * @param webcheaturl -- web url where the cheat was found and downloaded.
		 * @param time_now -- local system time now.
		 * @param time_started -- local system time when cheat process started. ( 0 if not yet run )
		 * @param time_stopped -- local system time when cheat process stopped. ( 0 if still running )
		 * @param cheatname -- descriptive name for the cheat.
		 * @param game_process_id -- process ID of the running game.
		 * @param cheat_process_id -- process ID of the cheat process that ran
		 * @param cheat_param_1 -- cheat param 1
		 * @param cheat_param_2 -- cheat param 2
		 * @param cheat_data_dump -- data collection in json format
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void ReportCheatData(String key,long steamid,int appid,String pathandfilename,String webcheaturl,long time_now,long time_started,long time_stopped,String cheatname,int game_process_id,int cheat_process_id,long cheat_param_1,long cheat_param_2,String cheat_data_dump,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/ICheatReportingService/ReportCheatData/v1";
			JsonObject param = new JsonObject();
			param.put("key", key);
			param.put("steamid", steamid);
			param.put("appid", appid);
			param.put("pathandfilename", pathandfilename);
			param.put("webcheaturl", webcheaturl);
			param.put("time_now", time_now);
			param.put("time_started", time_started);
			param.put("time_stopped", time_stopped);
			param.put("cheatname", cheatname);
			param.put("game_process_id", game_process_id);
			param.put("cheat_process_id", cheat_process_id);
			param.put("cheat_param_1", cheat_param_1);
			param.put("cheat_param_2", cheat_param_2);
			param.put("cheat_data_dump", cheat_data_dump);
			VxHolder.request(HttpMethod.POST, requestURI, param, successHandler, failedHandler);
		}
	}
    public static class IWorkshopService {

		/** 
		 * @param key -- Access key
		 * @param appid
		 * @param gameitemid
		 * @param associated_workshop_files
		 * @param partner_accounts
		 * @param validate_only -- Only validates the rules and does not persist them.
		 * @param make_workshop_files_subscribable
		 * @param associated_workshop_file_for_direct_payments
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void SetItemPaymentRules(String key,int appid,int gameitemid,JsonObject associated_workshop_files,JsonObject partner_accounts,@Nullable boolean validate_only,boolean make_workshop_files_subscribable,JsonObject associated_workshop_file_for_direct_payments,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/IWorkshopService/SetItemPaymentRules/v1";
			JsonObject param = new JsonObject();
			param.put("key", key);
			param.put("appid", appid);
			param.put("gameitemid", gameitemid);
			param.put("associated_workshop_files", associated_workshop_files);
			param.put("partner_accounts", partner_accounts);
			param.put("make_workshop_files_subscribable", make_workshop_files_subscribable);
			param.put("associated_workshop_file_for_direct_payments", associated_workshop_file_for_direct_payments);
			VxHolder.request(HttpMethod.POST, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param key -- Access key
		 * @param appid
		 * @param gameitemid
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GetFinalizedContributors(String key,int appid,int gameitemid,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/IWorkshopService/GetFinalizedContributors/v1";
			JsonObject param = new JsonObject();
			param.put("key", key);
			param.put("appid", appid);
			param.put("gameitemid", gameitemid);
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param key -- Access key
		 * @param appid
		 * @param item_id
		 * @param date_start
		 * @param date_end
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void GetItemDailyRevenue(String key,int appid,int item_id,int date_start,int date_end,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/IWorkshopService/GetItemDailyRevenue/v1";
			JsonObject param = new JsonObject();
			param.put("key", key);
			param.put("appid", appid);
			param.put("item_id", item_id);
			param.put("date_start", date_start);
			param.put("date_end", date_end);
			VxHolder.request(HttpMethod.GET, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param key -- Access key
		 * @param appid
		 * @param languages
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void PopulateItemDescriptions(String key,int appid,JsonObject languages,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/IWorkshopService/PopulateItemDescriptions/v1";
			JsonObject param = new JsonObject();
			param.put("key", key);
			param.put("appid", appid);
			param.put("languages", languages);
			VxHolder.request(HttpMethod.POST, requestURI, param, successHandler, failedHandler);
		}

		/** 
		 * @param key -- Access key
		 * @param appid -- AppID
		 * @param gameitemid -- Game Item ID
		 * @param date -- YYY-MM-DD formatted string
		 * @param payment_us_usd -- US Payment portion in USD Cents
		 * @param payment_row_usd -- ROW Payment portion in USD Cents
		 * @param successHandler -- the handler that will be called with the succeeded result
		 * @param failedHandler --  failed Handler
		 */
		public static void AddSpecialPayment(String key,int appid,int gameitemid,String date,long payment_us_usd,long payment_row_usd,Handler<JsonObject> successHandler,Handler<Throwable> failedHandler) {
			String requestURI = "https://partner.steam-api.com/IWorkshopService/AddSpecialPayment/v1";
			JsonObject param = new JsonObject();
			param.put("key", key);
			param.put("appid", appid);
			param.put("gameitemid", gameitemid);
			param.put("date", date);
			param.put("payment_us_usd", payment_us_usd);
			param.put("payment_row_usd", payment_row_usd);
			VxHolder.request(HttpMethod.POST, requestURI, param, successHandler, failedHandler);
		}
	}

}
