package org.rdswitchboard.libraries.graph;

import java.util.Objects;

public class GraphKey implements Cloneable {
	private String index;
	private String key;
	private Object value;
	
	public GraphKey() {
		this.key = GraphUtils.PROPERTY_KEY;
	}
	
	public GraphKey(String index, Object value) {
		this.index = index;
		this.key = GraphUtils.PROPERTY_KEY;
		this.value = value;
	}

	public GraphKey(String index, String key, Object value) {
		this.index = index;
		this.key = key;
		this.value = value;
	}
	
	public void copy(GraphKey t) {
		if (null != t && this != t) { 
			this.index = t.index;
			this.key = t.key;
			this.value = t.value;
		}
	}
	
	public String getIndex() {
		return index;
	}
	
	public void setIndex(String index) {
		this.index = index;
	}
	
	public String getKey() {
		return key;
	}

	public boolean hasKey() {
		return null != key;
	}

	public void setKey(String key) {
		this.key = key;
	}
	
	public Object getValue() {
		return value;
	}
	
	public void setValue(Object value) {
		this.value = value;
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.index, this.key, this.value);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (null == obj)
			return false;
		if (obj instanceof GraphKey) {
			GraphKey other = (GraphKey) obj;
			
			return Objects.equals(this.index, other.index)
					&& Objects.equals(this.key, other.key)
					&& Objects.equals(this.value, other.value);
		}
		
		return false;
	}
	
	@Override
	public Object clone() throws CloneNotSupportedException {
		return new GraphKey(index, key, value); 
	}

	@Override
	public String toString() {
		return "index=" + index + ", key=" + key + ", value="
				+ value;
	}
}
