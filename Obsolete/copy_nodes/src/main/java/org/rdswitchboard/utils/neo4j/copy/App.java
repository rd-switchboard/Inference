package org.rdswitchboard.utils.neo4j.copy;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.ResourceIterable;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.index.Index;
import org.neo4j.tooling.GlobalGraphOperations;
import org.rdswitchboard.libraries.graph.GraphUtils;
import org.rdswitchboard.libraries.neo4j.Neo4jUtils;

public class App {
	private static final String PROPERTIES_FILE = "properties/copy_nodes.properties";
	private static final String NEO4J1_FOLDER = "neo4j.old"; 
	private static final String NEO4J2_FOLDER = "neo4j";
	
	private static final Set<String> SOURCES = new HashSet<String>();
	private static final Set<String> TYPES = new HashSet<String>();
	private static final Map<String, Index<Node>> mapIndexes = new HashMap<String, Index<Node>>();
	
	private static GraphDatabaseService graphDb1;
	private static GraphDatabaseService graphDb2;
	
	private static final RelationshipType relKnownAs = DynamicRelationshipType.withName(GraphUtils.RELATIONSHIP_KNOWN_AS);
	
	public static void main(String[] args) {
		try {
			String propertiesFile = PROPERTIES_FILE;
	        if (args.length > 0 && !StringUtils.isEmpty(args[0])) 
	        	propertiesFile = args[0];
	
	        Properties properties = new Properties();
	        try (InputStream in = new FileInputStream(propertiesFile)) {
	            properties.load(in);
	        }
	        
	        System.out.println("Export Neo4j database");
	                	        
	        String neo4j1Folder = properties.getProperty("neo4j1", NEO4J1_FOLDER);
	        if (StringUtils.isEmpty(neo4j1Folder))
	            throw new IllegalArgumentException("Neo4j1 Folder can not be empty");
	        System.out.println("Source Neo4j Folder: " + neo4j1Folder);

	        String neo4j2Folder = properties.getProperty("neo4j2", NEO4J2_FOLDER);
	        if (StringUtils.isEmpty(neo4j2Folder))
	            throw new IllegalArgumentException("Neo4j2 Folder can not be empty");
	        System.out.println("Target Neo4j Folder: " + neo4j2Folder);
	        
	        System.out.println("Connecting to source database");
	        
	        graphDb1 = Neo4jUtils.getReadOnlyGraphDb(neo4j1Folder);
	        
	        System.out.println("Connecting to destination database");
	        graphDb2 = Neo4jUtils.getGraphDb(neo4j2Folder);
	        
	        GlobalGraphOperations global = Neo4jUtils.getGlobalOperations(graphDb1);
	        
	        long nodeCounter = 0;
	        long relCounter = 0;
	        	        
	        SOURCES.add(GraphUtils.SOURCE_DRYAD);
	        SOURCES.add(GraphUtils.SOURCE_ORCID);
	        SOURCES.add(GraphUtils.SOURCE_WEB);
	        SOURCES.add(GraphUtils.SOURCE_FIGSHARE);
	        SOURCES.add(GraphUtils.SOURCE_CROSSREF);
	        
	        
	        TYPES.add(GraphUtils.TYPE_INSTITUTION);
	        TYPES.add(GraphUtils.TYPE_PUBLICATION);
	        TYPES.add(GraphUtils.TYPE_RESEARCHER);
	        TYPES.add(GraphUtils.TYPE_DATASET);
	        TYPES.add(GraphUtils.TYPE_GRANT);
	        
	        System.out.println("Create Indexes");
	        try ( Transaction tx = graphDb2.beginTx() ) {
		        for (String label : SOURCES) { 
		        	Neo4jUtils.createConstrant(graphDb2, DynamicLabel.label(label), GraphUtils.PROPERTY_KEY);
		        }
		        
	        	tx.success();
	        }
	        
	        try ( Transaction tx = graphDb2.beginTx() ) {
		        for (String label : SOURCES) { 
		        	mapIndexes.put(label, Neo4jUtils.getNodeIndex(graphDb2, label));
		        }
		        
	        	tx.success();
	        }
	        
	        System.out.println("Copy Nodes");
	        try ( Transaction ignored = graphDb1.beginTx() ) 
			{
	        	long chunkSize = 0;
	        	long chunkCnt = 0;
	        	Transaction tx = graphDb2.beginTx();
	        	try {
		        	ResourceIterable<Node> srcNodes = global.getAllNodes();
		        	for (Node srcNode : srcNodes) {
		        		Object sources = srcNode.getProperty(GraphUtils.PROPERTY_SOURCE);
		        		if (sources instanceof String) {
		        			String source = (String) sources;
		        			Node dstNode = processNode(srcNode, source);
		        			if (null != dstNode) {
		        				++chunkSize;
		        			}
		        		} else if (sources instanceof String[]) {
		        			Map<Long, Node> map = new HashMap<Long, Node>();
		        			for (String source : (String[]) sources) {
		        				Node dstNode = processNode(srcNode, source);
		        				if (null != dstNode) {
		        					map.put(dstNode.getId(), dstNode);
		        					++chunkSize;
		        				}
		        				
		        				if (map.size() > 1) {
		        					for (Map.Entry<Long, Node> e1 : map.entrySet()) {
		        						for (Map.Entry<Long, Node> e2 : map.entrySet()) {
		        							if (e1.getKey() != e2.getKey())
		        								Neo4jUtils.createUniqueRelationship(e1.getValue(), e2.getValue(), relKnownAs, Direction.BOTH, null);
		        						}
		        					}		        						 
		        				}
			        		}
		        		} else 
		        			throw new Exception("Invalid source format");
		        		
		        		if (++chunkSize > 1000) {
        					
		        			nodeCounter += chunkSize;
        					chunkSize = 0;
        					++chunkCnt;

        					System.out.println("Writing " + chunkCnt + " nodes chunk to database");
        				
        					tx.success();
        					tx.close();
        					tx = graphDb2.beginTx();			        					
        				}
		        	}
		        	
		        	nodeCounter += chunkSize;
		        	tx.success();
	        	} finally {
	        		tx.close();
	        	}
			}
	        
	        System.out.println("Done. Created " + nodeCounter + " nodes");
	        
	        System.out.println("Copy Relationships");
	        try ( Transaction ignored = graphDb1.beginTx() ) 
			{
	        	long chunkSize = 0;
	        	long chunkCnt = 0;
	        	Transaction tx = graphDb2.beginTx();
	        	try {
		        	ResourceIterable<Node> srcNodes = global.getAllNodes();
		        	for (Node srcNode1 : srcNodes) {
		        		Object sources = srcNode1.getProperty(GraphUtils.PROPERTY_SOURCE);
		        		if (sources instanceof String) {
		        			chunkSize += processRelationships(srcNode1, (String) sources);
		        		} else if (sources instanceof String[]) {
		        			Map<Long, Node> map = new HashMap<Long, Node>();
		        			for (String source : (String[]) sources) {
		        				chunkSize += processRelationships(srcNode1, source);
			        		}
		        		} else 
		        			throw new Exception("Invalid source format");
		        		
		        		if (++chunkSize > 1000) {
        					
		        			relCounter += chunkSize;
        					chunkSize = 0;
        					++chunkCnt;

        					System.out.println("Writing " + chunkCnt + " nodes relationships to database");
        				
        					tx.success();
        					tx.close();
        					tx = graphDb2.beginTx();			        					
        				}
		        	}
		        	
		        	relCounter += chunkSize;
		        	tx.success();
	        	} finally {
	        		tx.close();
	        	}				
			}
	        
	        System.out.println("Done. Created " + nodeCounter + " and " + relCounter + " relationships");
	        	
		} catch (Exception e) {
			e.printStackTrace();
		}	
	}
	
	public static Node processNode(Node srcNode, String source) {
		if (!SOURCES.contains(source))
			return null;
		
		String type = (String) srcNode.getProperty(GraphUtils.PROPERTY_TYPE);
		if (!TYPES.contains(type))
			return null;
		
		Label lSource = DynamicLabel.label(source);
		Object key = srcNode.getProperty(GraphUtils.PROPERTY_KEY);
		
		Node dstNode = findSingleNode(lSource, key);
		
		if (dstNode == null) {
			dstNode = graphDb2.createNode();
			dstNode.setProperty(GraphUtils.PROPERTY_KEY, key);
			
			for (String property : srcNode.getPropertyKeys()) {
				if (!property.equals(GraphUtils.PROPERTY_KEY) 
						&& !property.equals(GraphUtils.PROPERTY_SOURCE)
						&& !property.equals(GraphUtils.PROPERTY_TYPE))
					dstNode.setProperty(property, srcNode.getProperty(property));
			}
			
			mapIndexes.get(source).add(dstNode, GraphUtils.PROPERTY_KEY, key);
		}

		dstNode.setProperty(GraphUtils.PROPERTY_SOURCE, source);
		dstNode.setProperty(GraphUtils.PROPERTY_TYPE, type);

		dstNode.addLabel(lSource);
		dstNode.addLabel(DynamicLabel.label(type));
				
		return dstNode;
	}
	
	public static int processRelationships(Node srcNode1, String source1) throws Exception {
		Label lSource1 = DynamicLabel.label(source1);
		Object key1 = srcNode1.getProperty(GraphUtils.PROPERTY_KEY);
		int counter = 0;
		
		Node dstNode1 = findSingleNode(lSource1, key1);
		if (null != dstNode1) {
			Iterable<Relationship> relationships = srcNode1.getRelationships(Direction.OUTGOING);
			for (Relationship relationship : relationships) {
				Node srcNode2 = relationship.getOtherNode(srcNode1);
				Object sources = srcNode2.getProperty(GraphUtils.PROPERTY_SOURCE);
        		if (sources instanceof String) {
        			if (processRelationships(dstNode1, srcNode2, (String) sources, relationship.getType()))
        				++counter;
        		} else if (sources instanceof String[]) {
        			for (String source : (String[]) sources) {
        				if (processRelationships(dstNode1, srcNode2, source, relationship.getType()))
            				++counter;
	        		}
        		} else 
        			throw new Exception("Invalid source format");
			}
			
		}
		
		return counter;		
	}
	
	public static boolean processRelationships(Node dstNode1, Node srcNode2, String source, RelationshipType type) {
		Label lSource2 = DynamicLabel.label(source);
		Object key2 = srcNode2.getProperty(GraphUtils.PROPERTY_KEY);
		
		Node dstNode2 = findSingleNode(lSource2, key2);
		if (null != dstNode2) { 
			dstNode1.createRelationshipTo(dstNode2, type);
			
			return true;
		}
		
		return false;
	}
	
	/*public static Label getNodeLabel(Node node, List<Label> labels) {
		for (Label label : labels) 
			if (node.hasLabel(label))
				return label;
		
		return null;
	}
	
	public static boolean isNodeExists(GraphDatabaseService graphDb, Label label, Object key) {
		try (ResourceIterator<Node> hits = graphDb.findNodes(label, GraphUtils.PROPERTY_KEY, key)) {
			return hits.hasNext();
		}
	}*/
	
	public static Node findSingleNode(Label label, Object key) {
		try (ResourceIterator<Node> hits = graphDb2.findNodes(label, GraphUtils.PROPERTY_KEY, key)) {
			if (hits.hasNext())
				return hits.next();
		}
		
		return null;
	}
}