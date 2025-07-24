package cn.game.games.cache.entity;

import cn.game.games.cache.base.DbEntity;
import java.io.Serializable;

public class ZongmenData implements Serializable, DbEntity {

	/**
	 * 宗门id
	 * @mbg.generated
	 */
	private long id;
	/**
	 * 宗门名称
	 * @mbg.generated
	 */
	private String name;
	/**
	 * 宗门创建时间
	 * @mbg.generated
	 */
	private String createTime;
	/**
	 * 等级
	 * @mbg.generated
	 */
	private byte lv;
	/**
	 * 宗门图标
	 * @mbg.generated
	 */
	private int icon;
	/**
	 * 公告
	 * @mbg.generated
	 */
	private String notice;
	/**
	 * 宣言
	 * @mbg.generated
	 */
	private String declaration;
	/**
	 * 当前经验
	 * @mbg.generated
	 */
	private int exp;
	/**
	 * 宗门所在的服务器节点id，暂时用不到
	 * @mbg.generated
	 */
	private String serverNodeId;
	/**
	 * 该宗门属于那个逻辑服务器id
	 * @mbg.generated
	 */
	private String createServerId;
	/**
	 * 所有模块数据
	 * @mbg.generated
	 */
	private String modules;
	/**
	 * @mbg.generated
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * @mbg.generated
	 */
	public long getId() {
		return id;
	}

	/**
	 * @mbg.generated
	 */
	public void setId(long id) {
		this.id = id;
	}

	/**
	 * @mbg.generated
	 */
	public String getName() {
		return name;
	}

	/**
	 * @mbg.generated
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @mbg.generated
	 */
	public String getCreateTime() {
		return createTime;
	}

	/**
	 * @mbg.generated
	 */
	public void setCreateTime(String createTime) {
		this.createTime = createTime;
	}

	/**
	 * @mbg.generated
	 */
	public byte getLv() {
		return lv;
	}

	/**
	 * @mbg.generated
	 */
	public void setLv(byte lv) {
		this.lv = lv;
	}

	/**
	 * @mbg.generated
	 */
	public int getIcon() {
		return icon;
	}

	/**
	 * @mbg.generated
	 */
	public void setIcon(int icon) {
		this.icon = icon;
	}

	/**
	 * @mbg.generated
	 */
	public String getNotice() {
		return notice;
	}

	/**
	 * @mbg.generated
	 */
	public void setNotice(String notice) {
		this.notice = notice;
	}

	/**
	 * @mbg.generated
	 */
	public String getDeclaration() {
		return declaration;
	}

	/**
	 * @mbg.generated
	 */
	public void setDeclaration(String declaration) {
		this.declaration = declaration;
	}

	/**
	 * @mbg.generated
	 */
	public int getExp() {
		return exp;
	}

	/**
	 * @mbg.generated
	 */
	public void setExp(int exp) {
		this.exp = exp;
	}

	/**
	 * @mbg.generated
	 */
	public String getServerNodeId() {
		return serverNodeId;
	}

	/**
	 * @mbg.generated
	 */
	public void setServerNodeId(String serverNodeId) {
		this.serverNodeId = serverNodeId;
	}

	/**
	 * @mbg.generated
	 */
	public String getCreateServerId() {
		return createServerId;
	}

	/**
	 * @mbg.generated
	 */
	public void setCreateServerId(String createServerId) {
		this.createServerId = createServerId;
	}

	/**
	 * @mbg.generated
	 */
	public String getModules() {
		return modules;
	}

	/**
	 * @mbg.generated
	 */
	public void setModules(String modules) {
		this.modules = modules;
	}

	/**
	 * @mbg.generated
	 */
	@Override
	public Class<?> getMapperClass() {
		return cn.game.games.net.data.mapper.ZongmenDataMapper.class;
	}

	/**
	 * @mbg.generated
	 */
	@Override
	public Object primaryKey() {
		return id;
	}


}