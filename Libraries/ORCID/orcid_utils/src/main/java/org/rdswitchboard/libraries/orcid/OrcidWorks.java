package org.rdswitchboard.libraries.orcid;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class OrcidWorks {
	private List<OrcidWork> works;
	private String scope;
	
	@JsonProperty("orcid-work")
	public List<OrcidWork> getWorks() {
		return works;
	}

	@JsonProperty("orcid-work")
	public void setWorks(List<OrcidWork> works) {
		this.works = works;
	}
	
	public String getScope() {
		return scope;
	}

	public void setScope(String scope) {
		this.scope = scope;
	}


	@Override
	public String toString() {
		return "OrcidWorks [works=" + works + ", scope=" + scope + "]";
	}
}
