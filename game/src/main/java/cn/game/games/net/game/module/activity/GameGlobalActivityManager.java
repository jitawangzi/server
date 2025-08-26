package cn.game.games.net.game.module.activity;

import cn.game.protocol.generated.config.ActivityConfig;

/**    
 * Game中的全体活动，只是控制活动开启、关闭等，不保存活动数据
 * 2025年8月25日 10:22:51
 * @author SYQ
 */
public class GameGlobalActivityManager extends GlobalActivityManager{

	
	@Override
	protected boolean canOpen(ActivityConfig config) {
		return super.canOpen(config) && !config.isCross;
	}
	
	@Override
	protected void afterActivityOpen(ActivityBase activity) {
	}
	
}
