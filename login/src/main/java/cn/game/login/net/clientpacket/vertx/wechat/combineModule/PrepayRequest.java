package cn.game.login.net.clientpacket.vertx.wechat.combineModule;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName PrepayRequest
 *
 * @description:
 * @author: ly
 * @create: 2024-09-27 14:53 @Version 1.0
 */
public class PrepayRequest {
  /** 【合单商户Appid】 合单发起方的Appid 必填 * */
  @SerializedName("combine_appid")
  private String combineAppid;

  /** 【合单商户订单号】 合单支付总订单号，最短2个字符，最长32个字符，只能是数字、大小写字母，以及_-|* ，且在同一个商户号下唯一 必填* */
  @SerializedName("combine_out_trade_no")
  private String combineOutTradeNo;

  /** 【合单商户号】 合单发起方商户号 必填* */
  @SerializedName("combine_mchid")
  private String combineMchid;

  /** scene_info 选填 ReqSceneInfo 【场景信息】 场景信息 属性 */
  @SerializedName("scene_info")
  private ReqSceneInfo sceneInfo;

  /** combine_payer_info 必填 ReqPayerInfo 【支付者】 支付者信息，可以指定用户的实名信息，必须指定用户的openid或sub_openid。 属性 */
  @SerializedName("combine_payer_info")
  private ReqPayerInfo combinePayerInfo;

  /**
   * time_start 选填 string 【交易起始时间】
   * 订单生效时间，按照rfc3339格式，格式为yyyy-MM-DDTHH:mm:ss+TIMEZONE，yyyy-MM-DD表示年月日，T出现在字符串中，表示time元素的开头，HH:mm:ss表示时分秒，TIMEZONE表示时区（+08:00表示东八区时间，领先UTC8小时，即北京时间）如
   * 2018-06-08T10:34:56+08:00 代表，北京时间2018年06月08日10时34分56秒。
   */
  @SerializedName("time_start")
  private String timeStart;

  /**
   * time_expire 选填 string 【交易结束时间】
   * 订单失效时间，按照rfc3339格式，格式为yyyy-MM-DDTHH:mm:ss+TIMEZONE，yyyy-MM-DD表示年月日，T出现在字符串中，表示time元素的开头，HH:mm:ss表示时分秒，TIMEZONE表示时区（+08:00表示东八区时间，领先UTC8小时，即北京时间）如
   * 2018-06-08T10:34:56+08:00 代表，北京时间2018年06月08日10时34分56秒。
   */
  @SerializedName("time_expire")
  private String timeExpire;

  /** notify_url 必填 string(255) 【通知地址】 接收微信支付异步通知回调地址，通知url必须为直接可访问的url，不能携带参数。必须使用https协议。 */
  @SerializedName("notify_url")
  private String notifyUrl;

  /** sub_orders 必填 array[ReqSubOrderCompatible] 【子单信息】 子单列表，最多支持子单条数：50。 */
  @SerializedName("sub_orders")
  private List<ReqSubOrderCompatible> subOrders;

 public String getCombineAppid() {
  return combineAppid;
 }

 public void setCombineAppid(String combineAppid) {
  this.combineAppid = combineAppid;
 }

 public String getCombineOutTradeNo() {
  return combineOutTradeNo;
 }

 public void setCombineOutTradeNo(String combineOutTradeNo) {
  this.combineOutTradeNo = combineOutTradeNo;
 }

 public String getCombineMchid() {
  return combineMchid;
 }

 public void setCombineMchid(String combineMchid) {
  this.combineMchid = combineMchid;
 }

 public ReqSceneInfo getSceneInfo() {
  return sceneInfo;
 }

 public void setSceneInfo(ReqSceneInfo sceneInfo) {
  this.sceneInfo = sceneInfo;
 }

 public ReqPayerInfo getCombinePayerInfo() {
  return combinePayerInfo;
 }

 public void setCombinePayerInfo(ReqPayerInfo combinePayerInfo) {
  this.combinePayerInfo = combinePayerInfo;
 }

 public String getTimeStart() {
  return timeStart;
 }

 public void setTimeStart(String timeStart) {
  this.timeStart = timeStart;
 }

 public String getTimeExpire() {
  return timeExpire;
 }

 public void setTimeExpire(String timeExpire) {
  this.timeExpire = timeExpire;
 }

 public String getNotifyUrl() {
  return notifyUrl;
 }

 public void setNotifyUrl(String notifyUrl) {
  this.notifyUrl = notifyUrl;
 }

 public List<ReqSubOrderCompatible> getSubOrders() {
  return subOrders;
 }

 public void setSubOrders(List<ReqSubOrderCompatible> subOrders) {
  this.subOrders = subOrders;
 }

 public void addSubOrders(ReqSubOrderCompatible subOrderCompatible){
     if (subOrders == null){
         subOrders = new ArrayList<>();
     }
     subOrders.add(subOrderCompatible);
 }

 @Override
 public String toString() {
  return "PrepayRequest{" +
          "combineAppid='" + combineAppid + '\'' +
          ", combineOutTradeNo='" + combineOutTradeNo + '\'' +
          ", combineMchid='" + combineMchid + '\'' +
          ", sceneInfo=" + sceneInfo +
          ", combinePayerInfo=" + combinePayerInfo +
          ", timeStart='" + timeStart + '\'' +
          ", timeExpire='" + timeExpire + '\'' +
          ", notifyUrl='" + notifyUrl + '\'' +
          ", subOrders=" + subOrders +
          '}';
 }
}
