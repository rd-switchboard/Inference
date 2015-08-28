package org.rdswitchboard.libraries.marc21;

import gov.loc.marc21.slim.DataFieldType;
import gov.loc.marc21.slim.RecordType;
import gov.loc.marc21.slim.SubfieldatafieldType;

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
import org.rdswitchboard.libraries.graph.GraphSchema;
import org.rdswitchboard.libraries.graph.GraphUtils;
import org.rdswitchboard.libraries.graph.interfaces.GraphCrosswalk;

public class CrosswalkMarc21 implements GraphCrosswalk {
	private static final String TYPE_INSPIRE = "Inspire";
	
	private static final String COLLECTION_ARTICLE = "ARTICLE";
	private static final String COLLECTION_BOOK = "BOOK";
	
/*	private static final String MD_TYPE_MODS = "MODS";
	
	//private static final String GENRE_DATASET = "dataset";
	//private static final String GENRE_ARTICLE = "article";
		
	private static final String NODE_GENRE = "genre";
	private static final String NODE_IDENTIFIER = "identifier";
	private static final String NODE_TITLE_INFO = "titleInfo";
	private static final String NODE_NAME = "name";
	private static final String NODE_ROLE = "role";	
	private static final String NODE_ROLE_TERM = "roleTerm";
	private static final String NODE_NAME_PART = "namePart";
	private static final String NODE_RELATED_ITEM = "relatedItem";
	
	private static final String ATTRIBUTE_TYPE = "type";
	
	private static final String PART_DOI = "doi:";
	private static final String PART_PURL = "purl.org";
	private static final String PART_DELEMITER = " ";
	
	private static final String IDENIFIER_URI = "uri";
	
	private static final String ROLE_AUTHOR = "author";
	
	private static final String RELATION_HOST = "host";
	private static final String RELATION_CONSTITUENT = "constituent";
	private static final String RELATION_IS_REFERENCED_BY = "isReferencedBy";*/
	
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
	public CrosswalkMarc21( ) throws JAXBException {
		// configure unmarshaller
		unmarshaller = JAXBContext.newInstance( "org.openarchives.oai._2:gov.loc.marc21.slim" ).createUnmarshaller();
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
		graph.addSchema(new GraphSchema(source, GraphUtils.PROPERTY_INSPIRE_ID, false));
		graph.addSchema(new GraphSchema(source, GraphUtils.PROPERTY_URL, false));
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
							.withSource(source)
							.withType(GraphUtils.TYPE_PUBLICATION);
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
								if (processMarc21(graph, node, ((JAXBElement<RecordType>) metadata).getValue())) { 
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
	
	/**
	 * Function will marks node as deleted
	 * @param node
	 */
	/*private void setDeleted(GraphNode node) {
		if (!node.isDeleted()) {
			node.setDeleted(true);
			++deletedRecords;
		}
	}*/
	
	/**
	 * Function will marks node as broken
	 * @param node
	 */
	/*private void setBroken(GraphNode node) {
		if (!node.isBroken()) {
			node.setBroken(true);
			++brokenRecords;
		}
	}*/
	
	/**
	 * Function will process Marc21 Object
	 * @param graph
	 * @param node
	 * @param mets
	 * @return true if object has been processed
	 */
	private boolean processMarc21(Graph graph, GraphNode node, RecordType record) {
		List<DataFieldType> fields = record.getDatafield(); 
		if (null == fields || fields.isEmpty())
			return false;
				
		boolean isArcticle = false;
		
		for (DataFieldType field : fields) {
			String tag = field.getTag();
			if (tag.equals("024")) {
				if (field.getInd1().equals("7"))
					node.addProperty(GraphUtils.PROPERTY_DOI, GraphUtils.extractDoi(extractSubField(field, "a")));
			} else if (tag.equals("035")) {
				String id = extractSubField(field, "a");
				if (StringUtils.isNotBlank(id)) {
					String type = extractSubField(field, "9");
					if (null != type && type.equals(TYPE_INSPIRE)) {
						node.addProperty(GraphUtils.PROPERTY_INSPIRE_ID, id);
						node.addProperty(GraphUtils.PROPERTY_URL, GraphUtils.generateInspireUrl(id));
					}
				}
			} else if (tag.equals("037")) 
				node.addProperty(GraphUtils.PROPERTY_LOCAL_ID, extractSubField(field, "a"));
			else if (tag.equals("100"))  // Author, first  
				node.addProperty(GraphUtils.PROPERTY_AUTHORS, extractSubField(field, "a"));
			else if (tag.equals("700"))  // Author(s), additional
				node.addProperty(GraphUtils.PROPERTY_AUTHORS, extractSubField(field, "a"));
			else if (tag.equals("245"))  // Title
				node.addProperty(GraphUtils.PROPERTY_TITLE, extractSubField(field, "a"));
			else if (tag.equals("980"))  {// Collection identifier  
				String type = extractSubField(field, "a");
				if (null != type && (type.equals(COLLECTION_ARTICLE) || type.equals(COLLECTION_BOOK)))
					isArcticle = true;
				//node.addProperty("collection_id", );
			}
		}
		
		return isArcticle;
	}

	/*private boolean tag(DataFieldType field, String tag, String ind1, String ind2) {
		return tag.equals(field.getTag()) 
				&& (null == ind1 || ind1.equals(field.getInd1()))
				&& (null == ind2 || ind2.equals(field.getInd2()));
	}*/
	
	private String extractSubField(DataFieldType field, String code) {
		for (SubfieldatafieldType subfield : field.getSubfield()) 
			if (code.equals(subfield.getCode())) 
				return subfield.getValue();
			
		return null;
	}
	
	/*
	private Set<String> extractControlField(gov.loc.marc21.slim.RecordType record, String tag) {
		Set<String> values = null;
		for (ControlFieldType field : record.getControlfield()) {
			if (tag.equals(field.getTag())) {
				if (null == values)
					values = new HashSet<String>();
				
				values.add(field.getValue());
			}
		}
			
		return values;
	}*/
	
}
