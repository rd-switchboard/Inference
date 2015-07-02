package org.rdswitchboard.utils.neo4j.local;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.neo4j.cypher.javacompat.ExecutionEngine;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.factory.GraphDatabaseSettings;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.schema.ConstraintDefinition;
import org.neo4j.graphdb.schema.IndexDefinition;
import org.neo4j.graphdb.schema.Schema;
import org.neo4j.kernel.impl.util.StringLogger;
import org.neo4j.tooling.GlobalGraphOperations;

public class Neo4jUtils {
	public static final String NEO4J_CONF = "/conf/neo4j.properties";
	public static final String NEO4J_DB = "/data/graph.db";
	
	public static GraphDatabaseService getReadOnlyGraphDb( final String graphDbPath ) {
		GraphDatabaseService graphDb = new GraphDatabaseFactory()
				.newEmbeddedDatabaseBuilder( graphDbPath + NEO4J_DB )
				.loadPropertiesFromFile( graphDbPath + NEO4J_CONF )
				.setConfig( GraphDatabaseSettings.read_only, "true" )
				.newGraphDatabase();
		
		registerShutdownHook( graphDb );
		
		return graphDb;
	}
	
	public static GraphDatabaseService getGraphDb( final String graphDbPath ) {
		GraphDatabaseService graphDb = new GraphDatabaseFactory()
			.newEmbeddedDatabaseBuilder( graphDbPath + NEO4J_DB )
			.loadPropertiesFromFile( graphDbPath + NEO4J_CONF )
			.newGraphDatabase();
		
		registerShutdownHook( graphDb );
		
		return graphDb;
	}
	
	public static GlobalGraphOperations getGlobalOperations( final GraphDatabaseService graphDb ) {
		return GlobalGraphOperations.at(graphDb);
	}
	
	public static ExecutionEngine getExecutionEngine( final GraphDatabaseService graphDb ) {
		return new ExecutionEngine(graphDb, StringLogger.SYSTEM);
	}
	
	public static void registerShutdownHook( final GraphDatabaseService graphDb )
	{
	    // Registers a shutdown hook for the Neo4j instance so that it
	    // shuts down nicely when the VM exits (even if you "Ctrl-C" the
	    // running application).
	    Runtime.getRuntime().addShutdownHook( new Thread()
	    {
	        @Override
	        public void run()
	        {
	            graphDb.shutdown();
	        }
	    } );
	}
			
	public static Map<String, Object> getProperties(Node node) {
		Iterable<String> keys = node.getPropertyKeys();
		Map<String, Object> pars = null;
		
		for (String key : keys) {
			if (null == pars)
				pars = new HashMap<String, Object>();
			
			pars.put(key, node.getProperty(key));
		}
		
		return pars;
	}
	
	public static Map<String, Object> getProperties(Relationship relationship) {
		Iterable<String> keys = relationship.getPropertyKeys();
		Map<String, Object> pars = null;
		
		for (String key : keys) {
			if (null == pars)
				pars = new HashMap<String, Object>();
			
			pars.put(key, relationship.getProperty(key));
		}
		
		return pars;
	}
	
	public static ConstraintDefinition createConstrant(GraphDatabaseService graphDb, Label label, String key) {
		Schema schema = graphDb.schema();
		
		for (ConstraintDefinition constraint : schema.getConstraints(label))
			for (String property : constraint.getPropertyKeys())
				if (property.equals(key))
					return constraint;  // already existing
			
		return schema
				.constraintFor(label)
				.assertPropertyIsUnique(key)
				.create();
	}
	
	public static ConstraintDefinition createConstrant(GraphDatabaseService graphDb, String label, String key) {
		return createConstrant(graphDb, DynamicLabel.label(label), key);
	}

	public static IndexDefinition createIndex(GraphDatabaseService graphDb, Label label, String key) {
		Schema schema = graphDb.schema();
		
		for (IndexDefinition index : schema.getIndexes(label))
			for (String property : index.getPropertyKeys())
				if (property.equals(key))
					return index;  // already existing
			
		return schema
				.indexFor(label)
				.on(key)
				.create();
	}
	
	public static IndexDefinition createIndex(GraphDatabaseService graphDb, String label, String key) {
		return createIndex(graphDb, DynamicLabel.label(label), key);
	}

	public static Index<Node> getNodeIndex(GraphDatabaseService graphDb, String label) {
		return graphDb.index().forNodes( label );			
	}
	
	public static Node findNode(Index<Node> index, String key, String value) {
		 return index.get(key, value).getSingle();
	}
	
	public static Relationship findRelationship(Iterable<Relationship> rels, long nodeId, Direction direction) {
		for (Relationship rel : rels) {
			switch (direction) {
			case INCOMING:
				if (rel.getStartNode().getId() == nodeId)
					return rel;
				break;
			case OUTGOING:
				if (rel.getEndNode().getId() == nodeId)
					return rel;
				
			case BOTH:
				if (rel.getStartNode().getId() == nodeId || 
				    rel.getEndNode().getId() == nodeId)
					return rel;
			}
		}
		
		return null;
	}
	
	public static Relationship findRelationship(Node nodeStart, long nodeId, 
			RelationshipType type, Direction direction) {
		return findRelationship(nodeStart.getRelationships(type, direction), nodeId, direction);
	}
	
	public static Relationship findRelationship(Node nodeStart, Node endNode, 
			RelationshipType type, Direction direction) {
		return findRelationship(nodeStart, endNode.getId(), type, direction);
	}
	
	public static void addLabel(Node node, Label label) {
		node.addLabel(label);
	}

	public static void addLabels(Node node, Label[] labels) {
		for (Label label : labels)
			node.addLabel(label);
	}
	
	public static void setProperties(Node node, Map<String, Object> properties) {
		for (Entry<String, Object> entry : properties.entrySet())
			node.setProperty(entry.getKey(), entry.getValue());
	}
	
	public static void setProperties(Relationship relationship, Map<String, Object> properties) {
		for (Entry<String, Object> entry : properties.entrySet())
			relationship.setProperty(entry.getKey(), entry.getValue());
	}
	
	public static Node createNode(GraphDatabaseService graphDb) {
		return graphDb.createNode();
	}
	
	public static Node createNode(GraphDatabaseService graphDb, Label label) {
		Node node = graphDb.createNode();
		addLabel(node, label);
		return node;
	}

	public static Node createNode(GraphDatabaseService graphDb, Label[] labels) {
		Node node = graphDb.createNode();
		addLabels(node, labels);
		return node;
	}
	
	public static Node createNode(GraphDatabaseService graphDb, Map<String, Object> properties) {
		Node node = graphDb.createNode();
		if (null != properties)
			setProperties(node, properties);
		return node;
	}
	
	public static Node createNode(GraphDatabaseService graphDb, Label label, Map<String, Object> properties) {
		Node node = graphDb.createNode();
		addLabel(node, label);
		if (null != properties)
			setProperties(node, properties);
		return node;
	}

	public static Node createNode(GraphDatabaseService graphDb, Label[] labels, Map<String, Object> properties) {
		Node node = graphDb.createNode();
		addLabels(node, labels);
		if (null != properties)
			setProperties(node, properties);
		return node;
	}
	
	public static Node createUniqueNode(GraphDatabaseService graphDb, Index<Node> index, String key, String value) {
		Node node = index.get(key, value).getSingle();
		if (null == node) {
			node = createNode(graphDb);
			
			index.add(node, key, value);
		}
		
		return node;
	}

	public static Node createUniqueNode(GraphDatabaseService graphDb, Index<Node> index, String key, String value, 
			Label label) {
		Node node = index.get(key, value).getSingle();
		if (null == node) {
			node = createNode(graphDb, label);
			
			index.add(node, key, value);
		}
		
		return node;
	}
	
	public static Node createUniqueNode(GraphDatabaseService graphDb, Index<Node> index, String key, String value, 
			Label[] labels) {
		Node node = index.get(key, value).getSingle();
		if (null == node) {
			node = createNode(graphDb, labels);
			
			index.add(node, key, value);
		}
		
		return node;
	}

	public static Node createUniqueNode(GraphDatabaseService graphDb, Index<Node> index, String key, String value, 
			Map<String, Object> properties) {
		Node node = index.get(key, value).getSingle();
		if (null == node) {
			node = createNode(graphDb, properties);
			
			index.add(node, key, value);
		}
		
		return node;
	}

	public static Node createUniqueNode(GraphDatabaseService graphDb, Index<Node> index, String key, String value, 
			Label label, Map<String, Object> properties) {
		Node node = index.get(key, value).getSingle();
		if (null == node) {
			node = createNode(graphDb, label, properties);
			
			index.add(node, key, value);
		}
		
		return node;
	}
	
	public static Node createUniqueNode(GraphDatabaseService graphDb, Index<Node> index, String key, String value, 
			Label[] labels, Map<String, Object> properties) {
		Node node = index.get(key, value).getSingle();
		if (null == node) {
			node = createNode(graphDb, labels, properties);
			
			index.add(node, key, value);
		}
		
		return node;
	}

	public static Relationship createRelationship(Node nodeStart, Node nodeEnd, RelationshipType type) {
		return nodeStart.createRelationshipTo(nodeEnd, type);
	}
	
	public static Relationship createRelationship(Node nodeStart, Node nodeEnd, RelationshipType type, 
			Map<String, Object> properties) {
		Relationship relationship = createRelationship(nodeStart, nodeEnd, type);
		if (null != properties)
			setProperties(relationship, properties);
	
		return relationship;
	}	
	
	public static Relationship createUniqueRelationship(Node nodeStart, Node nodeEnd, RelationshipType type, 
			Direction direction, Map<String, Object> properties) {

		Relationship relationship = findRelationship(nodeStart, nodeEnd, type, direction);
		if (null == relationship)
			return createRelationship(nodeStart, nodeEnd, type, properties);

		return relationship;
	}
		
	public static Relationship mergeRelationship(Node nodeStart, Node nodeEnd, RelationshipType type, 
			Direction direction, Map<String, Object> properties) {

		Relationship relationship = findRelationship(nodeStart, nodeEnd, type, direction);
		if (null == relationship) 
			relationship = createRelationship(nodeStart, nodeEnd, type);
		if (null != properties)
			setProperties(relationship, properties);
		return relationship;
	}
}
