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
	
	@JsonProperty("DOI")
	public void setDoi(final String doi) {
		this.doi = doi;
	}
	
	public String toString() {
		return "Funder [award=" + award + ", name=" + name + ", doi=" + doi + "]";
	}
	
}
