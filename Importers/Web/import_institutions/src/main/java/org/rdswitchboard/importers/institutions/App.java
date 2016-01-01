package org.rdswitchboard.importers.institutions;

import java.io.FileReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.parboiled.common.StringUtils;
import org.rdswitchboard.libraries.configuration.Configuration;
import org.rdswitchboard.libraries.graph.Graph;
import org.rdswitchboard.libraries.graph.GraphNode;
import org.rdswitchboard.libraries.graph.GraphSchema;
import org.rdswitchboard.libraries.graph.GraphUtils;
import org.rdswitchboard.libraries.neo4j.Neo4jDatabase;

import au.com.bytecode.opencsv.CSVReader;

/**
 * Main class for Web:Institution importer
 * 
 * This software design to process institutions.csv file located in the working directory
 * and will post data into Neo4J located at http://localhost:7474/db/data/
 * <p>
 * The institutions.csv fomat should be:
 * <br>
 * country,state,institution_name,institution_url
 * <p>
 * The first file line will be counted as header and will be ignored. The Institution host will be 
 * automatically extracted from an institution url
 * 
 * @author Dmitrij Kudriavcev, dmitrij@kudriavcev.info
 * @version 1.0.0
 */


public class App {

	private static final String INSTITUTIONS_SCV_FILE = "data/institutions.csv";
	
	/**
	 * Main class function
	 * @param args String[] Neo4J URL.
	 * If missing, the default parameters will be used.
	 */
	public static void main(String[] args) {
		try {
			Properties properties = Configuration.fromArgs(args);
	        
	        System.out.println("Importing Web Institutions");
	                
	        String neo4jFolder = properties.getProperty(Configuration.PROPERTY_NEO4J);
	        if (StringUtils.isEmpty(neo4jFolder))
	            throw new IllegalArgumentException("Neo4j Folder can not be empty");
	        System.out.println("Neo4j: " + neo4jFolder);
	        
	        String institutions = properties.getProperty(Configuration.PROPERTY_INSTITUTIONS, INSTITUTIONS_SCV_FILE);
	        if (StringUtils.isEmpty(institutions))
	            throw new IllegalArgumentException("Invalid path to Institutions CSV file");
	        
	        List<GraphSchema> schemas = new ArrayList<GraphSchema>();
	        schemas.add( new GraphSchema(GraphUtils.SOURCE_WEB, GraphUtils.PROPERTY_KEY, true));
	        
			Neo4jDatabase importer = new Neo4jDatabase(neo4jFolder);
			//importer.setVerbose(true);
			importer.importSchemas(schemas);
			
			Graph graph = importInstitutionsCsv(institutions);
			if (null == graph)
				return;
			
			importer.importGraph(graph);
			importer.printStatistics(System.out);
		} catch (Exception e) {
			e.printStackTrace();
			
			System.exit(1);
		}		
	}
	
	private static Graph importInstitutionsCsv(final String csv) {
		// Imoprt Grant data
		System.out.println("Importing file: " + csv);
					
		// process grats data file
		CSVReader reader;
		Graph graph = new Graph();
		try 
		{
			reader = new CSVReader(new FileReader(csv));
			String[] institution;
			boolean header = false;
			while ((institution = reader.readNext()) != null) 
			{
				if (!header)
				{
					header = true;
					continue;
				}
				if (institution.length != 4)
					continue;
						
				String country = institution[0];
				String state = institution[1];
				String title = institution[2];
				
				URL url = GraphUtils.toURL(institution[3]);
				String formalizedUrl = GraphUtils.extractFormalizedUrl(url);
				String host = GraphUtils.extractHost(url);
				
				if (null != host) {
				//	System.out.println("Institution: " + formalizedUrl + ", host: " + host);
		
					GraphNode node = new GraphNode()
						.withKey(GraphUtils.SOURCE_WEB, host)
						.withSource(GraphUtils.SOURCE_WEB)
						.withType(GraphUtils.TYPE_INSTITUTION)
						.withProperty(GraphUtils.PROPERTY_TITLE, title)
						.withProperty(GraphUtils.PROPERTY_URL, formalizedUrl)
						.withProperty(GraphUtils.PROPERTY_HOST, host);
							
					if (StringUtils.isNotEmpty(country))
						node.setProperty(GraphUtils.PROPERTY_COUNTRY, country);
					if (StringUtils.isNotEmpty(state))
						node.setProperty(GraphUtils.PROPERTY_STATE, state);
					
					graph.addNode(node);
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
