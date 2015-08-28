package org.rdswitchboard.libraries.crossref;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Class to store List of CrossRef Items
 * @author Dmitrij Kudriavcev, dmitrij@kudriavcev.info
 *
 */
public class ItemList {
	private Query query;
	private int itemsPerPage;
	private long totalResults;
	private List<Item> items;
	private Object facets;
	
	public Query getQuery() {
		return query;		
	}
	
	public void setQuery(Query query) {
		this.query = query;
	}
	
	@JsonProperty("items-per-page")
	public int getItemsPerPage() {
		return itemsPerPage;
	}
	
	@JsonProperty("items-per-page")
	public void setItemsPerPage(int itemsPerPage) {
		this.itemsPerPage = itemsPerPage;
	}
	
	public List<Item> getItems() {
		return items;
	}
	
	public void setItems(List<Item> items) {
		this.items = items;
	}
	
	@JsonProperty("total-results")
	public long getTotalResults() {
		return totalResults;
	}
	
	@JsonProperty("total-results")
	public void setTotalResults(long totalResults) {
		this.totalResults = totalResults;
	}
	
	public Object getFacets() {
		return facets;
	}
	
	public void setFacets(Object facets) {
		this.facets = facets;
	}
	
	@Override
	public String toString() {
		return "Message [query=" + query + 
				", itemsPerPage=" + itemsPerPage + 
				", totalResuls=" + totalResults +
				", items=" + items + 
				"]";		
	}	
}
