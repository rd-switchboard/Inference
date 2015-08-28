package org.rdswitchboard.utils.neo4j.delete;

import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.ResourceIterable;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.Transaction;
import org.neo4j.tooling.GlobalGraphOperations;
import org.rdswitchboard.libraries.graph.GraphUtils;
import org.rdswitchboard.libraries.neo4j.Neo4jUtils;

public class App {
	public static void main(String[] args) {
		GraphDatabaseService graphDb = Neo4jUtils.getGraphDb("neo4j-aggrigator");
		
		//deleteNodesWithLabel(graphDb, DynamicLabel.label(GraphUtils.SOURCE_CROSSREF));
		//deleteNodesWithLabels(graphDb, DynamicLabel.label(GraphUtils.SOURCE_WEB), DynamicLabel.label(GraphUtils.TYPE_PUBLICATION));
		deleteOrpahnNodes(graphDb);
	}
	
	public static void deleteNodesWithLabel(GraphDatabaseService graphDb, Label label) {
		System.out.println("Deleteing nodes with label: " + label.name());
		try (Transaction tx = graphDb.beginTx()) {
			try (ResourceIterator<Node> nodes = graphDb.findNodes(label)) {
				while (nodes.hasNext()) 
					deleteNode(nodes.next());
			}
			
			tx.success();
		}
	}

	public static void deleteNodesWithLabels(GraphDatabaseService graphDb, Label label1, Label label2) {
		System.out.println("Deleteing nodes with label: " + label1.name() + ":" + label2.name());
		try (Transaction tx = graphDb.beginTx()) {
			try (ResourceIterator<Node> nodes = graphDb.findNodes(label1)) {
				while (nodes.hasNext()) {
					Node node = nodes.next();
					if (node.hasLabel(label2))
						deleteNode(node);
				}
			}
			
			tx.success();
		}
	}

	public static void deleteOrpahnNodes(GraphDatabaseService graphDb) {
		System.out.println("Deleteing Orpahn nodes");
		Transaction tx = graphDb.beginTx();
		try {
			
			int counter = 0;
			int pkg = 0; 
			GlobalGraphOperations global = Neo4jUtils.getGlobalOperations(graphDb);
			ResourceIterable<Node> nodes = global.getAllNodes();
			for (Node node : nodes) 
				if (!node.hasRelationship()) {
					deleteNode(node);
					
					if (++counter >= 1000) {
						System.out.println("Package: " + (++pkg));
						
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
