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
	private Long id;
	/**
	 * 邮件配置表id
	 * @mbg.generated
	 */
	private Integer mailId;
	/**
	 * 角色id
	 * @mbg.generated
	 */
	private Long playerId;
	/**
	 * 是否查看过
	 * @mbg.generated
	 */
	private Boolean see;
	/**
	 * 有i教案查看时间
	 * @mbg.generated
	 */
	private Integer seeTime;
	/**
	 * @mbg.generated
	 */
	private Boolean receive;
	/**
	 * 附件奖励领取时间
	 * @mbg.generated
	 */
	private Integer receiveTime;
	/**
	 * 发送者名字
	 * @mbg.generated
	 */
	private String sender;
	/**
	 * 邮件类型
	 * @mbg.generated
	 */
	private Byte type;
	/**
	 * 邮件标题
	 * @mbg.generated
	 */
	private String title;
	/**
	 * 邮件内容
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
	private Integer createTime;
	/**
	 * 邮件是否被删除。
	 * @mbg.generated
	 */
	private Boolean isDeleted;
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
	public Integer getMailId() {
		return mailId;
	}

	/**
	 * @mbg.generated
	 */
	public void setMailId(Integer mailId) {
		this.mailId = mailId;
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
	public Boolean getSee() {
		return see;
	}

	/**
	 * @mbg.generated
	 */
	public void setSee(Boolean see) {
		this.see = see;
	}

	/**
	 * @mbg.generated
	 */
	public Integer getSeeTime() {
		return seeTime;
	}

	/**
	 * @mbg.generated
	 */
	public void setSeeTime(Integer seeTime) {
		this.seeTime = seeTime;
	}

	/**
	 * @mbg.generated
	 */
	public Boolean getReceive() {
		return receive;
	}

	/**
	 * @mbg.generated
	 */
	public void setReceive(Boolean receive) {
		this.receive = receive;
	}

	/**
	 * @mbg.generated
	 */
	public Integer getReceiveTime() {
		return receiveTime;
	}

	/**
	 * @mbg.generated
	 */
	public void setReceiveTime(Integer receiveTime) {
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
	public Byte getType() {
		return type;
	}

	/**
	 * @mbg.generated
	 */
	public void setType(Byte type) {
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
	public Integer getCreateTime() {
		return createTime;
	}

	/**
	 * @mbg.generated
	 */
	public void setCreateTime(Integer createTime) {
		this.createTime = createTime;
	}

	/**
	 * @mbg.generated
	 */
	public Boolean getIsDeleted() {
		return isDeleted;
	}

	/**
	 * @mbg.generated
	 */
	public void setIsDeleted(Boolean isDeleted) {
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

	public static Mail valueOf(long receiverId, int mailId, String sender, String title, String content, byte type,
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
		mail.setType(type);
		mail.setIsDeleted(false);
		return mail;

	}

	public static Mail valueOfMailId(long receiverId, int mailId) {
		return valueOfMailId(receiverId, mailId, "", "", null);
	}

	public static Mail valueOfMailId(long receiverId, int mailId, String content, String title, List<Goods> goods) {

		MailConfig mailConfig = MailManager.instance().get(mailId);

//		List<Goods> goods = new ArrayList<>();
//		for (int[] re : mailConfig.Reward) {
//			Goods g = new Goods();
//			g.setId(re[0]);
//			g.setCount(re[1]);
//			goods.add(g);
//		}
		Mail mail = new Mail();
		mail.setPlayerId(receiverId);
		mail.setMailId(mailId);
		mail.setAttachmentList(goods == null ? new ArrayList<Goods>() : goods);
//		mail.setContent(content == null ? "" : content);
		mail.setContent(content == null ? "" : content);
		mail.setCreateTime((int) (System.currentTimeMillis() / 1000));
		mail.setId(IdUtil.getId());
		mail.setReceive(false);
		mail.setReceiveTime(0);
		mail.setSee(false);
		mail.setSeeTime(0);
//		mail.setSender(sender == null ? "" : sender);
//		mail.setTitle(title == null ? "" : title);
//		mail.setType(type);

		mail.setSender("");
		mail.setTitle(title == null ? "" : title);
		mail.setType((byte) mailConfig.Type);
		mail.setIsDeleted(false);

		return mail;

	}

}