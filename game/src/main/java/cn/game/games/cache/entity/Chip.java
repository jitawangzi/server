package cn.game.games.cache.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import cn.game.util.FourTuple;
import cn.game.util.Triple;

/**
 * t_chip
 * @author
 */
public class Chip implements Serializable {

	/**
	 * @mbg.generated
	 */
	private Long id;

	/**
	 * 装备所属玩家id
	 * @mbg.generated
	 */
	private Long playerId;

	/**
	 * 配置表id
	 * @mbg.generated
	 */
	private Integer dictId;

	/**
	 * 角色id
	 * @mbg.generated
	 */
	private Integer roleId;

	/**
	 * 位置
	 * @mbg.generated
	 */
	private Byte position;

	/**
	 * 等级
	 * @mbg.generated
	 */
	private Integer grade;

	/**
	 * 基础属性 属性:初始值:成长值
	 * @mbg.generated
	 */
	private String commonAttr;

	/**
	 * 现词缀属性 属性:值:最小值:最大值
	 * @mbg.generated
	 */
	private String affixAttr;

	/**
	 * 新词缀属性 属性:值:最小值:最大值
	 * @mbg.generated
	 */
	private String newaffixAttr;

	/**
	 * 是否锁定
	 * @mbg.generated
	 */
	private Byte checklock;

	/**
	 * 获取时间
	 * @mbg.generated
	 */
	private Long getTime;

	/**
	 * @mbg.generated
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * @mbg.generated
	 */
	public Long getId() {
		return id;
	}

	/**
	 * @mbg.generated
	 */
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * @mbg.generated
	 */
	public Long getPlayerId() {
		return playerId;
	}

	/**
	 * @mbg.generated
	 */
	public void setPlayerId(Long playerId) {
		this.playerId = playerId;
	}

	/**
	 * @mbg.generated
	 */
	public Integer getDictId() {
		return dictId;
	}

	/**
	 * @mbg.generated
	 */
	public void setDictId(Integer dictId) {
		this.dictId = dictId;
	}

	/**
	 * @mbg.generated
	 */
	public Integer getRoleId() {
		return roleId;
	}

	/**
	 * @mbg.generated
	 */
	public void setRoleId(Integer roleId) {
		this.roleId = roleId;
	}

	/**
	 * @mbg.generated
	 */
	public Byte getPosition() {
		return position;
	}

	/**
	 * @mbg.generated
	 */
	public void setPosition(Byte position) {
		this.position = position;
	}

	/**
	 * @mbg.generated
	 */
	public Integer getGrade() {
		return grade;
	}

	/**
	 * @mbg.generated
	 */
	public void setGrade(Integer grade) {
		this.grade = grade;
	}

	/**
	 * @mbg.generated
	 */
	public String getCommonAttr() {
		return commonAttr;
	}

	/**
	 * @mbg.generated
	 */
	public void setCommonAttr(String commonAttr) {
		this.commonAttr = commonAttr;
	}

	/**
	 * @mbg.generated
	 */
	public String getAffixAttr() {
		return affixAttr;
	}

	/**
	 * @mbg.generated
	 */
	public void setAffixAttr(String affixAttr) {
		this.affixAttr = affixAttr;
	}

	/**
	 * @mbg.generated
	 */
	public String getNewaffixAttr() {
		return newaffixAttr;
	}

	/**
	 * @mbg.generated
	 */
	public void setNewaffixAttr(String newaffixAttr) {
		this.newaffixAttr = newaffixAttr;
	}

	/**
	 * @mbg.generated
	 */
	public Byte getChecklock() {
		return checklock;
	}

	/**
	 * @mbg.generated
	 */
	public void setChecklock(Byte checklock) {
		this.checklock = checklock;
	}

	/**
	 * @mbg.generated
	 */
	public Long getGetTime() {
		return getTime;
	}

	/**
	 * @mbg.generated
	 */
	public void setGetTime(Long getTime) {
		this.getTime = getTime;
	}

	public void setAffixAttrInfo(String affixAttr) {
		this.affixAttr = affixAttr;
		if (!StringUtils.isEmpty(this.affixAttr)) {
			this.affixAttrList = new ArrayList<>();
			setAffixAttrPair(this.affixAttr, affixAttrList);
		}
	}

	public void setNewAffixAttrInfo(String newaffixAttr) {
		this.newaffixAttr = newaffixAttr;
		if (!StringUtils.isEmpty(this.newaffixAttr)) {
			this.newaffixAttrList = new ArrayList<>();
			setAffixAttrPair(this.newaffixAttr, newaffixAttrList);
		}
	}

	private transient List<Triple<Integer,Integer,Integer>> commonAttrList = new ArrayList<>();

	private transient List<FourTuple<Integer,Integer,Integer,Integer>> affixAttrList = new ArrayList<>();

	private transient List<FourTuple<Integer,Integer,Integer,Integer>> newaffixAttrList = new ArrayList<>();

	public List<Triple<Integer,Integer,Integer>> getCommonAttrList() {
		if (commonAttrList.isEmpty() && !StringUtils.isEmpty(this.commonAttr)) {
			//commonAttrList = JSONObject.parseArray(this.commonAttr, CommonAttrData.class);
			String[] split = commonAttr.split("\\|");
			for (String str : split) {
				String[] split1 = str.split(":");

				Triple<Integer,Integer,Integer> triple = new Triple<>(Integer.parseInt(split1[0]), Integer.parseInt(split1[1]), Integer.parseInt(split1[2]));
				commonAttrList.add(triple);
			}

		}
		return commonAttrList;
	}

	public List<FourTuple<Integer,Integer,Integer,Integer>> getAffixAttrList() {
		if (affixAttrList.isEmpty() && !StringUtils.isEmpty(this.affixAttr)) {
			setAffixAttrPair(this.affixAttr, affixAttrList);
		}
		return affixAttrList;
	}

	public List<FourTuple<Integer,Integer,Integer,Integer>> getNewaffixAttrList() {
		if (newaffixAttrList.isEmpty() && !StringUtils.isEmpty(this.newaffixAttr)) {
			setAffixAttrPair(this.newaffixAttr, newaffixAttrList);
		}
		return newaffixAttrList;
	}

	public void setAffixAttrPair(String affixAttr, List<FourTuple<Integer,Integer,Integer,Integer>> attrlist) {
		String[] split = affixAttr.split("\\|");
		for (String str : split) {
			String[] split1 = str.split(":");
			if (split1.length <= 2) {
				continue;
			}
			FourTuple<Integer,Integer,Integer,Integer> pair = new FourTuple<>(Integer.parseInt(split1[0]),
					Integer.parseInt(split1[1]), Integer.parseInt(split1[2]), Integer.parseInt(split1[3]));
			attrlist.add(pair);
		}
	}


}