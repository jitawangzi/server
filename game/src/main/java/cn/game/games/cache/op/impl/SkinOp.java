package cn.game.games.cache.op.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import cn.game.games.cache.entity.Skin;
import cn.game.games.cache.op.face.ISkinOp;
import cn.game.games.core.BasePlayerModule;
import cn.game.games.core.event.EventTypeEnum;
import cn.game.games.core.event.GameEvent;
import cn.game.games.net.data.mapper.SkinMapper;
import cn.game.games.net.game.constant.MapperConstant;
import cn.game.games.util.DAO;
import cn.game.protocol.protobuf.PlayerMsg.PlayerAllInfo.Builder;

public class SkinOp extends BasePlayerModule implements ISkinOp {

	private List<Integer> skins;

	@Override
	public void init() {
		skins = new ArrayList<Integer>();
	}
	@Override
	public void initLoadData(List<Skin> skins) {
		for (Skin skin : skins) {
			this.skins.add(skin.getSkin());
		}
	}

	@Override
	public boolean add(int skin) {
		if (!skins.contains(skin)) {
			skins.add(skin);
			Skin addSkin = new Skin();
			addSkin.setPlayerId(playerId);
			addSkin.setSkin(skin);
			addSkin.setGetTime((int) (System.currentTimeMillis() / 1000));
			insert(addSkin);
			return true;
		}
		return false;
	}

	@Override
	public void insert(Skin skin) {
		DAO.execute(SkinMapper.class, MapperConstant.insert, skin);
	}
	@Override
	public List<Integer> list() {
		return this.skins;
	}
	@Override
	public boolean checkSkin(int skin) {
		return this.skins.contains(skin);
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
