package cn.game.games.net.cross.guild;

import java.util.List;

/**
 * @ClassName GuildConstants
 *
 * @description: 公会常量枚举相关
 * @author: ly
 * @create: 2025-02-05 15:51 @Version 1.0
 */
public class GuildConstants {

	/** 定时执行存储公会时间戳 */
	public static final long SAVE_ZONG_MEN_DATA_PERIOD_TIMER = 30;
	/**5 分钟存储一次 **/
	public static final long SAVE_ZONG_MEN_DATA_TIMER = 5 * 60 * 1000L;

	/** 公会职位 宗主 */
	public static final int ZONG_MEN_POSITION_ZONG_ZHU = 1;
	/** 公会职位 长老、副帮主 */
	public static final int ZONG_MEN_POSITION_ZHANG_LAO = 2;
	/** 公会职位 精英 */
	public static final int ZONG_MEN_POSITION_JING_YING = 3;
	/** 公会职位 帮众 */
	public static final int ZONG_MEN_POSITION_BANG_ZHONG = 4;

	public enum GuildEvenType {
		CROSS_DAY(0, "跨天"), JOIN_ZONG_MEN(1, "加入公会"), ZONG_MEN_CREATE(2, "创建公会"), ZONG_MEN_LEVEL_UP(3, "公会升级"),
		ZONG_MEN_POSITION_CHANGE(4, "权限变动"), QUIT_ZONG_MEN(5, "退出公会"), CHANGE_ZONG_MEN_NAME(6, "修改公会名称"),
		CHANGE_ZONG_MEN_NOTICE(7, "修改公会公告"), CHANGE_ZONG_MEN_DECLARATION(8, "修改公会宣言"), ZONG_MEN_KICK_MEMBER(9, "踢人"),
		CROSS_WEEK(0, "跨周"),
		
		;

		private int id;
		private String desc;

		GuildEvenType(int id, String desc) {
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

	public interface GuildEventHandler {
		GuildEvenType[] getRegisterEvent();

		void handleEventType(GuildEvenType type, Guild info, Object... params);
	}

}
