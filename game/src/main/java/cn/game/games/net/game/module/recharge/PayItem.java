package cn.game.games.net.game.module.recharge;

import java.util.ArrayList;
import java.util.List;

@com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)
public class PayItem {
	/** 游戏服务器产生的订单id */
	private long orderId;
	/** sdk产生的订单id */
	private String sdkOrderId;
	/** 支付的人民币，元 */
	private int rmb;
	/** 付费类型 */
	private PayType payType;
	/** 买了什么东西？ 根据不同的付费类型，具体物品的id，例如通行证id，月卡id、某某活动id等等 。
	 * 通常是代表一种特殊的购买，不单是加物品 */
	private int payId;
	private int sdkGoodsId	; 
	/**
	 * 充值 额外需要的参数列表
	 */
	private List<Integer> paySubIds = new ArrayList<>();
	/** 如果直接购买的某某东西，这个代表物品id ,一般如果有这个id，则不需要payId */
	private int addId;
	/** 如果直接购买的某某东西，这个代表物品数量  */
	private int addCount;
	/** 订单是否已发货，不是绝对正确。 */
	private boolean isFinish;
	private long finishTime;

    /** 非空表示已开始履约；失败/中断订单不能自动重发，旧记录缺省为 null。 */
    private String completionState;

    /** 与奖励一同进入聚合快照的版本化履约凭证；旧数据缺省为空。 */
    private CompletionProof completionProof;
    /** 本进程刚生成的凭证须等原保存回执确认；不序列化，也不通过第二次保存发布。 */
    @com.fasterxml.jackson.annotation.JsonIgnore
    private transient boolean proofAwaitingCommit;

    /** 绑定玩家与全部购买参数，防止错单、金额或商品变更后复用完成凭证。 */
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)
    private record CompletionProof(int version, long playerId, long orderId, String sdkOrderId,
            int rmb, PayType payType, int payId, int sdkGoodsId, List<Integer> paySubIds, int addId, int addCount) {}

    /** @param playerId 奖励所属玩家；仅完整奖励及 Charge 成功后调用，随后必须保存同一聚合快照。 */
    public void prepareCompletionProof(long playerId) {
        if (!isFinish || !"REWARDS_COMPLETED".equals(completionState) || completionProof != null) {
            throw new IllegalStateException("PAYMENT_PROOF_INVALID_PHASE");
        }
        completionProof = proofFor(playerId);
        proofAwaitingCommit = true;
    }

    /** 原保存成功后启用内存凭证；超时、UNKNOWN、失败及迟到提交不调用。 */
    public void confirmCompletionProof() { proofAwaitingCommit = false; }

    /** @param playerId 当前权威玩家 @param sdkId 本次通知的平台单号 @return 是否有匹配且已提交的本批凭证 */
    public boolean hasCommittedCompletionProof(long playerId, String sdkId) {
        return !proofAwaitingCommit && isFinish && "REWARDS_COMPLETED".equals(completionState)
                && java.util.Objects.equals(sdkOrderId, sdkId) && completionProof != null
                && completionProof.equals(proofFor(playerId));
    }

    /** @param playerId 当前所属玩家 @return 当前订单参数的不可变凭证，用于生成及比对 */
    private CompletionProof proofFor(long playerId) {
        return new CompletionProof(1, playerId, orderId, sdkOrderId, rmb, payType, payId, sdkGoodsId,
                paySubIds == null ? List.of() : List.copyOf(paySubIds), addId, addCount);
    }

    /** @return 当前履约阶段；旧 finish 不被追认为完整奖励证据 */
    public String getCompletionState() { return completionState; }

    /** @param state 持久化的履约阶段，供保守阻断及人工核对使用 */
    public void setCompletionState(String state) { completionState = state; }

	public void finish() {
		this.isFinish = true;
		this.finishTime = cn.game.util.DateUtil.currentTimeMillis();
	}

	public long getOrderId() {
		return orderId;
	}

	public void setOrderId(long orderId) {
		this.orderId = orderId;
	}

	public int getRmb() {
		return rmb;
	}

	public void setRmb(int rmb) {
		this.rmb = rmb;
	}

	public PayType getPayType() {
		return payType;
	}

	public void setPayType(PayType payType) {
		this.payType = payType;
	}

	public int getPayId() {
		return payId;
	}

	public void setPayId(int payId) {
		this.payId = payId;
	}

	public int getAddId() {
		return addId;
	}

	public void setAddId(int addId) {
		this.addId = addId;
	}

	public int getAddCount() {
		return addCount;
	}

	public void setAddCount(int addCount) {
		this.addCount = addCount;
	}

	public boolean isFinish() {
		return isFinish;
	}

	public void setFinish(boolean isFinish) {
		this.isFinish = isFinish;
	}

	public long getFinishTime() {
		return finishTime;
	}

	public void setFinishTime(long finishTime) {
		this.finishTime = finishTime;
	}

	public List<Integer> getPaySubIds() {
		return paySubIds;
	}

	public void setPaySubIds(List<Integer> paySubIds) {
		this.paySubIds = paySubIds;
	}

	public void addPaySubIds(int  ... paySubIds){
		for (int paySubId : paySubIds){
			this.paySubIds.add(paySubId);
		}
	}

	public String getSdkOrderId() {
		return sdkOrderId;
	}

	public void setSdkOrderId(String sdkOrderId) {
		this.sdkOrderId = sdkOrderId;
	}

	public int getSdkGoodsId() {
		return sdkGoodsId;
	}

	public void setSdkGoodsId(int sdkGoodsId) {
		this.sdkGoodsId = sdkGoodsId;
	}

	@Override
	public String toString() {
		return "PayItem [orderId=" + orderId + ", rmb=" + rmb + ", payType=" + payType + ", payId=" + payId + ", paySubIds=" + paySubIds
				+ ", addId=" + addId + ", addCount=" + addCount + ", isFinish=" + isFinish + ", finishTime=" + finishTime + "]";
	}

}
