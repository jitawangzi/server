package cn.game.games.cache.entity;

import cn.game.games.cache.base.DbEntity;
import java.io.Serializable;
import java.util.Date;

public class GmMail implements Serializable, DbEntity {

	/**
	 * @mbg.generated
	 */
	private long id;
	/**
	 * 创建时间
	 * @mbg.generated
	 */
	private Date createTime;
	/**
	 * 邮件类型 0 个人邮件 1 全服邮件 2  全服公告
	 * @mbg.generated
	 */
	private byte mailopttype;
	/**
	 * 邮件标题
	 * @mbg.generated
	 */
	private String title;
	/**
	 * 审核标识 0 未审核; 1 已审核
	 * @mbg.generated
	 */
	private byte optFlag;
	/**
	 * 该类型邮件操作的开始时间
	 * @mbg.generated
	 */
	private String sendStartTimer;
	/**
	 * 该类型邮件操作的结束时间
	 * @mbg.generated
	 */
	private String sendEndTimer;
	/**
	 * 全服邮件 玩家最低等级
	 * @mbg.generated
	 */
	private int minLevel;
	/**
	 * 全服邮件 玩家最大等级
	 * @mbg.generated
	 */
	private int maxLevel;
	/**
	 * 附件
	 * @mbg.generated
	 */
	private String attachment;
	/**
	 * 全服邮件 时间校验方式 0 登录时间 1 注册时间 2 不检查
	 * @mbg.generated
	 */
	private byte timeCheckType;
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
	 * 发送邮件的人 GM账号
	 * @mbg.generated
	 */
	private String sendName;
	/**
	 * 邮件给那些人发送 ; 分割
	 * @mbg.generated
	 */
	private String pids;
	/**
	 * 邮件内容
	 * @mbg.generated
	 */
	private String context;
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
	public byte getMailopttype() {
		return mailopttype;
	}

	/**
	 * @mbg.generated
	 */
	public void setMailopttype(byte mailopttype) {
		this.mailopttype = mailopttype;
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
	public byte getOptFlag() {
		return optFlag;
	}

	/**
	 * @mbg.generated
	 */
	public void setOptFlag(byte optFlag) {
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
	public int getMinLevel() {
		return minLevel;
	}

	/**
	 * @mbg.generated
	 */
	public void setMinLevel(int minLevel) {
		this.minLevel = minLevel;
	}

	/**
	 * @mbg.generated
	 */
	public int getMaxLevel() {
		return maxLevel;
	}

	/**
	 * @mbg.generated
	 */
	public void setMaxLevel(int maxLevel) {
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
	public byte getTimeCheckType() {
		return timeCheckType;
	}

	/**
	 * @mbg.generated
	 */
	public void setTimeCheckType(byte timeCheckType) {
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
	public String getSendName() {
		return sendName;
	}

	/**
	 * @mbg.generated
	 */
	public void setSendName(String sendName) {
		this.sendName = sendName;
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