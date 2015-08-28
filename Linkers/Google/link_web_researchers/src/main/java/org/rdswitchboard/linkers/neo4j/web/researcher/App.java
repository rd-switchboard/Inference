package org.rdswitchboard.linkers.neo4j.web.researcher;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

import org.parboiled.common.StringUtils;

public class App {
	private static final String PROPERTIES_FILE = "properties/link_web_researchers.properties";
	
	private static final String GOOGLE_CACHE = "google";
	private static final String BLACK_LIST = "conf/black.list";
	private static final String MIN_TITLE_LENGTH = "30";
	private static final String MAX_THREADS = "100";
	
	public static void main(String[] args) {
		try {
            String propertiesFile = PROPERTIES_FILE;
            if (args.length > 0 && !StringUtils.isEmpty(args[0])) 
                    propertiesFile = args[0];

            Properties properties = new Properties();
	        try (InputStream in = new FileInputStream(propertiesFile)) {
	            properties.load(in);
	        }
	        	        
	        String neo4jFolder = properties.getProperty("neo4j");
	        if (StringUtils.isEmpty(neo4jFolder))
	            throw new IllegalArgumentException("Neo4j Folder can not be empty");
	        System.out.println("Neo4J: " + neo4jFolder);

	        String googleCache = properties.getProperty("google.cahce", GOOGLE_CACHE);
	        if (StringUtils.isEmpty(googleCache))
	            throw new IllegalArgumentException("Google Cache Folder can not be empty");
	        System.out.println("Google Cahce: " + googleCache);

	        String blackList = properties.getProperty("black.list", BLACK_LIST);
	        if (StringUtils.isEmpty( blackList))
	            throw new IllegalArgumentException("Black List Path can not be empty");
	        System.out.println("Black List: " +  blackList);

	        int minTitleLength = Integer.parseInt(properties.getProperty("min.title.length", MIN_TITLE_LENGTH));
	        System.out.println("Min Title Length: " +  minTitleLength);
	        
	        int maxThreads = Integer.parseInt(properties.getProperty("max.threads", MAX_THREADS));
	        System.out.println("Max Threads: " +  maxThreads);

	        Linker linker = new Linker(neo4jFolder, blackList, minTitleLength, true);
	        linker.setMaxThreads(maxThreads);
	        linker.link(googleCache);
	        
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
