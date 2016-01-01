package org.rdswitchboard.importers.orcid;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

import org.parboiled.common.StringUtils;
import org.rdswitchboard.libraries.configuration.Configuration;

public class App {

	private static final String ORCID_FOLDER = "orcid/json";

	/**
	 * Main class function
	 * @param args String[] Expected to have path to the institutions.csv file and Neo4J URL.
	 * If missing, the default parameters will be used.
	 */
	public static void main(String[] args) {
		try {
			Properties properties = Configuration.fromArgs(args);
			
	        System.out.println("Importing ORCID Files");
	                
	        String neo4jFolder = properties.getProperty(Configuration.PROPERTY_NEO4J);
	        if (StringUtils.isEmpty(neo4jFolder))
	            throw new IllegalArgumentException("Neo4j Folder can not be empty");
	        System.out.println("Neo4j: " + neo4jFolder);
	        
	        String orcidFolder = properties.getProperty(Configuration.PROPERTY_ORCID_JSON, ORCID_FOLDER);
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
			
			System.exit(1);
		}
	}

}
