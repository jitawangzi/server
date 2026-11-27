package cn.game.games.net.cross.activity.impl;

import java.util.List;

import cn.game.games.net.game.module.award.Goods;

/**
 * 跨服世界Boss结算异步发邮 SPI。
 * <p>
 * 职责边界：隔离 {@link cn.game.games.net.game.helper.MailHelper#sendMail(long, int, List, boolean)} 静态远端投递，
 * 既保障生产环境单向异步 RPC 无阻塞发邮，又支持单元测试注入内存发邮桩精确断言发邮次数与附件明细。
 * </p>
 *
 * @author shuyongqiang
 */
@FunctionalInterface
public interface CrossWorldBossMailSender {

	/**
	 * 向指定玩家异步投递奖励邮件（严禁在实现中阻塞等待目标服返回）
	 *
	 * @param receiverId 接收玩家 ID
	 * @param mailId     邮件模板 ID
	 * @param goods      邮件附件道具列表
	 * @param notify     是否下发新邮件通知
	 */
	void sendMail(long receiverId, int mailId, List<Goods> goods, boolean notify);
}
