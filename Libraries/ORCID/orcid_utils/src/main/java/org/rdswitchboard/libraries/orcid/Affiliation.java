package org.rdswitchboard.libraries.orcid;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

public class Affiliation {
	private String type;
	private String departmentName;
	private String roleTitle;
	private Date startDate;
	private Date endDate; 
	private Organization organization;
	private Source source;
	private String createdDate;
	private String lastModifiedDate;
	private String visibility;
	private String putCode;
	
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
	
	@JsonProperty("department-name")
	public String getDepartmentName() {
		return departmentName;
	}
	
	public void setDepartmentName(String departmentName) {
		this.departmentName = departmentName;
	}
	
	@JsonProperty("role-title")
	public String getRoleTitle() {
		return roleTitle;
	}
	
	public void setRoleTitle(String roleTitle) {
		this.roleTitle = roleTitle;
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
	
	@JsonProperty("put-code")
	public String getPutCode() {
		return putCode;
	}
	
	public void setPutCode(String putCode) {
		this.putCode = putCode;
	}
	
	@Override
	public String toString() {
		return "Affiliation [type=" + type + ", departmentName="
				+ departmentName + ", roleTitle=" + roleTitle + ", startDate="
				+ startDate + ", endDate=" + endDate + ", organization="
				+ organization + ", source=" + source + ", createdDate="
				+ createdDate + ", lastModifiedDate=" + lastModifiedDate
				+ ", visibility=" + visibility + ", putCode=" + putCode + "]";
	}
}
