package org.rdswitchboard.libraries.orcid;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Organization {
	private String name;
	private Address address;
	private DisambiguatedOrganization disambiguatedOrganization;
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public Address getAddress() {
		return address;
	}

	public void setAddress(Address address) {
		this.address = address;
	}
	
	@JsonProperty("disambiguated-organization")
	public DisambiguatedOrganization getDisambiguatedOrganization() {
		return disambiguatedOrganization;
	}
	
	public void setDisambiguatedOrganization(
			DisambiguatedOrganization disambiguatedOrganization) {
		this.disambiguatedOrganization = disambiguatedOrganization;
	}

	@Override
	public String toString() {
		return "Organization [name=" + name + ", address=" + address
				+ ", disambiguatedOrganization=" + disambiguatedOrganization
				+ "]";
	}
}
