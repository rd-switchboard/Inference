package org.rdswitchboard.libraries.crossref;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonProperty;


/**
 * Class to store author information
 * @author Dima Kudriavcev, dmitrij@kudriavcev.info
 *
 */

public class Author {
	private String family;
	private String given;
	private String suffix;
	private String orcid;
	
	private List<Affiliation> affiliations;
	
	public String getFamily() {
		return family;		
	}
	
	public void setFamily(final String family) { 
		this.family = family;
	}
	
	public String getGiven() {
		return given;
	}
	
	public void setGiven(final String given) {
		this.given = given;
	}
	
	public String getSuffix() {
		return suffix;
	}
	
	public void setSuffix(final String suffix) {
		this.suffix = suffix;
	}
	
	@JsonProperty("ORCID")	
	public String getOrcid() {
		return orcid;
	}
	
	@JsonProperty("ORCID")	
	public void setOrcid(final String orcid) {
		this.orcid = orcid;
	}
	
	public String getFullName() {
		StringBuilder sb = new StringBuilder();
		if (null != suffix && !suffix.isEmpty()) {
			sb.append(suffix);
			sb.append(" ");
		}
		
		sb.append(given);
		sb.append(" ");
		sb.append(family);
		
		return sb.toString();				
	}
	
	@JsonProperty("affiliation")
	public List<Affiliation> getAffiliations() {
		return affiliations;
	}

	public void setAffiliations(List<Affiliation> affiliations) {
		this.affiliations = affiliations;
	}
	
	@JsonAnySetter
	public void handleUnknown(String key, Object value) {
		System.out.println("Warning. Ignoring Author property: " + key + " with value: " + value);			
	}
	
	@Override
	public String toString() {
		return "Author [suffix=" + suffix + 
				", family=" + family + 
				", given=" + given + 
				", orcid=" + orcid + 
				", affiliations=" + affiliations + "]";
	}
}
