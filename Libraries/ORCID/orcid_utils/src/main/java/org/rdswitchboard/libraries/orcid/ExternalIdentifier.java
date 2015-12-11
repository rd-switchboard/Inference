package org.rdswitchboard.libraries.orcid;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

public class ExternalIdentifier {
	private OrcidIdentifier orcidId;
	private String commonName;
	private String reference;
	private String url;
	
	@JsonProperty("external-id-orcid")
	public OrcidIdentifier getOrcidId() {
		return orcidId;
	}
	
	@JsonProperty("external-id-orcid")
	//@JsonDeserialize(using = ValueDeserializer.class)
	public void setOrcidId(OrcidIdentifier orcidId) {
		this.orcidId = orcidId;
	}
	
	public String getOrcidUri() {
		if (null != this.orcidId) 
			return this.orcidId.getUri();
		else
			return null;
	}
	
	@JsonProperty("external-id-common-name")
	public String getCommonName() {
		return commonName;
	}
	
	@JsonProperty("external-id-common-name")
	@JsonDeserialize(using = ValueDeserializer.class)
	public void setCommonName(String commonName) {
		this.commonName = commonName;
	}
	
	@JsonProperty("external-id-reference")
	public String getReference() {
		return reference;
	}
	
	@JsonProperty("external-id-reference")
	@JsonDeserialize(using = ValueDeserializer.class)
	public void setReference(String reference) {
		this.reference = reference;
	}
	
	@JsonProperty("external-id-url")
	public String getUrl() {
		return url;
	}
	
	@JsonProperty("external-id-url")
	@JsonDeserialize(using = ValueDeserializer.class)
	public void setUrl(String url) {
		this.url = url;
	}
	
	@Override
	public String toString() {
		return "ExternalIdentifier [orcidId=" + orcidId + ", commonName="
				+ commonName + ", reference=" + reference + ", url=" + url
				+ "]";
	}	
}
