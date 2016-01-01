package org.rdswitchboard.exporters.keys;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.ResourceIterable;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.Transaction;
import org.neo4j.tooling.GlobalGraphOperations;
import org.rdswitchboard.libraries.graph.GraphUtils;
import org.rdswitchboard.libraries.neo4j.Neo4jUtils;
import org.rdswitchboard.libraries.neo4j.interfaces.ProcessNode;

public class App {
	private static final String PROPERTIES_FILE = "properties/export_keys.properties";
	private static final String NEXUS_NEO4J_FOLDER = "neo4j-nexus";
/*	private static final String SOURCES_NAME = "conf/rds_sources.csv";*/
	private static final String OUTPUT_NAME = "rds_keys.csv";
	
	public static void main(String[] args) {
		try {
			String propertiesFile = PROPERTIES_FILE;
			if (args.length > 0 && !StringUtils.isEmpty(args[0])) 
				propertiesFile = args[0];
		
		    Properties properties = new Properties();
		    try (InputStream in = new FileInputStream(propertiesFile)) {
		        properties.load(in);
		    }
		        	               	        
	        String neo4jFolder = properties.getProperty("neo4j", NEXUS_NEO4J_FOLDER);
	        if (StringUtils.isEmpty(neo4jFolder))
	            throw new IllegalArgumentException("Neo4j Folder can not be empty");
	        System.out.println("Neo4j Folder: " + neo4jFolder);

	        /*String sourcesFile = properties.getProperty("sources", SOURCES_NAME);
	        if (StringUtils.isEmpty(sourcesFile))
	            throw new IllegalArgumentException("Path to a list of valid sources can not be empty");
	        System.out.println("Sources: " + sourcesFile);*/

	        String outputName = properties.getProperty("out", OUTPUT_NAME);
	        if (StringUtils.isEmpty(outputName))
	            throw new IllegalArgumentException("Output name can not be empty");
	        System.out.println("Output: " + outputName);
	        
/*	        Map<Label, String> malLabels = new HashMap<Label, String>();
	        
	        List<String> lines = Files.readAllLines(FileSystems.getDefault().getPath(sourcesFile), StandardCharsets.UTF_8);
	        for (String line : lines) {
	        	String[] parts = line.split(",");
	        	String source = parts[0].trim();
	        	String property = parts.length > 1 ? parts[1].trim() : null; 
	        	malLabels.put(DynamicLabel.label(source), property);
	        }*/
	        
	        GraphDatabaseService graphDb = Neo4jUtils.getGraphDb(neo4jFolder);
	        Label labelAnds = DynamicLabel.label(GraphUtils.SOURCE_ANDS);
	        Label labelDryad = DynamicLabel.label(GraphUtils.SOURCE_DRYAD);
	        Label labelCrossref = DynamicLabel.label(GraphUtils.SOURCE_CROSSREF);
	        Label labelCern = DynamicLabel.label(GraphUtils.SOURCE_CERN);
	        Label labelOrcid = DynamicLabel.label(GraphUtils.SOURCE_ORCID);
	        Label labelWeb = DynamicLabel.label(GraphUtils.SOURCE_WEB);
	        Label labelDli = DynamicLabel.label(GraphUtils.SOURCE_DLI);
	        Label labelDara = DynamicLabel.label(GraphUtils.SOURCE_DARA);
	        	        
	        try (PrintWriter writer = new PrintWriter(outputName)) {
	        	
		        exportKeys(graphDb, labelAnds, null, writer);
		        exportKeys(graphDb, labelDryad, new Label[] { labelCrossref },  writer);
		        exportKeys(graphDb, labelCern, new Label[] { labelAnds, labelDryad, labelCrossref, labelOrcid, labelWeb, labelDli, labelDara},  writer);
		        
		        writer.flush();
	        }
	        
		} catch (Exception e) {
			e.printStackTrace();
			
			System.exit(1);
		}
	}
	
	private static boolean hasRelatedNodes(Node node, Label[] labels) {
		if (null == labels)
			return true;
		Iterable<Relationship> relationships = node.getRelationships();
		for (Relationship relationship : relationships) {
			Node other = relationship.getOtherNode(node);
			for (Label label : labels)
				if (other.hasLabel(label))
					return true;
		}
		
		return false;
	}
	
	private static void exportKeys(GraphDatabaseService graphDb, Label labelSource, Label[] labelsRel, 
			PrintWriter writer) throws Exception {
        long counter = 0;
		try (Transaction ignored = graphDb.beginTx()) {
        	try (ResourceIterator<Node> nodes = graphDb.findNodes(labelSource)) {
        		while (nodes.hasNext()) {
        			Node node = nodes.next();
        			if (node.hasProperty(GraphUtils.PROPERTY_ORIGINAL_KEY) 
        					&& hasRelatedNodes(node, labelsRel)) {
        				Object originalKeys = node.getProperty(GraphUtils.PROPERTY_ORIGINAL_KEY);
						if (originalKeys instanceof String) {
							writer.println((String) originalKeys);
							++counter;
						} else if (originalKeys instanceof String[]) 
							for (String originalKey : (String[]) originalKeys) {
								writer.println(originalKey);
								++counter;
							}
						else 
							throw new Exception("Invalid format of Original Keys property:" + originalKeys.getClass().getName());
        			}
        		}
        	}
        }
        
        System.out.println("Exported " + counter + " " + labelSource.name() + "'s keys");
	}
	
}
