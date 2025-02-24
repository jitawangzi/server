package cn.game.core.sdk;

/**
 * 支付通知数据实体类，用于封装通过解码 receipt 得到的订单信息。
 *
 * 对应文档中传递的 receipt 字段，格式示例：
 *
 * {
 *    "orderId": "订单编号",
 *    "goodsRegisterId": "商品注册ID",
 *    "goodsNumber": "产品实际数量",
 *    "goodsPrice": 价格,
 *    "areaId": "分区ID",
 *    "areaName": "分区名称",
 *    "channelId": "渠道标识",
 *    "roleId": "角色ID",
 *    "roleName": "角色名称",
 *    "appDate": "时间戳yyyyMMddHHmmss",
 *    "userid": "用户唯一标识",
 *    "goodsName": "产品名称",
 *    "orgOrderId": "原始订单id",
 *    "startDate": "生效时间",
 *    "endDate": "到期时间",
 *    "gameGoodsId": "游戏商品ID",
 *    "daysLasted": "已付费订阅天数",
 *    "isAd": false
 * }
 */
public class PaymentNotification {
	private String orderId;
	private String goodsRegisterId;
	private String goodsNumber;
	private double goodsPrice;
	private String areaId;
	private String areaName;
	private String channelId;
	private String roleId;
	private String roleName;
	private String appDate;
	private String userid;
	private String goodsName;
	private String orgOrderId;
	private String startDate;
	private String endDate;
	private String gameGoodsId;
	private String daysLasted;
	private boolean isAd;
	// 额外保存 pushInfo 信息（如有需要）
	private String pushInfo;

	// 下面是 getter 和 setter 方法
	public String getOrderId() {
		return orderId;
	}

	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}

	public String getGoodsRegisterId() {
		return goodsRegisterId;
	}

	public void setGoodsRegisterId(String goodsRegisterId) {
		this.goodsRegisterId = goodsRegisterId;
	}

	public String getGoodsNumber() {
		return goodsNumber;
	}

	public void setGoodsNumber(String goodsNumber) {
		this.goodsNumber = goodsNumber;
	}

	public double getGoodsPrice() {
		return goodsPrice;
	}

	public void setGoodsPrice(double goodsPrice) {
		this.goodsPrice = goodsPrice;
	}

	public String getAreaId() {
		return areaId;
	}

	public void setAreaId(String areaId) {
		this.areaId = areaId;
	}

	public String getAreaName() {
		return areaName;
	}

	public void setAreaName(String areaName) {
		this.areaName = areaName;
	}

	public String getChannelId() {
		return channelId;
	}

	public void setChannelId(String channelId) {
		this.channelId = channelId;
	}

	public String getRoleId() {
		return roleId;
	}

	public void setRoleId(String roleId) {
		this.roleId = roleId;
	}

	public String getRoleName() {
		return roleName;
	}

	public void setRoleName(String roleName) {
		this.roleName = roleName;
	}

	public String getAppDate() {
		return appDate;
	}

	public void setAppDate(String appDate) {
		this.appDate = appDate;
	}

	public String getUserid() {
		return userid;
	}

	public void setUserid(String userid) {
		this.userid = userid;
	}

	public String getGoodsName() {
		return goodsName;
	}

	public void setGoodsName(String goodsName) {
		this.goodsName = goodsName;
	}

	public String getOrgOrderId() {
		return orgOrderId;
	}

	public void setOrgOrderId(String orgOrderId) {
		this.orgOrderId = orgOrderId;
	}

	public String getStartDate() {
		return startDate;
	}

	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}

	public String getEndDate() {
		return endDate;
	}

	public void setEndDate(String endDate) {
		this.endDate = endDate;
	}

	public String getGameGoodsId() {
		return gameGoodsId;
	}

	public void setGameGoodsId(String gameGoodsId) {
		this.gameGoodsId = gameGoodsId;
	}

	public String getDaysLasted() {
		return daysLasted;
	}

	public void setDaysLasted(String daysLasted) {
		this.daysLasted = daysLasted;
	}

	public boolean isAd() {
		return isAd;
	}

	public void setAd(boolean ad) {
		isAd = ad;
	}

	public String getPushInfo() {
		return pushInfo;
	}

	public void setPushInfo(String pushInfo) {
		this.pushInfo = pushInfo;
	}
}