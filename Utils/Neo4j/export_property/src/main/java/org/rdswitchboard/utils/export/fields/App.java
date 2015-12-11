package org.rdswitchboard.utils.export.fields;

import java.io.FileWriter;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.rdswitchboard.libraries.configuration.Configuration;
import org.rdswitchboard.libraries.graph.GraphUtils;
import org.rdswitchboard.libraries.neo4j.Neo4jDatabase;
import org.rdswitchboard.libraries.neo4j.interfaces.ProcessNode;

import au.com.bytecode.opencsv.CSVWriter;


public class App {
	
	private static final String[] HEADER = { "source", "key", "property" };
	
	public static void main(String[] args) {
		try {
			Properties properties = Configuration.fromArgs(args);
			
			String neo4jFolder = properties.getProperty(Configuration.PROPERTY_NEO4J);
	        if (StringUtils.isEmpty(neo4jFolder))
	            throw new IllegalArgumentException("Neo4j Folder can not be empty");
	        
	        String exportFile = properties.getProperty(Configuration.PROPERTY_EXPORT_FILE);
	        if (StringUtils.isEmpty(exportFile))
	            throw new IllegalArgumentException("Export file can not be empty");
	        
	        String exportProperty = properties.getProperty(Configuration.PROPERTY_EXPORT_PROPERTY);
	        if (StringUtils.isEmpty(exportProperty))
	            throw new IllegalArgumentException("Export property name can not be empty");

	        String exportSource = properties.getProperty(Configuration.PROPERTY_EXPORT_SOURCE);
	        if (StringUtils.isEmpty(exportSource))
	            throw new IllegalArgumentException("Export node source can not be empty");

	        String exportType = properties.getProperty(Configuration.PROPERTY_EXPORT_TYPE);

	        export(neo4jFolder, exportFile, exportProperty, exportSource, getLabelOrNull(exportType));
	        
		} catch (Exception e) {
			e.printStackTrace();
		}		
	}

	private static Label getLabelOrNull(String str) {
		if (StringUtils.isEmpty(str))
			return null;
		
		return DynamicLabel.label(str);
	}
	
	/*
	private static Label[] getLabels(String str)
	{
		if (StringUtils.isEmpty(str)) 
			return null;
		
		String arr[] = str.split(",");
		Label labels[] = new Label[arr.length];
		
		for (int n = 0; n < arr.length; ++n) 
			labels[n] = DynamicLabel.label(arr[n].trim());
		
		return labels;			
	}*/
	
	/*
	private static boolean isNodeHasALabel(Node node, Label[] labels) {
		if (null == labels || labels.length == 0)
			return true;
		
		for (Label label : labels) {
			if (node.hasLabel(label))
				return true;
		}
		
		return false;
	}*/
	
	
	private static void export(final String folder, final String file, final String property, 
			final String source, final Label type) throws Exception {
		
		try (CSVWriter writer = new CSVWriter(new FileWriter(file))) {
			writer.writeNext(HEADER);
						
			Neo4jDatabase neo4j = new Neo4jDatabase(folder);
			neo4j.enumrateAllNodesWithLabelAndProperty(source, property, new ProcessNode() {
				
				@Override
				public boolean processNode(Node node) throws Exception {
					if (null == type || node.hasLabel(type)) {
						String key = (String) node.getProperty(GraphUtils.PROPERTY_KEY);
						Object values = node.getProperty(property);
						if (values instanceof String) 
							writer.writeNext(new String[] { source, key, (String)values }); 	
						else if (values instanceof String[]) 
							for (String value : (String[])values)
								writer.writeNext(new String[] { source, key, value });
												
					}
					
					return true;
				}			
			});			
		}
	}	
}
