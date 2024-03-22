package cn.game.games.cache.op.impl;

import java.util.ListIterator;

import cn.game.games.cache.entity.HomePage;
import cn.game.games.core.BasePlayerModule;
import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.GameEvent;
import cn.game.games.net.data.mapper.HomePageMapper;
import cn.game.games.net.game.constant.MapperConstant;
import cn.game.games.util.DAO;
import cn.game.protocol.generated.config.OldGlobalConst;
import cn.game.protocol.protobuf.PlayerMsg.PlayerAllInfo.Builder;

public class HomePageOp extends BasePlayerModule {
	private HomePage homePage;

	@Override
	public void init() {
	}

	public void initLoadData(HomePage homePage) {
		if (homePage != null) {
			this.homePage = homePage;
		}
	}

	/**
	 * 设置看板娘ID，第一次设置才插库，如增加其他字段，需对应增加逻辑
	 * 
	 * @param roleId
	 */
	public void setPosterGirl(int roleId) {
		if (homePage == null) {
			homePage = HomePage.valueOf(playerId, roleId);
			insert(homePage);
			return;
		}
		if (homePage.getPosterGirlId() == roleId) {
			return;
		}
		homePage.setPosterGirlId(roleId);
		update(homePage);
	}

	private void insert(HomePage homePage) {
		DAO.execute(HomePageMapper.class, MapperConstant.insert,
				homePage);
	}

	private void update(HomePage homePage) {
		DAO.execute(HomePageMapper.class,
				MapperConstant.updateByPrimaryKey, homePage);

	}

	public int getPosterGirlId() {
		if (homePage != null) {
			return homePage.getPosterGirlId();
		}
		// 看板娘初始角色
		int defaultPosterGirlId = OldGlobalConst.initialRole;
		return defaultPosterGirlId;
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
	public void buildPlayerAllInfo(Builder builder) {
		// TODO Auto-generated method stub

	}

}
