package org.rdswitchboard.linkers.neo4j.web.researcher;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Semaphore;
import java.util.regex.Pattern;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexHits;
import org.rdswitchboard.libraries.graph.GraphUtils;
import org.rdswitchboard.libraries.neo4j.Neo4jException;
import org.rdswitchboard.libraries.neo4j.Neo4jUtils;
import org.rdswitchboard.utils.google.cache2.Link;
import org.rdswitchboard.utils.google.cache2.Result;
import org.rdswitchboard.utils.google.cache2.GoogleUtils;

public class Linker {	
	private static final String FIELD_PATTERN = "pattern";
	private static final String FIELD_ID = "id";
	private static final String FIELD_TITLE = "title";
	
	private static final String CACHE_PUBLICATION = "publication";
	private static final String CACHE_GRANT = "grant";
	
	// for Dryad titles
	private static final String PART_DATA_FROM = "data from:";
	
	private GraphDatabaseService graphDb;
	private Index<Node> indexWeb;
	private Label labelWeb = DynamicLabel.label( GraphUtils.SOURCE_WEB );
	private Label labelResearcher = DynamicLabel.label( GraphUtils.TYPE_RESEARCHER );
	private RelationshipType relRelatedTo = DynamicRelationshipType.withName( GraphUtils.RELATIONSHIP_RELATED_TO );
	
	private boolean verbose = false;
	
	private Set<String> blackList;
	private List<Pattern> webPatterns;
	
	private JAXBContext jaxbContext;
	private Unmarshaller jaxbUnmarshaller;
	
	private final int minTitleLength;
	private int maxThreads;
	
	public Linker(final String neo4jFolder, final String blackList, final int minTitleLength, boolean verbose) throws FileNotFoundException, IOException, JAXBException, Neo4jException {
		this.minTitleLength = minTitleLength;
		this.verbose = verbose;
		
		jaxbContext = JAXBContext.newInstance(Link.class, Result.class);
		jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		
		graphDb = Neo4jUtils.getGraphDb( neo4jFolder );
		
		try ( Transaction tx = graphDb.beginTx() ) {
			indexWeb = Neo4jUtils.getNodeIndex(graphDb, GraphUtils.SOURCE_WEB);
		}
		
		this.blackList = GoogleUtils.loadBlackList( blackList );
		loadWebPatterns();
	}
	
	public int getMaxThreads() {
		return maxThreads;
	}

	public void setMaxThreads(int maxThreads) {
		this.maxThreads = maxThreads;
	}

	public boolean isVerbose() {
		return verbose;
	}

	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}
	
	public void link(String googleCache) throws Exception {
		Map<String, Set<Long>> nodes;
		
		nodes = new HashMap<String, Set<Long>>();
		
		if (verbose)
			System.out.println("Processing ANDS:Grant");
		
		loadNodes( nodes, GraphUtils.SOURCE_ANDS, GraphUtils.TYPE_GRANT, 
				GraphUtils.PROPERTY_TITLE, filterHas(GraphUtils.PROPERTY_PURL) );
		
		if (verbose)
			System.out.println("Done. Loaded " + nodes.size() + " Nodes\nProcessing Nodes");
				
		try ( Transaction tx = graphDb.beginTx() ) {
			linkCached(nodes, googleCache, CACHE_GRANT);
			
			tx.success();
		}
			
		
		/*if (verbose)
			System.out.println("Processing Dryad:Publication");
		
		nodes = new HashMap<String, Set<Long>>();
		loadNodes( nodes, GraphUtils.SOURCE_CROSSREF, GraphUtils.TYPE_PUBLICATION, 
				GraphUtils.PROPERTY_TITLE, null );
		
		if (verbose)
			System.out.println("Done. Loaded " + nodes.size() + " Nodes\nProcessing Nodes");
				
		try ( Transaction tx = graphDb.beginTx() ) {
			linkCached(nodes, googleCache, CACHE_PUBLICATION);
			
			tx.success();
		}*/
		
		
		if (verbose)
			System.out.println("Processing Simple Search");
		
		nodes = new HashMap<String, Set<Long>>();
		
	/*	loadNodes( nodes, GraphUtils.SOURCE_ANDS, GraphUtils.TYPE_GRANT, 
				GraphUtils.PROPERTY_TITLE, null );
		loadNodes( nodes, GraphUtils.SOURCE_ANDS, GraphUtils.TYPE_DATASET, 
				GraphUtils.PROPERTY_TITLE, null );
		loadNodes( nodes, GraphUtils.SOURCE_DRYAD, GraphUtils.TYPE_DATASET, 
				GraphUtils.PROPERTY_TITLE, null );
		loadNodes( nodes, GraphUtils.SOURCE_CROSSREF, GraphUtils.TYPE_PUBLICATION, 
				GraphUtils.PROPERTY_TITLE, null );
		loadNodes( nodes, GraphUtils.SOURCE_CERN, GraphUtils.TYPE_PUBLICATION, 
				GraphUtils.PROPERTY_TITLE, null );
		loadNodes( nodes, GraphUtils.SOURCE_DARA, GraphUtils.TYPE_PUBLICATION, 
				GraphUtils.PROPERTY_TITLE, null );
		loadNodes( nodes, GraphUtils.SOURCE_DARA, GraphUtils.TYPE_DATASET, 
				GraphUtils.PROPERTY_TITLE, null );
		loadNodes( nodes, GraphUtils.SOURCE_OPEN_AIRE, GraphUtils.TYPE_DATASET, 
				GraphUtils.PROPERTY_TITLE, null );
		loadNodes( nodes, GraphUtils.SOURCE_OPEN_AIRE, GraphUtils.TYPE_PUBLICATION, 
				GraphUtils.PROPERTY_TITLE, null );*/
		loadNodes( nodes, GraphUtils.SOURCE_ORCID, GraphUtils.TYPE_PUBLICATION, 
				GraphUtils.PROPERTY_TITLE, null );
		loadNodes( nodes, GraphUtils.SOURCE_DARA, GraphUtils.TYPE_PUBLICATION, 
				GraphUtils.PROPERTY_TITLE, null );
		loadNodes( nodes, GraphUtils.SOURCE_DARA, GraphUtils.TYPE_DATASET, 
				GraphUtils.PROPERTY_TITLE, null );
		
		linkSimpleSearch(nodes, googleCache);
		
		
		if (verbose)
			System.out.println("Processing Fuzzu Search");
		
		nodes = new HashMap<String, Set<Long>>();
		
		loadNodes( nodes, GraphUtils.SOURCE_ANDS, GraphUtils.TYPE_GRANT, 
				GraphUtils.PROPERTY_TITLE, null );
		loadNodes( nodes, GraphUtils.SOURCE_ANDS, GraphUtils.TYPE_DATASET, 
				GraphUtils.PROPERTY_TITLE, null );
		loadNodes( nodes, GraphUtils.SOURCE_DRYAD, GraphUtils.TYPE_DATASET, 
				GraphUtils.PROPERTY_TITLE, null );
		loadNodes( nodes, GraphUtils.SOURCE_CROSSREF, GraphUtils.TYPE_PUBLICATION, 
				GraphUtils.PROPERTY_TITLE, null );
		loadNodes( nodes, GraphUtils.SOURCE_CERN, GraphUtils.TYPE_PUBLICATION, 
				GraphUtils.PROPERTY_TITLE, null );
		loadNodes( nodes, GraphUtils.SOURCE_DLI, GraphUtils.TYPE_DATASET, 
				GraphUtils.PROPERTY_TITLE, null );
		loadNodes( nodes, GraphUtils.SOURCE_DLI, GraphUtils.TYPE_PUBLICATION, 
				GraphUtils.PROPERTY_TITLE, null );
		
		linkFuzzySearch(nodes, googleCache);
		
		// add publication linking here
	}
	
	private void loadWebPatterns() {
		if (verbose)
			System.out.println("Loading Web Patterns");
		
        this.webPatterns = new ArrayList<Pattern>();

        String cypher = "MATCH (n:" + GraphUtils.SOURCE_WEB 
	    		  + ":" + GraphUtils.TYPE_PATTERN 
		          + ") WHERE HAS(n." + GraphUtils.PROPERTY_PATTERN 
		          + ") RETURN n." + GraphUtils.PROPERTY_PATTERN 
		          + " AS " + FIELD_PATTERN;
        
        try ( Transaction ignored = graphDb.beginTx();
        	org.neo4j.graphdb.Result result = graphDb.execute( cypher ) ) {
    	    while ( result.hasNext() ) {
    	        Map<String,Object> row = result.next();
    	        String pattern = ((String) row.get(FIELD_PATTERN));
    	        if (verbose) 
    	        	System.out.println("Pattern: " + pattern);
    	        
    	        if (null != pattern) 
	   	        	this.webPatterns.add(Pattern.compile(pattern));
    	    }
    	}    
	}
	
	private void loadNodes(Map<String, Set<Long>> nodes, String source, String type, 
			String fieldTitle, String filter) {

		if (verbose)
			System.out.println("Source: " + source + ", Type: " + type + ", Field: " + fieldTitle + ", Filter: " + filter);
		
		String cypher = "MATCH (n:" + source + ":" + type + ")";
		if (null != filter)
			cypher += " WHERE " + filter;
		cypher += " RETURN ID(n) AS " + FIELD_ID + ", n." + fieldTitle + " AS " + FIELD_TITLE;
				
		try ( Transaction ignored = graphDb.beginTx();
			org.neo4j.graphdb.Result result = graphDb.execute( cypher ) ) {
    	    while ( result.hasNext() ) {
    	        Map<String,Object> row = result.next();
    	        long nodeId = (Long) row.get(FIELD_ID);
    	        Object titles = row.get(FIELD_TITLE);
    	        if (null != titles) {
    	        	if (titles instanceof String)
    	        		putUnique(nodes, (String) titles, nodeId);
    	        	else if (titles instanceof String[])
    	        		for (String title : (String[]) titles)
    	        			putUnique(nodes, title, nodeId);
    	        }
    	    }
    	}    		
	}
	
	private void linkCached(Map<String, Set<Long>> nodes, String googleCache, String folderName) throws Exception {
		if (verbose)
			System.out.println("Processing cached pages: " + folderName);
		
		File linksFolder = GoogleUtils.getLinkFolder(googleCache);
		File cacheFolder = GoogleUtils.getResultFolder(googleCache);
		File metadataFolder = GoogleUtils.getMetadataFolder(googleCache);
		File[] files = cacheFolder.listFiles();
		for (File file : files) 
			if (!file.isDirectory()) {
				/*if (verbose)
					System.out.println("Processing file: " + file.toString());*/
				
				Result result = (Result) jaxbUnmarshaller.unmarshal(file);
				if (result != null) {
					String text = result.getText();
					if (verbose)
						System.out.println("Searching for a string: " + text);
					Set<Long> nodeIds = nodes.get(text.trim().toLowerCase());
					if (null != nodeIds) { 
						if (verbose)
							System.out.println("Found " + nodeIds.size() + " possible matches");

						for (String l : result.getLinks()) {
							Link link = (Link) jaxbUnmarshaller.unmarshal(new File(linksFolder, l));
							if (null != link) {
								
								if (verbose)
									System.out.println("Testing link: " + link.getLink());
								if (isLinkFollowAPattern(link.getLink())) {
									if (verbose)
										System.out.println("Found matching URL: " + link.getLink() + " for grant: " + text);
								
									Node nodeResearcher = getOrCreateWebResearcher(link, metadataFolder);
									for (Long nodeId : nodeIds) 
										Neo4jUtils.createUniqueRelationship(graphDb.getNodeById(nodeId), 
												nodeResearcher, relRelatedTo, Direction.OUTGOING, null);	
								}
							}
						}
					}
				} else
					throw new Exception("Unable to parse a file: " + file.toString());
			}
	}	
	
	private void linkSimpleSearch(Map<String, Set<Long>> nodes, String googleCache) throws Exception {
		if (verbose)
			System.out.println("Processing Simple Search");
		
		Semaphore semaphore = new Semaphore(maxThreads);
		
		List<MatcherThread> threads = new ArrayList<MatcherThread>();
		for (int i = 0; i < maxThreads; ++i) {
			MatcherThread thread = new MatcherThread(semaphore);
			thread.start();
			threads.add(thread);
		}

		int counter = 0;
		
		Transaction tx = graphDb.beginTx();
		try {
			File linksFolder = GoogleUtils.getLinkFolder(googleCache);
			File metadataFolder = GoogleUtils.getMetadataFolder(googleCache);
			File[] files = linksFolder.listFiles();
			for (File file : files) 
				if (!file.isDirectory()) {
					/*if (verbose)
						System.out.println("Processing file: " + file.toString());*/
					
					Link link = (Link) jaxbUnmarshaller.unmarshal(file);
					if (link != null && isLinkFollowAPattern(link.getLink())) {
						if (verbose)
							System.out.println("Testing link: " + link.getLink());
							
						MatcherSimple matcher = new MatcherSimple(googleCache, link, nodes);

						semaphore.acquire(); 
						
						boolean matcherAssigned = false;
						for (MatcherThread thread : threads) 
							if (thread.isFree()) {
								
								counter += processResult(thread.getResult(), metadataFolder);
								if (counter >= 1000) {
									tx.success();
									tx.close();
									
									tx = graphDb.beginTx();
									
									counter = 0;
								}
								
								thread.addMatcher(matcher);
								matcherAssigned = true;
								
								break;
							}
						
						if (!matcherAssigned)
							throw new MatcherThreadException("All matcher threads are busy");
					}
				}
			
			for (MatcherThread thread : threads) {
				thread.finishCurrentAndExit();
				
				processResult(thread.getResult(), metadataFolder);
				
				thread.join();
			}
			
			tx.success();
		} finally {
			tx.close();
		}
	}
	
	private void linkFuzzySearch(Map<String, Set<Long>> nodes, String googleCache) throws Exception {
		if (verbose)
			System.out.println("Processing Fuzzy Search");
		
		Semaphore semaphore = new Semaphore(maxThreads);
		
		List<MatcherThread> threads = new ArrayList<MatcherThread>();
		for (int i = 0; i < maxThreads; ++i) {
			MatcherThread thread = new MatcherThread(semaphore);
			thread.start();
			threads.add(thread);
		}
		
		int counter = 0;
		
		Transaction tx = graphDb.beginTx();
		try {
			File linksFolder = GoogleUtils.getLinkFolder(googleCache);
			File metadataFolder = GoogleUtils.getMetadataFolder(googleCache);
			File[] files = linksFolder.listFiles();
			for (File file : files) 
				if (!file.isDirectory()) {
					/*if (verbose)
						System.out.println("Processing file: " + file.toString());*/
					
					Link link = (Link) jaxbUnmarshaller.unmarshal(file);
					if (link != null && isLinkFollowAPattern(link.getLink())) {
						if (verbose)
							System.out.println("Testing link: " + link.getLink());
							
						Matcher matcher = new MatcherFuzzy(googleCache, link, nodes);

						semaphore.acquire(); 
						
						boolean matcherAssigned = false;
						for (MatcherThread thread : threads) 
							if (thread.isFree()) {
								counter += processResult(thread.getResult(), metadataFolder); 
								if (counter >= 1000) {
									tx.success();
									tx.close();
									
									tx = graphDb.beginTx();
									
									counter = 0;
								}									
								
								thread.addMatcher(matcher);
								matcherAssigned = true;
								
								break;
							}
						
						if (!matcherAssigned)
							throw new MatcherThreadException("All matcher threads are busy");
					}
				}
			for (MatcherThread thread : threads) {
				processResult(thread.getResult(), metadataFolder);
				
				thread.finishCurrentAndExit();
				thread.join();
			}	
			
			tx.success();
		} finally {
			tx.close();
		}
	}
	
	private int processResult(MatcherResult result, File metadataFolder) {
		int counter = 0;
		if (null != result) {
			if (verbose)
				System.out.println("Found " + result.getNodes().size() + " macthing texts in: " + result.getLink().getLink());

			try {
				Node nodeResearcher = getOrCreateWebResearcher(result.getLink(), metadataFolder);
				for (Long nodeId : result.getNodes()) {
					Neo4jUtils.createUniqueRelationship(graphDb.getNodeById(nodeId), 
							nodeResearcher, relRelatedTo, Direction.OUTGOING, null);
					++counter;
				}
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		return counter;
	}
	
	private void putUnique(Map<String, Set<Long>> nodes, String key, Long id) {
    	key = key.trim().toLowerCase();
    	if (key.startsWith(PART_DATA_FROM))
    		key = key.substring(PART_DATA_FROM.length()).trim();
   	 	if (key.length() > minTitleLength && !blackList.contains(key)) { 
			Set<Long> set = nodes.get(key);
			if (null == set) 
				nodes.put(key, set = new HashSet<Long>());
			set.add(id);
   	 	}
	}
	
	private String filterHas(String field) {
		return  "HAS (n." + field + ")";
	}
	
	private boolean isLinkFollowAPattern(String link) {
        for (Pattern pattern : webPatterns) 
                if (pattern.matcher(link).find())
                        return true;
        return false;
    }
	
	private Node findWebResearcher(String url) {
		IndexHits<Node> hits = indexWeb.get(GraphUtils.PROPERTY_KEY, url);
		if (null != hits && hits.hasNext())
			return hits.getSingle();
		
		return null;
	}
	
	private Node getOrCreateWebResearcher(Link link, File metadataFolder) throws Exception {
		String url = GraphUtils.extractFormalizedUrl(link.getLink());
		
		Node node = findWebResearcher(url);
		if (null != node) {
			if (!node.hasLabel(labelResearcher))
				throw new Exception("The node defined by URL: " + url + " is not a Researcher node");
			
			return node;
		}
			
		node = graphDb.createNode();
		node.setProperty(GraphUtils.PROPERTY_KEY, url);
		node.setProperty(GraphUtils.PROPERTY_SOURCE, GraphUtils.SOURCE_WEB);
		node.setProperty(GraphUtils.PROPERTY_TYPE, GraphUtils.TYPE_RESEARCHER);
		node.setProperty(GraphUtils.PROPERTY_URL, url);
		
		if (link.getMetadata() != null) {
			String author = GoogleUtils.getMetatag(new File(metadataFolder, link.getMetadata()), 
					GoogleUtils.METDATA_DC_TITLE);				
			if (null != author)
				node.setProperty(GraphUtils.PROPERTY_TITLE, author);
		}
		
		node.addLabel(labelWeb);
		node.addLabel(labelResearcher);
		
		indexWeb.add(node, GraphUtils.PROPERTY_KEY, url);

		return node;
	}
	
	/*private Map<String, Object> getPageMap(String link, String searchString) {
		QueryResponse response = googleQuery.queryCache(searchString);
		if (null != response) 
			for (Item item : response.getItems()) 
				if (item.getLink().equals(link)) 
					return item.getPagemap();

		return null;
	}*/
	
	/*private String getAuthor(String link, String searchString) {
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
						 
						 if (null != dcTitle) {
							 System.out.println("Found dc.title: " + dcTitle);	

							 return dcTitle;
						 } 
						 
						 if (null != citationAuthor) {
							 System.out.println("Found citation_author: " + citationAuthor);	
							 
							 return citationAuthor;
						 }
						 	
						 System.out.println("Unable to find author information in metatag");																 
					 }
				 }
			 }
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		 
		return null;
	}*/
}
