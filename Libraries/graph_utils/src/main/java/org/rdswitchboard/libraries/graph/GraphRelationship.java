package org.rdswitchboard.libraries.graph;

import java.util.Map;

/**
 * A class to store single Relationship between two nodes
 * 
 * It consists from:
 *  - relationship: a type of relationship
 *  - start: a set of properties, describing start node
 *  - end: a set of properties, describing end node 
 *  - this: an optional set of relationship properties.
 *  
 * Typically, nodes properties consists from two fields:
 *  - source: name of the source and used to distinguish different subsets
 *  - key: unique key of the node within source. If source does not exists,
 *         the key must be unique within a database.  
 * 
 * @author Dima Kudriavcev (dmitrij@kudriavcev.info)
 * @date 2015-07-24
 * @version 1.0.0
 */

public class GraphRelationship extends GraphProperties {
    private String relationship;
    private GraphKey start;
    private GraphKey end;
    
    public GraphRelationship() {
    	
    }
     
/*    public GraphRelationship(String relationship, GraphProperties start, GraphProperties end) {
    	this.relationship = relationship;
    	this.start = start;
    	this.end = end;
    }
    
    public GraphRelationship(String relationship, GraphProperties start, GraphProperties end, Map<String, Object> properties) {
    	super(properties);
    	
    	this.relationship = relationship;
    	this.start = start;
    	this.end = end;
    }*/


	public String getRelationship() {
		return relationship;
	}

	public void setRelationship(String relationship) {
		this.relationship = relationship;
	}

	public GraphKey getStart() {
		return start;
	}

	public void setStart(GraphKey start) {
		this.start = start;
	}

	public void setStart(String index, Object value) {
		this.start = new GraphKey(index, value);
	}

	public void setStart(String index, String key, Object value) {
		this.start = new GraphKey(index, key, value);
	}
	
	public GraphKey getEnd() {
		return end;
	}

	public void setEnd(GraphKey end) {
		this.end = end;
	}
	
	public void setEnd(String index, Object value) {
		this.end = new GraphKey(index, value);
	}

	public void setEnd(String index, String key, Object value) {
		this.end = new GraphKey(index, key, value);
	}

	public GraphRelationship withRelationship(String relationship) {
		setRelationship(relationship);
		return this;
	}
	
	public GraphRelationship withProperties(Map<String, Object> properties) {
		setProperties(properties);
		return this;
	}
	
	public GraphRelationship withProperty(String key, Object property) {
		setProperty(key, property);
		return this;
	}
	
	public GraphRelationship withStart(GraphKey start) {
		setStart(start);
		return this;
	}

	public GraphRelationship withStart(String index, Object value) {
		setStart(index, value);
		return this;
	}

	public GraphRelationship withStart(String index, String key, Object value) {
		setStart(index, key, value);
		return this;
	}

	public GraphRelationship withEnd(GraphKey end) {
		setEnd(end);
		return this;
	}

	public GraphRelationship withEnd(String index, Object value) {
		setEnd(index, value);
		return this;
	}

	public GraphRelationship withEnd(String index, String key, Object value) {
		setEnd(index, key, value);
		return this;
	}
	
	@Override
	public String toString() {
		return "GraphRelationship [relationship=" + relationship + ", start="
				+ start + ", end=" + end + ", properties=" + properties + "]";
	}
}
