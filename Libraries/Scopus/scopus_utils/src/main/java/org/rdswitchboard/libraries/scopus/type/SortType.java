package org.rdswitchboard.libraries.scopus.type;

public enum SortType {
	/*coverDateAsc("+coverDate"), 
	coverDateDes("-coverDate"), 
	publicationNameAcs("+publicationName"), 
	publicationNameDes("-publicationName"),
	relevancyAsc("+relevancy"),
	relevancyDes("-relevancy"),
	sortOrderAsc("+sort-order"),
	sortOrderDes("-sort-order");*/
	
	artnumAsc("artnum", true),
	artnumDes("artnum", false),
	auciteAsc("aucite", true),
	auciteDes("aucite", false),
	citedbyCountAsc("citedby-count", true),
	citedbyCountDes("citedby-count", false),
	coverDateAsc("coverDate", true),
	coverDateDes("coverDate", false),
	creatorAsc("creator", true),
	creatorDes("creator", false),
	origLoadDateAsc("orig-load-date", true),
	origLoadDateDes("orig-load-date", false),
	pagecountAsc("pagecount", true), 
	pagecountDes("pagecount", false),
	pagefirstAsc("pagefirst", true),
	pagefirstDes("pagefirst", false),
	pageRangeAsc("pageRange", true),
	pageRangeDsc("pageRange", false),
	publicationNameAsc("publicationName", true),
	publicationNameDes("publicationName", false),
	pubyearAsc("pubyear", true),
	pubyearDes("pubyear", false),
	relevancyAsc("relevancy", true),
	relevancyDes("relevancy", false),
	titleAsc("title", true),
	titleDes("title", false), 
	volumeAsc("volume", true),
	volumeDes("volume", false);
	
	private final String sortName;
	private final boolean ascending;
	
	private SortType(final String sortName, final boolean ascending) {
		this.sortName = sortName;
		this.ascending = ascending;
    }
     
	public String getSortName() {
		return sortName;
	}
	
	public boolean isAscending() {
		return ascending;
	}
	
	@Override
    public String toString() {
		return (ascending ? "+" : "-") + sortName;
    } 
}
