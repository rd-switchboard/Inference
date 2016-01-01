package org.rdswitchboard.libraries.orcid;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

public class FundingContributor {
	public String orcid;
	public String creditName;
	public String email;
	public FundingContributorAttributes attributes;
	
	@JsonProperty("contributor-orcid")
	public String getOrcid() {
		return orcid;
	}
	
	@JsonDeserialize(using = ValueDeserializer.class)
	public void setOrcid(String orcid) {
		this.orcid = orcid;
	}
	
	@JsonProperty("credit-name")
	public String getCreditName() {
		return creditName;
	}
	
	@JsonDeserialize(using = ValueDeserializer.class)
	public void setCreditName(String creditName) {
		this.creditName = creditName;
	}
	
	@JsonProperty("contributor-email")
	public String getEmail() {
		return email;
	}
	
	@JsonDeserialize(using = ValueDeserializer.class)
	public void setEmail(String email) {
		this.email = email;
	}
	
	@JsonProperty("funding-contributor-attributes")
	public FundingContributorAttributes getAttributes() {
		return attributes;
	}
	
	public void setAttributes(FundingContributorAttributes attributes) {
		this.attributes = attributes;
	}
	
	@Override
	public String toString() {
		return "FundingContributor [orcid=" + orcid + ", creditName="
				+ creditName + ", email=" + email + ", attributes="
				+ attributes + "]";
	}
}
