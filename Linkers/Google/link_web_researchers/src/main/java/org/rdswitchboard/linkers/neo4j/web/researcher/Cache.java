package org.rdswitchboard.linkers.neo4j.web.researcher;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Cache {
	private List<Entry> entries;

	@XmlElementWrapper(name = "entries")
    @XmlElement(name = "entry")
	public List<Entry> getEntries() {
		return entries;
	}

	public void setEntries(List<Entry> entries) {
		this.entries = entries;
	}

	@Override
	public String toString() {
		return "Cache [entries=" + entries + "]";
	}
}
