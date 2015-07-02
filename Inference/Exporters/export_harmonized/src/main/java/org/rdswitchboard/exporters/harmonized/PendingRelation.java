package org.rdswitchboard.exporters.harmonized;

import org.neo4j.graphdb.RelationshipType;

public class PendingRelation {
	private final long nodeId;
	private final RelationshipType type;

	public PendingRelation(long nodeId, RelationshipType type) {
		this.nodeId = nodeId;
		this.type = type;
	}

	public long getNodeId() {
		return nodeId;
	}


	public RelationshipType getType() {
		return type;
	}

	@Override
	public String toString() {
		return "PendingRelation [nodeId=" + nodeId + ", type=" + type + "]";
	}
}
