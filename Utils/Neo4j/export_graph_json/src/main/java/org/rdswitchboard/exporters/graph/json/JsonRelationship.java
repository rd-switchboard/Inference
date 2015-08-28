
package org.rdswitchboard.exporters.graph.json;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Relationship in JSON format
 * 
 * Will contain {@link JsonRelationship#from}, {@link JsonRelationship#to} and {@link JsonRelationship#type} properties
 * The from and to properties should specify an actual id of {@link JsonNode}
 * 
 * @version 3.0.0
 * @author Dima Kudriavcev (dmitrij@kudriavcev.info)
 * @date 24 May 2015 	
 *
 * History:
 * 	- added id property
 */

@JsonInclude(JsonInclude.Include.NON_NULL)
public class JsonRelationship {
	private long id;
	private long from;
	private long to;
	private String type;
		
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getFrom() {
		return from;
	}

	public void setFrom(long from) {
		this.from = from;
	}

	public long getTo() {
		return to;
	}

	public void setTo(long to) {
		this.to = to;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	@Override
	public String toString() {
		return "JsonRelationship [id=" + id + ", from=" + from + ", to=" + to
				+ ", type=" + type + "]";
	}
}
