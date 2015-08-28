package org.rdswitchboard.libraries.scopus.type;

public enum SearchViewType {
	searchViewTypeStandard("STANDARD"), 
	searchViewTypeComplete("COMPLETE");

	private String searchViewName;
	
	private SearchViewType(String searchViewName) {
		this.searchViewName = searchViewName;
    }
     
	@Override
    public String toString(){
		return searchViewName;
    } 
}
