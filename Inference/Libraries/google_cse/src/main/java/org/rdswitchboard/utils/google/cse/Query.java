package org.rdswitchboard.utils.google.cse;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.ws.rs.core.MediaType;

import org.apache.commons.io.FileUtils;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;

/**
 * Class to perform a Google Query.
 * <p>
 * This class require  Google Custom Search Engine ID and Google API Key
 * <p>
 * To obtain it, please follow this manual: https://developers.google.com/custom-search/json-api/v1/overview
 * @author Dmitrij Kudriavcev, dmitrij@kudriavcev.info
 * @version 1.0.1
 * 
 * 2014-12-07: Added queryCache function
 */
public class Query {

	private String cseId;
	private String apiKey;
	private String jsonFolder;
	
	private static final String CSE_API = "https://www.googleapis.com/customsearch/v1?key=%s&cx=%s&q=%s";
	private static final String ENCODE_UTF8 = "UTF-8";
	private static final ObjectMapper mapper = new ObjectMapper();   
	
	/**
	 * Class Constructor
	 * @param cseId String containing Google Custom Search Engine Id
	 * @param apiKey String containing Google API Key
	 */
	public Query(final String cseId, final String apiKey) {
		this.cseId = cseId;
		this.apiKey = apiKey;
	}

	/**
	 * @return Folder to store JSON files. 
	 */
	public String getJsonFolder() {
		return jsonFolder;
	}

	/**
	 * Function to set a path to store JSON files, returned from Google.
	 * <p>
	 * If folder is set, the each response from a Google will be stored into this folder.
	 * Next, if request with same text occurs, the response will be read from this folder 
	 * rather that query Google. This can save time and money, but also could be potentially
	 * dangerous, because the response could be too old. Also the folder could grown rapidly
	 * and need to be clean periodically.
	 * 
	 * @param jsonFolder
	 */
	public void setJsonFolder(String jsonFolder) {
		this.jsonFolder = jsonFolder;
		
		new File(this.jsonFolder).mkdirs();
	} 
	
	/**
	 * Function will sent query to the Google. Each Query will be enclosed in "" to force
	 * Google to return only pages, contains exact string. The query string will be also URLEncoded
	 * with UTF-8 encoding.
	 * <p>
	 * If Json Folder has been set, the function will first search request string in the folder and
	 * will return cached results, if they exists. It will also cache any new result in this folder 
	 * for future use.
	 * 
	 * @param queryString A query string without a quotes and non encoded.
	 * @return QueryResponse
	 */
	public QueryResponse query(final String queryString) {
		try {
			String query = URLEncoder.encode("\"" + queryString + "\"", ENCODE_UTF8);
			File jsonFile = null;
			String json = null;
			if (null != jsonFolder) {
				jsonFile = new File(jsonFolder + "/" + query + ".json");
				if (jsonFile.exists() && !jsonFile.isDirectory())
					json = FileUtils.readFileToString(jsonFile);
			}
			
			if (null == json || json.isEmpty()) {
				json = get(String.format(CSE_API, apiKey, cseId, query));
				
				if (null == json || json.isEmpty())
					return null;
				
				if (null != jsonFile) 
					FileUtils.write(jsonFile, json);
			}
			
			//System.out.println(json);
			
			return mapper.readValue(json, QueryResponse.class);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
	
	/**
	 * Function to query cache only. No actuall Google request will be send
	 * therefore no cseId and apiKey will be required
	 * 
	 * @param queryString A query string without a quotes and non encoded.
	 * @return QueryResponse
	 */
	public QueryResponse queryCache(final String queryString) {
		try {
			String query = URLEncoder.encode("\"" + queryString + "\"", ENCODE_UTF8);
			File jsonFile = null;
			String json = null;
			if (null != jsonFolder) {
				jsonFile = new File(jsonFolder + "/" + query + ".json");
				if (jsonFile.exists() && !jsonFile.isDirectory())
					json = FileUtils.readFileToString(jsonFile);
			}
			
			if (null == json || json.isEmpty()) 
				return null;
			
			return mapper.readValue(json, QueryResponse.class);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
		
		for (int i = 0; i < 10; ++i) {					
			try {
					ClientResponse response = Client.create()
							.resource( url )
							.accept( MediaType.APPLICATION_JSON ) 
							.get( ClientResponse.class );
			
				if (response.getStatus() == 200) 
					return response.getEntity( String.class );
				else {
					System.out.println("Error: " + response.getStatus() + ", JSON: " + response.getEntity( String.class ));
			
					return null;
				}
			} catch (Exception ex) {
				ex.printStackTrace();
				
				try {
					Thread.sleep(10000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		
		return null;			
    }

}
