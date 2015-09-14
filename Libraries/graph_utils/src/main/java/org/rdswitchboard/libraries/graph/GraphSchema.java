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
	private String index;
	private String key;
	private boolean unique;
	
	public GraphSchema() {
		this.index = null;
		this.key= GraphUtils.PROPERTY_KEY;
		this.unique = false;
	}
	
	public GraphSchema(String index, boolean unique) {
		setIndex(index);
		this.key = GraphUtils.PROPERTY_KEY;
		setUnique(unique);
	}
	
	public GraphSchema(String index, String key, boolean unique) {
		setIndex(index);
		setKey(key);
		setUnique(unique);
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
	
	public String getKey() {
		return key;
	}
	
	public void setKey(String key) {
		this.key = key;
	}
		 
	public GraphSchema withKey(String key) {
		setKey(key);
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
					&& Objects.equals(this.index, ((GraphSchema) obj).index)
					&& Objects.equals(this.key, ((GraphSchema) obj).key);
		
		return false;
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.unique, this.index, this.key);
	}

	@Override
	public String toString() {
		return "GraphSchema [index=" + index + ", key=" + key + ", unique="
				+ unique + "]";
	}
}
