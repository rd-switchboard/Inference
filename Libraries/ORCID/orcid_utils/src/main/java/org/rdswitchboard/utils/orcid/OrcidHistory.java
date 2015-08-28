package org.rdswitchboard.utils.orcid;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * History:
 * 1.1.1 Added deactivationDate property
 * @author dima
 *
 */

public class OrcidHistory {
	private String creationMethod;
	private String completionDate;
	private String submissionDate;
	private String lastModifiedDate;
	private String claimed;
	private String deactivationDate;
	private Source source;
	private String visibility;
	
	@JsonProperty("creation-method")
	public String getCreationMethod() {
		return creationMethod;
	}
	
	@JsonProperty("creation-method")
	public void setCreationMethod(String creationMethod) {
		this.creationMethod = creationMethod;
	}
	
	@JsonProperty("completion-date")
	public String getCompletionDate() {
		return completionDate;
	}
	
	@JsonProperty("completion-date")
	@JsonDeserialize(using = ValueDeserializer.class)
	public void setCompletionDate(String completionDate) {
		this.completionDate = completionDate;
	}
	
	@JsonProperty("submission-date")
	public String getSubmissionDate() {
		return submissionDate;
	}
	
	@JsonProperty("submission-date")
	@JsonDeserialize(using = ValueDeserializer.class)
	public void setSubmissionDate(String submissionDate) {
		this.submissionDate = submissionDate;
	}
	
	@JsonProperty("last-modified-date")
	public String getLastModifiedDate() {
		return lastModifiedDate;
	}
	
	@JsonProperty("last-modified-date")
	@JsonDeserialize(using = ValueDeserializer.class)
	public void setLastModifiedDate(String lastModifiedDate) {
		this.lastModifiedDate = lastModifiedDate;
	}
	
	public String getClaimed() {
		return claimed;
	}
	
	@JsonDeserialize(using = ValueDeserializer.class)
	public void setClaimed(String claimed) {
		this.claimed = claimed;
	}
		
	public String getDeactivationDate() {
		return deactivationDate;
	}

	@JsonProperty("deactivation-date")
	@JsonDeserialize(using = ValueDeserializer.class)
	public void setDeactivationDate(String deactivationDate) {
		this.deactivationDate = deactivationDate;
	}


	public Source getSource() {
		return source;
	}
	
	public void setSource(Source source) {
		this.source = source;
	}
	
	public String getVisibility() {
		return visibility;
	}
	
	public void setVisibility(String visibility) {
		this.visibility = visibility;
	}
	
	@Override
	public String toString() {
		return "OrcidHistory [creationMethod=" + creationMethod
				+ ", completionDate=" + completionDate 
				+ ", submissionDate=" + submissionDate 
				+ ", lastModifiedDate=" + lastModifiedDate
				+ ", claimed=" + claimed 
				+ ", deactivationDate=" + deactivationDate 
				+ ", source=" + source
				+ ", visibility=" + visibility + "]";
	}
}
