package org.rdswitchboard.utils.fuzzy_search;

/**
 * Fuzzy Search algorithm
 * 
 */


import java.lang.reflect.Field;

public class FuzzySearch {
	
	private static Field fieldValue;
	
	static {
		try {
			fieldValue = String.class.getDeclaredField("value");
			fieldValue.setAccessible(true);
		} catch (Exception e) {
			e.printStackTrace();
		} 	
	}
	
	public static final char[] stringToCharArray(String string) throws FuzzySearchException {
		try {
			return (char[]) fieldValue.get(string);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			
			throw new FuzzySearchException("Unable to find String Value: Illegal Argument");
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			
			throw new FuzzySearchException("Unable to find String Value: Illegal Access");
		}	
	}
	
/**
	 DISTANCE 1 POSITIONS
	 insertions:
	 a,1,2,3; 1,a,2,3; 1,2,a,3; 1,2,3,a
	 
	 substitutions:
	 a,2,3; 1,a,3; 1,2,a
	 
	 deletions:
	 2,3; 1,3; 1,2

	 DISTANCE 2 POSITIONS
	
	 PAIRS: (0,1);(0,2);(0,3);(0,4);(1,2);(1,3);(1,4);(2,3);(2,4);(3,4);(4,4)
	 
	 double insertions:
	 a,b,1,2,3(0,1); a,1,b,2,3(0,2); a,1,2,b,3(0,3); a,1,2,3,b(0,4); 1,a,b,2,3(1,2); 1,a,2,b,3(1,3); 1,a,2,3,b(1,4); 1,2,a,b,3(2,3); 1,2,a,3,b(2,4); 1,2,3,a,b(3,4);
	 b,a,1,2,3(0,1); b,1,a,2,3(0,2); b,1,2,a,3(0,3); b,1,2,3,a(0,4); 1,b,a,2,3(1,2); 1,b,2,a,3(1,3); 1,b,2,3,a(1,4); 1,2,b,a,3(2,3); 1,2,b,3,a(2,4); 1,2,3,b,a(3,4);
	 
	 double substitutions:
	 a,b,3(0,1); a,2,b(0,2); 1,a,b(1,2);
	 b,a,3(0,1); b,2,a(0,2); 1,b,a(1,2);
	 
	 insertion substitution:
	 a,b,2,3(0,1); a,1,b,3(0,2); a,1,2,b(0,3); 1,a,b,3(1,2); 1,a,2,b(1,3); 1,2,a,b(2,3)
	 b,a,2,3(0,1); b,1,a,3(0,2); b,1,2,a(0,3); 1,b,a,3(1,2); 1,b,2,a(1,3); 1,2,b,a(2,3)
	 
	 substitution insertion:
	 a,b,2,3(0,1); a,2,b,3(0,2); a,2,3,b(0,3); 1,a,b,3(1,2); 1,a,3,b(1,3); 1,2,a,b(2,3)
	 b,a,2,3(0,1); b,2,a,3(0,2); b,2,3,a(0,3); 1,b,a,3(1,2); 1,b,3,a(1,3); 1,2,b,a(2,3)
	 
	 3 deletion deletion:
	 3(0,1); 2(0,2); 1(1,2);
	
	 deletion insertion a:
	 a,2,3(0,1); 2,a,3(0,2); 2,3,a(0,3); 1,a,3(1,2); 1,3,a(1,3); 1,2,a(2,3)
	 
	 deletion insertion b:
	 b,2,3(0,1); 2,b,3(0,2); 2,3,b(0,3); 1,b,3(1,2); 1,3,b(1,3); 1,2,b(2,3)
	 
	 insertion deletion a:
	 a,1,3(0,1); a,1,2(0,2); a,1,2,3(0,3); 1,a,2(1,2); 1,a,2,3(1,3); 1,2,a,3(2,3)
	 
	 insertion deletion b:
	 b,1,3(0,1); b,1,2(0,2); b,1,2,3(0,3); 1,b,2(1,2); 1,b,2,3(1,3); 1,2,b,3(2,3)
	 
	 deletion substitution a:
	 a,3(0,1); 2,a(0,2); 1,a(1,2);
	 
	 deletion substitution b:
	 b,3(0,1); 2,b(0,2); 1,b(1,2);
	 
	 substitution deletion a:
	 a,3(0,1); a,2(0,2); 1,a(1,2);
	 
	 substitution deletion b:
	 b,3(0,1); b,2(0,2); 1,b(1,2);
	 */	
	
	/**
	 * Function to find a needle in a string
	 * 
	 * Function will use Field to get access to the string characters, 
	 * as this is fastest way possible

	 * @param needle String, a string to find 
	 * @param string String, a data where we will search the string
	 * @param distance int the maximum distance, can not be less that 1
	 * @return int - the index of the string or -1 if string can not be find
	 * @throws FuzzySearchException 
	 */
	public static int find(String needle, String string, int distance) throws FuzzySearchException {
		try {
			return find((char[]) fieldValue.get(needle), (char[]) fieldValue.get(string), distance);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			
			throw new FuzzySearchException("Unable to find String Value: Illegal Argument");
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			
			throw new FuzzySearchException("Unable to find String Value: Illegal Access");
		}	
	}
	
	/**
	 * Function to find a needle in a string
	 *
	 * A fast version of the string, that will use final char[] parameters
	 * @param needle final char[], a string to find 
	 * @param data final char[], a data where we will search the string
	 * @param distance int the maximum distance, can not be less that 1
	 * @return int - the index of the string or -1 if string can not be find
	 * @throws FuzzySearchException 
	 */
	public static int find(final char[] needle, final char[] data, int distance) throws FuzzySearchException {
		// check for distance parameter. The distance 0 will make function to search for exact string
		// in this case there is more convenient way as String.find()
		if (distance < 1)
			throw new FuzzySearchException("The distance must to be greather or equal to 1");
		
		// obtain needle length
		int lenNeedle = needle.length;
		
		// check that needle.length > distance 
		// if needle length is less or equal to distance,  
		// what could replace entire string and this still will be a match. 
		// Return 0 for now, but later we might want to throw an exception
		if (lenNeedle <= distance)
			return 0;
		
		// Calculate the maximum search length. The length is equals:
		// data.length - needle.length + distance
		int lenData = data.length;
		int lenSearch = lenData - lenNeedle + distance + 1;

		// init result
		int resultDistance = 0;
		int result = -1;
		 
		// the counter of distance
		int counter, needleOffset, dataOffset, errorOffset, errorLength;
		boolean characterFound;
		
		// Run the loop and try to find the beginning of the string
		for (int dataPos = 0; dataPos < lenSearch; ++dataPos) {
			
			// start from begining of the needle
			counter = needleOffset = dataOffset = 0;
			
			// run loop until we reach errors limit or string is finished 
			while ((dataPos + dataOffset) < lenData && needleOffset < lenNeedle) {
				// check that current data character is a exact match of current needle character
				if (data[dataPos + dataOffset] == needle[needleOffset]) {
					// we have found exact match of the string, move on
					++dataOffset;
					++needleOffset;
				} else  if (counter < distance) { // check, that we have a margin for an error
					characterFound = false;
					// check as many next characters as errors we do have left
					errorLength = distance - counter + 1;
					if (errorLength > lenNeedle - needleOffset)
						errorLength = lenNeedle - needleOffset;
					for (errorOffset = 1; errorOffset < errorLength; ++errorOffset) {
						// check next data and needle character
						if (data[dataPos + dataOffset] == needle[needleOffset + errorOffset]) {
							if (data[dataPos + dataOffset + errorOffset] == needle[needleOffset]) {
								// the character has been substituted
								dataOffset += errorOffset + 1;
								needleOffset += errorOffset + 1;
							} else {
								// the character has been deleted
								++dataOffset;
								needleOffset += errorOffset + 1;
							}
							
							characterFound = true;
							break;
						} else if (data[dataPos + dataOffset + errorOffset] == needle[needleOffset]) {
							// the character has been inserted
							dataOffset += errorOffset + 1;
							++needleOffset;
							
							characterFound = true;
							break;
						} else if (data[dataPos + dataOffset + errorOffset] == needle[needleOffset + errorOffset]) {
							// the character has been replaced
							dataOffset += errorOffset + 1;
							needleOffset += errorOffset + 1;
							characterFound = true;
							break;
						}  
					}
						
					// if we didn't find next position, abort the search, otherways increase the counter
					if (characterFound) 
						counter += errorOffset;
					else
						break; 						
				} else // if current character is wrong and there is no margin for an error, abort the search
					break;				
			}
			
			// add all unprocessed characters as an error
			if (needleOffset < lenNeedle)
				counter += lenNeedle - needleOffset;
			
			// check that we have tested enough of the string
			if (counter <= distance && (result < 0 || resultDistance > counter)) {
				result = dataPos;
				resultDistance = counter;
			}
		}
		
		return result;
	}
	
	/**
	 * Function to find first needle in a string
	 * 
	 * @param needle
	 * @param data
	 * @param distance
	 * @return true if needle exists
	 * @throws FuzzySearchException
	 */
	public static boolean isExist(final char[] needle, final char[] data, int distance) throws FuzzySearchException {
		// check for distance parameter. The distance 0 will make function to search for exact string
		// in this case there is more convenient way as String.find()
		if (distance < 1)
			throw new FuzzySearchException("The distance must to be greather or equal to 1");
		
		// obtain needle length
		int lenNeedle = needle.length;
		
		// check that needle.length > distance 
		// if needle length is less or equal to distance,  
		// what could replace entire string and this still will be a match. 
		// Return 0 for now, but later we might want to throw an exception
		if (lenNeedle <= distance)
			return false;
		
		// Calculate the maximum search length. The length is equals:
		// data.length - needle.length + distance
		int lenData = data.length;
		int lenSearch = lenData - lenNeedle + distance + 1;

		// init result
		int resultDistance = 0;
		int result = -1;
		 
		// the counter of distance
		int counter, needleOffset, dataOffset, errorOffset, errorLength;
		boolean characterFound;
		
		// Run the loop and try to find the beginning of the string
		for (int dataPos = 0; dataPos < lenSearch; ++dataPos) {
			
			// start from begining of the needle
			counter = needleOffset = dataOffset = 0;
			
			// run loop until we reach errors limit or string is finished 
			while ((dataPos + dataOffset) < lenData && needleOffset < lenNeedle) {
				// check that current data character is a exact match of current needle character
				if (data[dataPos + dataOffset] == needle[needleOffset]) {
					// we have found exact match of the string, move on
					++dataOffset;
					++needleOffset;
				} else  if (counter < distance) { // check, that we have a margin for an error
					characterFound = false;
					// check as many next characters as errors we do have left
					errorLength = distance - counter + 1;
					if (errorLength > lenNeedle - needleOffset)
						errorLength = lenNeedle - needleOffset;
					for (errorOffset = 1; errorOffset < errorLength; ++errorOffset) {
						// check next data and needle character
						if (data[dataPos + dataOffset] == needle[needleOffset + errorOffset]) {
							if (data[dataPos + dataOffset + errorOffset] == needle[needleOffset]) {
								// the character has been substituted
								dataOffset += errorOffset + 1;
								needleOffset += errorOffset + 1;
							} else {
								// the character has been deleted
								++dataOffset;
								needleOffset += errorOffset + 1;
							}
							
							characterFound = true;
							break;
						} else if (data[dataPos + dataOffset + errorOffset] == needle[needleOffset]) {
							// the character has been inserted
							dataOffset += errorOffset + 1;
							++needleOffset;
							
							characterFound = true;
							break;
						} else if (data[dataPos + dataOffset + errorOffset] == needle[needleOffset + errorOffset]) {
							// the character has been replaced
							dataOffset += errorOffset + 1;
							needleOffset += errorOffset + 1;
							characterFound = true;
							break;
						}  
					}
						
					// if we didn't find next position, abort the search, otherways increase the counter
					if (characterFound) 
						counter += errorOffset;
					else
						break; 						
				} else // if current character is wrong and there is no margin for an error, abort the search
					break;				
			}
			
			// add all unprocessed characters as an error
			if (needleOffset < lenNeedle)
				counter += lenNeedle - needleOffset;
			
			// check that we have tested enough of the string
			if (counter <= distance)
				return true;
		}
		
		return false;
	}
	
	
	
	/*
	public static void main(String[] args) {
		final String DATA = "   Hello World  Hellx World ";
		
		try {
		/*	System.out.println("test: " + find("Hello World", DATA, 1));
			System.out.println("Best match:" + find("Hellx World", DATA, 1));
			System.out.println("Best match:" + find("Hillx World", DATA, 1));
			System.out.println("Deletion:" + find("Helo World", DATA, 1));
			System.out.println("Deletion 2:" + find("Hel World", DATA, 2));
			System.out.println("Insertion:" + find("Hello1 World", DATA, 1));
			System.out.println("Replacment:" + find("Helly World", DATA, 1));
			System.out.println("Substitution:" + find("Helol World", DATA, 1));
			System.out.println("Substitution 2:" + find("Heoll World", DATA, 2));
			System.out.println("Invalid string:" + find("Heoll World", DATA, 1));
			System.out.println("Begin search:" + find("1   Hello World", DATA, 1));* /
			System.out.println("End search:" + find("Hellx World ....", DATA, 3));
		} catch (Exception e) {
			e.printStackTrace();
			fail("Exception");
		}
	}*/
}
