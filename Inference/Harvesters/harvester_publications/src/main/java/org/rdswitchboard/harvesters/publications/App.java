package org.rdswitchboard.harvesters.publications;

import java.io.IOException;

import javax.xml.bind.JAXBException;

public class App {
private static final String PROPERTIES_URI = "conf/publications.conf";
	
	public static void main(String[] args) {

		String propertiesUri = PROPERTIES_URI;
		if (args.length > 0 && !args[0].isEmpty())
			propertiesUri = args[0];

		try {
			Harvester importer = new Harvester(propertiesUri);
			importer.harvestPublications();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}	
}
