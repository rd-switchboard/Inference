package org.rdswitchboard.utils.graph;

public class GraphSchema {
	private String label;
	private String key;
	private boolean unique;
	
	public GraphSchema() {
		
	}
	
	public GraphSchema(String label, String key, boolean unique) {
		this.label = label;
		this.key = key;
		this.unique = unique;
	}
	
	public String getLabel() {
		return label;
	}
	
	public void setLabel(String label) {
		this.label = label;
	}
	
	public String getKey() {
		return key;
	}
	
	public void setKey(String key) {
		this.key = key;
	}
	
	public boolean isUnique() {
		return unique;
	}
	
	public void setUnique(boolean unique) {
		this.unique = unique;
	}

	@Override
	public String toString() {
		return "GraphIndex [label=" + label + ", key=" + key + ", unique="
				+ unique + "]";
	}
}
