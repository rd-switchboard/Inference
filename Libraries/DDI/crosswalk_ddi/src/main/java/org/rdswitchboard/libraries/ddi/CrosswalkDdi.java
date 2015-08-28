package org.rdswitchboard.libraries.ddi;

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

import ddi.instance._3_1.DDIInstanceType;
import ddi.reusable._3_1.CitationType;
import ddi.reusable._3_1.ContributorType;
import ddi.reusable._3_1.CreatorType;
import ddi.reusable._3_1.DateType;
import ddi.reusable._3_1.IdentifiedStructuredStringType;
import ddi.reusable._3_1.InternationalStringType;
import ddi.reusable._3_1.TypedStringType;
import ddi.studyunit._3_1.StudyUnitType;

public class CrosswalkDdi implements GraphCrosswalk {

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
	
	public CrosswalkDdi() throws JAXBException {
		// configure unmarshaller
		unmarshaller = JAXBContext.newInstance( "org.openarchives.oai._2:ddi.archive._3_1:ddi.comparative._3_1:ddi.conceptualcomponent._3_1:ddi.datacollection._3_1:ddi.dataset._3_1:ddi.dcelements._3_1:ddi.ddiprofile._3_1:ddi.group._3_1:ddi.instance._3_1:ddi.logicalproduct._3_1:ddi.physicaldataproduct_ncube_inline._3_1:ddi.physicaldataproduct_ncube_normal._3_1:ddi.physicaldataproduct_ncube_tabular._3_1:ddi.physicaldataproduct_proprietary._3_1:ddi.physicaldataproduct._3_1:ddi.physicalinstance._3_1:ddi.reusable._3_1:ddi.studyunit._3_1:org.purl.dc.elements._1:org.w3._1999.xhtml" ).createUnmarshaller();
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
							.withSource(source)
							.withType(GraphUtils.TYPE_DATASET);
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
								if (processRecord(graph, node, ((JAXBElement<DDIInstanceType>) metadata).getValue())) { 
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
	

	private boolean processRecord(Graph graph, GraphNode node, DDIInstanceType record) {
		List<StudyUnitType> studyUnits = record.getStudyUnit();
		if (null != studyUnits) 
			for (StudyUnitType studyUnit : studyUnits) {
				node.addProperty(GraphUtils.PROPERTY_DARA_ID, studyUnit.getId());
				
				CitationType citation = studyUnit.getCitation();
				if (null != citation) {
					List<InternationalStringType> titles = citation.getTitle();
					if (null != titles) for (InternationalStringType title : titles) {
						String lang = title.getLang();
						if (null == lang || lang.toLowerCase().equals(LANG_EN))
							node.addProperty(GraphUtils.PROPERTY_TITLE, title.getValue());
					}
					
					List<CreatorType> creators = citation.getCreator();
					if (null != creators) for (CreatorType creator : creators) {
						node.addProperty(GraphUtils.PROPERTY_AUTHORS, creator.getValue());
					}
					
					/*List<ContributorType> contributors = citation.getContributor();
					if (null != contributors) for (ContributorType contributor : contributors) {
						node.addProperty(GraphUtils.PROPERTY_AUTHORS, contributor.getValue());
					}*/
					
					DateType publicationDate = citation.getPublicationDate();
					if (null != publicationDate) {
						node.addProperty(GraphUtils.PROPERTY_PUBLISHED_DATE, publicationDate.getSimpleDate());
					}
					
					List<TypedStringType> indetifiers = citation.getInternationalIdentifier();
					if (null != indetifiers) for (TypedStringType indetifier : indetifiers) {
						String type = indetifier.getType();
						if (null != type && type.toLowerCase().equals(TYPE_DOI))
							node.addProperty(GraphUtils.PROPERTY_DOI, GraphUtils.extractDoi(indetifier.getValue()));
					}
					
					//citation.g
				}				
			}		
		
		return node.hasProperty(GraphUtils.PROPERTY_TITLE) && node.hasProperty(GraphUtils.PROPERTY_TYPE);
	}	
}
