package org.rdswitchboard.linkers.neo4j.web.researcher.model;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.Hibernate;
import org.springframework.transaction.annotation.Transactional;

@Entity
@Table(name="results")
public class Result {
	// if fail, google `hibernate composite key`
	
	private byte level;
	private boolean found;
	private ResultId resultId;
	
	private Link link;
	private Text text;
	
	@EmbeddedId
	public ResultId getResultId() {
		return resultId;
	}

	public void setResultId(ResultId resultId) {
		this.resultId = resultId;
	}

	public byte getLevel() {
		return level;
	}
	
	public void setLevel(byte level) {
		this.level = level;
	}
	
	public boolean isFound() {
		return found;
	}
	
	public void setFound(boolean found) {
		this.found = found;
	}
	
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="link_id", nullable=false, insertable=false, updatable=false)
	public Link getLink() {
		return link;
	}

	public void setLink(Link link) {
		this.link = link;
	}

	@ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="text_id", nullable=false, insertable=false, updatable=false)
	public Text getText() {
		return text;
	}

	public void setText(Text text) {
		this.text = text;
	}

	@Override
	public String toString() {
		return "Result [level=" + level + ", found=" + found + ", resultId=" + resultId + "]";
	}
}
