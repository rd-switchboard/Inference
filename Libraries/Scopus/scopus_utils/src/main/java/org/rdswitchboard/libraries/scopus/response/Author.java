package org.rdswitchboard.libraries.scopus.response;

import java.util.Set;

public class Author {
	private final String authorId;
	private final String authorUrl;
	private final String fullName;
	private final String givenName;
	private final String familyName;
	private final String initials;
	private final Set<String> affiliations;

	public Author(
			final String authorId,
			final String authorUrl,
			final String fullName,
			final String givenName,
			final String familyName,
			final String initials,
			final Set<String> affiliations) {

		this.authorId = authorId;
		this.authorUrl = authorUrl;
		this.fullName = fullName;
		this.givenName = givenName;
		this.familyName = familyName;
		this.initials = initials;
		this.affiliations = affiliations;
	}

	public String getAuthorId() {
		return authorId;
	}
	
	public String getAuthorUrl() {
		return authorUrl;
	}

	public String getFullName() {
		return fullName;
	}

	public String getGivenName() {
		return givenName;
	}

	public String getFamilyName() {
		return familyName;
	}

	public String getInitials() {
		return initials;
	}

	public Set<String> getAffiliations() {
		return affiliations;
	}

	@Override
	public String toString() {
		return "Author [authorId=" + authorId + ", affiliations="
				+ affiliations + ", authorUrl=" + authorUrl + ", fullName="
				+ fullName + ", givenName=" + givenName + ", familyName="
				+ familyName + ", initials=" + initials + "]";
	}
}
