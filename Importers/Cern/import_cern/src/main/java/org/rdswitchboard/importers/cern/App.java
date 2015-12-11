package org.rdswitchboard.importers.cern;

import java.io.InputStream;
import java.util.Properties;

import org.rdswitchboard.libraries.configuration.Configuration;
import org.rdswitchboard.libraries.graph.Graph;
import org.rdswitchboard.libraries.graph.GraphUtils;
import org.rdswitchboard.libraries.marc21.CrosswalkMarc21;
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

/**
 * Main class for CERN DC data importer
 *
 * The class uses automatically generated org.openarchives.oai._2 and org.purl.dc.elements._1
 *
 * This software design to process xml records in cern/xml/oai_dc
 * and will post data into Neo4J located at http://localhost:7474/db/data/
 * 
 * @author Dmitrij Kudriavcev, dmitrij@kudriavcev.info
 *
 */
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
	        
	        String prefix = properties.getProperty(Configuration.PROPERTY_CERN_S3);
	        	
	        if (StringUtils.isNullOrEmpty(prefix))
	            throw new IllegalArgumentException("AWS S3 Prefix can not be empty");
        
	        System.out.println("S3 Prefix: " + prefix);
	        	        
	        /*debugFile(accessKey, secretKey, bucket, "rda/rif/class:collection/54800.xml");*/ 
	        
        	processFiles(bucket, prefix, neo4jFolder);
		} catch (Exception e) {
            e.printStackTrace();
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
	
	private static void processFiles(String bucket, String prefix, String neo4jFolder) throws Exception {
        AmazonS3 s3client = new AmazonS3Client(new InstanceProfileCredentialsProvider());
        
        CrosswalkMarc21 crosswalk = new CrosswalkMarc21();
        crosswalk.setSource(GraphUtils.SOURCE_CERN);
        //crosswalk.setVerbose(true);
        
    	Neo4jDatabase importer = new Neo4jDatabase(neo4jFolder);
    	//importer.setVerbose(true);
    	
    	ListObjectsRequest listObjectsRequest;
		ObjectListing objectListing;
		S3Object object;
		
	    listObjectsRequest = new ListObjectsRequest()
			.withBucketName(bucket)
			.withPrefix(prefix);
	    do {
			objectListing = s3client.listObjects(listObjectsRequest);
			for (S3ObjectSummary objectSummary : 
				objectListing.getObjectSummaries()) {
				
				String file = objectSummary.getKey();

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
