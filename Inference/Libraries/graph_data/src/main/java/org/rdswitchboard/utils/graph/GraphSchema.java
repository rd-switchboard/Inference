package org.rdswitchboard.utils.graph;

import java.util.HashSet;
import java.util.Set;

public class GraphSchema {
	private Set<String> indexes;
	
	public void addIndex(String source, String type) {
		if (null == indexes)
			indexes = new HashSet<String>();
		
		indexes.add(source + "_" + type);
	}
	
	public void addIndex(GraphConnection connection) {
		addIndex(connection.getSource(), connection.getType());
	}

	public Set<String> getIndexes() {
		return indexes;
	}

	public void setIndexes(Set<String> indexes) {
		this.indexes = indexes;
	}

	@Override
	public String toString() {
		return "GraphSchema [indexes=" + indexes + "]";
	}
}
