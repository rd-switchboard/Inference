package org.rdswitchboard.linkers.researchers;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.rdswitchboard.utils.neo4j.local.Neo4jUtils;
import org.rdswitchboard.utils.aggrigation.AggrigationUtils;
import org.rdswitchboard.utils.aggrigation.AggrigationUtils.RelTypes;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.ResourceIterable;
import org.neo4j.graphdb.Transaction;
import org.neo4j.tooling.GlobalGraphOperations;

import com.google.common.collect.Sets;

public class Linker {
	private static final int MAX_COMMANDS = 1024;
	
	private GraphDatabaseService graphDb;
	private GlobalGraphOperations global;
	
	private Pattern patternOrcid = Pattern.compile("\\d{4}-\\d{4}-\\d{4}-\\d{4}");
		
	private Map<String, Set<Long>> researchers = new HashMap<String, Set<Long>>();
	
	/**
	 * Class constructor 
	 * 
	 * @param nodeSource
	 * @param nodeType
	 * @param propertyName
	 * @param dbFolder
	 * @param outputFolder
	 * @throws UnsupportedEncodingException 
	 * @throws FileNotFoundException 
	 */
	public Linker(final String dbFolder) throws FileNotFoundException, UnsupportedEncodingException {
		System.out.println(" Neo4j folder: " + dbFolder);
		
		graphDb = Neo4jUtils.getGraphDb(dbFolder);
		global = Neo4jUtils.getGlobalOperations(graphDb);
		
	//	logger = new PrintWriter("relationships.log", StandardCharsets.UTF_8.name());
	}
	
	/**
	 * Function to perform a export
	 * @throws JsonGenerationException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	public void process() {
		loadOrcids("RDA", "Researcher", "identifier_orcid");
		loadOrcids("Orcid", "Researcher", "orcid_id");
		loadOrcids("FigShare", "Researcher", "orcid");
		loadOrcids("CrossRef", "Researcher", "orcid");
		
		createRelationships();
	}
	
	/**
	 * Function to create single GraphField class fron Node class
	 * @param node
	 */
	private void loadOrcids(String source, String type, String propertyOrcid) {
		long nodeCounter = 0;
		System.out.println("Processing " + source + ":" + type + "(" + propertyOrcid + ")");
		
		Label labelSource = DynamicLabel.label(source);
		Label labelType = DynamicLabel.label(type);
			
		try ( Transaction tx = graphDb.beginTx() ) {
			ResourceIterable<Node> nodes = global.getAllNodesWithLabel( labelSource );
			for (Node node : nodes) 
				if (node.hasLabel( labelType ) && node.hasProperty( propertyOrcid )) {
					Object value = node.getProperty( propertyOrcid );
					if (null != value) {
						Long nodeId = node.getId();
						if (value instanceof String) {
							loadOrcid((String) value, nodeId);
						} else if (value instanceof String[]) {
							for (String orcid : (String[]) value)
								loadOrcid(orcid, nodeId);
						} else if (value instanceof Iterable<?>) {
							for (Object orcid : (Iterable<?>) value) 
								loadOrcid((String) orcid, nodeId);
						}
						
						++nodeCounter;
					}
				}
					
		}
		
		System.out.println("Done. Loaded " + nodeCounter + " nodes");
	}
	
	private void loadOrcid(String orcid, Long nodeId) {
		orcid = extractOrcid(orcid);

		if (null != orcid) {
			if (researchers.containsKey(orcid))
				researchers.get(orcid).add(nodeId);
			else
				researchers.put(orcid, Sets.newHashSet(nodeId));
		}
	}
	
	private void createRelationships() {
		System.out.println("Processing " + researchers.size() + " unique ORCID's"); 
		System.out.println("Creating missing relationships"); 

		long relCounter = 0;
		long packetCounter = 0;

		Transaction tx = graphDb.beginTx();
		try {
			for (Entry<String, Set<Long>> entry : researchers.entrySet()) {
				System.out.println("Processing ORCID: " + entry.getKey());
				
				relCounter += AggrigationUtils.createMissingRelationships(graphDb, 
						entry.getValue(), RelTypes.knownAs, Direction.BOTH);

				if ((relCounter - packetCounter) >= MAX_COMMANDS) {
					tx.success();
					tx.close();

					tx = graphDb.beginTx();
					
					packetCounter = relCounter;
				}			
			}
			
			if (relCounter >= packetCounter) 
				tx.success();
			
		} catch (Exception e) {
			tx.failure();
			
			throw e;
		} finally {
			tx.close();
		}
		
		System.out.println("Done. Created " + relCounter + " new relationships");
	}
	
	private String extractOrcid(String str) {
		 Matcher matcher = patternOrcid.matcher(str);
		 if (matcher.find())
			  return matcher.group();
		
		 return null;
	}
}
