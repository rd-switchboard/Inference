package org.rdswitchboard.importers.figshare.objects;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Author {
	private String name;

	@JsonProperty("author_name")
	public String getName() {
		return name;
	}

	@JsonProperty("author_name")
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return "Author [name=" + name + "]";
	}
}
