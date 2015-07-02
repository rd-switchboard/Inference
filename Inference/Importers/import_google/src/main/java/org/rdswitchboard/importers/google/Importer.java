package org.rdswitchboard.importers.google;

/**
 * 
 * @author dima
 *
 */

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.rdswitchboard.utils.google.cache.Grant;
import org.rdswitchboard.utils.google.cache.Page;
import org.rdswitchboard.utils.google.cache.Publication;
import org.rdswitchboard.utils.google.cse.Item;
import org.rdswitchboard.utils.google.cse.Query;
import org.rdswitchboard.utils.google.cse.QueryResponse;
import org.rdswitchboard.utils.neo4j.Neo4jUtils;
import org.rdswitchboard.utils.aggrigation.AggrigationUtils;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.index.IndexHits;
import org.neo4j.rest.graphdb.RestAPI;
import org.neo4j.rest.graphdb.RestAPIFacade;
import org.neo4j.rest.graphdb.entity.RestNode;
import org.neo4j.rest.graphdb.index.RestIndex;
import org.neo4j.rest.graphdb.query.RestCypherQueryEngine;
import org.neo4j.rest.graphdb.util.QueryResult;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

public class Importer {

	private RestAPI graphDb;
	private RestCypherQueryEngine engine;
	private RestIndex<Node> indexWebResearcher;
	
	private JAXBContext jaxbContext;
	//private Marshaller jaxbMarshaller;
	private Unmarshaller jaxbUnmarshaller;
	
//	private ObjectMapper mapper; 
//	private static final TypeReference<Map<String, Object>> refMap = new TypeReference<Map<String, Object>>() {};
	
	private Query googleQuery;
	
	private PrintWriter logger;
	private PrintWriter grants;
	private PrintWriter publications;
	
	private enum Labels implements Label {
		Web, Dryad, RDA, Researcher, Pattern, Publication, Grant
	}
	
	private enum RelTypes implements RelationshipType {
		relatedTo
	}
	
	private static final String FOLDER_CACHE = "cache";
	private static final String FOLDER_PUBLICATION = "publication";
	private static final String FOLDER_GRANT = "grant";
	private static final String FOLDER_JSON = "json";
	
	private static final String LABEL_PATTERN = Labels.Pattern.name();
	
	private static final String PROPERTY_URL = "url";
	private static final String PROPERTY_NAME = "name";
	private static final String PROPERTY_SOURCE_GOOGLE = "source_google";
	
	private static final String PART_DATA_FROM = "data from: ";
	
	private static final boolean VALUE_TRUE = true;
	private static final int MIN_TITLE_LENGTH = 20;
	
	
	private List<Pattern> webPatterns;
	private Set<String> blackList;
	//private Map<String, Page> pages;
	
	
	public Importer(String neo4jUrl) throws FileNotFoundException, UnsupportedEncodingException, JAXBException {
		graphDb = new RestAPIFacade(neo4jUrl);
		engine = new RestCypherQueryEngine(graphDb);  
		
		Neo4jUtils.createConstraint(engine, Labels.Web, Labels.Researcher);
		indexWebResearcher = Neo4jUtils.getIndex(graphDb, Labels.Web, Labels.Researcher);
				
		jaxbContext = JAXBContext.newInstance(Publication.class, Grant.class, Page.class);
	//	jaxbMarshaller = jaxbContext.createMarshaller();
		jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		
		// setup Object mapper
		//mapper = new ObjectMapper(); 
		
		googleQuery = new Query(null, null); 
//		//googleQuery.setJsonFolder(dataFolder + "/json");
		
/*		// Do not need to setup query engine, the data must came only from cache at this stage
		googleQuery = new Query(null, null); 
		//googleQuery.setJsonFolder(dataFolder + "/json");
		*/	
		logger = new PrintWriter("import_google.log", StandardCharsets.UTF_8.name());
		grants  = new PrintWriter("google_grants.log", StandardCharsets.UTF_8.name());
		publications  = new PrintWriter("google_publications.log", StandardCharsets.UTF_8.name());
	}
	
	private Set<String> loadBlackList(String blackList) throws FileNotFoundException, IOException {
        Set<String> list = new HashSet<String>();
        
        try(BufferedReader br = new BufferedReader(new FileReader(new File(blackList)))) {
            for(String line; (line = br.readLine()) != null; ) 
                list.add(line.trim().toLowerCase());
        }
        
        return list;
    }

	private List<Pattern> loadWebPatterns(RestCypherQueryEngine engine) {
	        List<Pattern> webPatterns = new ArrayList<Pattern>();
	
	        QueryResult<Map<String, Object>> articles = engine.query("MATCH (n:" + AggrigationUtils.LABEL_WEB + ":" + LABEL_PATTERN + ") RETURN n.pattern as pattern", null);
	        for (Map<String, Object> row : articles) {
	                String pattern = (String) row.get("pattern");
	                if (null != pattern) {
	                        webPatterns.add(Pattern.compile(pattern));
	                }
	        }
	        
	        return webPatterns;
	}
	
	private Map<String, Set<Long>> loadDryadPublications(RestCypherQueryEngine engine, Map<String, Set<Long>> nodes, Set<String> blackList) {
        if (null == nodes)
                nodes = new HashMap<String, Set<Long>>(); 

        QueryResult<Map<String, Object>> articles = engine.query("MATCH (n:" + AggrigationUtils.LABEL_DRYAD + ":" + AggrigationUtils.LABEL_PUBLICATION + ") RETURN id(n) AS id, n.title AS title", null);
        for (Map<String, Object> row : articles) {
                long nodeId = (long) (Integer) row.get("id");
                String title = ((String) row.get("title"));
                if (null != title) {
                        title = title.trim().toLowerCase();
                        if (title.contains(PART_DATA_FROM))
                                title = title.substring(PART_DATA_FROM.length());
                        if (title.length() > MIN_TITLE_LENGTH 
                                        && !blackList.contains(title)) 
                                putUnique(nodes, title, nodeId);
                }
        }

        return nodes;
	}
	
	private Map<String, Set<Long>> loadRdaGrants(RestCypherQueryEngine engine, Map<String, Set<Long>> nodes, Set<String> blackList) {
        if (null == nodes)
                nodes = new HashMap<String, Set<Long>>(); 

        QueryResult<Map<String, Object>> articles = engine.query("MATCH (n:" + AggrigationUtils.LABEL_RDA + ":" + AggrigationUtils.LABEL_GRANT + ") WHERE has (n.identifier_purl) RETURN id(n) AS id, n,name_primary AS primary, n.name_alternative AS alternative", null);
        for (Map<String, Object> row : articles) {
                long nodeId = (long) (Integer) row.get("id");
                String primary = ((String) row.get("primary"));
                String alternative = ((String) row.get("alternative"));

                if (null != primary) {
                        primary = primary.trim().toLowerCase();
                        if (primary.length() > MIN_TITLE_LENGTH
                                        && !blackList.contains(primary)) 
                                putUnique(nodes, primary, nodeId);
                }
                if (null != alternative) {
                        alternative = alternative.trim().toLowerCase();
                        if (alternative.length() > MIN_TITLE_LENGTH
                                        && !alternative.equals(primary) 
                                        && !blackList.contains(alternative)) 
                                putUnique(nodes, alternative, nodeId);
                }
        }

        return nodes;
	}
	
	private void putUnique(Map<String, Set<Long>> nodes, String key, Long id) {
        if (nodes.containsKey(key))
                nodes.get(key).add(id);
        else {
                Set<Long> set = new HashSet<Long>();
                set.add(id);

                nodes.put(key, set);
        }
	}
	
	private boolean isLinkFollowAPattern(List<Pattern> webPatterns, String link) {
        for (Pattern pattern : webPatterns) 
                if (pattern.matcher(link).find())
                        return true;
        return false;
    }
	
	private RestNode findWebResearcher(RestIndex<Node> indexWebResearcher, String link) {
        IndexHits<Node> hits = indexWebResearcher.get(Neo4jUtils.PROPERTY_KEY, link);
        if (null != hits && hits.hasNext())
                return (RestNode) hits.getSingle();

        return null;
    }  
	
	
	/**
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 * 
	 */
	public void init(String blackList) throws FileNotFoundException, IOException {
		this.blackList = loadBlackList(blackList);
		this.webPatterns = loadWebPatterns(engine);
	}
	
	/**
	 * 
	 * @param googleCache
	 */
	public void processPublications(String googleCache) {
		Map<String, Set<Long>> dryadPublications = loadDryadPublications(engine, null, blackList);
	//	pages = loadPages(googleCache);
		processPublications(dryadPublications, googleCache);
	}
	
	/**
	 * 
	 * @param googleCache
	 */
	public void processGrants(String googleCache) {
		Map<String, Set<Long>> rdaGrants = loadRdaGrants(engine, null, blackList);
	//	pages = loadPages(googleCache);
		processGrants(rdaGrants, googleCache);
	}
	
	private void processPublications(Map<String, Set<Long>> dryadPublications, String googleCache) {
		log ("Processing cached publications");
		
		googleQuery.setJsonFolder(new File(googleCache, FOLDER_JSON).getPath());
		
		Map<String, Object> pars = new HashMap<String, Object>();
		pars.put(PROPERTY_SOURCE_GOOGLE, VALUE_TRUE);
	
		File publicationCache = new File (googleCache, FOLDER_CACHE + "/" + FOLDER_PUBLICATION);
		File[] files = publicationCache.listFiles();
		for (File file : files) 
			if (!file.isDirectory())
				try {
					Publication publication = (Publication) jaxbUnmarshaller.unmarshal(file);
					if (publication != null) {
						Set<Long> nodeIds = dryadPublications.get(publication.getTitle().trim().toLowerCase());
						if (null != nodeIds) 
							for (String link : publication.getLinks()) 
								if (isLinkFollowAPattern(webPatterns, link)) {
									log ("Found matching URL: " + link + " for publication: " + publication.getTitle());
									logPublication(publication.getTitle());
									
									RestNode nodeResearcher = getOrCreateWebResearcher(link, publication.getTitle());
									for (Long nodeId : nodeIds) {
										RestNode nodePublication = graphDb.getNodeById(nodeId);
									
										Neo4jUtils.createUniqueRelationship(graphDb, nodePublication, nodeResearcher, 
												RelTypes.relatedTo, Direction.OUTGOING, pars);
									}
								}
					}							
				} catch (JAXBException e) {
					e.printStackTrace();
				}
		
	}	
	
	private void processGrants(Map<String, Set<Long>> rdaGrants, String googleCache) {
		log ("Processing cached grants");
		
		googleQuery.setJsonFolder(new File(googleCache, FOLDER_JSON).getPath());
		
		Map<String, Object> pars = new HashMap<String, Object>();
		pars.put(PROPERTY_SOURCE_GOOGLE, VALUE_TRUE);
		
		File grantCache = new File (googleCache, FOLDER_CACHE + "/" + FOLDER_GRANT);
		File[] files = grantCache.listFiles();
		for (File file : files) 
			if (!file.isDirectory())
				try {
					Grant grant = (Grant) jaxbUnmarshaller.unmarshal(file);
					if (grant != null) {
						Set<Long> nodeIds = rdaGrants.get(grant.getName().trim().toLowerCase());
						if (null != nodeIds) 
							for (String link : grant.getLinks()) 
								if (isLinkFollowAPattern(webPatterns, link)) {
									log ("Found matching URL: " + link + " for grant: " + grant.getName());
									logGrant(grant.getName());
									
									RestNode nodeResearcher = getOrCreateWebResearcher(link, grant.getName());
									for (Long nodeId : nodeIds) {
										RestNode nodeGrant = graphDb.getNodeById(nodeId);
										
										Neo4jUtils.createUniqueRelationship(graphDb, nodeGrant, nodeResearcher, 
												RelTypes.relatedTo, Direction.OUTGOING, pars);	
									}
								}
					}							
				} catch (JAXBException e) {
					e.printStackTrace();
				}
		
	}	

	private RestNode getOrCreateWebResearcher(String link, String searchString) {
		RestNode node = findWebResearcher(indexWebResearcher, link);
		if (null != node)
			return node;
		
		Map<String, Object> map = new HashMap<String, Object>();
		map.put(Neo4jUtils.PROPERTY_KEY, link);
		map.put(Neo4jUtils.PROPERTY_NODE_SOURCE, AggrigationUtils.LABEL_WEB);
		map.put(Neo4jUtils.PROPERTY_NODE_TYPE, AggrigationUtils.LABEL_RESEARCHER);
		map.put(PROPERTY_URL, link);
		
		String author = getAuthor(link, searchString);
		if (null != author)
			map.put(PROPERTY_NAME, author);
		
		node = graphDb.createNode(map);
		
		if (!node.hasLabel(Labels.Researcher))
			node.addLabel(Labels.Researcher); 
		if (!node.hasLabel(Labels.Web))
			node.addLabel(Labels.Web);	
	
		indexWebResearcher.add(node, Neo4jUtils.PROPERTY_KEY, link);	
			
		return node;
	}
		
	private Map<String, Object> getPageMap(String link, String searchString) throws JsonParseException, JsonMappingException, IOException, JAXBException {
		/*Page page = pages.get(link);
		if (null != page) {
			String mapUri = page.getMap();
			if (null != mapUri && mapUri.isEmpty()) {
				
				if (mapUri.equals(VALUE_NULL)) 
					return null;
				
				return mapper.readValue(mapUri, refMap);
			} 
			*/
		
			QueryResponse response = googleQuery.queryCache(searchString);
			if (null != response) 
				for (Item item : response.getItems()) 
					if (item.getLink().equals(link)) {
						Map<String, Object> pagemap = item.getPagemap();
						if (null != pagemap) {
		/*					String mapFile = FilenameUtils.removeExtension(page.getCache());
							String mapFileName = FilenameUtils.getName(mapFile).replace("data", "map");
							String mapFilePath = FilenameUtils.getPathNoEndSeparator(mapFile);
							String mapBasePath = FilenameUtils.getPathNoEndSeparator(mapFilePath);
							String mapPath = FilenameUtils.concat(mapBasePath, FOLDER_MAP + "/" + mapFileName + ".json");
							
							page.setMap(mapPath);
							
							mapper.writeValue(new File(mapPath), pagemap);
							jaxbMarshaller.marshal(page, new File(page.getSelf()));*/
							
							return pagemap;
						}

						break;
					}
			
	/*		page.setMap(VALUE_NULL);
			jaxbMarshaller.marshal(page, new File(page.getSelf()));
		}
		*/
		return null;
	}
	
	private String getAuthor(String link, String searchString) {
		String author = null;
		
		try {
			Map<String, Object> pagemap = getPageMap(link, searchString);
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
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		 
		return author;
	}
	
		
	/**
	 * Service log fiunction
	 * @param message
	 */
	private void log(String message) {
		System.out.println(message);
		
		logger.println(message);  	
	}
	
	private void logGrant(String grant) {
		grants.println(grant);  	
	}
	
	private void logPublication(String publication) {
		publications.println(publication);  	
	}
	
	/**
	 * Servise flush log function
	 */
	/*private void flushLog() {
		logger.flush();
	}*/
}
