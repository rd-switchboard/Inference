package org.rdswitchboard.importers.orcid;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.parboiled.common.StringUtils;
import org.rdswitchboard.libraries.graph.Graph;
import org.rdswitchboard.libraries.graph.GraphNode;
import org.rdswitchboard.libraries.graph.GraphRelationship;
import org.rdswitchboard.libraries.graph.GraphSchema;
import org.rdswitchboard.libraries.graph.GraphUtils;
import org.rdswitchboard.libraries.neo4j.Neo4jDatabase;
import org.rdswitchboard.utils.orcid.Contributor;
import org.rdswitchboard.utils.orcid.ExternalIdentifier;
import org.rdswitchboard.utils.orcid.ExternalIdentifiers;
import org.rdswitchboard.utils.orcid.Orcid;
import org.rdswitchboard.utils.orcid.OrcidActivities;
import org.rdswitchboard.utils.orcid.OrcidBio;
import org.rdswitchboard.utils.orcid.OrcidIdentifier;
import org.rdswitchboard.utils.orcid.OrcidMessage;
import org.rdswitchboard.utils.orcid.OrcidProfile;
import org.rdswitchboard.utils.orcid.OrcidWork;
import org.rdswitchboard.utils.orcid.OrcidWorks;
import org.rdswitchboard.utils.orcid.PersonalDetails;
import org.rdswitchboard.utils.orcid.RequestType;
import org.rdswitchboard.utils.orcid.WorkContributors;
import org.rdswitchboard.utils.orcid.WorkIdentifier;
import org.rdswitchboard.utils.orcid.WorkIdentifiers;
import org.rdswitchboard.utils.orcid.WorkTitle;

/**
 * History
 * 1.1.0: Updated Contributors function. The Contributor node will be created only if there is extra data
 *        All contributors are available via `contributors` property on the Work node
 * 1.1.1: The contributors will be always a set even if contributor is only one
 * 1.1.2: The contrubutor must have valid orcid_id, contributor role will be sent back to relationship
 * 1.1.3: Remover contributor node, the relationship will go to Researcher (if any)
 * 1.2.0: Updated contributor relationship
 * @author dima
 *
 */
public class ImporterOrcid {
	/*private static final String LABEL_ORCID = "Orcid";
	private static final String LABEL_RESEARCHER = "Researcher";
	private static final String LABEL_IDENTIFICATOR = "Identificator";
	private static final String LABEL_PAGE = "Page";
	private static final String LABEL_WORK = "Work";
//	private static final String LABEL_CONTRIBUTOR = "Contributor";
	private static final String LABEL_ORCID_RESEARCHER = LABEL_ORCID + "_" + LABEL_RESEARCHER;
	private static final String LABEL_ORCID_IDENTIFICATOR = LABEL_ORCID + "_" + LABEL_IDENTIFICATOR;
	private static final String LABEL_ORCID_PAGE = LABEL_ORCID + "_" + LABEL_PAGE;
	private static final String LABEL_ORCID_WORK = LABEL_ORCID + "_" + LABEL_WORK;
//	private static final String LABEL_ORCID_CONTRIBUTOR = LABEL_ORCID + "_" + LABEL_CONTRIBUTOR;

	private static final String RELATIONSHIP_IDENTIFIED_BY = "identifiedBy";
	private static final String RELATIONSHIP_LINKED_TO = "linkedTo";
	private static final String RELATIONSHIP_AUTHOR = "author";
	private static final String RELATIONSHIP_CONTRIBUTOR = "contributor";
	
	private static final String PROPERTY_KEY = "key"; 
	private static final String PROPERTY_NODE_SOURCE = "node_source";
	private static final String PROPERTY_NODE_TYPE = "node_type";
	private static final String PROPERTY_ORCID_ID = "orcid_id"; 
	private static final String PROPERTY_ORCID_TYPE = "orcid_type"; 
	private static final String PROPERTY_ORCID_SOURCE = "orcid_source";
	private static final String PROPERTY_FAMILY_NAME = "family_name";
	private static final String PROPERTY_GYVEN_NAMES = "gyven_names";
	private static final String PROPERTY_FULL_NAME = "full_name";
	private static final String PROPERTY_CREADIT_NAME = "creadit_name";
	private static final String PROPERTY_OTHER_NAMES = "other_names";
	private static final String PROPERTY_BIOGRAPHY = "biography";
	private static final String PROPERTY_SCOPUS_ID = "scopus_id";
	private static final String PROPERTY_URL = "url";
	private static final String PROPERTY_NAME = "name";
	private static final String PROPERTY_REFERENCE = "reference";
	private static final String PROPERTY_JOURNAL_TITLE = "jounral_title";
	private static final String PROPERTY_TITLE = "title";
	private static final String PROPERTY_SUBTITLE = "subtitle";
	private static final String PROPERTY_TRANSLATED_TITLE = "translated_title";
	private static final String PROPERTY_PUBLICATION_DATE = "publication_date";
	private static final String PROPERTY_CITATION = "citation";
	private static final String PROPERTY_IDENTIFIER = "identifier";
	private static final String PROPERTY_DESCRIPTION = "description";
	private static final String PROPERTY_SEQUINCE = "sequince";
	private static final String PROPERTY_ROLE = "role";
	private static final String PROPERTY_CONTRIBUTORS = "contributors";
*/		
	private static final String NAME_SCOPUS_AUTHOR_ID = "Scopus Author ID";
	
	private static final String IDENTIFICATOR_DOI = "DOI";
	private static final String IDENTIFICATOR_ISBN = "ISBN";
	private static final String IDENTIFICATOR_ISSN = "ISSN";
		
/*	private RestAPI graphDb;
	private RestCypherQueryEngine engine;
	private RestIndex<Node> indexOrcidResearcher;
	private RestIndex<Node> indexOrcidIdentificator;
	private RestIndex<Node> indexOrcidPage;
	private RestIndex<Node> indexOrcidWork;*/
	//private RestIndex<Node> indexOrcidContributor;
	
	/*private Label labelOrcid = DynamicLabel.label(LABEL_ORCID);
	private Label labelResearcher = DynamicLabel.label(LABEL_RESEARCHER);
	private Label labelIdentificator = DynamicLabel.label(LABEL_IDENTIFICATOR);
	private Label labelPage = DynamicLabel.label(LABEL_PAGE);
	private Label labelWork = DynamicLabel.label(LABEL_WORK);*/
	//private Label labelContributor = DynamicLabel.label(LABEL_CONTRIBUTOR);
		
/*	private RelationshipType relIdentifiedBy = DynamicRelationshipType.withName(RELATIONSHIP_IDENTIFIED_BY);
	private RelationshipType relLinkedTo = DynamicRelationshipType.withName(RELATIONSHIP_LINKED_TO);
	private RelationshipType relAuthor = DynamicRelationshipType.withName(RELATIONSHIP_AUTHOR);
	private RelationshipType relContributor = DynamicRelationshipType.withName(RELATIONSHIP_CONTRIBUTOR);
*/	
	private Orcid orcid = new Orcid();
	private Neo4jDatabase importer;
	
	private boolean verbose;
	
//	private List<ContributorData> contributos = new ArrayList<ContributorData>();
	
	public static void GetTestRecord(String orcidId) {
		Orcid orcid = new Orcid();
		
		String json = orcid.queryIdString(orcidId, RequestType.profile);
		
		File outFile = new File(orcidId + ".json");
		if (null == json)
			System.out.println("Unable to retreive a record");
		else
			System.out.println(json);
		
		try {
			FileUtils.writeStringToFile(outFile, json);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	/**
	 * Class constructor. 
	 * @param neo4jUrl An URL to the Neo4J
	 * @throws Exception 
	 */
	public ImporterOrcid(final String neo4jUrl) throws Exception {
		importer = new Neo4jDatabase(neo4jUrl);
		
		/*graphDb = new RestAPIFacade(neo4jUrl);
		engine = new RestCypherQueryEngine(graphDb);  
		
		engine.query("CREATE CONSTRAINT ON (n:" + LABEL_ORCID_RESEARCHER + ") ASSERT n." + PROPERTY_KEY + " IS UNIQUE", null);
		engine.query("CREATE CONSTRAINT ON (n:" + LABEL_ORCID_IDENTIFICATOR + ") ASSERT n." + PROPERTY_KEY + " IS UNIQUE", null);
		engine.query("CREATE CONSTRAINT ON (n:" + LABEL_ORCID_PAGE + ") ASSERT n." + PROPERTY_KEY + " IS UNIQUE", null);
		engine.query("CREATE CONSTRAINT ON (n:" + LABEL_ORCID_WORK + ") ASSERT n." + PROPERTY_KEY + " IS UNIQUE", null);
	//	engine.query("CREATE CONSTRAINT ON (n:" + LABEL_ORCID_CONTRIBUTOR + ") ASSERT n." + PROPERTY_KEY + " IS UNIQUE", null);
				
		indexOrcidResearcher = graphDb.index().forNodes(LABEL_ORCID_RESEARCHER);
		indexOrcidIdentificator = graphDb.index().forNodes(LABEL_ORCID_IDENTIFICATOR);
		indexOrcidPage = graphDb.index().forNodes(LABEL_ORCID_PAGE);
		indexOrcidWork = graphDb.index().forNodes(LABEL_ORCID_WORK);
		//indexOrcidContributor = graphDb.index().forNodes(LABEL_ORCID_CONTRIBUTOR);*/
	}
	
	public boolean isVerbose() {
		return verbose;
	}
	
	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}
	
	public void printStatistcs(PrintStream out) {
		importer.printStatistics(out);
	}
	
	/**
	 * Function to import instititions from an CSV file.
	 * For every line in the file, except a header line, an instace of Web:Institution will be 
	 * created. Institution URL will be used as an unique node key. The nodes with the same key 
	 * will NOT be overwritten.
	 * @param institutionsCsv A path to institutions.csv file
	 */
	public void importOrcid(final String orcdiFolder) {
		
		List<GraphSchema> schemas = new ArrayList<GraphSchema>();
		schemas.add(new GraphSchema(GraphUtils.SOURCE_ORCID, GraphUtils.PROPERTY_KEY, true));
		schemas.add(new GraphSchema(GraphUtils.SOURCE_ORCID, GraphUtils.PROPERTY_URL, false));
		schemas.add(new GraphSchema(GraphUtils.SOURCE_ORCID, GraphUtils.PROPERTY_ORCID_ID, false));
		schemas.add(new GraphSchema(GraphUtils.SOURCE_ORCID, GraphUtils.PROPERTY_SCOPUS_ID, false));
		schemas.add(new GraphSchema(GraphUtils.SOURCE_ORCID, GraphUtils.PROPERTY_ISSN, false));
		schemas.add(new GraphSchema(GraphUtils.SOURCE_ORCID, GraphUtils.PROPERTY_ISBN, false));
		schemas.add(new GraphSchema(GraphUtils.SOURCE_ORCID, GraphUtils.PROPERTY_DOI, false));
		
		importer.importSchemas(schemas);
		
		Graph graph = new Graph();
		int chunks = 0;
		
		File[] files = new File(orcdiFolder).listFiles();
		for (File file : files) 
			if (!file.isDirectory())
				try {
					importRecord(file, graph);
					
					if (graph.getNodesCount() >= 1000) {
						
						System.out.println("Import chunk: " + (++chunks));
						importer.importGraph(graph);
					
						graph = new Graph();
					}
					
				} catch (Exception e) {
					e.printStackTrace();
					break;
				}
		
		System.out.println("Import final chunk");
		importer.importGraph(graph);
		
		/*for (ContributorData contributorData : contributos) {
			RestNode nodeResearcher = findNodeByKey(indexOrcidResearcher, PROPERTY_KEY, contributorData.getResearcherKey());
			if (null != nodeResearcher) {
				RestNode nodeWork = graphDb.getNodeById(contributorData.getWorkId());
				if (null != nodeWork) {
					Map<String, Object> attr = new HashMap<String, Object>();
					
					addProperty(attr, PROPERTY_SEQUINCE, contributorData.getSequince());	
					addProperty(attr, PROPERTY_ROLE, contributorData.getRole());
					addProperty(attr, PROPERTY_NAME, contributorData.getName());
						
					createUniqueRelationship(nodeResearcher, nodeWork, 
							relContributor, Direction.OUTGOING, attr);
				}				
			}
		}*/
	} 
		
	private void importRecord(File file, Graph graph) throws Exception {
		/*if (verbose)
			System.out.println("Processing: " + file.getName());*/
				 
		OrcidMessage message = orcid.parseJson(file);
		if (null != message) { // must have a message
			OrcidProfile profile = message.getProfile();
			if (null != profile)  // must have a profile
				processResearcher(profile, graph);
		} else
			throw new Exception("Unable to parse JSON file");
	}
	
	private void processResearcher(OrcidProfile profile, Graph graph) throws Exception {
		
		OrcidIdentifier identifier = profile.getIdentifier();
		if (null != identifier && StringUtils.isNotEmpty(identifier.getUri())) { // must have an identifier
			String key = GraphUtils.extractFormalizedUrl(identifier.getUri());
			
			GraphNode node = new GraphNode()
				.withKey(GraphUtils.SOURCE_ORCID, key)
				.withSource(GraphUtils.SOURCE_ORCID)
				.withType(GraphUtils.TYPE_RESEARCHER)
				.withProperty(GraphUtils.PROPERTY_URL, key)
				.withProperty(GraphUtils.PROPERTY_ORCID_ID, identifier.getPath());
			
//			addProperty(map, PROPERTY_ORCID_TYPE, profile.getType());
			
			OrcidBio bio = profile.getBio();
			if (null != bio) {
				PersonalDetails personalDetails = bio.getPersonalDetails();
				if (null != personalDetails) {
					node.setProperty(GraphUtils.PROPERTY_LAST_NAME, personalDetails.getFamilyName());
					node.setProperty(GraphUtils.PROPERTY_FIRST_NAME, personalDetails.getGivenNames());
//					addProperty(map, PROPERTY_CREADIT_NAME, personalDetails.getCreditName());
	
					String fullName = personalDetails.getFullName();
					if (null != fullName && !fullName.isEmpty())
						node.setProperty(GraphUtils.PROPERTY_FULL_NAME, fullName);
					else 
						fullName = personalDetails.getGivenNames() + " " + personalDetails.getFamilyName();
						
					fullName = fullName.trim();
					node.setProperty(GraphUtils.PROPERTY_TITLE, fullName);
					
					/*OtherNames otherNames = personalDetails.getOtherNames();
					if (null != otherNames && null != otherNames.getNames()) 
						for (String otherName : otherNames.getNames()) 
							if (null != otherName && !otherName.isEmpty() && !otherName.equals(fullName)) 
								addProperty(map, PROPERTY_OTHER_NAMES, otherName);*/
				}
				
				//addProperty(map, PROPERTY_BIOGRAPHY, bio.getBiography());
				
				// try and extract Scopus ID
				ExternalIdentifiers externalIdentifiers =  bio.getExternalIdentifiers();
				if (null != externalIdentifiers) {
					List<ExternalIdentifier> identifiers = externalIdentifiers.getIdentifiers();
					if (null != identifiers) 
						for (ExternalIdentifier externalIdentifier : identifiers) {
							String commonName = externalIdentifier.getCommonName();
							if (null != commonName && commonName.equals(NAME_SCOPUS_AUTHOR_ID)) { 	
								String scopusId = GraphUtils.extractScopusAuthorId(externalIdentifier.getUrl());
								if (StringUtils.isNotEmpty(scopusId))
									node.addProperty(GraphUtils.PROPERTY_SCOPUS_ID, scopusId);
								else
									System.err.println("Unable to extract scopus author id from URL: " + externalIdentifier.getUrl());
							}
						}
				}
	
				graph.addNode(node);
				
				/*OrcidHistory history = profile.getHistory();
				if (null != history) {
					Source source = history.getSource();
					if (null != source) 
						addProperty(map, PROPERTY_ORCID_SOURCE, source.getName());
				}*/
				
				// Creare Orcid:Researcher
			//	System.out.println("Creating Orcid:Researcher " + identifier.getUri());
				
				// Create Orcid:Identificator
				/*if (null != externalIdentifiers) {
					List<ExternalIdentifier> identifiers = externalIdentifiers.getIdentifiers();
					if (null != identifiers) 
						for (ExternalIdentifier externalIdentifier : identifiers) 
							processIdentifier(graph, key, externalIdentifier);
				}*/	
				
				// Create Orcid:Page
				/*ResearcherUrls researcherUrls =  bio.getResearcherUrls();
				if (null != researcherUrls) {
					List<ResearcherUrl> urls = researcherUrls.getUrl();
					for (ResearcherUrl researcherUrl : urls) 
						processOrcidPage(graph, key, researcherUrl); 
				}*/
				
				// Create Orcid:Work
				OrcidActivities activities = profile.getActivities();						
				if (null != activities) {
					OrcidWorks works = activities.getWorks();
					if (null != works && null != works.getWorks())
						for (OrcidWork work : works.getWorks()) 
							processOrcidWork(graph, key, work);
				}
			}
		}	
	}
	
	/*private void processIdentifier(Graph grahp, String key, ExternalIdentifier externalIdentifier) {
		String url = externalIdentifier.getUrl();
		if (null != url && !url.isEmpty()) {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put(PROPERTY_KEY, url);
			map.put(PROPERTY_URL, url);
			map.put(PROPERTY_NODE_SOURCE, LABEL_ORCID);
			map.put(PROPERTY_NODE_TYPE, LABEL_IDENTIFICATOR);
			
			addProperty(map, PROPERTY_NAME, externalIdentifier.getCommonName());
			addProperty(map, PROPERTY_ORCID_ID, externalIdentifier.getOrcidUri());
			addProperty(map, PROPERTY_REFERENCE, externalIdentifier.getReference());
			
		//	System.out.println("Creating Orcid:Identificator " + url);
			RestNode nodeIdentificator = graphDb.getOrCreateNode(indexOrcidIdentificator, 
					PROPERTY_KEY, url, map);
			if (!nodeIdentificator.hasLabel(labelIdentificator))
				nodeIdentificator.addLabel(labelIdentificator); 
			if (!nodeIdentificator.hasLabel(labelOrcid))
				nodeIdentificator.addLabel(labelOrcid);
			
			createUniqueRelationship(nodeResearcher, nodeIdentificator, 
					relIdentifiedBy, Direction.OUTGOING, null);
		}
	}*/
	
	/*private void processOrcidPage(RestNode nodeResearcher, ResearcherUrl researcherUrl) {
		String url = researcherUrl.getUrl();
		if (null != url && !url.isEmpty()) {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put(PROPERTY_KEY, url);
			map.put(PROPERTY_URL, url);
			map.put(PROPERTY_NODE_SOURCE, LABEL_ORCID);
			map.put(PROPERTY_NODE_TYPE, LABEL_PAGE);
			
			addProperty(map, PROPERTY_NAME, researcherUrl.getName());
			
		//	System.out.println("Creating Orcid:Page " + url);
			RestNode nodePage = graphDb.getOrCreateNode(indexOrcidPage, 
					PROPERTY_KEY, url, map);
			if (!nodePage.hasLabel(labelPage))
				nodePage.addLabel(labelPage); 
			if (!nodePage.hasLabel(labelOrcid))
				nodePage.addLabel(labelOrcid);
			
			createUniqueRelationship(nodeResearcher, nodePage, 
					relLinkedTo, Direction.OUTGOING, null);
		}
	}*/
	
	private void processOrcidWork(Graph graph, String researcherKey, OrcidWork work) throws Exception {
		String putCode = work.getPutCode();
		if (null != putCode &&  !putCode.isEmpty()) {
			String key = researcherKey + ":" + putCode;
			
			GraphNode node = new GraphNode()
				.withKey(GraphUtils.SOURCE_ORCID, key)
				.withSource(GraphUtils.SOURCE_ORCID)
				.withType(GraphUtils.TYPE_PUBLICATION)
				.withProperty(GraphUtils.PROPERTY_LOCAL_ID, putCode)
			//	.withProperty(GraphUtils.PROPERTY_TITLE, work.getTitle().getTitle())
				.withProperty(GraphUtils.PROPERTY_PUBLISHED_DATE, work.getPublicationDateString());
				
			graph.addNode(node);
				
			graph.addRelationship(new GraphRelationship()
				.withRelationship(GraphUtils.RELATIONSHIP_AUTHOR)
				.withStart(GraphUtils.SOURCE_ORCID, researcherKey)
				.withEnd(GraphUtils.SOURCE_ORCID, key));
			
			
			WorkTitle title = work.getTitle();
			if (null != title)
				node.addProperty(GraphUtils.PROPERTY_TITLE, title.getTitle());
			
			String url = work.getUrl();
			if (null != url) {
				node.addProperty(GraphUtils.PROPERTY_URL, GraphUtils.extractFormalizedUrl(url));
				
				String eid = GraphUtils.extractScopusEID(url);
				if (null != eid) 
					node.addProperty(GraphUtils.PROPERTY_SCOPUS_EID, eid);				
			}
			
			WorkIdentifiers workIdentifiers = work.getWorlIdentifiers();
			if (null != workIdentifiers && null != workIdentifiers.getIdentifiers()) 
				for (WorkIdentifier workId : workIdentifiers.getIdentifiers()) {
					String type = workId.getType();
					if (null != type) { 
						if (type.equals(IDENTIFICATOR_DOI)) {
							String doi = GraphUtils.extractDoi(workId.getId());
							if (null != doi) {
								node.addProperty(GraphUtils.PROPERTY_DOI, doi);
							//	node.addProperty(GraphUtils.PROPERTY_URL, GraphUtils.generateDoiUri(doi));
							} else
								System.err.println("Unable to extract doi from: " + workId.getId());
						} else if (type.equals(IDENTIFICATOR_ISBN)) {
							node.addProperty(GraphUtils.PROPERTY_ISBN, workId.getId());
						} else if (type.equals(IDENTIFICATOR_ISSN)) {
							node.addProperty(GraphUtils.PROPERTY_ISSN, workId.getId());
						}
					}
				}

			
//			addProperty(map, PROPERTY_PUBLICATION_DATE, work.getPublicationDateString());
//			addProperty(map, PROPERTY_DESCRIPTION, work.getShortDescription());
			
/*			WorkCitation citation = work.getCitation();
			if (null != citation) {
				String property = PROPERTY_CITATION;
				String type = citation.getType();
				if (null != type && !type.isEmpty()) 
					property += "_" + type;
				
				addProperty(map, property, citation.getCitation());
			}*/
			
			/*			
			/*WorkTitle title = work.getTitle();
			if (null != title) {
				addProperty(map, PROPERTY_TITLE, title.getTitle());	
				addProperty(map, PROPERTY_SUBTITLE, title.getSubtitle());
				addProperty(map, PROPERTY_TRANSLATED_TITLE, title.getTranslatedTitle());
			}*/
			
			WorkContributors contributors = work.getWorkContributors();
			if (null != contributors && null != contributors.getContributor()) 
				for (Contributor contributor : contributors.getContributor()) {
					String name = contributor.getCreditName();
				
					node.addProperty(GraphUtils.PROPERTY_AUTHORS, name);

					OrcidIdentifier contributorId = contributor.getOrcidId();
					if (null != contributorId) {
						url = GraphUtils.extractFormalizedUrl(contributorId.getUri());
						if (StringUtils.isNotEmpty(url)) {
							graph.addRelationship(new GraphRelationship()
								.withRelationship(GraphUtils.RELATIONSHIP_RELATED_TO)
								.withStart(GraphUtils.SOURCE_ORCID, url)
								.withEnd(GraphUtils.SOURCE_ORCID, key));
						}
					}
				}

			/*if (!conytributorsSet.isEmpty())
				map.put(PROPERTY_CONTRIBUTORS, conytributorsSet);*/

			//	System.out.println("Creating Orcid:Work " + url);
		/*	RestNode nodeWork = graphDb.getOrCreateNode(indexOrcidWork, 
					PROPERTY_KEY, key, map);
			if (!nodeWork.hasLabel(labelWork))
				nodeWork.addLabel(labelWork); 
			if (!nodeWork.hasLabel(labelOrcid))
				nodeWork.addLabel(labelOrcid);
			
			createUniqueRelationship(nodeResearcher, nodeWork, 
					relAuthor, Direction.OUTGOING, null);
			*/
			//WorkContributors contributors = work.getWorkContributors();
		/*	if (null != contributors && null != contributors.getContributor()) 
				for (Contributor contributor : contributors.getContributor()) 
					processOrcidContributor(key, contributor);*/
		}		
	}
	
	/*private void processOrcidContributor(String workKey, Contributor contributor) {
		// we only interesting in contributors, who have an orcid_id
		OrcidIdentifier contributorId = contributor.getOrcidId();
		if (null != contributorId) {
			// Check what orcid_id is valid
			String url = contributorId.getUri();
			if (StringUtils.isNotEmpty(url)) {
					
				ContributorData contributorData = new ContributorData();
				contributorData.setResearcherKey(url);
				contributorData.setWorkId(workNodeId);
				contributorData.setName(contributor.getCreditName());
					
				ContributorAttributes attributes = contributor.getContributorAttributes();
				if (null != attributes) {
					contributorData.setSequince(attributes.getContributorSequince());
					contributorData.setRole(attributes.getContributorRole());
				}

				contributos.add(contributorData);
			}
		}
		
		/*
				
					// Build an unique key for contributor
					//String key = workKey + "/" + name;
				
					Map<String, Object> map = new HashMap<String, Object>();
					map.put(PROPERTY_KEY, url);
					map.put(PROPERTY_NODE_SOURCE, LABEL_ORCID);
					map.put(PROPERTY_NODE_TYPE, LABEL_CONTRIBUTOR);
					map.put(PROPERTY_NAME, name);
					map.put(PROPERTY_URL, url);
					map.put(PROPERTY_ORCID_ID, contributorId.getPath());
					
					Map<String, Object> attr = null;
					ContributorAttributes attributes = contributor.getContributorAttributes();
					if (null != attributes) {
						attr = new HashMap<String, Object>();
					
						addProperty(attr, PROPERTY_SEQUINCE, attributes.getContributorSequince());	
						addProperty(attr, PROPERTY_ROLE, attributes.getContributorRole());	
					}
					
					//		System.out.println("Creating Orcid:Contributor " + key);
					RestNode nodeContributor = graphDb.getOrCreateNode(indexOrcidContributor, 
							PROPERTY_KEY, url, map);
					if (!nodeContributor.hasLabel(labelContributor))
						nodeContributor.addLabel(labelContributor); 
					if (!nodeContributor.hasLabel(labelOrcid))
						nodeContributor.addLabel(labelOrcid);
						
					createUniqueRelationship(nodeContributor, nodeWork, 
							relContributor, Direction.OUTGOING, attr);
				}
			}
		}*/
	//}

	/*
	@SuppressWarnings("unchecked")
	private void addProperty(Map<String, Object> map, final String key, final String value) {
		if (null != key && null != value && !key.isEmpty()) {
			Object par = map.get(key);
			if (null == par) 
				map.put(key, value);
			else {
				if (par instanceof String) {
					if (((String)par).equals(key))
						return; // we already have this string
					
					Set<String> pars = new HashSet<String>();
					pars.add((String) par);
					pars.add(value);
					map.put(key, pars);
				} else 
					((Set<String>)par).add(value);
			}
		}
	}*/
	
	/*
	private void createUniqueRelationship(RestNode nodeStart, RestNode nodeEnd, 
			RelationshipType type, Direction direction, Map<String, Object> data) {

		// get all node relationships. They should be empty for a new node
		Iterable<Relationship> rels = nodeStart.getRelationships(type, direction);		
		for (Relationship rel : rels) {
			switch (direction) {
			case INCOMING:
				if (rel.getStartNode().getId() == nodeEnd.getId())
					return;
			case OUTGOING:
				if (rel.getEndNode().getId() == nodeEnd.getId())
					return;				
			case BOTH:
				if (rel.getStartNode().getId() == nodeEnd.getId() || 
				    rel.getEndNode().getId() == nodeEnd.getId())
					return;
			}
		}
		
		if (direction == Direction.INCOMING)
			graphDb.createRelationship(nodeEnd, nodeStart, type, data);
		else
			graphDb.createRelationship(nodeStart, nodeEnd, type, data);
	}
	
	private RestNode findNodeByKey(RestIndex<Node> index, String key, String value) {
		IndexHits<Node> hits = index.get(key, value);
		if (null != hits)
			return (RestNode) hits.getSingle();
		else
			return null;
		
	}*/
}
