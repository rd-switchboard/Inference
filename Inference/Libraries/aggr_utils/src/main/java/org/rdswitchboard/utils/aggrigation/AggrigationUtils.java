package org.rdswitchboard.utils.aggrigation;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.schema.ConstraintDefinition;
import org.rdswitchboard.utils.neo4j.local.Neo4jUtils;

public class AggrigationUtils {
	public enum Labels implements Label {
		Institution,
		Researcher, 
		Grant, 
		Publication, 
		Dataset,
		RDA, 
		Dryad, 
		ORCID, 
		CrossRef,
		figshare,
		Scopus,
		CERN,
		Web,
		NHMRC,
		ARC
	}
	
	public enum RelTypes implements RelationshipType {
		relatedTo,
		knownAs
	}
	
	public static final String LABEL_INSTITUTION = Labels.Institution.name();
	public static final String LABEL_RESEARCHER = Labels.Researcher.name();
	public static final String LABEL_GRANT = Labels.Grant.name();
	public static final String LABEL_DATASET = Labels.Dataset.name();
	public static final String LABEL_PUBLICATION = Labels.Publication.name();
	public static final String LABEL_RDA = Labels.RDA.name();
	public static final String LABEL_DRYAD = Labels.Dryad.name();
	public static final String LABEL_ORCID = Labels.ORCID.name();
	public static final String LABEL_FIGSHARE = Labels.figshare.name();
	public static final String LABEL_WEB = Labels.Web.name();
	public static final String LABEL_CROSSREF = Labels.CrossRef.name();
	public static final String LABEL_NHMRC = Labels.NHMRC.name();
	public static final String LABEL_ARC = Labels.ARC.name();
	
	public static final String LABEL_INSTITUTION_LOWERCASE = LABEL_INSTITUTION.toLowerCase();
	public static final String LABEL_RESEARCHER_LOWERCASE = LABEL_RESEARCHER.toLowerCase();
	public static final String LABEL_GRANT_LOWERCASE = LABEL_GRANT.toLowerCase();
	public static final String LABEL_DATASET_LOWERCASE = LABEL_DATASET.toLowerCase();
	public static final String LABEL_PUBLICATION_LOWERCASE = LABEL_PUBLICATION.toLowerCase();
	public static final String LABEL_RDA_LOWERCASE = LABEL_RDA.toLowerCase();
	public static final String LABEL_DRYAD_LOWERCASE = LABEL_DRYAD.toLowerCase();
	public static final String LABEL_ORCID_LOWERCASE = LABEL_ORCID.toLowerCase();
	public static final String LABEL_FIGSHARE_LOWERCASE = LABEL_FIGSHARE.toLowerCase();
	public static final String LABEL_WEB_LOWERCASE = LABEL_WEB.toLowerCase();
	public static final String LABEL_CROSSREF_LOWERCASE = LABEL_CROSSREF.toLowerCase();
	public static final String LABEL_NHMRC_LOWERCASE = LABEL_NHMRC.toLowerCase();
	public static final String LABEL_ARC_LOWERCASE = LABEL_ARC.toLowerCase();
	
	public static final String REL_KNOWN_AS = RelTypes.knownAs.name();

    public static final String PROPERTY_KEY = "key";
    public static final String PROPERTY_NODE_TYPE = "node_type";
    public static final String PROPERTY_NODE_SOURCE = "node_source";
    public static final String PROPERTY_TITLE = "title";
    public static final String PROPERTY_INITIALS = "initials";
    public static final String PROPERTY_FIRST_NAME = "first_name";
    public static final String PROPERTY_LAST_NAME = "last_name";
    public static final String PROPERTY_FULL_NAME = "full_name";
    public static final String PROPERTY_COUNTRY = "country";
    public static final String PROPERTY_URL = "url";
    public static final String PROPERTY_PURL = "purl";
    public static final String PROPERTY_ISNI = "isni";
    public static final String PROPERTY_NLA = "nla";
    public static final String PROPERTY_ORCID = "orcid";
    public static final String PROPERTY_GRANT_NUMBER = "grant_number";
    public static final String PROPERTY_DATE = "date";
    public static final String PROPERTY_AUTHORS = "authors";
    public static final String PROPERTY_DOI = "doi";
    public static final String PROPERTY_ISBN = "isbn";
    public static final String PROPERTY_RDA_URL = "rda_url";
    public static final String PROPERTY_ORCID_URL = "orcid_url";
    public static final String PROPERTY_DRYAD_URL = "dryad_url";
    public static final String PROPERTY_FIGSHARE_URL = "figshare_url";
    
    private static final String PART_PROTOCOL = "://";
    private static final String PART_SLASH = "/";
    private static final String PART_WWW = "www.";
    private static final String PART_ORCID_URI = "orcid.org/";
    private static final String PART_DOI_PERFIX = "doi:";
    private static final String PART_DOI_URI = "dx.doi.org/";
	
    private static final Pattern patternDoi = Pattern.compile("(^|doi:|dx\\.doi\\.org/)\\d{2}\\.\\d{4,}/.+$");
    private static final Pattern patternOrcid = Pattern.compile("\\d{4}-\\d{4}-\\d{4}-\\d{4}");

    public static String getArgument(String[] args, int id) {
		return args.length > id && !args[id].isEmpty() ? args[id] : null;
	}		
	
	public static String getArgument(String[] args, int id, String defValue) {
		String value = getArgument(args, id);
		return null == value ? defValue : value;
	}
	
	public static int getIntArgument(String[] args, int id, int defValue) {
		String value = getArgument(args, id);
		return null == value ? defValue : Integer.parseInt(value);
	}	    
    
    /**
     * Function to create missing relationships between set of nodes (knownAs relationships for example)
     * 
     * The function need to be encapsulated in the Neo4j transaction 
     * 
     * @param graphDb GraphDatabaseService
     * @param nodes Set<Long>, id of nodes
     * @param type RelationshipType
     * @param direction Direction
     * @return Number of relationships being created
     */
    public static long createMissingRelationships(GraphDatabaseService graphDb, Set<Long> nodes, 
    		RelationshipType type, Direction direction) {
		long relCounter = 0;
		
		if (nodes.size() > 1) {
			Long[] ids = (Long[]) nodes.toArray(new Long[nodes.size()]);
			for (int i1 = 0; i1 < ids.length - 1; ++i1) {
				Node start = graphDb.getNodeById(ids[i1]);
				Iterable<Relationship> rels = start.getRelationships(type, direction);
				for (int i2 = i1 + 1; i2 < ids.length; ++i2) 
					if (Neo4jUtils.findRelationship(rels, ids[i2], direction) == null) {
						Node end = graphDb.getNodeById(ids[i2]);
								
						if (direction != Direction.OUTGOING)
							start.createRelationshipTo(end, type);
						else
							end.createRelationshipTo(start, type);
						
						++relCounter;
					}							
			}
		}
		
		return relCounter;
	}
    
	public static String combineLabel(Label label1, Label label2) {
		return label1.name() + "_" + label2.name();
	}
	
	public static String doubleLabel(Label label1, Label label2) {
		return label1.name() + ":" + label2.name();
	}
    
    public static ConstraintDefinition createConstraint(GraphDatabaseService graphDb, 
    		Label labelSource, Label labelType) {
    	return Neo4jUtils.createConstrant(graphDb, combineLabel(labelSource, labelType), PROPERTY_KEY);
    }
    
    public static String extractUri(String str) {
    	if (null != str) {
    		str = str.trim();
    		if (!str.isEmpty()) {
	    		int index = str.indexOf(PART_PROTOCOL);
		    	if (index >= 0)
		    		str = str.substring(index + PART_PROTOCOL.length());
		    	if (str.startsWith(PART_WWW))
		    		str = str.substring(PART_WWW.length());
		    	if (str.endsWith(PART_SLASH))
		    		str = str.substring(0, str.length()-1);

		    	if (!str.isEmpty())
		    		return str;
    		}
    	}
    	
    	return null;
    }
    
    public static String extractOrcid(String str) {
    	if (null != str && !str.isEmpty()) {
    		Matcher matcher = patternOrcid.matcher(str);
			if (matcher.find())
				return matcher.group();
    	}
    	
    	return null;
	}
    
    public static String extractDoi(String str) {
    	if (null != str && !str.isEmpty()) {
    		Matcher matcher = patternDoi.matcher(str);
    		if (matcher.find()) {
    			String doi =  matcher.group();
    			if (doi.startsWith(PART_DOI_PERFIX))
    				doi = doi.substring(PART_DOI_PERFIX.length());
    			else if (doi.startsWith(PART_DOI_URI))
    				doi = doi.substring(PART_DOI_URI.length());
			  
    			return doi;
    		}
    	}
    	
		return null;
	}
    
    public static String generateOrcidUri(String orcid) {
    	return (null == orcid || orcid.isEmpty()) ? null : (PART_ORCID_URI + orcid);
    }
    
    public static String generateDoiUri(String doi) {
    	return (null == doi || doi.isEmpty()) ? null : (PART_DOI_URI + doi);
    }
    
	public static Set<String> loadList(String listName) throws FileNotFoundException, IOException {
		Set<String> list = new HashSet<String>();
		
		File file = new File(listName);
		if (file.exists()) {
			try(BufferedReader br = new BufferedReader(new FileReader(file))) {
			    for(String line; (line = br.readLine()) != null; ) 
			    	list.add(line.trim().toLowerCase());
			}
		}
		
		return list;
	}
	
	public static void saveList(String listName, Set<String> list) throws FileNotFoundException, IOException {
		try(BufferedWriter bw = new BufferedWriter(new FileWriter(new File(listName)))) {
		    for(String line : list) {
		    	bw.write(line);
		    	bw.write("\n");
		    }	
	    }
	}
	
	public static boolean isAllUpper(String s) {
	    for(char c : s.toCharArray()) 
	    	if(Character.isLetter(c) && Character.isLowerCase(c)) 
	    		return false;
	    return true;
	}
}
