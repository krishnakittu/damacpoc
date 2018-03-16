package com.dmacpoc.entities;

import java.io.Serializable;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
@PersistenceCapable(detachable = "true", table = "mail_object")
public class MailObject implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@Persistent(primaryKey = "true", valueStrategy = IdGeneratorStrategy.INCREMENT)
//	@Extension(vendorName = "datanucleus", key = "strategy-when-notnull", value = "false")
	long id;
	
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	String to;
	
	String cc;
	
	String from;
	
	String subject;
	
	String messageId;
	
	int msgRef;

	public String getTo() {
		return to;
	}

	public void setTo(String to) {
		this.to = to;
	}

	public String getCc() {
		return cc;
	}

	public void setCc(String cc) {
		this.cc = cc;
	}

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getMessageId() {
		return messageId;
	}

	public void setMessageId(String messageId) {
		this.messageId = messageId;
	}

	public int getMsgRef() {
		return msgRef;
	}

	public void setMsgRef(int msgRef) {
		this.msgRef = msgRef;
	}

	@Override
	public String toString() {
		return "MailObject [to=" + to + ", cc=" + cc + ", from=" + from
				+ ", subject=" + subject + ", messageId=" + messageId
				+ ", msgRef=" + msgRef + "]";
	}

	public MailObject(String to, String cc, String from, String subject,
			String messageId, int msgRef) {
		super();
		this.to = to;
		this.cc = cc;
		this.from = from;
		this.subject = subject;
		this.messageId = messageId;
		this.msgRef = msgRef;
	}

	public MailObject() {
		super();
	}
	
	

}
