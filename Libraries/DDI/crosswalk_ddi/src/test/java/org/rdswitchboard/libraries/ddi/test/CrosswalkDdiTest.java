package org.rdswitchboard.libraries.ddi.test;

import static org.junit.Assert.*;

import java.io.InputStream;

import javax.xml.bind.JAXBException;

import org.junit.BeforeClass;
import org.junit.Test;
import org.rdswitchboard.libraries.ddi.CrosswalkDdi;
import org.rdswitchboard.libraries.graph.Graph;
import org.rdswitchboard.libraries.graph.GraphNode;
import org.rdswitchboard.libraries.graph.GraphUtils;

public class CrosswalkDdiTest {
	private static final String TEST_FILE = "/ddi.xml";
	private static final int RECORDS_COUNT = 1;
	private static final String RECORD_KEY = "oai:oai.da-ra.de:7237";
	private static final String RECORD_TITLE = "Demographic change and the public sectors labor market SFB580-B8";
	private static final String RECORD_DOI = "10.7478/s0044.1.v1";
	
	private static CrosswalkDdi crosswalk;

	@BeforeClass
	public static void method() {
		try {
			crosswalk = new CrosswalkDdi();
			crosswalk.setSource("Test");
			//graph = crosswalk.process(new FileInputStream());
			
		} catch (JAXBException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testCrosswalk() {
		
		assertNotNull("Test file missing", 
				getClass().getResource(TEST_FILE));
		
		assertNotNull("Macr21 Crosswalk do not exists", 
				crosswalk);

		Graph graph = null;
		try {
			try (InputStream is = getClass().getResourceAsStream(TEST_FILE)) {
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
