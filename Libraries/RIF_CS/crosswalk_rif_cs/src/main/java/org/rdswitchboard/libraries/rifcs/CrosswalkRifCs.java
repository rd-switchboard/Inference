package org.rdswitchboard.libraries.rifcs;

import java.io.InputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

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

import au.org.ands.standards.rif_cs.registryobjects.Activity;
import au.org.ands.standards.rif_cs.registryobjects.Collection;
import au.org.ands.standards.rif_cs.registryobjects.DatesType;
import au.org.ands.standards.rif_cs.registryobjects.DatesType.Date;
import au.org.ands.standards.rif_cs.registryobjects.IdentifierType;
import au.org.ands.standards.rif_cs.registryobjects.NameType;
import au.org.ands.standards.rif_cs.registryobjects.Party;
import au.org.ands.standards.rif_cs.registryobjects.RegistryObjects;
import au.org.ands.standards.rif_cs.registryobjects.RelatedObjectType;
import au.org.ands.standards.rif_cs.registryobjects.RelationType;
import au.org.ands.standards.rif_cs.registryobjects.Service;

public class CrosswalkRifCs implements GraphCrosswalk {	
	private static final String COLLECTION_TYPE_DATASET = "dataset";
	private static final String COLLECTION_TYPE_NON_GEOGRAOPHIC_DATASET = "nonGeographicDataset";
	private static final String COLLECTION_TYPE_RESEARCH_DATASET = "researchDataSet";
	
	/*private static final String ACTIVITY_TYPE_PROJECT = "project";
	private static final String ACTIVITY_TYPE_PROGRAM = "program";
	private static final String ACTIVITY_TYPE_AWARD = "award";*/
	
	private static final String PARTY_TYPE_PERSON = "person";
	private static final String PARTY_TYPE_PUBLISHER = "publisher";
	private static final String PARTY_TYPE_GROUP = "group";
	private static final String PARTY_TYPE_ADMINISTRATIVE_POSITION = "administrativePosition";
	
	private static final String IDENTIFICATOR_NLA = "AU-ANL:PEAU";
	private static final String IDENTIFICATOR_LOCAL = "local";
	private static final String IDENTIFICATOR_ARC = "arc";
	private static final String IDENTIFICATOR_NHMRC = "nhmrc";
	private static final String IDENTIFICATOR_ORCID = "orcid";
	private static final String IDENTIFICATOR_DOI = "doi";
	private static final String IDENTIFICATOR_PURL = "purl";
	
	private static final String NAME_PRIMARY = "primary";
	
	private static final String NAME_PART_FAMILY = "family";
	private static final String NAME_PART_GIVEN = "given";
	private static final String NAME_PART_SUFFIX = "suffix";
	private static final String NAME_PART_TITLE = "title";
	
	private static final String[] GRANT_DATES = new String[] { "startDate" };
	private static final String[] COLLECTION_DATES = new String[] { null };

	private Unmarshaller unmarshaller;
	private long existingRecords = 0;
	private long deletedRecords = 0;
	private long brokenRecords = 0;
	private long filesCounter = 0;
	private long markTime = 0;
	
	private boolean verbose = false;
	
	private String source;
	
	public CrosswalkRifCs() throws JAXBException {
		unmarshaller = JAXBContext.newInstance( "org.openarchives.oai._2:au.org.ands.standards.rif_cs.registryobjects:au.org.ands.standards.rif_cs.extendedregistryobjects" ).createUnmarshaller();
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
	
	@Override
	public void setSource(String source) {
		this.source = source;
		
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
		
		JAXBElement<?> element = (JAXBElement<?>) unmarshaller.unmarshal( xml );
		Graph graph = new Graph();
		graph.addSchema(new GraphSchema(source, GraphUtils.PROPERTY_KEY, true));
		graph.addSchema(new GraphSchema(source, GraphUtils.PROPERTY_NLA, false));
		graph.addSchema(new GraphSchema(source, GraphUtils.PROPERTY_NHMRC_ID, false));
		graph.addSchema(new GraphSchema(source, GraphUtils.PROPERTY_ARC_ID, false));
		graph.addSchema(new GraphSchema(source, GraphUtils.PROPERTY_ORCID_ID, false));
		graph.addSchema(new GraphSchema(source, GraphUtils.PROPERTY_DOI, false));
		graph.addSchema(new GraphSchema(source, GraphUtils.PROPERTY_PURL, false));
		
		OAIPMHtype root = (OAIPMHtype) element.getValue();
		ListRecordsType records = root.getListRecords();
		if (null != records &&  null != records.getRecord()) {
			for (RecordType record : records.getRecord()) {
				HeaderType header = record.getHeader();
					
				StatusType status = header.getStatus();
				boolean deleted = status == StatusType.DELETED;
							
				if (null != record.getMetadata()) {
					Object metadata = record.getMetadata().getAny();
					if (metadata instanceof RegistryObjects) {
						RegistryObjects registryObjects = (RegistryObjects) metadata;
						if (registryObjects.getRegistryObject() != null && registryObjects.getRegistryObject().size() > 0) {
							for (RegistryObjects.RegistryObject registryObject : registryObjects.getRegistryObject()) {
								String group = registryObject.getGroup();
								String key = registryObject.getKey();
								if (verbose) 
									System.out.println("Key: " + key);
								
								String url = null;
								try {
									url = GraphUtils.generateAndsUrl(key);
								} catch(UnsupportedEncodingException e) {
									e.printStackTrace();
								}								
								
								GraphNode node = new GraphNode()
									.withKey(new GraphKey(source, key))
									.withSource(source)
									.withProperty(GraphUtils.PROPERTY_URL, url)
									.withProperty(GraphUtils.PROPERTY_ANDS_GROUP, group);
								
								if (deleted) {
									graph.addNode(node.withDeleted(true));
									
									++deletedRecords;
								} else if (registryObject.getCollection() != null)
									importCollection(graph, node, registryObject.getCollection());
								else if (registryObject.getActivity() != null) 
									importActivity(graph, node, registryObject.getActivity());
								else if (registryObject.getService() != null)
									importService(graph, node, registryObject.getService());
								else if (registryObject.getParty() != null)
									importParty(graph, node, registryObject.getParty());	
								else {
									graph.addNode(node.withBroken(true));
									
									++brokenRecords;
								}
								
								++existingRecords;	
							}
								
							// at this point all registry objects should be imported, abort the function
						} else
							throw new Exception("Metadata does not contains any records");
					} else
						throw new Exception("Metadata is not in rif format");
				} else
					throw new Exception("Unable to find metadata");
			}
		} else
			System.out.println("Unable to find records");
		
		return graph;
	}
	
	public void printStatistics(PrintStream out) {
		long spentTime = getSpentTime();
		out.println( String.format("Processed %d files.\nSpent %d millisecods.\nFound %d records.\nFound %d deleted records.\nFound %d broken records.\nSpent ~ %f milliseconds per record.", 
				filesCounter, spentTime, existingRecords, deletedRecords, brokenRecords, (float) spentTime / (float) existingRecords));
	}
	
	private boolean importCollection(Graph graph, GraphNode node, Collection collection) {
		String type = collection.getType();
		if (type.equals(COLLECTION_TYPE_DATASET) 
				|| type.equals(COLLECTION_TYPE_NON_GEOGRAOPHIC_DATASET) 
				|| type.equals(COLLECTION_TYPE_RESEARCH_DATASET))
			node.setType(GraphUtils.TYPE_DATASET);
		else
			return false;// ignore
				
		for (Object object : collection.getIdentifierOrNameOrDates()) {
			if (object instanceof IdentifierType) 
				processIdentifier(node, (IdentifierType) object);
			else if (object instanceof NameType)
				processName(node, (NameType) object);
			else if (object instanceof RelatedObjectType) 
				processRelatedObject(graph, node.getKey(), (RelatedObjectType) object);
			else if (object instanceof DatesType) 
				processDates(node, GraphUtils.PROPERTY_PUBLISHED_DATE, COLLECTION_DATES, (DatesType) object);
		}
		
		graph.addNode(node);
		
		return true;
	}
	
	private boolean importActivity(Graph graph, GraphNode node, Activity activity) {
/*		String type = activity.getType();
		if (type.equals(ACTIVITY_TYPE_PROJECT) 
				|| type.equals(ACTIVITY_TYPE_PROGRAM) 
				|| type.equals(ACTIVITY_TYPE_AWARD))*/
			node.setType(GraphUtils.TYPE_GRANT);
//		else
//			return false;// ignore
				
		for (Object object : activity.getIdentifierOrNameOrLocation()) {
			if (object instanceof IdentifierType) 
				processIdentifier(node, (IdentifierType) object);
			else if (object instanceof NameType)
				processName(node, (NameType) object);
			else if (object instanceof RelatedObjectType) 
				processRelatedObject(graph, node.getKey(), (RelatedObjectType) object);
			else if (object instanceof DatesType) 
				processDates(node, GraphUtils.PROPERTY_AWARDED_DATE, GRANT_DATES, (DatesType) object);
		}
		
		graph.addNode(node);
		
		return true;
	}
	
	private boolean importService(Graph graph, GraphNode node, Service service) {
		return false; // ignore all
	}
	
	private boolean importParty(Graph graph, GraphNode node, Party party) {
		String type = party.getType();
		if (type.equals(PARTY_TYPE_PERSON) || type.equals(PARTY_TYPE_PUBLISHER))
			node.setType(GraphUtils.TYPE_RESEARCHER);
		else if (type.equals(PARTY_TYPE_GROUP) || type.equals(PARTY_TYPE_ADMINISTRATIVE_POSITION))
			node.setType(GraphUtils.TYPE_INSTITUTION);
		else
			return false;// ignore
				
		for (Object object : party.getIdentifierOrNameOrLocation()) {
			if (object instanceof IdentifierType) 
				processIdentifier(node, (IdentifierType) object);
			else if (object instanceof NameType)
				processName(node, (NameType) object);
			else if (object instanceof RelatedObjectType) 
				processRelatedObject(graph, node.getKey(), (RelatedObjectType) object);
		}
		
		graph.addNode(node);
		
		return true;
	}
	
	private void processIdentifier(GraphNode node, IdentifierType identifier) {
		String type = identifier.getType();
		String key = identifier.getValue();
		if (null != type) {
			if (type.equals(IDENTIFICATOR_NLA)) {
				type = GraphUtils.PROPERTY_NLA;
				key = GraphUtils.extractFormalizedUrl(key);
			} else if (type.equals(IDENTIFICATOR_LOCAL))
				type = GraphUtils.PROPERTY_LOCAL_ID;
			else if (type.equals(IDENTIFICATOR_ARC))
				type = GraphUtils.PROPERTY_ARC_ID;
			else if (type.equals(IDENTIFICATOR_NHMRC))
				type = GraphUtils.PROPERTY_NHMRC_ID;
			else if (type.equals(IDENTIFICATOR_ORCID)) {
				type = GraphUtils.PROPERTY_ORCID_ID;
				key = GraphUtils.extractOrcidId(key);
			}
			else if (type.equals(IDENTIFICATOR_DOI)) {
				type = GraphUtils.PROPERTY_DOI;
				key = GraphUtils.extractDoi(key);
			}
			else if (type.equals(IDENTIFICATOR_PURL)) {
				type = GraphUtils.PROPERTY_PURL;
				key = GraphUtils.extractFormalizedUrl(key);
			} else 
				type = null;			
		}
		
		if (null != type && StringUtils.isNotEmpty(key)) 
			node.addProperty(type, key);
	}
	
	private void processName(GraphNode node, NameType name) {
		String type = name.getType();
		if (null != type && type.equals(NAME_PRIMARY)) {
			String family = null;
			String given = null;
			String title = null;
			String suffix =  null;
			
			for (NameType.NamePart part : name.getNamePart()) {
				final String nameType = part.getType();
				if (null != nameType && !nameType.isEmpty()) {
					if (nameType.equals(NAME_PART_FAMILY)) {
						family = part.getValue();
						node.addProperty(GraphUtils.PROPERTY_LAST_NAME, family);
					} else if (nameType.equals(NAME_PART_GIVEN)) {
						given = part.getValue();
						node.addProperty(GraphUtils.PROPERTY_FIRST_NAME, given);
					} else if (nameType.equals(NAME_PART_TITLE)) {
						title = part.getValue();
						node.addProperty(GraphUtils.PROPERTY_NAME_PREFIX, title);
					} else if (nameType.equals(NAME_PART_SUFFIX)) {
						suffix = part.getValue();
						node.addProperty(GraphUtils.PROPERTY_NAME_PREFIX, suffix);
					}
				}				
			}
			
			StringBuilder sb = new StringBuilder();
			if (null != title && !title.isEmpty()) 
				sb.append(title);
			if (null != suffix && !suffix.isEmpty()) {
				if (sb.length() > 0)
					sb.append(" ");
				
				sb.append(suffix);
			}
			if (null != given && !given.isEmpty()) {
				if (sb.length() > 0)
					sb.append(" ");
				
				sb.append(given);
			}
			if (null != family && !family.isEmpty()) {
				if (sb.length() > 0)
					sb.append(" ");
				
				sb.append(family);
			}
			for (NameType.NamePart part : name.getNamePart()) {
				final String nameType = part.getType();
				if (null != nameType 
						&& !nameType.isEmpty() 
						&& (nameType.equals(NAME_PART_FAMILY) 
								|| nameType.equals(NAME_PART_GIVEN) 
								|| nameType.equals(NAME_PART_TITLE) 
								|| nameType.equals(NAME_PART_SUFFIX)))
					continue;
				
				if (sb.length() > 0)
					sb.append(" ");
				
				sb.append(part.getValue());				
			}
			
			String fullName = sb.toString();
			if (!fullName.isEmpty())
				node.addProperty(GraphUtils.PROPERTY_TITLE, fullName);
		}
	}
	
	private void processRelatedObject(Graph graph, GraphKey from, RelatedObjectType relatedObject) {
		for (RelationType relType : relatedObject.getRelation()) {
			String key = relatedObject.getKey();
			String type = relType.getType();
			if (null != key && !key.isEmpty() && null != type && !type.isEmpty()) { 
				GraphRelationship relationship = new GraphRelationship()
					.withRelationship(type)
					.withStart(from)
					.withEnd(new GraphKey(from.getIndex(), key));
				
				graph.addRelationship(relationship);
			}
		}
	}
	
	private void processDates(GraphNode node, String propertyName, String[] types, DatesType dates) {
		List<Date> list = dates.getDate();
		if (null != list) {
			String date = null;
			
			for (String type : types) 
				if ((date = extractDate(type, list)) != null)
					break;
			
			if (null != date)
				node.addProperty(propertyName, date);
		}
	}
	
	private String extractDate(String type, List<Date> dates) {
		for (Date date : dates) {
			if (null == type || type.equals(date.getType()))
				return date.getValue();
		}
		
		return null;		
	}	
}
