package org.rdswitchboard.importers.search;

public class MatcherThreadException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7113086478493476737L;

	public MatcherThreadException(String reason) {
		super ("Matcher Thread Exception: " + reason);
	}
}
