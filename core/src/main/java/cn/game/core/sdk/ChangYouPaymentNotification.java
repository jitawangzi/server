package cn.game.core.sdk;

import java.util.Map;

import com.alibaba.druid.support.json.JSONUtils;
import com.fasterxml.jackson.annotation.JsonProperty;

import cn.game.util.JsonUtil;
import cn.game.util.StrUtil;

/**
 * 支付通知数据实体类，用于封装通过解码 receipt 得到的订单信息。
 */
public class ChangYouPaymentNotification {
	private ChangYouReceipt receipt;
	private ChangYouPushInfo pushInfo;
	private transient String receiptJson;
	private transient String receiptJsonOriginal;

	public ChangYouReceipt getReceipt() {
		return receipt;
	}

	public void setReceipt(ChangYouReceipt receipt) {
		this.receipt = receipt;
	}

	public ChangYouPushInfo getPushInfo() {
		return pushInfo;
	}

	public void setPushInfo(ChangYouPushInfo pushInfo) {
		this.pushInfo = pushInfo;
	}

	public String getReceiptJson() {
		return receiptJson;
	}

	public void setReceiptJson(String receiptJson) {
		this.receiptJson = receiptJson;
	}

	public String getReceiptJsonOriginal() {
		return receiptJsonOriginal;
	}

	public void setReceiptJsonOriginal(String receiptJsonOriginal) {
		this.receiptJsonOriginal = receiptJsonOriginal;
	}

	/**
	
	对应文档中传递的 receipt 字段，格式示例：**
	{
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
	public static class ChangYouReceipt {
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
		@JsonProperty("daysLasted")
		private String daysLasted = "";
		@JsonProperty("isAd")
		private boolean isAd;
		// 新增必要字段（若需要）
		@JsonProperty("goodsType")
		private int goodsType;

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

		public static void main(String[] args) {

			String jsonString = "{\"endDate\":\"\",\"orderId\":\"debug25031011595632489820\",\"roleId\":\"240200198\",\"appDate\":\"20250310120005\",\"goodsRegisterId\":\"com.cy.gyxy.yuanbao1\",\"userid\":\"3054001190093600\",\"goodsType\":0,\"areaId\":\"1\",\"areaName\":\" \",\"orgOrderId\":\"\",\"goodsNumber\":\"1\",\"goodsPrice\":0.01,\"roleName\":\"安宁象力士\",\"isAd\":false,\"goodsName\":\"元宝\",\"channelId\":\"4001\",\"gameGoodsId\":\"com.cy.gyxy.yuanbao1\",\"startDate\":\"\"}";
			ChangYouReceipt object = JsonUtil.parseObject(jsonString, ChangYouReceipt.class);
			System.out.println(object.getAppDate());

			ChangYouPushInfo receipt = JsonUtil.parseObject(jsonString, ChangYouPushInfo.class);
//			ChangYouPushInfo pushInfo = JsonUtil.parseObject(json[1], ChangYouPushInfo.class);
			System.out.println(receipt);

		}
	}

	public static class ChangYouPushInfo {
		private long gameOrderId;

		public long getGameOrderId() {
			return gameOrderId;
		}

		public void setGameOrderId(long gameOrderId) {
			this.gameOrderId = gameOrderId;
		}
		public static ChangYouPushInfo fromKV(String kvString) {
			Map<String, String> kv = StrUtil.parseKv(kvString, false); 
	        return JsonUtil.convertValue(kv, ChangYouPushInfo.class) ; 
		}
	}

}