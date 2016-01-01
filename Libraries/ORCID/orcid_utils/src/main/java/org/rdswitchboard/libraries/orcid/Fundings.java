package org.rdswitchboard.libraries.orcid;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Fundings {
	private List<Funding> fundings;
	private String scope;

	@JsonProperty("funding")
	public List<Funding> getFundings() {
		return fundings;
	}

	@JsonProperty("funding")
	public void setFundings(List<Funding> fundings) {
		this.fundings = fundings;
	}

	public String getScope() {
		return scope;
	}

	public void setScope(String scope) {
		this.scope = scope;
	}

	@Override
	public String toString() {
		return "Fundings [fundings=" + fundings + ", scope=" + scope + "]";
	}	
}
