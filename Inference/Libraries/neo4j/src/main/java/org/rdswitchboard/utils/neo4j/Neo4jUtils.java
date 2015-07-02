package org.rdswitchboard.utils.neo4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.rest.graphdb.RestAPI;
import org.neo4j.rest.graphdb.batch.CypherResult;
import org.neo4j.rest.graphdb.entity.RestNode;
import org.neo4j.rest.graphdb.entity.RestRelationship;
import org.neo4j.rest.graphdb.index.RestIndex;
import org.neo4j.rest.graphdb.query.RestCypherQueryEngine;
import org.neo4j.rest.graphdb.util.QueryResult;

public class Neo4jUtils {
		
	public static final String PROPERTY_KEY = "key";
	public static final String PROPERTY_URL = "url";

	public static final String PROPERTY_NODE_TYPE = "node_type";
	public static final String PROPERTY_NODE_SOURCE = "node_source";
	
	public static String combineLabel(Label label1, Label label2) {
		return label1.name() + "_" + label2.name();
	}
	
	public static String doubleLabel(Label label1, Label label2) {
		return label1.name() + ":" + label2.name();
	}
	
	public static void createConstraint(RestCypherQueryEngine engine, String label, String key) {
		engine.query("CREATE CONSTRAINT ON (n:" +  label + ") ASSERT n." + key + " IS UNIQUE", null);
	}

	public static void createConstraint(RestCypherQueryEngine engine, Label label) {
		createConstraint(engine, label.name(), PROPERTY_KEY);
	}
	
	public static void createConstraint(RestCypherQueryEngine engine, Label label1, Label label2) {
		createConstraint(engine, combineLabel(label1, label2), PROPERTY_KEY);
	}
		
	public static void createIndex(RestCypherQueryEngine engine, String label, String key) {
		engine.query("CREATE INDEX ON :"+ label + "(" + key + ")", null);
	}

	public static void createIndex(RestCypherQueryEngine engine, Label label, String key) {
		createIndex(engine, label.name(), key);
	}
	
	/*
	public static void createIndex(RestCypherQueryEngine engine, Label labelSource, Label labelType, String key) {
		createIndex(engine, combineLabel(labelSource, labelType), key);
	}*/	
	
	public static void createIndex(RestCypherQueryEngine engine, Label label) {
		createIndex(engine, label.name(), PROPERTY_KEY);
	}
	
	/*
	public static void createIndex(RestCypherQueryEngine engine, Label labelSource, Label labelType) {
		createIndex(engine, combineLabel(labelSource, labelType), PROPERTY_KEY);
	}*/
		
	public static RestIndex<Node> getIndex(RestAPI graphDb, String label) {
		return graphDb.index().forNodes(label);
	}
		
	public static RestIndex<Node> getIndex(RestAPI graphDb, Label label) {
		return graphDb.index().forNodes(label.name());
	}
	
	public static RestIndex<Node> getIndex(RestAPI graphDb, Label labelSource, Label labelType) {
		return graphDb.index().forNodes(combineLabel(labelSource, labelType));
	}
		
	public static RestRelationship createUniqueRelationship(RestAPI graphDb, RestNode nodeStart, RestNode nodeEnd, 
			RelationshipType type, Map<String, Object> data) {

		// get all node relationships. They should be empty for a new node
		Iterable<Relationship> rels = nodeStart.getRelationships(type);		
		for (Relationship rel : rels) 
			if (rel.getStartNode().getId() == nodeStart.getId() && 
				rel.getEndNode().getId() == nodeEnd.getId())
				return (RestRelationship) rel;
		
		return graphDb.createRelationship(nodeStart, nodeEnd, type, data);
	}
	
	public static void copyMisingProperties(Relationship rel, Map<String, Object> data) {
		if (data != null) 
			for (Map.Entry<String, Object> entity : data.entrySet()) 
				if (!rel.hasProperty(entity.getKey()))
					rel.setProperty(entity.getKey(), entity.getValue());
	}
	
	public static RestRelationship createUniqueRelationship(RestAPI graphDb, RestNode nodeStart, RestNode nodeEnd, 
			RelationshipType type, Direction direction, Map<String, Object> data) {

		// get all node relationships. They should be empty for a new node
		Iterable<Relationship> rels = nodeStart.getRelationships(type, direction);		
		for (Relationship rel : rels) {
			switch (direction) {
			case INCOMING:
				if (rel.getStartNode().getId() == nodeEnd.getId()) {
					copyMisingProperties(rel, data);
					return (RestRelationship) rel;
				}
			case OUTGOING:
				if (rel.getEndNode().getId() == nodeEnd.getId()){
					copyMisingProperties(rel, data);
					return (RestRelationship) rel;
				}
			case BOTH:
				if (rel.getStartNode().getId() == nodeEnd.getId() || 
				    rel.getEndNode().getId() == nodeEnd.getId()){
					copyMisingProperties(rel, data);
					return (RestRelationship) rel;
				}
			}
		}
		
		if (direction == Direction.INCOMING)
			return graphDb.createRelationship(nodeEnd, nodeStart, type, data);
		else
			return graphDb.createRelationship(nodeStart, nodeEnd, type, data);
	}
	
	public static Map<String, Object> getProperties(RestNode node) {
		Iterable<String> keys = node.getPropertyKeys();
		Map<String, Object> pars = null;
		
		for (String key : keys) {
			if (null == pars)
				pars = new HashMap<String, Object>();
			
			pars.put(key, node.getProperty(key));
		}
		
		return pars;
	}
	
	public static Map<String, Object> getProperties(RestRelationship relationship) {
		Iterable<String> keys = relationship.getPropertyKeys();
		Map<String, Object> pars = null;
		
		for (String key : keys) {
			if (null == pars)
				pars = new HashMap<String, Object>();
			
			pars.put(key, relationship.getProperty(key));
		}
		
		return pars;
	}
	
	public static String getNodeProperty(RestNode node, String property) {
		if (node.hasProperty(property))
			return (String) node.getProperty(property);
		else
			return null;
	}
	
	public static String getNodeSource(RestNode node) {
		return getNodeProperty(node, PROPERTY_NODE_SOURCE);
	}

	public static String getNodeType(RestNode node) {
		return getNodeProperty(node, PROPERTY_NODE_TYPE);
	}

	public static String getNodeKey(RestNode node) {
		return getNodeProperty(node, PROPERTY_KEY);
	}
	
	public static List<RestNode> findNodesByKey(RestCypherQueryEngine engine, String label,
			final String key, final String value) {
		List<RestNode> result = null;
		
		Map<String, Object> pars = new HashMap<String, Object>();
		pars.put(key, value);
		
	//	System.out.println("MATCH (n:" + label + ") WHERE has(n." + key + ") and n." + key + "={" + key + "} RETURN n");
		
		QueryResult<Map<String, Object>> nodes = engine.query("MATCH (n:" + label + ") WHERE has(n." + key + ") and n." + key + "={" + key + "} RETURN n", pars);
		for (Map<String, Object> row : nodes) {
			RestNode node = (RestNode) row.get("n");
			if (null != node) {
				if (null == result)
					result = new ArrayList<RestNode>();
				
				result.add(node);
			}
		}
		
		return result;
	}
	
	public static RestNode findNodeByKey(RestCypherQueryEngine engine, String label,
			final String key, final String value) {
		List<RestNode> nodes = findNodesByKey(engine, label, key, value);
	
		if (null != nodes && nodes.size() == 1)
			return nodes.get(0);
		else
			return null;
	}
	
	public static RestNode findNodeByKey(RestCypherQueryEngine engine, Label label,
			final String key, final String value) {
		return findNodeByKey(engine, label.name(), key, value);
	}
	
	public static RestNode findNodeByKey(RestCypherQueryEngine engine, Label label,
			final String value) {
		return findNodeByKey(engine, label.name(), PROPERTY_KEY, value);
	}
	
	public static RestNode findNodeByKey(RestCypherQueryEngine engine, Label labelSource, Label labelType,
			final String key, final String value) {
		return findNodeByKey(engine, doubleLabel(labelSource, labelType), key, value);
	}
	
	public static RestNode findNodeByKey(RestCypherQueryEngine engine, Label labelSource, Label labelType,
			final String value) {
		return findNodeByKey(engine, doubleLabel(labelSource, labelType), PROPERTY_KEY, value);
	}
	
	public static RestNode createUniqueNode(RestAPI graphDb, RestIndex<Node> index, 
			Label[] labels, String key, Object value, Map<String, Object> props) {
				
		props.put(key, value);
		
		RestNode node = graphDb.getOrCreateNode(index, key, value, props);
		
		for (Label label : labels) 
			if (!node.hasLabel(label))
				node.addLabel(label);
		
		return node;
	}
	
	public static RestNode createUniqueNode(RestAPI graphDb, RestIndex<Node> index, 
			Label labelSource, Label labelType, String key, Object value, Map<String, Object> props) {
		
		props.put(PROPERTY_NODE_SOURCE, labelSource.name());
		props.put(PROPERTY_NODE_TYPE, labelType.name());
		
		return createUniqueNode(graphDb, index, new Label[] { labelType, labelSource }, key, value, props);
	}
	
	public static RestNode createUniqueNode(RestAPI graphDb, RestIndex<Node> index, 
			Label labelSource, Label labelType, Object value, Map<String, Object> props) {
		
		return createUniqueNode(graphDb, index, labelSource, labelType, PROPERTY_KEY, value, props);
	}
	
	/*
	public static RestNode mergeNode(RestCypherQueryEngine engine, 
			String labelType, String labelSource, Map<String, Object> props) {
				
		StringBuilder cypher = new StringBuilder();
		cypher.append("MERGE (n:");
		cypher.append(labelSource);
		cypher.append(":");
		cypher.append(labelType);
		cypher.append(" { ");
		boolean bInit = false;
		
		for (String key : props.keySet()) {
		    if (bInit)
		    	cypher.append(", ");
		    else
		    	bInit = true;
		    
		    cypher.append(key);
		    cypher.append(": { " + key + " }");
		    //cypher.append(key);
		}
		
		cypher.append(" }) RETURN n");
		
	//	System.out.println(cypher.toString());
		
		engine.query(cypher.toString(), props);
		
		return null;
		
	}
	*/
	
	public static CypherResult mergeNode(RestAPI graphDb, 
			String labelType, String labelSource, Map<String, Object> props) {
				
		StringBuilder cypher = new StringBuilder();
		cypher.append("MERGE (n:");
		cypher.append(labelSource);
		cypher.append(":");
		cypher.append(labelType);
		cypher.append("{");
		boolean bInit = false;
		
		for (String key : props.keySet()) {
		    if (bInit)
		    	cypher.append(",");
		    else
		    	bInit = true;
		    
		    cypher.append("`");
		    cypher.append(key);
		    cypher.append("`:{`");
		    cypher.append(key);
		    cypher.append("`}");
		    //cypher.append(key);
		}
		
		cypher.append("})");
		
	//	System.out.println(cypher.toString());
		
		CypherResult result = graphDb.query(cypher.toString(), props);
		
		return result;		
	}
	
	public static CypherResult createUniqueRelationship(RestAPI graphDb, 
			String labelFrom, String keyFrom, String valueFrom,
			String labelTo, String keyTo, String valueTo,
			String relationship, Map<String, Object> props) {
				
		StringBuilder cypher = new StringBuilder();
		
		cypher.append("MATCH (from:");
		cypher.append(labelFrom);
		cypher.append("{");
		cypher.append(keyFrom);
		cypher.append(":{key_from}}),(to:");
		cypher.append(labelTo);
		cypher.append("{");
		cypher.append(keyTo);
		cypher.append(":{key_to}}) CREATE UNIQUE (from)-[:`");
		cypher.append(relationship);
		cypher.append("`");
		if (null != props) 
			cypher.append("{props}");
		cypher.append("]->(to)");

		Map<String, Object> map = new HashMap<String, Object>();
		map.put("key_from", valueFrom);
		map.put("key_to", valueTo);
		if (null != props)
			map.put("props", props);		
		
	//	System.out.println(cypher.toString());
		
		CypherResult result = graphDb.query(cypher.toString(), map);
			
		return result;
		
	}
	
	public static CypherResult mergeRelationship(RestAPI graphDb, 
			String labelFrom, String keyFrom, String valueFrom,
			String labelTo, String keyTo, String valueTo,
			String relationship, Map<String, Object> props) {
				
		StringBuilder cypher = new StringBuilder();
		
		cypher.append("MATCH (from:");
		cypher.append(labelFrom);
		cypher.append("{");
		cypher.append(keyFrom);
		cypher.append(":{key_from}}),(to:");
		cypher.append(labelTo);
		cypher.append("{");
		cypher.append(keyTo);
		cypher.append(":{key_to}) MERGE (from)-[:`");
		cypher.append(relationship);
		cypher.append("`");
		if (null != props) 
			cypher.append("{props}");
		cypher.append("]->(to)");
		
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("key_from", valueFrom);
		map.put("key_to", valueTo);
		if (null != props)
			map.put("props", props);		

	//	System.out.println(cypher.toString());
		
		CypherResult result = graphDb.query(cypher.toString(), map);
			
		return result;
		
	}
}
