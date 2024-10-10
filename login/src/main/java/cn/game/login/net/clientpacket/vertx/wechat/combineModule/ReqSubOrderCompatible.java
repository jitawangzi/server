package cn.game.login.net.clientpacket.vertx.wechat.combineModule;

import com.google.gson.annotations.SerializedName;

/**
 * @ClassName ReqSubOrderCompatible
 *
 * @description:
 * @author: ly
 * @create: 2024-09-27 15:17 @Version 1.0
 */
public class ReqSubOrderCompatible {
  /** mchid 必填 string(32) 【子单商户号】 子单发起方商户号，与发起方Appid有绑定关系 */
  @SerializedName("mchid")
  private String mchId;

  /** attach 必填 string(128) 【附加数据】 附加数据，在查询API和支付通知中原样返回，可作为自定义参数使用。 */
  @SerializedName("attach")
  private String attach;

  /** out_trade_no 必填 string(32) 【子单商户订单号】 商户系统内部订单号，最短2个字符，最长32个字符，只能是数字、大小写字母_-|* ，且在同一个商户号下唯一。 */
  @SerializedName("out_trade_no")
  private String outTradeNo;

  /** detail 选填 string(6000) 【商品详情】 商品详细描述 */
  @SerializedName("detail")
  private String detail;

  /** description 必填 string(127) 【商品描述】 商品简单描述。需传入应用市场上的APP名字-实际商品名称，例如 天天爱消除-游戏充值 */
  @SerializedName("description")
  private String description;

  /** goods_tag 选填 string(32) 【订单优惠标记】 订单优惠标记，使用代金券或立减优惠功能时需要的参数，说明详见代金券或立减优惠 */
  @SerializedName("goods_tag")
  private String goodsTag;

  /** amount 必填 ReqAmountInfo 【订单金额】 订单金额 属性 */
  @SerializedName("amount")
  private ReqAmountInfo amount;

  public String getMchId() {
    return mchId;
  }

  public void setMchId(String mchId) {
    this.mchId = mchId;
  }

  public String getAttach() {
    return attach;
  }

  public void setAttach(String attach) {
    this.attach = attach;
  }

  public String getOutTradeNo() {
    return outTradeNo;
  }

  public void setOutTradeNo(String outTradeNo) {
    this.outTradeNo = outTradeNo;
  }

  public String getDetail() {
    return detail;
  }

  public void setDetail(String detail) {
    this.detail = detail;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getGoodsTag() {
    return goodsTag;
  }

  public void setGoodsTag(String goodsTag) {
    this.goodsTag = goodsTag;
  }

  public ReqAmountInfo getAmount() {
    return amount;
  }

  public void setAmount(ReqAmountInfo amount) {
    this.amount = amount;
  }

  @Override
  public String toString() {
    return "ReqSubOrderCompatible{" +
            "mchId='" + mchId + '\'' +
            ", attach='" + attach + '\'' +
            ", outTradeNo='" + outTradeNo + '\'' +
            ", detail='" + detail + '\'' +
            ", description='" + description + '\'' +
            ", goodsTag='" + goodsTag + '\'' +
            ", amount=" + amount +
            '}';
  }
}
