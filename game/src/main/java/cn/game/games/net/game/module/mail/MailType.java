package cn.game.games.net.game.module.mail;

public enum MailType {
    Personal((byte)0),//	/** 邮件类型， 公告邮件*/
    Notice((byte)1) ,	/** 邮件类型，系统自动发的邮件 */
    System((byte)2),
    DASHENG_XUN_SHAN((byte)3);

   private byte value;
    private MailType(byte value) {
        this.value = value;
    }
    public byte getValue() {
        return value;
    }
    public static MailType valueOf(byte value) {
        for (MailType type : MailType.values()) {
            if (type.getValue() == value) {
                return type;
            }
        }
        return null;
    }
}

