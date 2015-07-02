package org.rdswitchboard.utils.neo4j;

public class Relation {
	private String keyFrom;
	private String keyTo;
	private String relationName;

	public String getKeyFrom() {
		return keyFrom;
	}

	public void setKeyFrom(String keyFrom) {
		this.keyFrom = keyFrom;
	}

	public String getKeyTo() {
		return keyTo;
	}

	public void setKeyTo(String keyTo) {
		this.keyTo = keyTo;
	}

	public String getRelationName() {
		return relationName;
	}

	public void setRelationName(String relationName) {
		this.relationName = relationName;
	}

	@Override
	public String toString() {
		return "Relation [keyFrom=" + keyFrom + ", keyTo=" + keyTo
				+ ", relationName=" + relationName + "]";
	}
}
