package org.rdswitchboard.importers.google;

/**
 * 
 * @author dima
 *
 */

public class App {
	//private static final String DATA_FOLDER = "publications";
//	private static final String NEO4J_URL = "http://ec2-54-187-84-58.us-west-2.compute.amazonaws.com:7474/db/data/"; 
	private static final String NEO4J_URL = "http://localhost:7474/db/data/";
//	private static final String NEO4J_URL = "http://localhost:7476/db/data/";
	//private static final String CACHE_FOLDER = "cache";

	public static void main(String[] args) {
		String neo4jUrl = NEO4J_URL;
		if (args.length > 0 && null != args[0] && !args[0].isEmpty())
			neo4jUrl = args[0];
	/*	
		String cacheFolder = CACHE_FOLDER;
		if (args.length > 0 && null != args[0] && !args[0].isEmpty())
			cacheFolder = args[0];
		*/
		
		try {
			Importer importer = new Importer(neo4jUrl);
			importer.init("conf/black.list");
			importer.processPublications("google/publications");
			importer.processGrants("google/grants");
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
