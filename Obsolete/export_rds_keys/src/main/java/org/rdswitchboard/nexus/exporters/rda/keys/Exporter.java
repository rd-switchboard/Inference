package org.rdswitchboard.nexus.exporters.rda.keys;

import java.io.FileNotFoundException;
import java.io.PrintWriter;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.ResourceIterable;
import org.neo4j.graphdb.Transaction;
import org.neo4j.tooling.GlobalGraphOperations;
import org.rdswitchboard.utils.aggrigation.AggrigationUtils;
import org.rdswitchboard.utils.neo4j.Neo4jUtils;

public class Exporter {
	private static final String ANDS_SITE = "researchdata.ands.org.au";
	
	private GraphDatabaseService graphDb;
	private GlobalGraphOperations global;
	
	public Exporter(String dbFolder) {
		System.out.println("Source Neo4j folder: " + dbFolder);
		
		// Open the Graph Database
		graphDb = Neo4jUtils.getReadOnlyGraphDb(dbFolder);
		global = Neo4jUtils.getGlobalOperations(graphDb);
	}
	
	public void process() throws FileNotFoundException {
		long beginTime = System.currentTimeMillis();
		long nodeCounter = 0;
		
		try ( PrintWriter out = new PrintWriter("rda_keys.csv") ) {
			out.println("slug,id");
				
			try ( Transaction tx = graphDb.beginTx() ) {
				// query all RDA nodes
			
				ResourceIterable<Node> nodes = global.getAllNodesWithLabel(AggrigationUtils.Labels.RDA);
				for (Node node : nodes) {
					String key = (String) node.getProperty(AggrigationUtils.PROPERTY_KEY);
					if (null != key && key.startsWith(ANDS_SITE)) {
							
						System.out.println("=============================");	
						System.out.println("Exporting RDA record with key: " + node.getProperty(AggrigationUtils.PROPERTY_KEY));	
						
						// check if slug exists
						String[] arr = key.split("/");
						if (arr.length >= 3) {
							
							boolean valid = false;
							Iterable<Relationship> relationships = node.getRelationships();
							for (Relationship rel : relationships) {
								Node other = rel.getOtherNode(node);
								if (other.getId() != node.getId() && !isInstitutionType(getNodeType(other))) {
									valid = true;
									break;
								}
							}
	
							if (valid)							
								out.println(arr[1] + "," + arr[2]);
						}
					}
					
					++nodeCounter;
				}
			}
		}
		
		long endTime = System.currentTimeMillis();
		
		System.out.println(String.format("Done. Exported %d keys over %d ms. Average %f ms per key", 
				nodeCounter, endTime - beginTime, (float)(endTime - beginTime) / (float) nodeCounter));
	}
	
	private String getNodeType(Node node) {
		return ((String) node.getProperty(AggrigationUtils.PROPERTY_NODE_TYPE, null));
	}
		
	private boolean isInstitutionType(String type) {
		return type.equals(AggrigationUtils.LABEL_INSTITUTION_LOWERCASE);
	}
}
