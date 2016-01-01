package org.rdswitchboard.libraries.orcid;

import java.util.List;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

public class ContactDetails {
	private List<Email> email;
	private Address address;

	public List<Email> getEmail() {
		return email;
	}

	public void setEmail(List<Email> email) {
		this.email = email;
	}

	public Address getAddress() {
		return address;
	}

	public void setAddress(Address address) {
		this.address = address;
	}

	@Override
	public String toString() {
		return "ContactDetails [email=" + email + ", address=" + address + "]";
	}
}
