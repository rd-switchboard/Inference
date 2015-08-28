package org.rdswitchboard.libraries.scopus.test;

import static org.junit.Assert.*;

import java.io.UnsupportedEncodingException;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.rdswitchboard.libraries.scopus.Scopus;
import org.rdswitchboard.libraries.scopus.type.AbstractType;
import org.rdswitchboard.libraries.scopus.type.ResourceType;

public class ScopusTest {
	private static Scopus scopus;
		
	@BeforeClass
	public static void init() {
		scopus = new Scopus("", "");
	}
	
	@Ignore
	public void abstractTest() {
		// downlpad record
		String abstractJson = scopus.abstractString(AbstractType.abstractTypeScopusId, "0037070197");
		System.out.println(abstractJson);
		
		assertNotNull(abstractJson);

  //      AbstractResponse result  = scopus.parseAbstractResponse(new File("/home/dima/abstractResult.json"));
  //       System.out.println(result);
	}
	
	@Ignore
	public void searchTest() {
		String json = null;
		try {
			json = scopus.searchString(ResourceType.resourceTypeScopus, "AU-ID(8528261900)");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		System.out.println(json);
		
		assertNotNull("Should be able to download search information", json);
	   
		
	}

}
