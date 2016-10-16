package org.rdswitchboard.utils.neo4j.replace;

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
import org.neo4j.graphdb.index.Index;
import org.rdswitchboard.libraries.graph.GraphUtils;
import org.rdswitchboard.libraries.neo4j.Neo4jUtils;

public class App {
	
	public static void main(String[] args) {
		try
		{
			if (args.length != 3)
				System.err.println("Ussage: java -jar replace_source.jar [neo4j Folder] [OLD Label] [New Label]");
			else {
				String neo4j = args[0];
				String oldLabel = args[1];
				String newLabel = args[2];
				
				GraphDatabaseService graphDb = Neo4jUtils.getGraphDb(neo4j);
				
				replaceLabels(graphDb, oldLabel, newLabel);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static void replaceLabels(GraphDatabaseService graphDb, String oldLabel, String newLabel) {

		Label labelOld = DynamicLabel.label(oldLabel);
		Label labelNew = DynamicLabel.label(newLabel);
		
		System.out.println("Creating index for label: " + newLabel);
		
		try (Transaction tx = graphDb.beginTx()) {
			Neo4jUtils.createConstrant(graphDb, newLabel, GraphUtils.PROPERTY_KEY);
			
			tx.success();
		}
				
		Set<Long> nodeIds = new HashSet<Long>();
		
		System.out.println("Quering nodes with label: " + oldLabel);
		
		try (Transaction ignored = graphDb.beginTx()) {
			try (ResourceIterator<Node> nodes = graphDb.findNodes(labelOld)) {
				while (nodes.hasNext()) 
					nodeIds.add(nodes.next().getId());
			}
		} 
		
		System.out.println("Replacing label in " + nodeIds.size() + " nodes");
		
		int counter = 0;

		Transaction tx = graphDb.beginTx();
		try {
			Index<Node> index = Neo4jUtils.getNodeIndex(graphDb, newLabel);	
						
			for (Long id : nodeIds){
				Node node = graphDb.getNodeById(id);
				
				node.setProperty(GraphUtils.PROPERTY_SOURCE, newLabel);
				node.addLabel(labelNew);
				node.removeLabel(labelOld);
				
				index.add(node, GraphUtils.PROPERTY_KEY, node.getProperty(GraphUtils.PROPERTY_KEY));
				
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
}
