package org.rdswitchboard.utils.google.cse;

import java.util.List;

/**
 * Class to store Google query response
 * @author Dmitrij Kudriavcev, dmitrij@kudriavcev.info
 *
 */
public class QueryResponse {
	private String kind;
	private UrlTemplate url;
	private QueryInfo queries;
	private QueryContext context;
	private SearchInformation searchInformation;
	private Spelling spelling;
	
	private List<Item> items;
	
	public String getKind() {
		return kind;
	}
	
	public void setKind(final String kind) {
		this.kind = kind; 
	}
	
	public UrlTemplate getUrl() {
		return url;
	}
	
	public void setUrl(UrlTemplate url) {
		this.url = url;
	}
	
	public QueryInfo getQueries() {
		return queries;
	}
	
	public void setQueries(QueryInfo queries) {
		this.queries = queries;
	}
	
	public QueryContext getContext() {
		return context;
	}

	public void setContext(QueryContext context) {
		this.context = context;
	}

	public SearchInformation getSearchInformation() {
		return searchInformation;
	}
	
	public void setSearchInformation(SearchInformation searchInformation) {
		this.searchInformation = searchInformation;
	}
	
	public List<Item> getItems() {
		return items;
	}

	public void setItems(List<Item> items) {
		this.items = items;
	}
	
	public Spelling getSpelling() {
		return spelling;
	}

	public void setSpelling(Spelling spelling) {
		this.spelling = spelling;
	}


	@Override
	public String toString() {
		return "QueryResponse [kind=" + kind + 
				", url=" + url +
				", queries=" + queries +
				", context=" + context + 
				", searchInformation=" + searchInformation +
				", spelling=" + spelling +
				", items=" + items + "]";
	}	
}
