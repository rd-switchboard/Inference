package org.rdswitchboard.harvesters.grants;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Page implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -7117214069746581920L;
	private String link;
	private String cache;
	private String self;
	private Set<String> data = new HashSet<String>(); 
	//private Map<String, Object> metadata;
	
	public Page() {		
	}
	
	public Page(final String link, final String cache, final String data) {
		this.link = link;
		this.cache = cache;
		this.data.add(data);
		/*this.metadata = metadata;*/
	}
	
	public String getLink() {
		return link;
	}
	
	@XmlElement
	public void setLink(final String link) {
		this.link = link;
	}
	
	public String getCache() {
		return cache;
	}
	
	@XmlElement
	public void setCache(final String cache) {
		this.cache = cache;
	}
	
	public Set<String> getData() {
		return data;
	}
	
	public void addData(final String data) {
		this.data.add(data);
	}
	
	@XmlElement
	public void setData(Set<String> data) {
		this.data = data;
	}
	
	public String getSelf() {
		return self;
	}

	public void setSelf(String self) {
		this.self = self;
	}
	
	/*
	public Map<String, Object> getMetadata() {
		return metadata;
	}

	public void setMetadata(Map<String, Object> metadata) {
		this.metadata = metadata;
	}*/

	@Override
	public String toString() {
		return "Page [link=" + link + ", cache=" + cache + ", self=" + self
				+ ", data=" + data + /*", metadata=" + metadata +*/ "]";
	}
}
