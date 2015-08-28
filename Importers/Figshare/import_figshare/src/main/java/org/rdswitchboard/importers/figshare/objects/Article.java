package org.rdswitchboard.importers.figshare.objects;

import java.util.Arrays;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Article {
	private String doi;
	private Integer articleId;
	private Author[] authors;
	private String description;
	private Link[] links;
	private String publishedDate;
	private String title;
	private String type;
	private String url;
	
	@JsonProperty("DOI")
	public String getDoi() {
		return doi;
	}

	@JsonProperty("DOI")
	public void setDoi(String doi) {
		this.doi = doi;
	}
	
	@JsonProperty("article_id")
	public Integer getArticleId() {
		return articleId;
	}
	
	@JsonProperty("article_id")
	public void setArticleId(Integer articleId) {
		this.articleId = articleId;
	}
	
	public Author[] getAuthors() {
		return authors;
	}
	
	public void setAuthors(Author[] authors) {
		this.authors = authors;
	}
	
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	public Link[] getLinks() {
		return links;
	}
	
	public void setLinks(Link[] links) {
		this.links = links;
	}
	
	@JsonProperty("published_date")
	public String getPublishedDate() {
		return publishedDate;
	}
	
	@JsonProperty("published_date")
	public void setPublishedDate(String publishedDate) {
		this.publishedDate = publishedDate;
	}
	
	public String getTitle() {
		return title;
	}
	
	public void setTitle(String title) {
		this.title = title;
	}
	
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getUrl() {
		return url;
	}
	
	public void setUrl(String url) {
		this.url = url;
	}

	@Override
	public String toString() {
		return "Article [doi=" + doi + ", articleId=" + articleId
				+ ", authors=" + Arrays.toString(authors) + ", description="
				+ description + ", links=" + Arrays.toString(links)
				+ ", publishedDate=" + publishedDate + ", title=" + title
				+ ", type=" + type + ", url=" + url + "]";
	}
}
