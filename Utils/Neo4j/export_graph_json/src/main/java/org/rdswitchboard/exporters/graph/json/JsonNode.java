package org.rdswitchboard.exporters.graph.json;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonInclude;


/**
 * Node in JSON format 
 * 
 * Will contain a Node {@link JsonNode#id}, Node {@link JsonNode#type} and a map of Node {@link JsonNode#properties}
 * Optional {@link JsonNode#extras} set can be created, containing 'root' or 'incomplete' flags
 * 
 * @version 3.0.0 
 * @author Dima Kudriavcev (dmitrij@kudriavcev.info)
 * @date 24 May 2015 	
 *
 */

@JsonInclude(JsonInclude.Include.NON_NULL)
public class JsonNode {
	public static final String EXTRA_ROOT = "root";
	public static final String EXTRA_INCOMPLETE = "incomplete";
	
	private long id;
	private String type;
	private Set<String> extras;

	private final Map<String, Object> properties = new HashMap<String, Object>();

	public long getId() {
		return id;
	}
	
	public void setId(long id) {
		this.id = id;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
	
	public Set<String> getExtras() {
		return extras;
	}
		
	public void addExtra(String extra) {
		if (null == extras)
			extras = new HashSet<String>();
		
		for (String e : extras) 
			if (e.equals(extra))
				return;
		
		extras.add(extra);
	}
	
	public Map<String, Object> getProperties() {
		return properties;
	}
	
	public void addProperty(String name, Object value) {
		this.properties.put(name, value);
	}

	@Override
	public String toString() {
		return "Node [id=" + id 
				+ ", type=" + type 
				+ ", extras=" + extras
				+ ", properties=" + properties 
				+ "]";
	}	
}
