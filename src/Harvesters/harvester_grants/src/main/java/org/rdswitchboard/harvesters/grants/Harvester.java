package org.rdswitchboard.harvesters.grants;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.io.FileUtils;
import org.rdswitchboard.utils.google.cse.Item;
import org.rdswitchboard.utils.google.cse.Query;
import org.rdswitchboard.utils.google.cse.QueryResponse;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.rest.graphdb.RestAPI;
import org.neo4j.rest.graphdb.RestAPIFacade;
import org.neo4j.rest.graphdb.entity.RestNode;
import org.neo4j.rest.graphdb.index.RestIndex;
import org.neo4j.rest.graphdb.query.RestCypherQueryEngine;
import org.neo4j.rest.graphdb.util.QueryResult;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;


public class Harvester {
	private static final String PART_DATA_FROM = "Data from:";
	private static final String PART_PDF = ".pdf";
	private final File folderData;
	private final File folderGrant;
	private final File folderPage;
	
	private static final String CACHE_DATA = "data";
	private static final String CACHE_GRANT = "grant";
	private static final String CACHE_PAGE = "page";
	
	private Query query;  //new Query(CSE, API_KEY);
	private RestAPI graphDb;
	private RestCypherQueryEngine engine;
	//private RestIndex<Node> indexDryadRecord;
	
	private Map<String, Page> pages;
	private Map<String, Grant> grants;
	
	private JAXBContext jaxbContext;
	private Marshaller jaxbMarshaller;
	private Unmarshaller jaxbUnmarshaller;
	
	public Harvester(final String propertiesFile) throws IOException, JAXBException {
		
		Properties prop = new Properties();
		
		InputStream inputStream = new FileInputStream( new File(propertiesFile) );// getClass().getClassLoader().getResourceAsStream(propertiesFile);
		prop.load(inputStream);
		inputStream.close();
		
		String cache = prop.getProperty("cache");
		folderData = new File(cache, CACHE_DATA);
		folderGrant = new File(cache, CACHE_GRANT);
		folderPage = new File(cache, CACHE_PAGE);
		folderData.mkdirs();
		folderGrant.mkdirs();
		folderPage.mkdirs();
		
		graphDb = new RestAPIFacade(prop.getProperty("neo4j")); //"http://localhost:7474/db/data/");  
		engine = new RestCypherQueryEngine(graphDb);  
		
		query = new Query(prop.getProperty("cse"), prop.getProperty("api_key"));
		query.setJsonFolder(prop.getProperty("json"));
		
		jaxbContext = JAXBContext.newInstance(Grant.class, Page.class);
		jaxbMarshaller = jaxbContext.createMarshaller();
		jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		
		loadPages();
		loadGrants();
	}
	
	@SuppressWarnings("unchecked")
	public void harvestGrants() {
		QueryResult<Map<String, Object>> nodes = engine.query("MATCH (n:RDA:Grant) WHERE has (n.identifier_purl) RETURN ID(n) AS id, n.key AS key, n.name_primary AS primary, n.name_alternative AS alternative", null);
		for (Map<String, Object> row : nodes) {
			int nodeId = (int) row.get("id");
			String nodeKey = (String) row.get("key");
			String namePrimary = null;
			String nameAlternative = null;
			String nameGrant = null;

			Object primary = (String) row.get("primary");
			if (primary != null) {
				if (primary instanceof String)
					namePrimary = (String) primary;
				else
					namePrimary = ((List<String>)primary).get(0);
			}
			Object alternative = row.get("alternative");;
			if (alternative != null) {
				if (alternative instanceof String)
					nameAlternative = (String) alternative;
				else
					nameAlternative = ((List<String>)alternative).get(0);
			}			
			
			
			// queryCache for cache search
			
			if (null != namePrimary && !namePrimary.isEmpty()) {
				QueryResponse response = query.query(nameGrant = namePrimary);
				
				if (null == response || response.getItems() == null || response.getItems().size() == 0) 
					if (null != nameAlternative && !nameAlternative.isEmpty()) 
						response = query.query(nameGrant = nameAlternative);
					
				if (null != response && response.getItems() != null && response.getItems().size() > 0) {
					System.out.println("found " + response.getItems().size() + " sites");
					
					for (Item item : response.getItems()) {
						String link = item.getLink();
						try {
							// try and parts link
							URL linkUrl = new URL(link);
							
														// check that link is not a PDF
							String path = linkUrl.getPath().toLowerCase();
							if (path.endsWith(PART_PDF))
								continue;							
							
							// we migth be already downloaded this page
							// in this case, we need to check, that page actually contains the grant name
							// if that is true, we will just add the grant name to it
							// and will add link to the grant, creating the grant if needed
							Page page = null;
							Grant grant = null;
							
							if (pages.containsKey(link)) {
								page = pages.get(link);
								page.addData(nameGrant);
							}
							else {
								String html = get(link);
								if (null != html) {
									File tempFile = File.createTempFile("data_", ".dat", folderData);
									FileUtils.write(tempFile, html);
									page = new Page(link, tempFile.getPath(), nameGrant);
									pages.put(link, page);
								}
							}
								
							if (null != page) {
								if (grants.containsKey(nameGrant)) {
									grant = grants.get(nameGrant);
									grant.addLink(link);
								} else {
									grant = new Grant(nodeId, nodeKey, nameGrant, link);
									grants.put(nameGrant, grant);
								}
								
								savePage(page);
								saveGrant(grant);
							}
						} catch (MalformedURLException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			}	
		}
	}
	
	private String get( final String url ) {
		for (int i = 0; i < 2; ++i) {
			try {
				System.out.println("Downloading: " + url);
				
				Client client = Client.create();
				client.setConnectTimeout(3000);
				client.setReadTimeout(3000);
				
				ClientResponse response = client.resource( url ).get( ClientResponse.class );
				
				if (response.getStatus() == 200) 
					return response.getEntity( String.class );
				
		//		System.out.println("Error: " + response.getStatus() + ", JSON: " + response.getEntity( String.class ));
				return null;
				
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		return null;
    }
	
	protected void loadPages() {
		pages = new HashMap<String, Page>();
		
		File[] files = folderPage.listFiles();
		for (File file : files) 
			if (!file.isDirectory())
			{
				try {
					Page page = (Page) jaxbUnmarshaller.unmarshal(file);
					if (page != null) {
						page.setSelf(file.getPath());
						pages.put(page.getLink(), page);
					}
				} catch (JAXBException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
	}
	
	protected void savePage(Page page) {
		try {
			File file;
			if (page.getSelf() != null)
				file = new File(page.getSelf());
			else {
				file = File.createTempFile("link_", ".xml", folderPage);
				page.setSelf(file.getPath());
			}
			jaxbMarshaller.marshal(page, file);
			//jaxbMarshaller.marshal(page, System.out);
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	protected void loadGrants() {
		// Reset pages map
		grants = new HashMap<String, Grant>();
		
		File[] files = folderGrant.listFiles();
		for (File file : files) 
			if (!file.isDirectory())
			{
				try {
					Grant grant = (Grant) jaxbUnmarshaller.unmarshal(file);
					if (grant != null) {
						grant.setSelf(file.getPath());
						grants.put(grant.getName(), grant);
					}
				} catch (JAXBException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
	}
	
	protected void saveGrant(Grant grant) {
		try {
			File file;
			if (grant.getSelf() != null)
				file = new File(grant.getSelf());
			else {
				file = File.createTempFile("grant_", ".xml", folderGrant);
				grant.setSelf(file.getPath());
			}
			
			jaxbMarshaller.marshal(grant, new File(grant.getSelf()));
		//	jaxbMarshaller.marshal(grant, System.out);
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
