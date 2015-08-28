package org.rdswitchboard.utils.orcid;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * History:
 * 1.0.9: added shortDescription
 * 
 * @author Dmitrij Kudriavcev, dmitrij@kudriavcev.info
 */

public class OrcidWork {
	private String putCode;
	private WorkTitle title;
	private String journalTitle;
	private WorkCitation citation;
	private String workType;
	private PublicationDate publicationDate;
	private WorkIdentifiers worlIdentifiers;
	private String url;
	private WorkContributors workContributors;
	private OrcidIdentifier workSource;
	private String languageCode;
	private String country;
	private String shortDescription;
	private String visibility;
	
	@JsonProperty("put-code")
	public String getPutCode() {
		return putCode;
	}

	@JsonProperty("put-code")
	public void setPutCode(String putCode) {
		this.putCode = putCode;
	}

	@JsonProperty("work-title")
	public WorkTitle getTitle() {
		return title;
	}

	@JsonProperty("work-title")
	public void setTitle(WorkTitle title) {
		this.title = title;
	}

	@JsonProperty("journal-title")
	public String getJournalTitle() {
		return journalTitle;
	}

	@JsonProperty("journal-title")
	@JsonDeserialize(using = ValueDeserializer.class)
	public void setJournalTitle(String journalTitle) {
		this.journalTitle = journalTitle;
	}

	@JsonProperty("work-citation")
	public WorkCitation getCitation() {
		return citation;
	}

	@JsonProperty("work-citation")
	public void setCitation(WorkCitation citation) {
		this.citation = citation;
	}

	@JsonProperty("work-type")
	public String getWorkType() {
		return workType;
	}

	@JsonProperty("work-type")
	public void setWorkType(String workType) {
		this.workType = workType;
	}

	@JsonProperty("publication-date")
	public PublicationDate getPublicationDate() {
		return publicationDate;
	}

	@JsonProperty("publication-date")
	public void setPublicationDate(PublicationDate publicationDate) {
		this.publicationDate = publicationDate;
	}

	public String getPublicationDateString() {
		if (null != publicationDate)
			return publicationDate.getDate();
		else
			return null;
	}
	
	
	@JsonProperty("work-external-identifiers")
	public WorkIdentifiers getWorlIdentifiers() {
		return worlIdentifiers;
	}

	@JsonProperty("work-external-identifiers")
	public void setWorlIdentifiers(WorkIdentifiers worlIdentifiers) {
		this.worlIdentifiers = worlIdentifiers;
	}

	public String getUrl() {
		return url;
	}

	@JsonDeserialize(using = ValueDeserializer.class)
	public void setUrl(String url) {
		this.url = url;
	}

	@JsonProperty("work-contributors")
	public WorkContributors getWorkContributors() {
		return workContributors;
	}

	@JsonProperty("work-contributors")
	public void setWorkContributors(WorkContributors workContributors) {
		this.workContributors = workContributors;
	}

	@JsonProperty("work-source")
	public OrcidIdentifier getWorkSource() {
		return workSource;
	}

	@JsonProperty("work-source")
	public void setWorkSource(OrcidIdentifier workSource) {
		this.workSource = workSource;
	}

	public String getVisibility() {
		return visibility;
	}

	public void setVisibility(String visibility) {
		this.visibility = visibility;
	}
	
	@JsonProperty("language-code")
	public String getLanguageCode() {
		return languageCode;
	}

	@JsonProperty("language-code")
	public void setLanguageCode(String languageCode) {
		this.languageCode = languageCode;
	}

	public String getCountry() {
		return country;
	}

	@JsonDeserialize(using = ValueDeserializer.class)
	public void setCountry(String country) {
		this.country = country;
	}
	
	@JsonProperty("short-description")
	public String getShortDescription() {
		return shortDescription;
	}

	@JsonProperty("short-description")
	public void setShortDescription(String shortDescription) {
		this.shortDescription = shortDescription;
	}

	@Override
	public String toString() {
		return "OrcidWork [putCode=" + putCode 
				+ ", title=" + title
				+ ", journalTitle=" + journalTitle 
				+ ", citation=" + citation
				+ ", workType=" + workType 
				+ ", publicationDate=" + publicationDate 
				+ ", worlIdentifiers=" + worlIdentifiers
				+ ", url=" + url 
				+ ", workContributors=" + workContributors
				+ ", workSource=" + workSource 
				+ ", languageCode=" + languageCode 
				+ ", country=" + country 
				+ ", shortDescription=" + shortDescription
				+ ", visibility=" + visibility + "]";
	}
}
