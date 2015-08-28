package org.rdswitchboard.libraries.graph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

public class Graph {
	private List<GraphNode> nodes;
	private List<GraphRelationship> relationships;
	private Set<GraphSchema> schemas;
	
	public int getNodesCount() {
		return null == nodes? 0 : nodes.size();
	}
	
	public Collection<GraphNode> getNodes() {
		return nodes;
	}

	public void setNodes(Collection<GraphNode> nodes) {
		this.nodes = new ArrayList<GraphNode>(nodes);
	}

	public Collection<GraphRelationship> getRelationships() {
		return relationships;
	}

	public int getRelationshipsCount() {
		return null == relationships? 0 : relationships.size();
	}

	public void setRelationships(Collection<GraphRelationship> relationships) {
		this.relationships = new ArrayList<GraphRelationship>(relationships);
	}
	
	public int getSchemasCount() {
		return null == schemas? 0 : schemas.size();
	}	

	public Collection<GraphSchema> getSchemas() {
		return schemas;
	}
	
	public void setSchemas(Collection<GraphSchema> schemas) {
		this.schemas = new HashSet<GraphSchema>(schemas);
	}

	public void addNode(GraphNode node) {
		if (null == nodes) 
			nodes = new ArrayList<GraphNode>();
		nodes.add(node);
	}
	
	public void addRelationship(GraphRelationship relationship) {
		if (null == relationships) 
			relationships = new ArrayList<GraphRelationship>();
		relationships.add(relationship);
	}
	
	public void addSchema(GraphSchema schema) {
		if (null == schemas) 
			schemas = new HashSet<GraphSchema>();
		schemas.add(schema);
	}

	@Override
	public String toString() {
		return "Graph [nodes=" + nodes + ", relationships=" + relationships
				+ ", schemas=" + schemas + "]";
	}	
}
