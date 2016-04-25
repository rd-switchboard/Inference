package org.rdswitchboard.libraries.graph;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

public class GraphUtils {
	// schema fields
	public static final String SCHEMA_LABEL = "label";
	public static final String SCHEMA_INDEX = "index";
	public static final String SCHEMA_UNIQUE = "unique";
	
	// required properties
	public static final String PROPERTY_INDEX = "index";
	public static final String PROPERTY_KEY = "key";
	public static final String PROPERTY_SOURCE = "node_source";
	public static final String PROPERTY_TYPE = "node_type";
	public static final String PROPERTY_TITLE = "title";
	public static final String PROPERTY_RDS_URL = "rds_url";
	
	// properties required for meta-data harmonization  
	public static final String PROPERTY_URL = "url"; 
   
	// optional properties
	public static final String PROPERTY_NLA = "nla";
	public static final String PROPERTY_DOI = "doi";
	public static final String PROPERTY_PURL = "purl";
	public static final String PROPERTY_LOCAL_ID = "local_id";
	public static final String PROPERTY_NAME_PREFIX = "name_prefix";
	public static final String PROPERTY_FIRST_NAME = "first_name";
	public static final String PROPERTY_MIDDLE_NAME = "middle_name";
	public static final String PROPERTY_LAST_NAME = "last_name";
	public static final String PROPERTY_FULL_NAME = "full_name";
	public static final String PROPERTY_COUNTRY = "country";
	public static final String PROPERTY_STATE = "state";
	public static final String PROPERTY_HOST = "host";
	public static final String PROPERTY_PATTERN = "pattern";
	public static final String PROPERTY_ORIGINAL_SOURCE = "original_source";
	public static final String PROPERTY_SCOPUS_ID = "scopus_id";
	public static final String PROPERTY_SCOPUS_EID = "scopus_eid";
	public static final String PROPERTY_ORCID_ID = "orcid";
	public static final String PROPERTY_ANDS_GROUP = "ands_group";
	public static final String PROPERTY_AWARDED_DATE = "awarded_date";
	public static final String PROPERTY_PUBLISHED_DATE = "published_date";
	public static final String PROPERTY_ARC_ID = "arc_id";
	public static final String PROPERTY_NHMRC_ID = "nhmrc_id";
	public static final String PROPERTY_ISBN = "isbn";
	public static final String PROPERTY_ISSN = "issn";
	public static final String PROPERTY_INSPIRE_ID = "inspire_id";
	//public static final String PROPERTY_OAI = "oai";
	public static final String PROPERTY_AUTHORS = "authors";
	public static final String PROPERTY_PARTICIPANTS = "participants";
	public static final String PROPERTY_REFERENCED_BY = "referenced_by"; // temporary for Dryad
	public static final String PROPERTY_ORIGINAL_KEY = "original_key";
	public static final String PROPERTY_DARA_ID = "dara_id";
	public static final String PROPERTY_TITLE_LANGUAGE = "title_language";
	public static final String PROPERTY_VERSION = "version";
	public static final String PROPERTY_LAST_UPDATED = "last_updated";
	public static final String PROPERTY_FUNDER = "funder";
	public static final String PROPERTY_START_YEAR = "start_year";
	public static final String PROPERTY_END_YEAR = "end_year";
	public static final String PROPERTY_PUBLICATION_YEAR = "publication_year";
	public static final String PROPERTY_LICENSE = "license";
	public static final String PROPERTY_MEGABYTE = "megabyte";
	
	// control properties
	public static final String PROPERTY_DELETED = "deleted";
	public static final String PROPERTY_BROKEN = "broken";
	
	// meta-data sources
	public static final String SOURCE_SYSTEM = "system";
	public static final String SOURCE_ANDS = "ands";
	public static final String SOURCE_ARC = "arc";
	public static final String SOURCE_NHMRC = "nhmrc";
	public static final String SOURCE_WEB = "web";
	public static final String SOURCE_ORCID = "orcid";
	public static final String SOURCE_DRYAD = "dryad";
	public static final String SOURCE_CROSSREF = "crossref";
	public static final String SOURCE_FIGSHARE = "figshare";
	public static final String SOURCE_CERN = "cern";
	public static final String SOURCE_DLI = "dli";
	public static final String SOURCE_DARA = "dara";
	
	// meta-data types
	public static final String TYPE_DATASET = "dataset";
	public static final String TYPE_GRANT = "grant";
	public static final String TYPE_RESEARCHER = "researcher";
	public static final String TYPE_INSTITUTION = "institution";
	public static final String TYPE_SERVICE = "service";
	public static final String TYPE_PUBLICATION = "publication";
	public static final String TYPE_PATTERN = "pattern";
	public static final String TYPE_VERSION = "version";
	
	// relationships
	
	public static final String RELATIONSHIP_RELATED_TO = "relatedTo";
	public static final String RELATIONSHIP_KNOWN_AS = "knownAs";
	public static final String RELATIONSHIP_AUTHOR = "author";
	public static final String RELATIONSHIP_PATTERN = "pattern";
	public static final String RELATIONSHIP_ADMINISTRATOR = "administrator";
	public static final String RELATIONSHIP_INVESTIGATOR = "investigator";
	
	public static final String SCOPUS_PARTNER_ID = "MN8TOARS";
	
	private static final String URL_REGEX = "^((https?|ftp)://|(www|ftp)\\.)?[a-z0-9-]+(\\.[a-z0-9-]+)+([/?].*)?$";
	//private static final String DOI_REGEX = "\\d{2,}(\\.\\d{4,})?/.+$";
	private static final String DOI_REGEX = "\\d+(\\.\\d+)*/.+$";
	private static final String ORCID_REGEX = "\\d{4}-\\d{4}-\\d{4}-\\d{3}(\\d|X)";
	private static final String SCOPUS_AUTHOR_REGEX = "author[iI][dD]=\\d+";
	private static final String SCOPUS_PARTNER_REGEX = "partner[iI][dD]=[A-Z0-9]+";
	private static final String SCOPUS_EID_REGEX = "eid=[a-z0-9\\-\\.]+";
	//private static final String SCOPUS_RECORD_REGEX = "scopus\\.com/inward/record\\.url?.*eid=[a-z0-9\\-\\.]+";
	
    private static final String PART_PROTOCOL = "://";
    private static final String PART_PROTOCOL_HTTP ="http://";
    private static final String PART_SLASH = "/";
    private static final String PART_EQUALS = "=";
    private static final String PART_WWW = "www.";
    private static final String PART_WWW3 = "www3.";
    private static final String PART_WEB = "web.";
    private static final String PART_ORCID_URI = "orcid.org/";
    private static final String PART_DOI_PERFIX = "doi:";
    private static final String PART_DOI_URI = "dx.doi.org/";
    private static final String PART_SCOPUS_URL = "www.scopus.com/inward/authorDetails.url?authorID=%s&partnerID=%s";
    private static final String PART_SCOPUS_EID_URL = "www.scopus.com/inward/record.url?eid=%s&partnerID=%s";
    private static final String PART_ARC_PURL = "purl.org/au-research/grants/arc/";
    private static final String PART_NHMRC_PURL = "purl.org/au-research/grants/nhmrc/";
    private static final String PART_INSPIRE_URL = "inspirehep.net/record/";
    private static final String PART_ANDS_URL = "https://researchdata.ands.org.au/view/?key=";
    
    private static final Pattern patternUrl = Pattern.compile(URL_REGEX);
    private static final Pattern patternDoi = Pattern.compile(DOI_REGEX);
    private static final Pattern patternOrcid = Pattern.compile(ORCID_REGEX);
    private static final Pattern patternScopusAuthor = Pattern.compile(SCOPUS_AUTHOR_REGEX);
    private static final Pattern patternScopusPartner = Pattern.compile(SCOPUS_PARTNER_REGEX);
    private static final Pattern patternScopusEID = Pattern.compile(SCOPUS_EID_REGEX);
   // private static final Pattern patternRecord = Pattern.compile(SCOPUS_RECORD_REGEX);
    
    /**
     * Unsafe function to convert string to URL. If string does not starts with protocol, it will attach HTTP protocol to it.

     * If string is impossible to convert to URL, the MalformedURLException will be throwed. 
     * 
     * This function should be use for a debug purpose only, for a prodaction mode please condider to use toURL what will not throw any exception
     * 
     * @param str String containing URL
     * @return constructed URL object or null if String is empty
     * @throws MalformedURLException if URL is mailformed
     */
    
    public static URL toURLUnsafe(String str) throws MalformedURLException {
		if (null == str)
			return null;
		
		str = str.trim();
		if (str.isEmpty())
			return null;
		
		return new URL(str.indexOf( PART_PROTOCOL ) >= 0 ? str : PART_PROTOCOL_HTTP + str);
}
    
    /**
     * Function to convert string to URL. If string does not starts with protocol, it will attach HTTP protocol to it.
     * 
     * If String is impossible to convert to URL, the null will be returned
     * 
     * @param str String containing URL
     * @return constructed URL object or null if URL can not be constructed
     */
    
    public static URL toURL(String str) {
    	try {
    		if (null == str)
    			return null;
    		
    		str = str.trim();
    		if (str.isEmpty())
    			return null;
    		
    		return new URL(str.indexOf( PART_PROTOCOL ) >= 0 ? str : PART_PROTOCOL_HTTP + str);
    	} catch (MalformedURLException e) {
			return null;
		}
    }
    
	/**
	 * Function will extract an URL from a given string by using regualr expression URL_REGEX
	 * @param str Any String
	 * @return String, containing URL or null if URL can not be extracted
	 */
	public static String extractUrl(String str) {
		if (StringUtils.isNotEmpty(str)) {
    		Matcher matcher = patternUrl.matcher(str);
			if (matcher.find())
				return matcher.group();
    	}
    	
    	return null;
	}
	
	/**
	 * Function to extract host name from URL object. The parts www, www3 and web will be cut off from the beginning of the host name 
	 * @param url URL object
	 * @return String containing host name or null if host can not be extracted
	 */
	
	public static String extractHost(URL url) {
		// extract host name
		String host = url.getHost();
		
		// cut of www. from host name
		if (host.startsWith(PART_WWW))
			host = host.substring(PART_WWW.length());
		if (host.startsWith(PART_WWW3))
			host = host.substring(PART_WWW3.length());
		if (host.startsWith(PART_WEB))
			host = host.substring(PART_WEB.length());
		
		return host;
	}
	
	/**
	 * Function to extract host name from a given string containing an URL
	 * @param str Stirng containing URL
	 * @return String containing host name or null if host can not be extracted
	 */
	
	public static String extractHost(String str) {
		URL url = toURL(str);
		return null == url ? null : extractHost(url);
	}

	/**
	 * Function to extract formalized url from a given URL object
	 * Formalized URL does not contains a protocol, www, www3 or web parts of host name and a termintaing slash.
	 * @param url URL object
	 * @return String containing formalized URL or null if URL can not be extracted
	 */
	
	public static String extractFormalizedUrl(URL url){
		// extract host name
		String host = extractHost(url);
		// extract file name
		String file = url.getFile();
		
		// cut of terminating slash from a file name
		if (file.endsWith(PART_SLASH))
			file = file.substring(0, file.length()-1);
		
		// return extracted url
		return host + file;
    }
	
	/**
	 * Function to extract formalized url from a given string containing an URL
	 * @param str String containing URL
	 * @return String containing formalized URL or null if URL can not be extracted
	 */
	public static String extractFormalizedUrl(String str) {
		URL url = toURL(str);
		return null == url ? null : extractFormalizedUrl(url);
    }
	
	/**
	 * Function to extractORCID Id
	 * @param str String containing URL or ORCID ID
	 * @return String containing ORCID ID or null if ORCID ID can not be extracted
	 */
	public static String extractOrcidId(String str) {
    	if (StringUtils.isNotEmpty(str)) {
    		Matcher matcher = patternOrcid.matcher(str);
			if (matcher.find())
				return matcher.group();
    	}
    	
    	return null;
	}
	
	/**
	 * Function to extract DOI
	 * @param str String containing DOI or DOI URL
	 * @return String containing DOI or null if DOI can not be extracted
	 */
	public static String extractDoi(String str) {
		if (StringUtils.isNotEmpty(str)) {
			int pos = str.indexOf(PART_DOI_PERFIX);
			if (pos >= 0) 
				str = str.substring(pos + PART_DOI_PERFIX.length());
			
			pos = str.indexOf(PART_DOI_URI);
			if (pos >= 0) 
				str = str.substring(pos + PART_DOI_URI.length());
			
    		Matcher matcher = patternDoi.matcher(str);
    		if (matcher.find()) 
    			return matcher.group();
    	}
    	
		return null;
	}
		
	/**
	 * Function to extract Scopus Author ID fom Scopus URL
	 * @param str String 
	 * @return Scopus Author ID or null if none
	 */
	public static String extractScopusAuthorId(String str) {
    	if (StringUtils.isNotEmpty(str)) {
    		Matcher matcher = patternScopusAuthor.matcher(str);
    		if (matcher.find()) {
    			String scopus =  matcher.group();
    			int pos = scopus.indexOf(PART_EQUALS);
    			if (pos >= 0) {
    				scopus = scopus.substring(pos + PART_EQUALS.length());
    				if (!scopus.isEmpty())
    					return scopus;
    			}
    		}
    	}
    	
		return null;
	}
	
	/**
	 Function to extract Scopus Parnter ID fom Scopus URL
	 * @param str String 
	 * @return Scopus Parnter ID or null if none
	 */
	public static String extractScopusPartnerId(String str) {
    	if (StringUtils.isNotEmpty(str)) {
    		Matcher matcher = patternScopusPartner.matcher(str);
    		if (matcher.find()) {
    			String scopus =  matcher.group();
    			int pos = scopus.indexOf(PART_EQUALS);
    			if (pos >= 0) {
    				scopus = scopus.substring(pos + PART_EQUALS.length());
    				if (!scopus.isEmpty())
    					return scopus;
    			}
    		}
    	}
    	
		return null;
	}
	
	
	/**
	 * Function to extract Scopus EID fom Scopus URL
	 * @param str String 
	 * @return Scopus EID or null if none
	 */
	public static String extractScopusEID(String str) {
    	if (StringUtils.isNotEmpty(str)) {
    		Matcher matcher = patternScopusEID.matcher(str);
    		if (matcher.find()) {
    			String scopus =  matcher.group();
    			int pos = scopus.indexOf(PART_EQUALS);
    			if (pos >= 0) {
    				scopus = scopus.substring(pos + PART_EQUALS.length());
    				if (!scopus.isEmpty())
    					return scopus;
    			}
    		}
    	}
    	
		return null;
	}

	/**
	 * Function to generate ORCID URI
	 * @param orcid String
	 * @return ORCID URL
	 */
	public static String generateOrcidUri(String orcid) {
    	return StringUtils.isEmpty(orcid) ? null : (PART_ORCID_URI + orcid);
    }

	/**
	 * Function to generate DOI URI
	 * @param doi String
	 * @return DOI URL
	 */
    public static String generateDoiUri(String doi) {
    	return StringUtils.isEmpty(doi) ? null : (PART_DOI_URI + doi);
    }
    
    /**
     * Function to generate Scopus URI
     * @param authorId String
     * @param partnerId String
     * @return Scopus URI
     */

    public static String generateScopusUri(String authorId, String partnerId) {
    	return (StringUtils.isEmpty(authorId) || StringUtils.isEmpty(partnerId)) ? null : String.format(PART_SCOPUS_URL, authorId, partnerId);
    }
    
    /**
     * Function to generate Scopus URI
     * @param authorId String 
     * @return Scopus URI with default partner id (SCOPUS_PARTNER_ID)
     */
    public static String generateScopusUri(String authorId) {
    	return generateScopusUri(authorId, SCOPUS_PARTNER_ID);
    }
    
    /**
     * Function to generate Scopus EID URI
     * @param eId
     * @param partnerId
     * @return Scopus EID URI
     */
    public static String generateScopusEidUri(String eId, String partnerId) {
    	return (StringUtils.isEmpty(eId) || StringUtils.isEmpty(partnerId)) ? null : String.format(PART_SCOPUS_EID_URL, eId, partnerId);
    }
    
    /**
     * Function to generate Scopus EID URI
     * @param eId String 
     * @return Scopus EID URI with default partner id (SCOPUS_PARTNER_ID)
     */
    public static String generateScopusEidUri(String eId) {
    	return generateScopusEidUri(eId, SCOPUS_PARTNER_ID);
    }
    
    /**
     * Function to generate ARC Grant Purl
     * @param arcId
     * @return arc purl
     */
    
    public static String generateArcGrantPurl(String arcId) {
    	return StringUtils.isEmpty(arcId) ? null : (PART_ARC_PURL + arcId);
    }
    
    /**
     * Function to generate NHMRC Grant Purl
     * @param arcId
     * @return NHMRC Grant purl
     */
    
    public static String generateNhmrcGrantPurl(String nhmrcId) {
    	return StringUtils.isEmpty(nhmrcId) ? null : (PART_NHMRC_PURL + nhmrcId);
    }

    /**
     * Function to generate NHMRC Grant Purl
     * @param arcId
     * @return NHMRC Grant purl
     */

    public static String generateInspireUrl(String inspireId) {
    	return StringUtils.isEmpty(inspireId) ? null : (PART_INSPIRE_URL + inspireId);
    }
    
    public static String generateAndsUrl(String key) throws UnsupportedEncodingException {
    	return StringUtils.isEmpty(key) ? null : (PART_ANDS_URL + URLEncoder.encode(key, StandardCharsets.UTF_8.name()));
    }
    
    
    /*public static boolean isScopusRecordURL(String str) {
    	if (StringUtils.isNotEmpty(str)) {
    		Matcher matcher = patternRecord.matcher(str);
			return matcher.find();
    	}
    	
    	return false;
    }*/
}
