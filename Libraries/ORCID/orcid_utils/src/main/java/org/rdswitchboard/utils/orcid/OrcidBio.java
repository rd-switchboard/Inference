package org.rdswitchboard.utils.orcid;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

public class OrcidBio {
	private PersonalDetails personalDetails;
	private String biography;
	private ContactDetails contactDetails;
	private Keywords keywords;
	private String delegation;
	private String applications;
	private String scope;
	private ExternalIdentifiers externalIdentifiers;
	private ResearcherUrls researcherUrls;

	@JsonProperty("personal-details")
	public PersonalDetails getPersonalDetails() {
		return personalDetails;
	}

	@JsonProperty("personal-details")
	public void setPersonalDetails(PersonalDetails personalDetails) {
		this.personalDetails = personalDetails;
	}

	public String getBiography() {
		return biography;
	}

	@JsonDeserialize(using = ValueDeserializer.class)
	public void setBiography(String biography) {
		this.biography = biography;
	}

	@JsonProperty("contact-details")
	public ContactDetails getContactDetails() {
		return contactDetails;
	}

	@JsonProperty("contact-details")
	public void setContactDetails(ContactDetails contactDetails) {
		this.contactDetails = contactDetails;
	}

	public Keywords getKeywords() {
		return keywords;
	}

	public void setKeywords(Keywords keywords) {
		this.keywords = keywords;
	}

	public String getDelegation() {
		return delegation;
	}

	public void setDelegation(String delegation) {
		this.delegation = delegation;
	}

	public String getApplications() {
		return applications;
	}

	public void setApplications(String applications) {
		this.applications = applications;
	}

	public String getScope() {
		return scope;
	}

	public void setScope(String scope) {
		this.scope = scope;
	}
	
	@JsonProperty("external-identifiers")
	public ExternalIdentifiers getExternalIdentifiers() {
		return externalIdentifiers;
	}

	@JsonProperty("external-identifiers")
	public void setExternalIdentifiers(ExternalIdentifiers externalIdentifiers) {
		this.externalIdentifiers = externalIdentifiers;
	}
	
	@JsonProperty("researcher-urls")
	public ResearcherUrls getResearcherUrls() {
		return researcherUrls;
	}

	@JsonProperty("researcher-urls")
	public void setResearcherUrls(ResearcherUrls researcherUrls) {
		this.researcherUrls = researcherUrls;
	}
	
	@Override
	public String toString() {
		return "OrcidBio [personalDetails=" + personalDetails 
				+ ", biography=" + biography 
				+ ", contactDetails=" + contactDetails
				+ ", keywords=" + keywords 
				+ ", delegations=" + delegation
				+ ", applications=" + applications 
				+ ", scope=" + scope 
				+ ", externalIdentifiers=" + externalIdentifiers 
				+ ", researcherUrls=" + researcherUrls
				+ "]";
	}
}
