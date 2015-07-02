package org.rdswitchboard.utils.graph;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class GraphUtils {
	public static final String FOLDER_NODE = "node";
	public static final String FOLDER_RELATIONSHIP = "relationship";
	public static final String FOLDER_FIELD = "field";
	public static final String FOLDER_SCHEMA = "schema";
	
	public static final String GRAPH_EXTENSION = ".json";
	public static final String GRAPH_SCHEMA = "schema" + GRAPH_EXTENSION;
	
	public static final int MIN_TITLE_LENGTH = 20;
	//public static final int MIN_TITLE_WORDS = 2;
	
	public static final File getSchemaFolder(File folder) {
		return new File(folder, FOLDER_SCHEMA);
	}
	
	public static final File getNodeFolder(File folder) {
		return new File(folder, FOLDER_NODE);
	}
	
	public static final File getRelationshipFolder(File folder) {
		return new File(folder, FOLDER_RELATIONSHIP);
	}
	
	public static final File getFieldFolder(File folder) {
		return new File(folder, FOLDER_FIELD);
	}
	
	public static boolean isTitleShort(String title) {
//		return title.length() < MIN_TITLE_LENGTH;
		return title.getBytes().length < MIN_TITLE_LENGTH;
	}

	/*
	public static boolean isTitleSimple(String title) {
		int counter = 1;
 	 	for (int i = 0; i < title.length(); ++i) 
 	 	    if(title.charAt(i) == ' ') {
 	 	        if (++counter >= MIN_TITLE_WORDS)
 	 	        	return false;
 	 	    }

 	 	return true;
	}
	*/
}
