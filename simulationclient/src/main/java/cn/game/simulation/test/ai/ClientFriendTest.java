package cn.game.simulation.test.ai;

import java.util.List;

import cn.game.protocol.protobuf.BaseMsg.SimplePlayerInfo;
import cn.game.protocol.protobuf.FriendMsg.FriendApplyListRequest_30000053;
import cn.game.protocol.protobuf.FriendMsg.FriendApplyListResponse_30000054;
import cn.game.protocol.protobuf.FriendMsg.FriendApplyRequest_30000005;
import cn.game.protocol.protobuf.FriendMsg.FriendApplyResponse_30000006;
import cn.game.protocol.protobuf.FriendMsg.FriendBlackListRequest_30000051;
import cn.game.protocol.protobuf.FriendMsg.FriendBlackListResponse_30000052;
import cn.game.protocol.protobuf.FriendMsg.FriendInfo;
import cn.game.protocol.protobuf.FriendMsg.FriendListRequest_30000001;
import cn.game.protocol.protobuf.FriendMsg.FriendListResponse_30000002;
import cn.game.protocol.protobuf.FriendMsg.FriendRecommendRequest_30000003;
import cn.game.protocol.protobuf.FriendMsg.FriendRecommendResponse_30000004;

/**
 * 模拟客户端好友功能及新账号好友列表场景测试
 * 
 * 验证新账号创建登录后：
 * 1. 账号基本信息正确加载与自动注册
 * 2. 初始好友列表非空且好友数量为0
 * 3. 初始黑名单列表非空且数量为0
 * 4. 初始好友申请列表非空且数量为0
 * 5. 推荐好友查询功能正常并返回有效玩家数据
 * 6. 接口调用的幂等性与重复请求稳定性验证
 * 
 * @author shuyongqiang
 */
public class ClientFriendTest extends ClientBaseScenarioTest {

    public static void main(String[] args) {
        new ClientFriendTest().start();
    }

    @Override
    protected void executeProcess() throws Exception {
        System.out.println("==================================================");
        System.out.println(">>> 开始执行新账号创建与好友列表功能测试");
        System.out.println("==================================================");

        // 0. 验证新账号注册与登录状态
        long playerId = client.getPlayerId();
        String playerName = client.getPlayerAllInfo() != null && client.getPlayerAllInfo().hasPlayer()
                ? client.getPlayerAllInfo().getPlayer().getName()
                : "未知";

        System.out.println(String.format("当前测试新账号信息: PlayerId=%d, Name=%s", playerId, playerName));
        if (playerId <= 0) {
            throw new RuntimeException("新账号创建/登录失败：PlayerId 非法 (" + playerId + ")");
        }

        // 1. 测试请求好友列表 (FriendListRequest_30000001)
        System.out.println("\n[步骤 1] 请求好友列表 (FriendListRequest_30000001)...");
        FriendListResponse_30000002 friendListResp = sendAndWait(FriendListRequest_30000001.getDefaultInstance());
        validateFriendList(friendListResp, "初始好友列表");
        System.out.println("好友列表请求成功，好友数量: " + friendListResp.getFriendsCount());
        if (friendListResp.getFriendsCount() != 0) {
            throw new RuntimeException("断言失败：新创建的账号初始好友数量应为 0，实际为: " + friendListResp.getFriendsCount());
        }

        // 2. 测试请求黑名单列表 (FriendBlackListRequest_30000051)
        System.out.println("\n[步骤 2] 请求黑名单列表 (FriendBlackListRequest_30000051)...");
        FriendBlackListResponse_30000052 blackListResp = sendAndWait(FriendBlackListRequest_30000051.getDefaultInstance());
        if (blackListResp == null) {
            throw new RuntimeException("黑名单列表响应为 null");
        }
        System.out.println("黑名单列表请求成功，黑名单数量: " + blackListResp.getPlayersCount());
        if (blackListResp.getPlayersCount() != 0) {
            throw new RuntimeException("断言失败：新创建的账号初始黑名单数量应为 0，实际为: " + blackListResp.getPlayersCount());
        }

        // 3. 测试请求好友申请列表 (FriendApplyListRequest_30000053)
        System.out.println("\n[步骤 3] 请求好友申请列表 (FriendApplyListRequest_30000053)...");
        FriendApplyListResponse_30000054 applyListResp = sendAndWait(FriendApplyListRequest_30000053.getDefaultInstance());
        if (applyListResp == null) {
            throw new RuntimeException("好友申请列表响应为 null");
        }
        System.out.println("好友申请列表请求成功，申请数量: " + applyListResp.getPlayersCount());
        if (applyListResp.getPlayersCount() != 0) {
            throw new RuntimeException("断言失败：新创建的账号初始申请列表数量应为 0，实际为: " + applyListResp.getPlayersCount());
        }

        // 4. 测试搜索推荐好友 (FriendRecommendRequest_30000003)
        System.out.println("\n[步骤 4] 搜索推荐好友 (FriendRecommendRequest_30000003)...");
        FriendRecommendResponse_30000004 recommendResp = sendAndWait(FriendRecommendRequest_30000003.getDefaultInstance());
        if (recommendResp == null) {
            throw new RuntimeException("推荐好友列表响应为 null");
        }
        List<SimplePlayerInfo> recommendPlayers = recommendResp.getPlayersList();
        System.out.println("推荐好友请求成功，推荐玩家数量: " + recommendPlayers.size());
        for (SimplePlayerInfo p : recommendPlayers) {
            if (p.getId() == null || p.getId().isEmpty()) {
                throw new RuntimeException("推荐好友校验失败：存在 ID 为空的推荐玩家");
            }
            if (p.getId().equals(String.valueOf(playerId))) {
                throw new RuntimeException("推荐好友校验失败：推荐列表中包含了玩家自己 (ID=" + playerId + ")");
            }
            System.out.println(String.format("  - 推荐玩家: ID=%s, Name=%s, Level=%d", p.getId(), p.getName(), p.getLevel()));
        }

        // 5. 测试幂等性：重复请求好友列表与申请列表 (对齐 client-test.md 第6节 对抗性测试要求)
        System.out.println("\n[步骤 5] 验证重复请求幂等性...");
        FriendListResponse_30000002 secondFriendListResp = sendAndWait(FriendListRequest_30000001.getDefaultInstance());
        validateFriendList(secondFriendListResp, "重复请求好友列表");
        if (secondFriendListResp.getFriendsCount() != 0) {
            throw new RuntimeException("断言失败：重复请求好友列表结果不一致，数量应为 0，实际为: " + secondFriendListResp.getFriendsCount());
        }

        FriendApplyListResponse_30000054 secondApplyListResp = sendAndWait(FriendApplyListRequest_30000053.getDefaultInstance());
        if (secondApplyListResp == null || secondApplyListResp.getPlayersCount() != 0) {
            throw new RuntimeException("断言失败：重复请求申请列表数量应为 0");
        }
        System.out.println("重复请求幂等性校验通过！");

        // 6. 如果存在推荐玩家，测试申请好友协议交互
        if (!recommendPlayers.isEmpty()) {
            SimplePlayerInfo target = recommendPlayers.get(0);
            System.out.println(String.format("\n[步骤 6] 测试向推荐玩家申请好友: TargetId=%s, Name=%s", target.getId(), target.getName()));
            FriendApplyRequest_30000005 applyReq = FriendApplyRequest_30000005.newBuilder()
                    .addPlayerIds(target.getId())
                    .build();
            FriendApplyResponse_30000006 applyResp = sendAndWait(applyReq);
            if (applyResp == null) {
                throw new RuntimeException("好友申请响应为 null");
            }
            System.out.println("好友申请已发送成功！");
        } else {
            System.out.println("\n[步骤 6] 当前服务器无其他活跃玩家推荐，跳过添加好友申请测试");
        }

        System.out.println("\n==================================================");
        System.out.println(">>> 新账号创建与好友列表功能端到端测试全部通过！");
        System.out.println("==================================================");
    }

    private void validateFriendList(FriendListResponse_30000002 resp, String context) {
        if (resp == null) {
            throw new RuntimeException(context + " 校验失败：响应为 null");
        }
        for (FriendInfo friend : resp.getFriendsList()) {
            if (!friend.hasPlayer()) {
                throw new RuntimeException(context + " 校验失败：FriendInfo 缺少 player 数据");
            }
            SimplePlayerInfo player = friend.getPlayer();
            if (player.getId() == null || player.getId().isEmpty()) {
                throw new RuntimeException(context + " 校验失败：好友 player ID 为空");
            }
        }
    }
}
