package org.rdswitchboard.importers.web.researchers;

import java.util.regex.Pattern;

public class LinkingNode {
	private final String title;
	private final String type;
	private final String titleLowerCase;
	private final Pattern pattern;
	private final long nodeId;
	private int counter;
	
	public LinkingNode(long nodeId, String title, String type) {
		this.nodeId = nodeId;
		this.title = title;
		this.type = type;
		
		titleLowerCase = title.toLowerCase();
		pattern = Pattern.compile(titleLowerCase.replaceAll("[^a-z0-9]", "."));
		counter = 0;
	}
	
	public void incCounter() {
		++counter;
	}
	
	public boolean isUnique() {
		return 0 == counter;
	}
	
	public String getTitle() {
		return title;
	}
	
	public String getType() {
		return type;
	}
	public String getTitleLowerCase() {
		return titleLowerCase;
	}	
	
	public Pattern getPattern() {
		return pattern;
	}
	
	public long getNodeId() {
		return nodeId;
	}
	
	@Override
	public String toString() {
		return "RDA:Grant [type=" + type 
				+ ", title=" + title 
				+ ", pattern=" + pattern 
				+ ", nodeId=" + nodeId + "]";
	}	
}
