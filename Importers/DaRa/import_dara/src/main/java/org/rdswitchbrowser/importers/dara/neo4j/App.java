package org.rdswitchbrowser.importers.dara.neo4j;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.rdswitchboard.libraries.configuration.Configuration;
import org.rdswitchboard.libraries.dara.CrosswalkDara;
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
import com.amazonaws.util.StringUtils;

public class App {
	
	public static void main(String[] args) {
		try {
			Properties properties = Configuration.fromArgs(args);
			
	        String neo4jFolder = properties.getProperty(Configuration.PROPERTY_NEO4J);
	        
	        if (StringUtils.isNullOrEmpty(neo4jFolder))
	            throw new IllegalArgumentException("Neo4j Folder can not be empty");
	        
	        System.out.println("Neo4J: " + neo4jFolder);
	        
	        String bucket = properties.getProperty(Configuration.PROPERTY_S3_BUCKET);
	        
	        if (StringUtils.isNullOrEmpty(bucket))
                throw new IllegalArgumentException("AWS S3 Bucket can not be empty");

	        System.out.println("S3 Bucket: " + bucket);
	        
	        String prefix = properties.getProperty(Configuration.PROPERTY_DARA_S3);
	        	
	        if (StringUtils.isNullOrEmpty(prefix))
	            throw new IllegalArgumentException("AWS S3 Prefix can not be empty");
        
	        System.out.println("S3 Prefix: " + prefix);
	        
	        
	       /*debugFile(accessKey, secretKey, bucket, "rda/rif/class:collection/54800.xml");*/ 
	        
        	processFiles(bucket, prefix, neo4jFolder);
		} catch (Exception e) {
            e.printStackTrace();
		}       
	}
	
	private static void processFiles(String bucket, String prefix, String neo4jFolder) throws Exception {
        AmazonS3 s3client = new AmazonS3Client(new InstanceProfileCredentialsProvider());
        
        CrosswalkDara crosswalk = new CrosswalkDara();
        crosswalk.setSource(GraphUtils.SOURCE_DARA);
   //    crosswalk.setVerbose(true);
        
    	Neo4jDatabase importer = new Neo4jDatabase(neo4jFolder);
    //	importer.setVerbose(true);
    	
    	ListObjectsRequest listObjectsRequest;
		ObjectListing objectListing;

		String file = prefix + "/latest.txt";
		S3Object object = s3client.getObject(new GetObjectRequest(bucket, file));
		
		String latest;
		try (InputStream txt = object.getObjectContent()) {
			latest = prefix + "/" + IOUtils.toString(txt, StandardCharsets.UTF_8);
		}		
		
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
				importer.importGraph(graph);
			}
			listObjectsRequest.setMarker(objectListing.getNextMarker());
		} while (objectListing.isTruncated());
		
		System.out.println("Done");
		
		crosswalk.printStatistics(System.out);
		importer.printStatistics(System.out);
	}
}
