package org.rdswitchboard.exporters.harmonized;

import org.neo4j.graphdb.Label;

public class DoubleLabel {
	private final Label source;
	private final Label type;
	
	public DoubleLabel(final Label source, final Label type) {
		this.source = source;
		this.type = type;
	}

	public Label getSource() {
		return source;
	}

	public Label getType() {
		return type;
	}

	@Override
	public String toString() {
		return "DoubleLabel [source=" + source + ", type=" + type + "]";
	}
}
