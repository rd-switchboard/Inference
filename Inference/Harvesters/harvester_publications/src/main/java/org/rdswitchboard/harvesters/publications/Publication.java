package org.rdswitchboard.harvesters.publications;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Publication implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1582106469908751156L;
	private int nodeId;
	private String nodeKey;
	private String title;
	private String self;
	private Set<String> links = new HashSet<String>();
	
	public Publication() {		
	}	
	
	public Publication(int nodeId, final String nodeKey, final String title, final String link) {
		this.nodeId = nodeId;
		this.nodeKey = nodeKey;
		this.title = title;
		links.add(link);
	}
	
	public String getTitle() {
		return title;
	}
	
	@XmlElement
	public void setTitle(final String title) {
		this.title = title;
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
	
	public int getNodeId() {
		return nodeId;
	}

	@XmlElement
	public void setNodeId(int nodeId) {
		this.nodeId = nodeId;
	}
	
	public String getNodeKey() {
		return nodeKey;
	}

	@XmlElement
	public void setNodeKey(final String nodeKey) {
		this.nodeKey = nodeKey;
	}

	public String getSelf() {
		return self;
	}

	public void setSelf(String self) {
		this.self = self;
	}

	@Override
	public String toString() {
		return "Grant [nodeId=" + nodeId + ", nodeKey=" + nodeKey + ", title=" + title + ", self=" + self
				+ ", links=" + links + "]";
	}
}
