package cn.game.games.cache.op.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import cn.game.games.cache.entity.User;
import cn.game.games.cache.entity.UserTag;
import cn.game.games.cache.op.face.IUserOp;
import cn.game.games.core.BasePlayerModule;
import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.GameEvent;
import cn.game.protocol.protobuf.PlayerMsg.PlayerAllInfo.Builder;

public class UserOp extends BasePlayerModule implements IUserOp {

	private List<Integer> tagIds;
	private User user;

	@Override
	public void init() {
		tagIds = new ArrayList<>();
	}

	@Override
	public int initLoadData(long uid, User user, List<UserTag> tags) {
		if (user == null) {
			user = new User();
			user.setId(uid);
			user.setRmb(0);
//			DAO.insert(UserMapper.class, user);
		}
		this.user = user;
		for (UserTag userTag : tags) {
			this.tagIds.add(userTag.getTagId());
		}
		return 0;
	}

	@Override
	public List<Integer> getTagIds() {
		return tagIds;
	}
	@Override
	public User getUser() {
		return user;
	}

	@Override
	public EventTypeEnum[] getEventTypes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void handleEvent(GameEvent event) {
		// TODO Auto-generated method stub

	}

	@Override
	public Class<?>[] defaultDbMapperClass() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void initFromDb(ListIterator<?> iterator) {
		// TODO Auto-generated method stub

	}
	@Override
	public void initFromDbAfter() {

	};
	@Override
	public void buildPlayerAllInfo(Builder builder) {
		// TODO Auto-generated method stub

	}
	
}
