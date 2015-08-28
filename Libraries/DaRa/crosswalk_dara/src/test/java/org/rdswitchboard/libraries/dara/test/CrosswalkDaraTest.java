package org.rdswitchboard.libraries.dara.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.InputStream;

import javax.xml.bind.JAXBException;

import org.junit.BeforeClass;
import org.junit.Test;
import org.rdswitchboard.libraries.dara.CrosswalkDara;
import org.rdswitchboard.libraries.graph.Graph;
import org.rdswitchboard.libraries.graph.GraphNode;
import org.rdswitchboard.libraries.graph.GraphUtils;

public class CrosswalkDaraTest {
	private static final String TEST_FILE = "/dara.xml";
	private static final int RECORDS_COUNT = 45;
	private static final String RECORD_KEY = "oai:oai.da-ra.de:9498";
	private static final String RECORD_TITLE = "Poverty in a State of Wealth, Social Disparities in the City-State of Singapore";
	private static final String RECORD_DOI = "10.4232/10.ASEAS-1.2-6";
	
	private static CrosswalkDara crosswalk;

	@BeforeClass
	public static void method() {
		try {
			crosswalk = new CrosswalkDara();
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
		
		assertNotNull("Dara Crosswalk do not exists", 
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
