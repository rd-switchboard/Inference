package org.rdswitchboard.libraries.dara;

import java.io.InputStream;
import java.io.PrintStream;
import java.math.BigInteger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.commons.lang.StringUtils;
import org.openarchives.oai._2.Creator;
import org.openarchives.oai._2.Creators;
import org.openarchives.oai._2.HeaderType;
import org.openarchives.oai._2.ListRecordsType;
import org.openarchives.oai._2.OAIPMHtype;
import org.openarchives.oai._2.Person;
import org.openarchives.oai._2.PublicationDate;
import org.openarchives.oai._2.Relation;
import org.openarchives.oai._2.Relations;
import org.openarchives.oai._2.Resource;
import org.openarchives.oai._2.ResourceIdentifier;
import org.openarchives.oai._2.StatusType;
import org.openarchives.oai._2.Title;
import org.openarchives.oai._2.Titles;
import org.rdswitchboard.libraries.graph.Graph;
import org.rdswitchboard.libraries.graph.GraphKey;
import org.rdswitchboard.libraries.graph.GraphNode;
import org.rdswitchboard.libraries.graph.GraphSchema;
import org.rdswitchboard.libraries.graph.GraphUtils;
import org.rdswitchboard.libraries.graph.interfaces.GraphCrosswalk;


public class CrosswalkDara implements GraphCrosswalk {

	private static final String NODE_RESOURCE = "resource";
	private static final String LANG_EN = "en";
	private static final String TYPE_DOI = "doi";
	
	private Unmarshaller unmarshaller;
	
	private long processedFiles = 0;
	private long processedRecords = 0;
	private long deletedRecords = 0;
	private long createdRecords = 0;
	private long createdRelationships = 0;

	private long markTime = 0;
	
	private boolean verbose = false;
	
	private String source;
	
	/**
	 * Class constructor
	 * 
	 * @throws JAXBException
	 */
	
	public CrosswalkDara() throws JAXBException {
		// configure unmarshaller
		unmarshaller = JAXBContext.newInstance( "org.openarchives.oai._2" ).createUnmarshaller();
	}

	/**
	 * getCreatedRecords
	 * @return
	 */
	public long getCreatedRecords() {
		return createdRecords;
	}
	
	/**
	 * getDeletedRecords
	 * @return
	 */
	public long getDeletedRecords() {
		return deletedRecords;
	}

	/**
	 * getBrokenRecords
	 * @return
	 */
	public long getProcessedRecords() {
		return processedRecords;
	}
	
	/**
	 * getCreatedRelationships
	 * @return
	 */
	public long getCreatedRelationships() {
		return createdRelationships;
	}

	/**
	 * getProcessedFiles
	 * @return
	 */
	public long getProcessedFiles() {
		return processedFiles;
	}

	/**
	 * getMarkTime
	 * @return
	 */
	public long getMarkTime() {
		return markTime;
	}
	
	/**
	 * getSpentTime
	 * @return
	 */
	public long getSpentTime() {
		return markTime == 0 ? 0 : System.currentTimeMillis() - markTime;
	}
	
	/**
	 * isVerbose
	 * @return
	 */
	public boolean isVerbose() {
		return verbose;
	}

	/**
	 * setVerbose
	 * @param verbose
	 */
	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}
	
	/**
	 * reset counters
	 */
	public void resetCounters() {
		createdRecords = deletedRecords = processedRecords = createdRelationships = processedFiles = markTime = 0;
	}
	
	/**
	 * mark time
	 */
	public void mark() {
		markTime = System.currentTimeMillis();
	}
	
	@Override
	public String getSource() {
		return source;
	}

	@Override
	public void setSource(String source) {
		this.source = source;
	}

	/**
	 * Process XML Document
	 * @param source String - Data Source Name
	 * @param xml InputStream - Input Stream containing an XML
	 * @return Graph object
	 * @throws JAXBException 
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Graph process(InputStream xml) throws Exception  {
		if (0 == markTime)
			markTime = System.currentTimeMillis();
		
		++processedFiles;
		
		// unmarshall XML file
		JAXBElement<?> element = (JAXBElement<?>) unmarshaller.unmarshal( xml );

		// create graph object
		Graph graph = new Graph();
		// setup graph schema
		graph.addSchema(new GraphSchema(source, GraphUtils.PROPERTY_KEY, true));
		graph.addSchema(new GraphSchema(source, GraphUtils.PROPERTY_DOI, false));
		graph.addSchema(new GraphSchema(source, GraphUtils.PROPERTY_DARA_ID, false));
		// extract root object
		if (element.getValue() instanceof OAIPMHtype) {
			OAIPMHtype root = (OAIPMHtype) element.getValue();
			// extract all records
			ListRecordsType records = root.getListRecords();
			// check if file has some records
			if (null != records &&  null != records.getRecord()) {
				// process all records
				for (org.openarchives.oai._2.RecordType record : records.getRecord()) {
					// extract record header
					HeaderType header = record.getHeader();
					
					// extract record identifier
					String idetifier = header.getIdentifier();
					if (verbose)
						System.out.println("Record: " + idetifier.toString());
					if (StringUtils.isNotBlank(idetifier)) {
						// String oai = GraphUtils.extractOai(idetifier);
				
						++processedRecords;
						
						// extract record status
						StatusType status = header.getStatus();
				
						// create new node
						GraphNode node = new GraphNode()
							.withKey(new GraphKey(source, idetifier))
							.withSource(source);
//							.withType(GraphUtils.TYPE_DATASET);
					//		.withProperty(GraphUtils.PROPERTY_OAI, oai);
						
						// add it to the graph						
						// check if record has been marked as deleted
						if (status == StatusType.DELETED) {
							graph.addNode(node.withDeleted(true));
							++deletedRecords;
						}
						
						// check if record has metadata
						if (null != record.getMetadata()) {
							// we expect only one metadata object per record
							Object metadata = record.getMetadata().getAny();
							
							
							if (null != metadata && metadata instanceof Resource) {
								if (processRecord(graph, node, (Resource) metadata)) { 
									graph.addNode(node);
									++createdRecords;
								}
							}
						} 
					}
				} 
			} 
		} else
			throw new Exception("This is not OAI:PMH Document");
		
		return graph;
	}
	
	/**
	 * Print Statistics
	 * @param out
	 */
	public void printStatistics(PrintStream out) {
		long spentTime = getSpentTime();

		out.println("Spent " + spentTime + " millisecods.");
		out.println("Processed " + processedFiles + " files.");
		out.println("Processed " + processedRecords + " records.");
		out.println(createdRecords + " records has been created.");
		out.println(createdRelationships + " relationships has been created.");
		out.println(deletedRecords + " records has been deleted.");
		out.println((processedRecords - deletedRecords - createdRecords) + " records has been broken.");
		out.println("Spent ~ " + ((float) spentTime / (float) createdRecords) + " milliseconds per record.");
	}
	
	private boolean processRecord(Graph graph, GraphNode node, Resource record) {
		BigInteger resourceType = record.getResourceType();
		if (null == resourceType) 
			return false; // no record type
		
		// Record myst have valid type
		int type = resourceType.intValue();
		if (1 == type || 2 == type) 
			node.setType(GraphUtils.TYPE_DATASET);
		else if (3 == type)
			node.setType(GraphUtils.TYPE_PUBLICATION);
		else 
			return false;
		
		ResourceIdentifier identifier = record.getResourceIdentifier();
		if (null != identifier)
			node.addProperty(GraphUtils.PROPERTY_DARA_ID, identifier.getIdentifier());

		// Record myst have a title
		Titles titles = record.getTitles();
		if (null != titles) {
			for (Title title : titles.getTitle()) {
				String language = title.getLanguage();
				if (null == language || language.toLowerCase().equals(LANG_EN))
					node.addProperty(GraphUtils.PROPERTY_TITLE, title.getTitleName());
			}
		}
		
		if (!node.hasProperty(GraphUtils.PROPERTY_TITLE)) 
			return false;
		
		Creators creators = record.getCreators();
		if (null != creators) {
			for (Creator creator : creators.getCreator()) {
				Person person = creator.getPerson(); 
				if (null != person) {
					String fullName = null;
					if (StringUtils.isNotEmpty(person.getFirstName()))
						fullName = person.getFirstName();
					if (StringUtils.isNotEmpty(person.getMiddleName())) {
						if (null != fullName) 
							fullName += ' ' + person.getMiddleName();
						else
							fullName = person.getMiddleName();
					}
					if (StringUtils.isNotEmpty(person.getLastName())) {
						if (null != fullName) 
							fullName += ' ' + person.getLastName();
						else
							fullName = person.getLastName();
					}
					
					node.addProperty(GraphUtils.PROPERTY_AUTHORS, fullName);
				}
			}
		}
		
		String doi = GraphUtils.extractDoi(record.getDoiProposal());
		if (null != doi) {
			node.addProperty(GraphUtils.PROPERTY_DOI, doi);
			node.addProperty(GraphUtils.PROPERTY_URL, GraphUtils.generateDoiUri(doi));
		}
		
		PublicationDate date = record.getPublicationDate();
		if (null != date) {
			XMLGregorianCalendar d = date.getDate();
			if (null != d)
				node.addProperty(GraphUtils.PROPERTY_PUBLISHED_DATE, d.toString());
			else if (null != (d = date.getMonthyear()))
				node.addProperty(GraphUtils.PROPERTY_PUBLISHED_DATE, d.toString());
			else if (null != (d = date.getDate()))
				node.addProperty(GraphUtils.PROPERTY_PUBLISHED_DATE, d.toString());
		}
		
		Relations relations = record.getRelations();
		if (null != relations) {
			for (Relation relation : relations.getRelation()) {
				String identifierType = relation.getIdentifierType();
				if (TYPE_DOI.equals(identifierType)) {
					doi = GraphUtils.extractDoi(relation.getIdentifier());
					
					node.addProperty(GraphUtils.PROPERTY_REFERENCED_BY, doi);
				}
			}
		}
		
		return true;
	}	
}
