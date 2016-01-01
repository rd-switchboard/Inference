package org.rdswitchboard.libraries.orcid;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

public class FundingExternalIdentifier {
	private String type;
	private String value;
	private String url;
	
	@JsonProperty("funding-external-identifier-type")
	public String getType() {
		return type;
	}
	
	public void setType(String type) {
		this.type = type;
	}
	
	@JsonProperty("funding-external-identifier-value")
	public String getValue() {
		return value;
	}
	
	public void setValue(String value) {
		this.value = value;
	}
	
	@JsonProperty("funding-external-identifier-url")
	public String getUrl() {
		return url;
	}
	
	@JsonDeserialize(using = ValueDeserializer.class)
	public void setUrl(String url) {
		this.url = url;
	}
	
	@Override
	public String toString() {
		return "FundingExternalIdentifier [type=" + type + ", value=" + value
				+ ", url=" + url + "]";
	}
}
