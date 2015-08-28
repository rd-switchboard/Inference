package org.rdswitchboard.libraries.scopus.response;

import java.util.Arrays;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties({ "@_fa" })
public class Entry {
	private String scopusEid;
//	private String scopusId;
	private String acticleNumber;
	private String intId;
	private String identifier;
	private String title;
	private String crteator;
	private String description;
	private String authkeywords;
//	private String openaccess;
	//private String pii;
	private String aggregationType;
	private String copyright;
	private String coverDisplayDate;
	private String doi;
	private String isbn;
	private String issn;
	private String eIssn;
	private String volume;
	private String pageRange;
	private String startingPage;
	private String endingPage;
	private String issueIdentifier;
	private String issueName;
	private String publicationName;
	private String teaser;
	private String url;
//	private String pubType;
	private String citedbyCount;
	private String subtype;
	private String subtypeDescription;
//	private boolean openaccessFlag;
	private Affiliation[] affiliations;
	private Author[] authors;
	private Link[] links;
	private String[] coverDates;
	
	public Affiliation[] getAffiliations() {
		return affiliations;
	}

	@JsonProperty("affiliation")
	public void setAffiliations(Affiliation[] affiliations) {
		this.affiliations = affiliations;
	}
	
	public String getActicleNumber() {
		return acticleNumber;
	}

	@JsonProperty("article-number")
	public void setActicleNumber(String acticleNumber) {
		this.acticleNumber = acticleNumber;
	}

	public String getAuthkeywords() {
		return authkeywords;
	}

	public void setAuthkeywords(String authkeywords) {
		this.authkeywords = authkeywords;
	}
	
	public Author[] getAuthors() {
		return authors;
	}

	@JsonProperty("author")
	public void setAuthors(Author[] authors) {
		this.authors = authors;
	}

	public String getCitedbyCount() {
		return citedbyCount;
	}

	@JsonProperty("citedby-count")
	public void setCitedbyCount(String citedbyCount) {
		this.citedbyCount = citedbyCount;
	}

	public String getDescription() {
		return description;
	}
	

	public String getCrteator() {
		return crteator;
	}

	@JsonProperty("dc:creator")
	public void setCrteator(String crteator) {
		this.crteator = crteator;
	}

	@JsonProperty("dc:description")
	public void setDescription(String description) {
		this.description = description;
	}
	
	public String getIdentifier() {
		return identifier;
	}
	
	public String getTitle() {
		return title;
	}
	
	@JsonProperty("dc:title")
	public void setTitle(String title) {
		this.title = title;
	}

	@JsonProperty("dc:identifier")
	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	public String getScopusEid() {
		return scopusEid;
	}

	@JsonProperty("scopus-eid")
	public void setScopusEid(String scopusEid) {
		this.scopusEid = scopusEid;
	}

	// aliase for scopusEid
	@JsonProperty("eid")
	public void setEid(String eid) {
		this.scopusEid = eid;
	}

	public String getIntId() {
		return intId;
	}

	@JsonProperty("intid")
	public void setIntId(String intId) {
		this.intId = intId;
	}

	public Link[] getLinks() {
		return links;
	}

	@JsonProperty("link")
	public void setLinks(Link[] links) {
		this.links = links;
	}
	
	public String getAggregationType() {
		return aggregationType;
	}

	@JsonProperty("prism:aggregationType")
	public void setAggregationType(String aggregationType) {
		this.aggregationType = aggregationType;
	}
	
	public String[] getCoverDates() {
		return coverDates;
	}
	
	public String getTeaser() {
		return teaser;
	}

	@JsonProperty("prism:teaser")
	public void setTeaser(String teaser) {
		this.teaser = teaser;
	}
		
	@JsonProperty("prism:coverDate")
	public void setCoverDates(String[] coverDates) {
		this.coverDates = coverDates;
	}

	public String getCoverDisplayDate() {
		return coverDisplayDate;
	}

	@JsonProperty("prism:coverDisplayDate")
	public void setCoverDisplayDate(String coverDisplayDate) {
		this.coverDisplayDate = coverDisplayDate;
	}

	public String getDoi() {
		return doi;
	}
	
	@JsonProperty("prism:doi")
	public void setDoi(String doi) {
		this.doi = doi;
	}
	
	public String getIssn() {
		return issn;
	}

	@JsonProperty("prism:issn")
	public void setIssn(String issn) {
		this.issn = issn;
	}
	
	public String getIssueIdentifier() {
		return issueIdentifier;
	}


	public String getIsbn() {
		return isbn;
	}

	@JsonProperty("prism:isbn")
	public void setIsbn(String isbn) {
		this.isbn = isbn;
	}
	
	public String geteIssn() {
		return eIssn;
	}

	@JsonProperty("prism:eIssn")
	public void seteIssn(String eIssn) {
		this.eIssn = eIssn;
	}	
	
	public String getIssueName() {
		return issueName;
	}

	@JsonProperty("prism:issueName")
	public void setIssueName(String issueName) {
		this.issueName = issueName;
	}
	
	@JsonProperty("prism:issueIdentifier")
	public void setIssueIdentifier(String issueIdentifier) {
		this.issueIdentifier = issueIdentifier;
	}
	
	public String getPageRange() {
		return pageRange;
	}

	@JsonProperty("prism:pageRange")
	public void setPageRange(String pageRange) {
		this.pageRange = pageRange;
	}
	
	public String getStartingPage() {
		return startingPage;
	}

	@JsonProperty("prism:startingPage")
	public void setStartingPage(String startingPage) {
		this.startingPage = startingPage;
	}
	
	public String getEndingPage() {
		return endingPage;
	}

	@JsonProperty("prism:endingPage")
	public void setEndingPage(String endingPage) {
		this.endingPage = endingPage;
	}

	public String getPublicationName() {
		return publicationName;
	}

	@JsonProperty("prism:publicationName")
	public void setPublicationName(String publicationName) {
		this.publicationName = publicationName;
	}
	
	public String getUrl() {
		return url;
	}

	@JsonProperty("prism:url")
	public void setUrl(String url) {
		this.url = url;
	}
	
	public String getVolume() {
		return volume;
	}

	@JsonProperty("prism:volume")
	public void setVolume(String volume) {
		this.volume = volume;
	}
	
	public String getCopyright() {
		return copyright;
	}

	@JsonProperty("prism:copyright")
	public void setCopyright(String copyright) {
		this.copyright = copyright;
	}
	
	public String getSubtype() {
		return subtype;
	}

	public void setSubtype(String subtype) {
		this.subtype = subtype;
	}
	
	public String getSubtypeDescription() {
		return subtypeDescription;
	}
	
	public void setSubtypeDescription(String subtypeDescription) {
		this.subtypeDescription = subtypeDescription;
	}

	@Override
	public String toString() {
		return "Entry [scopusEid=" + scopusEid + ", acticleNumber="
				+ acticleNumber + ", intId=" + intId + ", identifier="
				+ identifier + ", title=" + title + ", crteator=" + crteator
				+ ", description=" + description + ", authkeywords="
				+ authkeywords + ", aggregationType=" + aggregationType
				+ ", copyright=" + copyright + ", coverDisplayDate="
				+ coverDisplayDate + ", doi=" + doi + ", isbn=" + isbn
				+ ", issn=" + issn + ", eIssn=" + eIssn + ", volume=" + volume
				+ ", pageRange=" + pageRange + ", startingPage=" + startingPage
				+ ", endingPage=" + endingPage + ", issueIdentifier="
				+ issueIdentifier + ", issueName=" + issueName
				+ ", publicationName=" + publicationName + ", teaser=" + teaser
				+ ", url=" + url + ", citedbyCount=" + citedbyCount
				+ ", subtype=" + subtype + ", subtypeDescription="
				+ subtypeDescription + ", affiliations="
				+ Arrays.toString(affiliations) + ", authors="
				+ Arrays.toString(authors) + ", links="
				+ Arrays.toString(links) + ", coverDates="
				+ Arrays.toString(coverDates) + "]";
	}

	
	
	
	
	
	
	
	
/*
	
	public String getScopusId() {
		return scopusId;
	}

	@JsonProperty("scopus-id")
	public void setScopusId(String scopusId) {
		this.scopusId = scopusId;
	}
	*/
		

	
/*
	public String getOpenaccess() {
		return openaccess;
	}

	public void setOpenaccess(String openaccess) {
		this.openaccess = openaccess;
	}*/
	
	/*
	public String getPii() {
		return pii;
	}

	public void setPii(String pii) {
		this.pii = pii;
	}
*/
	

	

	/*
	
	public String getPubType() {
		return pubType;
	}

	public void setPubType(String pubType) {
		this.pubType = pubType;
	}

	public boolean isOpenaccessFlag() {
		return openaccessFlag;
	}
	
	public void setOpenaccessFlag(boolean openaccessFlag) {
		this.openaccessFlag = openaccessFlag;
	}
*/
	
	
}
