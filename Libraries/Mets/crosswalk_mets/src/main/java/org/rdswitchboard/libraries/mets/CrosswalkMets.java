package org.rdswitchboard.libraries.mets;

import gov.loc.mets.MdSecType;
import gov.loc.mets.Mets;
import gov.loc.mets.MdSecType.MdWrap;
import gov.loc.mets.MdSecType.MdWrap.XmlData;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
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
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Mets importing library. Used by Dryad importer software
 * 
 * @author Dmitrij Kudriavcev, dmitrij@kudriavcev.info
 *
 */
public class CrosswalkMets implements GraphCrosswalk {
	private static final String MD_TYPE_MODS = "MODS";
	
	//private static final String GENRE_DATASET = "dataset";
	//private static final String GENRE_ARTICLE = "article";
		
	//private static final String NODE_GENRE = "genre";
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
	private static final String RELATION_IS_REFERENCED_BY = "isReferencedBy";
	
	private Unmarshaller unmarshaller;
	
	private long processedFiles = 0;
	private long deletedRecords = 0;
	private long brokenRecords = 0;
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
	public CrosswalkMets( ) throws JAXBException {
		// configure unmarshaller
		unmarshaller = JAXBContext.newInstance( "org.openarchives.oai._2:gov.loc.mets" ).createUnmarshaller();
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
	public long getBrokenRecords() {
		return brokenRecords;
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
		createdRecords = deletedRecords = brokenRecords = createdRelationships = processedFiles = markTime = 0;
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
		graph.addSchema(new GraphSchema(source, GraphUtils.PROPERTY_PURL, false));
		graph.addSchema(new GraphSchema(source, GraphUtils.PROPERTY_URL, false));

		// extract root object
		if (element.getValue() instanceof OAIPMHtype) {
			OAIPMHtype root = (OAIPMHtype) element.getValue();
			// extract all records
			ListRecordsType records = root.getListRecords();
			// check if file has some records
			if (null != records &&  null != records.getRecord()) {
				// process all records
				for (RecordType record : records.getRecord()) {
					// extract record header
					HeaderType header = record.getHeader();
					
					// extract record identifier
					String idetifier = header.getIdentifier();
					if (verbose)
						System.out.println("Record: " + idetifier.toString());
					if (StringUtils.isNotBlank(idetifier)) {
						// String oai = GraphUtils.extractOai(idetifier);
				
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
							// save deleted nodes so we can delete them from the database
							graph.addNode(node.withDeleted(true));		
							++deletedRecords;
							++createdRecords;
						} else if (null != record.getMetadata()) {
							// we expect only one metadata object per record
							Object metadata = record.getMetadata().getAny();
							// check if metadata is in Mets format
							if (null != metadata && metadata instanceof Mets 
									&& processMets(graph, node, (Mets) metadata)) {
								
								// save only nodes who have passed metadata valiadtion
								graph.addNode(node);
								++createdRecords;
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
		out.println( String.format("Processed %d files.\nSpent %d millisecods.\nFound %d records.Found %d relationships.\nFound %d deleted records.\nFound %d broken records.\nSpent ~ %f milliseconds per record.", 
				processedFiles, spentTime, createdRecords, createdRelationships, deletedRecords, brokenRecords, (float) spentTime / (float) createdRecords));
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
	 * Function will process Mets Object
	 * @param graph
	 * @param node
	 * @param mets
	 * @return true if object has been processed
	 */
	private boolean processMets(Graph graph, GraphNode node, Mets mets) {
		// we expect each mets object to have only one dmdSpec object
		if (mets.getDmdSec().size() == 1) {
			// extrcat smdSpec object 
			return processDmdSec(graph, node, mets.getDmdSec().get(0));
		}
		
		return false;
	}
	
	/**
	 * Function will process mets:dmdSec object
	 * @param graph
	 * @param node
	 * @param dmdSec
	 * @return true if object has been processed
	 */
	private boolean processDmdSec(Graph graph, GraphNode node, MdSecType dmdSec) {
		if (null != dmdSec) {
			// extract mdWarp object
			MdWrap mdWrap = dmdSec.getMdWrap();
			// we expect mdWrap to have MODS type
			if (null != mdWrap && mdWrap.getMDTYPE().equals(MD_TYPE_MODS)) 
				return processXmlData(graph, node, mdWrap.getXmlData());
		}
		
		return false;
	} 
	
	/**
	 * Function will process mets:xmlData object
	 * @param graph
	 * @param node
	 * @param xmlData
	 * @return true if object has been processed
	 */
	private boolean processXmlData(Graph graph, GraphNode node, XmlData xmlData) {
		if (null != xmlData) {
			List<Object> xmlObjects = xmlData.getAny();

	//		Element genre = findXmlElement(xmlObjects, NODE_GENRE);
/*			if (null != genre) {
				String nodeType = null;
				String genreString = genre.getTextContent().toLowerCase();
				if (genreString.equals(GENRE_DATASET))
					nodeType = GraphUtils.TYPE_DATASET;
				else if (genreString.equals(GENRE_ARTICLE))
					nodeType = GraphUtils.TYPE_PUBLICATION;

				if (null != nodeType) {
					node.setType(nodeType);*/
					
					processIdentifiers(node, findXmlElements(xmlObjects, NODE_IDENTIFIER));
					processTitle(node, findXmlElement(xmlObjects, NODE_TITLE_INFO));
					processNames(node, findXmlElements(xmlObjects, NODE_NAME));
					processRelatedItems(graph, node,  
							findXmlElements(xmlObjects, NODE_RELATED_ITEM));
					
//					if (node.hasProperty(GraphUtils.PROPERTY_REFERENCED_BY)) 
					return true;
/*				}
			}*/ 
		} 
		
		// if we have reach this place, the record must be broken
		return false;
	}
	
	/**
	 * Function will process mods:identifier objects list
	 * @param node
	 * @param identifiers
	 */
	private void processIdentifiers(GraphNode node, List<Element> identifiers) {
		if (null != identifiers)
			for (Element identifier : identifiers) {
				String type = identifier.getAttribute(ATTRIBUTE_TYPE);
				String identifierString = identifier.getTextContent();
				if (null != identifierString && ! identifierString.isEmpty()) {
					if (identifierString.contains(PART_DOI)) {
						String doi = GraphUtils.extractDoi(identifierString);
						if (StringUtils.isNotEmpty(doi)) {
							node.addIndex(source, GraphUtils.PROPERTY_DOI, doi);
							//node.addProperty(GraphUtils.PROPERTY_DOI, doi);
						}
					} else if (identifierString.contains(PART_PURL)) {
						String purl = GraphUtils.extractFormalizedUrl(identifierString);
						if (StringUtils.isNotEmpty(purl))
							node.addProperty(GraphUtils.PROPERTY_PURL, purl);		 
					} else if (null != type && type.equals(IDENIFIER_URI)) {
						String url = GraphUtils.extractFormalizedUrl(identifierString);
						if (StringUtils.isNotEmpty(url))
							node.addProperty(GraphUtils.PROPERTY_URL, url);
					}
				}														
			}
	}
	
	/**
	 * Function will process mods:title object
	 * @param node
	 * @param title
	 */
	private void processTitle(GraphNode node, Element title) {
		if (null != title) {
			String titleString = title.getTextContent();
			if (StringUtils.isNotEmpty(titleString))
				node.addProperty(GraphUtils.PROPERTY_TITLE, titleString);
		}
	}

	/**
	 * Function will process mods:name objects list
	 * @param node
	 * @param names
	 */
	private void processNames(GraphNode node, List<Element> names) {
		if (null != names) 
			for (Element name : names) {
				String roleString = null;
				Element role = findXmlElement(name.getChildNodes(), NODE_ROLE);
				if (null != role) {
					Element roleTerm = findXmlElement(role.getChildNodes(), NODE_ROLE_TERM);
					if (null != roleTerm)
						roleString = roleTerm.getTextContent();																
				}
	
				if (null != roleString && roleString.equals(ROLE_AUTHOR)) {
					String nameString = null;
				
					List<Element> nameParts = findXmlElements(name.getChildNodes(), NODE_NAME_PART);
					if (null != nameParts) 
						for (Element namePart : nameParts) {
							if (null == nameString)
								nameString = namePart.getTextContent();
							else 
								nameString += PART_DELEMITER + namePart.getTextContent();
							
						}
						
					if (StringUtils.isNotBlank(nameString))
						node.addProperty(GraphUtils.PROPERTY_AUTHORS, nameString);
				}							
			}
	}
	
	/**
	 * Function will create relationsips based on relatedItem objects list
	 * @param graph
	 * @param source
	 * @param key
	 * @param relatedItems
	 */
	private void processRelatedItems(Graph graph, GraphNode node, List<Element> relatedItems) {
		GraphKey start = node.getKey();
		if (null == start)
			throw new IllegalArgumentException("The Node does not have a Key");
		if (null != relatedItems)
			for (Element relatedItem : relatedItems) {
				String relation = relatedItem.getTextContent();
				String relationType = relatedItem.getAttribute(ATTRIBUTE_TYPE);
				if (StringUtils.isNotEmpty(relationType)) {
					if (relationType.equals(RELATION_HOST) 
							|| relationType.equals(RELATION_CONSTITUENT)) {
						if (relation.contains(PART_DOI)) {
							String doi = GraphUtils.extractDoi(relation);
							if (null != doi) {
								graph.addRelationship(new GraphRelationship()
									.withRelationship(relationType)
									.withStart(start)
									.withEnd(new GraphKey(start.getIndex(), GraphUtils.PROPERTY_DOI, doi)));
							} else 
								node.addProperty("invalid_doi", doi);
						}
							
						++createdRelationships;
					} else if (relationType.equals(RELATION_IS_REFERENCED_BY)) {
						node.addProperty(GraphUtils.PROPERTY_REFERENCED_BY, relation);					
					}
				}
			}
	}
	
	/**
	 * Function will extract single XML element with specifed tag name from list of XML objects
	 * @param xmls
	 * @param name
	 * @return Element
	 */
	private Element findXmlElement(List<Object> xmls, String name) {
		for (Object xml : xmls) 
			if (xml instanceof Element && ((Element) xml).getLocalName().equals(name))
				return (Element) xml;

		return null;
	}
	
	/**
	 * Function will extract single XML element with specifed tag name from NodeList object
	 * @param xmls
	 * @param name
	 * @return
	 */
	private static Element findXmlElement(NodeList xmls, String name) {
		if (null != xmls) {
			int length = xmls.getLength();
			for (int i = 0; i < length; ++i) {
				Node xml = xmls.item(i);
				if (xml instanceof Element && ((Element) xml).getLocalName().equals(name))
					return (Element) xml;
			}
		}

		return null;
	}	
	
	/**
	 * Function will extract all XML elements with specifed tag name from list of XML objects
	 * @param xmls
	 * @param name
	 * @return
	 */
	private static List<Element> findXmlElements(List<Object> xmls, String name) {
		List<Element> list = null;		
		for (Object xml : xmls) 
			if (xml instanceof Element && ((Element) xml).getLocalName().equals(name)) {
				if (null == list)
					list = new ArrayList<Element>();
				
				list.add((Element) xml);
			}

		return list;
	}
	
	/**
	 * Function will extract all XML elements with specifed tag name from NodeList object
	 * @param xmls
	 * @param name
	 * @return
	 */
	private static List<Element> findXmlElements(NodeList xmls, String name) {
		List<Element> list = null;		
		
		int length = xmls.getLength();
		for (int i = 0; i < length; ++i) {
			Node xml = xmls.item(i);
			if (xml instanceof Element && ((Element) xml).getLocalName().equals(name)) {
				if (null == list)
					list = new ArrayList<Element>();
				
				list.add((Element) xml);
			}
		}

		return list;
	}	
}
