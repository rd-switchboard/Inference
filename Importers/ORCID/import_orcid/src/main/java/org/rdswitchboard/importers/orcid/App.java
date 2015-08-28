package org.rdswitchboard.importers.orcid;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

import org.parboiled.common.StringUtils;

public class App {

	private static final String PROPERTIES_FILE = "properties/import_orcid.properties";
	private static final String ORCID_FOLDER = "orcid/json";

	/**
	 * Main class function
	 * @param args String[] Expected to have path to the institutions.csv file and Neo4J URL.
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
	        
	        System.out.println("Importing ORCID Files");
	                
	        String neo4jFolder = properties.getProperty("neo4j");
	        if (StringUtils.isEmpty(neo4jFolder))
	            throw new IllegalArgumentException("Neo4j Folder can not be empty");
	        System.out.println("Neo4j: " + neo4jFolder);
	        
	        String orcidFolder = properties.getProperty("orcid.json", ORCID_FOLDER);
	        if (StringUtils.isEmpty(orcidFolder))
	            throw new IllegalArgumentException("Invalid path to ORCID Json Folder");
			
		//	Importer.GetTestRecord("0000-0002-4259-9774"); // Amir's
		//	Importer.GetTestRecord("0000-0002-6386-9753");
			
	        ImporterOrcid importer = new ImporterOrcid(neo4jFolder);
	        
	        importer.setVerbose(true);
			
	        importer.importOrcid(orcidFolder);
			
			importer.printStatistcs(System.out);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
