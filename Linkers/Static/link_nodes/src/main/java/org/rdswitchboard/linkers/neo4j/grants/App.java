package org.rdswitchboard.linkers.neo4j.grants;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

import org.parboiled.common.StringUtils;

public class App {
	private static final String PROPERTIES_FILE = "properties/link_nodes.properties";
	
	private static final String GOOGLE_CACHE = "google";
	private static final String BLACK_LIST = "conf/black.list";
	private static final String MIN_TITLE_LENGTH = "30";
	
	public static void main(String[] args) {
		try {
            String propertiesFile = PROPERTIES_FILE;
            if (args.length > 0 && !StringUtils.isEmpty(args[0])) 
                    propertiesFile = args[0];

            Properties properties = new Properties();
	        try (InputStream in = new FileInputStream(propertiesFile)) {
	            properties.load(in);
	        }
	        
	        System.out.println("Linking Nodes");
	        	        
	        String neo4jFolder = properties.getProperty("neo4j");
	        if (StringUtils.isEmpty(neo4jFolder))
	            throw new IllegalArgumentException("Neo4j Folder can not be empty");
	        System.out.println("Neo4J: " + neo4jFolder);
	        
	        Linker linker = new Linker(neo4jFolder);
	        linker.link();
	      //  linker.printStatistics(System.out);
	        
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
