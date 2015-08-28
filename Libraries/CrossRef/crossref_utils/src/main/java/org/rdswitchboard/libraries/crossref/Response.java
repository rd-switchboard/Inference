package org.rdswitchboard.libraries.crossref;

import com.fasterxml.jackson.annotation.JsonProperty;


/**
 * Template class to Store CrossRef Response. 
 * @author Dmitrij Kudriavcev, dmitrij@kudriavcev.info
 *
 * @param <T> Class to store actual message
 */
public class Response<T> {
	private String status;
	private String messageType;
	private String messageVersion;
	private T message;

	public static void Foo2(Item instance) {
	}
	
	
	public static <T> T Foo(T instance) {
		return instance;
	}
	
	public String getStatus() {
		return status;
	}
	
	public void setStatus(final String status) {
		this.status = status;
	}
	
	@JsonProperty("message-type")
	public String getMessageType() {
		return messageType;
	}
	
	@JsonProperty("message-type")
	public void setMessageType(final String messageType) {
		this.messageType = messageType;
	}
	
	@JsonProperty("message-version")
	public String getMessageVersion() {
		return messageVersion;
	}
	
	@JsonProperty("message-version")
	public void setMessageVersion(final String messageVersion) {
		this.messageVersion = messageVersion;
	}
	
	public T getMessage() {
		return message;
	}
	
	public void setMessage(T message) {
		this.message = message;
	}
	
	@Override
	public String toString() {
		return "Response Works [status=" + status + 
				", messageType=" + messageType + 
				", messageVersion=" + messageVersion +
				", message=" + message +
				"]";		
	}	
}
