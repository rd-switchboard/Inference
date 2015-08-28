package org.rdswitchboard.importers.figshare.objects;

import java.util.Arrays;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Researcher {
	private Integer articleCount;
	private Article[] articles;
	private String description;
	private String facebook;
	private Integer id;
	private String jobTitle;
	private String linkedin;
	private String location;
	private String name;
	private String twitter;
	private String orcid;

	@JsonProperty("article_count")
	public Integer getActicleCount() {
		return articleCount;
	}
	
	@JsonProperty("article_count")
	public void setActicleCount(Integer articleCount) {
		this.articleCount = articleCount;
	}
	
	public Article[] getArticles() {
		return articles;
	}
	
	public void setArticles(Article[] articles) {
		this.articles = articles;
	}
	
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	public String getFacebook() {
		return facebook;
	}
	
	public void setFacebook(String facebook) {
		this.facebook = facebook;
	}
	
	public Integer getId() {
		return id;
	}
	
	public void setId(Integer id) {
		this.id = id;
	}
	
	@JsonProperty("job_title")
	public String getJobTitle() {
		return jobTitle;
	}
	
	@JsonProperty("job_title")
	public void setJobTitle(String jobTitle) {
		this.jobTitle = jobTitle;
	}
	
	public String getLinkedin() {
		return linkedin;
	}
	
	public void setLinkedin(String linkedin) {
		this.linkedin = linkedin;
	}
	
	public String getLocation() {
		return location;
	}
	
	public void setLocation(String location) {
		this.location = location;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getTwitter() {
		return twitter;
	}
	
	public void setTwitter(String twitter) {
		this.twitter = twitter;
	}
	
	public String getOrcid() {
		return orcid;
	}

	public void setOrcid(String orcid) {
		this.orcid = orcid;
	}
	
	@Override
	public String toString() {
		return "Researcher [articleCount=" + articleCount 
				+ ", articles=" + Arrays.toString(articles) 
				+ ", description=" + description
				+ ", facebook=" + facebook 
				+ ", id=" + id 
				+ ", jobTitle=" + jobTitle 
				+ ", linkedin=" + linkedin 
				+ ", location=" + location 
				+ ", name=" + name 
				+ ", twitter=" + twitter
				+ ", orcid=" + orcid + "]";
	}
}
