package org.rdswitchboard.utils.google.cse;

/**
 * Class to store Google Uri Template
 * @author Dmitrij Kudriavcev, dmitrij@kudriavcev.info
 *
 */
public class UrlTemplate {
	private String type;
	private String template;
	
	public String getType() {
		return type;
	}
	
	public void setType(final String type) {
		this.type = type;
	}
	
	public String getTemplate() {
		return template;
	}
	
	public void setTemplate(final String template) {
		this.template = template;
	}
	
	@Override
	public String toString() {
		return "UrlTemplate [type=" + type + ", template=" + template + "]";
	}
}
