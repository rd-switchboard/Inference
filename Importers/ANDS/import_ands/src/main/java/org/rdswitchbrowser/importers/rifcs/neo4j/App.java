package org.rdswitchbrowser.importers.rifcs.neo4j;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.rdswitchboard.libraries.configuration.Configuration;
import org.rdswitchboard.libraries.graph.Graph;
import org.rdswitchboard.libraries.graph.GraphUtils;
import org.rdswitchboard.libraries.neo4j.Neo4jDatabase;
import org.rdswitchboard.libraries.rifcs.CrosswalkRifCs;

import com.amazonaws.auth.InstanceProfileCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;

public class App {

	public static final String DEF_XML_TYPE = "oai";
		
	public static void main(String[] args) {
		try {
			Properties properties = Configuration.fromArgs(args);
			        
	        String neo4jFolder = properties.getProperty(Configuration.PROPERTY_NEO4J);
	        
	        if (StringUtils.isEmpty(neo4jFolder))
	            throw new IllegalArgumentException("Neo4j Folder can not be empty");
	        
	        System.out.println("Neo4J: " + neo4jFolder);
	        
	        String bucket = properties.getProperty(Configuration.PROPERTY_S3_BUCKET);
	        String prefix = properties.getProperty(Configuration.PROPERTY_ANDS_S3S);
	        String xmlFolder = properties.getProperty(Configuration.PROPERTY_ANDS_XML);
	        String xmlType = properties.getProperty(Configuration.PROPERTY_ANDS_XML_TYPE, DEF_XML_TYPE);
	        
	        CrosswalkRifCs.XmlType type = CrosswalkRifCs.XmlType.valueOf(xmlType); 
	        
	        if (!StringUtils.isEmpty(bucket) && !StringUtils.isEmpty(prefix)) {
	        	System.out.println("S3 Bucket: " + bucket);
	        	System.out.println("S3 Prefix: " + prefix);
	        	
	        	processS3Files(bucket, prefix, neo4jFolder, type);
	        } else if (!StringUtils.isEmpty(xmlFolder)) {
	        	System.out.println("XML: " + xmlFolder);
	        	
	        	processFiles(xmlFolder, neo4jFolder, type);
	        } else
                throw new IllegalArgumentException("Please provide either S3 Bucket and prefix OR a path to a XML Folder");

	        	        
	       /*debugFile(accessKey, secretKey, bucket, "rda/rif/class:collection/54800.xml");*/ 
	        
        	
		} catch (Exception e) {
            e.printStackTrace();
            
            System.exit(1);
		}       
	}
	
	/*
	private static void debugFile(String accessKey, String secretKey, String bucket, String file) throws Exception {
		AWSCredentials awsCredentials = new BasicAWSCredentials(accessKey, secretKey);
        AmazonS3 s3client = new AmazonS3Client(awsCredentials);
        
        Crosswalk crosswalk = new Crosswalk();
        crosswalk.setVerbose(true);
    	Importer importer = new Importer(awsCredentials);
    	importer.setVerbose(true);

    	System.out.println("Processing file: " + file);
				
		S3Object object = s3client.getObject(new GetObjectRequest(bucket, file));
		InputStream xml = object.getObjectContent();
								
		System.out.println("Parsing file: " + file);
		Collection<Record> records = crosswalk.process(xml).values();

		System.out.println("Uploading " + records.size() + " records");
		importer.importRecords(SOURCE_ANDS, records);
	}
	*/
	
	private static void processS3Files(String bucket, String prefix, String neo4jFolder, CrosswalkRifCs.XmlType type) throws Exception {
        AmazonS3 s3client = new AmazonS3Client(new InstanceProfileCredentialsProvider());
        
        CrosswalkRifCs crosswalk = new CrosswalkRifCs();
        crosswalk.setSource(GraphUtils.SOURCE_ANDS);
        crosswalk.setType(type);
     //   crosswalk.setVerbose(true);
        
    	Neo4jDatabase neo4j = new Neo4jDatabase(neo4jFolder);
    	//importer.setVerbose(true);
    		    
    	ListObjectsRequest listObjectsRequest;
		ObjectListing objectListing;
		
		String file = prefix + "/latest.txt";
		S3Object object = s3client.getObject(new GetObjectRequest(bucket, file));
		
		String latest;
		try (InputStream txt = object.getObjectContent()) {
			latest = prefix + "/" + IOUtils.toString(txt, StandardCharsets.UTF_8).trim() + "/";
		}
		
		System.out.println("S3 Repository: " + latest);
		
	    listObjectsRequest = new ListObjectsRequest()
			.withBucketName(bucket)
			.withPrefix(latest);
	    do {
			objectListing = s3client.listObjects(listObjectsRequest);
			for (S3ObjectSummary objectSummary : 
				objectListing.getObjectSummaries()) {
				
				file = objectSummary.getKey();

		        System.out.println("Processing file: " + file);
				
				object = s3client.getObject(new GetObjectRequest(bucket, file));
				InputStream xml = object.getObjectContent();
								
				System.out.println("Parsing file: " + file);
				Graph graph = crosswalk.process(xml);
				neo4j.importGraph(graph);
			}
			listObjectsRequest.setMarker(objectListing.getNextMarker());
		} while (objectListing.isTruncated());
		
		System.out.println("Done");
		
		crosswalk.printStatistics(System.out);
		neo4j.printStatistics(System.out);
	}
	
	private static void processFiles(String xmlFolder, String neo4jFolder, CrosswalkRifCs.XmlType type) throws Exception {
        CrosswalkRifCs crosswalk = new CrosswalkRifCs();
        crosswalk.setSource(GraphUtils.SOURCE_ANDS);
        crosswalk.setType(type);
     //   crosswalk.setVerbose(true);
        
    	Neo4jDatabase neo4j = new Neo4jDatabase(neo4jFolder);
    	//importer.setVerbose(true);
    		    
		File[] files = new File(xmlFolder).listFiles();
		for (File file : files) 
			if (!file.isDirectory()) 
		        try (InputStream xml = new FileInputStream(file))
		        {
			        System.out.println("Processing file: " + file);
					
			     	Graph graph = crosswalk.process(xml);
					neo4j.importGraph(graph);
		        }
		
		System.out.println("Done");
		
		crosswalk.printStatistics(System.out);
		neo4j.printStatistics(System.out);
	}
	
	
	
	/*private static void processMultiThread(String accessKey, String secretKey, 
			String bucket, String prefix, int maxThreads) throws Exception {
		AWSCredentials awsCredentials = new BasicAWSCredentials(accessKey, secretKey);
        AmazonS3 s3client = new AmazonS3Client(awsCredentials);

		Semaphore semaphore = new Semaphore(maxThreads);

		List<ImportThread> threads = new ArrayList<ImportThread>();
		for (int i = 0; i < maxThreads; ++i) {
			ImportThread thread = new ImportThread(SOURCE_ANDS, semaphore, awsCredentials);
			thread.start();
			threads.add(thread);
		}		
        
    	ListObjectsRequest listObjectsRequest = new ListObjectsRequest()
			.withBucketName(bucket)
			.withPrefix(prefix);
		ObjectListing objectListing;
		S3Object object;	

		do {
			objectListing = s3client.listObjects(listObjectsRequest);
			for (S3ObjectSummary objectSummary : 
				objectListing.getObjectSummaries()) {
				
				semaphore.acquire(); 

				String file = objectSummary.getKey();
		        System.out.println("Processing file: " + file);
				
				object = s3client.getObject(new GetObjectRequest(bucket, file));
				InputStream xml = object.getObjectContent();
				
				boolean importAssigned = false;
				for (ImportThread thread : threads) 
					if (thread.isFree()) {
						thread.process(xml);
						importAssigned = true;
						
						break;
					}								
				
				if (!importAssigned)
					throw new ImportThreadException("All matcher threads are busy");
			}
			listObjectsRequest.setMarker(objectListing.getNextMarker());
		} while (objectListing.isTruncated());
		
		for (ImportThread thread : threads) {
			thread.finishCurrentAndExit();
			thread.join();
		}
	}*/
}
