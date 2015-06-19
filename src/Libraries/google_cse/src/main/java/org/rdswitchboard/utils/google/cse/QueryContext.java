package org.rdswitchboard.utils.google.cse;

/**
 * Class to store Google query context
 * @author Dmitrij Kudriavcev, dmitrij@kudriavcev.info
 *
 */
public class QueryContext {
	private String title;
	
	public String getTitle() {
		return title;
	}
	
	public void setTitle(final String title) {
		this.title = title;
	}
	
	@Override
	public String toString() {
		return "QueryContext [title=" + title + "]";
	}
}
