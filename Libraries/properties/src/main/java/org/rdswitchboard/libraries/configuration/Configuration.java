package org.rdswitchboard.libraries.configuration;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

public class Configuration {

	// Properties file
	public static final String PROPERTIES_FILE = "inference.conf";
	
	// Source and Target
	public static final String PROPERTY_SYNC_HOME = "sync.home";
	public static final String PROPERTY_SYNC_SOURCE = "sync.source";
	public static final String PROPERTY_SYNC_TARGET = "sync.target";
	public static final String PROPERTY_SYNC_KEYS = "sync.keys";
	public static final String PROPERTY_SYNC_LEVEL = "sync.level";
	public static final String PROPERTY_SYNC_BUCKET = "sync.bucket";
	
	// Neo4j
	public static final String PROPERTY_NEO4J = "neo4j";
	public static final String PROPERTY_NEO4J_NEXUS = "neo4j.nexus";
	public static final String PROPERTY_NEO4J_VERSION= "neo4j.version";
	
	// Data
	public static final String PROPERTY_SOURCE = "source";
	public static final String PROPERTY_CROSSWALK = "crosswalk";
		
	// XML
	public static final String PROPERTY_XML_FOLDER = "xml.folder";
	public static final String PROPERTY_XML_TYPE = "xml.type";
	
	// S3
	public static final String PROPERTY_S3_BUCKET = "s3.bucket";
	public static final String PROPERTY_S3_PREIFX = "s3.prefix";
	
	// ANDS
	public static final String PROPERTY_ANDS_S3 = "ands.s3";
	public static final String PROPERTY_ANDS_XML = "ands.xml";
	public static final String PROPERTY_ANDS_XML_TYPE = "ands.xml.type";
	public static final String PROPERTY_ANDS_SOURCE = "ands.source";
	public static final String PROPERTY_ANDS_CROSSWALK = "ands.crosswalk";
	
	// ARC
	public static final String PROPERTY_ARC_COMPLETED = "arc.completed";
	public static final String PROPERTY_ARC_NEW = "arc.new";
		
	// CERN
	public static final String PROPERTY_CERN_S3 = "cern.s3";
	
	// CrossRef
	public static final String PROPERTY_CROSSREF_CACHE = "crossref.cache";
	public static final String PROPERTY_CROSSREF_DATA = "crossref.data";
	
	// DaRa
	public static final String PROPERTY_DARA_S3 = "dara.s3";
	
	// Dryad
	public static final String PROPERTY_DRYAD_S3 = "dryad.s3";
	
	// NHMRC 
	public static final String PROPERTY_NHMRC_GRANTS = "nhmrc.grants";
	public static final String PROPERTY_NHMRC_ROLES = "nhmrc.roles";
	
	// OpenAIRE
	public static final String PROPERTY_DLI_S3 = "dli.s3";
	
	// ORCID
	public static final String PROPERTY_ORCID_JSON = "orcid.json";
	
	// Google
	public static final String PROPERTY_GOOGLE_CACHE = "google.cache";
	public static final String PROPERTY_GOOGLE_BLACK_LIST = "google.black.list";
	public static final String PROPERTY_GOOGLE_MIN_TITLE_LENGTH = "google.min.title.length";
	public static final String PROPERTY_GOOGLE_THREADS = "google.threads";
	public static final String PROPERTY_GOOGLE_MAX_ATTEMPTS = "google.max.attempts";
	public static final String PROPERTY_GOOGLE_ATTEMPT_DELAY = "google.attempt.delay";
	public static final String PROPERTY_GOOGLE_CONNECTION_TIMEOUT = "google.connection.timeout";
	public static final String PROPERTY_GOOGLE_READ_TIMEOUT = "google.read.timeout";
	public static final String PROPERTY_GOOGLE_CSE_ID = "google.cse.id";
	public static final String PROPERTY_GOOGLE_API_KEY = "google.api.key";
	
	// Export
	
	public static final String PROPERTY_EXPORT_FILE = "export.file";
	public static final String PROPERTY_EXPORT_SOURCE = "export.source";
	public static final String PROPERTY_EXPORT_TYPE = "export.type";
	public static final String PROPERTY_EXPORT_PROPERTY = "export.property";
	
	// Static
	public static final String PROPERTY_INSTITUTIONS = "institutions";
	public static final String PROPERTY_PATTERNS = "patterns";
	public static final String PROPERTY_SERVICES = "services";
	public static final String PROPERTY_ARCHITECTURE_VERSION = "architecture.version";
	public static final String PROPERTY_GOOGLE_VERSION = "google.version";
	public static final String PROPERTY_BUILD_NUMBER = "build.number";
	public static final String PROPERTY_VERSION_FILE = "version.file";
	public static final String PROPERTY_VERSIONS_FOLDER = "versions.folder";
	
	// Output
	
	public static final String PROPERTY_KEYS_FILE = "keys.file";
	
	// Test
	public static final String PROPERTY_TEST_MIN_CONNECTIONS = "test.min.connections";
	
	// Private
	private static final String PROPERTY_CONF = "conf";	
	private static char EQUALS = '=';
	private static String REG = "=";
	
	public static Properties fromArgs(String[] args) throws Exception
	{
		Properties properties = new Properties();
		boolean redirected = false;
		
		String propertiesFile = PROPERTIES_FILE;
		if (null != args && args.length > 0) {
			for (String arg : args)
			{
				if (arg.indexOf(EQUALS) >= 0)
				{
					String[] parts = arg.split(REG);
					if (parts.length == 2 && !parts[0].isEmpty() && parts[0].equals(PROPERTY_CONF)) 
						propertiesFile = parts[1];
					else
						continue;				
				} else  
					propertiesFile = arg;
					
				redirected = true;
				break;
			}
		}
				
		File conf = new File(propertiesFile); 
		if (conf.exists() && conf.isFile()) {
			try 
			{
				try (InputStream in = new FileInputStream(conf)) {
					properties.load(in);
				}
			} catch (Exception e) {
				throw new Exception("Unable to load configuration file: `" + propertiesFile + "`. " + e.getMessage());
			}
		} else if (redirected) 
			throw new Exception("Unable to load configuration file: `" + propertiesFile + "`. This is not a file.");
		
		for (String arg : args)
		{
			if (arg.indexOf(EQUALS) >= 0)
			{
				String[] parts = arg.split(REG);
				if (parts.length == 2 && !parts[0].isEmpty() && !parts[0].equals(PROPERTY_CONF)) 
				{
					properties.setProperty(parts[0], parts[1]);
				}
			}				
		}
	
		return properties;
	}
}
