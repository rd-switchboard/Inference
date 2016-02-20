package org.rdswitchboard.linkers.neo4j.web.researcher;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.rdswitchboard.utils.google.cache2.Link;

public class MatcherResult {
	private Link link;
	private long elapsedTime = 0;
	private String error = null;
	private Set<Long> nodes = null;
	
	public Link getLink() {
		return link;
	}
	
	public void setLink(Link link) {
		this.link = link;
	}
	
	public Set<Long> getNodes() {
		return nodes;
	}
	
	public long getNodesSize() {
		return null == nodes ? 0 : nodes.size();
	}
	
	public void addNodes(Collection<Long> nodes) {
		if (null == this.nodes)
			this.nodes = new HashSet<Long>();
		this.nodes.addAll(nodes);
	}

	public void startWatch() {
		elapsedTime = System.currentTimeMillis();
	}
	
	public void stopWatch() {
		if (0 != elapsedTime)
			elapsedTime = System.currentTimeMillis() - elapsedTime;
	}
	
	public long getElapsedTime() {
		return elapsedTime;
	}
	
	public boolean isValid() {
		return null == error && null != nodes && nodes.size() > 0;
	}
	
	public boolean isError() {
		return null == error;
	}
	
	public String getError() {
		return error;
	}
	
	public void setError(String error) {
		this.error = error;
	}
	
	public boolean hasNodes() {
		return null != this.nodes && !this.nodes.isEmpty();
	}
}
