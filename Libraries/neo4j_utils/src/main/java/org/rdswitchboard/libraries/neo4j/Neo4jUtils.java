package org.rdswitchboard.libraries.neo4j;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
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
import org.neo4j.tooling.GlobalGraphOperations;

/**
 * Neo4jUtils class
 * 
 * @author Dima Kudriavcev
 * 
 * The class is depricated, please use Neo4jDatabase instead
 */

public class Neo4jUtils {
	public static final String NEO4J_CONF = "/conf/neo4j.properties";
	public static final String NEO4J_DB = "/data/graph.db";
	
	public static File GetDbPath(final String folder) throws Neo4jException, IOException
	{
		File db = new File(folder, NEO4J_DB);
		if (!db.exists())
			db.mkdirs();
				
		if (!db.isDirectory())
			throw new Neo4jException("The " + folder + " folder is not valid Neo4j instance. Please provide path to an existing Neo4j instance");
		
		return db;
	}
	
	public static File GetConfPath(final String folder) throws Neo4jException
	{
		File conf = new File(folder, NEO4J_CONF);
		if (!conf.exists() || conf.isDirectory())
			throw new Neo4jException("The " + folder + " folder is not valid Neo4j instance. Please provide path to an existing Neo4j instance");
		
		return conf;
	}	
	
	public static GraphDatabaseService getReadOnlyGraphDb( final String graphDbPath ) throws Neo4jException {
		if (StringUtils.isEmpty(graphDbPath))
			throw new Neo4jException("Please provide path to an existing Neo4j instance");
		
		try {
			GraphDatabaseService graphDb = new GraphDatabaseFactory()
				.newEmbeddedDatabaseBuilder( GetDbPath(graphDbPath).toString() )
				.loadPropertiesFromFile( GetConfPath(graphDbPath).toString() )
				.setConfig( GraphDatabaseSettings.read_only, "true" )
				.newGraphDatabase();
			
			registerShutdownHook( graphDb );
			
			return graphDb;
		} catch (Exception e) {
			throw new Neo4jException("Unable to open Neo4j instance located at: " + graphDbPath + ". Error: " + e.getMessage());
		}
	}
	
	public static GraphDatabaseService getGraphDb( final String graphDbPath ) throws Neo4jException {
		if (StringUtils.isEmpty(graphDbPath))
			throw new Neo4jException("Please provide path to an existing Neo4j instance");
		
		try {
			GraphDatabaseService graphDb = new GraphDatabaseFactory()
				.newEmbeddedDatabaseBuilder( GetDbPath(graphDbPath).toString() )
				.loadPropertiesFromFile( GetConfPath(graphDbPath).toString() )
				.newGraphDatabase();
		
			registerShutdownHook( graphDb );
		
			return graphDb;
		} catch (Exception e) {
			throw new Neo4jException("Unable to open Neo4j instance located at: " + graphDbPath + ". Error: " + e.getMessage());
		}
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
	    });
	}
	
	@Deprecated
	public static ConstraintDefinition createConstrant(final GraphDatabaseService graphDb, 
			final Label label, final String key) {
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
	
	@Deprecated
	public static ConstraintDefinition createConstrant(final GraphDatabaseService graphDb, 
			final String label, final String key) {
		return createConstrant(graphDb, DynamicLabel.label(label), key);
	}
	
	@Deprecated
	public static GlobalGraphOperations getGlobalOperations(final GraphDatabaseService graphDb) {
		return GlobalGraphOperations.at(graphDb);
	}
	
	@Deprecated
	public static IndexDefinition createIndex(final GraphDatabaseService graphDb, 
			final Label label, final String key) {
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
	
	@Deprecated
	public static IndexDefinition createIndex(final GraphDatabaseService graphDb, 
			final String label, final String key) {
		return createIndex(graphDb, DynamicLabel.label(label), key);
	}
	
	@Deprecated
	public static Index<Node> getNodeIndex(final GraphDatabaseService graphDb, final String label) {
		return graphDb.index().forNodes( label );
	}
	
	@Deprecated
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
	
	@Deprecated
	public static Relationship findRelationship(Node nodeStart, long nodeId, 
			RelationshipType type, Direction direction) {
		return findRelationship(nodeStart.getRelationships(type, direction), nodeId, direction);
	}
	
	@Deprecated
	public static Relationship findRelationship(Node nodeStart, Node endNode, 
			RelationshipType type, Direction direction) {
		return findRelationship(nodeStart, endNode.getId(), type, direction);
	}
	
	@Deprecated
	public static void addLabel(Node node, Label label) {
		node.addLabel(label);
	}

	@Deprecated
	public static void addLabels(Node node, Label[] labels) {
		for (Label label : labels)
			node.addLabel(label);
	}
	
	@Deprecated
	public static void setProperties(Node node, Map<String, Object> properties) {
		for (Map.Entry<String, Object> entry : properties.entrySet())
			node.setProperty(entry.getKey(), entry.getValue());
	}
	
	@Deprecated
	public static void setProperties(Relationship relationship, Map<String, Object> properties) {
		for (Map.Entry<String, Object> entry : properties.entrySet())
			relationship.setProperty(entry.getKey(), entry.getValue());
	}
	
	@Deprecated
	public static Node createNode(GraphDatabaseService graphDb) {
		return graphDb.createNode();
	}
	
	@Deprecated
	public static Node createNode(GraphDatabaseService graphDb, Label label) {
		Node node = graphDb.createNode();
		addLabel(node, label);
		return node;
	}

	@Deprecated
	public static Node createNode(GraphDatabaseService graphDb, Label[] labels) {
		Node node = graphDb.createNode();
		addLabels(node, labels);
		return node;
	}
	
	@Deprecated
	public static Node createNode(GraphDatabaseService graphDb, Map<String, Object> properties) {
		Node node = graphDb.createNode();
		if (null != properties)
			setProperties(node, properties);
		return node;
	}
	
	@Deprecated
	public static Node createNode(GraphDatabaseService graphDb, Label label, Map<String, Object> properties) {
		Node node = graphDb.createNode();
		addLabel(node, label);
		if (null != properties)
			setProperties(node, properties);
		return node;
	}

	@Deprecated
	public static Node createNode(GraphDatabaseService graphDb, Label[] labels, Map<String, Object> properties) {
		Node node = graphDb.createNode();
		addLabels(node, labels);
		if (null != properties)
			setProperties(node, properties);
		return node;
	}
	
	@Deprecated
	public static Node createUniqueNode(GraphDatabaseService graphDb, Index<Node> index, String key, String value) {
		Node node = index.get(key, value).getSingle();
		if (null == node) {
			node = createNode(graphDb);
			
			index.add(node, key, value);
		}
		
		return node;
	}

	@Deprecated
	public static Node createUniqueNode(GraphDatabaseService graphDb, Index<Node> index, String key, String value, 
			Label label) {
		Node node = index.get(key, value).getSingle();
		if (null == node) {
			node = createNode(graphDb, label);
			
			index.add(node, key, value);
		}
		
		return node;
	}
	
	@Deprecated
	public static Node createUniqueNode(GraphDatabaseService graphDb, Index<Node> index, String key, String value, 
			Label[] labels) {
		Node node = index.get(key, value).getSingle();
		if (null == node) {
			node = createNode(graphDb, labels);
			
			index.add(node, key, value);
		}
		
		return node;
	}

	@Deprecated
	public static Node createUniqueNode(GraphDatabaseService graphDb, Index<Node> index, String key, String value, 
			Map<String, Object> properties) {
		Node node = index.get(key, value).getSingle();
		if (null == node) {
			node = createNode(graphDb, properties);
			
			index.add(node, key, value);
		}
		
		return node;
	}

	@Deprecated
	public static Node createUniqueNode(GraphDatabaseService graphDb, Index<Node> index, String key, String value, 
			Label label, Map<String, Object> properties) {
		Node node = index.get(key, value).getSingle();
		if (null == node) {
			node = createNode(graphDb, label, properties);
			
			index.add(node, key, value);
		}
		
		return node;
	}
	
	@Deprecated
	public static Node createUniqueNode(GraphDatabaseService graphDb, Index<Node> index, String key, String value, 
			Label[] labels, Map<String, Object> properties) {
		Node node = index.get(key, value).getSingle();
		if (null == node) {
			node = createNode(graphDb, labels, properties);
			
			index.add(node, key, value);
		}
		
		return node;
	}

	@Deprecated
	public static Relationship createRelationship(Node nodeStart, Node nodeEnd, RelationshipType type) {
		return nodeStart.createRelationshipTo(nodeEnd, type);
	}
	
	@Deprecated
	public static Relationship createRelationship(Node nodeStart, Node nodeEnd, RelationshipType type, 
			Map<String, Object> properties) {
		Relationship relationship = createRelationship(nodeStart, nodeEnd, type);
		if (null != properties)
			setProperties(relationship, properties);
	
		return relationship;
	}	
	
	@Deprecated
	public static Relationship createUniqueRelationship(Node nodeStart, Node nodeEnd, RelationshipType type, 
			Direction direction, Map<String, Object> properties) {

		Relationship relationship = findRelationship(nodeStart, nodeEnd, type, direction);
		if (null == relationship)
			return createRelationship(nodeStart, nodeEnd, type, properties);

		return relationship;
	}
		
	@Deprecated
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
