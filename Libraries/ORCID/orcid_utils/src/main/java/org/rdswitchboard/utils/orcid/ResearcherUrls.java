package org.rdswitchboard.utils.orcid;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ResearcherUrls {
	private List<ResearcherUrl> url;
	private String visibility;
	
	@JsonProperty("researcher-url")
	public List<ResearcherUrl> getUrl() {
		return url;
	}

	@JsonProperty("researcher-url")
	public void setUrl(List<ResearcherUrl> url) {
		this.url = url;
	}
	
	public String getVisibility() {
		return visibility;
	}

	public void setVisibility(String visibility) {
		this.visibility = visibility;
	}

	@Override
	public String toString() {
		return "ResearcherUrls [url=" + url + ", visibility=" + visibility + "]";
	}
}
