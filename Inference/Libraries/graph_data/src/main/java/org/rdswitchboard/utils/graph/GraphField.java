package org.rdswitchboard.utils.graph;

public class GraphField {
	private GraphConnection conn;
	private Object field;
	
	public GraphField() {
		
	}
	
	public GraphField(GraphConnection conn, Object field) {
		this.conn = conn;
		this.field = field;
	}

	public GraphConnection getConn() {
		return conn;
	}

	public void setConn(GraphConnection conn) {
		this.conn = conn;
	}

	public Object getField() {
		return field;
	}

	public void setField(Object field) {
		this.field = field;
	}

	@Override
	public String toString() {
		return "GraphField [conn=" + conn + ", field=" + field + "]";
	}
}
