package org.rdswitchboard.search.google;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.rdswitchboard.libraries.configuration.Configuration;
import org.rdswitchboard.libraries.graph.GraphUtils;
import org.rdswitchboard.libraries.neo4j.Neo4jDatabase;
import org.rdswitchboard.libraries.neo4j.interfaces.ProcessNode;
import org.rdswitchboard.utils.google.cache2.GoogleUtils;
import org.rdswitchboard.utils.google.cache2.Link;
import org.rdswitchboard.utils.google.cache2.Result;
import org.rdswitchboard.utils.google.cse.Item;
import org.rdswitchboard.utils.google.cse.Query;
import org.rdswitchboard.utils.google.cse.QueryResponse;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;

public class App {
	private static final String GOOGLE_CACHE = "google";
	private static final String BLACK_LIST = "conf/black.list";
	private static final String MIN_TITLE_LENGTH = "30";
	private static final String MAX_ATTEMPTS = "2";
	private static final String ATTEMPT_DELAY = "1000";
	private static final String CONNECTION_TIMEOUT = "30000";
	private static final String READ_TIMEOUT = "30000";
	
	//private static final String PART_DATA_FROM = "Data from:";
	private static final String PART_PDF = ".pdf";
	
	private static Query query;
	private static Neo4jDatabase neo4j;
	private static Set<String> blackList;
	private static Client client;
	
	private static int minTitleLength;
	private static int maxAttempts;
	private static int attemptDelay;
	
	private static Map<String, Link> links;
	//private static Map<String, Result> results;
	
	private static File dataFolder;
	private static File metadataFolder;
	private static File linkFolder;
	private static File brokenFolder;
	private static File resultFolder;
		
	public static void main(String[] args) {
		try {
			Properties properties = Configuration.fromArgs(args);
	        
	        String neo4jFolder = properties.getProperty(Configuration.PROPERTY_NEO4J);
	        if (StringUtils.isEmpty(neo4jFolder))
                throw new IllegalArgumentException("Neo4j Folder can not be empty");
	        System.out.println("Neo4j Folder: " + neo4jFolder);

	        String blackListPath = properties.getProperty(Configuration.PROPERTY_GOOGLE_BLACK_LIST, BLACK_LIST);
	        if (StringUtils.isEmpty(blackListPath))
                throw new IllegalArgumentException("Black List Path can not be empty");
	        System.out.println("Black List: " + blackListPath);
			
	        String googleCseId = properties.getProperty(Configuration.PROPERTY_GOOGLE_CSE_ID);
	        if (StringUtils.isEmpty(googleCseId))
                throw new IllegalArgumentException("Google CSE ID can not be empty");

	        String googleApiKey = properties.getProperty(Configuration.PROPERTY_GOOGLE_API_KEY);
	        if (StringUtils.isEmpty(googleApiKey))
                throw new IllegalArgumentException("Google API Key can not be empty");
	  
	        String googleCache = properties.getProperty(Configuration.PROPERTY_GOOGLE_CACHE, GOOGLE_CACHE);
	        if (StringUtils.isEmpty(googleCache))
                throw new IllegalArgumentException("Google Cache Folder can not be empty");
	        System.out.println("Google Folder: " + googleCache);
	        	        
	        dataFolder = GoogleUtils.getDataFolder(googleCache);
	        dataFolder.mkdirs();
	        System.out.println("Data Folder: " + dataFolder);
	        
	        metadataFolder = GoogleUtils.getMetadataFolder(googleCache);
	        metadataFolder.mkdirs();
	        System.out.println("Metadata Folder: " + metadataFolder);
	        
	        linkFolder = GoogleUtils.getLinkFolder(googleCache);
	        linkFolder.mkdirs();
	        System.out.println("Link Folder: " + linkFolder);

	        brokenFolder = GoogleUtils.getBrokenLinkFolder(googleCache);
	        brokenFolder.mkdirs();
	        System.out.println("Broken Link Folder: " + brokenFolder);

	        resultFolder = GoogleUtils.getResultFolder(googleCache);
	        resultFolder.mkdirs();
	        System.out.println("Result Folder: " + resultFolder);
	        
	        minTitleLength = Integer.parseInt(properties.getProperty(Configuration.PROPERTY_GOOGLE_MIN_TITLE_LENGTH, MIN_TITLE_LENGTH));
	        System.out.println("Minimum Title Length: " +  minTitleLength);

	        maxAttempts = Integer.parseInt(properties.getProperty(Configuration.PROPERTY_GOOGLE_MAX_ATTEMPTS, MAX_ATTEMPTS));
	        attemptDelay = Integer.parseInt(properties.getProperty(Configuration.PROPERTY_GOOGLE_ATTEMPT_DELAY, ATTEMPT_DELAY));
	        int connectionTimeout = Integer.parseInt(properties.getProperty(Configuration.PROPERTY_GOOGLE_CONNECTION_TIMEOUT, CONNECTION_TIMEOUT));
	        int readTimeout = Integer.parseInt(properties.getProperty(Configuration.PROPERTY_GOOGLE_READ_TIMEOUT, READ_TIMEOUT));
	        	       
	        blackList = GoogleUtils.loadBlackList(blackListPath);
	        links =  GoogleUtils.loadLinks(brokenFolder, links);
	        links =  GoogleUtils.loadLinks(linkFolder, links);
	      //  results = GoogleUtils.loadResuls(resultFolder);
	        
	        query = new Query(googleCseId, googleApiKey);
	        query.setJsonFolder(GoogleUtils.getJsonFolder(googleCache).toString());
	        
	        neo4j = new Neo4jDatabase(neo4jFolder);
	        
	        client = Client.create();
			client.setConnectTimeout(connectionTimeout);
			client.setReadTimeout(readTimeout);
			
	        processNodes(
	        		DynamicLabel.label(GraphUtils.SOURCE_ANDS), 
	        		DynamicLabel.label(GraphUtils.TYPE_GRANT), 
	        		GraphUtils.PROPERTY_TITLE);	        
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static void processNodes(final Label source, final Label type, final String titleProperty) {
		try {
			neo4j.enumrateAllNodesWithLabel(source, new ProcessNode() {
	
				@Override
				public boolean processNode(Node node) throws Exception {
					try {
						if (node.hasLabel(type) && node.hasProperty(titleProperty)) {
							Object titles = node.getProperty(titleProperty);
							if (titles instanceof String)
								processTitle((String) titles);
							else if (titles instanceof String[])
								for (String title : (String[]) titles)
									processTitle(title);
						}
					} catch (Exception e) {
						e.printStackTrace();
						
						return false;
					}					
					
					return true;
				}			
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static void processTitle(String title) throws Exception {
		if (null != title) {
			title = title.trim().toLowerCase();
		
			if (title.length() > minTitleLength && !blackList.contains(title)) {
				
				Result result = new Result();
				result.setText(title);
					
				System.out.println("Quering: " + title);
			
				QueryResponse response = query.query(title);
				if (null == response)
					throw new Exception("Google Query has return unexpected result");
				if (response.getItems() != null && response.getItems().size() > 0) {
					System.out.println("found " + response.getItems().size() + " sites");
						
					for (Item item : response.getItems()) {
						String url = item.getLink();
						try {
							Link link = links.get(url);
							if (null == link) {
								
								URL linkUrl = new URL(url);
								String path = linkUrl.getPath().toLowerCase();
								
								if (path.endsWith(PART_PDF))
									continue;
								
								link = new Link();
								link.setLink(url);
								links.put(url, link);
							
								String html = download(url);
								if (null != html) {
									String dataFile = GoogleUtils.saveData(dataFolder, html);
									if (null != dataFile) {
										
										link.setData(dataFile);
									
										Map<String, Object> pagemap = item.getPagemap();
										if (null != pagemap) {
											String metadataFile = GoogleUtils.savePagemap(metadataFolder, pagemap);
											if (null != metadataFile)
												link.setMetadata(metadataFile);
										}
									}
								}
								
								GoogleUtils.saveLink(
										link.getData() != null ? linkFolder : brokenFolder, link);
							}
							
							if (null != link && null != link.getData())
								result.addLink(link.getSelf());
						} catch (MalformedURLException e) {
							e.printStackTrace();
						} 
					}
					
					if (result.getLinks() != null && result.getLinks().size() > 0)
						GoogleUtils.saveResult(resultFolder, result);
				}
			}
		}
	}
	
	private static String download( final String url ) {
		for (int i = 0; i < maxAttempts; ++i) {
			try {
				System.out.println("Downloading: " + url);
				
				ClientResponse response = client.resource( url ).get( ClientResponse.class );
				
				if (response.getStatus() == 200) 
					return response.getEntity( String.class );
				
		//		System.out.println("Error: " + response.getStatus() + ", JSON: " + response.getEntity( String.class ));
				return null;
				
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			
			if (i < maxAttempts - 1) {
				try {
					Thread.sleep(attemptDelay);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		
		return null;
    }
}
