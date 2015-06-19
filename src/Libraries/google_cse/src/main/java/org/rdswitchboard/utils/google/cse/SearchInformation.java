package org.rdswitchboard.utils.google.cse;

/**
 * Class to store Google search information
 * @author Dmitrij Kudriavcev, dmitrij@kudriavcev.info
 *
 */
public class SearchInformation {
	private double searchTime;
	private String formattedSearchTime;
	private String totalResults;
	private String formattedTotalResults;
	
	public double getSearchTime() {
		return searchTime;
	}
	
	public void setSearchTime(double searchTime) {
		this.searchTime = searchTime;
	}
	
	public String getFormattedSearchTime() {
		return formattedSearchTime;
	}
	
	public void setFormattedSearchTime(final String formattedSearchTime) {
		this.formattedSearchTime = formattedSearchTime;
	}
	
	public String getTotalResults() {
		return totalResults;
	}
	
	public void setTotalResults(final String totalResults) {
		this.totalResults = totalResults;
	}
	
	public String getFormattedTotalResults() {
		return formattedTotalResults;
	}
	
	public void setFormattedTotalResults(final String formattedTotalResults) {
		this.formattedTotalResults = formattedTotalResults;
	}
	
	@Override
	public String toString() {
		return "SearchInformation [searchTime=" + searchTime +
				", formattedSearchTime=" + formattedSearchTime + 
				", totalResults=" + totalResults +
				", formattedTotalResults" + formattedTotalResults + "]";
	}
}
