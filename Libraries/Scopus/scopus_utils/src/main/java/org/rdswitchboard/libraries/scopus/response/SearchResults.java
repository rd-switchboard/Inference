package org.rdswitchboard.libraries.scopus.response;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

@JsonRootName("search-results") 
public class SearchResults {
	private Long itemsPerPage;
	private Long startIndex; 
	private Long totalResults;
	
	private Query query;
	private List<Entry> entries;
	private List<Link> links;
		
	public Long getItemsPerPage() {
		return itemsPerPage;
	}
	
	@JsonProperty("opensearch:itemsPerPage")
	public void setItemsPerPage(Long itemsPerPage) {
		this.itemsPerPage = itemsPerPage;
	}
	
	public Long getStartIndex() {
		return startIndex;
	}
	
	@JsonProperty("opensearch:startIndex")
	public void setStartIndex(Long startIndex) {
		this.startIndex = startIndex;
	}
	
	public Long getTotalResults() {
		return totalResults;
	}
	
	@JsonProperty("opensearch:totalResults")
	public void setTotalResults(Long totalResults) {
		this.totalResults = totalResults;
	}
	
	public Query getQuery() {
		return query;
	}
	
	@JsonProperty("opensearch:Query")
	public void setQuery(Query query) {
		this.query = query;
	}
	
	public List<Entry> getEntries() {
		return entries;
	}
	
	@JsonProperty("entry")
	public void setEntries(List<Entry> entries) {
		this.entries = entries;
	}
	
	public List<Link> getLinks() {
		return links;
	}
	
	@JsonProperty("link")
	public void setLinks(List<Link> links) {
		this.links = links;
	}

	@Override
	public String toString() {
		return "SearchResults [itemsPerPage=" + itemsPerPage + ", startIndex="
				+ startIndex + ", totalResults=" + totalResults + ", query="
				+ query + ", entries=" + entries + ", links=" + links + "]";
	}	
}
