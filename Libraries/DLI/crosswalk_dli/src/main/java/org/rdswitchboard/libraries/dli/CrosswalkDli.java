package org.rdswitchboard.libraries.dli;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.lang.StringUtils;
import org.openarchives.oai._2.HeaderType;
import org.openarchives.oai._2.ListRecordsType;
import org.openarchives.oai._2.OAIPMHtype;
import org.openarchives.oai._2.StatusType;
import org.rdswitchboard.libraries.graph.Graph;
import org.rdswitchboard.libraries.graph.GraphKey;
import org.rdswitchboard.libraries.graph.GraphNode;
import org.rdswitchboard.libraries.graph.GraphRelationship;
import org.rdswitchboard.libraries.graph.GraphSchema;
import org.rdswitchboard.libraries.graph.GraphUtils;
import org.rdswitchboard.libraries.graph.interfaces.GraphCrosswalk;

import eu.dli.AuthorType;
import eu.dli.DliObjectType;
import eu.dli.Identifier;
import eu.dli.ObjectType;
import eu.dli.RelationType;

public class CrosswalkDli implements GraphCrosswalk {
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
	
	//http://dliservice.research-infrastructures.eu/res/DLIFMetadataFormat.xsd
	public CrosswalkDli( ) throws JAXBException {
		// configure unmarshaller
		unmarshaller = JAXBContext.newInstance( "org.openarchives.oai._2:eu.dli" ).createUnmarshaller();
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
//							.withType(GraphUtils.TYPE_PUBLICATION);
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
							// check if metadata is in Mets format
							if (null != metadata && metadata instanceof JAXBElement)  {
								if (processDliObject(graph, node, ((JAXBElement<DliObjectType>) metadata).getValue())) { 
									graph.addNode(node);
									++createdRecords;
								}
							}
						} 
					}
				} 
			} else 
				throw new Exception("No Records has been detected in the OAI:PMH document");
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
	
	private boolean processDliObject(Graph graph, GraphNode node, DliObjectType record) {
		ObjectType objectType = record.getObjectType(); 
		if (ObjectType.DATASET.equals(objectType)) 
			node.setType(GraphUtils.TYPE_DATASET);
		else if (ObjectType.PUBLICATION.equals(objectType))
			node.setType(GraphUtils.TYPE_PUBLICATION);
		else
			return false;

		node.addProperty(GraphUtils.PROPERTY_LOCAL_ID, record.getDnetResourceIdentifier());
		node.addProperty(GraphUtils.PROPERTY_TITLE, record.getTitle());
		node.addProperty(GraphUtils.PROPERTY_PUBLISHED_DATE, record.getDate());
		
		Identifier identifier = record.getOriginalIdentifier();
		if (null != identifier && TYPE_DOI.equals(identifier.getType())) {
			String doi = GraphUtils.extractDoi(identifier.getValue());
			if (null != doi) {
				node.addProperty(GraphUtils.PROPERTY_DOI, doi);
				node.addProperty(GraphUtils.PROPERTY_URL, GraphUtils.generateDoiUri(doi));
			}
		}
		
		DliObjectType.Authors authors = record.getAuthors();
		if (null != authors) {
			List<AuthorType> list = authors.getAuthor();
			if (null != list) 
				for (AuthorType author : list) 
					node.addProperty(GraphUtils.PROPERTY_AUTHORS, author.getFullname());
		}
		
		DliObjectType.Relations relations = record.getRelations();
		if (null != relations) {
			List<RelationType> list = relations.getRelation();
			if (null != list)
				for (RelationType relation : list) {
					String relType = relation.getTypeOfRelation();
					if (null == relType)
						relType = GraphUtils.RELATIONSHIP_RELATED_TO;
					
					Identifier pid = relation.getPid();
					if (null != pid) {
						String idType = pid.getType();
						if (idType.equals(TYPE_DOI)) {
							String doi = GraphUtils.extractDoi(pid.getValue());
							if (null != doi) {
								++createdRelationships;
								
								graph.addRelationship(new GraphRelationship()
										.withRelationship(relType)
										.withStart(node.getKey())
										.withEnd(node.getKey().getIndex(), GraphUtils.PROPERTY_DOI, doi));
							}
 						} /*also avaliable handle, url*/						
					}
				}
		}
		
		return true;
	}
}
