package org.rdswitchboard.utils.neo4j.importers.url;

import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.util.Properties;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.index.IndexHits;
import org.neo4j.graphdb.index.ReadableIndex;
import org.parboiled.common.StringUtils;
import org.rdswitchboard.libraries.graph.GraphUtils;
import org.rdswitchboard.libraries.neo4j.Neo4jDatabase;

import au.com.bytecode.opencsv.CSVReader;

public class App {
	private static final String PROPERTIES_FILE = "properties/import_rds_url.properties";
	private static final String NEO4J_FOLDER = "neo4j";
	private static final String INPUT_NAME = "rds_urls.csv";
	private static final String REQUIRE_AUTOINDEX = "false";
	
	public static void main(String[] args) {
		try {
			String propertiesFile = PROPERTIES_FILE;
	        if (args.length > 0 && !StringUtils.isEmpty(args[0])) 
	        	propertiesFile = args[0];
	
	        Properties properties = new Properties();
	        try (InputStream in = new FileInputStream(propertiesFile)) {
	            properties.load(in);
	        }
	        
	        System.out.println("Importing RDS URL's");
	                
	        String neo4jFolder = properties.getProperty("neo4j", NEO4J_FOLDER);
	        if (StringUtils.isEmpty(neo4jFolder))
	            throw new IllegalArgumentException("Neo4j Folder can not be empty");
	        System.out.println("Neo4j: " + neo4jFolder);

	        String label = properties.getProperty("label");
	        
	        String inputName = properties.getProperty("input.name", INPUT_NAME);
	        if (StringUtils.isEmpty(inputName))
	            throw new IllegalArgumentException("Input file name can not be empty");


	        boolean requireAutoIndex = Boolean.parseBoolean(properties.getProperty("require_autoindex", REQUIRE_AUTOINDEX));
	        
	        Neo4jDatabase graphDb = new Neo4jDatabase(neo4jFolder);
	        ReadableIndex<Node> index;
	        
	        try ( Transaction tx = graphDb._beginTx() ) 
			{
	        	if (StringUtils.isEmpty(label)) {
	        		index = graphDb._getNodeAutoIndex(requireAutoIndex ? GraphUtils.PROPERTY_KEY : null);
	        	} else 
		        	index = graphDb._getNodeIndex(label);
	        	
	        	tx.success();
			}
	        
	        System.out.println("Processing CSV file");
	        long goodKeys = 0;
	        long badKeys = 0;
	        long unknownKeys  = 0;
	        
	    	CSVReader reader = new CSVReader(new FileReader(inputName));
	        try ( Transaction tx = graphDb._beginTx() ) 
			{
				String[] line;
				boolean header = false;
				while ((line = reader.readNext()) != null) 
				{
					if (!header)
					{
						header = true;
						continue;
					}
					if (line.length != 3)
						continue;
							
					String key = line[0];
					String slug = line[1];
					String id = line[2];
					
					String url = "rd-switchboard.net/" + slug + "/" + id;
					
					System.out.println("Processing key: " + key);
					
					IndexHits<Node> hits = index.get( GraphUtils.PROPERTY_KEY, key );
					Node node = null;
					int counter = 0;
					while (hits.hasNext()) {
						node = hits.next();
						++counter;
					}
					
					// only work with unique nodes
					if (counter == 0) {
						System.out.println("Unabele to find any node by this key");
						++unknownKeys;
					} else  if (1 == counter) {
						node.setProperty(GraphUtils.PROPERTY_RDS_URL, url);
						++goodKeys;
					} else  {
						System.out.println("Found too many nodes by this key: " + counter);
						++badKeys;
					}
				}
				
				tx.success();
			}
			
//			graphDb.shutdown();
			reader.close();

			System.out.println("Done. Assingned " + goodKeys + " URL's. " + unknownKeys + " keys has not been found in the database, " + badKeys + " keys was just bad"); 
			
		} catch (Exception e) {
			e.printStackTrace();
		}		
	}

}
