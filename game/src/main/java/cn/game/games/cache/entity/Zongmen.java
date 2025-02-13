package cn.game.games.cache.entity;

import cn.game.games.cache.base.DbEntity;
import java.io.Serializable;

public class Zongmen implements Serializable, DbEntity {

	/**
	 * 宗门id
	 * @mbg.generated
	 */
	private Long id;
	/**
	 * 宗门名称
	 * @mbg.generated
	 */
	private String name;
	/**
	 * 工会创建时间
	 * @mbg.generated
	 */
	private String createTime;
	/**
	 * 等级
	 * @mbg.generated
	 */
	private Byte lv;
	/**
	 * 宗门图标
	 * @mbg.generated
	 */
	private Integer icon;
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
	private Integer exp;
	/**
	 * 宗门所在的服务器节点id
	 * @mbg.generated
	 */
	private String serverNodeId;
	/**
	 * 该宗门属于那个服务器id
	 * @mbg.generated
	 */
	private Integer createServerId;
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
	public Byte getLv() {
		return lv;
	}

	/**
	 * @mbg.generated
	 */
	public void setLv(Byte lv) {
		this.lv = lv;
	}

	/**
	 * @mbg.generated
	 */
	public Integer getIcon() {
		return icon;
	}

	/**
	 * @mbg.generated
	 */
	public void setIcon(Integer icon) {
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
	public Integer getExp() {
		return exp;
	}

	/**
	 * @mbg.generated
	 */
	public void setExp(Integer exp) {
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
	public Integer getCreateServerId() {
		return createServerId;
	}

	/**
	 * @mbg.generated
	 */
	public void setCreateServerId(Integer createServerId) {
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
		return cn.game.games.net.data.mapper.ZongmenMapper.class;
	}

	/**
	 * @mbg.generated
	 */
	@Override
	public Object primaryKey() {
		return id;
	}


}