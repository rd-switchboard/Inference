package org.rdswitchboard.linkers.neo4j.web.researcher.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class ResultId implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8438216870022646550L;
	
	private long linkId;
	private long textId;
	
	public ResultId() {
		
	}
	
	public ResultId(long linkId, long textId) {
		this.linkId = linkId;
		this.textId = textId;
	}

	@Column(name="link_id", insertable=false, updatable=false)
	public long getLinkId() {
		return linkId;
	}

	public void setLinkId(long linkId) {
		this.linkId = linkId;
	}

	@Column(name="text_id", insertable=false, updatable=false)
	public long getTextId() {
		return textId;
	}

	public void setTextId(long textId) {
		this.textId = textId;
	}

	@Override
	public String toString() {
		return "ResultId [linkId=" + linkId + ", textId=" + textId + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (linkId ^ (linkId >>> 32));
		result = prime * result + (int) (textId ^ (textId >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ResultId other = (ResultId) obj;
		if (linkId != other.linkId)
			return false;
		if (textId != other.textId)
			return false;
		return true;
	}
}
