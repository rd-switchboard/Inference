package org.rdswitchboard.utils.orcid;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ContributorAttributes {
	private String contributorRole;
	private String contributorSequince;

	@JsonProperty("contributor-role")
	public String getContributorRole() {
		return contributorRole;
	}

	@JsonProperty("contributor-role")
	public void setContributorRole(String contributorRole) {
		this.contributorRole = contributorRole;
	}

	@JsonProperty("contributor-sequence")
	public String getContributorSequince() {
		return contributorSequince;
	}

	@JsonProperty("contributor-sequence")
	public void setContributorSequince(String contributorSequince) {
		this.contributorSequince = contributorSequince;
	}

	@Override
	public String toString() {
		return "ContributorAttributes [contributorRole=" + contributorRole
				+ ", contributorSequince=" + contributorSequince + "]";
	}
}
