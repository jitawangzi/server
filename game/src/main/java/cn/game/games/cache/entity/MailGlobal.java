package cn.game.games.cache.entity;

import java.io.Serializable;
import cn.game.games.cache.base.DbEntity;

public class MailGlobal implements Serializable, DbEntity {

	/**
	 * @mbg.generated
	 */
	private Long id;
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
	@Override
	public Class<?> getMapperClass() {
		return cn.game.games.net.data.mapper.MailGlobalMapper.class;
	}

	/**
	 * @mbg.generated
	 */
	@Override
	public Object primaryKey() {
		return id;
	}
}