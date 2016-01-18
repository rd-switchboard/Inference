package org.rdswitchboard.libraries.crossref;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Authority {
	private String doi;
	private String authority;
	private String status;

	@JsonProperty("DOI")	
	public String getDoi() {
		return doi;
	}
	
	public void setDoi(String doi) {
		this.doi = doi;
	}
	
	@JsonProperty("RA")
	public String getAuthority() {
		return authority;
	}
	
	public void setAuthority(String authority) {
		this.authority = authority;
	}
	
	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	@Override
	public String toString() {
		return "Authority [doi=" + doi + ", authority=" + authority +  ", status=" + status + "]";
	}
}
