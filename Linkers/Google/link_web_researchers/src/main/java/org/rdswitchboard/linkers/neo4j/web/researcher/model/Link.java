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
@Table(name = "links", uniqueConstraints = {
		@UniqueConstraint(columnNames = "link", name = "uk_link")
	})
public class Link {

	private long linkId;
	private String link;
	private String data;
	private String metadata;
	
	private List<Result> results;

	@Id
	@Column(name="link_id")
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	public long getLinkId() {
		return linkId;
	}
	
	public void setLinkId(long linkId) {
		this.linkId = linkId;
	}
	
	public String getLink() {
		return link;
	}
	
	public void setLink(String link) {
		this.link = link;
	}
	
	public String getData() {
		return data;
	}
	
	public void setData(String data) {
		this.data = data;
	}
	
	public String getMetadata() {
		return metadata;
	}

	public void setMetadata(String metadata) {
		this.metadata = metadata;
	}
	
	@OneToMany(cascade=CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "link")
	public List<Result> getResults() {
		return results;
	}

	public void setResults(List<Result> results) {
		this.results = results;
	}

	@Override
	public String toString() {
		return "Link [linkId=" + linkId + ", link=" + link + ", data=" + data + ", metadata=" + metadata + "]";
	}
	
	public boolean isDataEquals(String data) {
		if (this.data == null) {
			if (data != null)
				return false;
		} else if (!this.data.equals(data))
			return false;
		return true;
	}
	
	public boolean isMetadataEquals(String metadata) {
		if (this.metadata == null) {
			if (metadata != null)
				return false;
		} else if (!this.metadata.equals(metadata))
			return false;
		return true;
	}
}
