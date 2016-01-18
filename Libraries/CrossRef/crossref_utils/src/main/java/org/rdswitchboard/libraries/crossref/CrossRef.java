package org.rdswitchboard.libraries.crossref;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.List;

import javax.ws.rs.core.MediaType;

import org.apache.commons.io.FileUtils;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;

/**
 * Main class for CrossRef library
 * 
 * @author Dima Kudriavcev, dmitrij@kudriavcev.info
 * @version 1.0.0
 */
public class CrossRef {
	public static final String AUTHORITY_CROSSREF = "CrossRef";
	
	private static final String URL_CROSSREF = "http://api.crossref.org/";

	private static final String CACHE_WORKS = "works";
	private static final String CACHE_AUTHORITY = "authority";
	
	private static final String FUNCTION_WORKS = "works";
	private static final String FUNCTION_DOI_RA = "doiRA";
	/*private static final String FUNCTION_FUNDERS = "funders";
	private static final String FUNCTION_MEMBERS = "members";
	private static final String FUNCTION_TYPES = "types";
	private static final String FUNCTION_LICENSES = "licenses";
	private static final String FUNCTION_JOURNALS = "journals";*/
	
	private static final String URL_CROSSREF_WORKDS = URL_CROSSREF + FUNCTION_WORKS;
	private static final String URL_CROSSREF_DOI_RA = URL_CROSSREF + FUNCTION_DOI_RA;
	/*private static final String URL_CROSSREF_FUNDERS = URL_CROSSREF + FUNCTION_FUNDERS;
	private static final String URL_CROSSREF_MEMBERS = URL_CROSSREF + FUNCTION_MEMBERS;
	private static final String URL_CROSSREF_TYPES = URL_CROSSREF + FUNCTION_TYPES;
	private static final String URL_CROSSREF_LICENSES = URL_CROSSREF + FUNCTION_LICENSES;
	private static final String URL_CROSSREF_JOURNALS = URL_CROSSREF + FUNCTION_JOURNALS;*/
		
	private static final String URL_ENCODING = "UTF-8";
	
	/*private static final String PARAM_QUERY = "q";
	private static final String PARAM_HEADER = "header";*/
	
	private static final String STATUS_OK = "ok";
	
	private static final String MESSAGE_WORK = "work";
	private static final String MESSAGE_WORK_LIST = "work-list";
	
	private static final String EXT_JSON = ".json";
	
//	private static final String PART_DOI = "doi:";
	
	private File cacheFolder;
	private File cacheWorksFolder;
	private File cacheAuthorityFolder;
	
	private long maxAttempts = 10;
	private long attemptDelay = 1000;
	private boolean dbaEnabled = true;
	
	private static final ObjectMapper mapper = new ObjectMapper();   
	private static final TypeReference<Response<ItemList>> itemListType = new TypeReference<Response<ItemList>>() {};   
	private static final TypeReference<Response<Item>> itemType = new TypeReference<Response<Item>>() {};
	private static final TypeReference<List<Authority>> authorityListType = new TypeReference<List<Authority>>() {};
	
	/*
	static {
		SimpleModule module = new SimpleModule("DateModule");
		module = module.addDeserializer(Date.class, new CrossRefDateDeserializer());
		mapper.registerModule(module);
	}*/
	
	/**
	 * Request all works
	 * @return ItemList - a list of works
	 */
	public ItemList requestWorks() {
		try {
			String json = get(URL_CROSSREF_WORKDS);
			if (null != json) {			
				Response<ItemList> response = mapper.readValue(json, itemListType);
				
				//System.out.println(response);
				
				if (response.getStatus().equals(STATUS_OK) && 
					response.getMessageType().equals(MESSAGE_WORK_LIST)) 
					return response.getMessage();
			}		
			else
				System.err.println("Inavlid response");
			
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return null;
	}

	/**
	 * Request work by doi identificator
	 * @param doi String containing doi identificator
	 * @return Item - work information
	 */
	public Item requestWork(String doi) {
		try {
	/*		if (doi.startsWith(PART_DOI))
				doi = doi.substring(PART_DOI.length());*/
			
			String json = null;
			String encodedDoi = URLEncoder.encode(doi, URL_ENCODING);
			File jsonFile = null;
			if (null != cacheWorksFolder) {
				jsonFile = new File (cacheWorksFolder, encodedDoi + EXT_JSON);
				if (jsonFile.exists() && !jsonFile.isDirectory())
					json = FileUtils.readFileToString(jsonFile);
			}
			
			if (null == json) {
				json = get(URL_CROSSREF_WORKDS + "/" + encodedDoi.replace("%2F", "/"));
				if (json != null && !json.isEmpty())
					FileUtils.write(jsonFile, json);
			}
			
			if (null != json) {			
				Response<Item> response = mapper.readValue(json, itemType);
				
				//System.out.println(response);
				
				if (response.getStatus().equals(STATUS_OK) && 
					response.getMessageType().equals(MESSAGE_WORK)) 
					return response.getMessage();
			}		
			else
				System.err.println("Inavlid response");
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		
		return null;
	}
	
	public String requestAuthority(String doi) {
		try {
	/*		if (doi.startsWith(PART_DOI))
				doi = doi.substring(PART_DOI.length());*/
			
			String json = null;
			String encodedDoi = URLEncoder.encode(doi, URL_ENCODING);
			File jsonFile = null;
			if (null != cacheAuthorityFolder) {
				jsonFile = new File (cacheAuthorityFolder, encodedDoi + EXT_JSON);
				if (jsonFile.exists() && !jsonFile.isDirectory())
					json = FileUtils.readFileToString(jsonFile);
			}
			
			if (null == json) {
				json = get(URL_CROSSREF_WORKDS + "/" + encodedDoi.replace("%2F", "/"));
				if (json != null && !json.isEmpty())
					FileUtils.write(jsonFile, json);
			}
			
			if (null != json) {			
				List<Authority> authorities = mapper.readValue(json, authorityListType);
				
				//System.out.println(response);
				
				if (null == authorities)
					return null;
				
				for (Authority authority : authorities) {
					if (authority.getAuthority() != null)
						return authority.getAuthority();
					
					if (authority.getStatus() != null) 
						System.err.println(authority.getStatus());
				}
			}		
			else
				System.err.println("Inavlid response");
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	private String get( final String url ) {
		System.out.println("Downloading: " + url);
						
		long delay = attemptDelay;
		long attemps = maxAttempts;
		for (;;) {
			try {
				ClientResponse response = Client.create()
										  .resource( url )
										  .accept( MediaType.APPLICATION_JSON ) 
										  .get( ClientResponse.class );
				
				if (response.getStatus() == 200) 
					return response.getEntity( String.class );
				else
					return null;
				
			} catch (Exception e) {
				if (attemps <= 0)
					throw e;
				
				--attemps;
				try {
					Thread.sleep(delay);
				} catch (InterruptedException e1) {
					throw e;
				}
				if (dbaEnabled)
					delay = delay * 2;
			}
		}
    } 
	
	public File getCacheFolder() {
		return cacheFolder;
	}

	public void setCacheFolder(File cacheFolder) {
		this.cacheFolder = cacheFolder;
		this.cacheFolder.mkdirs();
		
		this.cacheWorksFolder = new File(this.cacheFolder, CACHE_WORKS);
		this.cacheWorksFolder.mkdirs();
		
		this.cacheAuthorityFolder = new File(this.cacheFolder, CACHE_AUTHORITY);
		this.cacheAuthorityFolder.mkdirs();
	}

	public void setCacheFolder(String cacheFolder) {
		setCacheFolder(new File(cacheFolder));
	}

	public long getMaxAttempts() {
		return maxAttempts;
	}

	public void setMaxAttempts(long maxAttempts) {
		this.maxAttempts = maxAttempts;
	}

	public long getAttemptDelay() {
		return attemptDelay;
	}

	public void setAttemptDelay(long attemptDelay) {
		this.attemptDelay = attemptDelay;
	}

	public boolean isDbaEnabled() {
		return dbaEnabled;
	}

	public void setDbaEnabled(boolean dbaEnabled) {
		this.dbaEnabled = dbaEnabled;
	}
}
