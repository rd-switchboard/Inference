package org.rdswitchboard.importers.crossref;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.Node;
import org.rdswitchboard.libraries.configuration.Configuration;
import org.rdswitchboard.libraries.graph.Graph;
import org.rdswitchboard.libraries.graph.GraphKey;
import org.rdswitchboard.libraries.graph.GraphNode;
import org.rdswitchboard.libraries.graph.GraphRelationship;
import org.rdswitchboard.libraries.graph.GraphSchema;
import org.rdswitchboard.libraries.graph.GraphUtils;
import org.rdswitchboard.libraries.neo4j.Neo4jDatabase;
import org.rdswitchboard.libraries.neo4j.interfaces.ProcessNode;

public class App {
	private static final String CROSSREF_FOLDER = "crossref/cahce";
	private static final String CROSSREF_VERSION_FILE = "crossref";
	private static final String DATE_FORMAT = "yyyy-MM-dd";
	
	private static CrossrefGraph crossref;
	private static Neo4jDatabase neo4j;
	private static int counter;
	
	private static final Map<String, Set<GraphKey>> references = new HashMap<String, Set<GraphKey>>();
	
	public static void main(String[] args) {
		try {
			Properties properties = Configuration.fromArgs(args);
	        
	        String neo4jFolder = properties.getProperty(Configuration.PROPERTY_NEO4J);
	        if (StringUtils.isEmpty(neo4jFolder))
	            throw new IllegalArgumentException("Neo4j Folder can not be empty");
	        System.out.println("Neo4J: " + neo4jFolder);
	     
	        String crossrefFolder = properties.getProperty(Configuration.PROPERTY_CROSSREF_CACHE, CROSSREF_FOLDER);
	        if (StringUtils.isEmpty(crossrefFolder))
	            throw new IllegalArgumentException("CrossRef Cache Folder can not be empty");
	        System.out.println("CrossRef: " + crossrefFolder);
	        
	        String versionFolder = properties.getProperty("versions");
	        if (StringUtils.isEmpty(versionFolder))
	            throw new IllegalArgumentException("Versions Folder can not be empty");
	        
	     /*   String sources = properties.getProperty("sources");
	        if (StringUtils.isNotEmpty(sources))
	            throw new IllegalArgumentException("Sources can not be empty");
	        System.out.println("Sources: " + crossrefFolder);*/
	        
	        crossref = new CrossrefGraph();
	        crossref.setCacheFolder(crossrefFolder);
	        
	        neo4j = new Neo4jDatabase(neo4jFolder);
	        List<GraphSchema> schemas = new ArrayList<GraphSchema>();
	        schemas.add(new GraphSchema(GraphUtils.SOURCE_CROSSREF, GraphUtils.PROPERTY_KEY, true));
	        schemas.add(new GraphSchema(GraphUtils.SOURCE_CROSSREF, GraphUtils.PROPERTY_DOI, false));
	        schemas.add(new GraphSchema(GraphUtils.SOURCE_CROSSREF, GraphUtils.PROPERTY_URL, false));
	        schemas.add(new GraphSchema(GraphUtils.SOURCE_CROSSREF, GraphUtils.PROPERTY_ISSN, false));
	        schemas.add(new GraphSchema(GraphUtils.SOURCE_CROSSREF, GraphUtils.PROPERTY_ORCID_ID, false));
	        //schemas.add(new 1GraphSchema(GraphUtils.SOURCE_WEB, GraphUtils.PROPERTY_KEY, true));
	        //schemas.add(new GraphSchema(GraphUtils.SOURCE_WEB, GraphUtils.PROPERTY_URL, false));
	        
	        neo4j.importSchemas(schemas);
	    //    neo4j.setVerbose(true);
	        
	   /*     if (null != sources) {
	        	String[] array = sources.split(",");
	        	for (String source : array) 
	        		process(source);
	        }*/
	        
	        loadReferences(GraphUtils.SOURCE_DRYAD, GraphUtils.PROPERTY_REFERENCED_BY);
	        loadReferences(GraphUtils.SOURCE_DARA, GraphUtils.PROPERTY_REFERENCED_BY);
	//        loadReferences(GraphUtils.SOURCE_ANDS, GraphUtils.PROPERTY_REFERENCED_BY);
	        processReferences(GraphUtils.RELATIONSHIP_RELATED_TO);
	        
	        loadReferences(GraphUtils.SOURCE_DARA, GraphUtils.PROPERTY_DOI);
//	        loadReferences(GraphUtils.SOURCE_ORCID, GraphUtils.PROPERTY_DOI);
//	        loadReferences(GraphUtils.SOURCE_CERN, GraphUtils.PROPERTY_DOI);
//	        loadReferences(GraphUtils.SOURCE_DLI, GraphUtils.PROPERTY_DOI);
	        loadReferences(GraphUtils.SOURCE_ANDS, GraphUtils.PROPERTY_DOI);
	        	        
	        processReferences(GraphUtils.RELATIONSHIP_KNOWN_AS);
	        
	        Files.write(Paths.get(versionFolder, CROSSREF_VERSION_FILE), 
	        		new SimpleDateFormat(DATE_FORMAT).format(new Date()).getBytes());
	        
		} catch (Exception e) {
            e.printStackTrace();
            
            System.exit(1);
		}       
	}
	
	private static void loadReferences(final String source, final String property) {
		System.out.println("Loading source: " + source + ", reference: " + property);
		int exists = references.size();
		counter = 0;
	
		try {
			neo4j.createIndex(DynamicLabel.label(source), property);
			neo4j.enumrateAllNodesWithLabelAndProperty(source, property, new ProcessNode() {
	
				@Override
				public boolean processNode(Node node) throws Exception {
					String keyValue = (String) node.getProperty(GraphUtils.PROPERTY_KEY);
					GraphKey key = new GraphKey(source, keyValue);
					Object dois = node.getProperty(property);
					if (dois instanceof String) {
						loadDoi(key, (String)dois); 	
					} else if (dois instanceof String[]) {
						for (String doi : (String[])dois)
							loadDoi(key, doi);
					}
					++counter;
										
					return true;
				}			
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		System.out.println("Done. Processed " + counter + " nodes and loaded " + (references.size() - exists) + " new DOI's");
	}
	
	private static void loadDoi(GraphKey key, String ref) {
		String doi = GraphUtils.extractDoi(ref);
		if (null != doi) {
			Set<GraphKey> ids = references.get(doi);
			if (null == ids) 
				references.put(doi, ids = new HashSet<GraphKey>());
			ids.add(key);					
		}
	}
	
	private static void processReferences(String relationships) {
		System.out.println("Processing " + references.size() + " unique DOI's");
		
		counter = 0;
		
		Graph graph = new Graph();
		for (Map.Entry<String, Set<GraphKey>> entry : references.entrySet()) {
			GraphNode node = crossref.queryGraph(graph, entry.getKey());
			if (null != node) {
				for (GraphKey key : entry.getValue()) {
					graph.addRelationship(new GraphRelationship() 
						.withRelationship(relationships)
						.withStart(key)
						.withEnd(node.getKey()));
				}
			}
			
			if (++counter % 1000 == 0)
				System.out.println("Processed " + counter + " DOI's");
						
			if (graph.getNodesCount() >= 10000 
					|| graph.getRelationshipsCount() >= 10000) {
				System.out.println("Importing data to the Neo4j");
				
				neo4j.importGraph(graph);
				graph = new Graph();
			}				
		}
		
		neo4j.importGraph(graph);
		
		references.clear();
	}
}
