package org.rdswitchboard.libraries.orcid;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

public class Funding {
	private String putCode;
	private String fundingType;
	private String organizationDefinedType;
	private FundingTitle fundingTitle;
	private String shortDescription;
	private String amount;
	private String url;
	private Date startDate;
	private Date endDate;
	private FundingExternalIdentifiers externalIdentifiers;
	private FundingContributors contributors;
	private Organization organization;
	private Source source;
	private String createdDate;
	private String lastModifiedDate;
	private String visibility;
	
	@JsonProperty("put-code")
	public String getPutCode() {
		return putCode;
	}
	
	public void setPutCode(String putCode) {
		this.putCode = putCode;
	}
	
	@JsonProperty("funding-type")
	public String getFundingType() {
		return fundingType;
	}
	
	public void setFundingType(String fundingType) {
		this.fundingType = fundingType;
	}
	
	@JsonProperty("organization-defined-type")
	public String getOrganizationDefinedType() {
		return organizationDefinedType;
	}
	
	@JsonDeserialize(using = ValueDeserializer.class)
	public void setOrganizationDefinedType(String organizationDefinedType) {
		this.organizationDefinedType = organizationDefinedType;
	}
	
	@JsonProperty("funding-title")
	public FundingTitle getFundingTitle() {
		return fundingTitle;
	}
	
	public void setFundingTitle(FundingTitle fundingTitle) {
		this.fundingTitle = fundingTitle;
	}
	
	@JsonProperty("short-description")
	public String getShortDescription() {
		return shortDescription;
	}
	
	@JsonDeserialize(using = ValueDeserializer.class)
	public void setShortDescription(String shortDescription) {
		this.shortDescription = shortDescription;
	}
	
	public String getAmount() {
		return amount;
	}
	
	@JsonDeserialize(using = ValueDeserializer.class)
	public void setAmount(String amount) {
		this.amount = amount;
	}
	
	public String getUrl() {
		return url;
	}
	
	@JsonDeserialize(using = ValueDeserializer.class)
	public void setUrl(String url) {
		this.url = url;
	}
	
	@JsonProperty("start-date")
	public Date getStartDate() {
		return startDate;
	}
	
	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}
	
	@JsonProperty("end-date")
	public Date getEndDate() {
		return endDate;
	}
	
	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}
	
	@JsonProperty("funding-external-identifiers")
	public FundingExternalIdentifiers getExternalIdentifiers() {
		return externalIdentifiers;
	}
	
	public void setExternalIdentifiers(FundingExternalIdentifiers externalIdentifiers) {
		this.externalIdentifiers = externalIdentifiers;
	}
	
	@JsonProperty("funding-contributors")
	public FundingContributors getContributors() {
		return contributors;
	}
	
	public void setContributors(FundingContributors contributors) {
		this.contributors = contributors;
	}
	
	public Organization getOrganization() {
		return organization;
	}
	
	public void setOrganization(Organization organization) {
		this.organization = organization;
	}
	
	public Source getSource() {
		return source;
	}
	
	public void setSource(Source source) {
		this.source = source;
	}
	
	@JsonProperty("created-date")
	public String getCreatedDate() {
		return createdDate;
	}
	
	@JsonDeserialize(using = ValueDeserializer.class)
	public void setCreatedDate(String createdDate) {
		this.createdDate = createdDate;
	}
	
	@JsonProperty("last-modified-date")
	public String getLastModifiedDate() {
		return lastModifiedDate;
	}
	
	@JsonDeserialize(using = ValueDeserializer.class)
	public void setLastModifiedDate(String lastModifiedDate) {
		this.lastModifiedDate = lastModifiedDate;
	}
	
	public String getVisibility() {
		return visibility;
	}
	
	public void setVisibility(String visibility) {
		this.visibility = visibility;
	}

	@Override
	public String toString() {
		return "Funding [putCode=" + putCode + ", fundingType=" + fundingType
				+ ", organizationDefinedType=" + organizationDefinedType
				+ ", fundingTitle=" + fundingTitle + ", shortDescription="
				+ shortDescription + ", amount=" + amount + ", url=" + url
				+ ", startDate=" + startDate + ", endDate=" + endDate
				+ ", externalIdentifiers=" + externalIdentifiers
				+ ", contributors=" + contributors + ", organization="
				+ organization + ", source=" + source + ", createdDate="
				+ createdDate + ", lastModifiedDate=" + lastModifiedDate
				+ ", visibility=" + visibility + "]";
	}
}
