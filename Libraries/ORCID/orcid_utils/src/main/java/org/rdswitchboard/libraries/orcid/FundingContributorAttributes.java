package org.rdswitchboard.libraries.orcid;

import com.fasterxml.jackson.annotation.JsonProperty;

public class FundingContributorAttributes {
	private String role;

	@JsonProperty("funding-contributor-role")
	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	@Override
	public String toString() {
		return "FundingContributorAttributes [role=" + role + "]";
	}
}
