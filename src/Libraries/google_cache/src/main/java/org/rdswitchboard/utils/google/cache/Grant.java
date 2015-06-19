package org.rdswitchboard.utils.google.cache;

import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Grant {
	private int nodeId;
	private String nodeKey;
	private String name;
	private String self;
	private Set<String> links = new HashSet<String>();
	
	public Grant() {		
	}	
	
	public Grant(int nodeId, final String nodeKey, final String name, final String link) {
		this.nodeId = nodeId;
		this.nodeKey = nodeKey;
		this.name = name;
		links.add(link);
	}
	
	public String getName() {
		return name;
	}
	
	@XmlElement
	public void setName(final String name) {
		this.name = name;
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
		return "Grant [nodeId=" + nodeId + ", nodeKey=" + nodeKey + ", name=" + name + ", self=" + self
				+ ", links=" + links + "]";
	}
}
