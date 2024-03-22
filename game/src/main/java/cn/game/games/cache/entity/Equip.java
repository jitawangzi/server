package cn.game.games.cache.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;
import cn.game.games.cache.base.DbEntity;

/**
 * t_equip
 * @author
 */
public class Equip extends ItemNoStack implements Serializable, DbEntity {

	/**
	 * 不同类型的东西，关联的其他功能的id
	 * @mbg.generated
	 */
	private Integer relatedId;
	/**
	 * 一个int型扩展字段
	 * @mbg.generated
	 */
	private Integer extId;
	/**
	 * 扩展参数
	 * @mbg.generated
	 */
	private String extParam;
	/**
	 * @mbg.generated
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * @mbg.generated
	 */
	public Integer getRelatedId() {
		return relatedId;
	}

	/**
	 * @mbg.generated
	 */
	public void setRelatedId(Integer relatedId) {
		this.relatedId = relatedId;
	}

	/**
	 * @mbg.generated
	 */
	public Integer getExtId() {
		return extId;
	}

	/**
	 * @mbg.generated
	 */
	public void setExtId(Integer extId) {
		this.extId = extId;
	}

	/**
	 * @mbg.generated
	 */
	public String getExtParam() {
		return extParam;
	}

	/**
	 * @mbg.generated
	 */
	public void setExtParam(String extParam) {
		this.extParam = extParam;
	}

	/**
	 * @mbg.generated
	 */
	@Override
	public Class<?> getMapperClass() {
		return cn.game.games.net.data.mapper.EquipMapper.class;
	}

	/**
	 * @mbg.generated
	 */
	@Override
	public Object primaryKey() {
		return id;
	}

	/** 售卖中(拾荒者商店) */
	public static final byte IN_SALE = -3;
	/** 遗失 */
	public static final byte LOST = -2;
	/** 待选择(背包已满) */
	public static final byte NEED_SELECT = -1;
	/** 在仓库 */
	public static final byte IN_REPOSITORY = 1;
	/** 在背包 */
	public static final byte IN_BAG = 2;
	/** 在角色身上 */
	public static final byte IN_ROLE = 3;


	private transient List<Integer> buffList = new ArrayList<>();

	public List<Integer> getBuffList() {
//		if (buffList.isEmpty()) {
//			buffList = StrUtil.toList(this.buff);
//		}
		return buffList;
	}

	public void addBuff(int id) {
		getBuffList();
		this.buffList.add(id);
//		this.buff = StrUtil.toString(buffList);
	}

	public void setStorage(byte inBag) {
		// TODO Auto-generated method stub

	}

	public void setPos(byte pos) {
		// TODO Auto-generated method stub

	}

	public void setRoleId(int roleId) {
		// TODO Auto-generated method stub

	}

	public int getRoleId() {
		// TODO Auto-generated method stub
		return 0;
	}

	public byte getStorage() {
		// TODO Auto-generated method stub
		return 0;
	}

	public int getDictId() {
		// TODO Auto-generated method stub
		return 0;
	}

	public byte getPos() {
		// TODO Auto-generated method stub
		return 0;
	}

	public int getStrength() {
		// TODO Auto-generated method stub
		return 1;
	}
	
}