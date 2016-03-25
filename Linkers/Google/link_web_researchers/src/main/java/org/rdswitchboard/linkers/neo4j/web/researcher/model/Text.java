package org.rdswitchboard.linkers.neo4j.web.researcher.model;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;


@Entity
@Table(name = "texts", uniqueConstraints = {
		@UniqueConstraint(columnNames = "text", name = "uk_text")
	})
public class Text {
	
	private long textId;
	private String text;
	private List<Result> results;

	@Id
	@Column(name="text_id")
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	public long getTextId() {
		return textId;
	}

	public void setTextId(long textId) {
		this.textId = textId;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}
	
	@OneToMany(cascade=CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "text")
	public List<Result> getResults() {
		return results;
	}

	public void setResults(List<Result> results) {
		this.results = results;
	}

	@Override
	public String toString() {
		return "Text [textId=" + textId + ", text=" + text + "]";
	}
}
