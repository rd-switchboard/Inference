package org.rdswitchboard.utils.graph;

import java.util.Map;

public class GraphRelationship {
	private GraphConnection start;
	private GraphConnection end;
	private String relationship;
	private Map<String, Object> properties;

	public GraphRelationship() {
		
	}
	
	public GraphRelationship(String relationship, Map<String, Object> properties, 
			GraphConnection start, GraphConnection end) {
		this.relationship = relationship;
		this.properties = properties;
		this.start = start;
		this.end = end;		
	}
	
	public GraphConnection getStart() {
		return start;
	}

	public void setStart(GraphConnection start) {
		this.start = start;
	}

	public GraphConnection getEnd() {
		return end;
	}

	public void setEnd(GraphConnection end) {
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
