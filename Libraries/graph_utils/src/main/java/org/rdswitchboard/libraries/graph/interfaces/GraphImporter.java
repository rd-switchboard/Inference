package org.rdswitchboard.libraries.graph.interfaces;

import java.util.Collection;

import org.rdswitchboard.libraries.graph.Graph;
import org.rdswitchboard.libraries.graph.GraphNode;
import org.rdswitchboard.libraries.graph.GraphRelationship;
import org.rdswitchboard.libraries.graph.GraphSchema;

public interface GraphImporter {
	void importGraph(Graph graph);
	
	void importNode(GraphNode node);
	void importNodes(Collection<GraphNode> nodes);
	
	void importSchema(GraphSchema schema);
	void importSchemas(Collection<GraphSchema> schemas); 
	
	void importRelationship(GraphRelationship relationship);
	void importRelationships(Collection<GraphRelationship> relationships);
	
	boolean isVerbose();
	void setVerbose(boolean verbose);
}
