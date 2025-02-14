package cn.game.games.net.cross.zongmen;

import cn.game.protocol.generated.config.GuildBargainConfig;
import cn.game.protocol.generated.manager.GuildBargainManager;
import cn.game.protocol.protobuf.ZongMenMsg;

/**    
 * 宗门砍价
 * 2025年2月14日 12:02:19
 * @author SYQ
 */
public class ZongMenBargain implements ZongMenConstants.ZongMenEventHandler {
	/** 累计砍下来的数量 */
	private int bargainTotalNum;
	/** 累计砍价人数 */
	private int memberBargainNum;
	/** 刷新出来的砍价物品id ： GuildBargain 表id */
	private int bargainItemId;

	void init() {
		refreshBargain();
	}

	private void refreshBargain() {
		bargainTotalNum = 0;
		memberBargainNum = 0;
		bargainItemId++ ; 
		GuildBargainConfig nullable = GuildBargainManager.instance().getNullable(bargainItemId);
		if (nullable == null) {
			bargainItemId = 1;
		}
		
	}
    public ZongMenConstants.ZongMenEvenType[] getRegisterEvent() {
        return new ZongMenConstants.ZongMenEvenType[]{ZongMenConstants.ZongMenEvenType.CROSS_DAY};
    }

    @Override
    public void handleEventType(ZongMenConstants.ZongMenEvenType type, ZongMenInfo info, Object... params) {
            switch (type){
			case CROSS_DAY -> refreshBargain();
            }
    }

	public int getBargainTotalNum() {
		return bargainTotalNum;
	}

	public int getMemberBargainNum() {
		return memberBargainNum;
	}

	public int getBargainItemId() {
		return bargainItemId;
	}

	public void setBargainTotalNum(int bargainTotalNum) {
		this.bargainTotalNum = bargainTotalNum;
	}

	public void setMemberBargainNum(int memberBargainNum) {
		this.memberBargainNum = memberBargainNum;
	}

	public void setBargainItemId(int bargainItemId) {
		this.bargainItemId = bargainItemId;
	}

	public ZongMenMsg.ZongMenBargainProto toProto(ZongMenMember member) {
		return ZongMenMsg.ZongMenBargainProto.newBuilder()
				.setIsBargain(member.isBargain)
				.setIsBargainBuy(member.isBargainBuy())
				.setTotalBargainCount(bargainTotalNum)
				.setTotalMemberCount(memberBargainNum)
				.setBargainItemId(bargainItemId)
				.build();
    }
}
