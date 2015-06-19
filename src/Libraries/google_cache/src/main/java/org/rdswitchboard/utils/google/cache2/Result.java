package org.rdswitchboard.utils.google.cache2;

import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * 
 * @author dima
 *
 */

@XmlRootElement
public class Result {
	private String text;
	private String self;
	private Set<String> links = new HashSet<String>();

	public String getText() {
		return text;
	}
	
	@XmlElement
	public void setText(final String text) {
		this.text = text;
	}
	
	public Set<String> getLinks() {
		return links;
	}
	
	public void addLink(final String link) {
		links.add(link);
	}

	@XmlElement
	public void setLinks(Set<String> links) {
		this.links = links;
	}

	public String getSelf() {
		return self;
	}

	public void setSelf(String self) {
		this.self = self;
	}

	@Override
	public String toString() {
		return "Result [text=" + text 
				+ ", self=" + self 
				+ ", links=" + links + "]";
	}
}
