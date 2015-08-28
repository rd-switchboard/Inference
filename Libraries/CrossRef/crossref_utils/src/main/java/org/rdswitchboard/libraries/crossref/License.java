package org.rdswitchboard.libraries.crossref;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/* icense with value: [{
 * content-version=vor, 
 * delay-in-days=0, 
 * start={
 * date-parts=[[2010, 12, 29]], 
 * timestamp=1293580800000}, 
 * URL=http://creativecommons.org/licenses/by/3.0/}]
 */

public class License {
	private int delayInDays;
	private String contentVersion;
	private String url;		
	private Date start;
	
	@JsonProperty("delay-in-days")
	public int getDelayInDays() {
		return delayInDays;
	}
	
	public void setDelayInDays(int delayInDays) {
		this.delayInDays = delayInDays;
	}
	
	@JsonProperty("content-version")
	public String getContentVersion() {
		return contentVersion;
	}
	
	public void setContentVersion(String contentVersion) {
		this.contentVersion = contentVersion;
	}
	
	@JsonProperty("URL")
	public String getUrl() {
		return url;
	}
	
	public void setUrl(String url) {
		this.url = url;
	}
	
	public Date getStart() {
		return start;
	}
	
	@JsonDeserialize(using = CrossRefDateDeserializer.class)
	public void setStart(Date start) {
		this.start = start;
	}
	
	@Override
	public String toString() {
		return "License [delayInDays=" + delayInDays + ", contentVersion="
				+ contentVersion + ", url=" + url + ", start=" + start + "]";
	}
}
