package org.rdswitchboard.libraries.orcid;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class FundingExternalIdentifiers {
	private List<FundingExternalIdentifier> identifiers;

	@JsonProperty("funding-external-identifier")
	public List<FundingExternalIdentifier> getIdentifiers() {
		return identifiers;
	}

	public void setIdentifiers(List<FundingExternalIdentifier> identifiers) {
		this.identifiers = identifiers;
	}

	@Override
	public String toString() {
		return "FundingExternalIdentifiers [getClass()=" + getClass()
				+ ", hashCode()=" + hashCode() + ", toString()="
				+ super.toString() + "]";
	}
	
	
}
