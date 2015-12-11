package org.rdswitchboard.libraries.orcid;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

public class Address {
	private String country;

	public String getCountry() {
		return country;
	}

	@JsonDeserialize(using = ValueDeserializer.class)
	public void setCountry(String country) {
		this.country = country;
	}

	@Override
	public String toString() {
		return "Address [country=" + country + "]";
	}
}
