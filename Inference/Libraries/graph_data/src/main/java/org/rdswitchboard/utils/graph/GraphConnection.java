package org.rdswitchboard.utils.graph;

public class GraphConnection {
	private String source;
	private String type;
	private String key;
	
	public GraphConnection() {
		
	}
	
	public GraphConnection(String source, String type, String key) {
		this.source = source;
		this.type = type;
		this.key = key;
	}
	
	public String getSource() {
		return source;
	}
	
	public void setSource(String source) {
		this.source = source;
	}
	
	public String getType() {
		return type;
	}
	
	public void setType(String type) {
		this.type = type;
	}
	
	public String getKey() {
		return key;
	}
	
	public void setKey(String key) {
		this.key = key;
	}

	@Override
	public String toString() {
		return "GraphConnection [source=" + source + ", type=" + type
				+ ", key=" + key + "]";
	}
}
