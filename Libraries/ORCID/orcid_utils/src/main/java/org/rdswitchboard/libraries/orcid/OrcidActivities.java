package org.rdswitchboard.libraries.orcid;

import com.fasterxml.jackson.annotation.JsonProperty;

public class OrcidActivities {
	private Affiliations affiliations;
	private OrcidWorks works;
	private String fundingList;

	public Affiliations getAffiliations() {
		return affiliations;
	}

	public void setAffiliations(Affiliations affiliations) {
		this.affiliations = affiliations;
	}

	@JsonProperty("orcid-works")
	public OrcidWorks getWorks() {
		return works;
	}

	public void setWorks(OrcidWorks works) {
		this.works = works;
	}

	@JsonProperty("funding-list")
	public String getFundingList() {
		return fundingList;
	}

	public void setFundingList(String fundingList) {
		this.fundingList = fundingList;
	}

	@Override
	public String toString() {
		return "OrcidActivities [affiliations=" + affiliations + ", works="
				+ works + ", fundingList=" + fundingList + "]";
	}
}
