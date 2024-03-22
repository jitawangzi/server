package cn.game.games.net.game.module.event;

import java.util.ArrayList;
import java.util.List;

public class BuffShow {
	/** buffid Buff.xlsx表id表id */
	private int id;
	/** buff作用目标id，可以是角色id，队伍id，商店id等 */
	private List<Integer> targetIds = new ArrayList<>();
	
	public BuffShow(int buffId, int... targets) {
		this.id = buffId;
		for (int i : targets) {
			targetIds.add(i);
		}
	}

	public BuffShow(int buffId, List<Integer> targetIds) {
		this.id = buffId;
		this.targetIds = targetIds;
	}
	
	public BuffShow() {

	}

	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public List<Integer> getTargetIds() {
		return targetIds;
	}
	public void setTargetIds(List<Integer> targetIds) {
		this.targetIds = targetIds;
	}

	/**
	 * 添加目标
	 * @param targetIds
	 */
	public void add(int... targetIds) {
		for (int id : targetIds) {
			this.targetIds.add(id);
		}
	}

	public void clear() {
		this.id = 0;
		this.targetIds.clear();
	}
	
}
