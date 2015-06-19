package org.rdswitchboard.harvesters.publications;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.io.FileUtils;
import org.rdswitchboard.utils.google.cse.Item;
import org.rdswitchboard.utils.google.cse.Query;
import org.rdswitchboard.utils.google.cse.QueryResponse;
import org.neo4j.rest.graphdb.RestAPI;
import org.neo4j.rest.graphdb.RestAPIFacade;
import org.neo4j.rest.graphdb.query.RestCypherQueryEngine;
import org.neo4j.rest.graphdb.util.QueryResult;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.config.ClientConfig;

public class Harvester {
	private static final String PART_DATA_FROM = "Data from:";
	private static final String PART_PDF = ".pdf";
	private final File folderData;
	private final File folderPublication;
	private final File folderPage;
	
	private static final String CACHE_DATA = "data";
	private static final String CACHE_PUBLICATION = "publication";
	private static final String CACHE_PAGE = "page";
	
	private Query query;  //new Query(CSE, API_KEY);
	private RestAPI graphDb;
	private RestCypherQueryEngine engine;
	//private RestIndex<Node> indexDryadRecord;
	
	private Map<String, Page> pages;
	private Map<String, Publication> publications;
	
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
		folderPublication = new File(cache, CACHE_PUBLICATION);
		folderPage = new File(cache, CACHE_PAGE);
		folderData.mkdirs();
		folderPublication.mkdirs();
		folderPage.mkdirs();
		
		graphDb = new RestAPIFacade(prop.getProperty("neo4j")); //"http://localhost:7474/db/data/");  
		engine = new RestCypherQueryEngine(graphDb);  
		
		query = new Query(prop.getProperty("cse"), prop.getProperty("api_key"));
		query.setJsonFolder(prop.getProperty("json"));
		
		jaxbContext = JAXBContext.newInstance(Publication.class, Page.class);
		jaxbMarshaller = jaxbContext.createMarshaller();
		jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		
		loadPages();
		loadPublications();
	}
	
	@SuppressWarnings("unchecked")
	public void harvestPublications() {
		QueryResult<Map<String, Object>> nodes = engine.query("MATCH (n:Dryad:Publication) RETURN ID(n) AS id, n.key AS key, n.title AS title", null);
		for (Map<String, Object> row : nodes) {
			int nodeId = (int) row.get("id");
			String nodeKey = (String) row.get("key");
			String publicationTitle = null;

			Object title = (String) row.get("title");
			if (title != null) {
				if (title instanceof String)
					publicationTitle = (String) title;
				else
					publicationTitle = ((List<String>)title).get(0);
			}
			
			if (null != publicationTitle && !publicationTitle.isEmpty()) {
				
				if (publicationTitle.startsWith(PART_DATA_FROM))
					publicationTitle = publicationTitle.substring(PART_DATA_FROM.length()).trim();
				
				System.out.println("Searching for: \"" + publicationTitle + "\"");
				
				QueryResponse response = query.query(publicationTitle);
				
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
							Publication publication = null;
							
							if (pages.containsKey(link)) {
								page = pages.get(link);
								page.addData(publicationTitle);
							}
							else {
								String html = get(link);
								if (null != html) {
									File tempFile = File.createTempFile("data_", ".dat", folderData);
									FileUtils.write(tempFile, html);
									page = new Page(link, tempFile.getPath(), publicationTitle);
									pages.put(link, page);
								}
							}
								
							if (null != page) {
								if (publications.containsKey(publicationTitle)) {
									publication = publications.get(publicationTitle);
									publication.addLink(link);
								} else {
									publication = new Publication(nodeId, nodeKey, publicationTitle, link);
									publications.put(publicationTitle, publication);
								}
								
								savePage(page);
								savePublication(publication);
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
	
	protected void loadPublications() {
		// Reset pages map
		publications = new HashMap<String, Publication>();
		
		File[] files = folderPublication.listFiles();
		for (File file : files) 
			if (!file.isDirectory())
			{
				try {
					Publication publication = (Publication) jaxbUnmarshaller.unmarshal(file);
					if (publication != null) {
						publication.setSelf(file.getPath());
						publications.put(publication.getTitle(), publication);
					}
				} catch (JAXBException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
	}
	
	protected void savePublication(Publication publications) {
		try {
			File file;
			if (publications.getSelf() != null)
				file = new File(publications.getSelf());
			else {
				file = File.createTempFile("publication_", ".xml", folderPublication);
				publications.setSelf(file.getPath());
			}
			
			jaxbMarshaller.marshal(publications, new File(publications.getSelf()));
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
