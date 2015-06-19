package org.rdswitchboard.utils.graph;

public class GraphIndex {
	private String index;
	private String key;
	private String value;
	
	public GraphIndex() {
		
	}
	
	public GraphIndex(String index, String key, String value) {
		this.index = index;
		this.key = key;
		this.value = value;
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

	public void setKey(String key) {
		this.key = key;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return "GraphIndex [index=" + index + ", key=" + key + ", value="
				+ value + "]";
	}
}
