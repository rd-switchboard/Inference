package org.rdswitchboard.utils.neo4j.test.connections;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.parboiled.common.StringUtils;
import org.rdswitchboard.libraries.graph.GraphUtils;
import org.rdswitchboard.libraries.neo4j.Neo4jDatabase;
import org.rdswitchboard.libraries.neo4j.interfaces.ProcessNode;


public class App {
	private static final String PROPERTIES_FILE = "properties/test_connections.properties";
	private static final String NEO4J_FOLDER = "neo4j";
	private static final String MIN_CONNECTIONS = "100";
		
	public static void main(String[] args) {
		try {
            String propertiesFile = PROPERTIES_FILE;
            if (args.length > 0 && !StringUtils.isEmpty(args[0])) 
                    propertiesFile = args[0];

            Properties properties = new Properties();
	        try (InputStream in = new FileInputStream(propertiesFile)) {
	            properties.load(in);
	        }
	        
	        System.out.println("Linking Nodes");
	        	        
	        String neo4jFolder = properties.getProperty("neo4j", NEO4J_FOLDER);
	        if (StringUtils.isEmpty(neo4jFolder))
	            throw new IllegalArgumentException("Neo4j Folder can not be empty");
	        System.out.println("Neo4J: " + neo4jFolder);
	        
	        final int minConnections = Integer.parseInt(properties.getProperty("min.connections", MIN_CONNECTIONS));
	        System.out.println("Min Connections: " + minConnections);

	        final Label labelWeb = DynamicLabel.label(GraphUtils.SOURCE_WEB);
	        final Label labelResearcher = DynamicLabel.label(GraphUtils.TYPE_RESEARCHER);
	        
	        Neo4jDatabase neo4j = new Neo4jDatabase(neo4jFolder, true); // read only database
	        
//	        System.out.println("Counting Connections");
	        
//	        countConnections(neo4j, Graph)
	        
	        System.out.println("Testing Connections to Web:Researcher");

	        neo4j.enumrateAllNodes(new ProcessNode() {

				@Override
				public boolean processNode(Node node) throws Exception {
					int counter = 0;
					
					Iterable<Relationship> relationships = node.getRelationships();
					for (Relationship relationship : relationships) {
						Node other = relationship.getOtherNode(node);
						if (other.hasLabel(labelWeb) && other.hasLabel(labelResearcher))
							++counter;
					}
					
					if (counter >= minConnections) {
						System.out.println(String.format("Connections: %d, node id: %d, title: %s, source: %s, type : %s", 
								counter,
								node.getId(),
								node.getProperty(GraphUtils.PROPERTY_TITLE),
								node.getProperty(GraphUtils.PROPERTY_SOURCE),
								node.getProperty(GraphUtils.PROPERTY_TYPE)));
					}
					
					return true;
				}
	        	
	        });
	        
	        
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/*
	private static void countConnections(Neo4jDatabase neo4j, String source1, String source2) {
		System.out.println("Connections between " + source1 + 
				" and " + source2 + 
				" : " + neo4j.getSourcesConnectionsCount(source1, source2));
	}*/
}
