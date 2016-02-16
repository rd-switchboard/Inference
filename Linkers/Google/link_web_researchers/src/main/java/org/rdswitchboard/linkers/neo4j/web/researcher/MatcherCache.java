package org.rdswitchboard.linkers.neo4j.web.researcher;

import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;

public class MatcherCache {
	private DateTime time;
	private int level;
	private boolean found;
	
	public MatcherCache() {
		
	}
	
	public MatcherCache(DateTime time, int level, boolean found) {
		this.time = time;
		this.level = level;
		this.found = found;
	}
	
	public MatcherCache(String time, int level, boolean found) {
		this.time = ISODateTimeFormat.dateTime().parseDateTime(time);
		this.level = level;
		this.found = found;
	}
	
	public MatcherCache(int level, boolean found) {
		this.time = new DateTime();
		this.level = level;
		this.found = found;
	}

	public DateTime getTime() {
		return time;
	}
	
	public String getTimeString() {
		return ISODateTimeFormat.time().print(this.time);
	}
	
	public void setTime(DateTime time) {
		this.time = time;
	}
	
	public int getLevel() {
		return level;
	}
	
	public void setLevel(int level) {
		this.level = level;
	}
	
	public boolean isFound() {
		return found;
	}
	
	public void setFound(boolean found) {
		this.found = found;
	}
	
	@Override
	public String toString() {
		return "MatcherCache [time=" + time + ", level=" + level + ", found=" + found + "]";
	}
}
