package org.rdswitchboard.linkers.neo4j.web.researcher;

import java.util.Properties;

import org.parboiled.common.StringUtils;
import org.rdswitchboard.libraries.configuration.Configuration;

public class App {
	
	private static final String GOOGLE_CACHE = "google";
	private static final String BLACK_LIST = "conf/black.list";
	private static final String MIN_TITLE_LENGTH = "30";
	private static final String MAX_THREADS = "100";
	
	public static void main(String[] args) {
		try {
			Properties properties = Configuration.fromArgs(args);
	        	        
	        String neo4jFolder = properties.getProperty(Configuration.PROPERTY_NEO4J);
	        if (StringUtils.isEmpty(neo4jFolder))
	            throw new IllegalArgumentException("Neo4j Folder can not be empty");
	        System.out.println("Neo4J: " + neo4jFolder);

	        String googleCache = properties.getProperty(Configuration.PROPERTY_GOOGLE_CACHE, GOOGLE_CACHE);
	        if (StringUtils.isEmpty(googleCache))
	            throw new IllegalArgumentException("Google Cache Folder can not be empty");
	        System.out.println("Google Cahce: " + googleCache);

	        String blackList = properties.getProperty(Configuration.PROPERTY_GOOGLE_BLACK_LIST, BLACK_LIST);
	        if (StringUtils.isEmpty( blackList))
	            throw new IllegalArgumentException("Black List Path can not be empty");
	        System.out.println("Black List: " +  blackList);

	        int minTitleLength = Integer.parseInt(properties.getProperty(Configuration.PROPERTY_GOOGLE_MIN_TITLE_LENGTH, MIN_TITLE_LENGTH));
	        System.out.println("Min Title Length: " +  minTitleLength);
	        
	        int maxThreads = Integer.parseInt(properties.getProperty(Configuration.PROPERTY_GOOGLE_THREADS, MAX_THREADS));
	        System.out.println("Max Threads: " +  maxThreads);

	        Linker linker = new Linker(neo4jFolder, blackList, minTitleLength, true);
	        linker.setMaxThreads(maxThreads);
	        linker.link(googleCache);
	        
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
