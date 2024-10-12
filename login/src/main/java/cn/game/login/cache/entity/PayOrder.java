package cn.game.login.cache.entity;

import java.io.Serializable;

public class PayOrder implements Serializable {

	/**
	 * 订单id
	 * @mbg.generated
	 */
	private Long id;
	/**
	 * 服务器id，不分服不需要
	 * @mbg.generated
	 */
	private String gameServerId;
	/**
	 * 渠道编号
	 * @mbg.generated
	 */
	private String channelCode;
	/**
	 * 渠道名称
	 * @mbg.generated
	 */
	private String channelLabel;
	/**
	 * passport uid
	 * @mbg.generated
	 */
	private Long userId;
	/**
	 * 某服务器里的玩家唯一id
	 * @mbg.generated
	 */
	private Long playerId;
	/**
	 * 例如微信的openid
	 * @mbg.generated
	 */
	private String thirdUid;
	/**
	 * 环境配置 0：现网环境（也叫正式环境）1：沙箱环境
	 * @mbg.generated
	 */
	private Byte env;
	/**
	 * 1未支付 2已支付
	 * @mbg.generated
	 */
	private Byte payState;
	/**
	 * 是否已发货
	 * @mbg.generated
	 */
	private Boolean isDeliver;
	/**
	 * 创建日期
	 * @mbg.generated
	 */
	private String createDate;
	/**
	 * @mbg.generated
	 */
	private String createTime;
	/**
	 * 支付日期
	 * @mbg.generated
	 */
	private String payDate;
	/**
	 * @mbg.generated
	 */
	private String payTime;
	/**
	 * 完成日期
	 * @mbg.generated
	 */
	private String completeDate;
	/**
	 * @mbg.generated
	 */
	private String completeTime;
	/**
	 * 调试信息，记录最近的操作描述
	 * @mbg.generated
	 */
	private String debugText;
	/**
	 * 第三方订单号
	 * @mbg.generated
	 */
	private String thirdOrderId;
	/**
	 * 价格分
	 * @mbg.generated
	 */
	private Integer price;
	/**
	 * 物品名称
	 * @mbg.generated
	 */
	private String itemName;
	/**
	 * 玩家支付成功后的回调
	 * @mbg.generated
	 */
	private String callback;
	/**
	 * @mbg.generated
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * @mbg.generated
	 */
	public Long getId() {
		return id;
	}

	/**
	 * @mbg.generated
	 */
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * @mbg.generated
	 */
	public String getGameServerId() {
		return gameServerId;
	}

	/**
	 * @mbg.generated
	 */
	public void setGameServerId(String gameServerId) {
		this.gameServerId = gameServerId;
	}

	/**
	 * @mbg.generated
	 */
	public String getChannelCode() {
		return channelCode;
	}

	/**
	 * @mbg.generated
	 */
	public void setChannelCode(String channelCode) {
		this.channelCode = channelCode;
	}

	/**
	 * @mbg.generated
	 */
	public String getChannelLabel() {
		return channelLabel;
	}

	/**
	 * @mbg.generated
	 */
	public void setChannelLabel(String channelLabel) {
		this.channelLabel = channelLabel;
	}

	/**
	 * @mbg.generated
	 */
	public Long getUserId() {
		return userId;
	}

	/**
	 * @mbg.generated
	 */
	public void setUserId(Long userId) {
		this.userId = userId;
	}

	/**
	 * @mbg.generated
	 */
	public Long getPlayerId() {
		return playerId;
	}

	/**
	 * @mbg.generated
	 */
	public void setPlayerId(Long playerId) {
		this.playerId = playerId;
	}

	/**
	 * @mbg.generated
	 */
	public String getThirdUid() {
		return thirdUid;
	}

	/**
	 * @mbg.generated
	 */
	public void setThirdUid(String thirdUid) {
		this.thirdUid = thirdUid;
	}

	/**
	 * @mbg.generated
	 */
	public Byte getEnv() {
		return env;
	}

	/**
	 * @mbg.generated
	 */
	public void setEnv(Byte env) {
		this.env = env;
	}

	/**
	 * @mbg.generated
	 */
	public Byte getPayState() {
		return payState;
	}

	/**
	 * @mbg.generated
	 */
	public void setPayState(Byte payState) {
		this.payState = payState;
	}

	/**
	 * @mbg.generated
	 */
	public Boolean getIsDeliver() {
		return isDeliver;
	}

	/**
	 * @mbg.generated
	 */
	public void setIsDeliver(Boolean isDeliver) {
		this.isDeliver = isDeliver;
	}

	/**
	 * @mbg.generated
	 */
	public String getCreateDate() {
		return createDate;
	}

	/**
	 * @mbg.generated
	 */
	public void setCreateDate(String createDate) {
		this.createDate = createDate;
	}

	/**
	 * @mbg.generated
	 */
	public String getCreateTime() {
		return createTime;
	}

	/**
	 * @mbg.generated
	 */
	public void setCreateTime(String createTime) {
		this.createTime = createTime;
	}

	/**
	 * @mbg.generated
	 */
	public String getPayDate() {
		return payDate;
	}

	/**
	 * @mbg.generated
	 */
	public void setPayDate(String payDate) {
		this.payDate = payDate;
	}

	/**
	 * @mbg.generated
	 */
	public String getPayTime() {
		return payTime;
	}

	/**
	 * @mbg.generated
	 */
	public void setPayTime(String payTime) {
		this.payTime = payTime;
	}

	/**
	 * @mbg.generated
	 */
	public String getCompleteDate() {
		return completeDate;
	}

	/**
	 * @mbg.generated
	 */
	public void setCompleteDate(String completeDate) {
		this.completeDate = completeDate;
	}

	/**
	 * @mbg.generated
	 */
	public String getCompleteTime() {
		return completeTime;
	}

	/**
	 * @mbg.generated
	 */
	public void setCompleteTime(String completeTime) {
		this.completeTime = completeTime;
	}

	/**
	 * @mbg.generated
	 */
	public String getDebugText() {
		return debugText;
	}

	/**
	 * @mbg.generated
	 */
	public void setDebugText(String debugText) {
		this.debugText = debugText;
	}

	/**
	 * @mbg.generated
	 */
	public String getThirdOrderId() {
		return thirdOrderId;
	}

	/**
	 * @mbg.generated
	 */
	public void setThirdOrderId(String thirdOrderId) {
		this.thirdOrderId = thirdOrderId;
	}

	/**
	 * @mbg.generated
	 */
	public Integer getPrice() {
		return price;
	}

	/**
	 * @mbg.generated
	 */
	public void setPrice(Integer price) {
		this.price = price;
	}

	/**
	 * @mbg.generated
	 */
	public String getItemName() {
		return itemName;
	}

	/**
	 * @mbg.generated
	 */
	public void setItemName(String itemName) {
		this.itemName = itemName;
	}

	/**
	 * @mbg.generated
	 */
	public String getCallback() {
		return callback;
	}

	/**
	 * @mbg.generated
	 */
	public void setCallback(String callback) {
		this.callback = callback;
	}

	@Override
	public String toString() {
		return "PayOrder{" +
				"id=" + id +
				", gameServerId='" + gameServerId + '\'' +
				", channelCode='" + channelCode + '\'' +
				", channelLabel='" + channelLabel + '\'' +
				", userId=" + userId +
				", playerId=" + playerId +
				", thirdUid='" + thirdUid + '\'' +
				", env=" + env +
				", payState=" + payState +
				", isDeliver=" + isDeliver +
				", createDate='" + createDate + '\'' +
				", createTime='" + createTime + '\'' +
				", payDate='" + payDate + '\'' +
				", payTime='" + payTime + '\'' +
				", completeDate='" + completeDate + '\'' +
				", completeTime='" + completeTime + '\'' +
				", debugText='" + debugText + '\'' +
				", thirdOrderId='" + thirdOrderId + '\'' +
				", price=" + price +
				", itemName='" + itemName + '\'' +
				", callback='" + callback + '\'' +
				'}';
	}
}