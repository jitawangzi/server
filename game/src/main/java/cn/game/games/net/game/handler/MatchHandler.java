// package cn.game.games.net.game.handler;
//
//
// import java.util.Collection;
//
// import org.springframework.stereotype.Component;
//
// import com.google.protobuf.InvalidProtocolBufferException;
//
// import cn.game.games.cache.base.ShareCacheFactory;
// import cn.game.games.cache.entity.Player;
// import cn.game.games.cache.op.impl.PlayerOp;
// import cn.game.games.core.net.NetClient;
// import cn.game.games.net.client.GameClient;
// import cn.game.games.net.game.GameClientManager;
// import cn.game.games.net.game.GameServer;
// import cn.game.games.net.game.module.match.MatchManager;
// import cn.game.protocol.protobuf.MatchMsg.MatchExitResponse_07000004;
// import cn.game.protocol.protobuf.MatchMsg.MatchPlayerExitResponse_07000007;
// import cn.game.protocol.protobuf.MatchMsg.MatchPlayerJoinResponse_07000005;
// import cn.game.protocol.protobuf.MatchMsg.MatchPlayerReadyResponse_07000010;
// import cn.game.protocol.protobuf.MatchMsg.MatchRaceFinishRequest_07000013;
// import cn.game.protocol.protobuf.MatchMsg.MatchRaceFinishResponse_07000014;
// import cn.game.protocol.protobuf.MatchMsg.MatchRaceScoreResponse_07000015;
// import cn.game.protocol.protobuf.MatchMsg.MatchReadyResponse_07000009;
// import cn.game.protocol.protobuf.MatchMsg.MatchResponse_07000002;
// import cn.game.protocol.protobuf.PbProtocol;
// import cn.game.games.net.race.match.MatchRoom;
// import cn.game.core.net.socket.handler.BaseHandler;
// import cn.game.games.core.net.socket.handler.Invoker;
// import cn.game.games.util.PbBuilder;
//
/// **
// * 竞赛匹配
// */
// @Component
// public class MatchHandler extends BaseHandler {
//
// @Override
// protected int getModule() {
// return 0x07;
// }
//
// @Override
// protected void inititialize() {
//
// putInvoker(PbProtocol.MatchRequest_07000001, new Invoker() {
// @Override
// public void invoke(NetClient client,Object message) throws
// InvalidProtocolBufferException {
// match(client, message);
// }
//
// });
//
// putInvoker(PbProtocol.MatchExitRequest_07000003, new Invoker() {
// @Override
// public void invoke(NetClient client,Object message) {
// exit(client, message);
// }
//
// });
// putInvoker(PbProtocol.MatchReadyRequest_07000008, new Invoker() {
// @Override
// public void invoke(NetClient client,Object message) {
// ready(client, message);
// }
//
// });
// putInvoker(PbProtocol.MatchRaceFinishRequest_07000013, new Invoker() {
// @Override
// public void invoke(NetClient client,Object message) {
// finish(client, message);
// }
//
// });
//
// }
//
// protected void finish(NetClient client, Object message) {
// Player player = ((GameClient)client).getPlayer() ;
// MatchRaceFinishRequest_07000013 request = (MatchRaceFinishRequest_07000013)
// message ;
//
// MatchRoom room =
// GameServer.getInstance().getRaceGameServerInterface().finish(player, request)
// ;
// if (room!=null) {
// MatchRaceScoreResponse_07000015.Builder builder =
// MatchRaceScoreResponse_07000015.newBuilder() ;
// builder.setScore(request.getScore()) ;
// builder.setId(player.getData().getId()) ;
//
// room.broadcast(builder.build(),player.getData().getId());
// }
//
// client.sendProtocol(MatchRaceFinishResponse_07000014.getDefaultInstance());
// }
//
// protected void ready(NetClient client, Object message) {
//
// PlayerOp playerOp = ShareCacheFactory.getCache(PlayerOp.class) ;
// Player player = playerOp.getPlayer(client.getPlayerId());
//
// MatchRoom matchRoom =
// GameServer.getInstance().getRaceGameServerInterface().ready(player) ;
//
// MatchPlayerReadyResponse_07000010.Builder ready =
// MatchPlayerReadyResponse_07000010.newBuilder() ;
// ready.setId(player.getData().getId()) ;
// matchRoom.broadcast(ready.build(), player.getData().getId());
//
// client.sendProtocol(MatchReadyResponse_07000009.getDefaultInstance());
//
// }
//
// protected void exit(NetClient client, Object message) {
//
// PlayerOp playerOp = ShareCacheFactory.getCache(PlayerOp.class) ;
// Player player = playerOp.getPlayer(client.getPlayerId());
//
// MatchRoom exit =
// GameServer.getInstance().getRaceGameServerInterface().exitMatch(player);
//
// if (exit!=null) {
// MatchPlayerExitResponse_07000007.Builder builder =
// MatchPlayerExitResponse_07000007.newBuilder();
// builder.setId(player.getData().getId()) ;
// exit.broadcast(builder.build());
// }
// player.getData().setMatchRoomId(0);
// client.sendProtocol(MatchExitResponse_07000004.getDefaultInstance());
//
// }
//
// protected void match(NetClient client, Object message) {
//
// PlayerOp playerOp = ShareCacheFactory.getCache(PlayerOp.class) ;
// Player player = playerOp.getPlayer(client.getPlayerId());
// MatchRoom room =
// GameServer.getInstance().getRaceGameServerInterface().match(player);
// player.getData().setMatchRoomId(room.getId());
// MatchResponse_07000002.Builder response = MatchResponse_07000002.newBuilder()
// ;
//
// if (room!=null) {
//
// for (Player player2 : room.getPlayers()) {
// if (player2.equals(player)) {
// continue ;
// }
// response.addFriends(PbBuilder.buildFriendBaseInfo(player2)) ;
// GameClient gameClient =
// GameClientManager.getInstance().getGameClientByPlayer(player2.getId());
//
// MatchPlayerJoinResponse_07000005.Builder builder =
// MatchPlayerJoinResponse_07000005.newBuilder();
// builder.setFriends(PbBuilder.buildFriendBaseInfo(player2));
// gameClient.sendProtocol(builder.build());
//
// }
// }
// client.sendProtocol(response.build());
//
// }
//
//
// }
