package cn.game.login.net.clientpacket.vertx.wechat.combineModule;

import com.google.gson.annotations.SerializedName;

/**
 * @ClassName ReqAmountInfo
 *
 * @description:
 * @author: ly
 * @create: 2024-09-27 15:23 @Version 1.0
 */
public class ReqAmountInfo {
  /** total_amount 必填 integer 【标价金额】 子单金额，单位为分 */
  @SerializedName("total_amount")
  private int totalAmount;

  /** currency 必填 string(8) 【标价币种】 符合ISO 4217标准的三位字母代码，人民币：CNY */
  @SerializedName("currency")
  private String currency = "CNY";

  public int getTotalAmount() {
    return totalAmount;
  }

  public void setTotalAmount(int totalAmount) {
    this.totalAmount = totalAmount;
  }

  public String getCurrency() {
    return currency;
  }

  public void setCurrency(String currency) {
    this.currency = currency;
  }

  @Override
  public String toString() {
    return "ReqAmountInfo{" + "totalAmount=" + totalAmount + ", currency='" + currency + '\'' + '}';
  }
}
