package cn.game.games.cache.entity;

import cn.game.games.cache.base.DbEntity;
import java.io.Serializable;
import java.util.Date;

public class GmOpt implements Serializable, DbEntity {

	/**
	 * @mbg.generated
	 */
	private Integer id;
	/**
	 * 操作时间
	 * @mbg.generated
	 */
	private Date createTime;
	/**
	 * 操作原因
	 * @mbg.generated
	 */
	private String optmsg;
	/**
	 * 操作的玩家id
	 * @mbg.generated
	 */
	private String optpid;
	/**
	 * 参数为上行包内容
	 * @mbg.generated
	 */
	private String optParam;
	/**
	 * 返回消息 下行包内容
	 * @mbg.generated
	 */
	private String optResult;
	/**
	 * @mbg.generated
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * @mbg.generated
	 */
	public Integer getId() {
		return id;
	}

	/**
	 * @mbg.generated
	 */
	public void setId(Integer id) {
		this.id = id;
	}

	/**
	 * @mbg.generated
	 */
	public Date getCreateTime() {
		return createTime;
	}

	/**
	 * @mbg.generated
	 */
	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	/**
	 * @mbg.generated
	 */
	public String getOptmsg() {
		return optmsg;
	}

	/**
	 * @mbg.generated
	 */
	public void setOptmsg(String optmsg) {
		this.optmsg = optmsg;
	}

	/**
	 * @mbg.generated
	 */
	public String getOptpid() {
		return optpid;
	}

	/**
	 * @mbg.generated
	 */
	public void setOptpid(String optpid) {
		this.optpid = optpid;
	}

	/**
	 * @mbg.generated
	 */
	public String getOptParam() {
		return optParam;
	}

	/**
	 * @mbg.generated
	 */
	public void setOptParam(String optParam) {
		this.optParam = optParam;
	}

	/**
	 * @mbg.generated
	 */
	public String getOptResult() {
		return optResult;
	}

	/**
	 * @mbg.generated
	 */
	public void setOptResult(String optResult) {
		this.optResult = optResult;
	}

	/**
	 * @mbg.generated
	 */
	@Override
	public Class<?> getMapperClass() {
		return cn.game.games.net.data.mapper.GmOptMapper.class;
	}

	/**
	 * @mbg.generated
	 */
	@Override
	public Object primaryKey() {
		return id;
	}
}