package org.rdswitchboard.importers.google.cache;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

import com.amazonaws.util.StringUtils;

public class App {
	
	private static final String PROPERTIES_FILE = "properties/import_search_cache.properties";
	
	public static void main(String[] args) {
        try {
        	String propertiesFile = PROPERTIES_FILE;
        	if (args.length > 0 && !StringUtils.isNullOrEmpty(args[0])) 
        		propertiesFile = args[0];
        	
        	Properties properties = new Properties();
            try (InputStream in = new FileInputStream(propertiesFile)) {
                properties.load(in);
            }
            
            Importer importer = new Importer(properties);
            importer.process("grant");
            importer.process("publication");
           
        } catch (Exception e) {
            e.printStackTrace();
    }       
}
}
