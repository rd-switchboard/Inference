package org.rdswitchboard.libraries.orcid;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

public class PersonalDetails {
	private String givenNames;
	private String familyName;
	private String creditName;
	private OtherNames otherNames;
	
	@JsonProperty("given-names")
	public String getGivenNames() {
		return givenNames;
	}
	
	@JsonProperty("given-names")
	@JsonDeserialize(using = ValueDeserializer.class)
	public void setGivenNames(String givenNames) {
		this.givenNames = givenNames;
	}
	
	@JsonProperty("family-name")
	public String getFamilyName() {
		return familyName;
	}
	
	@JsonProperty("family-name")
	@JsonDeserialize(using = ValueDeserializer.class)
	public void setFamilyName(String familyName) {
		this.familyName = familyName;
	}	
	
	@JsonProperty("credit-name")
	public String getCreditName() {
		return creditName;
	}
	
	@JsonProperty("credit-name")
	@JsonDeserialize(using = ValueDeserializer.class)
	public void setCreditName(String creditName) {
		this.creditName = creditName;
	}	
	
	public String getFullName() {
		String fullName = null;
		if (null != givenNames && !givenNames.isEmpty()) 
			fullName = givenNames;
		
		if (null != familyName && !familyName.isEmpty()) {
			if (null != fullName)
				fullName += " " + familyName;
			else
				fullName = familyName;
		}
		
		return fullName;
	}
	
	@JsonProperty("other-names")
	public OtherNames getOtherNames() {
		return otherNames;
	}

	@JsonProperty("other-names")
	public void setOtherNames(OtherNames otherNames) {
		this.otherNames = otherNames;
	}
	
	@Override
	public String toString() {
		return "PersonalDetails [guvenNames=" + givenNames 
				+ ", familyName=" + familyName 
				+ ", creditName=" + creditName
				+ ", otherNames=" + otherNames
				+ "]";
	}	
}
