package org.rdswitchboard.libraries.orcid;

import java.util.List;

public class Affiliations {
	private List<Affiliation> affiliations;
	
	public List<Affiliation> getAffiliations() {
		return affiliations;
	}

	public void setAffiliation(List<Affiliation> affiliations) {
		this.affiliations = affiliations;
	}
	
	@Override
	public String toString() {
		return "OrcidWorks [affiliations=" + affiliations + "]";
	}
}
