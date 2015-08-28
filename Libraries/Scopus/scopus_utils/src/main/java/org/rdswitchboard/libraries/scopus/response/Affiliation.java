package org.rdswitchboard.libraries.scopus.response;

import java.util.Set;

/**
 * Affiliation class
 * 
 * The AffiliationDeserializer class will be used to initialize this object
 * 
 * @author dima
 */

public class Affiliation {
	private final String id;
	private final String name;
	private final String city;
	private final String country;
	private final String url;
	private final Set<String> alternative;
	
	public Affiliation(
			final String id,
			final String name,
			final String city,
			final String country,
			final String url,
			final Set<String> alternative) {
		
		this.id = id;
		this.name = name;
		this.city = city;
		this.country = country;
		this.url = url;
		this.alternative = alternative;
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getCity() {
		return city;
	}

	public String getCountry() {
		return country;
	}

	public String getUrl() {
		return url;
	}

	public Set<String> getAlternative() {
		return alternative;
	}

	@Override
	public String toString() {
		return "Affiliation [id=" + id 
				+ ", name=" + name 
				+ ", city=" + city
				+ ", country=" + country 
				+ ", url=" + url
				+ ", alternative=" + alternative + "]";
	}
}
