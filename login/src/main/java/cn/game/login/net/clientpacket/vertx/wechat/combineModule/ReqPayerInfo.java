package cn.game.login.net.clientpacket.vertx.wechat.combineModule;

import com.google.gson.annotations.SerializedName;

/**
 * @ClassName ReqPayerInfo
 *
 * @description:
 * @author: ly
 * @create: 2024-09-27 15:11 @Version 1.0
 */
public class ReqPayerInfo {
    /**openid
            选填
    string(128)
【用户标识】 使用合单Appid获取的对应用户Openid。是用户在商户Appid下的唯一标识。*/
    @SerializedName("openid")
    private String openId;

    public String getOpenId() {
        return openId;
    }

    public void setOpenId(String openId) {
        this.openId = openId;
    }

    @Override
    public String toString() {
        return "ReqPayerInfo{" +
                "openId='" + openId + '\'' +
                '}';
    }
}
