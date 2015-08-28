/**
 * 
 */
package org.rdswitchboard.libraries.graph.test;

import static org.junit.Assert.*;

import org.junit.Test;
import org.rdswitchboard.libraries.graph.GraphUtils;

/**
 * @author dima
 *
 */
public class GraphUtilsTest {

	@Test
	public void urlTest() {
		assertEquals(
				"Should be able to extract an URL", 
				"http://www.scopus.com/inward/authorDetails.url?authorID=26538571500&partnerID=MN8TOARS", 
				GraphUtils.extractUrl("http://www.scopus.com/inward/authorDetails.url?authorID=26538571500&partnerID=MN8TOARS"));
	
		assertNull(
				"Should inore mailformed URL's", 
				GraphUtils.extractUrl("<a href=\"http://www.scopus.com/inward/authorDetails.url?authorID=26538571500&partnerID=MN8TOARS\">"));

		assertEquals(
				"Should be able to extract a Formalized URL", 
				"scopus.com/inward/authorDetails.url?authorID=26538571500&partnerID=MN8TOARS", 
				GraphUtils.extractFormalizedUrl("http://www.scopus.com/inward/authorDetails.url?authorID=26538571500&partnerID=MN8TOARS#MyAncor"));
	}

	@Test
	public void orcidTest() {
		assertEquals("Should be able to extract ORCID ID", 
				"0000-0003-0846-3352", 
				GraphUtils.extractOrcidId("http://orcid.org/0000-0003-0846-3352"));

		assertEquals("Should be able to extract ORCID ID with X at the end", 
				"0000-0003-0846-335X", 
				GraphUtils.extractOrcidId("http://orcid.org/0000-0003-0846-335X"));

		assertNull("Should ignore mailformed ORCID ID", 
				GraphUtils.extractOrcidId("http://orcid.org/0000-0003-0X46-335X"));

		assertNull("Should ignore truncated ORCID ID", 
				GraphUtils.extractOrcidId("http://orcid.org/0000"));

		assertNull("Should ignore string what is not an ORCID ID", 
				GraphUtils.extractOrcidId("KJHHJKJHKHKJHKJHKJHKJHKJH"));
		
		assertEquals("Should be able to generate ORCID URL", 
				"orcid.org/0000-0003-0846-3352", 
				GraphUtils.generateOrcidUri("0000-0003-0846-3352"));
	}

	@Test
	public void doiTest() {
		assertEquals("Should be able to recognize DOI", 
				"10.4049/jimmunol.1101206", 
				GraphUtils.extractDoi("10.4049/jimmunol.1101206"));

		assertEquals("Should be able to extract DOI", 
				"10.4049/jimmunol.1101206", 
				GraphUtils.extractDoi("doi:10.4049/jimmunol.1101206"));

		assertEquals("Should be able to extract DOI from URL (dx.doi.org)", 
				"10.4049/jimmunol.1101206", 
				GraphUtils.extractDoi("http://dx.doi.org/10.4049/jimmunol.1101206"));

		assertEquals("Should be able to extract DOI from URL (journals.aps.org)", 
				"10.1103/PhysRevB.90.104106", 
				GraphUtils.extractDoi("http://journals.aps.org/prb/abstract/10.1103/PhysRevB.90.104106"));
		
		assertEquals("Should be able to extract DOI from mailformed URL", 
				"10.4049/jimmunol.1101206", 
				GraphUtils.extractDoi("http://dx.doi.org/doi:10.4049/jimmunol.1101206"));

		assertEquals("Should be able to extract DOI starting with doi:", 
				"10.1080/106351598260581", 
				GraphUtils.extractDoi("doi:10.1080/106351598260581"));
		
		assertEquals("Should be able to extract mailformed DOI", 
				"10.1371/journal.pone.0019001", 
				GraphUtils.extractDoi("e19001 10.1371/journal.pone.0019001"));

		assertNull("Should not be able to extract incomplete DOI", 
				GraphUtils.extractDoi("10.7357"));
		
		assertEquals("Should be able to generate DOI URL", 
				"dx.doi.org/10.4049/jimmunol.1101206", 
				GraphUtils.generateDoiUri("10.4049/jimmunol.1101206"));
	}

	
	@Test
	public void scopusTest() {
			assertEquals("Should be able to extract Scopus Author Id", 
				"26538571500", 
				GraphUtils.extractScopusAuthorId("http://www.scopus.com/inward/authorDetails.url?authorID=26538571500&partnerID=MN8TOARS"));
		
		assertEquals("Should be able to extract Scopus Author Id with wrong case", 
				"25224691800", 
				GraphUtils.extractScopusAuthorId("http://www.scopus.com/authid/detail.url?authorId=25224691800"));
				
		assertEquals("Should be able to extract Scopus Partner Id", 
				"MN8TOARS", 
				GraphUtils.extractScopusPartnerId("http://www.scopus.com/inward/authorDetails.url?authorID=26538571500&partnerID=MN8TOARS"));
		
		assertEquals("Should be able to extract Scopus EID", 
				"2-s2.0-0036045752", 
				GraphUtils.extractScopusEID("http://www.scopus.com/inward/record.url?eid=2-s2.0-0036045752&partnerID=MN8TOARS"));

		assertEquals("Should be able to generate Scopus URL", 
				"www.scopus.com/inward/authorDetails.url?authorID=26538571500&partnerID=MN8TOARS", 
				GraphUtils.generateScopusUri("26538571500"));
	}

}
