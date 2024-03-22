package cn.game.games.cache.op.face;

import java.util.List;

import cn.game.games.cache.base.ICacheOp;
import cn.game.games.cache.entity.Skin;

public interface ISkinOp {

	// 初始数据,
	public void initLoadData(List<Skin> skins);

	public boolean add(int skin);

	public void insert(Skin skin);

	public List<Integer> list();

	public boolean checkSkin(int skin);

}
