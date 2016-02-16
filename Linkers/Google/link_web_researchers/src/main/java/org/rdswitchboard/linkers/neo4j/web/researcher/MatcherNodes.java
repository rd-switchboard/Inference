package org.rdswitchboard.linkers.neo4j.web.researcher;

import java.util.HashSet;
import java.util.Set;

public class MatcherNodes {
	private final Set<Long> nodes = new HashSet<Long>();
	
	public MatcherNodes(){
		
	}
	
	public MatcherNodes(long node) {
		nodes.add(node);
	}
	
	public void addNode(long node) {
		nodes.add(node);
	}
	
	public Set<Long> getNodes() {
		return nodes;
	}

	@Override
	public String toString() {
		return "MatcherNodes [nodes=" + nodes + "]";
	}
}
