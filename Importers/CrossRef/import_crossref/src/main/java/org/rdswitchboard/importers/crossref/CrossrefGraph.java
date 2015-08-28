package org.rdswitchboard.importers.crossref;

import org.rdswitchboard.libraries.crossref.Author;
import org.rdswitchboard.libraries.crossref.CrossRef;
import org.rdswitchboard.libraries.crossref.Item;
import org.rdswitchboard.libraries.graph.Graph;
import org.rdswitchboard.libraries.graph.GraphNode;
import org.rdswitchboard.libraries.graph.GraphRelationship;
import org.rdswitchboard.libraries.graph.GraphUtils;


public class CrossrefGraph extends CrossRef {

	private static final String PART_DOI = "doi:";
	/**
	 * Function to Query CrossRef metadata 
	 * @param doi An DOI in a format, returned by GraphUtils.extractDoi() function 
	 * @return Grah
	 */
	
	public GraphNode queryGraph(Graph graph, String doi) {
		// make sure we have doi
		Item item = requestWork(PART_DOI + doi);
		if (null != item) {
			String doiUri = GraphUtils.generateDoiUri(doi);
			GraphNode nodePublication = new GraphNode()
				.withKey(GraphUtils.SOURCE_CROSSREF, doiUri)
				.withSource(GraphUtils.SOURCE_CROSSREF)
				.withType(GraphUtils.TYPE_PUBLICATION)
				.withProperty(GraphUtils.PROPERTY_DOI, doi)
				.withProperty(GraphUtils.PROPERTY_URL, doiUri)
//				.withProperty(GraphUtils.PROPERTY_URL, item.getUrl())
//				.withProperty(GraphUtils.PROPERTY_NAME_PREFIX, item.getPrefix())
				.withProperty(GraphUtils.PROPERTY_ISSN, item.getIssn())
				.withProperty(GraphUtils.PROPERTY_TITLE, item.getTitle())
//				.withProperty(GraphUtils.PROPERTY_SUBTITLE, item.getSubject())
//				.withProperty(GraphUtils.PROPERTY_CONTAINER_TITLE, item.getContainerTitle())
//				.withProperty(GraphUtils.PROPERTY_AUTHORS, item.getAuthorString())
//				.withProperty(GraphUtils.PROPERTY_EDITORS, item.getEditorString())
				.withProperty(GraphUtils.PROPERTY_PUBLISHED_DATE, item.getIssuedString());
//				.withProperty(GraphUtils.PROPERTY_DEPOSITED_DATE, item.getDepositedString())
//				.withProperty(GraphUtils.PROPERTY_INDEXED_DATE, item.getIndexedString());
			
			graph.addNode(nodePublication);
			
		/*	String url = GraphUtils.extractFormalizedUrl(item.getUrl());
			if (null != url && !url.equals(doiUri)) {
				GraphNode nodeWeb = new GraphNode()
					.withKey(GraphUtils.SOURCE_WEB, url)
					.withSource(GraphUtils.SOURCE_WEB)
					.withType(GraphUtils.TYPE_PUBLICATION)
					.withProperty(GraphUtils.PROPERTY_URL, url)
					.withProperty(GraphUtils.PROPERTY_TITLE, item.getTitle());
					
				graph.addNode(nodeWeb);
				
				graph.addRelationship(new GraphRelationship()
					.withRelationship(GraphUtils.RELATIONSHIP_KNOWN_AS)
					.withStart(nodePublication.getKey())
					.withEnd(nodeWeb.getKey()));
			}*/
			
			if (null != item.getAuthor())
				for (Author author : item.getAuthor()) {
					String fullName = author.getFullName();
					String key = doi + ":" + fullName;
					
					nodePublication.addProperty(GraphUtils.PROPERTY_AUTHORS, fullName);
					
					GraphNode nodeResearcher = new GraphNode()
						.withKey(GraphUtils.SOURCE_CROSSREF, key)
						.withSource(GraphUtils.SOURCE_CROSSREF)
						.withType(GraphUtils.TYPE_RESEARCHER)
						.withProperty(GraphUtils.PROPERTY_NAME_PREFIX, author.getSuffix())
						.withProperty(GraphUtils.PROPERTY_FIRST_NAME, author.getGiven())
						.withProperty(GraphUtils.PROPERTY_LAST_NAME, author.getFamily())
						.withProperty(GraphUtils.PROPERTY_FULL_NAME, author.getFullName())
						.withProperty(GraphUtils.PROPERTY_ORCID_ID, author.getOrcid());
					
					graph.addNode(nodeResearcher);						
					graph.addRelationship(new GraphRelationship()
						.withRelationship(GraphUtils.RELATIONSHIP_AUTHOR)
						.withStart(nodePublication.getKey())
						.withEnd(nodeResearcher.getKey()));
				}
			
			return nodePublication;
			
		}
		
		return null;
	}
	
	/*
	public void process() {
		crossrefPublications = crossrefResearchers = 0;
		
		processNodes("Dryad:Publication", PROPERTY_REFERENCED_BY);
		
		System.out.println("Done. Imported " + crossrefPublications + " publications and " + crossrefResearchers + " researchers");
	}
	
	@SuppressWarnings("unchecked")
	private void processNodes(String label, String property) {
		StringBuilder sb = new StringBuilder();
		sb.append("MATCH (n:");
		sb.append(label);
		sb.append(") WHERE HAS(n.");
		sb.append(property);
		sb.append(") RETURN ID(n) AS id, n.");
		sb.append(property);
		sb.append(" AS doi");
		
		QueryResult<Map<String, Object>> result = engine.query(sb.toString(), null);
		for (Map<String, Object> row : result) {
			int nodeId = (int) row.get("id");
			Object dois = row.get("doi");
			if (null != dois) {
				RestNode node = graphDb.getNodeById(nodeId);
				if (null != node) {
					if (dois instanceof String) 
						processDoi(node, (String) dois);
					else if (dois instanceof String[])
						for (String doi :(String[]) dois) 
							processDoi(node, doi);
					else if (dois instanceof List<?>)
						for (String doi :(List<String>) dois) 
							processDoi(node, doi);
				}
			}
		}
	}

	private void processDoi(RestNode nodeStart, String doi) {
		if (doi.contains(PART_DOI)) {
			RestNode nodeCrossrefPublication = createCrossRefPublication(doi);
			if (null != nodeCrossrefPublication) {
				Neo4jUtils.createUniqueRelationship(graphDb, nodeStart, nodeCrossrefPublication, 
						Relationhips.knownAs, Direction.OUTGOING, null);	
			}
		}	
	}
	
	private RestNode createCrossRefPublication(final String doi) {
		System.out.println("Processing doi: " + doi);
		
		if (nodesCrossref.containsKey(doi))
			return nodesCrossref.get(doi);
		
		RestNode nodeCrossrefPublication = null;
		
		
		if (null != item) {
			Map<String, Object> map = new HashMap<String, Object>();
			addProperty(map, PROPERTY_URL, item.getUrl());
			addProperty(map, PROPERTY_PREFIX, item.getPrefix());
			addProperty(map, PROPERTY_ISSN, item.getIssn());
			addProperty(map, PROPERTY_TITLE, item.getTitle());
			addProperty(map, PROPERTY_SUBTITLE, item.getSubtitle());
//			addProperty(map, PROPERTY_SUBJECT, item.getSubject());
//			addProperty(map, PROPERTY_CONTAINER_TITLE, item.getContainerTitle());
			addProperty(map, PROPERTY_AUTHOR, item.getAuthorString());
			addProperty(map, PROPERTY_EDITOR, item.getEditorString());
			addProperty(map, PROPERTY_ISSUED, item.getIssuedString());
			addProperty(map, PROPERTY_DEPOSITED, item.getDepositedString());
			addProperty(map, PROPERTY_INDEXED, item.getIndexedString());
					
			nodeCrossrefPublication =  Neo4jUtils.createUniqueNode(graphDb, indexCrossrefPublication, 
						Labels.CrossRef, Labels.Publication, doi, map);
			
			System.out.println("Creating Crosref:Publication: " + doi);

			++crossrefPublications;
				
			if (null != item.getAuthor())
				for (Author author : item.getAuthor()) {
					RestNode nodeCrossrefResearcher = createCrossRefResearcher(doi, author);
					if (null != nodeCrossrefResearcher)
						Neo4jUtils.createUniqueRelationship(graphDb, nodeCrossrefResearcher, nodeCrossrefPublication, 
							Relationhips.author, Direction.OUTGOING, null);	
				}
			
			if (null != item.getEditor())
				for (Author editor : item.getEditor()) {
					RestNode nodeCrossrefResearcher = createCrossRefResearcher(doi, editor);
					
					if (null != nodeCrossrefResearcher)
						Neo4jUtils.createUniqueRelationship(graphDb, nodeCrossrefResearcher, nodeCrossrefPublication, 
								Relationhips.editor, Direction.OUTGOING, null);	
				}
		}
		else
			System.out.println("Unable to fina any crossref record by doi: " + doi);			
	
		nodesCrossref.put(doi, nodeCrossrefPublication);
		
		return nodeCrossrefPublication;
	}
	
	private RestNode createCrossRefResearcher(final String doi, Author author) {
		Map<String, Object> map = new HashMap<String, Object>();
		addProperty(map, PROPERTY_SUFFIX, author.getSuffix());
		addProperty(map, PROPERTY_GIVEN_NAME, author.getGiven());
		addProperty(map, PROPERTY_FAMILY_NAME, author.getFamily());
		addProperty(map, PROPERTY_FULL_NAME, author.getFullName());
		addProperty(map, PROPERTY_ORCID, author.getOrcid());
		
		++crossrefResearchers;
		
		String key = doi + ":" + author.getFullName();
		
		System.out.println("Creating Crosref:Researcher: " + key);
		
		return Neo4jUtils.createUniqueNode(graphDb, indexCrossrefResearcher, 
				Labels.CrossRef, Labels.Researcher, key, map);
	}
	
	/*private void addProperty(Map<String, Object> map, final String key, final Object value) {
		if (null != key && !key.isEmpty() && null != value) {
			if (value instanceof String && ((String) value).isEmpty())
				return;
			
			map.put(key, value);				
		}
	}*/
}
