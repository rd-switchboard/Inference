package org.rdswitchboard.utils.orcid;

import java.util.List;

public class WorkContributors {
	private List<Contributor> contributor;

	public List<Contributor> getContributor() {
		return contributor;
	}

	public void setContributor(List<Contributor> contributor) {
		this.contributor = contributor;
	}

	@Override
	public String toString() {
		return "WorkContributors [contributor=" + contributor + "]";
	}
}
