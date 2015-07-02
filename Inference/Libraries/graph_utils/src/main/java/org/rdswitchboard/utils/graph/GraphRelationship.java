package org.rdswitchboard.utils.graph;

import java.util.Map;

public class GraphRelationship {
	private GraphIndex start;
	private GraphIndex end;
	private String relationship;
	private Map<String, Object> properties;

	public GraphRelationship() {
		
	}
	
	public GraphRelationship(String relationship, Map<String, Object> properties, 
			GraphIndex start, GraphIndex end) {
		this.relationship = relationship;
		this.properties = properties;
		this.start = start;
		this.end = end;		
	}
	
	public GraphIndex getStart() {
		return start;
	}

	public void setStart(GraphIndex start) {
		this.start = start;
	}

	public GraphIndex getEnd() {
		return end;
	}

	public void setEnd(GraphIndex end) {
		this.end = end;
	}
	
	public String getRelationship() {
		return relationship;
	}

	public void setRelationship(String relationship) {
		this.relationship = relationship;
	}

	public Map<String, Object> getProperties() {
		return properties;
	}

	public void setProperties(Map<String, Object> properties) {
		this.properties = properties;
	}

	@Override
	public String toString() {
		return "GraphRelationsip [start=" + start
				+ ", end=" + end 
				+ ", relationship=" + relationship
				+ ", properties=" + properties + "]";
	}
}
