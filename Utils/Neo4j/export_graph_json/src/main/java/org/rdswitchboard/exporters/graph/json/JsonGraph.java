package org.rdswitchboard.exporters.graph.json;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Graph in JSON format
 * 
 * Will contain 2 arrays : arrays of {@link JsonGraph#nodes} and array of {@link JsonGraph#relationships}
 *
 * @version 3.0.0
 * @author Dima Kudriavcev (dmitrij@kudriavcev.info)
 * @date 24 May 2015 	
 *
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class JsonGraph {
	private List<JsonNode> nodes;
	private List<JsonRelationship> relationships;
	
	public List<JsonNode> getNodes() {
		return nodes;
	}
	
	public List<JsonRelationship> getRelationships() {
		return relationships;
	}	
	
	public void addNode(JsonNode node) {
		if (null == nodes)
			nodes = new ArrayList<JsonNode>();
		
		nodes.add(node);
	}
	
	public void addRelationship(JsonRelationship relationship) {
		if (null == relationships)
			relationships = new ArrayList<JsonRelationship>();
		
		relationships.add(relationship);
	}		
}
