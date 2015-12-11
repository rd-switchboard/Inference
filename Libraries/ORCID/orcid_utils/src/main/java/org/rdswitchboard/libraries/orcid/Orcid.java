package org.rdswitchboard.libraries.orcid;

import java.io.File;

import javax.ws.rs.core.MediaType;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;

/**
 * History
 * 1.1.0: Update API URL's
 * 
 * @author dima
 *
 */

public class Orcid {
	private static final String ORCID_URL = "http://pub.orcid.org/v1.1/";
	private static final String ORCID_HOST = "orcid.org/";
	
	private static final String ORCID_BIO = "/orcid-bio";
	private static final String ORCID_WORKS = "/orcid-works";
	private static final String ORCID_PROFILE = "/orcid-profile";
	//private static final String ORCID_RECORD = "/orcid-record";
	
	private static final ObjectMapper mapper = new ObjectMapper();  
	
	public OrcidMessage queryId(String orcidId, RequestType responseType) {
		return parseJson(queryIdString(orcidId, responseType));
	}
	
	public String queryIdString(String orcidId, RequestType responseType) {
		
		int idx = orcidId.indexOf(ORCID_HOST);
		if (idx >= 0)
			orcidId = orcidId.substring(idx + ORCID_HOST.length());
		
		String url = ORCID_URL + orcidId;
		if (responseType == RequestType.bio)
			url += ORCID_BIO;
		if (responseType == RequestType.works)
			url += ORCID_WORKS;
		else if (responseType == RequestType.profile)
			url += ORCID_PROFILE;
			
		return get(url);
	}
	
	public OrcidMessage parseJson(String json) {
		try {
			return mapper.readValue(json, OrcidMessage.class);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}
	
	public OrcidMessage parseJson(File fileJson) {
		try {
			return mapper.readValue(fileJson, OrcidMessage.class);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}
	
	private String get( final String url ) {
		System.out.println("Downloading: " + url);
				
		ClientResponse response = Client.create()
								  .resource( url )
								  .accept( MediaType.APPLICATION_JSON ) 
								  .get( ClientResponse.class );
		
		if (response.getStatus() == 200) 
			return response.getEntity( String.class );
		
		return null;
    } 
}
