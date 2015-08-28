package org.rdswitchboard.libraries.crossref;

import com.fasterxml.jackson.annotation.JsonProperty;

/*
 * link with value: [{
 * 	=text-mining, 
 *  =vor, 
 *  =text/xml, 
 *  =http://api.elsevier.com/content/article/PII:S1055790312003892?httpAccept=text/xml}, 
 *  {intended-application=text-mining, 
 *  content-version=vor, 
 *  content-type=text/plain, 
 *  URL=http://api.elsevier.com/content/article/PII:S1055790312003892?httpAccept=text/plain}]
*/


public class Link {
	private String intendedApplication;
	private String contentVersion;
	private String contentType;
	private String url;
	
	@JsonProperty("intended-application")
	public String getIntendedApplication() {
		return intendedApplication;
	}
	
	public void setIntendedApplication(String intendedApplication) {
		this.intendedApplication = intendedApplication;
	}
	
	@JsonProperty("content-version")
	public String getContentVersion() {
		return contentVersion;
	}
	
	public void setContentVersion(String contentVersion) {
		this.contentVersion = contentVersion;
	}
	
	@JsonProperty("content-type")
	public String getContentType() {
		return contentType;
	}
	
	public void setContentType(String contentType) {
		this.contentType = contentType;
	}
	
	@JsonProperty("URL")
	public String getUrl() {
		return url;
	}
	
	public void setUrl(String url) {
		this.url = url;
	}
	
	@Override
	public String toString() {
		return "Link [intendedApplication=" + intendedApplication
				+ ", contentVersion=" + contentVersion + ", contentType="
				+ contentType + ", url=" + url + "]";
	}	
}
