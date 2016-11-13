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
import org.rdswitchboard.libraries.orcid.Contributor;
import org.rdswitchboard.libraries.orcid.ExternalIdentifier;
import org.rdswitchboard.libraries.orcid.ExternalIdentifiers;
import org.rdswitchboard.libraries.orcid.Orcid;
import org.rdswitchboard.libraries.orcid.OrcidActivities;
import org.rdswitchboard.libraries.orcid.OrcidBio;
import org.rdswitchboard.libraries.orcid.OrcidIdentifier;
import org.rdswitchboard.libraries.orcid.OrcidMessage;
import org.rdswitchboard.libraries.orcid.OrcidProfile;
import org.rdswitchboard.libraries.orcid.OrcidWork;
import org.rdswitchboard.libraries.orcid.OrcidWorks;
import org.rdswitchboard.libraries.orcid.PersonalDetails;
import org.rdswitchboard.libraries.orcid.RequestType;
import org.rdswitchboard.libraries.orcid.WorkContributors;
import org.rdswitchboard.libraries.orcid.WorkIdentifier;
import org.rdswitchboard.libraries.orcid.WorkIdentifiers;
import org.rdswitchboard.libraries.orcid.WorkTitle;

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
	private static final String NAME_SCOPUS_AUTHOR_ID = "Scopus Author ID";
	
	private static final String IDENTIFICATOR_DOI = "DOI";
	private static final String IDENTIFICATOR_ISBN = "ISBN";
	private static final String IDENTIFICATOR_ISSN = "ISSN";
		
	private Orcid orcid = new Orcid();
	private Neo4jDatabase importer;
	
	private boolean verbose;
		
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
	 * @throws Exception 
	 */
	public void importOrcid(final String orcdiFolder) throws Exception {
		
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
		int fileCounter = 0;
		
		File[] files = new File(orcdiFolder).listFiles();
		for (File file : files) 
			if (!file.isDirectory()) {
				importRecord(file, graph);
				
				if (graph.getNodesCount() >= 1000) {
					
					System.out.println("Import chunk: " + (++chunks));
					importer.importGraph(graph);
				
					graph = new Graph();
				}
				
				if (++fileCounter % 1000 == 0) {
					System.out.println("Processed : " + fileCounter + " files");
				}
			}
		
		System.out.println("Import final chunk");
		importer.importGraph(graph);
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
				}
				
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
		}		
	}
}
