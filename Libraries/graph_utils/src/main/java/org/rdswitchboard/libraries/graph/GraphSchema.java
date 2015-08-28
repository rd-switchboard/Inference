package org.rdswitchboard.libraries.graph;

import java.util.Objects;


/**
 * A class to store schema of the Graph
 * 
 * Typically it has a label used to distinguish a Node by it source or type 
 * and an index name used to create index or constraint by that name.
 * Additional unique flag can be set to indicate what this index must be unique within a label.
 * 
 * @author Dima Kudriavcev (dmitrij@kudriavcev.info)
 * @date 2015-07-24
 * @version 1.0.0
 */

public class GraphSchema {
	private String label;
	private String index;
	private boolean unique;
	
	public GraphSchema() {
		
	}
	
	public GraphSchema(String label, String index, boolean unique) {
		setLabel(label);
		setIndex(index);
		setUnique(unique);
	}
	
	public String getLabel() {
		return label;
	}
	
	public void setLabel(String label) {
		this.label = label;
	}
		 
	public GraphSchema withLabel(String label) {
		setLabel(label);
		return this;
	}
	
	public String getIndex() {
		return index;
	}
	
	public void setIndex(String index) {
		this.index = index;
	}

	public GraphSchema withIndex(String index) {
		setIndex(index);
		return this;
	}
	 
	public boolean isUnique() {
		return unique;
	}
	
	public void setUnique(boolean unique) {
		this.unique = unique;
	}

	public GraphSchema withUnique(boolean unique) {
		setUnique(unique);
		return this;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (null == obj)
			return false;
		if (obj instanceof GraphSchema) 
			return this.unique == ((GraphSchema) obj).unique
					&& Objects.equals(this.label, ((GraphSchema) obj).label)
					&& Objects.equals(this.index, ((GraphSchema) obj).index);
		
		return false;
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.unique, this.label, this.index);
	}

	@Override
	public String toString() {
		return "GraphSchema [label=" + label + ", index=" + index + ", unique="
				+ unique + "]";
	}
}
