package cn.game.games.cache.entity;

import cn.game.games.cache.base.DbEntity;
import java.io.Serializable;
import java.util.Date;

public class GmMail implements Serializable, DbEntity {

	/**
	 * @mbg.generated
	 */
	private Integer id;
	/**
	 * 鍒涘缓鏃堕棿
	 * @mbg.generated
	 */
	private Date createTime;
	/**
	 * 邮件类型 0 个人邮件 1 全服邮件
	 * @mbg.generated
	 */
	private Integer mailopttype;
	/**
	 * 邮件给那些人发送 ; 分割
	 * @mbg.generated
	 */
	private String pids;
	/**
	 * 邮件标题
	 * @mbg.generated
	 */
	private String title;
	/**
	 * 邮件内容
	 * @mbg.generated
	 */
	private String context;
	/**
	 * 审核标识 0 未审核; 1 已审核
	 * @mbg.generated
	 */
	private Byte optFlag;
	/**
	 * 閸忋劍婀囬柇顔绘 閻㈢喐鏅ラ惃鍕磻婵妞傞梻?
	 * @mbg.generated
	 */
	private String sendStartTimer;
	/**
	 * 閸忋劍婀囬柇顔绘閻㈢喐鏅ラ惃鍕波閺夌喐妞傞梻?
	 * @mbg.generated
	 */
	private String sendEndTimer;
	/**
	 * 全服邮件 玩家最低等级
	 * @mbg.generated
	 */
	private Integer minLevel;
	/**
	 * 全服邮件 玩家最大等级
	 * @mbg.generated
	 */
	private Integer maxLevel;
	/**
	 * 閭欢闄勪欢
	 * @mbg.generated
	 */
	private String attachment;
	/**
	 * 全服邮件 时间校验方式 0 登录时间 1 注册时间
	 * @mbg.generated
	 */
	private Integer timeCheckType;
	/**
	 * 包含的服务器id
	 * @mbg.generated
	 */
	private String serverids;
	/**
	 * 审核时间戳
	 * @mbg.generated
	 */
	private String approvalTimer;
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
	public Integer getMailopttype() {
		return mailopttype;
	}

	/**
	 * @mbg.generated
	 */
	public void setMailopttype(Integer mailopttype) {
		this.mailopttype = mailopttype;
	}

	/**
	 * @mbg.generated
	 */
	public String getPids() {
		return pids;
	}

	/**
	 * @mbg.generated
	 */
	public void setPids(String pids) {
		this.pids = pids;
	}

	/**
	 * @mbg.generated
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * @mbg.generated
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * @mbg.generated
	 */
	public String getContext() {
		return context;
	}

	/**
	 * @mbg.generated
	 */
	public void setContext(String context) {
		this.context = context;
	}

	/**
	 * @mbg.generated
	 */
	public Byte getOptFlag() {
		return optFlag;
	}

	/**
	 * @mbg.generated
	 */
	public void setOptFlag(Byte optFlag) {
		this.optFlag = optFlag;
	}

	/**
	 * @mbg.generated
	 */
	public String getSendStartTimer() {
		return sendStartTimer;
	}

	/**
	 * @mbg.generated
	 */
	public void setSendStartTimer(String sendStartTimer) {
		this.sendStartTimer = sendStartTimer;
	}

	/**
	 * @mbg.generated
	 */
	public String getSendEndTimer() {
		return sendEndTimer;
	}

	/**
	 * @mbg.generated
	 */
	public void setSendEndTimer(String sendEndTimer) {
		this.sendEndTimer = sendEndTimer;
	}

	/**
	 * @mbg.generated
	 */
	public Integer getMinLevel() {
		return minLevel;
	}

	/**
	 * @mbg.generated
	 */
	public void setMinLevel(Integer minLevel) {
		this.minLevel = minLevel;
	}

	/**
	 * @mbg.generated
	 */
	public Integer getMaxLevel() {
		return maxLevel;
	}

	/**
	 * @mbg.generated
	 */
	public void setMaxLevel(Integer maxLevel) {
		this.maxLevel = maxLevel;
	}

	/**
	 * @mbg.generated
	 */
	public String getAttachment() {
		return attachment;
	}

	/**
	 * @mbg.generated
	 */
	public void setAttachment(String attachment) {
		this.attachment = attachment;
	}

	/**
	 * @mbg.generated
	 */
	public Integer getTimeCheckType() {
		return timeCheckType;
	}

	/**
	 * @mbg.generated
	 */
	public void setTimeCheckType(Integer timeCheckType) {
		this.timeCheckType = timeCheckType;
	}

	/**
	 * @mbg.generated
	 */
	public String getServerids() {
		return serverids;
	}

	/**
	 * @mbg.generated
	 */
	public void setServerids(String serverids) {
		this.serverids = serverids;
	}

	/**
	 * @mbg.generated
	 */
	public String getApprovalTimer() {
		return approvalTimer;
	}

	/**
	 * @mbg.generated
	 */
	public void setApprovalTimer(String approvalTimer) {
		this.approvalTimer = approvalTimer;
	}

	/**
	 * @mbg.generated
	 */
	@Override
	public Class<?> getMapperClass() {
		return cn.game.games.net.data.mapper.GmMailMapper.class;
	}

	/**
	 * @mbg.generated
	 */
	@Override
	public Object primaryKey() {
		return id;
	}
}