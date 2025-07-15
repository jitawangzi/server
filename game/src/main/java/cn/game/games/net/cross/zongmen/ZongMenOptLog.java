package cn.game.games.net.cross.zongmen;

import java.util.ArrayList;
import java.util.List;

import cn.game.games.net.game.manager.PlayerManager;
import cn.game.protocol.generated.config.GlobalConst;
import cn.game.protocol.protobuf.ChatMsg;
import cn.game.protocol.protobuf.PbProtocol;
import cn.game.protocol.protobuf.ZongMenMsg;

/**
 * @ClassName ZongMenOptLog
 *
 * @description: 宗门操作日志
 * @author: ly
 * @create: 2025-02-05 16:50 @Version 1.0
 */
public class ZongMenOptLog implements ZongMenConstants.ZongMenEventHandler {

    List<OptLogData> optLogDataList = new ArrayList<>();

    @Override
    public ZongMenConstants.ZongMenEvenType[] getRegisterEvent() {
        return new ZongMenConstants.ZongMenEvenType[] {
                ZongMenConstants.ZongMenEvenType.JOIN_ZONG_MEN,
                ZongMenConstants.ZongMenEvenType.ZONG_MEN_POSITION_CHANGE,
                ZongMenConstants.ZongMenEvenType.CHANGE_ZONG_MEN_NAME,
                ZongMenConstants.ZongMenEvenType.QUIT_ZONG_MEN,
                ZongMenConstants.ZongMenEvenType.CHANGE_ZONG_MEN_DECLARATION,
                ZongMenConstants.ZongMenEvenType.ZONG_MEN_KICK_MEMBER,
                ZongMenConstants.ZongMenEvenType.CHANGE_ZONG_MEN_NOTICE,
        };
    }

    @Override
    public void handleEventType(
            ZongMenConstants.ZongMenEvenType type, ZongMenInfo info, Object... params) {
        switch (type){
            case JOIN_ZONG_MEN -> {
                String joinPlayerName = params[1]+"";
                addLog(info,type.getId(),joinPlayerName);
            }
            case ZONG_MEN_POSITION_CHANGE -> {
                long targetPlayerId = (long)params[0];
                int oldPosition = (int)params[1];
                int newPosition = (int)params[2];
                PlayerManager.getInstance().getSimplePlayerFromRedisAsync(targetPlayerId).onSuccess(simplePlayer -> {
                    addLog(info,type.getId(),simplePlayer.getName(),oldPosition+"",newPosition+"");

                });
            }

            case CHANGE_ZONG_MEN_NAME -> {
                String changeNamePlayerName = params[0]+"";
                String newName = params[1]+"";
                addLog(info,type.getId(),changeNamePlayerName,newName);
            }
            case QUIT_ZONG_MEN,
                    CHANGE_ZONG_MEN_DECLARATION,
                    CHANGE_ZONG_MEN_NOTICE -> {
                String quitPlayerName = params[0]+"";
                addLog(info,type.getId(),quitPlayerName);
            }
            case ZONG_MEN_KICK_MEMBER -> {
                String playerName = params[0]+"";//操作人
                String kickPlayerName = params[1]+""; //被踢人
                addLog(info,type.getId(),playerName,kickPlayerName);
            }
        }
    }

    private void addLog(ZongMenInfo info,int type, String... params) {
        long now = System.currentTimeMillis();
        List<String> paramList = new ArrayList<>();
        for (String param : params) {
            paramList.add(param);
        }
        OptLogData logData = new OptLogData(now,type,paramList);
        while (optLogDataList.size() >= GlobalConst.ZongmenLogNum){
            optLogDataList.remove(optLogDataList.size() - 1);
        }
        optLogDataList.add(logData);
        List<Long> notifyPids = new ArrayList<>(info.getModule().menMemberMap.keySet());
        ZongMenHelper.broadcastNotifyMsgToPlayer(logData.toChatProto(), PbProtocol.ChatMessagePush_31010001,notifyPids);
    }

    public List<ZongMenMsg.ZongMenLogProto> toProto(){
        List<ZongMenMsg.ZongMenLogProto> list = new ArrayList<>();
        optLogDataList.forEach(optLogData -> {list.add(optLogData.toProto());});
        return list;
    }

    public static class OptLogData {
        long createTimer;
        int optType;
        List<String> params = new ArrayList<>();

        public OptLogData(long createTimer, int optType, List<String> params) {
            this.createTimer = createTimer;
            this.optType = optType;
            this.params = params;
        }

        public OptLogData() {}
        public ZongMenMsg.ZongMenLogProto toProto(){
            return ZongMenMsg.ZongMenLogProto.newBuilder().setType(optType).setCreateTimer((int)(createTimer/1000L)).addAllParams(params).build();
        }

        public ChatMsg.ChatMessagePush_31010001 toChatProto(){
            ChatMsg.ChatMessagePush_31010001.Builder msg = ChatMsg.ChatMessagePush_31010001.newBuilder();
            ChatMsg.ChatMessageInfo.Builder builder = ChatMsg.ChatMessageInfo.newBuilder();
            builder.setChatType(ChatMsg.ChatType.UNINON_CHAT);
            builder.setOptType(1);
            StringBuffer sb = new StringBuffer().append(optType);
            params.forEach(str ->{sb.append("&").append(str);});
            builder.setContent(sb.toString());
            msg.addMessageInfo(builder);
            return msg.build();
        }

    }
}

