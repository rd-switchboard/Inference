package org.rdswitchboard.libraries.orcid;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

public class WorkIdentifier {
	private String type;
	private String id;

	@JsonProperty("work-external-identifier-type")
	public String getType() {
		return type;
	}

	@JsonProperty("work-external-identifier-type")
	public void setType(String type) {
		this.type = type;
	}

	@JsonProperty("work-external-identifier-id")
	public String getId() {
		return id;
	}

	@JsonProperty("work-external-identifier-id")
	@JsonDeserialize(using = ValueDeserializer.class)
	public void setId(String id) {
		this.id = id;
	}

	@Override
	public String toString() {
		return "WorkIdentifier [type=" + type + ", id=" + id + "]";
	}
}
