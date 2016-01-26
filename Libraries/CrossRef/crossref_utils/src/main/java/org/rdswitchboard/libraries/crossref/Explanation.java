package org.rdswitchboard.libraries.crossref;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Explanation {
	private String url;

	@JsonProperty("URL")
	public String getUrl() {
		return url;
	}
	
	public void setUrl(String url) {
		this.url = url;
	}

	@JsonAnySetter
	public void handleUnknown(String key, Object value) {
		System.out.println("Warning. Ignoring Explanation property: " + key + " with value: " + value);			
	}
	
	@Override
	public String toString() {
		return "Explanation [url=" + url + "]";
	}
}
