package org.rdswitchboard.libraries.orcid;

import com.fasterxml.jackson.annotation.JsonProperty;

public class OrcidProfile {
	private String orcid;
	private String id;
	private String deprecated;
	private OrcidIdentifier identifier;
	private OrcidPreferences preferences;
	private OrcidHistory history;
	private OrcidBio bio;
	private OrcidActivities activities;
	private String type;
	private String groupType;
	private String clientType;
	private String internal;

	public String getOrcid() {
		return orcid;
	}

	public void setOrcid(String orcid) {
		this.orcid = orcid;
	}
	
	@JsonProperty("orcid-id")
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	
	@JsonProperty("orcid-deprecated")
	public String getDeprecated() {
		return deprecated;
	}

	public void setDeprecated(String deprecated) {
		this.deprecated = deprecated;
	}

	@JsonProperty("orcid-identifier")
	public OrcidIdentifier getIdentifier() {
		return identifier;
	}

	public void setIdentifier(OrcidIdentifier identifier) {
		this.identifier = identifier;
	}

	@JsonProperty("orcid-preferences")
	public OrcidPreferences getPreferences() {
		return preferences;
	}

	public void setPreferences(OrcidPreferences preferences) {
		this.preferences = preferences;
	}

	@JsonProperty("orcid-history")
	public OrcidHistory getHistory() {
		return history;
	}

	public void setHistory(OrcidHistory history) {
		this.history = history;
	}

	@JsonProperty("orcid-bio")
	public OrcidBio getBio() {
		return bio;
	}

	public void setBio(OrcidBio bio) {
		this.bio = bio;
	}

	@JsonProperty("orcid-activities")
	public OrcidActivities getActivities() {
		return activities;
	}

	public void setActivities(OrcidActivities activities) {
		this.activities = activities;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
	
	@JsonProperty("group-type")
	public String getGroupType() {
		return groupType;
	}

	public void setGroupType(String groupType) {
		this.groupType = groupType;
	}

	@JsonProperty("client-type")
	public String getClientType() {
		return clientType;
	}

	public void setClientType(String clientType) {
		this.clientType = clientType;
	}
	
	@JsonProperty("orcid-internal")
	public String getInternal() {
		return internal;
	}

	public void setInternal(String internal) {
		this.internal = internal;
	}

	@Override
	public String toString() {
		return "OrcidProfile [orcid=" + orcid + ", id=" + id + ", deprecated="
				+ deprecated + ", identifier=" + identifier + ", preferences="
				+ preferences + ", history=" + history + ", bio=" + bio
				+ ", activities=" + activities + ", type=" + type
				+ ", groupType=" + groupType + ", clientType=" + clientType
				+ ", internal=" + internal + "]";
	}
}
