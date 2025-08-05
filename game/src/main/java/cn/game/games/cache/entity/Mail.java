package cn.game.games.cache.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import cn.game.core.util.IdUtil;
import cn.game.games.cache.base.DbEntity;
import cn.game.games.net.game.module.award.Goods;
import cn.game.protocol.generated.config.MailConfig;
import cn.game.protocol.generated.manager.MailManager;

public class Mail implements Serializable, DbEntity {
    /**
	 * @mbg.generated
	 */
	private long id;
	/**
	 * 邮件配置表id
	 * @mbg.generated
	 */
	private int mailId;
	/**
	 * 角色id
	 * @mbg.generated
	 */
	private long playerId;
	/**
	 * 是否查看过
	 * @mbg.generated
	 */
	private boolean see;
	/**
	 * 有i教案查看时间
	 * @mbg.generated
	 */
	private int seeTime;
	/**
	 * @mbg.generated
	 */
	private boolean receive;
	/**
	 * 附件奖励领取时间
	 * @mbg.generated
	 */
	private int receiveTime;
	/**
	 * 发送者名字
	 * @mbg.generated
	 */
	private String sender;
	/**
	 * 邮件类型
	 * @mbg.generated
	 */
	private byte type;
	/**
	 * 邮件标题
	 * @mbg.generated
	 */
	private String title;
	/**
	 * 閭欢鍐呭
	 * @mbg.generated
	 */
	private String content;
	/**
	 * 附件中的奖励物品
	 * @mbg.generated
	 */
	private String attachment;
	/**
	 * @mbg.generated
	 */
	private int createTime;
	/**
	 * 邮件是否被删除。
	 * @mbg.generated
	 */
	private boolean isDeleted;
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
	public int getMailId() {
		return mailId;
	}

	/**
	 * @mbg.generated
	 */
	public void setMailId(int mailId) {
		this.mailId = mailId;
	}

	/**
	 * @mbg.generated
	 */
	public long getPlayerId() {
		return playerId;
	}

	/**
	 * @mbg.generated
	 */
	public void setPlayerId(long playerId) {
		this.playerId = playerId;
	}

	/**
	 * @mbg.generated
	 */
	public boolean getSee() {
		return see;
	}

	/**
	 * @mbg.generated
	 */
	public void setSee(boolean see) {
		this.see = see;
	}

	/**
	 * @mbg.generated
	 */
	public int getSeeTime() {
		return seeTime;
	}

	/**
	 * @mbg.generated
	 */
	public void setSeeTime(int seeTime) {
		this.seeTime = seeTime;
	}

	/**
	 * @mbg.generated
	 */
	public boolean getReceive() {
		return receive;
	}

	/**
	 * @mbg.generated
	 */
	public void setReceive(boolean receive) {
		this.receive = receive;
	}

	/**
	 * @mbg.generated
	 */
	public int getReceiveTime() {
		return receiveTime;
	}

	/**
	 * @mbg.generated
	 */
	public void setReceiveTime(int receiveTime) {
		this.receiveTime = receiveTime;
	}

	/**
	 * @mbg.generated
	 */
	public String getSender() {
		return sender;
	}

	/**
	 * @mbg.generated
	 */
	public void setSender(String sender) {
		this.sender = sender;
	}

	/**
	 * @mbg.generated
	 */
	public byte getType() {
		return type;
	}

	/**
	 * @mbg.generated
	 */
	public void setType(byte type) {
		this.type = type;
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
	public String getContent() {
		return content;
	}

	/**
	 * @mbg.generated
	 */
	public void setContent(String content) {
		this.content = content;
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
	public int getCreateTime() {
		return createTime;
	}

	/**
	 * @mbg.generated
	 */
	public void setCreateTime(int createTime) {
		this.createTime = createTime;
	}

	/**
	 * @mbg.generated
	 */
	public boolean getIsDeleted() {
		return isDeleted;
	}

	/**
	 * @mbg.generated
	 */
	public void setIsDeleted(boolean isDeleted) {
		this.isDeleted = isDeleted;
	}

	/**
	 * @mbg.generated
	 */
	@Override
	public Class<?> getMapperClass() {
		return cn.game.games.net.data.mapper.MailMapper.class;
	}

	/**
	 * @mbg.generated
	 */
	@Override
	public Object primaryKey() {
		return id;
	}

	private transient List<Goods> attachmentList;

	public List<Goods> getAttachmentList() {
		if (attachmentList == null && !StringUtils.isEmpty(this.attachment)) {
			attachmentList = JSONObject.parseArray(this.attachment, Goods.class);
		}
		return attachmentList;
	}

	public void setAttachmentList(List<Goods> attachmentList) {
		this.attachmentList = attachmentList;
		this.attachment = JSON.toJSONString(this.attachmentList);
	}

	/** 
	 * 全参数邮件实例化
	 * @param receiverId
	 * @param mailId
	 * @param sender
	 * @param title
	 * @param content
	 * @param type
	 * @param attachmentList
	 * @return
	 */
	public static Mail valueOf(long receiverId, int mailId, String sender, String title, String content, int type,
			List<Goods> attachmentList) {

		Mail mail = new Mail();
		mail.setMailId(mailId);
		mail.setAttachmentList(attachmentList);
		mail.setContent(content == null ? "" : content);
		mail.setCreateTime((int) (System.currentTimeMillis() / 1000));
		mail.setId(IdUtil.getId());
		mail.setPlayerId(receiverId);
		mail.setReceive(false);
		mail.setReceiveTime(0);
		mail.setSee(false);
		mail.setSeeTime(0);
		mail.setSender(sender == null ? "" : sender);
		mail.setTitle(title == null ? "" : title);
		if (type == 0) {
			MailConfig mailConfig = MailManager.instance().getNullable(mailId);
			if (mailConfig != null) {
				mail.setType((byte) mailConfig.Type); 
			}
		}else {
			mail.setType((byte) type);
		}
		mail.setIsDeleted(false);
		return mail;
	}

	/** 
	 * 玩家发送的邮件，直接指定类型
	 * @param receiverId
	 * @param sender
	 * @param title
	 * @param content
	 * @return
	 */
	public static Mail valueOf(long receiverId, String sender,String title, String content) {
		return valueOf(receiverId, 0, sender, title, content, 3, null); 
	}
	
	public static Mail valueOfMailId(long receiverId, int mailId) {
		return valueOfMailId(receiverId, mailId, "", "", null);
	}

	public static Mail valueOfMailId(long receiverId, int mailId, List<Goods> goods) {
		return valueOfMailId(receiverId, mailId, "", "", goods);
	}

	public static Mail valueOfMailId(long receiverId, int mailId, String title, String content, List<Goods> goods) {
		return valueOf(receiverId, mailId, "", title, content, 0, goods); 
	}

}