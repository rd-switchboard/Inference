package org.rdswitchboard.utils.neo4j.delete;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.ResourceIterable;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.Transaction;
import org.neo4j.tooling.GlobalGraphOperations;
import org.rdswitchboard.libraries.neo4j.Neo4jUtils;

public class App {
	public static void main(String[] args) {
		try {
			if (args.length != 2)
				System.err.println("Ussage: java -jar delete_nodes.jar [neo4j Folder] [Label1,Label2]|[orphants]");
			else {
				String neo4j = args[0];
				String[] labels = args[1].split(",");
				
				if (labels.length == 0)
					System.err.println("Please provide any node labels or `orphant` to delete orphant nodes");
				
				GraphDatabaseService graphDb = Neo4jUtils.getGraphDb(neo4j);
				
				if (labels[0].equals("orphant")) {
					
					deleteOrpahnNodes(graphDb);
					
				} else {
	
					deleteNodesWithLabels(graphDb, labels);			
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			
			System.exit(1);
		}
	}
	
	private static void deleteNodesWithLabels(GraphDatabaseService graphDb, String[] labels) {
		System.out.println("Quering nodes with labes: " + StringUtils.join(labels, ", "));

		Label label = DynamicLabel.label(labels[0]);
		Label[] others = null;
		if (labels.length > 1) {
			others = new Label[labels.length - 1];
			for (int n = 1; n < labels.length; ++n)
				others[n-1] = DynamicLabel.label(labels[n]); 
		}
		
		Set<Long> nodeIds = new HashSet<Long>();
		
		try (Transaction ignored = graphDb.beginTx()) {
			try (ResourceIterator<Node> nodes = graphDb.findNodes(label)) {
				while (nodes.hasNext()) {
					Node node = nodes.next();
					if (null == others || nodeHasLabels(node, others)) 
						nodeIds.add(node.getId());
					
				}
			}
		} 
		
		deleteNodes(graphDb, nodeIds);
	}
	
	private static boolean nodeHasLabels(Node node, Label[] labels) {
		for (Label label : labels)
			if (!node.hasLabel(label)) 
				return false;
		
		return true;
	}

	private static void deleteOrpahnNodes(GraphDatabaseService graphDb) {
		System.out.println("Quering orpahn nodes");

		Set<Long> nodeIds = new HashSet<Long>();
		
		try (Transaction ignored = graphDb.beginTx()) {

			GlobalGraphOperations global = Neo4jUtils.getGlobalOperations(graphDb);
			ResourceIterable<Node> nodes = global.getAllNodes();
			for (Node node : nodes) 
				if (!node.hasRelationship()) 
					nodeIds.add(node.getId());
		} 
		
		deleteNodes(graphDb, nodeIds);
	}
	
	private static void deleteNodes(GraphDatabaseService graphDb, Set<Long> nodeIds) {
		System.out.println("Deleting " + nodeIds.size() + " nodes");
		
		int counter = 0;

		Transaction tx = graphDb.beginTx();
		try {
						
			for (Long id : nodeIds){
				deleteNode(graphDb.getNodeById(id));			
				
				if (++counter >= 1000) {

					tx.success();
					tx.close();
					tx = graphDb.beginTx();
					counter = 0;
				}
			}
			
			tx.success();
		} finally {
			tx.close();
		}
	}
	
	private static void deleteNode(Node node) {
		for (Relationship relationship : node.getRelationships())
			relationship.delete();
					
		node.delete();
	}
}
