package org.rdswitchboard.linkers.neo4j.web.researcher;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.rdswitchboard.utils.google.cache2.Link;

public class MatcherResult {
	private Link link;
	private Set<Long> nodes;
	
	public Link getLink() {
		return link;
	}
	
	public void setLink(Link link) {
		this.link = link;
	}
	
	public Set<Long> getNodes() {
		return nodes;
	}
	
	public void addNodes(Collection<Long> nodes) {
		if (null == this.nodes)
			this.nodes = new HashSet<Long>();
		this.nodes.addAll(nodes);
	}

	public boolean hasNodes() {
		return null != this.nodes && !this.nodes.isEmpty();
	}
}
