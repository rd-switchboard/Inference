# Neo4j Functions

This library contains RD-Switchborad clases to work with Neo4j Graph database

#### Neo4jUtils

Old and depricated version of the Neo4j helper class. This class contains only static functions.
This class will be removed from future version of the Library. All code depending on it, must use
Neo4jDatabase class instead

#### Neo4jDatabase

New and advanced helper class for a Neo4j Database. This class protects a link to a Neo4j and manges
Neo4j transactions. It will also restart the transaction if it will become too big making sure the 
Neo4j will not exceed the memory limit.

Some Examples:

```java
// To load a Neo4j Database
Neo4jDatabase importer = new Neo4jDatabase(neo4jFolder);

// To make verbose output
importer.setVerbose(true);

// To import a graph into Neo4j
importer.importGraph(graph);

// To load a Neo4j database in readn only mode:
Neo4jDatabase readOnly = new Neo4jDatabase(neo4jFolder2, true);

// To enumerate all nodes from ANDS with contains DOI 
readOnly.enumrateAllNodesWithLabelAndProperty(GraphUtuls.SOURCE_ANDS, GraphUtils.PROPERTY_DOI, new ProcessNode() {
	@Override
	boolean processNode(Node node) throws Exception {
		String doi = node.getProperty(GraphUtils.PROPERTY_DOI);
	}
}):

```






