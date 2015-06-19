package org.rdswitchboard.utils.graph;

public class GraphField {
	private GraphIndex index;
	private Object field;
	
	public GraphField() {
		
	}
	
	public GraphField(GraphIndex index, Object field) {
		this.index = index;
		this.field = field;
	}

	public GraphIndex getConn() {
		return index;
	}

	public void setConn(GraphIndex index) {
		this.index = index;
	}

	public Object getField() {
		return field;
	}

	public void setField(Object field) {
		this.field = field;
	}

	@Override
	public String toString() {
		return "GraphField [index=" + index + ", field=" + field + "]";
	}
}
