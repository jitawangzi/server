package cn.game.games.net.game.module.recharge;

import java.util.ArrayList;
import java.util.List;

public class PayItem {
	/** 订单id */
	private long orderId;
	/** 支付的人民币，元 */
	private int rmb;
	/** 付费类型 */
	private PayType payType;
	/** 买了什么东西？ 根据不同的付费类型，具体物品的id，例如通行证id，月卡id、某某活动id等等 。
	 * 通常是代表一种特殊的购买，不单是加物品 */
	private int payId;
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

	public void finish() {
		this.isFinish = true;
		this.finishTime = System.currentTimeMillis();
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
}
