package org.rdswitchboard.utils.orcid;

public class OrcidIdentifier {
	private String value;
	private String uri;
	private String path;
	private String host;

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	@Override
	public String toString() {
		return "OrcidIdentifier [value=" + value + ", uri=" + uri + ", path="
				+ path + ", host=" + host + "]";
	}	
}
