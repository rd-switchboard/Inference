package org.grants.compiler.dryad;

public class App {

	private static final String SOURCE_NEO4J_URL = "http://localhost:7474/db/data/";	
	private static final String TARGET_NEO4J_URL = "http://localhost:7484/db/data/";	
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		String sourceNeo4jUrl = SOURCE_NEO4J_URL;
		if (args.length > 0 && !args[0].isEmpty())
			sourceNeo4jUrl = args[0];
		
		String targetNeo4j4jUrl = TARGET_NEO4J_URL;
		if (args.length > 1 && !args[1].isEmpty())
			targetNeo4j4jUrl = args[1];
			
		Compiler comiler = new Compiler(sourceNeo4jUrl, targetNeo4j4jUrl);			
		comiler.process();
	}
}
