package org.rdswitchboard.linkers.neo4j.web.researcher;

import java.util.List;

import org.rdswitchboard.linkers.neo4j.web.researcher.model.Link;
import org.rdswitchboard.linkers.neo4j.web.researcher.service.LinkService;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

// c4.8xlarge

public class App {
	
	private static final String GOOGLE_CACHE = "google";
	private static final String BLACK_LIST = "conf/black.list";
	private static final String MIN_TITLE_LENGTH = "30";
	private static final String MAX_THREADS = "100";
	
	public static void main(String[] args) {
		
		ApplicationContext context = 
	    		new ClassPathXmlApplicationContext("spring/config/Spring-Config.xml");
		
//		Hibernate.initialize(proxy);
		
		System.out.println("\n\n\n*** Begin Testing ***\n\n\n");
		
		LinkService linkService = (LinkService) context.getBean("linkService");
		
		System.out.println("\n\n\n*** Insert new Link ***\n\n\n");
		
		
		try { 
			Link link = new Link();
		
			link.setLink("http://test.test/test.html");
			link.setData("Data/test2.html");
			link.setMetadata("Metadata/test2.json");
			
			linkService.save(link);
			
			System.out.println(link);
		} catch (Exception e) {
			e.printStackTrace();
		} 
			
		System.out.println("\n\n\n*** Select all Links ***\n\n\n");
		
		linkService = (LinkService) context.getBean("linkService");
		
		List<Link> links = linkService.getAllLinks();
		
		

		System.out.println("\n\n\n*** Dump Links ***\n\n\n");
		
		System.out.println(links);
		
		/*
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
			
			System.exit(1);
		}*/
	}
	

}
