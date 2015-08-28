package org.rdswitchboard.libraries.graph;

import java.util.Set;
import java.util.HashSet;
import java.util.Map;

/**
 * 
 * @author dima
 *
 */

public class GraphNode extends GraphProperties {
	private GraphKey key;
	private final Set<GraphKey> indexes = new HashSet<GraphKey>();
	
	public GraphNode() {
	}
	
	public GraphNode(Map<String, Object> properties) {
		super(properties);
	}
	
	public boolean hasKey() {
		return null != key;
	}
	
	public GraphKey getKey() {
		return key;
	}

	public void setKey(GraphKey key) {
		this.key = key;
	}
	
	public void setKey(String index, Object value) {
		this.key = new GraphKey(index, value);
	}

	public void setKey(String index, String key, Object value) {
		this.key = new GraphKey(index, key, value);
	}

	public Set<GraphKey> getIndexes() {
		return indexes;
	}
	
	/**
	 * Function will return any existing key
	 * @return GraphKey
	 */
/*	public GraphKey getAnyKey() {
		return indexes.isEmpty() ? null : indexes.iterator().next();
	}	*/
	
	public void addIndex(GraphKey index) {
		indexes.add(index);
	}
	
	public void addIndex(String index, Object value) {
		indexes.add(new GraphKey(index, value));
	}

	public void addIndex(String index, String key, Object value) {
		indexes.add(new GraphKey(index, key, value));
	}

	/*public boolean hasKey() {
		return connection.hasKey();
	}
	
	public Object getKey() {
		return connection.getValue();
	}
	
	public void setKey(Object key) {
		connection.setValue(key);
		setProperty(GraphUtils.PROPERTY_KEY, key);
	}
	
	public void setKeyOnce(Object key) {
		if (!connection.hasKey()) {
			connection.setValue(key);
			setProperty(GraphUtils.PROPERTY_KEY, key);
		}
	}
	
	public boolean hasIndex() {
		return hasProperty(GraphUtils.PROPERTY_INDEX);
	}
	
	public String getIndex() {
		return (String) getProperty(GraphUtils.PROPERTY_INDEX);
	}
	
	public void setIndex(String index) {
		setProperty(GraphUtils.PROPERTY_INDEX, index);
	}*/

	public boolean hasSource() {
		return hasProperty(GraphUtils.PROPERTY_SOURCE);
	}
	
	public Object getSource() {
		return getProperty(GraphUtils.PROPERTY_SOURCE);
	}
	
	public void setSource(Object source) {
		setProperty(GraphUtils.PROPERTY_SOURCE, source);
	}

	public void addSource(Object source) {
		addProperty(GraphUtils.PROPERTY_SOURCE, source);
	}

	public boolean hasType() {
		return hasProperty(GraphUtils.PROPERTY_TYPE);
	}
	
	public Object getType() {
		return getProperty(GraphUtils.PROPERTY_TYPE);
	}
	
	public void setType(Object type) {
		setProperty(GraphUtils.PROPERTY_TYPE, type);
	}

	public void addType(Object type) {
		addProperty(GraphUtils.PROPERTY_TYPE, type);
	}
	
	public String[] getLabels() {
		Set<String> labels = new HashSet<String>();
		
		for (Object source : properties.get(GraphUtils.PROPERTY_SOURCE))
			labels.add((String) source);
		for (Object type : properties.get(GraphUtils.PROPERTY_TYPE))
			labels.add((String) type);
		
		return labels.toArray(new String[labels.size()]);
	}

	public boolean isDeleted() {
		Object deleted = getProperty(GraphUtils.PROPERTY_DELETED);
		return null != deleted && (Boolean) deleted;
	}

	public boolean isBroken() {
		Object broken = getProperty(GraphUtils.PROPERTY_BROKEN);
		return null != broken && (Boolean) broken;
	}
	
	public void setDeleted(boolean deleted) {
		setProperty(GraphUtils.PROPERTY_DELETED, deleted);
	}

	public void setBroken(boolean broken) {
		setProperty(GraphUtils.PROPERTY_BROKEN, broken);
	}

	public GraphNode withKey(GraphKey key) {
		setKey(key);
		return this;
	}
	
	public GraphNode withKey(String index, Object value) {
		setKey(index, value);
		return this;
	}

	public GraphNode withKey(String index, String key, Object value) {
		setKey(index, key, value);
		return this;
	}
	
	public GraphNode withIndex(GraphKey index) {
		addIndex(index);
		return this;
	}
	
	public GraphNode withIndex(String index, Object value) {
		addIndex(index, value);
		return this;
	}

	public GraphNode withIndex(String index, String key, Object value) {
		addIndex(index, key, value);
		return this;
	}
	
	public GraphNode withProperties(Map<String, Object> properties) {
		setProperties(properties);
		return this;
	}
	
	public GraphNode withProperty(String key, Object property) {
		setProperty(key, property);
		return this;
	}
	
	/*public GraphNode withKey(String key) {
		setKey(key);
		return this;
	}
	
	public GraphNode withIndex(String index) {
		setIndex(index);
		return this;
	}*/
	
	public GraphNode withSource(String source) {
		setSource(source);
		return this;
	}

	public GraphNode withType(String type) {
		setType(type);
		return this;
	}
	
	public GraphNode withDeleted(boolean deleted) {
		setDeleted(deleted);
		return this;
	}

	public GraphNode withBroken(boolean broken) {
		setBroken(broken);
		return this;
	}

	@Override
	public String toString() {
		return "GraphNode [key=" + key + ", indexes=" + indexes + ", properties=" + properties + "]";
	}
}
