package org.rdswitchboard.libraries.orcid;

import com.fasterxml.jackson.annotation.JsonProperty;

public class OrcidMessage {
	private String messageVersion;
	private OrcidProfile profile;
	private String searchResults;
	private String errorDescription;

	@JsonProperty("message-version")
	public String getMessageVersion() {
		return messageVersion;
	}

	public void setMessageVersion(String messageVersion) {
		this.messageVersion = messageVersion;
	}

	@JsonProperty("orcid-profile")
	public OrcidProfile getProfile() {
		return profile;
	}

	public void setProfile(OrcidProfile profile) {
		this.profile = profile;
	}

	@JsonProperty("orcid-search-results")
	public String getSearchResults() {
		return searchResults;
	}

	public void setSearchResults(String searchResults) {
		this.searchResults = searchResults;
	}
	
	@JsonProperty("error-desc")
	public String getErrorDescription() {
		return errorDescription;
	}

	public void setErrorDescription(String errorDescription) {
		this.errorDescription = errorDescription;
	}

	@Override
	public String toString() {
		return "OrcidMessage [messageVersion=" + messageVersion + ", profile="
				+ profile + ", searchResults=" + searchResults
				+ ", errorDescription=" + errorDescription + "]";
	}
}
