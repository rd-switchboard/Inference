package org.rdswitchboard.exporters.graph;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.Transaction;
import org.neo4j.tooling.GlobalGraphOperations;
import org.parboiled.common.StringUtils;
import org.rdswitchboard.exporters.graph.json.JsonGraph;
import org.rdswitchboard.exporters.graph.json.JsonNode;
import org.rdswitchboard.exporters.graph.json.JsonRelationship;
import org.rdswitchboard.libraries.graph.GraphUtils;
import org.rdswitchboard.libraries.neo4j.Neo4jUtils;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.InstanceProfileCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Export records from Nexus instance in JSON formats
 * The maximum of {@link Exporter#maxNodes} nodes will be exported
 * with {@link Exporter#maxLevel} level siblings. Each node will be 
 * allowed to add {@link Exporter#maxSiblings} to the graph. The 
 * limits can be disabled, if 0 has been supplied
 
 * The file name will be generated as (slug)-(record_id).json * 
 * The program will sent files directly into S3 bucket, S3 credentials 
 * must be provided:
 * 
 * s3AccessKey - S3 access key
 * s3SecureKey - S3 secure key
 * s3Bucket - S3 Bucket name 
 * s3Key - part (prefix) of S3 Key, usually folder name in format 'folder/' 
 *         supply '' to dump files into root folder
 * 
 * 
 * @version 4.0.0
 * @author Dima Kudriavcev (dmitrij@kudriavcev.info)
 * @date 24 May 2015 	
 * 
 * history:
 *    3.1.0 - added configuration profile and S3 support
 *    3.2.0 - Do not export relationships between dataset/publication and institution nodes
 *    		  Automatically adding publication permissions for now
 *    3.3.0 - Removed intermediate file.
 *    4.0.0 - Created library from the old code
 */

public class Exporter {
	
	private static final String CONTENT_ENCODING = "UTF-8";
	private static final String CONTENT_TYPE = "application/json";
	
	private GraphDatabaseService graphDb;

	private long nodeCounter = 0;
	
	private boolean publicReadRights = false;
	private long testNodeId = 0;
	private int maxLevel = 3;
	private int maxNodes = 100;
	private int maxSiblings = 10;
	private String s3Bucket;
	private String s3Key;
	private String neo4jFolder;
		
	//private AWSCredentials awsCredentials;
	private AmazonS3 s3client;
		
	//private enum NodeType { Grant, Researcher, Publication, Dataset, Institution };
	
	private static final ObjectMapper mapper = new ObjectMapper();
	private static final Charset charset = Charset.forName(CONTENT_ENCODING);
	
	//private Map<Label, String> labels;
	
	/**
	 * Function to set Neo4j folder
	 * @param neo4jFolder
	 */
	public void setNeo4jFolder(String neo4jFolder) {
		this.neo4jFolder = neo4jFolder;
	}
	
	/**
	 * Function to set AWS S3 Credentials
	 * @param accessKey
	 * @param secretKey
	 */
	public void setAwsCredentials(String accessKey, String secretKey) {
		this.s3client = new AmazonS3Client(new BasicAWSCredentials(accessKey, secretKey));
	}
	
	public void setAwsInstanceProfileCredentials() {
		this.s3client = new AmazonS3Client(new InstanceProfileCredentialsProvider());
	}
	
	/**
	 * Function to set target S3 Bucket
	 * @param bucket
	 */
	public void setS3Bucket(String bucket) {
		this.s3Bucket = bucket;		
	}
	
	/**
	 * Function to set S3 Key prefix
	 * @param key
	 */
	public void setS3Key(String key) {
		this.s3Key = key;
	}	
	
	/** 
	 * Function to set maximum export level
	 * @param maxLevel
	 */
	public void setMaxLevel(int maxLevel) {
		this.maxLevel = maxLevel;
	}
	
	/**
	 * Function to set maximum nodes count per graph
	 * @param maxNodes
	 */
	public void setMaxNodes(int maxNodes) {
		this.maxNodes = maxNodes;
	}
	
	/**
	 * Function to set maximum syblings count
	 * @param maxSiblings
	 */
	public void setMaxSiblings(int maxSiblings) {
		this.maxSiblings = maxSiblings;
	}
	
	/**
	 * Function to add exporting label
	 * @param label
	 */
	
	
	
	public long getTestNodeId() {
		return testNodeId;
	}

	public void setTestNodeId(long testNodeId) {
		this.testNodeId = testNodeId;
	}
	
/*	public void setLabels(Map<Label, String> labels) {
		this.labels = labels;
	}*/

	/**
	 * Function to enable Public Read Rights
	 * @param publicReadRights
	 */
	public void enablePublicReadRights(boolean publicReadRights) {
		this.publicReadRights = publicReadRights;
	}
	
	/**
	 * Function to begin exporting process
	 */
	
	public void process(Label type, Map<Label, Label[]> sources) {
		
	//	System.out.println("Target folder: " + outputFolder);
		System.out.println("Neo4j folder: " + neo4jFolder);
		System.out.println("S3 Bucket: " + s3Bucket);
		System.out.println("S3 Key: " + s3Key);
		System.out.println("Export level: " + maxLevel);
		System.out.println("Max nodes: " + maxNodes);
		System.out.println("Max siblings: " + maxSiblings);
		
		if (0 != testNodeId)
			System.out.println("Test Node ID: " + testNodeId);
		
		graphDb = Neo4jUtils.getReadOnlyGraphDb(neo4jFolder);
		
//		System.out.println("Region: " + s3client.setE);
//		System.out.println("Location: " + s3client.getBucketLocation(s3Bucket));
		
//		s3client.setRegion(Region.getRegion(Regions.US_WEST_2));
		s3client.setEndpoint("s3-us-west-2.amazonaws.com");
		
		long beginTime = System.currentTimeMillis();
							
		try ( Transaction tx = graphDb.beginTx() ) {
			try (ResourceIterator<Node> nodes = graphDb.findNodes(type)) {
				while (nodes.hasNext()) {
					Node node = nodes.next();
					if (isValid(node, sources)) 
						processNode(node);
				}
			} 
		}
		
		long endTime = System.currentTimeMillis();
		
		System.out.println(String.format("Done. Exported %d nodes over %d ms. Average %f ms per node", 
				nodeCounter, endTime - beginTime, (float)(endTime - beginTime) / (float) nodeCounter));
	} 

	private boolean isValid(Node node,  Map<Label, Label[]> sources) {
		if (!node.hasProperty(GraphUtils.PROPERTY_ORIGINAL_KEY))
			return false;
		
		for (Map.Entry<Label, Label[]> entry : sources.entrySet()) {
			if (node.hasLabel(entry.getKey())) {
				if (entry.getValue() == null)
					return true;
				
				Iterable<Relationship> relationships = node.getRelationships();
				for (Relationship relationship : relationships) {
					Node other = relationship.getOtherNode(node);
					for (Label label : entry.getValue())
						if (other.hasLabel(label))
							return true;
				}
			}
		}
		
		return false;
	}

	/**
	 * Function to check if node has all listed labels
	 * @param node Node. A node to test
	 * @param labels Collection of labels 
	 * @return true if node has all labels, false otherway
	 */
	/*private boolean hasLabels(Node node, Collection<Label> labels) {
		for (Label label : labels)
			if (!node.hasLabel(label))
				return false;
		
		return true;
	}*/
	
	/**
	 * Function to process a single node
	 * @param node Node to export
	 */
	private void processNode(Node node) {
		try {
			Set<String> jsonNames = generateName(node);
			if (null == jsonNames) {
				System.out.println("Unable to generate json name for node: " + node.getId());
				return;
			}
			
		//	jsonName += ".json";
			
			long rootId = node.getId();
			Map<Long, Node> graphNodes = new HashMap<Long, Node>();
				
			graphNodes.put(node.getId(), node);
			String type = getNodeType(node);
			if (!StringUtils.isEmpty(type) && isDatasetType(type)) { //  && !isInstitutionType(type)
				List<Node> root = new ArrayList<Node>();
				root.add(node);
						
				exctractNodes(graphNodes, root, maxLevel);
			}
					
			System.out.println("Done! Found " + graphNodes.size() + " unique nodes.");
					
			JsonGraph jsonGraph = new JsonGraph();
			for (Node graphNode : graphNodes.values()) {
				JsonNode jsonNode = extractNode(graphNode);
				if (graphNode.getId() == rootId)
					jsonNode.addExtra(JsonNode.EXTRA_ROOT);
				
				jsonGraph.addNode(jsonNode);
	
			//	type = getNodeType(graphNode);
			//	boolean isDoP = isDatasetType(type) || isPublicationType(type);
			//	boolean isI = isInstitutionType(type);
				
				Iterable<Relationship> relationships = graphNode.getRelationships();
				if (null != relationships) 
					for (Relationship relationship : relationships) {
						if (relationship.getStartNode().getId() == graphNode.getId()) {
							// Only outgoing relationships need to be exported, because 
							// we will process all nodes and each relationship will be output twice.
							// If both relationship nodes wasn't selected, the relationship will be 
							// ignored and node will have 'incomplete' flag attached.
							
							// first check if node exists in the map
							Node otherNode = graphNodes.get(relationship.getEndNode().getId());
							if (null == otherNode) 
								jsonNode.addExtra(JsonNode.EXTRA_INCOMPLETE);
							else
								jsonGraph.addRelationship(extractRelationship(relationship));
								
							
						/*	
							
								jsonNode.addExtra(JsonNode.EXTRA_INCOMPLETE);
							else if (isDoP) { // if parent was Dataset or Publication, do not export relationship to institution
								type = getNodeType(otherNode);
								if (isInstitutionType(type))
									jsonNode.addExtra(JsonNode.EXTRA_INCOMPLETE);
								else
									jsonGraph.addRelationship(extractRelationship(relationship));
							} else if (isI) { // if parent was Institution, do not export relationship to Dataset or Publication
								type = getNodeType(otherNode);
								if (isDatasetType(type) || isPublicationType(type))
									jsonNode.addExtra(JsonNode.EXTRA_INCOMPLETE);
								else
									jsonGraph.addRelationship(extractRelationship(relationship));
							} else // export all other existing relationships
								jsonGraph.addRelationship(extractRelationship(relationship));*/
						} else if (!graphNodes.containsKey(relationship.getStartNode().getId()))
							jsonNode.addExtra(JsonNode.EXTRA_INCOMPLETE);
					}
			}
			
			if (jsonGraph.getNodes() != null && jsonGraph.getNodes().size() > 1) {
				String jsonString = mapper.writeValueAsString(jsonGraph);
			
			/*File jsonFile = new File(outputFolder, JSON_FILE);
			Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(jsonFile), "utf-8"));
			
			writer.write(jsonString);
			writer.close();
			*/
			
				for (String jsonName : jsonNames) {
					System.out.println("Put Object: " + s3Bucket + "/" + s3Key + jsonName);
					
					byte[] bytes = jsonString.getBytes(charset);
					
					ObjectMetadata metadata = new ObjectMetadata();
					metadata.setContentEncoding(CONTENT_ENCODING);
					metadata.setContentType(CONTENT_TYPE);
					metadata.setContentLength(bytes.length);
					
					InputStream inputStream = new ByteArrayInputStream(bytes);
					
					PutObjectRequest request = new PutObjectRequest(s3Bucket, s3Key + jsonName, inputStream, metadata);
					if (publicReadRights)
						request.setCannedAcl(CannedAccessControlList.PublicRead);
					
					s3client.putObject(request);
											
					++nodeCounter;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Function to return the name of exporting Java file (without leading .json). 
	 * Must be implemented in the final exporter class.
	 * @param node Node The node to export
	 * @return json - JSON file name
	 */
	public Set<String> generateName(Node node) {
		if (node.hasProperty(GraphUtils.PROPERTY_ORIGINAL_KEY)) {
			Set<String> keys = new HashSet<String>();
			
			Object originalKeys = node.getProperty(GraphUtils.PROPERTY_ORIGINAL_KEY);
			if (originalKeys instanceof String) {
				try {
					keys.add(URLEncoder.encode((String) originalKeys, StandardCharsets.UTF_8.name()) + ".json");
				} catch (Exception e) { }
				
			} else if (originalKeys instanceof String[]) {
				for (String originalKey : (String[]) originalKeys) { 
					try {
						keys.add(URLEncoder.encode(originalKey, StandardCharsets.UTF_8.name()) + ".json");
					} catch (Exception e) { }
				}
			}
			
			if (!keys.isEmpty())
				return keys;
		}
		
		return null;
	}
	
	/**
	 * Function to extract all syblings
	 * @param graph
	 * @param nodes
	 * @param level
	 */
	private void exctractNodes(Map<Long, Node> graph, List<Node> nodes, int level) {
		// create empty array for siblings
		List<Node> siblings = level > 0 ? new ArrayList<Node>() : null; 
		
		// process all nodes
		for (Node node : nodes) {
			
			// extract node type
			String type = getNodeType(node);
			
			// test that node type exists
			if (!StringUtils.isEmpty(type)) {
			
				// record if node is either dataset or publication
			//	boolean isDoP = isDatasetType(type) || isPublicationType(type);
			
				// extract all node relationships
				Iterable<Relationship> relationships = node.getRelationships();
				
				// check if relatioonships exists
				if (null != relationships) {
					
					// new siblings counter
					int nSiblings = 0;
					
					// process all relationships
					for (Relationship relationship : relationships) {
						
						// extract other node
						Node other = relationship.getOtherNode(node);
						
						// save node only if it has not been saved before 
						if (!graph.containsKey(other.getId())) {
							
							// extract other node type
							type = getNodeType(other);
							
							// check that type is valid
							if (!StringUtils.isEmpty(type)) {
								
								// record if node is institution
								//boolean isI = isInstitutionType(type);
							
								// do not extract relationship between  dataset / publication and institution 
								//if (!isDoP || !isI) {
								
								// do not extract institutions
								if (!isInstitutionType(type)) {

									// extract node once
									graph.put(other.getId(), other);
	
									// abort the search if nodes cap has been reached
									if (maxNodes > 0 && graph.size() >= maxNodes) 
										return;
							
									// add node to the syblings array, if need to check it's siblings as well
									//if (level > 0 && (maxSiblings <= 0 || nSiblings < maxSiblings) && !isI) {
									if (level > 0 && (maxSiblings <= 0 || nSiblings < maxSiblings)) {
										siblings.add(other);
										++nSiblings;
									}
								}
							}
						}
					}
				}
			}
		}
	
		// process selected syblings
		if (level > 0 && siblings.size() > 0)
			exctractNodes(graph, siblings, level - 1);
		
	}
	
	/**
	 * Function to return node type
	 * @param node
	 * @return
	 */
	private String getNodeType(Node node) {
		return ((String) node.getProperty(GraphUtils.PROPERTY_TYPE, null));
	}
	
	/*private boolean isValidType(Node node) {
		String type = getNodeType(node);
		return type == null ? false : !type.equals(AggrigationUtils.LABEL_INSTITUTION_LOWERCASE);
	}*/
		
	/**
	 * Function to test is node type is Institution
	 * @param type
	 * @return
	 */
	private boolean isInstitutionType(String type) {
		return type.equals(GraphUtils.TYPE_INSTITUTION);
	}
	
	/**
	 * Function to test is node type is Dataset
	 * @param type
	 * @return
	 */
	private boolean isDatasetType(String type) {
		return type.equals(GraphUtils.TYPE_DATASET);
	}
	
	/**
	 * Function to test is node type is Publication
	 * @param type
	 * @return
	 */
	/*private boolean isPublicationType(String type) {
		return type.equals(GraphUtils.TYPE_PUBLICATION);
	}*/

	/**
	 * Function to extract Node data
	 * @param node
	 * @return
	 */
	private JsonNode extractNode(Node node) {
		String type = getNodeType(node);
		if (null == type)
			return null;
		
		JsonNode jsonNode = new JsonNode();
		jsonNode.setId(node.getId());
		jsonNode.setType(type);
				
		Iterable<String> keys = node.getPropertyKeys();
		for (String key : keys) 
			jsonNode.addProperty(key, node.getProperty(key));
		
		return jsonNode;
	}
	
	/**
	 * Function to extract relationship data
	 * @param relationship
	 * @return
	 */
	private JsonRelationship extractRelationship(Relationship relationship) {
		JsonRelationship jsonRelationship = new JsonRelationship();
		
		jsonRelationship.setId(relationship.getId());
		jsonRelationship.setFrom(relationship.getStartNode().getId());
		jsonRelationship.setTo(relationship.getEndNode().getId());
		jsonRelationship.setType(relationship.getType().name());
		
		return jsonRelationship;
	}
}
