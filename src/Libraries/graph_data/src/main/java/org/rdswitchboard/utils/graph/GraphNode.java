package org.rdswitchboard.utils.graph;

import java.util.Map;

public class GraphNode {
	private Map<String, Object> properties;

	public GraphNode() {
		
	}
	
	public GraphNode(Map<String, Object> properties) {
		this.properties = properties;
	}

	public Map<String, Object> getProperties() {
		return properties;
	}
	
	public void setProperties(Map<String, Object> properties) {
		this.properties = properties;
	}
	
	@Override
	public String toString() {
		return "GraphNode [properties=" + properties + "]";
	}
}
