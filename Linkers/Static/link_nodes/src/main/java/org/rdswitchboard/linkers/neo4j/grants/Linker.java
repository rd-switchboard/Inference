package org.rdswitchboard.linkers.neo4j.grants;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.Transaction;
import org.rdswitchboard.libraries.graph.GraphUtils;
import org.rdswitchboard.libraries.neo4j.Neo4jException;
import org.rdswitchboard.libraries.neo4j.Neo4jUtils;

public class Linker {
	private GraphDatabaseService graphDb;
	
	private RelationshipType relKnownAs = DynamicRelationshipType.withName( GraphUtils.RELATIONSHIP_KNOWN_AS );
	
	private boolean verbose = false;
	//private long processed = 0;
	//private long linked = 0;
//	private long skyped = 0;
	
	public Linker(final String neo4jFolder) throws Neo4jException {
		graphDb = Neo4jUtils.getGraphDb( neo4jFolder );
	}
	
	public boolean isVerbose() {
		return verbose;
	}

	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}
	
	public void link() throws Exception {
		// grants
		
		linkNodes(GraphUtils.TYPE_GRANT, 
				GraphUtils.SOURCE_ARC, GraphUtils.PROPERTY_ARC_ID, 
				GraphUtils.SOURCE_ANDS, GraphUtils.PROPERTY_ARC_ID);
		linkNodes(GraphUtils.TYPE_GRANT, 
				GraphUtils.SOURCE_NHMRC, GraphUtils.PROPERTY_NHMRC_ID, 
				GraphUtils.SOURCE_ANDS, GraphUtils.PROPERTY_NHMRC_ID);

		// publication
		linkNodes(GraphUtils.TYPE_PUBLICATION, GraphUtils.PROPERTY_DOI, new String[] { 
				GraphUtils.SOURCE_CROSSREF, // 10453
				GraphUtils.SOURCE_DARA,     // 57056
				GraphUtils.SOURCE_CERN,     // 174648
				GraphUtils.SOURCE_DLI,      // 306022
				GraphUtils.SOURCE_ORCID }); // 4618279
		
		// researcher
		linkNodes(GraphUtils.TYPE_RESEARCHER, GraphUtils.PROPERTY_ORCID_ID, new String[] { 
				GraphUtils.SOURCE_CROSSREF, 
				GraphUtils.SOURCE_ANDS,
				GraphUtils.SOURCE_ORCID });
					
		// Link Datasets
		linkNodes(GraphUtils.TYPE_DATASET, GraphUtils.PROPERTY_DOI, new String[] { 
				GraphUtils.SOURCE_DARA,   // 7842
				GraphUtils.SOURCE_ANDS,   // 43701
				GraphUtils.SOURCE_DRYAD,  // 57474
				GraphUtils.SOURCE_DLI }); // 1199379
	}
	
	private void linkNodes(String type, String property, String[] sources) {
		for (int i = 0; i < sources.length -1; ++i) {
			for (int j = i + 1; j < sources.length; ++j) {
				linkNodes(type, sources[i], property, sources[j], property);
			}
		}
 	}
	
	private void linkNodes(String type, 
			String srcLabel, String srcProperty, String dstLabel, String dstProperty) {
		System.out.println(String.format("Linkng %s:%s ( %s ) with %s:%s ( %s )", 
				type, srcLabel, srcProperty, type, dstLabel, dstProperty));
		
		Label labelType = DynamicLabel.label(type);
		Label labelSrc = DynamicLabel.label(srcLabel);
		Label labelDst = DynamicLabel.label(dstLabel);
		
		try ( Transaction tx = graphDb.beginTx() ) {
			Neo4jUtils.createIndex(graphDb, labelSrc, srcProperty);
			Neo4jUtils.createIndex(graphDb, labelDst, dstProperty);
			
			tx.success();
		}
		
		long processed = 0;
		long linked = 0;
		
		try ( Transaction tx = graphDb.beginTx() ) {
			
		/*	String cypher = String.format("MATCH (a:%s:%s), (b:%s:%s) WHERE HAS (a.%s) AND HAS (b.%s) AND a.%s=b.%s CREATE (a)-[x:%s]->(b) RETURN DISTINCT COUNT(x) AS n",
					type, srcLabel,
					type, dstLabel,
					srcProperty, dstProperty, 
					srcProperty, dstProperty,
					GraphUtils.RELATIONSHIP_KNOWN_AS);
			
			long linked = 0;
			
			try ( Result r =  graphDb.execute(cypher) ) {
				if (r.hasNext()){
					Map<String, Object> row = r.next();
					if (row.containsKey("n")) {
						long n = (long) row.get("n");
						
						linked += n;						
					}					
				}
			}
			
			if (verbose)
				System.out.println(String.format("Done. Found %s new links", linked));*/

		//	this.linked += linked;

			
			//Index<Node> index = graphDb.index().forNodes(dstLabel);
			try (ResourceIterator<Node> nodes = graphDb.findNodes( labelSrc )) {
				while (nodes.hasNext()) {
					Node node = nodes.next();
					if (node.hasLabel(labelType) && node.hasProperty(srcProperty)) {
						long id = node.getId();
						Object value = node.getProperty( srcProperty );
					
						if (verbose)
							System.out.println("Processing node: " + id);
						
						++processed;
						
						try (ResourceIterator<Node> hits = graphDb.findNodes(labelDst, dstProperty, value)) {
							if (hits.hasNext()) {
								Node hit = hits.next();
								if (hit.hasLabel(labelType)) {
									if (verbose)
										System.out.println("Establish a link with node: " + hit.getId());
									
									Neo4jUtils.createUniqueRelationship(node, hit, relKnownAs, Direction.BOTH, null);
									
									++linked;
								}
							}
						}
					}
				}
			}
			
			tx.success();
		}
		
		System.out.println(String.format("Processed %d nodes", processed));
		System.out.println(String.format("Linked %d nodes", linked));
	}
	
/*	public void printStatistics(PrintStream out) {
		out.println(String.format("Totally Processed %d nodes", processed));
		out.println(String.format("Totally Linked %d nodes", linked));
	}*/	
}
