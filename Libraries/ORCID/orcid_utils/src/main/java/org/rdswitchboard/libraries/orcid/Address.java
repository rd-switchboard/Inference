package org.rdswitchboard.libraries.orcid;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

public class Address {
	private String city;
	private String region;
	private String country;
	
	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getRegion() {
		return region;
	}

	public void setRegion(String region) {
		this.region = region;
	}

	public String getCountry() {
		return country;
	}

	//@JsonDeserialize(using = ValueDeserializer.class)
	public void setCountry(String country) {
		this.country = country;
	}

	@Override
	public String toString() {
		return "Address [city=" + city + ", region=" + region + ", country="
				+ country + "]";
	}
}
