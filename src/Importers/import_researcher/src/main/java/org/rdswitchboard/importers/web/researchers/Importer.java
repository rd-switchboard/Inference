package org.rdswitchboard.importers.web.researchers;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.rdswitchboard.utils.google.cse.Item;
import org.rdswitchboard.utils.google.cse.Query;
import org.rdswitchboard.utils.google.cse.QueryResponse;
import org.rdswitchboard.utils.fuzzy_search.FuzzySearch;
import org.rdswitchboard.utils.fuzzy_search.FuzzySearchException;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.index.IndexHits;
import org.neo4j.rest.graphdb.RestAPI;
import org.neo4j.rest.graphdb.RestAPIFacade;
import org.neo4j.rest.graphdb.entity.RestNode;
import org.neo4j.rest.graphdb.index.RestIndex;
import org.neo4j.rest.graphdb.query.RestCypherQueryEngine;
import org.neo4j.rest.graphdb.util.QueryResult;

public class Importer {
	private static final String FOLDER_PUBLICATIONS = "publications";
	private static final String FOLDER_GRANTS = "grants";
	
	private static final int MIN_TITLE_LENGTH = 10;
	private static final int MIN_DISTANCE = 1;
	private static final double TITLE_DISTANCE = 0.05;
	
	private static final String LABEL_PUBLICATION = "Publication";
	private static final String LABEL_RESEARCHER = "Researcher";
	private static final String LABEL_PATTERN = "Pattern";
	private static final String LABEL_GRANT = "Grant";
	private static final String LABEL_DRYAD = "Dryad";
	private static final String LABEL_WEB = "Web";
	private static final String LABEL_RDA = "RDA";
	
	private static final String LABEL_WEB_RESEARCHER = LABEL_WEB + "_" + LABEL_RESEARCHER;
	
	private static final String RELATIONSHIP_RELATED_TO = "relatedTo";
		
	private static final String PROPERTY_KEY = "key"; 
	private static final String PROPERTY_NODE_SOURCE = "node_source";
	private static final String PROPERTY_NODE_TYPE = "node_type";
	private static final String PROPERTY_URL = "url";
	private static final String PROPERTY_NAME = "name";
	
	/*private static final String PROPERTY_COUNTRY = "country";
	private static final String PROPERTY_STATE = "state";
	private static final String PROPERTY_TITLE = "title";
	
	private static final String PROPERTY_HOST = "host";
	private static final String PROPERTY_PATTERN = "pattern";*/
	
	private static final String PART_DATA_FROM = "Data from: ";
	
	private static final String TYPE_TITLE = "title";
	private static final String TYPE_SIMPLEFIED = "simplfied";
	private static final String TYPE_SCIENTIFIC = "scientific";
	private static final String TYPE_SIMPLIFIED = null;
	
	private RestAPI graphDb;
	private RestCypherQueryEngine engine;
	
	private RestIndex<Node> indexWebResearcher;
		
	private Label labelResearcher = DynamicLabel.label(LABEL_RESEARCHER);
	private Label labelWeb = DynamicLabel.label(LABEL_WEB);
	
	private RelationshipType relRelatedTo = DynamicRelationshipType.withName(RELATIONSHIP_RELATED_TO);
	
	private List<Pattern> webPatterns = new ArrayList<Pattern>();
	private Map<String, LinkingNode> rdaGrants = new HashMap<String, LinkingNode>();
	private Map<String, LinkingNode> dryadPublications = new HashMap<String, LinkingNode>();
	private Map<String, Page> pages = new HashMap<String, Page>();
	
	private JAXBContext jaxbContext;
	private Unmarshaller jaxbUnmarshaller;
	
	private Query googleQuery;// = new Query(null, null);   

	private PrintWriter logger;

	/**
	 * Class constructor. 
	 * @param neo4jUrl An URL to the Neo4J
	 * @throws JAXBException 
	 */
	public Importer(final String neo4jUrl) throws JAXBException {
		graphDb = new RestAPIFacade(neo4jUrl);
		engine = new RestCypherQueryEngine(graphDb);  
		
		engine.query("CREATE CONSTRAINT ON (n:" + LABEL_WEB_RESEARCHER + ") ASSERT n." + PROPERTY_KEY + " IS UNIQUE", Collections.<String, Object> emptyMap());
		indexWebResearcher = graphDb.index().forNodes(LABEL_WEB_RESEARCHER);
				
		jaxbContext = JAXBContext.newInstance(Publication.class, Grant.class, Page.class);
		jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		
		// Do not need to setup query engine, the data must came only from cache at this stage
		googleQuery = new Query(null, null); 
		//googleQuery.setJsonFolder(dataFolder + "/json");
		
		try {
			logger = new PrintWriter("import_web_researcher.log", "UTF-8");
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			e.printStackTrace();
		}		
	}
	
	private void log(String message) {
		System.out.println(message);
		
		logger.println(message);  
		logger.flush();
	}
	
	public void process() {
		loadWebPatterns();
		loadDryadPublications();
		loadRdaGrants();
		
	//	processPublications();
	//	processGrants();
		
		loadPublicationsPages();
		loadGrantsPages();
		processPages();
	}
	
	private boolean isLinkFollowAPattern(String link) {
		for (Pattern pattern : webPatterns) 
			if (pattern.matcher(link).find())
				return true;
		return false;
	}
	
	private int getDistance(int length) {
		int distance = (int) ((double) length * TITLE_DISTANCE + 0.5);
		return distance < MIN_DISTANCE ? MIN_DISTANCE : distance;			
	}
	
	/*
	private String titleToPattern(String title) {
		return title.toLowerCase().replaceAll("[^a-z0-9]", ".+");
	}*/
	
	private void loadWebPatterns() {
		log ("loaging Web:Pattern's");
		
		QueryResult<Map<String, Object>> articles = engine.query("MATCH (n:" + LABEL_WEB + ":" + LABEL_PATTERN + ") RETURN n.pattern as pattern", null);
		for (Map<String, Object> row : articles) {
			String pattern = (String) row.get("pattern");
			if (null != pattern) {
				log("Add Pattern: " + pattern);  
				webPatterns.add(Pattern.compile(pattern));			
			}
		}
	}
	
	private void loadDryadPublications() {
		log ("loaging Dryad:Publication's");
		
		QueryResult<Map<String, Object>> articles = engine.query("MATCH (n:" + LABEL_DRYAD + ":" + LABEL_PUBLICATION + ") RETURN id(n) AS id, n.title AS title", null);
		for (Map<String, Object> row : articles) {
			long nodeId = (long) (Integer) row.get("id");
			String title = (String) row.get("title");
			if (null != title) {
				if (title.contains(PART_DATA_FROM))
					title = title.substring(PART_DATA_FROM.length());
				
				if (dryadPublications.containsKey(title)) {
					log ("Found duplicated page name: " + title);
					dryadPublications.get(title).incCounter();
				} else
					dryadPublications.put(title, new LinkingNode(nodeId, title, TYPE_TITLE));			
			}
		}
	}
	
	private void loadRdaGrants() {
		log ("loaging RDA:Grant's");
		
		// We only interesting in Grants, that has PURL
		
		QueryResult<Map<String, Object>> articles = engine.query("MATCH (n:" + LABEL_RDA + ":" + LABEL_GRANT + ") WHERE has (n.identifier_purl) RETURN id(n) AS id, n.name_primary AS primary, n.name_alternative AS alternative", null);
		for (Map<String, Object> row : articles) {
			long nodeId = (long) (Integer) row.get("id");
			String primary = (String) row.get("primary");
			String alternative = (String) row.get("alternative");
			if (null != primary) {				
				if (rdaGrants.containsKey(primary)) {
					log ("Found duplicated grant name: " + primary);
					rdaGrants.get(primary).incCounter();
				} else
					rdaGrants.put(primary, new LinkingNode(nodeId, primary, TYPE_SIMPLIFIED));	
			}
			if (null != alternative && !alternative.equals(primary)) {
				if (rdaGrants.containsKey(alternative)) {
					log ("Found duplicated grant name: " + alternative);
					rdaGrants.get(alternative).incCounter();
				} else
					rdaGrants.put(alternative, new LinkingNode(nodeId, alternative, TYPE_SCIENTIFIC));	
			}
		}
	}
	
	private String getAuthor(String link, String searchString) {
		QueryResponse response = googleQuery.queryCache(searchString);
		
		String author = null;
		
		if (null != response) 
			for (Item item : response.getItems()) 
				if (item.getLink().equals(link)) {
					 Map<String, Object> pagemap = item.getPagemap();
					 if (null != pagemap) {
						 @SuppressWarnings("unchecked")
						 List<Object> metatags = (List<Object>) pagemap.get("metatags");
						 if (null != metatags && metatags.size() > 0) {
							 @SuppressWarnings("unchecked")
							 Map<String, Object> metatag = (Map<String, Object>) metatags.get(0);
							 if (null != metatag) {
								 String dcTitle = (String) metatag.get("dc.title");
								 String citationAuthor = (String) metatag.get("citation_author");
								 
								 if (null != citationAuthor) {
									 author = citationAuthor;
									 
									 log("Found citation_author: " + citationAuthor);	
								 }
								 
								 if (null != dcTitle) {
									 author = dcTitle;
									 
									 log("Found dc.title: " + dcTitle);	
								 } 
								 	
								 if (null == author) {
									 log("Unable to find author information in metatag");																 
								 }
							 }
						 }
					 }
					 
					 break;
				}
			
		return author;
	}
	
	private void processPublications() {
		log ("Processing cached publications");
		
		googleQuery.setJsonFolder(FOLDER_PUBLICATIONS + "/json");
		
		String path = FOLDER_PUBLICATIONS + "/cache/publication/";
		
		File[] files = new File(path).listFiles();
		for (File file : files) 
			if (!file.isDirectory())
				try {
					Publication publication = (Publication) jaxbUnmarshaller.unmarshal(file);
					if (publication != null) {
						LinkingNode dryadPublication = dryadPublications.get(publication.getTitle());
						if (null != dryadPublication && dryadPublication.isUnique()) 
							for (String link : publication.getLinks()) 
								if (isLinkFollowAPattern(link)) {
									log ("Found matching URL: " + link);
									
									RestNode nodeResearcher = getOrCreateWebResearcher(link, publication.getTitle());
									RestNode nodePublication = graphDb.getNodeById(dryadPublication.getNodeId());
									
									createUniqueRelationship(graphDb, nodePublication, nodeResearcher, 
											relRelatedTo, Direction.OUTGOING, null);								
								}
					}							
				} catch (JAXBException e) {
					e.printStackTrace();
				}
		
	}	
	
	private void processGrants() {
		log ("Processing cached grants");
		
		googleQuery.setJsonFolder(FOLDER_GRANTS + "/json");
		
		String path = FOLDER_GRANTS + "/cache/grant/";
		
		File[] files = new File(path).listFiles();
		for (File file : files) 
			if (!file.isDirectory())
				try {
					Grant grant = (Grant) jaxbUnmarshaller.unmarshal(file);
					if (grant != null) {
						LinkingNode rdaGrant = rdaGrants.get(grant.getName());
						if (null != rdaGrant && rdaGrant.isUnique()) 
							for (String link : grant.getLinks()) 
								if (isLinkFollowAPattern(link)) {
									log ("Found matching URL: " + link);
									
									RestNode nodeResearcher = getOrCreateWebResearcher(link, grant.getName());
									RestNode nodeGrant = graphDb.getNodeById(rdaGrant.getNodeId());
									
									createUniqueRelationship(graphDb, nodeGrant, nodeResearcher, 
											relRelatedTo, Direction.OUTGOING, null);								
								}
					}							
				} catch (JAXBException e) {
					e.printStackTrace();
				}
		
	}	
	
	private void loadPublicationsPages() {
		log ("Loading cached publication pages");
		
		//googleQuery.setJsonFolder(FOLDER_PUBLICATIONS + "/json");
		
		String path = FOLDER_PUBLICATIONS + "/cache/page/";
		
		File[] files = new File(path).listFiles();
		for (File file : files) 
			if (!file.isDirectory())
				try {
					Page page = (Page) jaxbUnmarshaller.unmarshal(file);
					if (page != null && isLinkFollowAPattern(page.getLink()))
						pages.put(page.getLink(), page);
				} catch (JAXBException e) {
					e.printStackTrace();
				}
	}
	
	private void loadGrantsPages() {
		log ("Loading cached grants pages");
		
	//	googleQuery.setJsonFolder(FOLDER_GRANTS + "/json");
		
		String path = FOLDER_GRANTS + "/cache/page/";
		
		File[] files = new File(path).listFiles();
		for (File file : files) 
			if (!file.isDirectory())
				try {
					Page page = (Page) jaxbUnmarshaller.unmarshal(file);
					if (page != null && isLinkFollowAPattern(page.getLink()))
						pages.put(page.getLink(), page);
				} catch (JAXBException e) {
					e.printStackTrace();
				}
	}
		
	private void processPages() {
		log ("Processing cached pages");
		
		for (Page page : pages.values()) {
			log ("Processing URL: " + page.getLink());
			File cacheFile = new File(page.getCache());
			if (cacheFile.exists() && !cacheFile.isDirectory()) {
				try {
					// Load the data
					String cacheData = FileUtils.readFileToString(cacheFile);
					// unescape HTML codes
					cacheData = StringEscapeUtils.unescapeHtml(cacheData)
									.toLowerCase()				// convert to lower case
									.replaceAll("\u00A0", " "); // replace all long spaces with simple space
					
					// convert to char array to save time
					final char[] data = FuzzySearch.stringToCharArray(cacheData);
					
					// enumerate all publications
					for (LinkingNode dryadPublication : dryadPublications.values()) 
						if (dryadPublication.isUnique()) {
							String title = dryadPublication.getTitle();
							if (title.length() > MIN_TITLE_LENGTH) {
								if (FuzzySearch.find(
										FuzzySearch.stringToCharArray(title), 
											data, 
											getDistance(title.length())) >= 0) {
									
									log ("Found matching Publication: " + title);
								
									RestNode nodeResearcher = findWebResearcher(page.getLink());
									if (null != nodeResearcher) {
										RestNode nodePublicaton = graphDb.getNodeById(dryadPublication.getNodeId());
										
										createUniqueRelationship(graphDb, nodePublicaton, nodeResearcher, 
												relRelatedTo, Direction.OUTGOING, null);
									}
								}
							}
					}	
					
					// enumerate all grans
					for (LinkingNode rdaGrant : rdaGrants.values()) 
						if (rdaGrant.isUnique()) {
							String title = rdaGrant.getTitle();
							if (title.length() > MIN_TITLE_LENGTH) {
								if (FuzzySearch.find(
										FuzzySearch.stringToCharArray(title), 
											data, 
											getDistance(title.length())) >= 0) {
									
									log ("Found matching Grant: " + title);
								
									RestNode nodeResearcher = findWebResearcher(page.getLink());
									if (null != nodeResearcher) {
										RestNode nodeGrant = graphDb.getNodeById(rdaGrant.getNodeId());
										
										createUniqueRelationship(graphDb, nodeGrant, nodeResearcher, 
												relRelatedTo, Direction.OUTGOING, null);
									}
								}
							}
					}	
				} catch (Exception e) {
					e.printStackTrace();
				} 
			}
		}
	}
	
	private RestNode findWebResearcher(String link) {
		IndexHits<Node> hits = indexWebResearcher.get(PROPERTY_KEY, link);
		if (null != hits && hits.hasNext())
			return (RestNode) hits.getSingle();
		
		return null;
	}
	
	private RestNode getOrCreateWebResearcher(String link, String searchString) {
		RestNode node = findWebResearcher(link);
		if (null != node)
			return node;
		
		Map<String, Object> map = new HashMap<String, Object>();
		map.put(PROPERTY_KEY, link);
		map.put(PROPERTY_NODE_SOURCE, LABEL_WEB);
		map.put(PROPERTY_NODE_TYPE, LABEL_RESEARCHER);
		map.put(PROPERTY_URL, link);
		
		String author = getAuthor(link, searchString);
		if (null != author)
			map.put(PROPERTY_NAME, author);
		
		node = graphDb.createNode(map);
		
		if (!node.hasLabel(labelResearcher))
			node.addLabel(labelResearcher); 
		if (!node.hasLabel(labelWeb))
			node.addLabel(labelWeb);	
	
		indexWebResearcher.add(node, PROPERTY_KEY, link);	
			
		return node;
	}
	
	
	/*
	private void processPages() {
		log ("Processing cached pages");
		
		String path = dataFolder + "/cache/page/";
			
		File[] files = new File(path).listFiles();
		for (File file : files) 
			if (!file.isDirectory())
				try {
					Page page = (Page) jaxbUnmarshaller.unmarshal(file);
					if (page != null) {
						
						if (isLinkFollowAPattern(page.getLink())) {
							log ("Found matching URL: " + page.getLink());
							
							String author = null;
							Set<String> publications = page.getData();
							if (null != publications && !publications.isEmpty())
								author = getAuthor(page.getLink(), publications.iterator().next());
							
							processPage(page.getLink(), page.getCache(), author, publications);
							
							RestNode nodePublication = graphDb.getNodeById(publication.getNodeId());
							
							createUniqueRelationship(graphDb, nodePublication, nodeResearcher, 
									relRelatedTo, Direction.OUTGOING, null);
						}
					}							
				} catch (JAXBException e) {
					e.printStackTrace();
				}
	}
	

	
	private void processPage(String link, String cache, String author, Set<String> publications) {
		log ("Creating Web:Researcher: url=" + link + ", author=" + link);
		
		Map<String, Object> map = new HashMap<String, Object>();
		map.put(PROPERTY_KEY, link);
		map.put(PROPERTY_NODE_SOURCE, LABEL_WEB);
		map.put(PROPERTY_NODE_TYPE, LABEL_RESEARCHER);
		map.put(PROPERTY_URL, link);
		if (null != author)
			map.put(PROPERTY_NAME, author);
		
		RestNode nodeResearcher = graphDb.getOrCreateNode(indexWebResearcher, 
				PROPERTY_KEY, link, map);
		if (!nodeResearcher.hasLabel(labelResearcher))
			nodeResearcher.addLabel(labelResearcher); 
		if (!nodeResearcher.hasLabel(labelWeb))
			nodeResearcher.addLabel(labelWeb);	
		
		for (DryadPublication publication : dryadPublications) 
			if (publications.contains(publication.getTitle())) {
				log ("Creating relationsip to Dryad:Publication: node_id=" + publication.getNodeId() + ", title=" + publication.getTitle());
				
				RestNode nodePublication = graphDb.getNodeById(publication.getNodeId());
				
				createUniqueRelationship(graphDb, nodePublication, nodeResearcher, 
						relRelatedTo, Direction.OUTGOING, null);
			}
		
		File cacheFile = new File(cache);
		if (cacheFile.exists() && !cacheFile.isDirectory()) {
			try {
				String cacheData = FileUtils.readFileToString(cacheFile).toLowerCase();
				for (RdaGrant grant : rdaGrants) 
					if (grantCounter.get(grant.getTitleLoverCase()).intValue() == 0)
						if (grant.getPattern().matcher(cacheData).find()) {
							log ("Creating relationsip to Rda:Grant: node_id=" + grant.getNodeId() + ", title=" + grant.getTitle());
							
							RestNode nodeGrant = graphDb.getNodeById(grant.getNodeId());
							
							createUniqueRelationship(graphDb, nodeGrant, nodeResearcher, 
									relRelatedTo, Direction.OUTGOING, null);
						}	
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	*/
	
	private void createUniqueRelationship(RestAPI graphDb, RestNode nodeStart, RestNode nodeEnd, 
			RelationshipType type, Direction direction, Map<String, Object> data) {

		// get all node relationships. They should be empty for a new node
		Iterable<Relationship> rels = nodeStart.getRelationships(type, direction);		
		for (Relationship rel : rels) {
			switch (direction) {
			case INCOMING:
				if (rel.getStartNode().getId() == nodeEnd.getId())
					return;
			case OUTGOING:
				if (rel.getEndNode().getId() == nodeEnd.getId())
					return;				
			case BOTH:
				if (rel.getStartNode().getId() == nodeEnd.getId() || 
				    rel.getEndNode().getId() == nodeEnd.getId())
					return;
			}
		}
		
		if (direction == Direction.INCOMING)
			graphDb.createRelationship(nodeEnd, nodeStart, type, data);
		else
			graphDb.createRelationship(nodeStart, nodeEnd, type, data);
	}
 }
