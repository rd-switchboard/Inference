package org.rdswitchboard.importers.patterns;

import java.io.FileReader;
import java.util.Properties;

import org.parboiled.common.StringUtils;
import org.rdswitchboard.libraries.configuration.Configuration;
import org.rdswitchboard.libraries.graph.Graph;
import org.rdswitchboard.libraries.graph.GraphNode;
import org.rdswitchboard.libraries.graph.GraphRelationship;
import org.rdswitchboard.libraries.graph.GraphSchema;
import org.rdswitchboard.libraries.graph.GraphUtils;
import org.rdswitchboard.libraries.neo4j.Neo4jDatabase;

import au.com.bytecode.opencsv.CSVReader;

public class App {
	private static final String PATTERNS_SCV_FILE = "data/patterns.csv";
	
	/**
	 * Main class function
	 * @param args String[] Neo4J URL.
	 * If missing, the default parameters will be used.
	 */
	public static void main(String[] args) {
		try {
			Properties properties = Configuration.fromArgs(args);
	        
	        System.out.println("Importing Web Patterns");
	                
	        String neo4jFolder = properties.getProperty(Configuration.PROPERTY_NEO4J);
	        if (StringUtils.isEmpty(neo4jFolder))
	            throw new IllegalArgumentException("Neo4j Folder can not be empty");
	        System.out.println("Neo4j: " + neo4jFolder);
	        
	        String patterns = properties.getProperty(Configuration.PROPERTY_PATTERNS, PATTERNS_SCV_FILE);
	        if (StringUtils.isEmpty(patterns))
	            throw new IllegalArgumentException("Invalid path to Patterns CSV file");
	        
	        Neo4jDatabase importer = new Neo4jDatabase(neo4jFolder);
			importer.setVerbose(true);
			importer.importSchema( new GraphSchema(GraphUtils.SOURCE_WEB, GraphUtils.PROPERTY_KEY, true) );
			
			Graph graph = importPatternsCsv(patterns);
			if (null == graph)
				return;
			
			importer.importGraph(graph);
			importer.printStatistics(System.out);
		} catch (Exception e) {
			e.printStackTrace();
			
			System.exit(1);
		}		
	}
	
	private static Graph importPatternsCsv(final String csv) {
		// Imoprt Grant data
		System.out.println("Importing file: " + csv);
					
		// process grats data file
		CSVReader reader;
		Graph graph = new Graph();
		try 
		{
			reader = new CSVReader(new FileReader(csv));
			String[] pattern;
			boolean header = false;
			while ((pattern = reader.readNext()) != null) 
			{
				if (!header)
				{
					header = true;
					continue;
				}
				if (pattern.length != 2)
					continue;
						
				String url = pattern[0];
				String pat = pattern[1];
				
				System.out.println("url: " + pat);
				
				String host = GraphUtils.extractHost(url);				
				if (null != host) {
					String key = "pattern:"+host+":"+pat;
					
					graph.addNode(new GraphNode()
						.withKey(GraphUtils.SOURCE_WEB, key)
						.withSource(GraphUtils.SOURCE_WEB)
						.withType(GraphUtils.TYPE_PATTERN)
						.withProperty(GraphUtils.PROPERTY_PATTERN, pat)
						.withProperty(GraphUtils.PROPERTY_HOST, host));
					
					graph.addRelationship(new GraphRelationship()
						.withRelationship(GraphUtils.RELATIONSHIP_PATTERN)
						.withStart(GraphUtils.SOURCE_WEB, host)
						.withEnd(GraphUtils.SOURCE_WEB, key));
				}
			}
				
			reader.close();			
		} catch (Exception e) {
			e.printStackTrace();
			
			return null;
		} 
				
		return graph;
	}
}
