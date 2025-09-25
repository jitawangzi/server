package cn.game.games.net.game.module.account;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.gson.JsonObject;

import cn.game.protocol.protobuf.PlayerMsg.PlayerLoginRequest_01000001;
import cn.game.util.JsonUtil;

/**    
 * 字段不要改，不要删
 * 2025年1月14日 11:08:50
 * @author SYQ
 */
public class Account {

    public static final String DEVICE_TYPE_ANDROID = "android";
    public static final String DEVICE_TYPE_IOS = "ios";

	@JsonIgnore
	private int accountType;// 账号类型,作用未知
	public String accountId;// 账号
	public String payChannel;// 充值渠道
	public String deviceId;// 设备id
	public String version;// 客户端版本
	public String adChannel;// 推广渠道

	@JsonIgnore
    private String roleInfo;
	@JsonIgnore
	public String sdkDeviceId;// 设备唯一号
	@JsonIgnore
	public String sdkVersion;// 设备唯一号
	public int platform;// 平台标示： 1 IOS APP ，2 安卓 APP ，3 IOS小游戏，4 安卓小游戏，5 window 微信小游戏，6 mac微信小游戏
	public String sdkPayChannel;// sdk充值渠道
	public String system;// 系统: 小游戏定死“system”
	@JsonIgnore
	public String clue_token;//
    /**邀请者id  不存在则为 0*/
	@JsonIgnore
    public  long invitePid;

	public Account(PlayerLoginRequest_01000001 req) {
//        this.accountType = req.getAccountType();
		this.accountId = req.getAccountId();
		this.payChannel = req.getSdkPayChannel();
		this.deviceId = req.getDeviceId();

		this.version = req.getVerstion();
        this.adChannel = req.getAdChannel();
		// 这里先写死
//		this.sdkDeviceId = this.deviceId;

//        this.sdkDeviceId = req.getSdkDeviceId();
        this.sdkVersion = req.getSdkVersion();
        this.platform = req.getPlatform();
        this.sdkPayChannel = req.getSdkPayChannel();
		this.system = req.getSystem();
		this.clue_token = req.getClueToken();
        if (req.getClueToken() != null &&  !req.getClueToken().isEmpty()){
            try {
                JsonObject tokenJson = JsonUtil.parserJson(req.getClueToken());
                if (tokenJson.has("query") && tokenJson.getAsJsonObject("query").has("friendID")){
                    this.invitePid = tokenJson.getAsJsonObject("query").get("friendID").getAsLong();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

	public Account() {

	}

    public long getInvitePid() {
        return invitePid;
    }

    public int getAccountType() {
        return accountType;
    }

    public String getRoleInfo() {
        return roleInfo;
    }

    public void setRoleInfo(String roleInfo) {
        this.roleInfo = roleInfo;
    }

	public int getPlatform() {
        return platform;
    }

	public String getClue_token() {
		return clue_token;
	}

}
