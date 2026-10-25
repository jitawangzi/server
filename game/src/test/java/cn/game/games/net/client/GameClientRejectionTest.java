package cn.game.games.net.client;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import cn.game.core.net.protocol.bytes.ByteArrayProtocol;
import cn.game.protocol.manual.ErrorMsgEnum;
import cn.game.protocol.protobuf.PbProtocol;

/** 验证队列外拒绝不会借用当前业务序号或写入正常响应缓存，不建立真实网络连接。 */
class GameClientRejectionTest {
    /** 正、零和负请求序号必须原样返回，不使用当前处理序号，也不生成重放缓存。 */
    @Test
    void rejectionUsesOriginalSequenceWithoutCaching() {
        RecordingGameClient client = new RecordingGameClient();
        int previousSequence = client.getCurMessageSeq();
        for (int seq : new int[] {41, 0, -7}) {
            client.rejectProtocol(new ByteArrayProtocol(1, new byte[0], seq), ErrorMsgEnum.system_overload.getId());
            Assertions.assertEquals(seq, client.sentSequence);
            Assertions.assertEquals(PbProtocol.PlayerErrorPush_01000099, client.sentId);
            Assertions.assertEquals(ErrorMsgEnum.system_overload.getId(), client.sentError);
            Assertions.assertFalse(client.recorded);
            Assertions.assertEquals(previousSequence, client.getCurMessageSeq());
            Assertions.assertTrue(client.getRecentMessages().isEmpty());
        }
    }

    /** 仅替换最终网络写入，保留 GameClient 的实际拒绝路径。 */
    private static final class RecordingGameClient extends GameClient {
        /** 捕获的原请求序号。 */
        private int sentSequence;
        /** 捕获的响应协议号。 */
        private int sentId;
        /** 捕获的错误码。 */
        private int sentError;
        /** 是否要求写入正常回复缓存。 */
        private boolean recorded;
        /** 无真实连接，用覆盖的最终写入方法记录数据。 */
        RecordingGameClient() { super(null); }
        @Override
        public boolean sendProtocol(int msgId, int seq, byte[] data, int errorCode, boolean record) {
            sentId = msgId; sentSequence = seq; sentError = errorCode; recorded = record;
            return true;
        }
    }
}
