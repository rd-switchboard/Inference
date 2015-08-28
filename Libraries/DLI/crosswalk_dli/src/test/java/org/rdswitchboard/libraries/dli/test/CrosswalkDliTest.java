package org.rdswitchboard.libraries.dli.test;

import static org.junit.Assert.*;

import java.io.InputStream;

import javax.xml.bind.JAXBException;

import org.junit.BeforeClass;
import org.junit.Test;
import org.rdswitchboard.libraries.dli.CrosswalkDli;
import org.rdswitchboard.libraries.graph.Graph;
import org.rdswitchboard.libraries.graph.GraphNode;
import org.rdswitchboard.libraries.graph.GraphUtils;

public class CrosswalkDliTest {

	private static final int RECORDS_COUNT = 93;
	private static final String RECORD_KEY = "oai:dnet:00041f4d1865b3577e09deab777c9cdb";
	private static final String RECORD_TITLE = "CCDC 634344: Experimental Crystal Structure Determination";
	private static final String RECORD_DOI = "10.5517/ccp92qr";
	
	private static CrosswalkDli crosswalk;

	@BeforeClass
	public static void method() {
		try {
			crosswalk = new CrosswalkDli();
			crosswalk.setSource("Test");
			//graph = crosswalk.process(new FileInputStream());
			
		} catch (JAXBException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testCrosswalk() {
		
		assertNotNull("Test file missing", 
				getClass().getResource("/dli.xml"));
		
		assertNotNull("Macr21 Crosswalk do not exists", 
				crosswalk);

		Graph graph = null;
		try {
			try (InputStream is = getClass().getResourceAsStream("/dli.xml")) {
				graph = crosswalk.process(is);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		assertNotNull("XML File can not be parsed", 
				graph);
		
		assertEquals("XML File should contain " +RECORDS_COUNT + " records", 
				RECORDS_COUNT,
				graph.getNodesCount());
		
		GraphNode node = graph.getNodes().iterator().next();
		
		assertNotNull("Graph Node do not exists", 
				node);
		
		assertEquals("First record key do not equal to '" + RECORD_KEY + "'", 
				RECORD_KEY,
				node.getKey().getValue());
		
		assertEquals("First record title do not equal to '" + RECORD_TITLE + "'", 
				RECORD_TITLE,
				node.getProperty(GraphUtils.PROPERTY_TITLE));

		assertEquals("First record DOI do not equal to '" + RECORD_DOI + "'", 
				RECORD_DOI,
				node.getProperty(GraphUtils.PROPERTY_DOI));

	}
}
