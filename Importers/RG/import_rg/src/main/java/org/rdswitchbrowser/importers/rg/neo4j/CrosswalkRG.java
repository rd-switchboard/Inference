package org.rdswitchbrowser.importers.rg.neo4j;


import java.io.InputStream;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.commons.lang.StringUtils;

import org.openarchives.oai._2.HeaderType;
import org.openarchives.oai._2.ListRecordsType;
import org.openarchives.oai._2.OAIPMHtype;
import org.openarchives.oai._2.RecordType;
import org.openarchives.oai._2.StatusType;

import org.rdswitchboard.libraries.graph.Graph;
import org.rdswitchboard.libraries.graph.GraphKey;
import org.rdswitchboard.libraries.graph.GraphNode;
import org.rdswitchboard.libraries.graph.GraphRelationship;
import org.rdswitchboard.libraries.graph.GraphSchema;
import org.rdswitchboard.libraries.graph.GraphUtils;
import org.rdswitchboard.libraries.graph.interfaces.GraphCrosswalk;
import org.researchgraph.schema.v2_0.xml.nodes.Dataset;
import org.researchgraph.schema.v2_0.xml.nodes.Grant;
import org.researchgraph.schema.v2_0.xml.nodes.Publication;
import org.researchgraph.schema.v2_0.xml.nodes.RegistryObjects;
import org.researchgraph.schema.v2_0.xml.nodes.Relation;
import org.researchgraph.schema.v2_0.xml.nodes.Researcher;


public class CrosswalkRG implements GraphCrosswalk {
	
	private static final SimpleDateFormat formatter;
	
	static {
		formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX");
		formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
	}
	
	public enum XmlType {
		oai, rg
	}

	private Unmarshaller unmarshaller;
	private long existingRecords = 0;
	private long deletedRecords = 0;
	private long brokenRecords = 0;
	private long filesCounter = 0;
	private long markTime = 0;
	
	private XmlType type = XmlType.oai;
	
	private boolean verbose = false;
	
	private String source = null;
	private boolean needAndsGroup = true;
	
	public CrosswalkRG() throws JAXBException {
		unmarshaller = JAXBContext.newInstance( "org.openarchives.oai._2:org.researchgraph.schema.v2_0.xml.nodes" ).createUnmarshaller();
	}
	
	public long getExistingRecords() {
		return existingRecords;
	}

	public long getDeletedRecords() {
		return deletedRecords;
	}

	public long getBrokenRecords() {
		return brokenRecords;
	}
	
	public long getFilesCounter() {
		return filesCounter;
	}

	public long getMarkTime() {
		return markTime;
	}
	
	public long getSpentTime() {
		return markTime == 0 ? 0 : System.currentTimeMillis() - markTime;
	}
	
	public boolean isVerbose() {
		return verbose;
	}

	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}
	
	public void resetCounters() {
		existingRecords = deletedRecords = brokenRecords = filesCounter = markTime = 0;
	}
	
	public void mark() {
		markTime = System.currentTimeMillis();
	}
	
	public XmlType getType() {
		return type;
	}
	
	public void setType(XmlType type) {
		this.type = type;
	}
	
	@Override
	public void setSource(String source) {
		this.source = source;
		this.needAndsGroup = GraphUtils.SOURCE_ANDS.equals(source);
	}

	@Override
	public String getSource() {
		return source;
	}

	@Override
	public Graph process(InputStream xml) throws Exception {
		if (0 == markTime)
			markTime = System.currentTimeMillis();
		
		++filesCounter;		
		
		Graph graph = new Graph();
		graph.addSchema(new GraphSchema(source, GraphUtils.PROPERTY_KEY, true));
		graph.addSchema(new GraphSchema(source, GraphUtils.PROPERTY_NLA, false));
		graph.addSchema(new GraphSchema(source, GraphUtils.PROPERTY_NHMRC_ID, false));
		graph.addSchema(new GraphSchema(source, GraphUtils.PROPERTY_ARC_ID, false));
		graph.addSchema(new GraphSchema(source, GraphUtils.PROPERTY_ORCID_ID, false));
		graph.addSchema(new GraphSchema(source, GraphUtils.PROPERTY_DOI, false));
		graph.addSchema(new GraphSchema(source, GraphUtils.PROPERTY_PURL, false));
		
		if (type == XmlType.oai)
			processOai((JAXBElement<?>) unmarshaller.unmarshal( xml ), graph);
		else 
			processRegistryObjects((RegistryObjects) unmarshaller.unmarshal( xml ), graph, false);
				
		return graph;
	}
		
	public void printStatistics(PrintStream out) {
		long spentTime = getSpentTime();
		out.println( String.format("Processed %d files.\nSpent %d millisecods.\nFound %d records.\nFound %d deleted records.\nFound %d broken records.\nSpent ~ %f milliseconds per record.", 
				filesCounter, spentTime, existingRecords, deletedRecords, brokenRecords, (float) spentTime / (float) existingRecords));
	}
	
	private void processOai(JAXBElement<?> element , Graph graph) throws Exception {
		OAIPMHtype root = (OAIPMHtype) element.getValue();
		ListRecordsType records = root.getListRecords();
		if (null != records &&  null != records.getRecord()) {
			for (RecordType record : records.getRecord()) {
				HeaderType header = record.getHeader();
					
				StatusType status = header.getStatus();
				boolean deleted = status == StatusType.DELETED;
							
				if (null != record.getMetadata()) {
					Object metadata = record.getMetadata().getAny();
				//	System.out.println(metadata.getClass().toString());
					if (metadata instanceof RegistryObjects) 
						processRegistryObjects((RegistryObjects) metadata, graph, deleted);
					else
						throw new Exception("Metadata is not in rif format");
				} else
					throw new Exception("Unable to find metadata");
			}
		} else
			System.out.println("Unable to find records");
	}
	
	
	
	private void processRegistryObjects(RegistryObjects registryObjects, 
			Graph graph, boolean deleted) throws Exception
	{
		if (null != registryObjects) {
			if (null != registryObjects.getResearchers()) {
				for (Researcher researcher : registryObjects.getResearchers().getResearcher()) {
					processResearcher(researcher, graph, deleted);
				}
			}
			
			if (null != registryObjects.getGrants()) {
				for (Grant grant : registryObjects.getGrants().getGrant()) {
					processGrant(grant, graph, deleted);
				}
			}
			
			if (null != registryObjects.getDatasets()) {
				for (Dataset dataset : registryObjects.getDatasets().getDataset()) {
					processDataset(dataset, graph, deleted);
				}
			}
			
			if (null != registryObjects.getPublications()) {
				for (Publication publication : registryObjects.getPublications().getPublication()) {
					processPublication(publication, graph, deleted);
				}
			}
			
			if (null != registryObjects.getRelations()) {
				for (Relation relation : registryObjects.getRelations().getRelation()) {
					processRelation(relation, graph);
				}
			}
		}
	}
	
	private boolean processResearcher(final Researcher researcher, final Graph graph, boolean deleted) {
		++existingRecords;
		
		if (verbose) 
			System.out.println("Processing Researcher");
		
		String key = researcher.getKey();
		if (StringUtils.isEmpty(key)) {
			return false;
		}	
		
		if (verbose) 
			System.out.println("Key: " + key);
		
		String source = researcher.getSource();
		if (StringUtils.isEmpty(source)) 
			source = this.source;
		
		GraphNode node = new GraphNode()
				.withKey(new GraphKey(source, key))
				.withSource(source)
				.withType(GraphUtils.TYPE_RESEARCHER);
		
		if (deleted) {
			graph.addNode(node.withDeleted(true));
			
			++deletedRecords;
			
			return true;
		}
		
		String localId = researcher.getLocalId();
		if (!StringUtils.isEmpty(localId)) 
			node.setProperty(GraphUtils.PROPERTY_LOCAL_ID, localId);
		
		XMLGregorianCalendar lastUpdated = researcher.getLastUpdated();
		if (null != lastUpdated) {
			String lastUpdatedString = formatter.format(lastUpdated.toGregorianCalendar().getTime());
			if (!StringUtils.isEmpty(lastUpdatedString)) 
				node.setProperty(GraphUtils.PROPERTY_LAST_UPDATED, lastUpdatedString);
		}
		
		String url = GraphUtils.extractFormalizedUrl(researcher.getUrl());
		if (!StringUtils.isEmpty(url)) 
			node.setProperty(GraphUtils.PROPERTY_URL, url);
		
		String fullName = researcher.getFullName();
		if (!StringUtils.isEmpty(fullName)) 
			node.setProperty(GraphUtils.PROPERTY_FULL_NAME, fullName);
		
		String firstName = researcher.getFirstName();
		if (!StringUtils.isEmpty(firstName)) 
			node.setProperty(GraphUtils.PROPERTY_FIRST_NAME, firstName);
		
		String lastName = researcher.getLastName();
		if (!StringUtils.isEmpty(lastName)) 
			node.setProperty(GraphUtils.PROPERTY_LAST_NAME, lastName);
		
		String orcid = GraphUtils.extractOrcidId(researcher.getOrcid());
		if (!StringUtils.isEmpty(orcid)) 
			node.setProperty(GraphUtils.PROPERTY_ORCID_ID, orcid);
		
		String scopus = GraphUtils.extractScopusAuthorId(researcher.getScopusAuthorId());
		if (!StringUtils.isEmpty(scopus)) 
			node.setProperty(GraphUtils.PROPERTY_SCOPUS_ID, scopus);
		
		graph.addNode(node);
		
		return true;
	}
	
	private boolean processGrant(final Grant grant, final Graph graph, boolean deleted) {
		++existingRecords;
		
		if (verbose) 
			System.out.println("Processing Grant");
		
		String key = grant.getKey();
		if (StringUtils.isEmpty(key)) {
			return false;
		}	
		
		if (verbose) 
			System.out.println("Key: " + key);
		
		String source = grant.getSource();
		if (StringUtils.isEmpty(source)) 
			source = this.source;
		
		GraphNode node = new GraphNode()
				.withKey(new GraphKey(source, key))
				.withSource(source)
				.withType(GraphUtils.TYPE_GRANT);
		
		if (deleted) {
			graph.addNode(node.withDeleted(true));
			
			++deletedRecords;
			
			return true;
		}
		
		String localId = grant.getLocalId();
		if (!StringUtils.isEmpty(localId)) 
			node.setProperty(GraphUtils.PROPERTY_LOCAL_ID, localId);
		
		XMLGregorianCalendar lastUpdated = grant.getLastUpdated();
		if (null != lastUpdated) {
			String lastUpdatedString = formatter.format(lastUpdated.toGregorianCalendar().getTime());
			if (!StringUtils.isEmpty(lastUpdatedString)) 
				node.setProperty(GraphUtils.PROPERTY_LAST_UPDATED, lastUpdatedString);
		}
		
		String url = GraphUtils.extractFormalizedUrl(grant.getUrl());
		if (!StringUtils.isEmpty(url)) 
			node.setProperty(GraphUtils.PROPERTY_URL, url);
		
		String title = grant.getTitle();
		if (!StringUtils.isEmpty(title)) 
			node.setProperty(GraphUtils.PROPERTY_TITLE, title);
		
		String purl = GraphUtils.extractFormalizedUrl(grant.getPurl());
		if (!StringUtils.isEmpty(purl)) 
			node.setProperty(GraphUtils.PROPERTY_PURL, purl);
		
		String participantList = grant.getParticipantList();
		if (!StringUtils.isEmpty(participantList)) {
			String[] participants = participantList.trim().split("\\s*,\\s*");
			if (participants.length > 0)
				node.setProperty(GraphUtils.PROPERTY_PARTICIPANTS, participants);
		}
			
		String funder = GraphUtils.extractFormalizedUrl(grant.getFunder());
		if (!StringUtils.isEmpty(funder)) 
			node.setProperty(GraphUtils.PROPERTY_FUNDER, funder);
		
		XMLGregorianCalendar startYear = grant.getStartYear();
		if (null != startYear && startYear.getYear() > 0)
			node.setProperty(GraphUtils.PROPERTY_START_YEAR, startYear.getYear());
		
		XMLGregorianCalendar endYear = grant.getEndYear();
		if (null != endYear && endYear.getYear() > 0)
			node.setProperty(GraphUtils.PROPERTY_END_YEAR, endYear.getYear());
		
		graph.addNode(node);
		
		return true;
	}
	
	private boolean processDataset(final Dataset dataset, final Graph graph, boolean deleted) {
		++existingRecords;
		
		if (verbose) 
			System.out.println("Processing Dataset");
		
		String key = dataset.getKey();
		if (StringUtils.isEmpty(key)) {
			return false;
		}	
		
		if (verbose) 
			System.out.println("Key: " + key);
		
		String source = dataset.getSource();
		if (StringUtils.isEmpty(source)) 
			source = this.source;
		
		GraphNode node = new GraphNode()
				.withKey(new GraphKey(source, key))
				.withSource(source)
				.withType(GraphUtils.TYPE_DATASET);
		
		if (deleted) {
			graph.addNode(node.withDeleted(true));
			
			++deletedRecords;
			
			return true;
		}
		
		String localId = dataset.getLocalId();
		if (!StringUtils.isEmpty(localId)) 
			node.setProperty(GraphUtils.PROPERTY_LOCAL_ID, localId);
		
		XMLGregorianCalendar lastUpdated = dataset.getLastUpdated();
		if (null != lastUpdated) {
			String lastUpdatedString = formatter.format(lastUpdated.toGregorianCalendar().getTime());
			if (!StringUtils.isEmpty(lastUpdatedString)) 
				node.setProperty(GraphUtils.PROPERTY_LAST_UPDATED, lastUpdatedString);
		}
		
		String url = GraphUtils.extractFormalizedUrl(dataset.getUrl());
		if (!StringUtils.isEmpty(url)) 
			node.setProperty(GraphUtils.PROPERTY_URL, url);
		
		String title = dataset.getTitle();
		if (!StringUtils.isEmpty(title)) 
			node.setProperty(GraphUtils.PROPERTY_TITLE, title);
		
		String doi = GraphUtils.extractDoi(dataset.getDoi());
		if (!StringUtils.isEmpty(doi)) 
			node.setProperty(GraphUtils.PROPERTY_DOI, doi);
		
		XMLGregorianCalendar publicationYear = dataset.getPublicationYear();
		if (null != publicationYear && publicationYear.getYear() > 0)
			node.setProperty(GraphUtils.PROPERTY_PUBLICATION_YEAR, publicationYear.getYear());
		
		String license = GraphUtils.extractFormalizedUrl(dataset.getLicense());
		if (!StringUtils.isEmpty(license)) 
			node.setProperty(GraphUtils.PROPERTY_LICENSE, license);
		
		BigDecimal megabyte = dataset.getMegabyte();
		if (null != megabyte)
			node.setProperty(GraphUtils.PROPERTY_MEGABYTE, megabyte.toString());
		
		graph.addNode(node);
		
		return true;
	}
	
	private boolean processPublication(final Publication publication, final Graph graph, boolean deleted) {
		++existingRecords;
				
		if (verbose) 
			System.out.println("Processing Publication");
		
		String key = publication.getKey();
		if (StringUtils.isEmpty(key)) {
			return false;
		}	
		
		if (verbose) 
			System.out.println("Key: " + key);
		
		String source = publication.getSource();
		if (StringUtils.isEmpty(source)) 
			source = this.source;
		
		GraphNode node = new GraphNode()
				.withKey(new GraphKey(source, key))
				.withSource(source)
				.withType(GraphUtils.TYPE_PUBLICATION);
		
		if (deleted) {
			graph.addNode(node.withDeleted(true));
			
			++deletedRecords;
			
			return true;
		}
		
		String localId = publication.getLocalId();
		if (!StringUtils.isEmpty(localId)) 
			node.setProperty(GraphUtils.PROPERTY_LOCAL_ID, localId);
		
		XMLGregorianCalendar lastUpdated = publication.getLastUpdated();
		if (null != lastUpdated) {
			String lastUpdatedString = formatter.format(lastUpdated.toGregorianCalendar().getTime());
			if (!StringUtils.isEmpty(lastUpdatedString)) 
				node.setProperty(GraphUtils.PROPERTY_LAST_UPDATED, lastUpdatedString);
		}
		
		String url = GraphUtils.extractFormalizedUrl(publication.getUrl());
		if (!StringUtils.isEmpty(url)) 
			node.setProperty(GraphUtils.PROPERTY_URL, url);
		
		String title = publication.getTitle();
		if (!StringUtils.isEmpty(title)) 
			node.setProperty(GraphUtils.PROPERTY_TITLE, title);
		
		String authorsList = publication.getAuthorsList();
		if (!StringUtils.isEmpty(authorsList)) {
			String[] authors = authorsList.trim().split("\\s*,\\s*");
			if (authors.length > 0)
				node.setProperty(GraphUtils.PROPERTY_AUTHORS, authors);
		}
		
		String doi = GraphUtils.extractDoi(publication.getDoi());
		if (!StringUtils.isEmpty(doi)) 
			node.setProperty(GraphUtils.PROPERTY_DOI, doi);
		
		XMLGregorianCalendar publicationYear = publication.getPublicationYear();
		if (null != publicationYear && publicationYear.getYear() > 0)
			node.setProperty(GraphUtils.PROPERTY_PUBLICATION_YEAR, publicationYear.getYear());
		
		String scopusEid = GraphUtils.extractScopusEID(publication.getScopusEid());
		if (!StringUtils.isEmpty(scopusEid)) 
			node.setProperty(GraphUtils.PROPERTY_SCOPUS_EID, scopusEid);
		
		graph.addNode(node);
		
		return true;
	}
	
	private boolean processRelation(final Relation relation, final Graph graph) {
		if (verbose) 
			System.out.println("Processing Publication");
	
		String label = relation.getLabel();
		if (!StringUtils.isEmpty(label)) 
			label = GraphUtils.RELATIONSHIP_RELATED_TO;
			
		String from = relation.getFromKey();
		if (!StringUtils.isEmpty(from)) {
			return false;
		}
		
		String to = relation.getToUri();
		if (!StringUtils.isEmpty(to)) {
			return false;
		}
			
		GraphRelationship relationship = new GraphRelationship()
				.withRelationship(label)
				.withStart(source, from)
				.withEnd(source, to);
				
		graph.addRelationship(relationship);
		
		return true;
	}
}
