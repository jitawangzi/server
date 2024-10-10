package cn.game.login.net.clientpacket.vertx.wechat.combineModule;

import com.google.gson.annotations.SerializedName;

/**
 * @ClassName ReqSceneInfo
 *
 * @description:
 * @author: ly
 * @create: 2024-09-27 15:06 @Version 1.0
 */
public class ReqSceneInfo {
  /** device_id 选填 string(16) 【商户端设备号】 终端设备号(门店号或收银设备ID) */
  @SerializedName("device_id")
  private String deviceId;

  /** payer_client_ip 必填 string(45) 【用户终端IP】 用户端实际IP，支持IPv4和IPv6两种格式的IP地址。 IP获取请参考获取用户IP指引 */
  @SerializedName("payer_client_ip")
  private String payerClientIp;

  public String getDeviceId() {
    return deviceId;
  }

  public void setDeviceId(String deviceId) {
    this.deviceId = deviceId;
  }

  public String getPayerClientIp() {
    return payerClientIp;
  }

  public void setPayerClientIp(String payerClientIp) {
    this.payerClientIp = payerClientIp;
  }

  @Override
  public String toString() {
    return "ReqSceneInfo{"
        + "deviceId='"
        + deviceId
        + '\''
        + ", payerClientIp='"
        + payerClientIp
        + '\''
        + '}';
  }
}
