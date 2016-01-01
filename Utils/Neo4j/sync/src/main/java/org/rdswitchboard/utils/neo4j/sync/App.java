package org.rdswitchboard.utils.neo4j.sync;

import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.ResourceIterable;
import org.neo4j.graphdb.Transaction;
import org.neo4j.tooling.GlobalGraphOperations;
import org.rdswitchboard.libraries.configuration.Configuration;
import org.rdswitchboard.libraries.graph.GraphUtils;
import org.rdswitchboard.libraries.neo4j.Neo4jUtils;

public class App {
	public static void main(String[] args) {
		try {
			Properties properties = Configuration.fromArgs(args);
	        
	        System.out.println("Sync Neo4j database");
	                	        
	        String source = properties.getProperty(Configuration.PROPERTY_SOURCE);
	        if (StringUtils.isEmpty(source))
	            throw new IllegalArgumentException("Source Neo4j can not be empty");
	        System.out.println("Source Neo4j: " + source);

	        String target = properties.getProperty(Configuration.PROPERTY_TARGET);
	        if (StringUtils.isEmpty(target))
	            throw new IllegalArgumentException("Target Neo4j can not be empty");
	        System.out.println("Target Neo4j: " + target);
	        
	        System.out.println("Downloading source database");
	        
	   /*     srcGraphDb = Neo4jUtils.getReadOnlyGraphDb(srcNeo4j1Folder);
	        
	        System.out.println("Connecting to destination database");
	        dstGraphDb = Neo4jUtils.getGraphDb(dstNeo4j2Folder);
	        
	        GlobalGraphOperations global = Neo4jUtils.getGlobalOperations(srcGraphDb);
	        
	        long nodeCounter = 0;
	        long relCounter = 0;
	        	        
	        SOURCES.add(GraphUtils.SOURCE_DRYAD);
	        SOURCES.add(GraphUtils.SOURCE_ORCID);
	        SOURCES.add(GraphUtils.SOURCE_WEB);
	        SOURCES.add(GraphUtils.SOURCE_FIGSHARE);
	        SOURCES.add(GraphUtils.SOURCE_CROSSREF);
	        SOURCES.add(GraphUtils.SOURCE_ARC);
	        SOURCES.add(GraphUtils.SOURCE_NHMRC);
	        SOURCES.add(GraphUtils.SOURCE_ANDS);
	        SOURCES.add(GraphUtils.SOURCE_DARA);
	        SOURCES.add(GraphUtils.SOURCE_CERN);
	        SOURCES.add(GraphUtils.SOURCE_DLI);
	        	
	        // do not export institutions
	        //TYPES.add(GraphUtils.TYPE_INSTITUTION);
	        TYPES.add(GraphUtils.TYPE_PUBLICATION);
	        TYPES.add(GraphUtils.TYPE_RESEARCHER);
	        TYPES.add(GraphUtils.TYPE_DATASET);
	        TYPES.add(GraphUtils.TYPE_GRANT);
	        
/*	        mapKeys.put(GraphUtils.TYPE_INSTITUTION, new HashMap<Long, Long>());
	        mapKeys.put(GraphUtils.TYPE_PUBLICATION, new HashMap<Long, Long>());
	        mapKeys.put(GraphUtils.TYPE_RESEARCHER, new HashMap<Long, Long>());
	        mapKeys.put(GraphUtils.TYPE_DATASET, new HashMap<Long, Long>());
	        mapKeys.put(GraphUtils.TYPE_GRANT, new HashMap<Long, Long>());
 /	    	
	        
	        System.out.println("Create Indexes");
	        try ( Transaction tx = dstGraphDb.beginTx() ) {
		        for (String label : TYPES) { 
		        	Neo4jUtils.createConstrant(dstGraphDb, DynamicLabel.label(label), GraphUtils.PROPERTY_KEY);
		        }
		        for (String label : SOURCES) { 
		        	Neo4jUtils.createIndex(dstGraphDb, DynamicLabel.label(label), GraphUtils.PROPERTY_KEY);
		        }
		        
	        	tx.success();
	        }
	        
	        try ( Transaction tx = dstGraphDb.beginTx() ) {
		        for (String label : TYPES) { 
		        	mapIndexes.put(label, Neo4jUtils.getNodeIndex(dstGraphDb, label));
		        }
		        for (String label : SOURCES) { 
		        	mapIndexes.put(label, Neo4jUtils.getNodeIndex(dstGraphDb, label));
		        }
		        
	        	tx.success();
	        }
	        
	        System.out.println("Export all nodes with url's");
	        try ( Transaction ignored = srcGraphDb.beginTx() ) 
			{
	        	long chunkSize = 0;
	        	long chunkCnt = 0;
	        	Transaction tx = dstGraphDb.beginTx();
	        	try {
		        	ResourceIterable<Node> srcNodes = global.getAllNodes();
		        	for (Node srcNode : srcNodes) {
		        		if (exportNode(srcNode, null))
		        			++chunkSize;
		        
		        		if (chunkSize > 1000) {
        					
		        			nodeCounter += chunkSize;
        					chunkSize = 0;
        					++chunkCnt;

        					System.out.println("Writing " + chunkCnt + " nodes chunk to database");
        				
        					tx.success();
        					tx.close();
        					tx = dstGraphDb.beginTx();			        					
        				}
		        	}
		        	
		        	nodeCounter += chunkSize;
		        	tx.success();
	        	} finally {
	        		tx.close();
	        	}
	        	
	        	System.out.println("Copy Relationships");
	        	
	        	chunkSize = 0;
	        	chunkCnt = 0;
	        	tx = dstGraphDb.beginTx();
	        	try {
		        	ResourceIterable<Node> srcNodes = global.getAllNodes();
		        	for (Node srcNode : srcNodes) {
		        		chunkSize += exportRelationships(srcNode);
		        
		        		if (chunkSize > 1000) {
        					
		        			relCounter += chunkSize;
        					chunkSize = 0;
        					++chunkCnt;

        					System.out.println("Writing " + chunkCnt + " relationships chunk to database");
        				
        					tx.success();
        					tx.close();
        					tx = dstGraphDb.beginTx();			        					
        				}
		        	}
		        	
		        	nodeCounter += chunkSize;
		        	tx.success();
	        	} finally {
	        		tx.close();
	        	}
			}
	        
	        System.out.println("Done. Created " + nodeCounter + " nodes and " + relCounter + " relationships");
	        */
	      	
		} catch (Exception e) {
			e.printStackTrace();
			
			System.exit(1);
		}
	}
}
