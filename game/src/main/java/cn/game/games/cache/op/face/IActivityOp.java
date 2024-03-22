package cn.game.games.cache.op.face;

import java.util.Collection;
import java.util.List;

import cn.game.games.cache.entity.Activity;
import cn.game.games.net.game.module.activity.ActivityBase;
import cn.game.protocol.protobuf.RewardMsg.RewardInfo;

public interface IActivityOp {

	public int initLoadData(List<Activity> list);

	public void initAdd(int id);

	public void update(int id);

	public void updateAll();

	public void delete(int id);

	public List<RewardInfo> receive(int id, int subId);

	public void refresh();

	public void end(int id);

	public void destroy(int id);

	public void open(int id);

	public ActivityBase get(int id);

	public void newDay();

	public Collection<ActivityBase> list();

}
