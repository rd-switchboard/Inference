package org.rdswitchboard.utils.google.cse;

import java.util.Map;

/**
 * Class to store Google response item
 * @author Dmitrij Kudriavcev, dmitrij@kudriavcev.info
 *
 */
public class Item {
	private String kind;
	private String title;
	private String htmlTitle;
	private String link;
	private String displayLink;
	private String snippet;
	private String htmlSnippet;
	private String cacheId;
	private String formattedUrl;
	private String htmlFormattedUrl;
	private String mime;
	private String fileFormat;
	private Map<String, Object> pagemap;
	
	public String getKind() {
		return kind;
	}

	public void setKind(String kind) {
		this.kind = kind;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getHtmlTitle() {
		return htmlTitle;
	}

	public void setHtmlTitle(String htmlTitle) {
		this.htmlTitle = htmlTitle;
	}

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public String getDisplayLink() {
		return displayLink;
	}

	public void setDisplayLink(String displayLink) {
		this.displayLink = displayLink;
	}

	public String getSnippet() {
		return snippet;
	}

	public void setSnippet(String snippet) {
		this.snippet = snippet;
	}

	public String getHtmlSnippet() {
		return htmlSnippet;
	}

	public void setHtmlSnippet(String htmlSnippet) {
		this.htmlSnippet = htmlSnippet;
	}

	public String getCacheId() {
		return cacheId;
	}

	public void setCacheId(String cacheId) {
		this.cacheId = cacheId;
	}

	public String getFormattedUrl() {
		return formattedUrl;
	}

	public void setFormattedUrl(String formattedUrl) {
		this.formattedUrl = formattedUrl;
	}

	public String getHtmlFormattedUrl() {
		return htmlFormattedUrl;
	}

	public void setHtmlFormattedUrl(String htmlFormattedUrl) {
		this.htmlFormattedUrl = htmlFormattedUrl;
	}

	public Map<String, Object> getPagemap() {
		return pagemap;
	}

	public void setPagemap(Map<String, Object> pagemap) {
		this.pagemap = pagemap;
	}
	
	public String getMime() {
		return mime;
	}

	public void setMime(String mime) {
		this.mime = mime;
	}
	
	public String getFileFormat() {
		return fileFormat;
	}

	public void setFileFormat(String fileFormat) {
		this.fileFormat = fileFormat;
	}
	
	@Override 
	public String toString() {
		return "Item [kind=" + kind +
				", title=" + title +
				", htmlTitle=" + htmlTitle +
				", link=" + link +
				", displayLink=" + displayLink +
				", snippet=" + snippet +
				", htmlSnippet=" + htmlSnippet +
				", cacheId=" + cacheId +
				", formattedUrl=" + formattedUrl +
				", htmlFormattedUrl=" + htmlFormattedUrl +
				", pagemap=" + pagemap + 
				", mime=" + mime + 
				", fileFormat" + fileFormat + "]";
	}
}
