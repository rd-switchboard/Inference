package org.rdswitchboard.libraries.scopus.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties({ "@_fa" })
public class Link {
	private String type;
	private String respType;
	private String href;
	
	public String getType() {
		return type;
	}

	@JsonProperty("@ref")
	public void setType(String type) {
		this.type = type;
	}


	public String getHref() {
		return href;
	}

	@JsonProperty("@href") 
	public void setHref(String href) {
		this.href = href;
	}
	
	public String getRespType() {
		return respType;
	}

	@JsonProperty("@type") 
	public void setRespType(String respType) {
		this.respType = respType;
	}

	@Override
	public String toString() {
		return "Link [type=" + type + ", refType=" + respType + ", href=" + href + "]";
	}
}
