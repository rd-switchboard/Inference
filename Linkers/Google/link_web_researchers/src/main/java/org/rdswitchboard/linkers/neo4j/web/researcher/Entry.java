package org.rdswitchboard.linkers.neo4j.web.researcher;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Entry {
	private String title;
	private String time;
	private int level;
	private boolean found;
	
	public Entry() {
		
	}
	
	public Entry(String title, String time, int level, boolean found) {
		title = this.title;
		time = this.time;
		level = this.level;
		found = this.found;
	}
	
	@XmlElement
	public String getTitle() {
		return title;
	}
	
	public void setTitle(String title) {
		this.title = title;
	}
	
	@XmlElement
	public String getTime() {
		return time;
	}
	
	public void setTime(String time) {
		this.time = time;
	}
	
	@XmlElement
	public int getLevel() {
		return level;
	}
	
	public void setLevel(int level) {
		this.level = level;
	}
	
	@XmlElement
	public boolean isFound() {
		return found;
	}
	
	public void setFound(boolean found) {
		this.found = found;
	}
	
	@Override
	public String toString() {
		return "MatcherCacheEntry [title=" + title + ", time=" + time + ", level=" + level + ", found=" + found + "]";
	}
}
