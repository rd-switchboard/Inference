package org.rdswitchboard.importers.dli;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.rdswitchboard.libraries.configuration.Configuration;
import org.rdswitchboard.libraries.dli.CrosswalkDli;
import org.rdswitchboard.libraries.graph.Graph;
import org.rdswitchboard.libraries.graph.GraphUtils;
import org.rdswitchboard.libraries.neo4j.Neo4jDatabase;

import com.amazonaws.auth.InstanceProfileCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;

public class App {
	
	public static final String DLI_VERSION_FILE = "dli";

	public static void main(String[] args) {
		try {
			Properties properties = Configuration.fromArgs(args);
	        
	        String neo4jFolder = properties.getProperty(Configuration.PROPERTY_NEO4J);
	        
	        if (StringUtils.isEmpty(neo4jFolder))
	            throw new IllegalArgumentException("Neo4j Folder can not be empty");
	        
	        System.out.println("Neo4J: " + neo4jFolder);
			
	        String bucket = properties.getProperty(Configuration.PROPERTY_S3_BUCKET);
	        
	        if (StringUtils.isEmpty(bucket))
                throw new IllegalArgumentException("AWS S3 Bucket can not be empty");

	        System.out.println("S3 Bucket: " + bucket);
	        
	        String prefix = properties.getProperty(Configuration.PROPERTY_DLI_S3);
	        	
	        if (StringUtils.isEmpty(prefix))
	            throw new IllegalArgumentException("AWS S3 Prefix can not be empty");
        
	        System.out.println("S3 Prefix: " + prefix);
	        
	        String versionFolder = properties.getProperty("versions");
	        if (StringUtils.isEmpty(versionFolder))
	            throw new IllegalArgumentException("Versions Folder can not be empty");
	        	        
        	processFiles(bucket, prefix, neo4jFolder, versionFolder);
		} catch (Exception e) {
            e.printStackTrace();
            
            System.exit(1);
		}       
	}
	
	private static void processFiles(String bucket, String prefix, String neo4jFolder, String versionFolder) throws Exception {
        AmazonS3 s3client = new AmazonS3Client(new InstanceProfileCredentialsProvider());
        
        CrosswalkDli crosswalk = new CrosswalkDli();
        crosswalk.setSource(GraphUtils.SOURCE_DLI);
        //crosswalk.setVerbose(true);
        
    	Neo4jDatabase importer = new Neo4jDatabase(neo4jFolder);
    	//importer.setVerbose(true);
    	
    	ListObjectsRequest listObjectsRequest;
		ObjectListing objectListing;

		String file = prefix + "/latest.txt";
		S3Object object = s3client.getObject(new GetObjectRequest(bucket, file));
		
		String latest;
		try (InputStream txt = object.getObjectContent()) {
			latest = IOUtils.toString(txt, StandardCharsets.UTF_8).trim();
		}
		
		String folder = prefix + "/" + latest + "/";		
		
		System.out.println("S3 Repository: " + latest);

		
	    listObjectsRequest = new ListObjectsRequest()
			.withBucketName(bucket)
			.withPrefix(folder);
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
				importer.importGraph(graph);
			}
			listObjectsRequest.setMarker(objectListing.getNextMarker());
		} while (objectListing.isTruncated());
		
	    Files.write(Paths.get(versionFolder, DLI_VERSION_FILE), latest.getBytes());
	    
		System.out.println("Done");
		
		crosswalk.printStatistics(System.out);
		importer.printStatistics(System.out);
	}
}
