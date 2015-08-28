package org.rdswitchboard.importers.crossref;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.rdswitchboard.libraries.graph.Graph;
import org.rdswitchboard.libraries.graph.GraphKey;
import org.rdswitchboard.libraries.graph.GraphNode;
import org.rdswitchboard.libraries.graph.GraphRelationship;
import org.rdswitchboard.libraries.graph.GraphSchema;
import org.rdswitchboard.libraries.graph.GraphUtils;
import org.rdswitchboard.libraries.neo4j.Neo4jDatabase;
import org.rdswitchboard.libraries.neo4j.interfaces.ProcessNode;

public class App {
	private static final String PROPERTIES_FILE = "properties/import_crossref.properties";
	private static final String NEO4J_FOLDER = "neo4j";
	private static final String CROSSREF_FOLDER = "crossref/cahce";
	
	private static CrossrefGraph crossref;
	private static Neo4jDatabase neo4j;
	
	private static final Map<String, Set<GraphKey>> references = new HashMap<String, Set<GraphKey>>();
	
	
	public static void main(String[] args) {
		try {
            String propertiesPath = PROPERTIES_FILE;
            if (args.length > 0 && StringUtils.isNotEmpty(args[0])) 
            	propertiesPath = args[0];

            Properties properties = new Properties();
            File propertiesFile = new File(propertiesPath);
            if (propertiesFile.exists() && propertiesFile.isFile()) {
		        try (InputStream in = new FileInputStream(propertiesFile)) {
		            properties.load(in);
		        }
            }
	        
	        String neo4jFolder = properties.getProperty("neo4j", NEO4J_FOLDER);
	        if (StringUtils.isEmpty(neo4jFolder))
	            throw new IllegalArgumentException("Neo4j Folder can not be empty");
	        System.out.println("Neo4J: " + neo4jFolder);
	     
	        String crossrefFolder = properties.getProperty("crossref", CROSSREF_FOLDER);
	        if (StringUtils.isEmpty(crossrefFolder))
	            throw new IllegalArgumentException("CrossRef Cache Folder can not be empty");
	        System.out.println("CrossRef: " + crossrefFolder);
	        
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
	        processReferences();
	     
		} catch (Exception e) {
            e.printStackTrace();
		}       
	}
	
	private static void loadReferences(final String source, final String property) {
		System.out.println("Loading source: " + source + ", reference: " + property);
	
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
					
					return true;
				}			
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
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
	
	private static void processReferences() {
		System.out.println("Processing references");
		
		Graph graph = new Graph();
		for (Map.Entry<String, Set<GraphKey>> entry : references.entrySet()) {
			GraphNode node = crossref.queryGraph(graph, entry.getKey());
			if (null != node) {
				for (GraphKey key : entry.getValue()) {
					graph.addRelationship(new GraphRelationship() 
						.withRelationship(GraphUtils.RELATIONSHIP_KNOWN_AS)
						.withStart(key)
						.withEnd(node.getKey()));
				}
			}
			
			if (graph.getNodesCount() >= 1000 
					|| graph.getRelationshipsCount() >= 1000) {
				neo4j.importGraph(graph);
				graph = new Graph();
			}				
		}
		
		neo4j.importGraph(graph);
	}
}
