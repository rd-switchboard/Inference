package org.rdswitchboard.linkers.publications;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
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
	
//	private PrintWriter logger; 
	
	private static final String PART_DOI1 = "doi:";
	private static final String PART_DOI2 = "dx.doi.org/";
	
	private Pattern patternDoi = Pattern.compile("(^|doi:|dx\\.doi\\.org/)\\d{2}\\.\\d{4,}/.+$");
	//private Pattern patternDoi = Pattern.compile("(^|:|/)\\d{2}\\.\\d{4,}/");
		
	private Map<String, Set<Long>> publications = new HashMap<String, Set<Long>>();
	
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
		loadDois("Orcid", "Work", "identifier_DOI");
		loadDois("CrossRef", "Publication", "key");
		loadDois("Dryad", "Publication", "doi");
		loadDois("FigShare", "Publication", "doi");
		
		createRelationships();
	}
	
	/**
	 * Function to create single GraphField class fron Node class
	 * @param node
	 */
	private void loadDois(String source, String type, String propertyDoi) {
		long nodeCounter = 0;
		System.out.println("Processing " + source + ":" + type + "(" + propertyDoi + ")");
		
		Label labelSource = DynamicLabel.label(source);
		Label labelType = DynamicLabel.label(type);
			
		try ( Transaction tx = graphDb.beginTx() ) {
			ResourceIterable<Node> nodes = global.getAllNodesWithLabel( labelSource );
			for (Node node : nodes) 
				if (node.hasLabel( labelType ) && node.hasProperty( propertyDoi )) {
					Object value = node.getProperty( propertyDoi );
					if (null != value) {
						Long nodeId = node.getId();
						if (value instanceof String) {
							loadDoi((String) value, nodeId);
						} else if (value instanceof String[]) {
							for (String doi : (String[]) value)
								loadDoi(doi, nodeId);
						} else if (value instanceof Iterable<?>) {
							for (Object doi : (Iterable<?>) value) 
								loadDoi((String) doi, nodeId);
						}
						
						++nodeCounter;
					}
				}
					
		}
		
		System.out.println("Done. Loaded " + nodeCounter + " nodes");
	}
	
	private void loadDoi(String doi, Long nodeId) {
		doi = extractDoi(doi);

		if (null != doi) {
			if (publications.containsKey(doi))
				publications.get(doi).add(nodeId);
			else
				publications.put(doi, Sets.newHashSet(nodeId));
		}
	}
	
	private void createRelationships() {
		System.out.println("Processing " + publications.size() + " unique DOI's"); 
		System.out.println("Creating missing relationships"); 

		long relCounter = 0;
		long packetCounter = 0;

		Transaction tx = graphDb.beginTx();
		try {
			for (Entry<String, Set<Long>> entry : publications.entrySet()) {
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
	
	/*
	private void createRelationships() {
		System.out.println("Processing " + publications.size() + " unique DOI's"); 
		System.out.println("Creating missing relationships"); 

		long relCounter = 0;
		
		List<Set<Long>> nodes = null;
		for (Entry<String, Set<Long>> entry : publications.entrySet()) {
			System.out.println("Processing doi: " + entry.getKey());
			
			if (null == nodes)
				nodes =  new ArrayList<Set<Long>>();
		
			nodes.add(entry.getValue());
			if (nodes.size() >= MAX_COMMANDS) {
				relCounter += createRelationships(nodes);
				nodes = null;
			}			
		}

		if (null != nodes)
			relCounter += createRelationships(nodes);
		
		System.out.println("Done. Created " + relCounter + " new relationships");
	}
	
	private long createRelationships(List<Set<Long>> nodes) {
		long relCounter = 0;
		
		try ( Transaction tx = graphDb.beginTx() ) {
			for (Set<Long> nodeIds : nodes) {
				if (nodeIds.size() > 1) {
					Long[] ids = (Long[]) nodeIds.toArray(new Long[nodeIds.size()]);
					for (int i1 = 0; i1 < ids.length - 1; ++i1) {
						Iterable<Relationship> rels = start.getRelationships(knownAs, Direction.BOTH);
						for (int i2 = i1 + 1; i2 < ids.length; ++i2) {
							if (Neo4jUtils.findRelationship(rels, ids[i2], Direction.BOTH) == null) {
								Node end = graphDb.getNodeById(ids[i2]);
									
								start.createRelationshipTo(end, knownAs);
								++relCounter;
							}							
						}
					}
				}
			}
			
			if (relCounter > 0)
				tx.success();
		}
		
		return relCounter;
	}*/
	
	private String extractDoi(String str) {
		 Matcher matcher = patternDoi.matcher(str);
		 if (matcher.find()) {

			  String doi =  matcher.group();
			  if (doi.startsWith(PART_DOI1))
				  doi = doi.substring(PART_DOI1.length());
			  else if (doi.startsWith(PART_DOI2))
				  doi = doi.substring(PART_DOI2.length());

			//  System.out.println("string: " + str + " doi: " + doi);
			  
			  return doi;
		 }
		
		//return doi.replace("doi:", "").replace("http://dx.doi.org/", "");
		 return null;
	}
}
