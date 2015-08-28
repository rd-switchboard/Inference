package org.rdswitchboard.nexus.exporters.rda.keys;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

public class App {

	public static void main(String[] args) {
		try {
			Properties properties = new Properties();

			InputStream in = new FileInputStream("properties/export_rda_keys.properties");
			properties.load(in);
			in.close();
			
			String sourceNeo4jFolder = properties.getProperty("neo4j", "neo4j");
			
	       	Exporter expoter = new Exporter(sourceNeo4jFolder);
	        expoter.process();
	        
		} catch (Exception e) {
			e.printStackTrace();
		}		
	}
}
