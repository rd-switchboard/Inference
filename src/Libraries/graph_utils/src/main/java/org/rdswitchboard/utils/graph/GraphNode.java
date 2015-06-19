package org.rdswitchboard.utils.graph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class GraphNode {
	private Set<String> labels;
	private Map<String, Object> properties;
	private List<GraphIndex> indexes;

	public GraphNode() {
		
	}
	
	public GraphNode(Map<String, Object> properties) {
		setProperties(properties);
	}

	public GraphNode(Set<String> labels, Map<String, Object> properties) {
		setLabels(labels);
		setProperties(properties);
	}
	
	public GraphNode(Set<String> labels, Map<String, Object> properties, List<GraphIndex> indexes) {
		setLabels(labels);
		setProperties(properties);
		setIndexes(indexes);
	}
	
	public GraphNode(Set<String> labels, Map<String, Object> properties, GraphIndex index) {
		setLabels(labels);
		setProperties(properties);
		addIndex(index);
	}

	public GraphNode(String label, Map<String, Object> properties, GraphIndex index) {
		addLabel(label);
		setProperties(properties);
		addIndex(index);
	}

	
	public Set<String> getLabels() {
		return labels;
	}

	public void setLabels(Set<String> labels) {
		this.labels = labels;
	}

/*	public void setLabels(Collection<String> labels) {
		if (labels != null && !labels.isEmpty())
			this.labels = new HashSet<String>(labels);
	}*/

	public void addLabel(String label) {
		if (null == this.labels) 
			this.labels = new HashSet<String>();
		this.labels.add(label);
	}
	
	public Map<String, Object> getProperties() {
		return properties;
	}
	
	public void setProperties(Map<String, Object> properties) {
		this.properties = properties;
	}

	public void setProperty(String key, Object value) {
		if (null == this.properties)
			this.properties = new HashMap<String, Object>();
		this.properties.put(key, value);
	}
	
	public List<GraphIndex> getIndexes() {
		return indexes;
	}

	public void setIndexes(List<GraphIndex> indexes) {
		this.indexes = indexes;
	}
	
	/*public void setIndexes(Collection<GraphIndex> indexes) {
		this.indexes = new ArrayList<GraphIndex>(indexes);
	}*/
	
	public void addIndex(GraphIndex index) {
		if (null == this.indexes)
			this.indexes = new ArrayList<GraphIndex>();
		this.indexes.add(index);
	}

	@Override
	public String toString() {
		return "GraphNode [labels=" + labels + ", properties=" + properties
				+ ", indexes=" + indexes + "]";
	}
}
