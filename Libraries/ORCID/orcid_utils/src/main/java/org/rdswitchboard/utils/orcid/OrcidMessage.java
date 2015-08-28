package org.rdswitchboard.utils.orcid;

import com.fasterxml.jackson.annotation.JsonProperty;

public class OrcidMessage {
	private String messageVersion;
	private OrcidProfile profile;

	@JsonProperty("message-version")
	public String getMessageVersion() {
		return messageVersion;
	}

	@JsonProperty("message-version")
	public void setMessageVersion(String messageVersion) {
		this.messageVersion = messageVersion;
	}

	@JsonProperty("orcid-profile")
	public OrcidProfile getProfile() {
		return profile;
	}

	@JsonProperty("orcid-profile")
	public void setProfile(OrcidProfile profile) {
		this.profile = profile;
	}

	@Override
	public String toString() {
		return "OrcidMessage [messageVersion=" + messageVersion + ", profile="
				+ profile + "]";
	}
}
