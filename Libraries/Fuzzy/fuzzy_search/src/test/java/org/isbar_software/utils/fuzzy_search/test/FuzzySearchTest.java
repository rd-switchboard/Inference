/**
 * 
 */
package org.isbar_software.utils.fuzzy_search.test;

import static org.junit.Assert.*;

import org.junit.Test;
import org.rdswitchboard.utils.fuzzy_search.FuzzySearch;
import org.rdswitchboard.utils.fuzzy_search.FuzzySearchException;

/**
 * @author Dmitrij Kudriavcev, dmitrij@kudriavcev.info
 *
 */
public class FuzzySearchTest {

	/**
	 * Test method for {@link org.rdswitchboard.utils.fuzzy_search.FuzzySearch#find(java.lang.String, java.lang.String, int)}.
	 */
	private static final String DATA = "   Hello World  Hellx World ";
	
	@Test
	public void testFindStringStringInt() {
		try {
			assertEquals("Simple search", FuzzySearch.find("Hello World", DATA, 1), 3);
			assertEquals("Best match", FuzzySearch.find("Hellx World", DATA, 1), 16);
			assertEquals("Best match", FuzzySearch.find("Hillx World", DATA, 1), 16);
			assertEquals("Deletion", FuzzySearch.find("Helo World", DATA, 1), 3);
			assertEquals("Deletion 2", FuzzySearch.find("Hel World", DATA, 2), 3);
			assertEquals("Insertion", FuzzySearch.find("Hello1 World", DATA, 1), 3);
			assertEquals("Replacment", FuzzySearch.find("Helly World", DATA, 1), 3);
			assertEquals("Substitution", FuzzySearch.find("Helol World", DATA, 1), 3);
			assertEquals("Substitution 2", FuzzySearch.find("Heoll World", DATA, 2), 3);
			assertEquals("Invalid string", FuzzySearch.find("Heoll World", DATA, 1), -1);
			assertEquals("Begin search", FuzzySearch.find("11   Hello World", DATA, 2), 0);
			assertEquals("End search", FuzzySearch.find("Hellx World ...", DATA, 3), 16);
		} catch (FuzzySearchException e) {
			e.printStackTrace();
			fail("Exception");
		}
	}


}
