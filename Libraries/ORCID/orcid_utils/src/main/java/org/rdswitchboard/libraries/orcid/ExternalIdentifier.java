package org.rdswitchboard.libraries.orcid;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

public class ExternalIdentifier {
	private String orcid;
	private OrcidIdentifier orcidId;
	private String sourceId;
	private Source source;
	private String commonName;
	private String reference;
	private String url;
	
	public String getOrcid() {
		return orcid;
	}

	public void setOrcid(String orcid) {
		this.orcid = orcid;
	}

	@JsonProperty("external-id-orcid")
	public OrcidIdentifier getOrcidId() {
		return orcidId;
	}
	
	public void setOrcidId(OrcidIdentifier orcidId) {
		this.orcidId = orcidId;
	}
	
	@JsonProperty("external-id-source")
	public String getSourceId() {
		return sourceId;
	}

	public void setSourceId(String sourceId) {
		this.sourceId = sourceId;
	}
	
	public Source getSource() {
		return source;
	}

	public void setSource(Source source) {
		this.source = source;
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
		return "ExternalIdentifier [orcid=" + orcid + ", orcidId=" + orcidId
				+ ", sourceId=" + sourceId + ", source=" + source
				+ ", commonName=" + commonName + ", reference=" + reference
				+ ", url=" + url + "]";
	}
}
