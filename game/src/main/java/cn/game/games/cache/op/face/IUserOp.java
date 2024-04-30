package cn.game.games.cache.op.face;

import java.util.List;

import cn.game.games.cache.entity.User;
import cn.game.games.cache.entity.UserTag;

/**
 * 所有文档通用的数据
 * 
 * @date 2021年9月22日 下午2:19:27
 * @author SYQ
 */
public interface IUserOp {

	public int initLoadData(long uid, User user, List<UserTag> tags);

	List<Integer> getTagIds();

	User getUser();

}
