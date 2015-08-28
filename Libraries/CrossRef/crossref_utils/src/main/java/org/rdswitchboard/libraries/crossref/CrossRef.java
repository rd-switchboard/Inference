package org.rdswitchboard.libraries.crossref;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;

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
	private static final String URL_CROSSREF = "http://api.crossref.org/";

	private static final String FUNCTION_WORKS = "works";
	/*private static final String FUNCTION_FUNDERS = "funders";
	private static final String FUNCTION_MEMBERS = "members";
	private static final String FUNCTION_TYPES = "types";
	private static final String FUNCTION_LICENSES = "licenses";
	private static final String FUNCTION_JOURNALS = "journals";*/
	
	private static final String URL_CROSSREF_WORKDS = URL_CROSSREF + FUNCTION_WORKS;
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
	
	private File cacheFolder;
	private File cacheWorksFolder;
	
	private static final ObjectMapper mapper = new ObjectMapper();   
	private static final TypeReference<Response<ItemList>> itemListType = new TypeReference<Response<ItemList>>() {};   
	private static final TypeReference<Response<Item>> itemType = new TypeReference<Response<Item>>() {};   
	
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
				System.out.println("Inavlid response");
			
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
	public Item requestWork(final String doi) {
		try {
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
				System.out.println("Inavlid response");
		} catch (JsonParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
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
	
	public File getCacheFolder() {
		return cacheFolder;
	}

	public void setCacheFolder(File cacheFolder) {
		this.cacheFolder = cacheFolder;
		this.cacheFolder.mkdirs();
		
		this.cacheWorksFolder = new File(this.cacheFolder, FUNCTION_WORKS);
		this.cacheWorksFolder.mkdirs();
	}

	public void setCacheFolder(String cacheFolder) {
		setCacheFolder(new File(cacheFolder));
	}
}
