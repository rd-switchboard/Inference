package org.rdswitchboard.utils.orcid;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * History:
 * 1.0.9: added translatedTitle
 * 
 * @author Dmitrij Kudriavcev, dmitrij@kudriavcev.info
 *
 */

public class WorkTitle {
	private String title;
	private String subtitle;
	private String translatedTitle;

	public String getTitle() {
		return title;
	}
	
	@JsonDeserialize(using = ValueDeserializer.class)
	public void setTitle(String title) {
		this.title = title;
	}

	public String getSubtitle() {
		return subtitle;
	}

	@JsonDeserialize(using = ValueDeserializer.class)
	public void setSubtitle(String subtitle) {
		this.subtitle = subtitle;
	}
	
	@JsonProperty("translated-title")
	public String getTranslatedTitle() {
		return translatedTitle;
	}

	@JsonProperty("translated-title")
	@JsonDeserialize(using = ValueDeserializer.class)
	public void setTranslatedTitle(String translatedTitle) {
		this.translatedTitle = translatedTitle;
	}

	@Override
	public String toString() {
		return "WorkTitle [title=" + title 
				+ ", subtitle=" + subtitle 
				+ ", translatedTitle=" + translatedTitle
				+ "]";
	}
}
