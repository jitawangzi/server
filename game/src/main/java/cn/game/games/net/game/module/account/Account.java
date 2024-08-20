package cn.game.games.net.game.module.account;

import cn.game.protocol.protobuf.PlayerMsg.PlayerLoginRequest_01000001;

public class Account {

    public static final String DEVICE_TYPE_ANDROID = "android";
    public static final String DEVICE_TYPE_IOS = "ios";

	private int accountType;// 账号类型,作用未知
	public String accountId;// 账号
    public final String payChannel;//充值渠道
	public String deviceId;// 设备id
    public final String version;//客户端版本
    public final String adChannel;//推广渠道

    private String roleInfo;
	public String sdkDeviceId;// 设备唯一号
    public final String sdkVersion;//设备唯一号
	public final int platform;// 平台标示： 1 IOS APP ，2 安卓 APP ，3 IOS小游戏，4 安卓小游戏，5 window 微信小游戏，6 mac微信小游戏
    public final String sdkPayChannel;//sdk充值渠道
	public final String system;// 系统: 小游戏定死“system”
	public final String clue_token;//

	public Account(PlayerLoginRequest_01000001 req) {
//        this.accountType = req.getAccountType();
//		this.accountId = req.getAccountId();
		this.payChannel = req.getSdkPayChannel();
//		this.deviceId = req.getDeviceId();

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
