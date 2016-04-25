package org.rdswitchbrowser.importers.rg.neo4j;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.rdswitchboard.libraries.configuration.Configuration;
import org.rdswitchboard.libraries.graph.Graph;
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
	
	public static void main(String[] args) {
		try {
			Properties properties = Configuration.fromArgs(args);
			        
	        String neo4jFolder = properties.getProperty(Configuration.PROPERTY_NEO4J);
	        
	        if (StringUtils.isEmpty(neo4jFolder))
	            throw new IllegalArgumentException("Neo4j Folder can not be empty");
	        
	        System.out.println("Neo4J: " + neo4jFolder);
	        
	        String bucket = properties.getProperty(Configuration.PROPERTY_S3_BUCKET);
	        String prefix = properties.getProperty(Configuration.PROPERTY_S3_PREIFX);
	        String xmlFolder = properties.getProperty(Configuration.PROPERTY_XML_FOLDER);
	        String xmlType = properties.getProperty(Configuration.PROPERTY_XML_TYPE);
	        String source = properties.getProperty(Configuration.PROPERTY_SOURCE);
	        String crosswalk = properties.getProperty(Configuration.PROPERTY_CROSSWALK);
		    
	        Templates template = null;
	        
	        if (!StringUtils.isEmpty(crosswalk)) {
	        	System.out.println("Crosswalk: " + crosswalk);
	        	
	        	template = TransformerFactory.newInstance().newTemplates(
	        			new StreamSource(
	        					new FileInputStream(crosswalk)));
	        } 
	        
	        CrosswalkRG.XmlType type = CrosswalkRG.XmlType.valueOf(xmlType); 
	        
	        if (!StringUtils.isEmpty(bucket) && !StringUtils.isEmpty(prefix)) {
	        	System.out.println("S3 Bucket: " + bucket);
	        	System.out.println("S3 Prefix: " + prefix);

	        	String versionFolder = properties.getProperty(Configuration.PROPERTY_VERSIONS_FOLDER);
		        if (StringUtils.isEmpty(versionFolder))
		            throw new IllegalArgumentException("Versions Folder can not be empty");
	        	
	        	processS3Files(bucket, prefix, neo4jFolder, versionFolder, source, type, template);
	        } else if (!StringUtils.isEmpty(xmlFolder)) {
	        	System.out.println("XML: " + xmlFolder);
	        	
	        	processFiles(xmlFolder, neo4jFolder, source, type, template);
	        } else
                throw new IllegalArgumentException("Please provide either S3 Bucket and prefix OR a path to a XML Folder");

	        	        
	       // debugFile(accessKey, secretKey, bucket, "rda/rif/class:collection/54800.xml");
	        
        	
		} catch (Exception e) {
            e.printStackTrace();
            
            System.exit(1);
		}       
	}
	
	private static void processS3Files(String bucket, String prefix, String neo4jFolder, 
			String versionFolder, String source, CrosswalkRG.XmlType type, Templates template) throws Exception {
        AmazonS3 s3client = new AmazonS3Client(new InstanceProfileCredentialsProvider());
        
        CrosswalkRG crosswalk = new CrosswalkRG();
        crosswalk.setSource(source);
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
			latest = IOUtils.toString(txt, StandardCharsets.UTF_8).trim();
		}
		
		if (StringUtils.isEmpty(latest)) 
			throw new Exception("Unable to find latest harvest in the S3 Bucket (latest.txt file is empty or not avaliable). Please check if you have access to S3 bucket and did you have completed the harvestring.");	
		
		String folder = prefix + "/" + latest + "/";
		
		System.out.println("S3 Repository: " + latest);
		
	    listObjectsRequest = new ListObjectsRequest()
			.withBucketName(bucket)
			.withPrefix(folder);
	    do {
			objectListing = s3client.listObjects(listObjectsRequest);
			for (S3ObjectSummary objectSummary : objectListing.getObjectSummaries()) {
				
				file = objectSummary.getKey();

		        System.out.println("Processing file: " + file);
				
				object = s3client.getObject(new GetObjectRequest(bucket, file));
				
				if (null != template) {
					Source reader = new StreamSource(object.getObjectContent());
					StringWriter writer = new StringWriter();
					
					Transformer transformer = template.newTransformer(); 
					transformer.transform(reader, new StreamResult(writer));
					
					InputStream stream = new ByteArrayInputStream(writer.toString().getBytes(StandardCharsets.UTF_8));
					
					Graph graph = crosswalk.process(stream);
					neo4j.importGraph(graph);
		        } else {
		        	InputStream xml = object.getObjectContent();
						
		        	Graph graph = crosswalk.process(xml);
					neo4j.importGraph(graph);
				}
			}
			listObjectsRequest.setMarker(objectListing.getNextMarker());
		} while (objectListing.isTruncated());
	    
	    Files.write(Paths.get(versionFolder, source), latest.getBytes());
	    
		System.out.println("Done");
				
		crosswalk.printStatistics(System.out);
		neo4j.printStatistics(System.out);
		
		
	}
	
	private static void processFiles(String xmlFolder, String neo4jFolder, String source, 
			CrosswalkRG.XmlType type, Templates template) throws Exception {
		CrosswalkRG crosswalk = new CrosswalkRG();
        crosswalk.setSource(source);
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
			        
			        if (null != template) {
						Source reader = new StreamSource(xml);
						StringWriter writer = new StringWriter();
						
						Transformer transformer = template.newTransformer(); 
						transformer.transform(reader, new StreamResult(writer));
						
						InputStream stream = new ByteArrayInputStream(writer.toString().getBytes(StandardCharsets.UTF_8));
						
						Graph graph = crosswalk.process(stream);
						neo4j.importGraph(graph);

			        } else {
						Graph graph = crosswalk.process(xml);
						neo4j.importGraph(graph);
					}
		        }
		
		System.out.println("Done");
		
		crosswalk.printStatistics(System.out);
		neo4j.printStatistics(System.out);
	}

}
