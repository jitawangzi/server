package cn.game.games.cache.op.impl;

import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import cn.game.core.base.ServerContext;
import cn.game.games.cache.entity.OfflineResourceAdd;
import cn.game.games.cache.entity.PlayerExt;
import cn.game.games.cache.entity.UnionApplication;
import cn.game.games.cache.op.face.IPlayerOp;
import cn.game.games.core.BasePlayerModule;
import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.GameEvent;
import cn.game.games.net.data.mapper.OfflineResourceAddMapper;
import cn.game.games.net.data.mapper.PlayerExtMapper;
import cn.game.games.net.data.mapper.UnionApplicationMapper;
import cn.game.games.net.game.constant.MapperConstant;
import cn.game.games.net.game.helper.PlayerHelper;
import cn.game.games.net.game.manager.PlayerManager;
import cn.game.games.util.DAO;
import cn.game.protocol.manual.ResourceConsumeEnum;
import cn.game.protocol.protobuf.PlayerMsg.PlayerAllInfo.Builder;

public class PlayerOp extends BasePlayerModule implements IPlayerOp {

	private transient EventTypeEnum[] events = new EventTypeEnum[] { EventTypeEnum.PLAYER_CREATE,
			EventTypeEnum.LoginFinish,
			EventTypeEnum.Reconnect };

	private Set<Long> unionApplicationIds;
	private Set<Integer> clues;
	private List<OfflineResourceAdd> offlineResourceAdds;

	@Override
	public void init() {
		unionApplicationIds = new HashSet<Long>();
		clues = new HashSet<>();
	}

	@Override
	public EventTypeEnum[] getEventTypes() {
		return events;
	}

	@Override
	public void handleEvent(GameEvent event) {
		switch (event.getType()) {
		case LoginFinish: {
			if (offlineResourceAdds != null) {
				for (OfflineResourceAdd offlineResourceAdd : offlineResourceAdds) {
					try {
						boolean add = offlineResourceAdd.getType();
						if (add) {
							PlayerHelper.addResources(playerId, offlineResourceAdd.getItemId(),
									offlineResourceAdd.getCount());
						} else {
							PlayerHelper.delResources(playerId, offlineResourceAdd.getItemId(),
									offlineResourceAdd.getCount(), ResourceConsumeEnum.GM);
						}

						DAO.delete(OfflineResourceAddMapper.class, offlineResourceAdd.getId());
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
			PlayerManager.getInstance().online(playerId, ServerContext.getInstance().getServerId());
			break;
		}
		case PLAYER_CREATE: {

			PlayerManager.getInstance().online(playerId, ServerContext.getInstance().getServerId());
			break;
		}
		case Reconnect: {
			player.setActive(true);
			break;
		}
		}
	}

	@Override
	public Class<?>[] defaultDbMapperClass() {
		return new Class[] { PlayerExtMapper.class, OfflineResourceAddMapper.class };
	}

	@Override
	public int initLoadData(Object... args) {

		List<Long> unionApplicationIds = (List<Long>) args[0];
		unionApplicationIds.addAll(unionApplicationIds);

		return 0;
	}

	protected void initFromDb(ListIterator<?> iterator) {
		List<PlayerExt> list = (List<PlayerExt>) iterator.next();
		PlayerExt playerExt = list.get(0);
		this.offlineResourceAdds = (List<OfflineResourceAdd>) iterator.next();

//		if (playerExt == null) {
//			playerExt = new PlayerExt();
//			playerExt.setPlayerId(playerId);
//			ObjUtil.setDefaultValue(playerExt);
//			DAO.insert(PlayerExtMapper.class, playerExt);
//		}
	}
	@Override
	public boolean addUnionApplication(long unionId) {
		boolean flag = unionApplicationIds.add(unionId);
		if (flag) {
			UnionApplication unionApplication = new UnionApplication();
			unionApplication.setPlayerId(playerId);
			unionApplication.setUnionId(unionId);
			DAO.execute(UnionApplicationMapper.class, MapperConstant.insert,
					unionApplication);
		}
		return flag;

	}

	@Override
	public boolean delUnionApplication(long unionId) {
		boolean flag = unionApplicationIds.remove(unionId);
		if (flag) {
//			UnionApplication unionApplication = new UnionApplication();
//			unionApplication.setPlayerId(playerId);
//			unionApplication.setUnionId(unionId);
			DAO.execute(UnionApplicationMapper.class, MapperConstant.deleteByPrimaryKey,
					new Object[] { playerId, unionId });
		}
		return flag;

	}

	@Override
	public Set<Integer> getAllClues() {
		return clues;
	}

	@Override
	public boolean isClueFinish(int id) {
		return clues.contains(id);
	}

	@Override
	public void buildPlayerAllInfo(Builder builder) {
		// TODO Auto-generated method stub

	}
}
