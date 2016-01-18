package org.rdswitchboard.libraries.crossref;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Class to store Funder information
 * 
 * @author Dmitrij Kudriavcev, dmitrij@kudriavcev.info
 *
 */
public class Funder {
	private List<String> award;
	private String name;
	private String doi;
	private String assertedBy;
	
	public List<String> getAward() {
		return award;
	}
	
	public void setAward(List<String> award) {
		if (!award.isEmpty())
			this.award = award;
		else
			this.award = null;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(final String name) {
		this.name = name;
	}
	
	@JsonProperty("DOI")
	public String getDoi() {
		return doi;
	}
	
	public void setDoi(final String doi) {
		this.doi = doi;
	}
	
	@JsonProperty("doi-asserted-by")
	public String getAssertedBy() {
		return assertedBy;
	}

	public void setAssertedBy(String assertedBy) {
		this.assertedBy = assertedBy;
	}

	public String toString() {
		return "Funder [award=" + award + 
				", name=" + name + 
				", doi=" + doi + 
				", assertedBy=" + assertedBy + 
				"]";
	}
	
}
