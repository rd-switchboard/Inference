package org.rdswitchboard.libraries.orcid;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class FundingContributors {
	private List<FundingContributor> contributors;

	@JsonProperty("funding-contributor")
	public List<FundingContributor> getContributors() {
		return contributors;
	}

	public void setContributors(List<FundingContributor> contributors) {
		this.contributors = contributors;
	}

	@Override
	public String toString() {
		return "FundingContributors [contributors=" + contributors + "]";
	}	
}
