package org.rdswitchboard.importers.version;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.rdswitchboard.libraries.configuration.Configuration;
import org.rdswitchboard.libraries.graph.GraphNode;
import org.rdswitchboard.libraries.graph.GraphUtils;
import org.rdswitchboard.libraries.neo4j.Neo4jDatabase;

//import static java.nio.charset.StandardCharsets.UTF_8;

public class App {
	private static final String NEO4J_VERSION_FILE = "data/version";
	private static final String PART_VERSION = "_lastupdate";
	
	public static void main(String[] args) {
		try {
			Properties properties = Configuration.fromArgs(args);
	        
	        System.out.println("Importing NHMRC Grants");
	                
	        String neo4jNexusFolder = properties.getProperty(Configuration.PROPERTY_NEO4J_NEXUS);
	        if (StringUtils.isEmpty(neo4jNexusFolder))
	            throw new IllegalArgumentException("Neo4j Nexus Folder can not be empty");
	        System.out.println("Neo4j: " + neo4jNexusFolder);

	        String neo4jVersion = properties.getProperty(Configuration.PROPERTY_NEO4J_VERSION);
	        if (StringUtils.isEmpty(neo4jVersion))
	            throw new IllegalArgumentException("Neo4j Version can not be empty");
	        System.out.println("Neo4j Version: " + neo4jVersion);
	        
	        String archVersion = properties.getProperty(Configuration.PROPERTY_ARCHITECTURE_VERSION);
	        if (StringUtils.isEmpty(archVersion))
	            throw new IllegalArgumentException("Architecture Version can not be empty");
	        System.out.println("Architecture Version: " + archVersion);
	        
	        String googleVersion = properties.getProperty(Configuration.PROPERTY_GOOGLE_VERSION);
	        if (StringUtils.isEmpty(googleVersion))
	            throw new IllegalArgumentException("Google Version can not be empty");
	        System.out.println("Google Search Version: " + googleVersion);

	        String buildNumberFile = properties.getProperty(Configuration.PROPERTY_BUILD_NUMBER);
	        if (StringUtils.isEmpty(buildNumberFile))
	            throw new IllegalArgumentException("Build Number File can not be empty");
	        Path buildNumberPath = Paths.get(buildNumberFile);
	        int buildNumber = 0;
	        if (Files.isRegularFile(buildNumberPath) & Files.isReadable(buildNumberPath)) {
	        	try {
	        		buildNumber = Integer.parseInt(new String(Files.readAllBytes(buildNumberPath)));
	        	} catch (NumberFormatException e) {
	        		buildNumber = 0;
	        	}
	        }
	        ++buildNumber;

	        String versionFile = properties.getProperty(Configuration.PROPERTY_VERSION_FILE);
	        if (StringUtils.isEmpty(versionFile))
            	throw new IllegalArgumentException("Version file can not be empty");
	        
	        String versionFolder = properties.getProperty(Configuration.PROPERTY_VERSIONS_FOLDER);
	        if (StringUtils.isEmpty(versionFolder))
	            throw new IllegalArgumentException("Versions Folder can not be empty");
	        
	        System.out.println("Build Number: " + buildNumber);

	        String version = String.format("%s.%s.%d-%s", archVersion, googleVersion, buildNumber, neo4jVersion);

	        System.out.println("Combined Version: " + version);

	        Map<String, String> versions = new HashMap<String, String>();
	        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(Paths.get(versionFolder))) {
	            for (Path path : directoryStream) 
	            	if (Files.isRegularFile(path) & Files.isReadable(path)) {
	            		String name = path.getFileName().toString();
	            		String value = new String(Files.readAllBytes(path));
	            		
	            		System.out.println(String.format("Module %s version: %s", name, value));
	            		
	            		versions.put(name, value);
	            	}
	        } catch (IOException ex) {}
	        
	        GraphNode node = new GraphNode()
				.withKey(GraphUtils.TYPE_VERSION, version)
				.withSource(GraphUtils.SOURCE_SYSTEM)
				.withType(GraphUtils.TYPE_VERSION)
				.withProperty(GraphUtils.PROPERTY_VERSION, version);
	        
	        for (Map.Entry<String, String> entry : versions.entrySet()) 
	        	node.addProperty(entry.getKey() + PART_VERSION, entry.getValue());

	        Neo4jDatabase importer = new Neo4jDatabase(neo4jNexusFolder);
	        importer.importNode(node);
	        importer.printStatistics(System.out);
	       
	        try (
	        	BufferedWriter buffer = Files.newBufferedWriter(Paths.get(neo4jNexusFolder, NEO4J_VERSION_FILE), StandardCharsets.UTF_8);
	        	PrintWriter writer = new PrintWriter(buffer)
	        ) {
	        	writer.println(String.format("Version: %s", version));
	        	writer.println(String.format("Architecture version: %s", archVersion));
	        	writer.println(String.format("Google Cache version: %s", googleVersion));
	        	writer.println(String.format("Build Number: %s", buildNumber));
	        	writer.println(String.format("Neo4j version: %s", neo4jVersion));
	        	
	        	for (Map.Entry<String, String> entry : versions.entrySet()) 
	        		writer.println(String.format("Module %s version: %s", entry.getKey(), entry.getValue()));
	        } catch (IOException x) {
	            System.err.format("IOException: %s%n", x);
	        }
	        
//	        Files.write(Paths.get(neo4jNexusFolder, NEO4J_VERSION_FILE), version.getBytes());
	        Files.write(Paths.get(versionFile), version.getBytes());
	        Files.write(Paths.get(buildNumberFile), Integer.toString(buildNumber).getBytes());
	        
		} catch (Exception e) {
			e.printStackTrace();
			
			System.exit(1);
		}		
	}
}
