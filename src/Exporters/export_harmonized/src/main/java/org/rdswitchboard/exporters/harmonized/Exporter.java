package org.rdswitchboard.exporters.harmonized;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.rdswitchboard.utils.graph.GraphIndex;
import org.rdswitchboard.utils.graph.GraphNode;
import org.rdswitchboard.utils.graph.GraphRelationship;
import org.rdswitchboard.utils.graph.GraphSchema;
import org.rdswitchboard.utils.graph.GraphUtils;
import org.rdswitchboard.utils.neo4j.local.Neo4jUtils;
import org.rdswitchboard.utils.aggrigation.AggrigationUtils;
import org.neo4j.cypher.javacompat.ExecutionEngine;
import org.neo4j.cypher.javacompat.ExecutionResult;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.Transaction;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Exporter {
	private static final int MAX_COMMANDS = 1024;
	
	private GraphDatabaseService graphDb;
//	private GlobalGraphOperations global;
	private ExecutionEngine engine;
	
	private final File schemaFolder;
	private final File nodesFolder;
	private final File relationshipsFolder;
			
	private static final ObjectMapper mapper = new ObjectMapper(); 
	
	private enum Labels implements Label {
		Work, Orcid, FigShare
	}
	
	private static final String PROPERTY_NAME_PRIMARY = "name_primary";
	private static final String PROPERTY_NAME_FORMELY = "name_formerly";
	private static final String PROPERTY_NAME_ALTERNATIVE = "name_alternative";
	private static final String PROPERTY_NAME_TEXT = "name_text";
	private static final String PROPERTY_NAME_FULL = "name_full";
	private static final String PROPERTY_NAME = "name";
	private static final String PROPERTY_IDENTIFIER_NLA = "identifier_nla";
	private static final String PROPERTY_IDENTIFIER_NLA_PARTY = "identifier_nla.party";
	private static final String PROPERTY_IDENTIFIER_PULR = "identifier_purl";
	//private static final String PROPERTY_IDENTIFIER_URI = "identifier_uri";
	private static final String PROPERTY_IDENTIFIER_AU_ANL_PEAU = "identifier_AU-ANL:PEAU";
	private static final String PROPERTY_IDENTIFIER_ORCID = "identifier_orcid";
	private static final String PROPERTY_IDENTIFIER_ARC = "identifier_arc";
	private static final String PROPERTY_IDENTIFIER_NHMRC = "identifier_nhmrc";
	private static final String PROPERTY_IDENTIFIER_DOI = "identifier_doi";
	private static final String PROPERTY_IDENTIFIER_DOI2 = "identifier_DOI";
	private static final String PROPERTY_IDENTIFIER_ISBN = "identifier_ISSN";
	private static final String PROPERTY_GYVEN_NAMES = "gyven_names";
	private static final String PROPERTY_GIVEN_NAME = "given_name";
	private static final String PROPERTY_FAMILY_NAME = "family_name";
	private static final String PROPERTY_ORCID_ID = "orcid_id";
	private static final String PROPERTY_SCIENTIFIC_TITLE = "scientific_title";
	private static final String PROPERTY_SIMPLIFIED_TITLE = "simplified_title";
	private static final String PROPERTY_RESEARCHERS = "researchers";
	private static final String PROPERTY_START_YEAR = "start_year";
	private static final String PROPERTY_APPLICATION_YEAR = "application_year";
	private static final String PROPERTY_AUTHOR = "author";
	private static final String PROPERTY_CONTRIBUTORS = "contributors";
	private static final String PROPERTY_PUBLICATION_DATE = "publication_date";
		
	private List<GraphSchema> graphSchema;
	private List<GraphNode> graphNodes;
	private List<GraphRelationship> graphRelationships;
	
//	private Map<String, Set<Long>> mapKeys = new HashMap<String, Set<Long>>();
	private Map<Long, String> mapInstitutionsKeys = new HashMap<Long, String>();
	private Map<Long, String> mapResearchersKeys = new HashMap<Long, String>();
	private Map<Long, String> mapGrantsKeys = new HashMap<Long, String>();
	private Map<Long, String> mapPublicationsKeys = new HashMap<Long, String>();
	private Map<Long, String> mapDatasetsKeys = new HashMap<Long, String>();
	
	private Map<Long, List<PendingRelation>> pendingRelationships = new HashMap<Long, List<PendingRelation>>();
	
	private long nodeFileCounter = 0;
	private long relationshipFileCounter;
	
	private List<DoubleLabel> institutionSources;
	private List<DoubleLabel> researcherSources;
	private List<DoubleLabel> grantSources;
	private List<DoubleLabel> datasetSources;
	private List<DoubleLabel> publicationSources;
	
	private static final String PART_NAME_SEPARATOR = " - ";
	private static final String[] INVALID_NAMES = { "Research Profiles", 
		"UWA Staff Profile", 
		"People: Applied Signal Processing Academic and Adjunct staff", 
		"School of Politics &amp; International Relations",
		"School of History", 
		"China in the World", "Our People", 
		"Climate Change Institute",
		"Examination of doctoral dissertations",
		"People",
		"Graduate students"
		};
		
	
	/**
	 * Class constructor 
	 * 
	 * @param nodeSource
	 * @param nodeType
	 * @param propertyName
	 * @param dbFolder
	 * @param outputFolder
	 */
	public Exporter(final String dbFolder, final String outputFolder) {
		System.out.println("Source Neo4j folder: " + dbFolder);
		System.out.println("Target folder: " + outputFolder);
			
		//graphDb = new GraphDatabaseFactory().newEmbeddedDatabase(dbPath);
		graphDb = Neo4jUtils.getReadOnlyGraphDb(dbFolder);
	//	global = Neo4jUtils.getGlobalOperations(graphDb);
		engine = Neo4jUtils.getExecutionEngine(graphDb);
		
		// Set output folder
		File folder = new File(outputFolder);

		schemaFolder = GraphUtils.getSchemaFolder(folder);
		nodesFolder = GraphUtils.getNodeFolder(folder);
		relationshipsFolder = GraphUtils.getRelationshipFolder(folder);
		
		schemaFolder.mkdirs();
		nodesFolder.mkdirs();
		relationshipsFolder.mkdirs();
				
		institutionSources = new ArrayList<DoubleLabel>();
		institutionSources.add(new DoubleLabel(AggrigationUtils.Labels.RDA, AggrigationUtils.Labels.Institution));
		institutionSources.add(new DoubleLabel(AggrigationUtils.Labels.Web, AggrigationUtils.Labels.Institution));
		institutionSources.add(new DoubleLabel(AggrigationUtils.Labels.ARC, AggrigationUtils.Labels.Institution));
		institutionSources.add(new DoubleLabel(AggrigationUtils.Labels.NHMRC, AggrigationUtils.Labels.Institution));
		
		researcherSources = new ArrayList<DoubleLabel>();
		researcherSources.add(new DoubleLabel(Labels.Orcid, AggrigationUtils.Labels.Researcher));
		researcherSources.add(new DoubleLabel(AggrigationUtils.Labels.Web, AggrigationUtils.Labels.Researcher));
		researcherSources.add(new DoubleLabel(AggrigationUtils.Labels.RDA, AggrigationUtils.Labels.Researcher));
		researcherSources.add(new DoubleLabel(Labels.FigShare, AggrigationUtils.Labels.Researcher));
		researcherSources.add(new DoubleLabel(AggrigationUtils.Labels.CrossRef, AggrigationUtils.Labels.Researcher));
		
		grantSources = new ArrayList<DoubleLabel>();
		grantSources.add(new DoubleLabel(AggrigationUtils.Labels.RDA, AggrigationUtils.Labels.Grant));
		grantSources.add(new DoubleLabel(AggrigationUtils.Labels.ARC, AggrigationUtils.Labels.Grant));
		grantSources.add(new DoubleLabel(AggrigationUtils.Labels.NHMRC, AggrigationUtils.Labels.Grant));
		
		datasetSources = new ArrayList<DoubleLabel>();
		datasetSources.add(new DoubleLabel(AggrigationUtils.Labels.RDA, AggrigationUtils.Labels.Dataset));
		datasetSources.add(new DoubleLabel(AggrigationUtils.Labels.Dryad, AggrigationUtils.Labels.Dataset));
		datasetSources.add(new DoubleLabel(AggrigationUtils.Labels.Dryad, AggrigationUtils.Labels.Publication));

		publicationSources = new ArrayList<DoubleLabel>();
		publicationSources.add(new DoubleLabel(AggrigationUtils.Labels.CrossRef, AggrigationUtils.Labels.Publication));
		publicationSources.add(new DoubleLabel(Labels.FigShare, AggrigationUtils.Labels.Publication));
		publicationSources.add(new DoubleLabel(Labels.Orcid, Labels.Work));
	}
	
	/**
	 * Function to perform a export
	 * @throws JsonGenerationException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	public void process() throws JsonGenerationException, JsonMappingException, IOException {
		exportIndex();
		
		exportInstitutions();
		exportResearchers();
		exportGrants();
		exportDatasets();
		exportPublications();
		
		if (null != graphNodes)
			saveNodes();
		if (null != graphRelationships)
			saveRelationships();
		
		System.out.println("Done! Exported "
				+ mapInstitutionsKeys.size() + " institutions, "
				+ mapResearchersKeys.size() + " researchers, " 
				+ mapGrantsKeys.size() + " grants, "
				+ mapPublicationsKeys.size() + " publications and "
				+ mapDatasetsKeys.size() + " datasets");
	}
	
	
	public void exportIndex() throws JsonGenerationException, JsonMappingException, IOException {
		System.out.println("Create indexes");
		
		graphSchema = new ArrayList<GraphSchema>();
		
		// init constraints
		graphSchema.add(new GraphSchema(AggrigationUtils.LABEL_INSTITUTION, AggrigationUtils.PROPERTY_KEY, true));
		graphSchema.add(new GraphSchema(AggrigationUtils.LABEL_INSTITUTION, AggrigationUtils.PROPERTY_TITLE, false));
		graphSchema.add(new GraphSchema(AggrigationUtils.LABEL_INSTITUTION, AggrigationUtils.PROPERTY_NODE_TYPE, false));
		graphSchema.add(new GraphSchema(AggrigationUtils.LABEL_INSTITUTION, AggrigationUtils.PROPERTY_NODE_SOURCE, false));
		graphSchema.add(new GraphSchema(AggrigationUtils.LABEL_INSTITUTION, AggrigationUtils.PROPERTY_DATE, false));
		graphSchema.add(new GraphSchema(AggrigationUtils.LABEL_INSTITUTION, AggrigationUtils.PROPERTY_NLA, false));
		graphSchema.add(new GraphSchema(AggrigationUtils.LABEL_INSTITUTION, AggrigationUtils.PROPERTY_RDA_URL, false));
		graphSchema.add(new GraphSchema(AggrigationUtils.LABEL_INSTITUTION, AggrigationUtils.PROPERTY_URL, false));
		
		graphSchema.add(new GraphSchema(AggrigationUtils.LABEL_RESEARCHER, AggrigationUtils.PROPERTY_KEY, true));
		graphSchema.add(new GraphSchema(AggrigationUtils.LABEL_RESEARCHER, AggrigationUtils.PROPERTY_INITIALS, false));
		graphSchema.add(new GraphSchema(AggrigationUtils.LABEL_RESEARCHER, AggrigationUtils.PROPERTY_FIRST_NAME, false));
		graphSchema.add(new GraphSchema(AggrigationUtils.LABEL_RESEARCHER, AggrigationUtils.PROPERTY_LAST_NAME, false));
		graphSchema.add(new GraphSchema(AggrigationUtils.LABEL_RESEARCHER, AggrigationUtils.PROPERTY_FULL_NAME, false));
		graphSchema.add(new GraphSchema(AggrigationUtils.LABEL_RESEARCHER, AggrigationUtils.PROPERTY_NODE_TYPE, false));
		graphSchema.add(new GraphSchema(AggrigationUtils.LABEL_RESEARCHER, AggrigationUtils.PROPERTY_NODE_SOURCE, false));
		graphSchema.add(new GraphSchema(AggrigationUtils.LABEL_RESEARCHER, AggrigationUtils.PROPERTY_NLA, false));
		graphSchema.add(new GraphSchema(AggrigationUtils.LABEL_RESEARCHER, AggrigationUtils.PROPERTY_ORCID, false));
		graphSchema.add(new GraphSchema(AggrigationUtils.LABEL_RESEARCHER, AggrigationUtils.PROPERTY_RDA_URL, false));
		graphSchema.add(new GraphSchema(AggrigationUtils.LABEL_RESEARCHER, AggrigationUtils.PROPERTY_URL, false));
		graphSchema.add(new GraphSchema(AggrigationUtils.LABEL_RESEARCHER, AggrigationUtils.PROPERTY_ORCID_URL, false));
		graphSchema.add(new GraphSchema(AggrigationUtils.LABEL_RESEARCHER, AggrigationUtils.PROPERTY_FIGSHARE_URL, false));
		
		graphSchema.add(new GraphSchema(AggrigationUtils.LABEL_GRANT, AggrigationUtils.PROPERTY_KEY, true));
		graphSchema.add(new GraphSchema(AggrigationUtils.LABEL_GRANT, AggrigationUtils.PROPERTY_TITLE, false));
		graphSchema.add(new GraphSchema(AggrigationUtils.LABEL_GRANT, AggrigationUtils.PROPERTY_NODE_TYPE, false));
		graphSchema.add(new GraphSchema(AggrigationUtils.LABEL_GRANT, AggrigationUtils.PROPERTY_NODE_SOURCE, false));
		graphSchema.add(new GraphSchema(AggrigationUtils.LABEL_GRANT, AggrigationUtils.PROPERTY_DATE, false));
		graphSchema.add(new GraphSchema(AggrigationUtils.LABEL_GRANT, AggrigationUtils.PROPERTY_PURL, false));
		graphSchema.add(new GraphSchema(AggrigationUtils.LABEL_GRANT, AggrigationUtils.PROPERTY_GRANT_NUMBER, false));
		graphSchema.add(new GraphSchema(AggrigationUtils.LABEL_GRANT, AggrigationUtils.PROPERTY_RDA_URL, false));
		
		graphSchema.add(new GraphSchema(AggrigationUtils.LABEL_DATASET, AggrigationUtils.PROPERTY_KEY, true));
		graphSchema.add(new GraphSchema(AggrigationUtils.LABEL_DATASET, AggrigationUtils.PROPERTY_TITLE, false));
		graphSchema.add(new GraphSchema(AggrigationUtils.LABEL_DATASET, AggrigationUtils.PROPERTY_NODE_TYPE, false));
		graphSchema.add(new GraphSchema(AggrigationUtils.LABEL_DATASET, AggrigationUtils.PROPERTY_NODE_SOURCE, false));
		graphSchema.add(new GraphSchema(AggrigationUtils.LABEL_DATASET, AggrigationUtils.PROPERTY_DATE, false));
		graphSchema.add(new GraphSchema(AggrigationUtils.LABEL_DATASET, AggrigationUtils.PROPERTY_DOI, false));
		graphSchema.add(new GraphSchema(AggrigationUtils.LABEL_DATASET, AggrigationUtils.PROPERTY_RDA_URL, false));
		graphSchema.add(new GraphSchema(AggrigationUtils.LABEL_DATASET, AggrigationUtils.PROPERTY_DRYAD_URL, false));
		
		graphSchema.add(new GraphSchema(AggrigationUtils.LABEL_PUBLICATION, AggrigationUtils.PROPERTY_KEY, true));
		graphSchema.add(new GraphSchema(AggrigationUtils.LABEL_PUBLICATION, AggrigationUtils.PROPERTY_TITLE, false));
		graphSchema.add(new GraphSchema(AggrigationUtils.LABEL_PUBLICATION, AggrigationUtils.PROPERTY_NODE_TYPE, false));
		graphSchema.add(new GraphSchema(AggrigationUtils.LABEL_PUBLICATION, AggrigationUtils.PROPERTY_NODE_SOURCE, false));
		graphSchema.add(new GraphSchema(AggrigationUtils.LABEL_PUBLICATION, AggrigationUtils.PROPERTY_DATE, false));
		graphSchema.add(new GraphSchema(AggrigationUtils.LABEL_PUBLICATION, AggrigationUtils.PROPERTY_DOI, false));
		graphSchema.add(new GraphSchema(AggrigationUtils.LABEL_PUBLICATION, AggrigationUtils.PROPERTY_ISBN, false));
		graphSchema.add(new GraphSchema(AggrigationUtils.LABEL_PUBLICATION, AggrigationUtils.PROPERTY_FIGSHARE_URL, false));

		graphSchema.add(new GraphSchema(AggrigationUtils.LABEL_FIGSHARE, AggrigationUtils.PROPERTY_FIGSHARE_URL, false));
		graphSchema.add(new GraphSchema(AggrigationUtils.LABEL_ORCID, AggrigationUtils.PROPERTY_ORCID_URL, false));
		graphSchema.add(new GraphSchema(AggrigationUtils.LABEL_RDA, AggrigationUtils.PROPERTY_RDA_URL, false));
		
		saveSchema();
	}
	
	/**
	 * Institution properties:
	 * key
	 * title
	 * nla
	 * isni
	 * country
	 * 
	 * @throws IOException 
	 * @throws JsonMappingException 
	 * @throws JsonGenerationException 
	 */
	
	private void exportInstitutions() throws JsonGenerationException, JsonMappingException, IOException {
		System.out.println("Export institutions");
		
		for (DoubleLabel label : institutionSources) 
			try ( Transaction tx = graphDb.beginTx() ) {
				ExecutionResult result = getAllNodesWithDoubleLabel(label);
				ResourceIterator<Node> institutions = result.columnAs("n");
				while (institutions.hasNext()) {
					Node institution = institutions.next();
					if (!mapInstitutionsKeys.containsKey(institution.getId())) {
						
						Map<String, Object> properties = new HashMap<String, Object>();
						properties.put(AggrigationUtils.PROPERTY_NODE_TYPE, AggrigationUtils.LABEL_INSTITUTION.toLowerCase());
						
						Set<String> sources = new HashSet<String>();
						Set<String> labels = new HashSet<String>();
						labels.add(AggrigationUtils.LABEL_INSTITUTION);
						
						Map<Long, Node> nodes = getKnownAs(institution, null, institutionSources);
						for (Node node : nodes.values())  
							if (node.hasLabel(AggrigationUtils.Labels.RDA)) {
								exportRdaInstitution(node, properties);
								sources.add(AggrigationUtils.LABEL_RDA.toLowerCase());
								labels.add(AggrigationUtils.LABEL_RDA);
							}
						for (Node node : nodes.values()) 
							if (node.hasLabel(AggrigationUtils.Labels.Web)) {
								exportWebInstitution(node, properties);
								sources.add(AggrigationUtils.LABEL_WEB.toLowerCase());
								labels.add(AggrigationUtils.LABEL_WEB);
							}
						for (Node node : nodes.values()) {
							if (node.hasLabel(AggrigationUtils.Labels.ARC)) {
								exportArcOrNhmrcInstitution(node, properties);
								sources.add(AggrigationUtils.LABEL_ARC.toLowerCase());
								labels.add(AggrigationUtils.LABEL_ARC);
							}
							if (node.hasLabel(AggrigationUtils.Labels.NHMRC))  {
								exportArcOrNhmrcInstitution(node, properties);
								sources.add(AggrigationUtils.LABEL_NHMRC.toLowerCase());
								labels.add(AggrigationUtils.LABEL_NHMRC);
							}						
						}
						
						if (sources.size() == 1)
							properties.put(AggrigationUtils.PROPERTY_NODE_SOURCE, sources.iterator().next());
						else if (sources.size() > 1)
							properties.put(AggrigationUtils.PROPERTY_NODE_SOURCE, sources.toArray(new String[sources.size()]));
	
						String key = (String) properties.get(AggrigationUtils.PROPERTY_KEY);
						for (Long nodeId : nodes.keySet())
							mapInstitutionsKeys.put(nodeId, key);
						
						if (null != key) {
							//System.out.println(AggrigationUtils.LABEL_INSTITUTION + ": " + key);
							
							GraphIndex index = new GraphIndex(AggrigationUtils.LABEL_INSTITUTION, AggrigationUtils.PROPERTY_KEY, key);
							exportGraphNode(new GraphNode(labels, properties, index));
							
							for (Node node : nodes.values()) 
								exportRelationships(node, AggrigationUtils.LABEL_INSTITUTION, key);
						}
					}
				}
			}
	}
	
	
	
	/**
	 * Researcher properties:
	 * key
	 * node_type
	 * initials
	 * first_name
	 * last_name
	 * full_name
	 * nla
	 * orcid
	 * isni
	 * country
	 * for_codes
	 * 
	 * @throws JsonGenerationException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	public void exportResearchers() throws JsonGenerationException, JsonMappingException, IOException {
		System.out.println("Export researchers");
		
		for (DoubleLabel label : researcherSources) 
			try ( Transaction tx = graphDb.beginTx() ) {
				ExecutionResult result = getAllNodesWithDoubleLabel(label);
				ResourceIterator<Node> researchers = result.columnAs("n");
				while (researchers.hasNext()) {
					Node researcher = researchers.next();
					if (!mapResearchersKeys.containsKey(researcher.getId())) {
						
						Map<String, Object> properties = new HashMap<String, Object>();
						properties.put(AggrigationUtils.PROPERTY_NODE_TYPE, AggrigationUtils.LABEL_RESEARCHER_LOWERCASE);
						
						Set<String> sources = new HashSet<String>();
						Set<String> labels = new HashSet<String>();
						labels.add(AggrigationUtils.LABEL_RESEARCHER);					
	
						Map<Long, Node> nodes = getKnownAs(researcher, null, researcherSources);
						for (Node node : nodes.values()) 
							if (node.hasLabel(Labels.Orcid)) {
								exportOrcidResearcher(node, properties);
								sources.add(AggrigationUtils.LABEL_ORCID_LOWERCASE);
								labels.add(AggrigationUtils.LABEL_ORCID);
							}
						for (Node node : nodes.values()) 
							if (node.hasLabel(AggrigationUtils.Labels.Web)) {
								exportWebResearcher(node, properties);
								sources.add(AggrigationUtils.LABEL_WEB_LOWERCASE);
								labels.add(AggrigationUtils.LABEL_WEB);
							}
						for (Node node : nodes.values()) 
							if (node.hasLabel(AggrigationUtils.Labels.RDA)) {
								exportRdaResearcher(node, properties);
								sources.add(AggrigationUtils.LABEL_RDA_LOWERCASE);
								labels.add(AggrigationUtils.LABEL_RDA);
							}
						for (Node node : nodes.values()) 
							if (node.hasLabel(Labels.FigShare)) {
								exportFigShareResearcher(node, properties);
								sources.add(AggrigationUtils.LABEL_FIGSHARE_LOWERCASE);
								labels.add(AggrigationUtils.LABEL_FIGSHARE);
							}
						for (Node node : nodes.values()) 
							if (node.hasLabel(AggrigationUtils.Labels.CrossRef)) {
								exportCrossRefResearcher(node, properties);
								sources.add(AggrigationUtils.LABEL_CROSSREF_LOWERCASE);
								labels.add(AggrigationUtils.LABEL_CROSSREF);
							}
						for (Node node : nodes.values()) 
							if (node.hasLabel(AggrigationUtils.Labels.NHMRC)) {
								exportNHMRCRefResearcher(node, properties);
								sources.add(AggrigationUtils.LABEL_NHMRC_LOWERCASE);
								labels.add(AggrigationUtils.LABEL_NHMRC);
							}
						
						if (sources.size() == 1)
							properties.put(AggrigationUtils.PROPERTY_NODE_SOURCE, sources.iterator().next());
						else if (sources.size() > 1)
							properties.put(AggrigationUtils.PROPERTY_NODE_SOURCE, sources.toArray(new String[sources.size()]));
						
						String key = (String) properties.get(AggrigationUtils.PROPERTY_KEY);
						for (Long nodeId : nodes.keySet())
							mapResearchersKeys.put(nodeId, key);
	
						if (null != key) {
						//	System.out.println(AggrigationUtils.LABEL_RESEARCHER + ": " + key);
							
							GraphIndex index = new GraphIndex(AggrigationUtils.LABEL_RESEARCHER, AggrigationUtils.PROPERTY_KEY, key);
							exportGraphNode(new GraphNode(labels, properties, index));
							
							for (Node node : nodes.values()) 
								exportRelationships(node, AggrigationUtils.LABEL_RESEARCHER, key);
						}
					}
				}
			}
	}
	
	/**
	 * Grant properties:
	 * key
	 * node_type
	 * title
	 * authors
	 * date
	 * purl
	 * grant_number
	 * country
	 * for_codes
	 * 
	 * @throws JsonGenerationException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	
	private void exportGrants() throws JsonGenerationException, JsonMappingException, IOException {
		System.out.println("Export grants");
		
		for (DoubleLabel label : grantSources) 
			try ( Transaction tx = graphDb.beginTx() ) {
				ExecutionResult result = getAllNodesWithDoubleLabel(label);
				ResourceIterator<Node> grants = result.columnAs("n");
				while (grants.hasNext()) {
					Node grant = grants.next();
					if (!mapGrantsKeys.containsKey(grant.getId())) {
						
						Map<String, Object> properties = new HashMap<String, Object>();
						properties.put(AggrigationUtils.PROPERTY_NODE_TYPE, AggrigationUtils.LABEL_GRANT_LOWERCASE);
						
						Set<String> sources = new HashSet<String>();
						Set<String> labels = new HashSet<String>();
						labels.add(AggrigationUtils.LABEL_GRANT);
						
						Map<Long, Node> nodes = getKnownAs(grant, null, grantSources);
						for (Node node : nodes.values()) 
							if (node.hasLabel(AggrigationUtils.Labels.RDA)) {
								exportRDAGrant(node, properties);
								sources.add(AggrigationUtils.LABEL_RDA_LOWERCASE);
								labels.add(AggrigationUtils.LABEL_RDA);
							}
						for (Node node : nodes.values()) 
							if (node.hasLabel(AggrigationUtils.Labels.ARC)) {
								exportARCGrant(node, properties);
								sources.add(AggrigationUtils.LABEL_ARC_LOWERCASE);
								labels.add(AggrigationUtils.LABEL_ARC);
							}
						for (Node node : nodes.values()) 
							if (node.hasLabel(AggrigationUtils.Labels.NHMRC)) {
								exportNHMRCGrant(node, properties);
								sources.add(AggrigationUtils.LABEL_NHMRC_LOWERCASE);
								labels.add(AggrigationUtils.LABEL_NHMRC);
							}
						
						if (sources.size() == 1)
							properties.put(AggrigationUtils.PROPERTY_NODE_SOURCE, sources.iterator().next());
						else if (sources.size() > 1)
							properties.put(AggrigationUtils.PROPERTY_NODE_SOURCE, sources.toArray(new String[sources.size()]));
										
						String key = (String) properties.get(AggrigationUtils.PROPERTY_KEY);
						for (Long nodeId : nodes.keySet())
							mapGrantsKeys.put(nodeId, key);
						
						if (null != key) {
						//	System.out.println(AggrigationUtils.LABEL_GRANT + ": " + key);
							
							GraphIndex index = new GraphIndex(AggrigationUtils.LABEL_GRANT, AggrigationUtils.PROPERTY_KEY, key);
							exportGraphNode(new GraphNode(labels, properties, index));
							
							for (Node node : nodes.values()) 
								exportRelationships(node, AggrigationUtils.LABEL_GRANT, key);
						}
					}
				}
			}
	}
	
	/**
	 * Dataset properties:
	 * key
	 * node_type
	 * title
	 * authors
	 * date
	 * doi
	 * country
	 * for_codes
	 * 
	 * @throws JsonGenerationException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	
	private void exportDatasets() throws JsonGenerationException, JsonMappingException, IOException {
		System.out.println("Export datasets");
		
		for (DoubleLabel label : datasetSources)
			try ( Transaction tx = graphDb.beginTx() ) {
				ExecutionResult result = getAllNodesWithDoubleLabel(label);
				ResourceIterator<Node> datasets = result.columnAs("n");
				while (datasets.hasNext()) {
					Node dataset = datasets.next();
					if (!mapDatasetsKeys.containsKey(dataset.getId())) {
						
						Map<String, Object> properties = new HashMap<String, Object>();
						properties.put(AggrigationUtils.PROPERTY_NODE_TYPE, AggrigationUtils.LABEL_DATASET_LOWERCASE);
						
						Set<String> sources = new HashSet<String>();
						Set<String> labels = new HashSet<String>();
						labels.add(AggrigationUtils.LABEL_DATASET);
						
						Map<Long, Node> nodes = getKnownAs(dataset, null, datasetSources);
						for (Node node : nodes.values()) 
							if (node.hasLabel(AggrigationUtils.Labels.RDA)) {
								exportRDADataset(node, properties);
								sources.add(AggrigationUtils.LABEL_RDA_LOWERCASE);
								labels.add(AggrigationUtils.LABEL_RDA);
							}
						for (Node node : nodes.values()) 
							if (node.hasLabel(AggrigationUtils.Labels.Dryad)) {
								exportDryadDataset(node, properties);
								sources.add(AggrigationUtils.LABEL_DRYAD_LOWERCASE);
								labels.add(AggrigationUtils.LABEL_DRYAD);
							}
						
						if (sources.size() == 1)
							properties.put(AggrigationUtils.PROPERTY_NODE_SOURCE, sources.iterator().next());
						else if (sources.size() > 1)
							properties.put(AggrigationUtils.PROPERTY_NODE_SOURCE, sources.toArray(new String[sources.size()]));
									
						String key = (String) properties.get(AggrigationUtils.PROPERTY_KEY);
						for (Long nodeId : nodes.keySet())
							mapDatasetsKeys.put(nodeId, key);
						
						if (null != key) {
						//	System.out.println(AggrigationUtils.LABEL_DATASET + ": " + key);
							
							GraphIndex index = new GraphIndex(AggrigationUtils.LABEL_DATASET, AggrigationUtils.PROPERTY_KEY, key);
							exportGraphNode(new GraphNode(labels, properties, index));
							
							for (Node node : nodes.values()) 
								exportRelationships(node, AggrigationUtils.LABEL_DATASET, key);
						}
					}
				}
			}
	}
	
	/**
	 * Dataset properties:
	 * key
	 * node_type
	 * title
	 * authors
	 * publicaiton_type
	 * date
	 * doi
	 * isbn
	 * for_codes
	 * 
	 * @throws JsonGenerationException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	
	private void exportPublications() throws JsonGenerationException, JsonMappingException, IOException {
		System.out.println("Export publication");
			
		for (DoubleLabel label : publicationSources)
			try ( Transaction tx = graphDb.beginTx() ) {
				ExecutionResult result = getAllNodesWithDoubleLabel(label);
				ResourceIterator<Node> publications = result.columnAs("n");
				while (publications.hasNext()) {
					Node publication = publications.next();
					if (!mapPublicationsKeys.containsKey(publication.getId())) {
						
						Map<String, Object> properties = new HashMap<String, Object>();
						properties.put(AggrigationUtils.PROPERTY_NODE_TYPE, AggrigationUtils.LABEL_PUBLICATION_LOWERCASE);
						
						Set<String> sources = new HashSet<String>();
						Set<String> labels = new HashSet<String>();
						labels.add(AggrigationUtils.LABEL_PUBLICATION);
						
						Map<Long, Node> nodes = getKnownAs(publication, null, publicationSources);
						for (Node node : nodes.values()) 
							if (node.hasLabel(Labels.Orcid)) {
								exportOrcidPublication(node, properties);							
								sources.add(AggrigationUtils.LABEL_ORCID_LOWERCASE);
								labels.add(AggrigationUtils.LABEL_ORCID);
							}
						for (Node node : nodes.values()) 
							if (node.hasLabel(AggrigationUtils.Labels.CrossRef)) {
								exportCrossRefPublication(node, properties);
								sources.add(AggrigationUtils.LABEL_CROSSREF_LOWERCASE);
								labels.add(AggrigationUtils.LABEL_CROSSREF);
							}
						for (Node node : nodes.values()) 
							if (node.hasLabel(Labels.FigShare)) {
								exportFigSharePublication(node, properties);
								sources.add(AggrigationUtils.LABEL_FIGSHARE_LOWERCASE);
								labels.add(AggrigationUtils.LABEL_FIGSHARE);
							}
	/*					for (Node node : nodes.values()) 
							if (node.hasLabel(AggrigationUtils.Labels.Dryad)) {
								exportDryadPublication(node, properties);
								sources.add(AggrigationUtils.LABEL_DRYAD);
							}*/
						
						if (sources.size() == 1)
							properties.put(AggrigationUtils.PROPERTY_NODE_SOURCE, sources.iterator().next());
						else if (sources.size() > 1)
							properties.put(AggrigationUtils.PROPERTY_NODE_SOURCE, sources.toArray(new String[sources.size()]));
						
						String key = (String) properties.get(AggrigationUtils.PROPERTY_KEY);
						for (Long nodeId : nodes.keySet())
							mapPublicationsKeys.put(nodeId, key);
						
						if (null != key) {
						//	System.out.println(AggrigationUtils.LABEL_PUBLICATION + ": " + key);
							
							GraphIndex index = new GraphIndex(AggrigationUtils.LABEL_PUBLICATION, AggrigationUtils.PROPERTY_KEY, key);
							exportGraphNode(new GraphNode(labels, properties, index));
						
							for (Node node : nodes.values()) 
								exportRelationships(node, AggrigationUtils.LABEL_PUBLICATION, key);
						}
					}
				}		
			}
	}
	
	private ExecutionResult getAllNodesWithDoubleLabel(DoubleLabel source) {
		StringBuilder sb = new StringBuilder();
		sb.append("MATCH (n:");
		sb.append(source.getSource());
		sb.append(":");
		sb.append(source.getType());
		sb.append(") RETURN n");
		
		System.out.println(sb.toString());
		
		return engine.execute(sb.toString());
	}
	
	private Object getProperty(Node node, String key) {
		return node.getProperty(key, null);
	}
	
	private String getUri(Node node, String key) throws IOException {
		Object uri = node.getProperty(key, null);
		if (null != uri) {
			if (uri instanceof String)
				return AggrigationUtils.extractUri((String) uri);
			else if (uri instanceof String[] && ((String[]) uri).length > 0) 
				return AggrigationUtils.extractUri(((String[]) uri)[0]);
			else 
				throw new IOException("Invalid URI propertry class: " + uri.getClass());
		}
		
		return null;
	}

	private String getOrcid(Node node, String key) throws IOException {
		Object orcid = node.getProperty(key, null);
		if (null != orcid) {
			if (orcid instanceof String)
				return AggrigationUtils.extractOrcid((String) orcid);
			else if (orcid instanceof String[] && ((String[]) orcid).length > 0) 
				return AggrigationUtils.extractOrcid(((String[]) orcid)[0]);
			else 
				throw new IOException("Invalid URI propertry class: " + orcid.getClass());
		}
		
		return null;
		
		//return AggrigationUtils.extractOrcid((String) node.getProperty(key, null));
	}

	private String getDoi(Node node, String key) throws IOException {
		Object doi = node.getProperty(key, null);
		if (null != doi) {
			if (doi instanceof String)
				return AggrigationUtils.extractDoi((String) doi);
			else if (doi instanceof String[] && ((String[]) doi).length > 0) 
				return AggrigationUtils.extractDoi(((String[]) doi)[0]);
			else 
				throw new IOException("Invalid URI propertry class: " + doi.getClass());
		}
		
		return null;
		
		//return AggrigationUtils.extractDoi((String) node.getProperty(key, null));
	}

	private String getDoiUri(Node node, String key) throws IOException {
		Object doi = node.getProperty(key, null);
		if (null != doi) {
			if (doi instanceof String)
				return AggrigationUtils.generateDoiUri(AggrigationUtils.extractDoi((String) doi));
			else if (doi instanceof String[] && ((String[]) doi).length > 0) 
				return AggrigationUtils.generateDoiUri(AggrigationUtils.extractDoi(((String[]) doi)[0]));
			else 
				throw new IOException("Invalid URI propertry class: " + doi.getClass());
		}
		
		return null;
		
//		return AggrigationUtils.generateDoiUri(AggrigationUtils.extractDoi((String) node.getProperty(key, null)));
	}
	
	private String getResearcherTitle(Node node) {
		String title = (String) node.getProperty(PROPERTY_NAME, null);
		if (null != title) {
			String[] titles = title.split(PART_NAME_SEPARATOR);
			for (String part : titles) {
				String namePart = part.trim();
				if (!namePart.isEmpty() && !AggrigationUtils.isAllUpper(namePart)) {
					for (String invalidName : INVALID_NAMES) 
						if (namePart.equals(invalidName)) {
							namePart = null;
							break;
						}
			
					if (null != namePart)
						return namePart;
				}
			}
		}
		
		return null;
	}
	
	private void exportSingle(Map<String, Object> properties, String key, Object value) {
		if (null != value && !properties.containsKey(key))
			properties.put(key, value);
	}

	@SuppressWarnings("unchecked")
	private void exportSet(Map<String, Object> properties, String key, Object value) {
		if (null != value) 
			if (properties.containsKey(key)) {
				Object oldValue = properties.get(key);
				if (oldValue instanceof Set<?>) {
					((Set<Object>)oldValue).add(value);
				} else {
					Set<Object> set = new HashSet<Object>();
					set.add(oldValue);
					set.add(value);
					properties.put(key, set);
				}
			} else
				properties.put(key, value);
	}
	
		
	/*
	private void exportProperty(Node node, Map<String, Object> properties, String key1, String key2) {
		if (!properties.containsKey(key2) && node.hasProperty(key1)) 
			properties.put(key2, node.getProperty(key1));
	}
	
	@SuppressWarnings("unchecked")
	private void exportUriSet(Node node, Map<String, Object> properties, String key1, String key2) {
		if (node.hasProperty(key1)) {
			if (!properties.containsKey(key2))
				properties.put(key2, node.getProperty(key1));
			else {
				String uri = AggrigationUtils.extractUri((String) node.getProperty(key1));
				
				if (null != uri && !uri.isEmpty()) { // Only string values are supported for now
					Object value2 = properties.get(key2);
					if (value2 instanceof String) {
						if (!value2.equals(uri)) {
							Set<String> set = new HashSet<String>();
							set.add((String) value2);
							set.add(uri);
							
							properties.put(key2, set);
						}
					} else if (value2 instanceof Set<?>) 
						((Set<String>) value2).add(uri);
				}
				
			}
		}
	}
	
	private void exportUri(Node node, Map<String, Object> properties, String key1, String key2) {
		if (!properties.containsKey(key2) && node.hasProperty(key1)) {
			Object value = node.getProperty(key1);
			if (null != value && value instanceof String) {
				String uri = AggrigationUtils.extractUri((String) value);
				if (null != uri && !uri.isEmpty())
					properties.put(key2, uri);
			}	
		}
	}
	
	private void exportOrcid(Node node, Map<String, Object> properties, String key1, String key2) {
		if (!properties.containsKey(key2) && node.hasProperty(key1)) {
			Object value = node.getProperty(key1);
			if (null != value && value instanceof String) {
				String orcid = AggrigationUtils.extractOrcid((String) value);
				if (null != orcid && !orcid.isEmpty())
					properties.put(key2, orcid);
			}	
		}
	}

	private void exportDoi(Node node, Map<String, Object> properties, String key1, String key2) {
		if (!properties.containsKey(key2) && node.hasProperty(key1)) {
			Object value = node.getProperty(key1);
			if (null != value && value instanceof String) {
				String doi = AggrigationUtils.extractDoi((String) value);
				if (null != doi && !doi.isEmpty())
					properties.put(key2, doi);
			}	
		}
	}
	
	private void exportDoiUri(Node node, Map<String, Object> properties, String key1, String key2) {
		if (!properties.containsKey(key2) && node.hasProperty(key1)) {
			Object value = node.getProperty(key1);
			if (null != value && value instanceof String) {
				String doi = AggrigationUtils.generateDoiUri(AggrigationUtils.extractDoi((String) value));
				if (null != doi && !doi.isEmpty())
					properties.put(key2, doi);
			}	
		}
	}*/

	private void exportGraphNode(GraphNode grahpNode) throws JsonGenerationException, JsonMappingException, IOException {
		if (null == graphNodes)
			graphNodes = new ArrayList<GraphNode>();
		
		graphNodes.add(grahpNode);
		
		if (graphNodes.size() >= MAX_COMMANDS)
			saveNodes();
	}
	
	private void exportGraphRelationship(GraphRelationship graphRelationship) throws JsonGenerationException, JsonMappingException, IOException {
		if (null == graphRelationships)
			graphRelationships = new ArrayList<GraphRelationship>();
		
		graphRelationships.add(graphRelationship);
		
		if (graphRelationships.size() >= MAX_COMMANDS)
			saveRelationships();
	}
	
/*	private boolean isConnectionAvaliable(Map<String, Object> properties) {
		return properties.containsKey(AggrigationUtils.PROPERTY_KEY) 
				&& properties.containsKey(AggrigationUtils.PROPERTY_NODE_TYPE);
	}*/
	
	/*
	private void exportSourceConnection(Map<String, Object> properties, String nodeSource, String url) throws JsonGenerationException, JsonMappingException, IOException {
		if (properties.containsKey(AggrigationUtils.PROPERTY_KEY)) {
			String key = AggrigationUtils.extractUri(url);
			if (null != key) {
				Map<String, Object> prop = new HashMap<String, Object>();
				prop.put(AggrigationUtils.PROPERTY_KEY, key);
				prop.put(AggrigationUtils.PROPERTY_NODE_TYPE, nodeSource);
				
				GraphNode node = new GraphNode(prop);
				
				GraphConnection start = new GraphConnection(null, 
						(String) properties.get(AggrigationUtils.PROPERTY_NODE_TYPE), 
						(String) properties.get(AggrigationUtils.PROPERTY_KEY));
				GraphConnection end = new GraphConnection(null, nodeSource, key);
				GraphRelationship rel = new GraphRelationship(AggrigationUtils.REL_KNOWN_AS, null, start, end);
				
				exportGraphNode(node);
				exportGraphRelationship(rel);
			}
		}
	}*/
		
	private void exportRdaInstitution(Node node, Map<String, Object> properties) throws JsonGenerationException, JsonMappingException, IOException {
		exportSingle(properties, AggrigationUtils.PROPERTY_KEY, getUri(node, AggrigationUtils.PROPERTY_URL));
//		exportSingle(properties, AggrigationUtils.PROPERTY_KEY, getUri(node, PROPERTY_IDENTIFIER_URI));
		exportSingle(properties, AggrigationUtils.PROPERTY_NLA, getUri(node, PROPERTY_IDENTIFIER_AU_ANL_PEAU));
		exportSingle(properties, AggrigationUtils.PROPERTY_NLA, getUri(node, PROPERTY_IDENTIFIER_NLA_PARTY));
		exportSingle(properties, AggrigationUtils.PROPERTY_PURL, getUri(node, PROPERTY_IDENTIFIER_PULR));
		exportSingle(properties, AggrigationUtils.PROPERTY_TITLE, getProperty(node, PROPERTY_NAME_PRIMARY));
		exportSingle(properties, AggrigationUtils.PROPERTY_TITLE, getProperty(node, PROPERTY_NAME_FORMELY));
		exportSingle(properties, AggrigationUtils.PROPERTY_TITLE, getProperty(node, PROPERTY_NAME));
		exportSingle(properties, AggrigationUtils.PROPERTY_TITLE, getProperty(node, PROPERTY_NAME_ALTERNATIVE));
		exportSingle(properties, AggrigationUtils.PROPERTY_TITLE, getProperty(node, PROPERTY_NAME_ALTERNATIVE));
		exportSet(properties, AggrigationUtils.PROPERTY_RDA_URL, getUri(node, AggrigationUtils.PROPERTY_KEY));
		
//		exportSourceConnection(properties, AggrigationUtils.LABEL_RDA, (String) node.getProperty(AggrigationUtils.PROPERTY_KEY));
	}	
	
	private void exportWebInstitution(Node node, Map<String, Object> properties) throws IOException {
		exportSingle(properties, AggrigationUtils.PROPERTY_KEY, getUri(node, AggrigationUtils.PROPERTY_URL));
		exportSingle(properties, AggrigationUtils.PROPERTY_TITLE, getProperty(node, AggrigationUtils.PROPERTY_TITLE));
		exportSingle(properties, AggrigationUtils.PROPERTY_COUNTRY, getProperty(node, AggrigationUtils.PROPERTY_COUNTRY));	
		exportSet(properties, AggrigationUtils.PROPERTY_URL, getUri(node, AggrigationUtils.PROPERTY_URL));	
	}
	
	private void exportArcOrNhmrcInstitution(Node node, Map<String, Object> properties) {
		// Only export node, if it key is avaliable
		exportSingle(properties, AggrigationUtils.PROPERTY_TITLE, getProperty(node, PROPERTY_NAME));
	}
	
	private void exportOrcidResearcher(Node node, Map<String, Object> properties) throws JsonGenerationException, JsonMappingException, IOException {
		exportSingle(properties, AggrigationUtils.PROPERTY_KEY, getUri(node, AggrigationUtils.PROPERTY_KEY));
		exportSingle(properties, AggrigationUtils.PROPERTY_FIRST_NAME, getProperty(node, PROPERTY_GYVEN_NAMES));
		exportSingle(properties, AggrigationUtils.PROPERTY_LAST_NAME, getProperty(node, PROPERTY_FAMILY_NAME));
		exportSingle(properties, AggrigationUtils.PROPERTY_FULL_NAME, getProperty(node, AggrigationUtils.PROPERTY_FULL_NAME));
		exportSingle(properties, AggrigationUtils.PROPERTY_ORCID, getOrcid(node, PROPERTY_ORCID_ID));
		exportSet(properties, AggrigationUtils.PROPERTY_ORCID_URL, getUri(node, AggrigationUtils.PROPERTY_URL));	
		
//		exportSourceConnection(properties, AggrigationUtils.LABEL_ORCID, (String) properties.get(AggrigationUtils.PROPERTY_KEY));
	}
	
	private void exportWebResearcher(Node node, Map<String, Object> properties) throws IOException {
		exportSingle(properties, AggrigationUtils.PROPERTY_KEY, getUri(node, AggrigationUtils.PROPERTY_KEY));
		exportSingle(properties, AggrigationUtils.PROPERTY_TITLE, getResearcherTitle(node));
		exportSet(properties, AggrigationUtils.PROPERTY_URL, getUri(node, AggrigationUtils.PROPERTY_URL));	
	}
	
	private void exportRdaResearcher(Node node, Map<String, Object> properties) throws JsonGenerationException, JsonMappingException, IOException {
		exportSingle(properties, AggrigationUtils.PROPERTY_KEY, getUri(node, AggrigationUtils.PROPERTY_KEY));
		exportSingle(properties, AggrigationUtils.PROPERTY_FULL_NAME, getProperty(node, PROPERTY_NAME_PRIMARY));
		exportSingle(properties, AggrigationUtils.PROPERTY_FULL_NAME, getProperty(node, PROPERTY_NAME_ALTERNATIVE));
		exportSingle(properties, AggrigationUtils.PROPERTY_NLA, getUri(node, PROPERTY_IDENTIFIER_AU_ANL_PEAU));
		exportSingle(properties, AggrigationUtils.PROPERTY_NLA, getUri(node, PROPERTY_IDENTIFIER_NLA));
		exportSingle(properties, AggrigationUtils.PROPERTY_ORCID, getOrcid(node, PROPERTY_IDENTIFIER_ORCID));
		exportSet(properties, AggrigationUtils.PROPERTY_RDA_URL, getUri(node, AggrigationUtils.PROPERTY_KEY));	
		
		//exportSourceConnection(properties, AggrigationUtils.LABEL_RDA, (String) node.getProperty(AggrigationUtils.PROPERTY_KEY));
	}
	
	private void exportFigShareResearcher(Node node, Map<String, Object> properties) throws JsonGenerationException, JsonMappingException, IOException {
		exportSingle(properties, AggrigationUtils.PROPERTY_KEY, getUri(node, AggrigationUtils.PROPERTY_KEY));
		exportSingle(properties, AggrigationUtils.PROPERTY_FULL_NAME, getProperty(node, PROPERTY_NAME));
		exportSingle(properties, AggrigationUtils.PROPERTY_ORCID, getOrcid(node, AggrigationUtils.PROPERTY_ORCID));
		exportSet(properties, AggrigationUtils.PROPERTY_FIGSHARE_URL, getUri(node, AggrigationUtils.PROPERTY_KEY));	

		//exportSourceConnection(properties,, (String) node.getProperty(AggrigationUtils.PROPERTY_KEY));
	}
	
	private void exportCrossRefResearcher(Node node, Map<String, Object> properties) throws IOException {
		exportSingle(properties, AggrigationUtils.PROPERTY_FIRST_NAME, getProperty(node, PROPERTY_GIVEN_NAME));
		exportSingle(properties, AggrigationUtils.PROPERTY_LAST_NAME, getProperty(node, PROPERTY_FAMILY_NAME));
		exportSingle(properties, AggrigationUtils.PROPERTY_FULL_NAME, getProperty(node, AggrigationUtils.PROPERTY_FULL_NAME));
		exportSingle(properties, AggrigationUtils.PROPERTY_ORCID, getOrcid(node, AggrigationUtils.PROPERTY_ORCID));
	}
	
	private void exportNHMRCRefResearcher(Node node, Map<String, Object> properties) {
		exportSingle(properties, AggrigationUtils.PROPERTY_FIRST_NAME, getProperty(node, AggrigationUtils.PROPERTY_FIRST_NAME));
		exportSingle(properties, AggrigationUtils.PROPERTY_LAST_NAME, getProperty(node, AggrigationUtils.PROPERTY_LAST_NAME));
		exportSingle(properties, AggrigationUtils.PROPERTY_FULL_NAME, getProperty(node, AggrigationUtils.PROPERTY_FULL_NAME));
	}
	
	private void exportRDAGrant(Node node, Map<String, Object> properties) throws JsonGenerationException, JsonMappingException, IOException {
		exportSingle(properties, AggrigationUtils.PROPERTY_KEY, getUri(node, PROPERTY_IDENTIFIER_PULR));
		exportSingle(properties, AggrigationUtils.PROPERTY_KEY, getUri(node, AggrigationUtils.PROPERTY_KEY));
		exportSingle(properties, AggrigationUtils.PROPERTY_TITLE, getProperty(node, PROPERTY_NAME_PRIMARY));
		exportSingle(properties, AggrigationUtils.PROPERTY_TITLE, getProperty(node, PROPERTY_NAME_ALTERNATIVE));
		exportSingle(properties, AggrigationUtils.PROPERTY_TITLE, getProperty(node, PROPERTY_NAME));
		exportSingle(properties, AggrigationUtils.PROPERTY_PURL, getUri(node, PROPERTY_IDENTIFIER_PULR));
		exportSingle(properties, AggrigationUtils.PROPERTY_GRANT_NUMBER, getProperty(node, PROPERTY_IDENTIFIER_ARC));
		exportSingle(properties, AggrigationUtils.PROPERTY_GRANT_NUMBER, getProperty(node, PROPERTY_IDENTIFIER_NHMRC));
		exportSet(properties, AggrigationUtils.PROPERTY_RDA_URL, getUri(node, AggrigationUtils.PROPERTY_KEY));	

//		exportSourceConnection(properties, AggrigationUtils.LABEL_RDA, (String) node.getProperty(AggrigationUtils.PROPERTY_KEY));
	}
	
	private void exportARCGrant(Node node, Map<String, Object> properties) throws IOException {
		exportSingle(properties, AggrigationUtils.PROPERTY_KEY, getUri(node, AggrigationUtils.PROPERTY_KEY));
		exportSingle(properties, AggrigationUtils.PROPERTY_TITLE, getProperty(node, PROPERTY_SCIENTIFIC_TITLE));
		exportSingle(properties, AggrigationUtils.PROPERTY_PURL, getUri(node, AggrigationUtils.PROPERTY_PURL));
		exportSingle(properties, AggrigationUtils.PROPERTY_AUTHORS, getProperty(node, PROPERTY_RESEARCHERS));
		exportSingle(properties, AggrigationUtils.PROPERTY_GRANT_NUMBER, getProperty(node, PROPERTY_IDENTIFIER_ARC));
		exportSingle(properties, AggrigationUtils.PROPERTY_DATE, getProperty(node, PROPERTY_START_YEAR));
		exportSingle(properties, AggrigationUtils.PROPERTY_DATE, getProperty(node, PROPERTY_APPLICATION_YEAR));
	}
	
	private void exportNHMRCGrant(Node node, Map<String, Object> properties) throws IOException {
		exportSingle(properties, AggrigationUtils.PROPERTY_KEY, getUri(node, AggrigationUtils.PROPERTY_KEY));
		exportSingle(properties, AggrigationUtils.PROPERTY_TITLE, getProperty(node, PROPERTY_SCIENTIFIC_TITLE));
		exportSingle(properties, AggrigationUtils.PROPERTY_TITLE, getProperty(node, PROPERTY_SIMPLIFIED_TITLE));
		exportSingle(properties, AggrigationUtils.PROPERTY_PURL, getUri(node, AggrigationUtils.PROPERTY_PURL));
		exportSingle(properties, AggrigationUtils.PROPERTY_DATE, getProperty(node, PROPERTY_START_YEAR));
		exportSingle(properties, AggrigationUtils.PROPERTY_DATE, getProperty(node, PROPERTY_APPLICATION_YEAR));
	}
	
	private void exportRDADataset(Node node, Map<String, Object> properties) throws JsonGenerationException, JsonMappingException, IOException {
		exportSingle(properties, AggrigationUtils.PROPERTY_KEY, getDoiUri(node, PROPERTY_IDENTIFIER_DOI));
		exportSingle(properties, AggrigationUtils.PROPERTY_KEY, getUri(node, AggrigationUtils.PROPERTY_KEY));
		exportSingle(properties, AggrigationUtils.PROPERTY_DOI, getDoi(node, PROPERTY_IDENTIFIER_DOI));
		exportSingle(properties, AggrigationUtils.PROPERTY_TITLE, getProperty(node, PROPERTY_NAME_PRIMARY));
		exportSingle(properties, AggrigationUtils.PROPERTY_TITLE, getProperty(node, PROPERTY_NAME_ALTERNATIVE));
		exportSingle(properties, AggrigationUtils.PROPERTY_TITLE, getProperty(node, PROPERTY_NAME));
		exportSingle(properties, AggrigationUtils.PROPERTY_TITLE, getProperty(node, PROPERTY_NAME_FULL));
		exportSingle(properties, AggrigationUtils.PROPERTY_TITLE, getProperty(node, PROPERTY_NAME_TEXT));
		exportSet(properties, AggrigationUtils.PROPERTY_RDA_URL, getUri(node, AggrigationUtils.PROPERTY_KEY));	
	}
	
	private void exportDryadDataset(Node node, Map<String, Object> properties) throws JsonGenerationException, JsonMappingException, IOException {
		exportSingle(properties, AggrigationUtils.PROPERTY_KEY, getDoiUri(node, AggrigationUtils.PROPERTY_DOI));
		exportSingle(properties, AggrigationUtils.PROPERTY_KEY, getUri(node, AggrigationUtils.PROPERTY_URL));
		exportSingle(properties, AggrigationUtils.PROPERTY_DOI, getDoi(node, AggrigationUtils.PROPERTY_DOI));
		exportSingle(properties, AggrigationUtils.PROPERTY_TITLE, getProperty(node, AggrigationUtils.PROPERTY_TITLE));
		exportSingle(properties, AggrigationUtils.PROPERTY_AUTHORS, getProperty(node, PROPERTY_AUTHOR));
		exportSet(properties, AggrigationUtils.PROPERTY_DRYAD_URL, getUri(node, AggrigationUtils.PROPERTY_KEY));	
	}
	
	private void exportOrcidPublication(Node node, Map<String, Object> properties) throws IOException {
		exportSingle(properties, AggrigationUtils.PROPERTY_KEY, getDoiUri(node, PROPERTY_IDENTIFIER_DOI2));
		exportSingle(properties, AggrigationUtils.PROPERTY_DOI, getDoi(node, PROPERTY_IDENTIFIER_DOI2));
		exportSingle(properties, AggrigationUtils.PROPERTY_TITLE, getProperty(node, AggrigationUtils.PROPERTY_TITLE));
		exportSingle(properties, AggrigationUtils.PROPERTY_ISBN, getProperty(node, PROPERTY_IDENTIFIER_ISBN));
		exportSingle(properties, AggrigationUtils.PROPERTY_AUTHORS, getProperty(node, PROPERTY_CONTRIBUTORS));
		exportSingle(properties, AggrigationUtils.PROPERTY_DATE, getProperty(node, PROPERTY_PUBLICATION_DATE));
	}
	
	private void exportCrossRefPublication(Node node, Map<String, Object> properties) throws IOException {
		exportSingle(properties, AggrigationUtils.PROPERTY_KEY, getDoiUri(node, AggrigationUtils.PROPERTY_KEY));
		exportSingle(properties, AggrigationUtils.PROPERTY_DOI, getDoi(node, AggrigationUtils.PROPERTY_KEY));
		exportSingle(properties, AggrigationUtils.PROPERTY_TITLE, getProperty(node, AggrigationUtils.PROPERTY_TITLE));
		exportSingle(properties, AggrigationUtils.PROPERTY_AUTHORS, getProperty(node, PROPERTY_AUTHOR));
	}
	
	private void exportFigSharePublication(Node node, Map<String, Object> properties) throws JsonGenerationException, JsonMappingException, IOException {
		exportSingle(properties, AggrigationUtils.PROPERTY_KEY, getDoiUri(node, AggrigationUtils.PROPERTY_KEY));
		exportSingle(properties, AggrigationUtils.PROPERTY_DOI, getDoi(node, AggrigationUtils.PROPERTY_KEY));
		exportSingle(properties, AggrigationUtils.PROPERTY_TITLE, getProperty(node, AggrigationUtils.PROPERTY_TITLE));
		exportSingle(properties, AggrigationUtils.PROPERTY_AUTHORS, getProperty(node, AggrigationUtils.PROPERTY_AUTHORS));
		exportSet(properties, AggrigationUtils.PROPERTY_FIGSHARE_URL, getUri(node, AggrigationUtils.PROPERTY_URL));	
	}
	
	/*
	private void exportDryadPublication(Node node, Map<String, Object> properties) throws JsonGenerationException, JsonMappingException, IOException {
		exportDoiUri(node, properties, AggrigationUtils.PROPERTY_DOI, AggrigationUtils.PROPERTY_KEY);
		exportDoi(node, properties, AggrigationUtils.PROPERTY_DOI, AggrigationUtils.PROPERTY_DOI);
		exportProperty(node, properties, AggrigationUtils.PROPERTY_TITLE, AggrigationUtils.PROPERTY_TITLE);
		exportProperty(node, properties, AggrigationUtils.PROPERTY_AUTHORS, AggrigationUtils.PROPERTY_AUTHORS);
	
		exportSourceConnection(properties, AggrigationUtils.LABEL_DRYAD, (String) properties.get(AggrigationUtils.PROPERTY_KEY));
	}
	*/
	private Map<Long, Node> getKnownAs(Node node, Map<Long, Node> map, List<DoubleLabel> sources) {
		if (null == map)
			map = new HashMap<Long, Node>();
		map.put(node.getId(), node);
		
		Iterable<Relationship> rels = node.getRelationships(AggrigationUtils.RelTypes.knownAs);
		for (Relationship rel : rels) {
			Node other = rel.getOtherNode(node);
			if (!map.containsKey(other.getId())) 
				for (DoubleLabel source : sources) 
					if (other.hasLabel(source.getSource()) && other.hasLabel(source.getType())) {
						getKnownAs(other, map, sources);
						break;
					}
		}
		
		return map;
	}
	
	public void exportRelationships(Node start, String startType, String startKey) throws JsonGenerationException, JsonMappingException, IOException {
		long startId = start.getId();
		if (pendingRelationships.containsKey(startId)) {
			for (PendingRelation rel : pendingRelationships.get(startId)) {
				
				long endId = rel.getNodeId();
				
				String endType = null;
				String endKey = null;
				
				if (mapInstitutionsKeys.containsKey(endId)) {
					endType = AggrigationUtils.LABEL_INSTITUTION;
					endKey = mapInstitutionsKeys.get(endId);
				} else if (mapResearchersKeys.containsKey(endId)) {
					endType = AggrigationUtils.LABEL_RESEARCHER;
					endKey = mapResearchersKeys.get(endId);
				} else if (mapGrantsKeys.containsKey(endId)) {
					endType = AggrigationUtils.LABEL_GRANT;
					endKey = mapGrantsKeys.get(endId);
				} else if (mapDatasetsKeys.containsKey(endId)) {
					endType = AggrigationUtils.LABEL_DATASET;
					endKey = mapDatasetsKeys.get(endId);
				} else if (mapPublicationsKeys.containsKey(endId)) {
					endType = AggrigationUtils.LABEL_PUBLICATION;
					endKey = mapPublicationsKeys.get(endId);
				} 				
				
				// checl that connection is possible and tha we do not connect node to is self
				if (null != endType && null != endKey && (!startType.equals(endType) || !startKey.equals(endKey))) {
					// We need to inverse relationship, because pendingRelationships contains missing id for start node 
					GraphIndex graphStart = new GraphIndex(endType, AggrigationUtils.PROPERTY_KEY, endKey);
					GraphIndex graphEnd = new GraphIndex(startType, AggrigationUtils.PROPERTY_KEY, startKey);
					GraphRelationship graphRel = new GraphRelationship(rel.getType().name(), null, graphStart, graphEnd);
				
					exportGraphRelationship(graphRel);
				}
			}
			
			pendingRelationships.remove(startId);
		}
		
		Iterable<Relationship> rels = start.getRelationships(Direction.OUTGOING);
		for (Relationship rel : rels) {
			Node end = rel.getEndNode();
			long endId = end.getId();
			
			String endType = null;
			String endKey = null;
			
			if (mapInstitutionsKeys.containsKey(endId)) {
				endType = AggrigationUtils.LABEL_INSTITUTION;
				endKey = mapInstitutionsKeys.get(endId);
			} else if (mapResearchersKeys.containsKey(endId)) {
				endType = AggrigationUtils.LABEL_RESEARCHER;
				endKey = mapResearchersKeys.get(endId);
			} else if (mapGrantsKeys.containsKey(endId)) {
				endType = AggrigationUtils.LABEL_GRANT;
				endKey = mapGrantsKeys.get(endId);
			} else if (mapDatasetsKeys.containsKey(endId)) {
				endType = AggrigationUtils.LABEL_DATASET;
				endKey = mapDatasetsKeys.get(endId);
			} else if (mapPublicationsKeys.containsKey(endId)) {
				endType = AggrigationUtils.LABEL_PUBLICATION;
				endKey = mapPublicationsKeys.get(endId);
			} 

			// check that we have found a type
			if (null != endType) {
				// check that connection is possible and we do not connect node to is self
				if (null != endKey && (!startType.equals(endType) || !startKey.equals(endKey))) {
					GraphIndex graphStart = new GraphIndex(startType, AggrigationUtils.PROPERTY_KEY, startKey);
					GraphIndex graphEnd = new GraphIndex(endType, AggrigationUtils.PROPERTY_KEY, endKey);
					GraphRelationship graphRel = new GraphRelationship(rel.getType().name(), null, graphStart, graphEnd);
					
					exportGraphRelationship(graphRel);
				}
			} else {
				PendingRelation pendingRelation = new PendingRelation(startId, rel.getType());
				
				if (pendingRelationships.containsKey(endId)) 
					pendingRelationships.get(endId).add(pendingRelation);
				else {
					List<PendingRelation> list = new ArrayList<PendingRelation>();
					list.add(pendingRelation);
					pendingRelationships.put(endId, list);
				}					
			}
		}
	}

	private void saveSchema() throws JsonGenerationException, JsonMappingException, IOException {
		mapper.writeValue(new File(schemaFolder, GraphUtils.GRAPH_SCHEMA), graphSchema);
		graphSchema = null;
	}
	
	private void saveNodes() throws JsonGenerationException, JsonMappingException, IOException {
		String fileName = Long.toString(nodeFileCounter) + GraphUtils.GRAPH_EXTENSION;
		mapper.writeValue(new File(nodesFolder, fileName), graphNodes);
		graphNodes = null;
		++nodeFileCounter;
	}
	
	private void saveRelationships() throws JsonGenerationException, JsonMappingException, IOException {
		String fileName = Long.toString(relationshipFileCounter) + GraphUtils.GRAPH_EXTENSION;
		mapper.writeValue(new File(relationshipsFolder, fileName), graphRelationships);
		graphRelationships = null;
		++relationshipFileCounter;
	}
}
