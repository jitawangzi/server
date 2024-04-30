package cn.game.games.net.game.module.battle;

import java.util.List;
import java.util.Map;

import cn.game.protocol.generated.enume.AttributeTypeEnum;
import cn.game.games.net.game.helper.RoleHelper;

public class BattleResult {
	private int type; // DungeonTypeEnum 的id，属于哪个玩法的关卡
	private int dungeonId; // type==2时，是RoutineTraining.xlsm的id ; type = 3 时，BattleEvent.xlsm表id
	private int id; // 关卡id，关卡包含普通关卡，战旗关卡，剧情关卡等，对应不同的配置表id，比如普通关卡BattleLevel.xlsm表id
	private boolean win; // 输赢
	private List<Integer> starList; // 通关评价，几星，实际为关卡的评价条件索引，用0，1，2代表完成的关卡星级条件
	private String uid; // 当type = 3 时，id不能区分了，需要用uid
	private String report; // 战报，json格式

	private int lineupId;
	
	private Map<AttributeTypeEnum, Map<Integer, Hurt>> hurtMap;
	
	/**
	 * 获取角色属性损失
	 * @param roleAttr
	 * @return
	 */
	public Map<Integer, Hurt> getRoleHurts(int roleAttr) {
		if (hurtMap == null) {
			calcHurt();
		}
		AttributeTypeEnum type = RoleHelper.getAttributeType(roleAttr);
		return getRoleHurts(type);
	}
	
	/**
	 * 计算伤害
	 */
	private void calcHurt() {
		// TODO Auto-generated method stub
	}
	
	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getDungeonId() {
		return dungeonId;
	}

	public void setDungeonId(int dungeonId) {
		this.dungeonId = dungeonId;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public boolean isWin() {
		return win;
	}

	public void setWin(boolean win) {
		this.win = win;
	}

	public List<Integer> getStarList() {
		return starList;
	}

	public void setStarList(List<Integer> starList) {
		this.starList = starList;
	}

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public String getReport() {
		return report;
	}

	public void setReport(String report) {
		this.report = report;
	}

	public int getLineupId() {
		return lineupId;
	}

	public void setLineupId(int lineupId) {
		this.lineupId = lineupId;
	}

	public Map<AttributeTypeEnum, Map<Integer, Hurt>> getHurtMap() {
		return hurtMap;
	}

	public void setHurtMap(Map<AttributeTypeEnum, Map<Integer, Hurt>> hurtMap) {
		this.hurtMap = hurtMap;
	}

	public Map<Integer, Hurt> getRoleHurts(AttributeTypeEnum type) {
		Map<Integer, Hurt> map = hurtMap.get(type);
		return map;
	}
	
	public Hurt getRoleHurt(int roleUid, AttributeTypeEnum type) {
		Map<Integer, Hurt> roleHurts = getRoleHurts(type);
		Hurt hurt = roleHurts.get(roleUid);
		return hurt;
	}


	
}
