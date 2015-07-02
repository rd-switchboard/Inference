package org.rdswitchboard.importers.fuzzy;

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

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.rdswitchboard.utils.google.cache.Grant;
import org.rdswitchboard.utils.google.cache.Page;
import org.rdswitchboard.utils.google.cache.Publication;
import org.rdswitchboard.utils.graph.GraphUtils;
import org.rdswitchboard.utils.neo4j.Neo4jUtils;
import org.rdswitchboard.utils.aggrigation.AggrigationUtils;
import org.rdswitchboard.utils.fuzzy_search.FuzzySearch;
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

public class Importer {

	private RestAPI graphDb;
	private RestCypherQueryEngine engine;
	private RestIndex<Node> indexWebResearcher;
	
	private JAXBContext jaxbContext;
	private Unmarshaller jaxbUnmarshaller;
		
	private PrintWriter logger;
	private PrintWriter fuzzy;
	
	private enum Labels implements Label {
		Web, Dryad, RDA, Researcher, Pattern, Publication, Grant
	}
	
	private enum RelTypes implements RelationshipType {
		relatedTo
	}
	
	private static final String FOLDER_CACHE = "cache";
	private static final String FOLDER_PAGE = "page";
	private static final String PROPERTY_SOURCE_FUZZY = "source_fuzzy";
	
	private static final String LABEL_PATTERN = Labels.Pattern.name();
    private static final String PART_DATA_FROM = "data from: ";
	
	private static final boolean VALUE_TRUE = true;
	private static final int MIN_TITLE_LENGTH = 20;
	private static final int MIN_DISTANCE = 1;
	private static final double TITLE_DISTANCE = 0.05;
		
	private List<Pattern> webPatterns;
	private Set<String> blackList;
	
	private Map<String, Set<Long>> nodes = new HashMap<String, Set<Long>>();

	public Importer(String neo4jUrl) throws FileNotFoundException, UnsupportedEncodingException, JAXBException {
		graphDb = new RestAPIFacade(neo4jUrl);
		engine = new RestCypherQueryEngine(graphDb);  
		
		Neo4jUtils.createConstraint(engine, Labels.Web, Labels.Researcher);
		indexWebResearcher = Neo4jUtils.getIndex(graphDb, Labels.Web, Labels.Researcher);
				
		jaxbContext = JAXBContext.newInstance(Publication.class, Grant.class, Page.class);
		jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		
		logger = new PrintWriter("import_fuzzy.log", StandardCharsets.UTF_8.name());
		fuzzy = new PrintWriter("fuzzy_search.log", StandardCharsets.UTF_8.name());
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
	
	private int getDistance(int length) {
        int distance = (int) ((double) length * TITLE_DISTANCE + 0.5);
        return distance < MIN_DISTANCE ? MIN_DISTANCE : distance;
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
		
		loadDryadPublications(engine, nodes, this.blackList);
		loadRdaGrants(engine, nodes, this.blackList);
	}
	
	/**
	 * 
	 * @param googleCache
	 */
	
	public void process(String googleCache) {
		log ("Processing cached publications");

	//	googleQuery.setJsonFolder(new File(googleCache, FOLDER_JSON).getPath());

		Map<String, Object> pars = new HashMap<String, Object>();
		pars.put(PROPERTY_SOURCE_FUZZY, VALUE_TRUE);

		File pages = new File (googleCache, FOLDER_CACHE + "/" + FOLDER_PAGE);
		File[] files = pages.listFiles();
		for (File file : files) 
			if (!file.isDirectory())
				try {
					Page page = (Page) jaxbUnmarshaller.unmarshal(file);
					if (page != null && isLinkFollowAPattern(webPatterns, page.getLink())) {
						log ("Processing URL: " + page.getLink());
						File cacheFile = new File("google/" + page.getCache()); // temporary solution
						if (cacheFile.exists() && !cacheFile.isDirectory()) {
							String cacheData = FileUtils.readFileToString(cacheFile);
							cacheData = StringEscapeUtils.unescapeHtml(cacheData)
									.toLowerCase()				// convert to lower case
									.replaceAll("\u00A0", " "); // replace all long spaces with simple space
							
							final char[] data = FuzzySearch.stringToCharArray(cacheData);
							
							for (Map.Entry<String, Set<Long>> entry : nodes.entrySet()) {
								if (FuzzySearch.find(
										FuzzySearch.stringToCharArray(entry.getKey()), 
											data, 
											getDistance(entry.getKey().length())) >= 0) {
									
									log ("Found matching URL: " + page.getLink() + " for needle: " + entry.getKey());
									logFuzzy(entry.getKey());
									
									RestNode nodeResearcher = findWebResearcher(indexWebResearcher, page.getLink());
									if (null != nodeResearcher)
										for (Long nodeId : entry.getValue()) {
											RestNode nodePublication = graphDb.getNodeById(nodeId);
										
											Neo4jUtils.createUniqueRelationship(graphDb, nodePublication, nodeResearcher, 
													RelTypes.relatedTo, Direction.OUTGOING, pars);
										}
								}
							}
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
		
	}	

	private void log(String message) {
		System.out.println(message);
		
		logger.println(message);  	
	}
	
	private void logFuzzy(String needle) {
		fuzzy.println(needle);  	
	}
}
