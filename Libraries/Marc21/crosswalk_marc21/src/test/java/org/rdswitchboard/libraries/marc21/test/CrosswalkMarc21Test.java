package org.rdswitchboard.libraries.marc21.test;

import static org.junit.Assert.*;

import java.io.FileInputStream;
import java.io.InputStream;

import javax.xml.bind.JAXBException;

import org.junit.BeforeClass;
import org.junit.Test;
import org.rdswitchboard.libraries.graph.Graph;
import org.rdswitchboard.libraries.graph.GraphNode;
import org.rdswitchboard.libraries.graph.GraphUtils;
import org.rdswitchboard.libraries.marc21.CrosswalkMarc21;

public class CrosswalkMarc21Test {
	
	private static CrosswalkMarc21 crosswalk;

	@BeforeClass
	public static void method() {
		try {
			crosswalk = new CrosswalkMarc21();
			crosswalk.setSource("Test");
			//graph = crosswalk.process(new FileInputStream());
			
		} catch (JAXBException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testCrosswalk() {
		
		assertNotNull("Test file missing", 
				getClass().getResource("/cern.xml"));
		
		assertNotNull("Macr21 Crosswalk do not exists", 
				crosswalk);

		Graph graph = null;
		try {
			try (InputStream is = getClass().getResourceAsStream("/cern.xml")) {
				graph = crosswalk.process(is);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		assertNotNull("XML File can not be parsed", 
				graph);
		
		assertEquals("XML File should contain 71 records", 
				71,
				graph.getNodes().size());
		
		GraphNode node = graph.getNodes().iterator().next();
		
		assertNotNull("Graph Node do not exists", 
				node);
		
		assertEquals("First record key do not equal to 'oai:cds.cern.ch:748205'", 
				"oai:cds.cern.ch:748205",
				node.getKey().getValue());
		
		assertEquals("First record title do not equal to 'The study on electromagnetic field of an RFQ'", 
				"The study on electromagnetic field of an RFQ",
				node.getProperty(GraphUtils.PROPERTY_TITLE));
	}
}
