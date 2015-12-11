package org.rdswitchboard.libraries.neo4j;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.index.AutoIndexer;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.ReadableIndex;
import org.neo4j.graphdb.schema.ConstraintDefinition;
import org.neo4j.graphdb.schema.IndexDefinition;
import org.neo4j.graphdb.schema.Schema;
import org.neo4j.tooling.GlobalGraphOperations;
import org.rdswitchboard.libraries.graph.Graph;
import org.rdswitchboard.libraries.graph.GraphKey;
import org.rdswitchboard.libraries.graph.GraphNode;
import org.rdswitchboard.libraries.graph.GraphRelationship;
import org.rdswitchboard.libraries.graph.GraphSchema;
import org.rdswitchboard.libraries.graph.interfaces.GraphImporter;
import org.rdswitchboard.libraries.neo4j.interfaces.ProcessNode;

public class Neo4jDatabase implements GraphImporter {
	private static final String COLUMN_N = "n";
		
	private GraphDatabaseService graphDb;
	
	private Map<String, Index<Node>> indexes = new HashMap<String, Index<Node>>();
	private AutoIndexer<Node> nodeAutoIndexer = null;
	
	private boolean verbose = false;
	private long nodesCreated = 0;
	private long nodesUpdated = 0;
	private long relationshipsCreated = 0;
	private long relationshipsUpdated = 0;
	
	private final Map<String, List<GraphRelationship>> unknownRelationships = new HashMap<String, List<GraphRelationship>>();
	private final Set<GraphSchema> importedSchemas = new HashSet<GraphSchema>();	

	public Neo4jDatabase(final String neo4jFolder) throws Exception {		
		graphDb = Neo4jUtils.getGraphDb( neo4jFolder );
	}

	
	public Neo4jDatabase(final String neo4jFolder, boolean readOnly) throws Exception {
		graphDb = readOnly 
				? Neo4jUtils.getReadOnlyGraphDb(neo4jFolder) 
				: Neo4jUtils.getGraphDb( neo4jFolder );
	}
	
	public GraphDatabaseService getGraphDatabaseService() {
		return graphDb;
	}
		
	public GlobalGraphOperations getGlobalOperations() {
		return GlobalGraphOperations.at(graphDb);
	}
	
	/*public static ExecutionEngine getExecutionEngine( final GraphDatabaseService graphDb ) {
		return new ExecutionEngine(graphDb, StringLogger.SYSTEM);
	}*/
	
	public boolean isVerbose() {
		return verbose;
	}

	public long getNodesCreated() {
		return nodesCreated;
	}

	public long getNodesUpdated() {
		return nodesUpdated;
	}

	public long getRelationshipsCreated() {
		return relationshipsCreated;
	}

	public long getRelationshipsUpdated() {
		return relationshipsUpdated;
	}

	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}
	
	public void resetCounters() {
		nodesCreated = nodesUpdated = relationshipsCreated = relationshipsUpdated = 0;
	}
	
	public void printStatistics(PrintStream out) {
		out.println( String.format("%d nodes has been created.\n%d nodes has been updated.\n%d relationships has been created.\n%d relationships has been updated.\n%d relationships keys has been invalid.", 
				nodesCreated, nodesUpdated, relationshipsCreated, relationshipsUpdated, unknownRelationships.size()) );
	}
	
	public long getSourcesConnectionsCount(String source1, String source2) {
		try ( Transaction ignored = graphDb.beginTx() ) 
		{
			String cypher = "MATCH (n1:" + source1 + ")-[x]-(n2:" + source2 + ") RETURN COUNT (DISTINCT x) AS n";
			try (Result result = graphDb.execute(cypher)) {
				if  ( result.hasNext() )
			    {
			        Map<String,Object> row = result.next();
			        return (Long) row.get(COLUMN_N);
			    }
			}
		}
		
		return 0;
	}
	
	
	public void enumrateAllNodes(ProcessNode processNode) throws Exception {
		try ( Transaction tx = graphDb.beginTx() ) 
		{
			ResourceIterable<Node> nodes = getGlobalOperations().getAllNodes();
			for (Node node : nodes) {
				if (!processNode.processNode(node))
					break;
			}
		
			tx.success();
		}
	}
	
	public void enumrateAllNodesWithLabel(Label label, ProcessNode processNode) throws Exception {
		try ( Transaction tx = graphDb.beginTx() ) 
		{
//			GlobalGraphOperations global = Neo4jUtils.getGlobalOperations(graphDb);
//			global.
			
			try (ResourceIterator<Node> nodes = graphDb.findNodes(label)) {
				while (nodes.hasNext()) {
					if (!processNode.processNode(nodes.next()))
						break;
				}
			}
			
			tx.success();
		}
	}
	
	public void enumrateAllNodesWithLabel(String label, ProcessNode processNode) throws Exception {
		enumrateAllNodesWithLabel(DynamicLabel.label(label), processNode);
	}
	
	public void enumrateAllNodesWithProperty(String property, ProcessNode processNode) throws Exception {
		try ( Transaction tx = graphDb.beginTx() ) 
		{
			String cypher = "MATCH (n) WHERE HAS(n." + property + ") RETURN n";
			try (Result result = graphDb.execute(cypher)) {
				while ( result.hasNext() )
			    {
			        Map<String,Object> row = result.next();
			        if (!processNode.processNode((Node) row.get(COLUMN_N)))
			        	break;
			    }
			}
			
			tx.success();
		}
	}
	
	public void enumrateAllNodesWithLabelAndProperty(String label, String property, ProcessNode processNode)  throws Exception {
		try ( Transaction tx = graphDb.beginTx() ) 
		{
			String cypher = "MATCH (n:" + label + ") WHERE HAS(n." + property + ") RETURN n";
			try (Result result = graphDb.execute(cypher)) {
				while ( result.hasNext() )
			    {
			        Map<String,Object> row = result.next();
			        if (!processNode.processNode((Node) row.get(COLUMN_N)))
			        	break;
			    }
			}
			
			tx.success();
		}
	}
	
	public void enumrateAllNodesWithLabelAndProperty(Label label, String property, ProcessNode processNode)  throws Exception {
		enumrateAllNodesWithLabelAndProperty(label.toString(), property, processNode);
	}
	
	public ConstraintDefinition createConstrant(Label label, String key) {
		ConstraintDefinition def = null;
		
		try ( Transaction tx = graphDb.beginTx() ) 
		{
			def = _createConstrant(label, key);
			
			tx.success();
		}
		
		return def;
	}	
	
	public ConstraintDefinition createConstrant(String label, String key) {
		return createConstrant(DynamicLabel.label(label), key);
	}
	
	public IndexDefinition createIndex(Label label, String key) {
		IndexDefinition def = null;
		
		try ( Transaction tx = graphDb.beginTx() ) 
		{
			def = _createIndex(label, key);
			
			tx.success();
		}
		
		return def;
	}
	
	public IndexDefinition createIndex(String label, String key) {
		return createIndex(DynamicLabel.label(label), key);
	}
	
	public Index<Node> getNodeIndex(String label) {
		try ( Transaction ignored = graphDb.beginTx() ) 
		{
			return _getNodeIndex(label);
		}
	}
	
	public void importGraph(Graph graph) {
	
		// schema can not be imported in the same transaction as nodes and relationships
		try ( Transaction tx = graphDb.beginTx() ) 
		{
			_importSchemas(graph.getSchemas());
			
			tx.success();
		}
		
		try ( Transaction tx = graphDb.beginTx() ) 
		{
			_importNodes(graph.getNodes());
			_importRelationships(graph.getRelationships());
			
			tx.success();
		}
	}
	
	public void importSchemas(Collection<GraphSchema> schemas) {
		try ( Transaction tx = graphDb.beginTx() ) 
		{
			_importSchemas(schemas);
		
			tx.success();
		}
	}

	public void importSchema(GraphSchema schema) {
		try ( Transaction tx = graphDb.beginTx() ) 
		{
			_importSchema(schema);
		
			tx.success();
		}
	}
	
	public void importNodes(Collection<GraphNode> nodes) {
		// Import nodes
		try ( Transaction tx = graphDb.beginTx() ) 
		{
			_importNodes(nodes);		
				
			tx.success();
		}
	}

	public void importNode(GraphNode node) {
		// Import nodes
		try ( Transaction tx = graphDb.beginTx() ) 
		{
			_importNode(node);		
				
			tx.success();
		}
	}
	
	public void importRelationships(Collection<GraphRelationship> relationships) {
		try ( Transaction tx = graphDb.beginTx() ) 
		{		
			_importRelationships(relationships);
			
			tx.success();
		}
	}
	
	public void importRelationship(GraphRelationship relationship) {
		try ( Transaction tx = graphDb.beginTx() ) 
		{		
			_importRelationship(relationship, true);
			
			tx.success();
		}
	}
	
	public ConstraintDefinition _createConstrant(Label label, String key) {
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
	
	public ConstraintDefinition _createConstrant(String label, String key) {
		return _createConstrant(DynamicLabel.label(label), key);
	}
	
	public IndexDefinition _createIndex(Label label, String key) {
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
	
	public IndexDefinition _createIndex(String label, String key) {
		return _createIndex(DynamicLabel.label(label), key);
	}
	
	public Index<Node> _getNodeIndex(String label) {
		if ( indexes.containsKey(label) ) 
			return indexes.get( label );
		
		Index<Node> index = graphDb.index().forNodes( label );
		indexes.put(label, index);
		return index;
	}
	
	public ReadableIndex<Node> _getNodeAutoIndex(String indexingProperty) {
		if (null == nodeAutoIndexer)
			nodeAutoIndexer = graphDb.index().getNodeAutoIndexer();
		if (null != indexingProperty) {
			if (verbose)
				System.out.println("Creating Node Auto Index on Property: " + indexingProperty);
	        
			nodeAutoIndexer.startAutoIndexingProperty( indexingProperty );
			nodeAutoIndexer.setEnabled( true );
		}
 
		return nodeAutoIndexer.getAutoIndex();
	}
	
	public Node _findNode(Index<Node> index, String key, Object value) {
		 return index.get(key, value).getSingle();
	}
	
	public Node _findNode(String label, String key, Object value) {
		return _getNodeIndex(label)
				.get(key, value)
				.getSingle();
		
	/*	ResourceIterable<Node> nodes = graphDb.findNodesByLabelAndProperty(
				DynamicLabel.label(connection.getType()), 
				AggrigationUtils.PROPERTY_KEY, 
				connection.getKey());
		
		try (ResourceIterator<Node> noded = nodes.iterator()) {
			if (noded.hasNext())
				return noded.next();
			else
				return null;
		}*/
	}
	
	public Node _findNode(GraphKey key) {
		return _findNode(key.getIndex(), key.getKey(), key.getValue());
		
	/*	ResourceIterable<Node> nodes = graphDb.findNodesByLabelAndProperty(
				DynamicLabel.label(connection.getType()), 
				AggrigationUtils.PROPERTY_KEY, 
				connection.getKey());
		
		try (ResourceIterator<Node> noded = nodes.iterator()) {
			if (noded.hasNext())
				return noded.next();
			else
				return null;
		}*/
	}
	
	
	
	public ResourceIterator<Node> _findNodes(Label label, String key, Object value) {
		return graphDb.findNodes(label, key, value);
	}
	
	public ResourceIterator<Node> _findNodes(String label, String key, Object value) {
		return _findNodes(DynamicLabel.label(label), key, value);
	}

	public ResourceIterator<Node> _findNodes(GraphKey key) {
		return _findNodes(DynamicLabel.label(key.getIndex()), key.getKey(), key.getValue());
	}
	
	public Node _findSingleNode(Label label, String key, Object value) {
		try (ResourceIterator<Node> nodes = _findNodes(label, key, value)) {
			if (!nodes.hasNext())
				return null;
			
			return nodes.next();
		}		
	}
	
	public Node _findSingleNode(String label, String key, Object value) {
		return _findSingleNode(DynamicLabel.label(label), key, value); 		
	}
	
	public Node _findSingleNode(GraphKey key) {
		return _findSingleNode(key.getIndex(), key.getKey(), key.getValue()); 		
	}	
	
	private List<Node> _findRelatedNodes(Label label, String key, Object value) {
		try (ResourceIterator<Node> hits = _findNodes(label, key, value)) {
			List<Node> nodes = new ArrayList<Node>();
			
			while (hits.hasNext()) {
				nodes.add(hits.next());
			}
			
			return nodes;
		}
	}
	
	private List<Node> _findRelatedNodes(String label, String key, Object value) {
		return _findRelatedNodes(DynamicLabel.label(label), key, value);
	}
	
	private List<Node> _findRelatedNodes(GraphKey key) {
		return _findRelatedNodes(DynamicLabel.label(key.getIndex()), key.getKey(), key.getValue());
	}
	
	public Relationship _findRelationship(Iterable<Relationship> rels, long nodeId, Direction direction) {
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
	
	public Relationship _findRelationship(Node nodeStart, long nodeId, 
			RelationshipType type, Direction direction) {
		return _findRelationship(nodeStart.getRelationships(type, direction), nodeId, direction);
	}
	
	public Relationship _findRelationship(Node nodeStart, Node endNode, 
			RelationshipType type, Direction direction) {
		return _findRelationship(nodeStart, endNode.getId(), type, direction);
	}
	
	public void _addLabel(Node node, Label label) {
		node.addLabel(label);
	}

	public void _addLabels(Node node, Label[] labels) {
		for (Label label : labels)
			node.addLabel(label);
	}

	public void _addLabel(Node node, String label) {
		node.addLabel( DynamicLabel.label( label ) );
	}

	public void _addLabels(Node node, String[] labels) {
		for (String label : labels)
			node.addLabel( DynamicLabel.label( label ) );
	}
	
	public void _setProperties(Node node, Map<String, Object> properties) {
		for (Map.Entry<String, Object> entry : properties.entrySet())
			node.setProperty(entry.getKey(), entry.getValue());
	}
	
	public void _setProperties(Relationship relationship, Map<String, Object> properties) {
		for (Map.Entry<String, Object> entry : properties.entrySet())
			relationship.setProperty(entry.getKey(), entry.getValue());
	}
	
	public Node _createNode() {
		++nodesCreated;
		
		return graphDb.createNode();
	}
	
	public Node _createNode(Label label) {
		++nodesCreated;
		Node node = graphDb.createNode();
		_addLabel(node, label);
		return node;
	}

	public Node _createNode(Label[] labels) {
		++nodesCreated;
		Node node = graphDb.createNode();
		_addLabels(node, labels);
		return node;
	}
	
	public Node _createNode(Map<String, Object> properties) {
		++nodesCreated;
		Node node = graphDb.createNode();
		if (null != properties)
			_setProperties(node, properties);
		return node;
	}
	
	public Node _createNode(Label label, Map<String, Object> properties) {
		++nodesCreated;
		Node node = graphDb.createNode();
		_addLabel(node, label);
		if (null != properties)
			_setProperties(node, properties);
		return node;
	}

	public Node _createNode(Label[] labels, Map<String, Object> properties) {
		++nodesCreated;
		Node node = graphDb.createNode();
		_addLabels(node, labels);
		if (null != properties)
			_setProperties(node, properties);
		return node;
	}
	
	public Node _createNode(String label, Map<String, Object> properties) {
		++nodesCreated;
		Node node = graphDb.createNode();
		_addLabel(node, label);
		if (null != properties)
			_setProperties(node, properties);
		return node;
	}

	public Node _createNode(String[] labels, Map<String, Object> properties) {
		++nodesCreated;
		Node node = graphDb.createNode();
		_addLabels(node, labels);
		if (null != properties)
			_setProperties(node, properties);
		return node;
	}
	
	public Node _updateNode(Node node, Label label, Map<String, Object> properties) {
		_addLabel(node, label);
		if (null != properties)
			_setProperties(node, properties);
		return node;
	}

	public Node _updateNode(Node node, Label[] labels, Map<String, Object> properties) {
		_addLabels(node, labels);
		if (null != properties)
			_setProperties(node, properties);
		return node;
	}
	
	public Node _updateNode(Node node, String label, Map<String, Object> properties) {
		_addLabel(node, label);
		if (null != properties)
			_setProperties(node, properties);
		return node;
	}

	public Node _updateNode(Node node, String[] labels, Map<String, Object> properties) {
		_addLabels(node, labels);
		if (null != properties)
			_setProperties(node, properties);
		return node;
	}
	
	
	public Node _createUniqueNode(Index<Node> index, String key, Object value) {
		Node node = _findNode(index, key, value);
		if (null == node) {
			node = _createNode();
			
			index.add(node, key, value);
		}
		
		return node;
	}
	
	public Node _createUniqueNode(Index<Node> index, String key, Object value, 
			Label label) {
		Node node = _findNode(index, key, value);
		if (null == node) {
			node = _createNode(label);
			
			index.add(node, key, value);
		}
		
		return node;
	}
	
	public Node _createUniqueNode(Index<Node> index, String key, Object value, 
			Label[] labels) {
		Node node = _findNode(index, key, value);
		if (null == node) {
			node = _createNode(labels);
			
			index.add(node, key, value);
		}
		
		return node;
	}

	public Node _createUniqueNode(Index<Node> index, String key, Object value, 
			Map<String, Object> properties) {
		Node node = _findNode(index, key, value);
		if (null == node) {
			node = _createNode(properties);
			
			index.add(node, key, value);
		}
		
		return node;
	}

	public Node _createUniqueNode(Index<Node> index, String key, Object value, 
			Label label, Map<String, Object> properties) {
		Node node = _findNode(index, key, value);
		if (null == node) {
			node = _createNode(label, properties);
			
			index.add(node, key, value);
		}
		
		return node;
	}
	
	public Node _createUniqueNode(Index<Node> index, String key, Object value, 
			Label[] labels, Map<String, Object> properties) {
		Node node = _findNode(index, key, value);
		if (null == node) {
			node = _createNode(labels, properties);
			
			index.add(node, key, value);
		}
		
		return node;
	}
	
	public Node _mergeNode(Index<Node> index, String key, Object value, 
			Label[] labels, Map<String, Object> properties) {
		Node node = _findNode(index, key, value);
		if (null == node) {
			node = _createNode();
			
			index.add(node, key, value);
		} else 
			
			++nodesUpdated;
			
		_addLabels(node, labels);
		if (null != properties)
			_setProperties(node, properties);
		
		return node;
	}
	
	public Node _mergeNode(Index<Node> index, String key, Object value, 
			String[] labels, Map<String, Object> properties) {
		Node node = _findNode(index, key, value);
		if (null == node) {
			node = _createNode();
			
			index.add(node, key, value);
		} else 
			
			++nodesUpdated;
			
		_addLabels(node, labels);
		if (null != properties)
			_setProperties(node, properties);
		
		return node;
	}

	public Relationship _createRelationship(Node nodeStart, Node nodeEnd, RelationshipType type) {
		++relationshipsCreated;
		
		return nodeStart.createRelationshipTo(nodeEnd, type);		
	}
	
	public Relationship _createRelationship(Node nodeStart, Node nodeEnd, RelationshipType type, 
			Map<String, Object> properties) {
		Relationship relationship = _createRelationship(nodeStart, nodeEnd, type);
		if (null != properties)
			_setProperties(relationship, properties);
	
		return relationship;
	}
	
	public Relationship _createUniqueRelationship(Node nodeStart, Node nodeEnd, RelationshipType type, 
			Direction direction, Map<String, Object> properties) {

		Relationship relationship = _findRelationship(nodeStart, nodeEnd, type, direction);
		if (null == relationship)
			return _createRelationship(nodeStart, nodeEnd, type, properties);

		return relationship;
	}
		
	public Relationship _mergeRelationship(Node nodeStart, Node nodeEnd, RelationshipType type, 
			Direction direction, Map<String, Object> properties) {

		Relationship relationship = _findRelationship(nodeStart, nodeEnd, type, direction);
		if (null == relationship) 
			relationship = _createRelationship(nodeStart, nodeEnd, type);
		else 
			++relationshipsUpdated;
		if (null != properties)
			_setProperties(relationship, properties);
		return relationship;
	}
	
	public Map<String, Object> _getProperties(Node node) {
		Iterable<String> keys = node.getPropertyKeys();
		Map<String, Object> pars = null;
		
		for (String key : keys) {
			if (null == pars)
				pars = new HashMap<String, Object>();
			
			pars.put(key, node.getProperty(key));
		}
		
		return pars;
	}
	
	public Map<String, Object> _getProperties(Relationship relationship) {
		Iterable<String> keys = relationship.getPropertyKeys();
		Map<String, Object> pars = null;
		
		for (String key : keys) {
			if (null == pars)
				pars = new HashMap<String, Object>();
			
			pars.put(key, relationship.getProperty(key));
		}
		
		return pars;
	}
	
	public void _importSchemas(Collection<GraphSchema> schemas) {
		if (null != schemas)
			for (GraphSchema schema : schemas) 
				_importSchema(schema);
	}
	
	public void _importSchema(GraphSchema schema) {
		// make sure we had imported each schema only once
		if (!importedSchemas.contains(schema)) {
			String index = schema.getIndex();
			String key = schema.getKey();
			
			if (schema.isUnique()) {
				if (verbose) {
					System.out.println("Creating Constraint {index=" + index + ", key=" + key + "}");
				}
				_createConstrant(index, key);
			} else {
				if (verbose) {
					System.out.println("Creating Index {index=" + index + ", key=" + key + "}");
				}
	
				_createIndex(index, key);
			}
			
			importedSchemas.add(schema);
		}
	}

	public void _importNodes(Collection<GraphNode> nodes) {
		// Import nodes
		if (null != nodes)
			for (GraphNode graphNode : nodes) 
				_importNode(graphNode);		
	}

	public Node _importNode(GraphNode graphNode) {
		if (graphNode.isBroken() || graphNode.isDeleted())
			return null;
		
		GraphKey key = graphNode.getKey();
		
		if (StringUtils.isEmpty(key.getIndex()))
			throw new IllegalArgumentException("Node Key Index can not be empty");
		if (StringUtils.isEmpty(key.getKey()))
			throw new IllegalArgumentException("Node Key Property can not be null");
		if (null == key.getValue())
			throw new IllegalArgumentException("Node Key Value can not be null");
			
		if (verbose) {
			System.out.println("Importing Node (" + key + ")");
		}
		
		Index<Node> idx = _getNodeIndex(key.getIndex());
		Node node = _findSingleNode(key.getIndex(), key.getKey(), key.getValue());
		if (null == node) {
			node = _createNode(graphNode.getLabels(), graphNode.getProperties());
			node.setProperty(key.getKey(), key.getValue());
			
			idx.add(node, key.getKey(), key.getValue());
			
			List<GraphRelationship> list = unknownRelationships.remove(getRelationshipKey(key));
			if (null != list) 
				for (GraphRelationship relationship : list) 
					_importRelationship(relationship, false);
			
			for (GraphKey index : graphNode.getIndexes()) {
				
				node.setProperty(index.getKey(), index.getValue());
				
				list = unknownRelationships.remove(getRelationshipKey(index));
				if (null != list) 
					for (GraphRelationship relationship : list) 
						_importRelationship(relationship, false);
			}
		} else  {
			++nodesUpdated;
			
			_updateNode(node, graphNode.getLabels(), graphNode.getProperties());
		}
		
		return node;
	}
	
	public void _importRelationships(Collection<GraphRelationship> relationships) {
		if (null != relationships)
			for (GraphRelationship graphRelationship : relationships) 
				_importRelationship(graphRelationship, true);
	}
		
	private void _importRelationship(GraphRelationship graphRelationship, boolean storeUnknown) {
		String relationshipName = graphRelationship.getRelationship();
		GraphKey start = graphRelationship.getStart();
		GraphKey end = graphRelationship.getEnd();
		
		List<Node> nodesStart = _findRelatedNodes(start);
		if (nodesStart.isEmpty() && storeUnknown) { 
			storeUnknownRelationship(getRelationshipKey(start), graphRelationship);
			
			if (verbose)
				System.out.println("Relationship Start Key (" + start + ") does not exists");
		}
		
		List<Node> nodesEnd = _findRelatedNodes(end);
		if (nodesEnd.isEmpty() && storeUnknown) {
			storeUnknownRelationship(getRelationshipKey(end), graphRelationship);
			
			if (verbose)
				System.out.println("Relationship End Key (" + end + ") does not exists");
		}
		
		if (nodesStart.isEmpty() || nodesEnd.isEmpty())
			return;
		
		if (verbose) 
			System.out.println("Importing Relationship (" + start + ")-[" + relationshipName + "]->(" + end + ")");
		
		RelationshipType relationshipType = DynamicRelationshipType.withName(relationshipName);
		for (Node nodeStart : nodesStart)
			for (Node nodeEnd : nodesEnd)
				_mergeRelationship(nodeStart, nodeEnd, relationshipType, 
						Direction.OUTGOING, graphRelationship.getProperties());
	}
	
	private static String getRelationshipKey(GraphKey key) {
		return key.getIndex() + "." + key.getKey() + "." + key.getValue();
	}
	
	private void storeUnknownRelationship(String key, GraphRelationship relationship) {
		List<GraphRelationship> list = unknownRelationships.get(key);
		if (null == list) 
			unknownRelationships.put(key, list = new ArrayList<GraphRelationship>());
		
		list.add(relationship);
	}
	
	/**
	 * We should never expose begin transaction function!
	 * @return Transaction
	 */	
	@Deprecated
	public Transaction _beginTx() {		
		return graphDb.beginTx();
	}

	@Deprecated
	public Node _getNodeById(long id) {
		return graphDb.getNodeById(id);
	}
}
