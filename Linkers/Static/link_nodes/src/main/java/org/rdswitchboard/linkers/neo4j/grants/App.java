package org.rdswitchboard.linkers.neo4j.grants;

import java.util.Properties;

import org.parboiled.common.StringUtils;
import org.rdswitchboard.libraries.configuration.Configuration;

public class App {
	
	public static void main(String[] args) {
		try {
			Properties properties = Configuration.fromArgs(args);
	        
	        System.out.println("Linking Nodes");
	        	        
	        String neo4jFolder = properties.getProperty(Configuration.PROPERTY_NEO4J);
	        if (StringUtils.isEmpty(neo4jFolder))
	            throw new IllegalArgumentException("Neo4j Folder can not be empty");
	        System.out.println("Neo4J: " + neo4jFolder);
	        
	        Linker linker = new Linker(neo4jFolder);
	        linker.link();
	      //  linker.printStatistics(System.out);
	        
		} catch (Exception e) {
			e.printStackTrace();
			
			System.exit(1);
		}
	}
}
