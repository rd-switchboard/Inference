package org.rdswitchboard.libraries.crossref;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * Class to store CorssRef Item information
 * @author Dmitrij Kudriavcev, dmitrij@kudriavcev.info
 *
 */

public class Item {
	private String doi;
	private String url;
	private String prefix;
	private String issue;
	private String volume;
	private String type;
	private String page;
	private String publisher;
	private String source;
	private String referenceCount;
	private String member;
	private String updatePolicy;
	private String articleNumber;
		
	private List<String> issn;
	private List<String> title;
	private List<String> subtitle;
	private List<String> subject;	
	private List<String> containerTitle;
	private List<String> alternativeId;
	private List<String> isbns;
	private List<String> archives;
	
	private List<Author> author;
	private List<Author> editor;
	private List<Funder> funder;
	private List<Link> links;
	private List<Assertion> assertions;
	private List<License> licenses;
	
	private Date issued;
	private Date deposited;
	private Date indexed;
	private Date publishedPrint;
	private Date publishedOnline;
	private Date created;
	
	private double score;
	
	private static DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	public List<String> getSubtitle() {
		return subtitle;
	}
	
	public void setSubtitle(List<String> subtitle) {
		if (!subtitle.isEmpty())
			this.subtitle = subtitle;
		else
			this.subtitle = null;
			
	}
	
	public List<String> getSubject() {
		return subject;
	}
	
	public void setSubject(List<String> subject) {
		if (!subject.isEmpty())
			this.subject = subject;
		else
			this.subject = null;
	}
		
	public Date getIssued() {
		return issued;
	}
	
	@JsonDeserialize(using = CrossRefDateDeserializer.class)
	public void setIssued(Date issued) {
		this.issued = issued;
	}
	
	public String getIssuedString() {
		if (null != issued)
			return df.format(issued);
		
		return null;
	}
	
	public double getScore() {
		return score;
	}
	
	public void setScore(double score) {
		this.score = score;
	}
	
	public String getPrefix() {
		return prefix;
	}
	
	public void setPrefix(final String prefix) {
		this.prefix = prefix;
	}
	
	public List<Author> getAuthor() {
		return author;
	}
	
	public void setAuthor(List<Author> author) {
		if (!author.isEmpty())
			this.author = author;
		else
			this.author = null;
			
	}
	
	public List<String> getAuthorString() {
		if (null == this.author || this.author.size() == 0)
			return null;
					
		List<String> list = new ArrayList<String>();
		for (Author author : this.author) 
			list.add(author.getFullName());

		return list;
	}
	
	public List<Author> getEditor() {
		return editor;
	}
	
	public void setEditor(List<Author> editor) {
		if (!editor.isEmpty())
			this.editor = editor;
		else
			this.editor = null;			
	}
	
	public List<String> getEditorString() {
		if (null == this.editor || this.editor.size() == 0)
			return null;
					
		List<String> list = new ArrayList<String>();
		for (Author editor : this.editor) 
			list.add(editor.getFullName());

		return list;
	}

	@JsonProperty("container-title")
	public List<String> getContainerTitle() {
		return containerTitle;
	}
	
	@JsonProperty("container-title")
	public void setContainerTitle(List<String> containerTitle) {
		if (!containerTitle.isEmpty())
			this.containerTitle = containerTitle;
		else
			this.containerTitle = null;
	}
	
	@JsonProperty("reference-count")
	public String getReferenceCount() {
		return referenceCount;
	}
	
	@JsonProperty("reference-count")
	public void setReferenceCount(final String referenceCount) {
		this.referenceCount = referenceCount;
	}
	
	public String getPage() {
		return page;
	}
	
	public void setPage(final String page) {
		this.page = page;
	}
	
	public Date getDeposited() {
		return deposited;
	}
	
	@JsonDeserialize(using = CrossRefDateDeserializer.class)
	public void setDeposited(Date deposited) {
		this.deposited = deposited;
	}
	
	public String getDepositedString() {
		if (null != deposited)
			return df.format(deposited);
		
		return null;
	}
	
	public String getIssue() {
		return issue;
	}
	
	public void setIssue(final String issue) {
		this.issue = issue;
	}
	
	public List<String> getTitle() {
		return title;
	}
	
	public void setTitle(List<String> title) {
		if (!title.isEmpty())
			this.title = title;
		else
			this.title = null;
	}
	
	public String getType() {
		return type;
	}
	
	public void setType(final String type) {
		this.type = type;
	}
	
	@JsonProperty("DOI")
	public String getDoi() {
		return doi;
	}
	
	@JsonProperty("DOI")
	public void setDoi(final String doi) {
		this.doi = doi;
	}
	
	@JsonProperty("ISSN")
	public List<String> getIssn() {
		return issn;
	}
	
	@JsonProperty("ISSN")
	public void setIssn(List<String> issn) {
		if (!issn.isEmpty())
			this.issn = issn;
		else
			this.issn = null;
	}
	
	@JsonProperty("URL")
	public String getUrl() {
		return url;
	}
	
	@JsonProperty("URL")
	public void setUrl(final String url) {
		this.url = url;
	}
	
	public String getSource() {
		return source;
	}
	
	public void setSource(final String source) {
		this.source = source;
	}
	
	public String getPublisher() {
		return publisher;
	}
	
	public void setPublisher(final String publisher) {
		this.publisher = publisher;
	}	
	
	public Date getIndexed() {
		return indexed;
	}
	
	@JsonDeserialize(using = CrossRefDateDeserializer.class)
	public void setIndexed(Date indexed) {
		this.indexed = indexed;
	}
	
	public String getIndexedString() {
		if (null != indexed)
			return df.format(indexed);
		
		return null;
	}
	
	public String getVolume() {
		return volume;
	}
	
	public void setVolume(final String volume) {
		this.volume = volume;
	}
	
	public String getMember() {
		return member;
	}
	
	public void setMember(final String member) {
		this.member = member;
	}
		
	public static void processItem(Item instance) {
	}
	
	@JsonProperty("update-policy")
	public String getUpdatePolicy() {
		return updatePolicy;
	}
	
	public void setUpdatePolicy(final String updatePolicy) {
		this.updatePolicy = updatePolicy;
	}
	
	public List<Funder> getFunder() {
		return funder;
	}
	
	public void setFunder(List<Funder> funder) {
		if (!funder.isEmpty())
			this.funder = funder;
		else
			this.funder = null;
	}
	
	@JsonProperty("alternative-id")
	public List<String> getAlternativeId() {
		return alternativeId;
	}

	public void setAlternativeId(List<String> alternativeId) {
		this.alternativeId = alternativeId;
	}

	@JsonProperty("link")
	public List<Link> getLinks() {
		return links;
	}

	public void setLinks(List<Link> links) {
		this.links = links;
	}
		
	@JsonProperty("assertion")
	public List<Assertion> getAssertions() {
		return assertions;
	}

	public void setAssertions(List<Assertion> assertions) {
		this.assertions = assertions;
	}
	
	@JsonProperty("license")
	public List<License> getLicenses() {
		return licenses;
	}

	public void setLicenses(List<License> licenses) {
		this.licenses = licenses;
	}

	@JsonProperty("archive")
	public List<String> getArchives() {
		return archives;
	}

	public void setArchives(List<String> archives) {
		this.archives = archives;
	}
	
	@JsonProperty("ISBN")
	public List<String> getIsbns() {
		return isbns;
	}

	public void setIsbns(List<String> isbns) {
		this.isbns = isbns;
	}
	
	@JsonProperty("published-print")
	public Date getPublishedPrint() {
		return publishedPrint;
	}
	
	@JsonDeserialize(using = CrossRefDateDeserializer.class)
	public void setPublishedPrint(Date published) {
		this.publishedPrint = published;
	}
	
	@JsonProperty("published-online")
	public Date getPublishedOnline() {
		return publishedOnline;
	}

	@JsonDeserialize(using = CrossRefDateDeserializer.class)
	public void setPublishedOnline(Date publishedOnline) {
		this.publishedOnline = publishedOnline;
	}

	public Date getCreated() {
		return created;
	}

	@JsonDeserialize(using = CrossRefDateDeserializer.class)
	public void setCreated(Date created) {
		this.created = created;
	}
	
	@JsonProperty("article-number")
	public String getArticleNumber() {
		return articleNumber;
	}

	public void setArticleNumber(String articleNumber) {
		this.articleNumber = articleNumber;
	}

	@JsonAnySetter
	public void handleUnknown(String key, Object value) {
		System.out.println("Warning. Ignoring Item property: " + key + " with value: " + value);			
	}

	@Override
	public String toString() {
		return "Item [doi=" + doi + ", url=" + url + ", prefix=" + prefix
				+ ", issue=" + issue + ", volume=" + volume + ", type=" + type
				+ ", page=" + page + ", publisher=" + publisher + ", source="
				+ source + ", referenceCount=" + referenceCount + ", member="
				+ member + ", updatePolicy=" + updatePolicy
				+ ", articleNumber=" + articleNumber + ", issn=" + issn
				+ ", title=" + title + ", subtitle=" + subtitle + ", subject="
				+ subject + ", containerTitle=" + containerTitle
				+ ", alternativeId=" + alternativeId + ", isbns=" + isbns
				+ ", archives=" + archives + ", author=" + author + ", editor="
				+ editor + ", funder=" + funder + ", links=" + links
				+ ", assertions=" + assertions + ", licenses=" + licenses
				+ ", issued=" + issued + ", deposited=" + deposited
				+ ", indexed=" + indexed + ", publishedPrint=" + publishedPrint
				+ ", publishedOnline=" + publishedOnline + ", created="
				+ created + ", score=" + score + "]";
	}
}
