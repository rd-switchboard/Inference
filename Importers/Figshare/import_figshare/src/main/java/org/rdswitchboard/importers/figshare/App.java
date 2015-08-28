package org.rdswitchboard.importers.figshare;

public class App {
	private static final String NEO4J_URL = "http://localhost:7474/db/data/";	
	private static final String FIGSHARE_URI = "figshare/active_authors_v2.json";	
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		String neo4jUrl = NEO4J_URL;
		if (args.length > 0 && !args[0].isEmpty())
			neo4jUrl = args[0];
			
		String figshareUri = FIGSHARE_URI;
		if (args.length > 1 && !args[1].isEmpty())
			figshareUri = args[1];
		
		try {
			Importer importer = new Importer(neo4jUrl, figshareUri);
			importer.process();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}			
	}
}
