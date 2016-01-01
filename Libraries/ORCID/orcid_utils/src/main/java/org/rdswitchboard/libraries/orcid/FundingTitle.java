package org.rdswitchboard.libraries.orcid;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

public class FundingTitle {
	private String title;
	private String translatedTitle;
	
	public String getTitle() {
		return title;
	}
	
	@JsonDeserialize(using = ValueDeserializer.class)
	public void setTitle(String title) {
		this.title = title;
	}
	
	@JsonProperty("translated-title")
	public String getTranslatedTitle() {
		return translatedTitle;
	}
	
	public void setTranslatedTitle(String translatedTitle) {
		this.translatedTitle = translatedTitle;
	}

	@Override
	public String toString() {
		return "fundingTitle [title=" + title + ", translatedTitle="
				+ translatedTitle + "]";
	}
}
