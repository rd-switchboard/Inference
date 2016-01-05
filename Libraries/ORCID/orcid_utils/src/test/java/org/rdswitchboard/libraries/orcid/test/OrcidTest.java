package org.rdswitchboard.libraries.orcid.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

import javax.xml.bind.JAXBException;

import org.junit.BeforeClass;
import org.junit.Test;
import org.rdswitchboard.libraries.orcid.Orcid;
import org.rdswitchboard.libraries.orcid.OrcidMessage;


public class OrcidTest {
	
	private static final String TEST_FOLDER = "tests";

	private static Orcid orcid;
	
	@BeforeClass
	public static void init() {
		orcid = new Orcid();
	}
	
	@Test
	public void testOrcid() {
		URL testUrl = ClassLoader.getSystemResource(TEST_FOLDER);
		
		assertNotNull("Test resource missing", testUrl);
		
		try {
			File testFolder = new File(testUrl.toURI());
			
			assertNotNull("Test folder missing", testFolder);
			assertTrue("Test folder is not a folder", testFolder.isDirectory());

			for (File testFile : testFolder.listFiles()) 
				if (testFile.isFile()) 
				{
				//	System.out.println("Testing: " + testFile.getAbsolutePath());
					
					OrcidMessage message = orcid.parseJson(testFile);
										
					assertNotNull("File can not be parsed", message);
				}
		} catch (Exception e) {
			e.printStackTrace();
			
			fail(e.getMessage());
		}
	}
}
