package org.rdswitchboard.exporters.graph;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.Label;
import org.parboiled.common.StringUtils;
import org.rdswitchboard.exporters.graph.Exporter;
import org.rdswitchboard.libraries.graph.GraphUtils;


/**
 * Main class for Nexus RDA records exported
 * 
 * Parameters: 
 * 	 $1 : Neor4j main folder (neo4j)
 *   $2 : Output folder (rda/json)
 *   $3 : Export level (3)
 *   $4 : Maximum nodes per file (100)
 *   $5 : Maximum new siblings per node (10)
 * 
 * Installation:
 * 
 * To compile project, please execute: 
 * $ mvn package
 * 
 * To run, please copy program jar file and whole jars directory from target into main project folder and run
 * $ java -jar <program name>.jar
 * 
 * @version 2.0.0
 * @author Dima Kudriavcev (dmitrij@kudriavcev.info)
 * @date 24 May 2015 	
 *
 */

public class App {
	//private static final String SOURCES_NAME = "conf/rds_sources.csv";
	
	private static void loadProperties(Properties properties, String propertiesFile) throws IOException {
		try (InputStream in = new FileInputStream(propertiesFile)) {
			properties.load(in);
		}
	}
	
	public static void main(String[] args) {
		try {
			Properties properties = new Properties();
			loadProperties(properties, "properties/export_graph_json.properties");
			
			String sourceNeo4jFolder = properties.getProperty("neo4j", "neo4j");
			
			String s3Bucket = properties.getProperty("s3.bucket");
			String s3Key = properties.getProperty("s3.key");
			boolean s3Public = Boolean.parseBoolean(properties.getProperty("s3.public", "false"));

		/*	String sourcesFile = properties.getProperty("sources", SOURCES_NAME);
		    if (StringUtils.isEmpty(sourcesFile))
		    	throw new IllegalArgumentException("Path to a list of valid sources can not be empty");
		    System.out.println("Sources: " + sourcesFile);*/
		
			int maxLevel = Integer.parseInt(properties.getProperty("max.level", "2"));
			int maxNodes = Integer.parseInt(properties.getProperty("max.nodes", "100"));
			int maxSiblings = Integer.parseInt(properties.getProperty("max.siblings", "10"));

			int testNodeId = Integer.parseInt(properties.getProperty("test.node.id", "0"));
			
		    
			
			if (StringUtils.isEmpty(s3Bucket)) {
				System.out.println("S3 Bucket name can not be empty");
				
				return;
			}

			if (StringUtils.isEmpty(s3Key)) {
				System.out.println("S3 Key prefix can not be empty");
				
				return;
			}
			

	 /*       Map<Label, String> malLabels = new HashMap<Label, String>();
	        
	      /*  List<String> lines = Files.readAllLines(FileSystems.getDefault().getPath(sourcesFile), StandardCharsets.UTF_8);
	        for (String line : lines) {
	        	String[] parts = line.split(",");
	        	String source = parts[0].trim();
	        	String property = parts.length > 1 ? parts[1].trim() : null; 
	        	malLabels.put(DynamicLabel.label(source), property);
	        }*/
			
			Label labelType = DynamicLabel.label(GraphUtils.TYPE_DATASET);
			
			Label labelAnds = DynamicLabel.label(GraphUtils.SOURCE_ANDS);
	        Label labelDryad = DynamicLabel.label(GraphUtils.SOURCE_DRYAD);
	        Label labelCrossref = DynamicLabel.label(GraphUtils.SOURCE_CROSSREF);
	        Label labelCern = DynamicLabel.label(GraphUtils.SOURCE_CERN);
	        Label labelOrcid = DynamicLabel.label(GraphUtils.SOURCE_ORCID);
	        Label labelWeb = DynamicLabel.label(GraphUtils.SOURCE_WEB);
	        Label labelDli = DynamicLabel.label(GraphUtils.SOURCE_DLI);
	        Label labelDara = DynamicLabel.label(GraphUtils.SOURCE_DARA);
	        
	        Map<Label, Label[]> sources = new HashMap<Label, Label[]>();
	        sources.put(labelAnds, null);
	        sources.put(labelDryad, new Label[] { labelCrossref });
	        sources.put(labelCern, new Label[] { labelAnds, labelDryad, labelCrossref, labelOrcid, labelWeb, labelDli, labelDara});
	        			
	       	Exporter expoter = new Exporter();
	       	expoter.setNeo4jFolder(sourceNeo4jFolder);
//	       	expoter.addLabel(DynamicLabel.label(GraphUtils.SOURCE_ANDS));
	       	expoter.setAwsInstanceProfileCredentials();
	       	
	       	expoter.setS3Bucket(s3Bucket);
	       	expoter.setS3Key(s3Key);
	       	expoter.enablePublicReadRights(s3Public);
	       	expoter.setMaxLevel(maxLevel);
	       	expoter.setMaxNodes(maxNodes);
	       	expoter.setMaxSiblings(maxSiblings);
	       	expoter.setTestNodeId(testNodeId);
	        expoter.process(labelType, sources);
	        
		} catch (Exception e) {
			e.printStackTrace();
		} 	
	}
}

