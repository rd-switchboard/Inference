package org.rdswitchboard.libraries.crossref;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Assertion {
	private int order;
	private String label;
	private String name;
	private String value;
	private String url;
	private String explanation;
	private Group group;
	
	public int getOrder() {
		return order;
	}

	public void setOrder(int order) {
		this.order = order;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getValue() {
		return value;
	}
	
	public void setValue(String value) {
		this.value = value;
	}
	
	public Group getGroup() {
		return group;
	}

	public void setGroup(Group group) {
		this.group = group;
	}

	@JsonProperty("URL")
	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
	
	@JsonAnySetter
	public void handleUnknown(String key, Object value) {
		System.out.println("Warning. Ignoring Assertion property: " + key + " with value: " + value);			
	}
	
	@Override
	public String toString() {
		return "Assertion [order=" + order + 
				", label=" + label + 
				", name=" + name + 
				", value=" + value + 
				", group=" + group + 
				", url=" + url + "]";
	}
}
