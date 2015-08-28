package org.rdswitchboard.importers.services;

import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.parboiled.common.StringUtils;
import org.rdswitchboard.libraries.graph.Graph;
import org.rdswitchboard.libraries.graph.GraphNode;
import org.rdswitchboard.libraries.graph.GraphSchema;
import org.rdswitchboard.libraries.graph.GraphUtils;
import org.rdswitchboard.libraries.neo4j.Neo4jDatabase;

import au.com.bytecode.opencsv.CSVReader;

/**
 * Main class
 * @author Dmitrij Kudriavcev, dmitrij@kudriavcev.info
 *
 */
public abstract class App {
	private static final String PROPERTIES_FILE = "properties/import_services.properties";
	private static final String SERVICES_SCV_FILE = "data/services.csv";
	
	/**
	 * Main class function
	 * @param args String[] Neo4J URL.
	 * If missing, the default parameters will be used.
	 */
	public static void main(String[] args) {
		try {
			String propertiesFile = PROPERTIES_FILE;
	        if (args.length > 0 && !StringUtils.isEmpty(args[0])) 
	        	propertiesFile = args[0];
	
	        Properties properties = new Properties();
	        try (InputStream in = new FileInputStream(propertiesFile)) {
	            properties.load(in);
	        }
	        
	        System.out.println("Importing Web Services");
	                
	        String neo4jFolder = properties.getProperty("neo4j");
	        if (StringUtils.isEmpty(neo4jFolder))
	            throw new IllegalArgumentException("Neo4j Folder can not be empty");
	        System.out.println("Neo4j: " + neo4jFolder);
	        
	        String services = properties.getProperty("services", SERVICES_SCV_FILE);
	        if (StringUtils.isEmpty(services))
	            throw new IllegalArgumentException("Invalid path to Services CSV file");
	        
	        List<GraphSchema> schemas = new ArrayList<GraphSchema>();
	        schemas.add( new GraphSchema()
	        		.withLabel(GraphUtils.SOURCE_WEB)
	        		.withIndex(GraphUtils.PROPERTY_KEY)
	        		.withUnique(true));
	        
			Neo4jDatabase importer = new Neo4jDatabase(neo4jFolder);
			importer.setVerbose(true);
			importer.importSchemas(schemas);
			
			Graph graph = importServicesCsv(services);
			if (null == graph)
				return;
			
			importer.importNodes(graph.getNodes());
		} catch (Exception e) {
			e.printStackTrace();
		}		
	}
	
	private static Graph importServicesCsv(final String csv) {
		// Imoprt Grant data
		System.out.println("Importing file: " + csv);
					
		// process grats data file
		CSVReader reader;
		Graph graph = new Graph();
		try 
		{
			reader = new CSVReader(new FileReader(csv));
			String[] service;
			boolean header = false;
			while ((service = reader.readNext()) != null) 
			{
				if (!header)
				{
					header = true;
					continue;
				}
				if (service.length != 2)
					continue;
						
				String name =service[0];
				String url = service[1];
				
				System.out.println("url: " + url);
				
				if (null != url && !url.isEmpty()) {
					graph.addNode(new GraphNode()
						.withKey(GraphUtils.SOURCE_WEB, url)
						.withSource(GraphUtils.SOURCE_WEB)
						.withType(GraphUtils.TYPE_SERVICE)
						.withProperty(GraphUtils.PROPERTY_TITLE, name)
						.withProperty(GraphUtils.PROPERTY_URL, url));
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
