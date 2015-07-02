package org.grants.compiler.dryad;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.rdswitchboard.utils.neo4j.Neo4jUtils;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.rest.graphdb.RestAPI;
import org.neo4j.rest.graphdb.RestAPIFacade;
import org.neo4j.rest.graphdb.entity.RestNode;
import org.neo4j.rest.graphdb.index.RestIndex;
import org.neo4j.rest.graphdb.query.RestCypherQueryEngine;
import org.neo4j.rest.graphdb.util.QueryResult;

public class Compiler {
	
	
	private static final String PROPERTY_DOI = "doi";
	private static final String PROPERTY_OAI = "oai";
	private static final String PROPERTY_HOST = "host";
	private static final String PROPERTY_PURL = "purl";
	private static final String PROPERTY_TITLE = "title";
	private static final String PROPERTY_AUTHOR = "author";
	private static final String PROPERTY_NAME = "name";
	private static final String PROPERTY_TIMESTAMP = "timestamp";
	private static final String PROPERTY_REFERENCED_BY = "referenced_by";
	private static final String PROPERTY_CONSTITUENT = "constituent";
	
	private static final String PROPERTY_IS_REFERENCED_BY = "isReferencedBy";
	private static final String PROPERTY_IDENTIFIER = "identifier";
	private static final String PROPERTY_IDENTIFIER_URI = "identifier_uri";
	
	private static enum Labels implements Label {
		Dryad, Publication, Dataset
    };
    
    private static enum Relationhips implements RelationshipType {
    	constituent
    };
	
	private RestAPI graphDb1;
	private RestAPI graphDb2;
	
	private RestCypherQueryEngine engine1;  
	private RestCypherQueryEngine engine2;  
		
	private Map<String, RestIndex<Node>> mapIndexes = new HashMap<String, RestIndex<Node>>();
	
	private RestIndex<Node> getIndex(Label labelSource, Label labelType) {
		String label = Neo4jUtils.combineLabel(labelSource, labelType);
		RestIndex<Node> index = mapIndexes.get(label);
		if (null == index) 
			mapIndexes.put(label, index = Neo4jUtils.getIndex(graphDb2, label));
				
		return index;
	}	
	
	public Compiler(final String neo4j1Url, final String neo4j2Url) {
		System.out.println("Source Neo4j: " + neo4j1Url);
		System.out.println("Target Neo4j: " + neo4j2Url);
		
		// connect to graph database
		graphDb1 = new RestAPIFacade(neo4j1Url);  
		graphDb2 = new RestAPIFacade(neo4j2Url);  
				
		// Create cypher engine
		engine1 = new RestCypherQueryEngine(graphDb1);  
		engine2 = new RestCypherQueryEngine(graphDb2);  
		
		Neo4jUtils.createConstraint(engine2, Labels.Dryad, Labels.Publication);
		Neo4jUtils.createConstraint(engine2, Labels.Dryad, Labels.Dataset);
		
		Neo4jUtils.createIndex(engine2, Labels.Dryad, PROPERTY_DOI);
	}
	
	public void process() {
		importDryadDataset();
		importDryadPublications();
		importDryadRelations();			
	}
	
	private void importDryadPublications() {
		QueryResult<Map<String, Object>> articles = engine1.query("MATCH (n:Dryad:Record) WHERE n.genre='Article' RETURN n", null);
		for (Map<String, Object> row : articles) 
			createDryadNode((RestNode) row.get("n"), Labels.Publication);
	}
	
	private void importDryadDataset() {
		// SKIP {skip_number} LIMIT {limit_number}
		QueryResult<Map<String, Object>> datasets = engine1.query("MATCH (n:Dryad:Record) WHERE n.genre IN ['Dataset', 'dataset']  RETURN n", null);
		for (Map<String, Object> row : datasets) 
			createDryadNode((RestNode) row.get("n"), Labels.Dataset);
	}
	
	@SuppressWarnings("unchecked")
	private void importDryadRelations() {
		QueryResult<Map<String, Object>> publications = engine2.query("MATCH (n:Dryad:Publication) WHERE has (n.constituent) RETURN ID(n) as id, n.constituent as doi", null);
		for (Map<String, Object> row : publications) {
			
			int id = (Integer) row.get("id");
			Object doi = row.get("doi");
			
			System.out.println(id);
			
			if (doi != null) {
				RestNode nodePublication = graphDb2.getNodeById(id);
				if (doi instanceof String) {
					 List<RestNode> nodesDatasets = findDryadNodeByDoi(Labels.Dataset, (String) doi);
					 if (null != nodesDatasets) 
						 for (RestNode nodeDataset : nodesDatasets) 
							 Neo4jUtils.createUniqueRelationship(graphDb2, nodePublication, nodeDataset, 
										Relationhips.constituent, Direction.OUTGOING, null);
				} else 
					for (String d : (List<String>) doi) {
						List<RestNode> nodesDatasets =findDryadNodeByDoi(Labels.Dataset, d);
						if (null != nodesDatasets) 
							for (RestNode nodeDataset : nodesDatasets) 
								 Neo4jUtils.createUniqueRelationship(graphDb2, nodePublication, nodeDataset, 
											Relationhips.constituent, Direction.OUTGOING, null);
		
					}
			}
		}
		
		QueryResult<Map<String, Object>> datasets = engine2.query("MATCH (n:Dryad:Dataset) WHERE has (n.host) RETURN ID(n) as id, n.host as doi", null);
		for (Map<String, Object> row : datasets) {
			
			int id = (Integer) row.get("id");
			Object doi = row.get("doi");
			
			System.out.println(id);
			
			if (doi != null) {
				RestNode nodeDataset = graphDb2.getNodeById(id);
				if (doi instanceof String) {
					 List<RestNode> nodesPublications = findDryadNodeByDoi(Labels.Publication, (String) doi);
					 if (null != nodesPublications) 
						 for (RestNode nodePublication : nodesPublications) 
							 Neo4jUtils.createUniqueRelationship(graphDb2, nodePublication, nodeDataset, 
										Relationhips.constituent, Direction.OUTGOING, null);
				} else 
					for (String d : (List<String>) doi) {
						 List<RestNode> nodesPublications = findDryadNodeByDoi(Labels.Publication, d);
						 if (null != nodesPublications) 
							 for (RestNode nodePublication : nodesPublications) 
								 Neo4jUtils.createUniqueRelationship(graphDb2, nodePublication, nodeDataset, 
											Relationhips.constituent, Direction.OUTGOING, null);					}
			}
		}
	}
	
	private void createDryadNode(RestNode node, Label labelType) {
		if (null == node) {
			System.out.println("Invalid node");
			return;
		}
		
		String key = (String) getProperty(node, PROPERTY_OAI);
		if (null == key || key.isEmpty()) {
			System.out.println("Invalid node key");
			return;
		}
		
		System.out.println(key);
					
		Map<String, Object> map = new HashMap<String, Object>();
		
		map.put(PROPERTY_OAI, key);
		
		copyProperty(map, node, PROPERTY_DOI);
		copyProperty(map, node, PROPERTY_HOST);
		copyProperty(map, node, PROPERTY_TITLE);
		copyProperty(map, node, PROPERTY_AUTHOR);
		copyProperty(map, node, PROPERTY_NAME);
		copyProperty(map, node, PROPERTY_TIMESTAMP);
		copyProperty(map, node, PROPERTY_CONSTITUENT);
		
		Object referencedBY = getProperty(node, PROPERTY_IS_REFERENCED_BY);
		if (null != referencedBY)
			map.put(PROPERTY_REFERENCED_BY, referencedBY);
		
		Object url = getProperty(node, PROPERTY_IDENTIFIER_URI);
		if (null != url)
			map.put(Neo4jUtils.PROPERTY_URL, url);
		
		Object purl = getProperty(node, PROPERTY_IDENTIFIER);
		if (null != purl)
			map.put(PROPERTY_PURL, purl);
		
		Neo4jUtils.createUniqueNode(graphDb2, getIndex(Labels.Dryad, labelType),
				Labels.Dryad, labelType, key, map);
	}
	
	private List<RestNode> findDryadNodeByDoi(Label labelType, final String doi) {
		List<RestNode> result = null;
		
		Map<String, Object> pars = new HashMap<String, Object>();
		pars.put("doi", doi);
		
		QueryResult<Map<String, Object>> nodes = engine2.query("MATCH (n:Dryad:" + labelType.name() + ") WHERE has(n.doi) and any (m in n.doi WHERE m = {doi}) RETURN n", pars);
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
	
	private Object getProperty(RestNode node, String key) {
		if (node.hasProperty(key)) 
			return node.getProperty(key);
		return null;
	}
	
	private void copyProperty(Map<String, Object> map, RestNode node, String key) {
		if (node.hasProperty(key)) {
			Object value = node.getProperty(key);
			if (null != value)
				map.put(key, value);
		}
	}
}
