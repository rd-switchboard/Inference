package org.rdswitchboard.libraries.scopus.response;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Query {
	private final String role;
	private final String searchTerms;
	private final String startPage;
	
	@JsonCreator
	public Query(
			@JsonProperty("@role") final String role,
	        @JsonProperty("@searchTerms") final String searchTerms,
	        @JsonProperty("@startPage") final String startPage) {
		
		this.role = role;
		this.searchTerms = searchTerms;
		this.startPage = startPage;		
	}

	public String getRole() {
		return role;
	}

	public String getSearchTerms() {
		return searchTerms;
	}

	public String getStartPage() {
		return startPage;
	}

	@Override
	public String toString() {
		return "Query [role=" + role + ", searchTerms=" + searchTerms
				+ ", startPage=" + startPage + "]";
	}
}
