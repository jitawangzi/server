package cn.game.games.net.game.module.union;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;

import cn.game.games.net.game.module.union.UnionService.UnionFullInfo;
import cn.game.games.net.game.module.union.UnionService.UnionMember;

public class UnionGameService {
	private final UnionService unionService;

	@Autowired
	public UnionGameService(UnionService unionService) {
		this.unionService = unionService;
	}

	// 创建工会
	public boolean createUnion(String playerId, String unionName) {
		UnionInfo info = new UnionInfo();
		info.setUnionId(UUID.randomUUID().toString());
		info.setName(unionName);
		info.setLeaderId(playerId);
		info.setLevel(1);
		info.setCreateTime(System.currentTimeMillis());

		return unionService.createUnion(info.getUnionId(), info);
	}

	// 玩家加入工会
	public boolean joinUnion(String unionId, String playerId, String playerName) {
		UnionMember member = new UnionMember();
		member.setMemberId(playerId);
		member.setName(playerName);
		member.setPosition(3); // 普通成员
		member.setJoinTime(System.currentTimeMillis());

		return unionService.addMember(unionId, member);
	}

	// 捐献资源
	public boolean donate(String unionId, String playerId, long amount) {
		// 增加资源
		unionService.addResource(unionId, amount);
		// 增加活跃度
		unionService.incrementActivity(unionId, amount / 100);
		return true;
	}

	// 获取工会信息
	public UnionFullInfo getUnionInfo(String unionId) {
		try {
			return unionService.getUnionFullInfoAsync(unionId).get(5, TimeUnit.SECONDS);
		} catch (Exception e) {
			throw new RuntimeException("Failed to get union info", e);
		}
	}
}