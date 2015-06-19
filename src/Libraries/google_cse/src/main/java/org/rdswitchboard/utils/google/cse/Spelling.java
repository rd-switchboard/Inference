package org.rdswitchboard.utils.google.cse;

/**
 * Class to store Google optional spelling
 * @author Dmitrij Kudriavcev, dmitrij@kudriavcev.info
 *
 */
public class Spelling {
	private String correctedQuery;
	private String htmlCorrectedQuery;

	public String getCorrectedQuery() {
		return correctedQuery;
	}

	public void setCorrectedQuery(String correctedQuery) {
		this.correctedQuery = correctedQuery;
	}

	public String getHtmlCorrectedQuery() {
		return htmlCorrectedQuery;
	}

	public void setHtmlCorrectedQuery(String htmlCorrectedQuery) {
		this.htmlCorrectedQuery = htmlCorrectedQuery;
	}

	@Override
	public String toString() {
		return "Spelling [correctedQuery=" + correctedQuery
				+ ", htmlCorrectedQuery=" + htmlCorrectedQuery + "]";
	}
}
