package org.rdswitchboard.libraries.orcid;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * History:
 * 
 * 1.0.11 : added clientId
 * 
 * @author Dmitrij Kudriavcev, dmitrij@kudriavcev.info
 *
 */

public class Source {
	private OrcidIdentifier identifier;
	private OrcidIdentifier clientId;
	private String name;
	private String date;
	
	@JsonProperty("source-orcid")
	public OrcidIdentifier getIdentifier() {
		return identifier;
	}
	
	@JsonProperty("source-orcid")
	public void setIdentifier(OrcidIdentifier identifier) {
		this.identifier = identifier;
	}
	
	@JsonProperty("source-client-id")
	public OrcidIdentifier getClientId() {
		return clientId;
	}

	@JsonProperty("source-client-id")
	public void setClientId(OrcidIdentifier clientId) {
		this.clientId = clientId;
	}	
	
	@JsonProperty("source-name")
	public String getName() {
		return name;
	}
	
	@JsonProperty("source-name")
	@JsonDeserialize(using = ValueDeserializer.class)
	public void setName(String name) {
		this.name = name;
	}
	
	@JsonProperty("source-date")
	public String getDate() {
		return date;
	}
	
	@JsonProperty("source-date")
	@JsonDeserialize(using = ValueDeserializer.class)
	public void setDate(String date) {
		this.date = date;
	}

	@Override
	public String toString() {
		return "Source [identifier=" + identifier 
				+ ", clientId=" + clientId
				+ ", name=" + name 
				+ ", date=" + date
				+ "]";
	}
}
