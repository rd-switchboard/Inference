package org.rdswitchboard.utils.neo4j.copy.harmonized;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.ResourceIterable;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.index.Index;
import org.neo4j.tooling.GlobalGraphOperations;
import org.rdswitchboard.libraries.configuration.Configuration;
import org.rdswitchboard.libraries.graph.GraphUtils;
import org.rdswitchboard.libraries.neo4j.Neo4jUtils;

public class App {	
	private static final Set<String> SOURCES = new HashSet<String>();
	private static final Set<String> TYPES = new HashSet<String>();
	private static final Map<String, Index<Node>> mapIndexes = new HashMap<String, Index<Node>>();
	private static final Map<Long, Long> mapKeys = new HashMap<Long, Long>();
	
	private static GraphDatabaseService srcGraphDb;
	private static GraphDatabaseService dstGraphDb;
	
	private static final RelationshipType relKnownAs = DynamicRelationshipType.withName(GraphUtils.RELATIONSHIP_KNOWN_AS);
	private static final RelationshipType relRelatedTo = DynamicRelationshipType.withName(GraphUtils.RELATIONSHIP_RELATED_TO);
	
	public static void main(String[] args) {
		try {
			Properties properties = Configuration.fromArgs(args);
	        
	        System.out.println("Export Neo4j database");
	                	        
	        String srcNeo4j1Folder = properties.getProperty(Configuration.PROPERTY_NEO4J);
	        if (StringUtils.isEmpty(srcNeo4j1Folder))
	            throw new IllegalArgumentException("Neo4j1 Folder can not be empty");
	        System.out.println("Source Neo4j Folder: " + srcNeo4j1Folder);

	        String dstNeo4j2Folder = properties.getProperty("target.neo4j");
	        if (StringUtils.isEmpty(dstNeo4j2Folder))
	            throw new IllegalArgumentException("Neo4j2 Folder can not be empty");
	        System.out.println("Target Neo4j Folder: " + dstNeo4j2Folder);
	        
	        System.out.println("Connecting to source database");
	        
	        srcGraphDb = Neo4jUtils.getReadOnlyGraphDb(srcNeo4j1Folder);
	        
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
*/	    	
	        
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
	        
	      	
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static Node findNode(String type, String key) {
		try (ResourceIterator<Node> hits = dstGraphDb.findNodes(DynamicLabel.label(type), GraphUtils.PROPERTY_KEY, key)) {
			if (hits.hasNext())
				return hits.next();
		}
		
		return null;
	}
	
	protected static Object getValue(Set<Object> set) {
		if (null == set)
			return null;
		int size = set.size();
		if (0 == size)
			return null;
		
		Object element = set.iterator().next();
		if (1 == size)
			return element;
		if (element instanceof Boolean)
			return set.toArray(new Boolean[size]);
		if (element instanceof Byte)
			return set.toArray(new Byte[size]);
		if (element instanceof Short)
			return set.toArray(new Short[size]);
		if (element instanceof Integer)
			return set.toArray(new Integer[size]);
		if (element instanceof Long)
			return set.toArray(new Long[size]);
		if (element instanceof Float)
			return set.toArray(new Float[size]);
		if (element instanceof Double)
			return set.toArray(new Double[size]);
		if (element instanceof String)
			return set.toArray(new String[size]);
		
		throw new ClassCastException("Unable to convert Property Array, they property type: " + element.getClass() + " is not supported");
	}
	
	protected static void addPropertyToSet(Set<Object> set, Object value) {
		if (value instanceof String[]) 
			set.addAll(Arrays.asList((String[]) value));
		else if (value instanceof Boolean[])
			set.addAll(Arrays.asList((Boolean[]) value));
		else if (value instanceof Byte[])
			set.addAll(Arrays.asList((Byte[]) value));
		else if (value instanceof Short[])
			set.addAll(Arrays.asList((Short[]) value));
		else if (value instanceof Integer[])
			set.addAll(Arrays.asList((Integer[]) value));
		else if (value instanceof Long[])
			set.addAll(Arrays.asList((Long[]) value));
		else if (value instanceof Float[])
			set.addAll(Arrays.asList((Float[]) value));
		else if (value instanceof Double[])
			set.addAll(Arrays.asList((Double[]) value));
		/*else if (value instanceof Object[])
			set.addAll(Arrays.asList((Object[]) value));*/
		else if (value instanceof Collection<?>)
			set.addAll((Collection<?>) value);
		else if (value instanceof Map<?,?>)
			throw new IllegalArgumentException("Maps as Parameters are not supported");
		else if (value.getClass().isArray())
			throw new IllegalArgumentException("Array myst be of Primitive type");
		else
			set.add(value);
	}
	
	private static void addProperty(Node node, String key, Object value) {
		if (node.hasProperty(key)) {
			Set<Object> set = new HashSet<Object>();
			addPropertyToSet(set, node.getProperty(key));
			addPropertyToSet(set, value);
			
			node.setProperty(key, getValue(set));
		} else
			node.setProperty(key, value);
	}
	
	private static boolean exportNode(Node srcNode, String key) throws Exception {
		// a simple check to see if we already have this node
		long nId = srcNode.getId();
		if (mapKeys.containsKey(nId))
			return false;

		//System.out.println("Exporting node");
		// try and extract existsing node url
		String url = null; 
		if (srcNode.hasProperty(GraphUtils.PROPERTY_URL)) {
			Object _url = srcNode.getProperty(GraphUtils.PROPERTY_URL);
			if (_url instanceof String) 
				url = GraphUtils.extractFormalizedUrl((String) _url);
			 else {
		//		 System.out.println("Node URL is invalid: " + _url);
				 return false; // double url
			 }
		} 

		// if we have found an url, check could it become this node key or it is equals to an existing key
		if (null != url)
		{
			if (null == key) {
				// The node does not have key, this must be first export of the node. 
				// check if this is an orphant node
				if (isOrphantNode(srcNode, null))
					return false;
				
				// assing the url as a key
				key = url;
			} else if (!key.equals(url)) {
				// This node already have the key, check what key is same as this URL
				return false;
			}
		}
		
		// check if we have valid key for the node
		if (null == key) {
		//	System.out.println("Node does not have a key");
			return false;
		}
		
		// extract node source and type
		String source = (String) srcNode.getProperty(GraphUtils.PROPERTY_SOURCE);
		if (!SOURCES.contains(source)) {
		//	System.out.println("Node source (" + source + ") is ignored");
			return false; // the node has wrong source
		}
		String type = (String) srcNode.getProperty(GraphUtils.PROPERTY_TYPE);
		if (!TYPES.contains(type)) {
		//	System.out.println("Node type (" + type + ") is ignored");
			return false; // the node has wrong type
		}
		
		// try and find existing node with this type and key
		Node dstNode = findNode(type, key);
		if (null == dstNode) {
		//	System.out.println("Creating new node");
			// the node does not exists, create it now
			dstNode = dstGraphDb.createNode(DynamicLabel.label(type));
			dstNode.setProperty(GraphUtils.PROPERTY_KEY, key);
			dstNode.setProperty(GraphUtils.PROPERTY_TYPE, type);
			
			// add it to legacy index
			mapIndexes.get(type).add(dstNode, GraphUtils.PROPERTY_KEY, key);
		} /*else
			System.out.println("Use existing node");*/

		// add node to keys map
		mapKeys.put(nId, dstNode.getId());

		// add source label to the node
		dstNode.addLabel(DynamicLabel.label(source));				
		
		// add node source
		addProperty(dstNode, GraphUtils.PROPERTY_SOURCE, source);
		// add node orginal key
//		String originalKey = source + ":" + (String) srcNode.getProperty(GraphUtils.PROPERTY_KEY);
		addProperty(dstNode, GraphUtils.PROPERTY_ORIGINAL_KEY, srcNode.getProperty(GraphUtils.PROPERTY_KEY));
		
		// copy all non existing properties, except key, url, source and type
		// ignore referenced_by property
		for (String property : srcNode.getPropertyKeys()) {
			if (!dstNode.hasProperty(property) 
					&& !property.equals(GraphUtils.PROPERTY_URL)
					&& !property.equals(GraphUtils.PROPERTY_KEY)
					&& !property.equals(GraphUtils.PROPERTY_SOURCE)
					&& !property.equals(GraphUtils.PROPERTY_TYPE)
					&& !property.equals(GraphUtils.PROPERTY_REFERENCED_BY)) {
				dstNode.setProperty(property, srcNode.getProperty(property));
			}
		}
		
		// process all knownAs relationships
		Iterable<Relationship> rels = srcNode.getRelationships(relKnownAs);
		for (Relationship rel : rels) {
			// find node sitting on other end of relationship
			Node other = rel.getOtherNode(srcNode);
			// check what we haven't seen that node before
			if (!mapKeys.containsKey(other.getId())) 
				exportNode(other, key);
		}
		
	//	System.out.println("Node export completed");
		
		return true;
	}
		
	private static boolean isOrphantNode(Node node, Set<Long> knownAs) {
		boolean added = false;
		
		Iterable<Relationship> rels = node.getRelationships();
		for (Relationship rel : rels) {
			if (!rel.getType().name().equals(GraphUtils.RELATIONSHIP_KNOWN_AS))
				return false;
			
			if (!added) {
				if (null == knownAs)
					knownAs = new HashSet<Long>();
				
				knownAs.add(node.getId());
				added = true;
			}
			
			Node other = rel.getOtherNode(node);
			if (!knownAs.contains(other.getId()) && !isOrphantNode(other, knownAs))
				return false;
		}
		
		return true;
	}
	
	private static int exportRelationships(Node srcNode) throws Exception {
		int counter = 0;
		Long startId = mapKeys.get(srcNode.getId());
		if (null != startId) {
			Iterable<Relationship> rels = srcNode.getRelationships(Direction.OUTGOING);
			for (Relationship rel : rels) {
				RelationshipType type = rel.getType();
				String name = type.name();
				if (!name.equals(GraphUtils.RELATIONSHIP_KNOWN_AS))
					type = relRelatedTo;
				
				Long endId = mapKeys.get(rel.getEndNode().getId());
				if (null != endId && !startId.equals(endId)) {
					Node startNode = dstGraphDb.getNodeById(startId);
					boolean exists = false;
					
					Iterable<Relationship> rels2 = startNode.getRelationships();
					for (Relationship rel2 : rels2) {
						if (rel2.getOtherNode(startNode).getId() == endId) {
							exists = true;
							break;
						}							
					}
					
					if (!exists) {
						Node endNode = dstGraphDb.getNodeById(endId);
						startNode.createRelationshipTo(endNode, type);
						
						++counter;
					}
				}			
			}
		}
		
		return counter;
	}
}