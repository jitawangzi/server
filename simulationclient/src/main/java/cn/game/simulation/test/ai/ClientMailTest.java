package cn.game.simulation.test.ai;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import cn.game.protocol.protobuf.MailMsg.MailDeleteRequest_12000007;
import cn.game.protocol.protobuf.MailMsg.MailDeleteResponse_12000008;
import cn.game.protocol.protobuf.MailMsg.MailInfo;
import cn.game.protocol.protobuf.MailMsg.MailListRequest_12000001;
import cn.game.protocol.protobuf.MailMsg.MailListResponse_12000002;
import cn.game.protocol.protobuf.MailMsg.MailReceiveRequest_12000005;
import cn.game.protocol.protobuf.MailMsg.MailReceiveResponse_12000006;
import cn.game.protocol.protobuf.MailMsg.MailSeeRequest_12000003;
import cn.game.protocol.protobuf.MailMsg.MailSeeResponse_12000004;

public class ClientMailTest extends ClientBaseScenarioTest {

    public static void main(String[] args) {
        new ClientMailTest().start();
    }

    /**
     * 测试用例实现说明（模拟与验证模式）
     *
     * 目的：
     * - 演示如何在客户端模拟邮件相关的完整交互流程（列表/查看/领取/删除），
     *   并对每一步服务器返回的数据做严格校验，避免“只看返回就通过”的脆弱测试。
     *
     * 总体契约（Contract）：
     * - 输入：依赖服务端已有或通过测试预置（GM/DB/接口）准备好的邮件数据；
     * - 输出：在出现不符合预期的数据时抛出 RuntimeException（导致测试失败）；
     * - 成功判定：所有步骤的断言/校验通过且没有异常抛出即视为成功。
     *
     * 模式（Pattern / 步骤）——按顺序执行并在每个步骤后校验：
     * 1) MailListRequest（请求邮件列表）
     *    - 目标：获取当前邮件快照；
     *    - 校验：调用 validateMailList(resp, context) 确保邮件的基本字段合法（uid、time、attachments、ranks 等）；
     *
     * 2) MailSeeRequest（查看邮件）
     *    - 选择策略：优先选取一封未读邮件单独查看（设置 uid），若无未读则使用默认实例进行一键查看；
     *    - 校验：查看后重新请求列表，确认目标邮件的 `see` 字段变为 true；若使用一键查看则确认所有邮件的 `see` 为 true；
     *    - 若服务端存在延迟写入（异步/缓存），建议对重新请求结果使用重试（见下方“重试建议”）。
     *
     * 3) MailReceiveRequest（领取附件）
     *    - 选择策略：优先选择一封 `attachmentsCount>0 && !receive` 的邮件发起单条领取请求；否则发起默认实例进行一键领取；
     *    - 校验：
     *       - 若目标邮件有附件，`MailReceiveResponse.rewards` 必须非空（rewardsCount>0）；
     *       - 领取后重新请求列表，目标邮件的 `receive` 字段应为 true；
     *       - 一键领取时，若存在待领取附件的邮件总数>0，则返回 rewardsCount>0；
     *
     * 4) MailDeleteRequest（删除邮件）
     *    - 选择策略：优先删除一封已查看邮件（see==true），否则发送默认实例删除所有已读邮件；
     *    - 校验：删除后重新请求列表，单删则确保目标 uid 不存在；一键删除则列表中不应存在已读邮件；
     *
     * 共同校验点（validateMailList）
     * - 确保 resp 非空；每封邮件 uid 唯一且非空；time/expireTime 非负；
     * - 附件（GoodsInfo）id>0 且 count>0；榜单项（MailRankInfo）name 非空且 rankId>0 等。
     *
     * 模拟数据准备（示例与建议）
     * - 推荐方式 A（通过 GM 接口 / 管理工具）:
     *     1. 使用 GM 工具发送一封带附件的个人邮件给测试玩家：设置 title/content、attachments（GoodsInfo 列表），并记录返回的 uid 或数据库 id；
     *     2. 创建一封只有内容（或榜单 ranks）的邮件用于测试 MailNewPush；
     *     3. 若需要测试过期逻辑，设置 expireTime 为未来或过去的时间戳以确认过期行为。
     * - 推荐方式 B（直接写 DB / 测试 Fixture）：
     *     1. 插入 Mail 表记录，设置 playerId、mailId、attachments（序列化或关联表）、uid（字符串）等；
     *     2. 启动服务或触发 MailModule 的加载，使邮件出现在 MailListResponse 中。
     * - 数据要求示例：
     *     - uid: 非空字符串，唯一（例如："840088043821052929"）
     *     - attachments: [{ id: 1001, count: 1 }, { id: 2002, count: 10 }]
     *     - time: 当前时间戳（秒），expireTime: 大于 time（或 0 表示永不过期）
     *     - ranks: 如用于榜单显示，确保每项 name 与 rankId 合法。
     *
     * 断言粒度建议：
     * - 基本粒度（目前实现）：检查 rewardsCount>0、see/receive 标志变化、UID 存在/不存在等；
     * - 更严格粒度（可选）：比对奖励明细与 attachments 的一一对应（类型/数量/ID）；
     *
     * 错误处理与重试建议：
     * - 若后端异步写库或有短暂延迟，可能在操作立刻返回后重新请求列表仍未反映变更；
     * - 建议在关键的“校验后请求”上使用简单的重试策略：例如最多重试 3 次，每次间隔 200ms，直到校验通过或超时；
     *   下面提供伪代码供参考（仅作注释说明）：
     *   retryUntil(() -> sendAndWait(MailListRequest.getDefaultInstance()), resp -> checkCondition(resp), 3, 200);
     *
     * 设计原则与注意事项：
     * - 每个“有副作用”的操作后都必须重新请求并校验，而不是仅凭操作返回值（某些返回可能为空的响应）认为成功；
     * - 对返回的数组/集合要做非空与元素有效性的校验（避免后续 NPE）；
     * - 在校验奖励时只判断 `rewardsCount>0` 是基本保障；如需更严格，应该比对 rewards 与邮件附件的类型/数量/id 一一对应；
     * - 保持测试幂等：测试运行前应确保环境邮件数据在可控范围内（可通过 GM 接口或 DB 准备好测试工单）；
     *
     * 使用到的辅助方法/约定：
     * - `sendAndWait(Message request)`：发送 protobuf 请求并阻塞获取响应；
     * - `validateMailList(MailListResponse_12000002 resp, String context)`：基本字段校验（本类中已实现）；
     * - 对于更复杂场景，可添加 `retryFetchAndCheck`、`assertRewardsMatchAttachments` 等工具方法。
     */
    @Override
    protected void executeProcess() throws Exception {
        // 1、请求邮件列表
        MailListResponse_12000002 listResponse = sendAndWait(MailListRequest_12000001.getDefaultInstance());
        validateMailList(listResponse, "初始邮件列表");
        List<MailInfo> mailList = listResponse.getMailsList();
        System.out.println("邮件数量: " + mailList.size());

        // 2、查看一封未读邮件（优先单封查看），如果没有未读则一键查看所有未读
        MailSeeRequest_12000003.Builder seeBuilder = MailSeeRequest_12000003.newBuilder();
        boolean hasUnread = false;
        String seeUid = null;
        for (MailInfo mailInfo : mailList) {
            if (!mailInfo.getSee()) { // 未读
                seeBuilder.setUid(mailInfo.getUid());
                hasUnread = true;
                seeUid = mailInfo.getUid();
                break;
            }
        }
        MailSeeResponse_12000004 seeResponse;
        if (hasUnread) {
            seeResponse = sendAndWait(seeBuilder.build());
            System.out.println("已读取邮件 uid=" + seeBuilder.getUid());
            // 验证：重新请求列表，确保该邮件的 see 字段为 true
            MailListResponse_12000002 afterSeeList = sendAndWait(MailListRequest_12000001.getDefaultInstance());
            validateMailList(afterSeeList, "查看后邮件列表");
            boolean found = false;
            for (MailInfo m : afterSeeList.getMailsList()) {
                if (m.getUid().equals(seeUid)) {
                    found = true;
                    if (!m.getSee()) {
                        throw new RuntimeException("查看邮件失败：邮件 uid=" + seeUid + " 在服务器上仍为未读");
                    }
                    break;
                }
            }
            if (!found) {
                throw new RuntimeException("查看邮件失败：邮件 uid=" + seeUid + " 在返回的邮件列表中未找到（可能被删除）");
            }
        } else {
            seeResponse = sendAndWait(MailSeeRequest_12000003.getDefaultInstance());
            System.out.println("执行一键读取所有未读邮件");
            // 验证：重新请求列表，确保没有未读邮件
            MailListResponse_12000002 afterSeeList = sendAndWait(MailListRequest_12000001.getDefaultInstance());
            validateMailList(afterSeeList, "一键查看后邮件列表");
            for (MailInfo m : afterSeeList.getMailsList()) {
                if (!m.getSee()) {
                    throw new RuntimeException("一键读取未读邮件失败：仍存在未读邮件 uid=" + m.getUid());
                }
            }
        }

        // 更新本地 mailList 以便后续判断
        listResponse = sendAndWait(MailListRequest_12000001.getDefaultInstance());
        validateMailList(listResponse, "查看后更新邮件列表");
        mailList = listResponse.getMailsList();

        // 3、领取附件奖励：优先领取一封有附件且未领取的邮件；否则一键领取所有
        MailReceiveRequest_12000005.Builder receiveBuilder = MailReceiveRequest_12000005.newBuilder();
        boolean hasAttachToReceive = false;
        String receiveUid = null;
        int expectedAttachCount = 0;
        for (MailInfo mailInfo : mailList) {
            if (mailInfo.getAttachmentsCount() > 0 && !mailInfo.getReceive()) {
                receiveBuilder.setUid(mailInfo.getUid());
                hasAttachToReceive = true;
                receiveUid = mailInfo.getUid();
                expectedAttachCount = mailInfo.getAttachmentsCount();
                break;
            }
        }
        MailReceiveResponse_12000006 receiveResponse;
        if (hasAttachToReceive && receiveUid != null && !receiveUid.isEmpty()) {
            receiveResponse = sendAndWait(receiveBuilder.build());
            System.out.println("领取附件，返回奖励数量=" + receiveResponse.getRewardsCount());
            // 验证：当邮件有附件时，返回 rewards 必须非空
            if (expectedAttachCount > 0 && receiveResponse.getRewardsCount() == 0) {
                throw new RuntimeException("领取附件失败：邮件 uid=" + receiveUid + " 期待有奖励返回，但 rewards 为空");
            }
            // 验证：重新请求列表，确保该邮件的 receive 字段为 true
            MailListResponse_12000002 afterReceiveList = sendAndWait(MailListRequest_12000001.getDefaultInstance());
            validateMailList(afterReceiveList, "领取单封附件后邮件列表");
            boolean found = false;
            for (MailInfo m : afterReceiveList.getMailsList()) {
                if (m.getUid().equals(receiveUid)) {
                    found = true;
                    if (!m.getReceive()) {
                        throw new RuntimeException("领取附件失败：邮件 uid=" + receiveUid + " 在服务器上仍为未领取");
                    }
                    break;
                }
            }
            if (!found) {
                throw new RuntimeException("领取附件失败：邮件 uid=" + receiveUid + " 在返回的邮件列表中未找到（可能被删除）");
            }
        } else {
            // 计算还未领取附件的邮件总数（attachments>0 && !receive）
            int totalPendingAttachments = 0;
            for (MailInfo mailInfo : mailList) {
                if (mailInfo.getAttachmentsCount() > 0 && !mailInfo.getReceive()) {
                    totalPendingAttachments += mailInfo.getAttachmentsCount();
                }
            }
            receiveResponse = sendAndWait(MailReceiveRequest_12000005.getDefaultInstance());
            System.out.println("一键领取所有附件，返回奖励数量=" + receiveResponse.getRewardsCount());
            if (totalPendingAttachments > 0 && receiveResponse.getRewardsCount() == 0) {
                throw new RuntimeException("一键领取附件失败：预期有奖励返回，但 rewards 为空");
            }
            // 验证：重新请求列表，确保所有有附件的邮件的 receive 字段为 true
            MailListResponse_12000002 afterReceiveList = sendAndWait(MailListRequest_12000001.getDefaultInstance());
            validateMailList(afterReceiveList, "一键领取后邮件列表");
            for (MailInfo m : afterReceiveList.getMailsList()) {
                if (m.getAttachmentsCount() > 0 && !m.getReceive()) {
                    throw new RuntimeException("一键领取附件失败：存在未领取附件的邮件 uid=" + m.getUid());
                }
            }
        }

        // 4、删除邮件：优先删除一封已查看的邮件（用 uid），如果没有则删除所有已读邮件（发送默认实例）
        MailDeleteRequest_12000007.Builder deleteBuilder = MailDeleteRequest_12000007.newBuilder();
        String deleteUid = null;
        for (MailInfo mailInfo : mailList) {
            if (mailInfo.getSee()) { // 已查看，可以删除
                deleteUid = mailInfo.getUid();
                deleteBuilder.setUid(deleteUid);
                break;
            }
        }
        MailDeleteResponse_12000008 deleteResponse;
        if (deleteUid != null && !deleteUid.isEmpty()) {
            deleteResponse = sendAndWait(deleteBuilder.build());
            System.out.println("删除邮件 uid=" + deleteUid);
            // 验证删除：重新请求列表，确保该 uid 不存在
            MailListResponse_12000002 afterDeleteList = sendAndWait(MailListRequest_12000001.getDefaultInstance());
            validateMailList(afterDeleteList, "单删后邮件列表");
            for (MailInfo m : afterDeleteList.getMailsList()) {
                if (m.getUid().equals(deleteUid)) {
                    throw new RuntimeException("删除邮件失败：邮件 uid=" + deleteUid + " 仍然存在于列表中");
                }
            }
        } else {
            // 删除所有已读邮件
            // 先计算已读邮件数
            int readCount = 0;
            for (MailInfo m : mailList) {
                if (m.getSee()) readCount++;
            }
            deleteResponse = sendAndWait(MailDeleteRequest_12000007.getDefaultInstance());
            System.out.println("删除所有已读邮件（未找到单个已读邮件）");
            MailListResponse_12000002 afterDeleteList = sendAndWait(MailListRequest_12000001.getDefaultInstance());
            validateMailList(afterDeleteList, "删所有已读后邮件列表");
            for (MailInfo m : afterDeleteList.getMailsList()) {
                if (m.getSee()) {
                    throw new RuntimeException("删除所有已读邮件失败：仍存在已读邮件 uid=" + m.getUid());
                }
            }
            // 如果一开始就没有已读邮件，则服务器删除后仍可能为0，且这是可接受的
            if (readCount == 0) {
                System.out.println("注意：开始时没有已读邮件，删除操作不会影响邮件列表");
            }
        }

        // 完成
        System.out.println("邮件协议测试完成");
    }

    // 辅助：校验 MailListResponse 中的 MailInfo 数据的合理性
    private void validateMailList(MailListResponse_12000002 resp, String context) {
        if (resp == null) {
            throw new RuntimeException(context + " 校验失败：响应为 null");
        }
        List<MailInfo> mails = resp.getMailsList();
        Set<String> uids = new HashSet<>();
        for (MailInfo m : mails) {
            if (m.getUid() == null || m.getUid().isEmpty()) {
                throw new RuntimeException(context + " 校验失败：存在空 uid 的邮件");
            }
            if (uids.contains(m.getUid())) {
                throw new RuntimeException(context + " 校验失败：存在重复 uid=" + m.getUid());
            }
            uids.add(m.getUid());
            if (m.getTime() < 0) {
                throw new RuntimeException(context + " 校验失败：邮件 uid=" + m.getUid() + " time 字段小于 0");
            }
            if (m.getExpireTime() < 0) {
                throw new RuntimeException(context + " 校验失败：邮件 uid=" + m.getUid() + " expireTime 字段小于 0");
            }
            // 检查附件
            for (int i = 0; i < m.getAttachmentsCount(); i++) {
                cn.game.protocol.protobuf.BaseMsg.GoodsInfo g = m.getAttachments(i);
                if (g.getId() <= 0) {
                    throw new RuntimeException(context + " 校验失败：邮件 uid=" + m.getUid() + " 附件 id 非法=" + g.getId());
                }
                if (g.getCount() <= 0) {
                    throw new RuntimeException(context + " 校验失败：邮件 uid=" + m.getUid() + " 附件 count 非法=" + g.getCount());
                }
            }
            // 检查 ranks
            for (int i = 0; i < m.getRanksCount(); i++) {
                cn.game.protocol.protobuf.MailMsg.MailRankInfo r = m.getRanks(i);
                if (r.getName() == null || r.getName().isEmpty()) {
                    throw new RuntimeException(context + " 校验失败：邮件 uid=" + m.getUid() + " ranks 中存在空 name");
                }
                if (r.getRankId() <= 0) {
                    throw new RuntimeException(context + " 校验失败：邮件 uid=" + m.getUid() + " ranks 中存在 rankId 非法=" + r.getRankId());
                }
            }
        }
    }
}