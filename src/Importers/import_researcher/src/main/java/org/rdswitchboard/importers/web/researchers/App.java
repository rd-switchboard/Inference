package org.rdswitchboard.importers.web.researchers;

import javax.xml.bind.JAXBException;

public class App {
	//private static final String DATA_FOLDER = "publications";
	private static final String NEO4J_URL = "http://ec2-54-69-203-235.us-west-2.compute.amazonaws.com:7476/db/data/"; 
//	private static final String NEO4J_URL = "http://localhost:7474/db/data/";
//	private static final String NEO4J_URL = "http://localhost:7476/db/data/";

	/**
	 * Main class function
	 * @param args String[] Expected to have path to the institutions.csv file and Neo4J URL.
	 * If missing, the default parameters will be used.
	 */
	public static void main(String[] args) {
		String neo4jUrl = NEO4J_URL;
		if (args.length > 0 && null != args[0] && !args[0].isEmpty())
			neo4jUrl = args[0];
		
		try {
			Importer importer = new Importer(neo4jUrl);
			importer.process();
		} catch (JAXBException e) {
			e.printStackTrace();
		}
	}
}
