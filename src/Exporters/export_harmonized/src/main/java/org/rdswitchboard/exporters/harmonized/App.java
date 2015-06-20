package org.rdswitchboard.exporters.harmonized;

import java.io.IOException;

public class App {
	private static final String SOURCE_NEO4J_FOLDER = "neo4j";	
	private static final String OUTPUT_FOLDER = "harmonized";
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		String sourceNeo4jFolder = SOURCE_NEO4J_FOLDER;
		if (args.length > 0 && !args[0].isEmpty())
			sourceNeo4jFolder = args[0];
			
		String outputFolder = OUTPUT_FOLDER;
		if (args.length > 1 && !args[1].isEmpty())
			outputFolder = args[1];
		
		try {
			Exporter expoter = new Exporter(sourceNeo4jFolder, outputFolder);
			expoter.process();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}			
	}
}
