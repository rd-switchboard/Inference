package org.rdswitchboard.utils.orcid;

import java.util.List;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

public class ContactDetails {
	private List<String> email;
	private Address address;

	public List<String> getEmail() {
		return email;
	}

	@JsonDeserialize(using = ValueDeserializer.class)
	public void setEmail(List<String> email) {
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
