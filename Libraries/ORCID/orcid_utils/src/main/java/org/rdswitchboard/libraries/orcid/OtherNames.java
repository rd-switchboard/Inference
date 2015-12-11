package org.rdswitchboard.libraries.orcid;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

public class OtherNames {
	private List<String> names;
	private String visibility;
	
	@JsonProperty("other-name")
	public List<String> getNames() {
		return names;
	}
	
	@JsonProperty("other-name")
	@JsonDeserialize(using = ValueDeserializer.class)
	public void setNames(List<String> names) {
		this.names = names;
	}
	
	public String getVisibility() {
		return visibility;
	}
	
	public void setVisibility(String visibility) {
		this.visibility = visibility;
	}
	
	@Override
	public String toString() {
		return "OtherNames [names=" + names + ", visibility="
				+ visibility + "]";
	}	
}
