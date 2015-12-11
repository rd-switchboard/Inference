package org.rdswitchboard.libraries.orcid;

public class OrcidPreferences {
	private String locale;

	public String getLocale() {
		return locale;
	}

	public void setLocale(String locale) {
		this.locale = locale;
	}

	@Override
	public String toString() {
		return "OrcidPreferences [locale=" + locale + "]";
	}
}
