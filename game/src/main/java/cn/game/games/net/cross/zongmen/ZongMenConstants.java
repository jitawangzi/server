package cn.game.games.net.cross.zongmen;

import java.util.List;

/**
 * @ClassName ZongMenConstants
 *
 * @description: 宗门常量枚举相关
 * @author: ly
 * @create: 2025-02-05 15:51 @Version 1.0
 */
public class ZongMenConstants {

    /** 定时执行存储宗门时间戳 */
    public static final long SAVE_ZONG_MEN_DATA_PERIOD_TIMER = 30;
    /**5 分钟存储一次 **/
    public static final long SAVE_ZONG_MEN_DATA_TIMER = 5*60*1000L;

    /** 宗门职位 宗主 */
    public static final int ZONG_MEN_POSITION_ZONG_ZHU = 1;
    /** 宗门职位 长老 */
    public static final int ZONG_MEN_POSITION_ZHANG_LAO = 2;
    /** 宗门职位 帮众 */
    public static final int ZONG_MEN_POSITION_BANG_ZHONG = 3;

    public enum ZongMenEvenType {
        CROSS_DAY(0,"跨天"),
        JOIN_ZONG_MEN(1,"加入宗门"),
        ZONG_MEN_CREATE(2, "创建宗门"),
        ZONG_MEN_LEVEL_UP(3,"宗门升级"),
        ZONG_MEN_POSITION_CHANGE(4,"权限变动"),
        QUIT_ZONG_MEN(5,"退出宗门")
        ;
        private int id;
        private String desc;

        ZongMenEvenType(int id, String desc) {
            this.id = id;
            this.desc = desc;
        }

        public int getId() {
            return id;
        }

        public String getDesc() {
            return desc;
        }
    }

    public   interface ZongMenEventHandler {
        ZongMenEvenType[] getRegisterEvent();
        void handleEventType(ZongMenEvenType type,ZongMenInfo info,Object ... params);
    }

}
