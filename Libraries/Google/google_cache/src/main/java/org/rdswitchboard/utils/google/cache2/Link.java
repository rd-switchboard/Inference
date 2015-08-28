package org.rdswitchboard.utils.google.cache2;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author dima
 * 
 * History
 * 1.0.1: added map field
 * 1.1.0: Added metadata file name, removed data
 */

@XmlRootElement
public class Link {
	private String link;
	private String data;
	private String metadata;
	private String self;
	
	public String getLink() {
		return link;
	}
	
	@XmlElement
	public void setLink(final String link) {
		this.link = link;
	}
	
	public String getData() {
		return data;
	}
	
	@XmlElement
	public void setData(final String data) {
		this.data = data;
	}
	
	public String getMetadata() {
		return metadata;
	}

	@XmlElement
	public void setMetadata(String metadata) {
		this.metadata = metadata;
	}

	public String getSelf() {
		return self;
	}
	
	@XmlElement
	public void setSelf(String self) {
		this.self = self;
	}

	@Override
	public String toString() {
		return "Page [link=" + link 
				+ ", data=" + data 
				+ ", self=" + self
				+ ", metadata=" + metadata + "]";
	}
}
