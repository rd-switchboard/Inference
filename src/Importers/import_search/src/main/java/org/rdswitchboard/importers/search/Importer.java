package org.rdswitchboard.importers.search;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
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

import org.rdswitchboard.utils.google.cache2.GoogleUtils;
import org.rdswitchboard.utils.google.cache2.Link;
import org.rdswitchboard.utils.graph.GraphConnection;
import org.rdswitchboard.utils.graph.GraphField;
import org.rdswitchboard.utils.graph.GraphSchema;
import org.rdswitchboard.utils.graph.GraphUtils;
import org.rdswitchboard.utils.aggrigation.AggrigationUtils;
import org.apache.commons.lang.StringUtils;
import org.neo4j.rest.graphdb.RestAPI;
import org.neo4j.rest.graphdb.RestAPIFacade;
import org.neo4j.rest.graphdb.query.RestCypherQueryEngine;
import org.neo4j.rest.graphdb.util.QueryResult;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

public class Importer {
	private RestAPI graphDb;
	private RestCypherQueryEngine engine;
/*	private RestIndex<Node> indexWebResearcher;*/
	
	private JAXBContext jaxbContext;
	private Unmarshaller jaxbUnmarshaller;
		
	private File fieldsFolder;
	private File googleLinkFolder;
	private File googleDataFolder;
	private File googleMetadataFolder;
	private File schemaFolder;
	private File nodesFolder;
	private File relationshipsFolder;
	
	private PrintWriter logger;
	
	private int fileCounter;

/*	private enum Labels implements Label {
		Web, Researcher //, Dryad, RDA, , Pattern, Publication, Grant
	}
	
	private enum RelTypes implements RelationshipType {
		relatedTo
	}*/
	
	private static final String LABEL_PATTERN = "Pattern";

	private static final int MAX_THREADS = 100;
	private static final int MIN_TITLE_LENGTH = 20;
//	private static final int MAX_COMMANDS = 1024;
//	private static final int MAX_NODES = 51200;
	
	private List<Pattern> webPatterns;
	private Set<String> blackList;
	
	
	/**
	 * Class Constructor
	 * 
	 * @param neo4jUrl
	 * @throws JAXBException
	 * @throws IOException 
	 */
	public Importer(String neo4jUrl, String fieldsPath, String googlePath, String cachePath, String blackList) throws JAXBException, IOException {
		System.setProperty("org.neo4j.rest.read_timeout", "600");
		
		graphDb = new RestAPIFacade(neo4jUrl);
		engine = new RestCypherQueryEngine(graphDb);  
		
		File folderInput = new File(fieldsPath);
		File folderOutput = new File(cachePath);
		File folderGoogle = new File(googlePath);
		
		fieldsFolder = GraphUtils.getFieldFolder(folderInput); 
		
		schemaFolder = GraphUtils.getSchemaFolder(folderOutput);
		nodesFolder = GraphUtils.getNodeFolder(folderOutput);
		relationshipsFolder = GraphUtils.getRelationshipFolder(folderOutput);

		schemaFolder.mkdirs();
		nodesFolder.mkdirs();
		relationshipsFolder.mkdirs();
		
		googleLinkFolder = GoogleUtils.getLinkFolder(folderGoogle);
		googleDataFolder = GoogleUtils.getDataFolder(folderGoogle);
		googleMetadataFolder = GoogleUtils.getMetadataFolder(folderGoogle);
				
		jaxbContext = JAXBContext.newInstance(Link.class);
		jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		
		logger = new PrintWriter("import_search.log", StandardCharsets.UTF_8.name());
					
		log("Loading black list");
		this.blackList = loadBlackList(blackList);

		log("Loading web patterns");
		this.webPatterns = loadWebPatterns(engine);
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
	
	/**
	 * @throws JsonMappingException 
	 * @throws JsonParseException 
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 * 
	 */
	public void process() throws JsonParseException, JsonMappingException, IOException {
		Map<String, List<GraphConnection>> nodes = new HashMap<String, List<GraphConnection>>();
		
		GraphSchema graphSchema = new GraphSchema();
		graphSchema.addIndex(AggrigationUtils.LABEL_WEB, AggrigationUtils.LABEL_RESEARCHER);
		
		log ("Loading Orcid Works");
		File[] fieldFiles = fieldsFolder.listFiles();
		for (File fieldFile : fieldFiles) 
			if (!fieldFile.isDirectory()) { 
			//	System.out.println("Processing fields: " + fieldFile.getName());
				
				GraphField[] graphfields = GoogleUtils.mapper.readValue(fieldFile, GraphField[].class);
				for (GraphField graphField : graphfields) {
					Object field = graphField.getField();
					if (field instanceof String) {
						String title = ((String)field).trim().toLowerCase();
						
						if (title.length() > MIN_TITLE_LENGTH 
								&& StringUtils.countMatches(title, " ") > 2 
								&& !blackList.contains(title)) {

							GraphConnection connection = graphField.getConn();
							graphSchema.addIndex(graphField.getConn());
							
							if (nodes.containsKey(title))
								nodes.get(title).add(connection);
							else {
								List<GraphConnection> list = new ArrayList<GraphConnection>();
								list.add(connection);
								
								nodes.put(title, list);
							}
						}
					}
				}
			}
		
		if (nodes.isEmpty()) {
			System.out.println("Error no valid nodes has been loaded");
		}
		
		log("Done. " + nodes.size() + " unique works has been loaded");
		
		
		log("Recordin database schema");
		GoogleUtils.mapper.writeValue(new File(schemaFolder, GraphUtils.GRAPH_SCHEMA), graphSchema);

		log("Processing cached pages");
		
		fileCounter = 0;
		long beginTime = System.currentTimeMillis();
		
		processPages(nodes);
				
		long endTime = System.currentTimeMillis();
		
		log(String.format("Done. Processed %d pages over %d ms. Average %f ms per file", 
				fileCounter, endTime - beginTime, (float)(endTime - beginTime) / (float)fileCounter));
	
	}
	
	private boolean isLinkFollowAPattern(List<Pattern> webPatterns, String link) {
        for (Pattern pattern : webPatterns) 
                if (pattern.matcher(link).find())
                        return true;
        return false;
    }
		
	private void processPages(Map<String, List<GraphConnection>> nodes) {

		try {
			Semaphore semaphore = new Semaphore(MAX_THREADS);
		
			List<MatcherThread> threads = new ArrayList<MatcherThread>();
			for (int i = 0; i < MAX_THREADS; ++i) {
				MatcherThread thread = new MatcherThread(semaphore);
				thread.start();
				threads.add(thread);
			}
		
			File[] files = googleLinkFolder.listFiles();
			for (File file : files) 
				if (!file.isDirectory()) {
					Link link = (Link) jaxbUnmarshaller.unmarshal(file);
					if (link != null && isLinkFollowAPattern(webPatterns, link.getLink())) {
						String fileName = Long.toString(fileCounter) + ".json";

						log ("Processing URL [" + fileCounter++ + "]: " + link.getLink());
						
						Matcher matcher = new Matcher(
								link.getLink(), 
								new File(googleDataFolder, link.getData()), 
								link.getMetadata() != null ? new File(googleMetadataFolder, link.getMetadata()) : null, 
								new File(nodesFolder, fileName), 
								new File(relationshipsFolder, fileName),
								nodes);
						
						semaphore.acquire(); 

						boolean matcherAssigned = false;
						for (MatcherThread thread : threads) 
							if (thread.isFree()) {
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
				thread.join();
			}			
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}

	private void log(String message) {
		System.out.println(message);
		
		logger.println(message);  	
	}
}
