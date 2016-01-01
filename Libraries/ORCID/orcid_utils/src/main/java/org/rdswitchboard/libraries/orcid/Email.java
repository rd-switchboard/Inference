package org.rdswitchboard.libraries.orcid;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

public class Email {
	private String value;
	private boolean primary;
	private boolean current;
	private boolean verified;
	private String visibility;
	private String source;
	private String sourceClientId;
	
	public String getValue() {
		return value;
	}
	
	public void setValue(String value) {
		this.value = value;
	}
	
	public boolean isPrimary() {
		return primary;
	}
	
	public void setPrimary(boolean primary) {
		this.primary = primary;
	}
	
	public boolean isCurrent() {
		return current;
	}
	
	public void setCurrent(boolean current) {
		this.current = current;
	}
	
	public boolean isVerified() {
		return verified;
	}
	
	public void setVerified(boolean verified) {
		this.verified = verified;
	}
	
	public String getVisibility() {
		return visibility;
	}
	
	public void setVisibility(String visibility) {
		this.visibility = visibility;
	}
	
	public String getSource() {
		return source;
	}
	
	public void setSource(String source) {
		this.source = source;
	}
	
	@JsonProperty("source-client-id")
	public String getSourceClientId() {
		return sourceClientId;
	}
	
	@JsonDeserialize(using = ValueDeserializer.class)
	public void setSourceClientId(String sourceClientId) {
		this.sourceClientId = sourceClientId;
	}
	
	@Override
	public String toString() {
		return "Email [value=" + value + ", primary=" + primary + ", current="
				+ current + ", verified=" + verified + ", visibility="
				+ visibility + ", source=" + source + ", sourceClientId="
				+ sourceClientId + "]";
	}
}
