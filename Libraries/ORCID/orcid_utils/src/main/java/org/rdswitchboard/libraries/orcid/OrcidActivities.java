package org.rdswitchboard.libraries.orcid;

import com.fasterxml.jackson.annotation.JsonProperty;

public class OrcidActivities {
	private Affiliations affiliations;
	private OrcidWorks works;
	private Fundings fundings;

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
	public Fundings getFundings() {
		return fundings;
	}

	public void setFunding(Fundings fundings) {
		this.fundings = fundings;
	}

	@Override
	public String toString() {
		return "OrcidActivities [affiliations=" + affiliations + ", works="
				+ works + ", fundings=" + fundings + "]";
	}
}
