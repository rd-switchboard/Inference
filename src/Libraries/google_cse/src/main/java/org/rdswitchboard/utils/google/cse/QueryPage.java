package org.rdswitchboard.utils.google.cse;

/**
 * Class to store Google query page
 * @author Dmitrij Kudriavcev, dmitrij@kudriavcev.info
 *
 */
public class QueryPage {
	private long count;
	private long startIndex;
	private String title;
	private String totalResults;
	private String searchTerms;
	private String inputEncoding;
	private String outputEncoding;
	private String safe;
	private String cx;

	public long getCount() {
		return count;
	}
	
	public void setCount(long count) {
		this.count = count;
	}
	
	public long getStartIndex() {
		return startIndex;
	}
	
	public void setStartIndex(long startIndex) {
		this.startIndex = startIndex;
	}
	
	public String getTitle() {
		return title;
	}
	
	public void setTitle(final String title) {
		this.title = title;
	}
	
	public String getTotalResults() {
		return totalResults;
	}
	
	public void setTotalResults(final String totalResults) {
		this.totalResults = totalResults;
	}
	
	public String getSearchTerms() {
		return searchTerms;
	}
	
	public void setSearchTerms(final String searchTerms) {
		this.searchTerms = searchTerms;
	}

	public String getInputEncoding() {
		return inputEncoding;
	}
	
	public void setInputEncoding(final String inputEncoding) {
		this.inputEncoding = inputEncoding;
	}
	
	public String getOutputEncoding() {
		return outputEncoding;
	}
	
	public void setOutputEncoding(final String outputEncoding) {
		this.outputEncoding = outputEncoding;
	}
	
	public String getSafe() {
		return safe;
	}
	
	public void setSafe(final String safe) {
		this.safe = safe;
	}
	
	public String getCx() {
		return cx;
	}
	
	public void setCx(final String cx) {
		this.cx = cx;
	}
	
	@Override
	public String toString() {
		return "QueryPage [count=" + count + 
				", startIndex=" + startIndex + 
				", title=" + title + 
				", totalResults=" + totalResults +
				", searchTerms=" + searchTerms +
				", inputEncoding=" + inputEncoding +
				", outputEncoding=" + outputEncoding +
				", safe=" + safe + 
				", cx=" + cx + "]";
	}
}
