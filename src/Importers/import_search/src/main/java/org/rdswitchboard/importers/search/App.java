package org.rdswitchboard.importers.search;

public class App {
	private static final String NEO4J_URL = "http://localhost:7474/db/data/";
//	private static final String NEO4J_URL = "http://ec2-54-187-84-58.us-west-2.compute.amazonaws.com:7474/db/data/"; 
	
	private static final String FIELDS_FOLDER = "orcid/fields"; 
//	private static final String FIELDS_FOLDER = "/home/dima/Grants/orcid/fields"; 

	private static final String GOOGLE_FOLDER = "google2";
//	private static final String GOOGLE_FOLDER = "/home/dima/Grants/google2";
	
	private static final String CACHE_FOLDER = "orcid/search";
//	private static final String CACHE_FOLDER = "/home/dima/Grants/orcid/search";

	private static final String BLACK_LIST = "conf/black.list";
//	private static final String BLACK_LIST = "/home/dima/Grants/conf/black.list";
	
	public static void main(String[] args) {
		
		String neo4jUrl = NEO4J_URL;
		if (args.length > 0)
			neo4jUrl = args[0];
		
		String fieldsPath = FIELDS_FOLDER;
		if (args.length > 1)
			fieldsPath = args[1];

		String googlePath = GOOGLE_FOLDER;
		if (args.length > 2)
			googlePath = args[2];
		
		String cachePath = CACHE_FOLDER;
		if (args.length > 3)
			cachePath = args[3];
		
		String blackList = BLACK_LIST;
		if (args.length > 4)
			blackList = args[4];
	
		try {
			Importer importer = new Importer(neo4jUrl, fieldsPath, googlePath, cachePath, blackList);
			importer.process();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
