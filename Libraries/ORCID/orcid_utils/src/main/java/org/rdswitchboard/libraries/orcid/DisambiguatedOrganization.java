package org.rdswitchboard.libraries.orcid;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DisambiguatedOrganization {
	private String identifier;
	private String source;
	
	@JsonProperty("disambiguated-organization-identifier")
	public String getIdentifier() {
		return identifier;
	}
	
	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}
	
	@JsonProperty("disambiguation-source")
	public String getSource() {
		return source;
	}
	
	public void setSource(String source) {
		this.source = source;
	}
	
	@Override
	public String toString() {
		return "DisambiguatedOrganization [identifier=" + identifier
				+ ", source=" + source + "]";
	}
}
