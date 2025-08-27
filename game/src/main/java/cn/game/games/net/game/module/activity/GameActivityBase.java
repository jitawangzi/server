package cn.game.games.net.game.module.activity;

import cn.game.core.base.ServerContext;
import cn.game.games.core.event.server.ServerEventHandler;

/**    
 * 这个是Game服务器级别的活动基类
 * 暂时这里不保存活动的具体数据，
 * 只是用来控制活动的开启、关闭等。 
 * 活动相关的数据，保存在 redis等地方
 * 
 * 2025年8月22日 17:59:14
 * @author SYQ
 */
public abstract class GameActivityBase extends ActivityBase implements ServerEventHandler {

	@Override
	public void init(int id, Object owner, boolean isNew) {
		ServerContext.getInstance().registerEventHandler(this);
		super.init(id, owner, isNew);
	}
}