package org.rdswitchboard.utils.google.cache2;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.io.FileUtils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class GoogleUtils {
	public static final String FOLDER_JSON = "json";
	public static final String FOLDER_RESULT = "result";
	public static final String FOLDER_CACHE = "cache";
	public static final String FOLDER_SCACHE = "scache";
	public static final String FOLDER_LINK = "link";
	public static final String FOLDER_BROKEN_LINK = "broken_link";
	public static final String FOLDER_DATA = "data";
	public static final String FOLDER_METADATA = "metadata";
	//public static final String FOLDER_GRANT = "grant";
	//public static final String FOLDER_PUBLICATION = "publication";
	
	public static final ObjectMapper mapper = new ObjectMapper();
	public static final TypeReference<Map<String, Object>> refPagemap = new TypeReference<Map<String, Object>>() {};

	public static final String METDATA_DC_TITLE = "dc.title";
	
	public static JAXBContext jaxbContext = null;
	public static Marshaller jaxbMarshaller = null;
	public static Unmarshaller jaxbUnmarshaller = null;
	
	static {
		try {
			jaxbContext = JAXBContext.newInstance(Link.class, Result.class);
			jaxbMarshaller = jaxbContext.createMarshaller();
			jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	 * Function to return metatag information from pagemap
	 * @param pagemap Map<String, Object>
	 * @param field field name (dc.title for dublin core title)
	 * @return metatag - String
	 */
	public static String getMetatag(Map<String, Object> pagemap, String field) { 
		if (null != pagemap) {
			 @SuppressWarnings("unchecked")
			 List<Object> metatags = (List<Object>) pagemap.get("metatags");
			 if (null != metatags && metatags.size() > 0) {
				 @SuppressWarnings("unchecked")
				 Map<String, Object> metatag = (Map<String, Object>) metatags.get(0);
				 if (null != metatag) {
					 return (String) metatag.get(field);
				 }
			 }
		}
		
		return null;
	}
	
	/**
	 * Function to parse pagemap into Java Map
	 * @param filePagemap
	 * @return pagemap - Map<String, Object>
	 */
	public static Map<String, Object> getPagemap(File filePagemap) {
		try {
			return mapper.readValue(filePagemap, refPagemap);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	

	
	/**
	 * Function to return metatag information from a file
	 * @param filePagemap a file, containing pagemap metadata
	 * @param field field name (dc.title for dublin core title)
	 * @return metatag - String
	 */
	public static String getMetatag(File filePagemap, String field) { 
		return getMetatag(getPagemap(filePagemap), field);
	}

	public static final File getCacheFolder(File folder, String targetFolder) {
		return new File(folder, FOLDER_CACHE + "/" + targetFolder);
	}

	public static final File getCacheFolder(String folder, String targetFolder) {
		return new File(folder, FOLDER_CACHE + "/" + targetFolder);
	}
	
	public static final File getJsonFolder(File folder) {
		return getCacheFolder(folder, FOLDER_JSON);
	}
		
	public static final File getLinkFolder(File folder) {
		return getCacheFolder(folder, FOLDER_LINK);
	}

	public static final File getDataFolder(File folder) {
		return getCacheFolder(folder, FOLDER_DATA);
	}

	public static final File getMetadataFolder(File folder) {
		return getCacheFolder(folder, FOLDER_METADATA);
	}
	
	public static final File getJsonFolder(String folder) {
		return getCacheFolder(folder, FOLDER_JSON);
	}
		
	public static final File getLinkFolder(String folder) {
		return getCacheFolder(folder, FOLDER_LINK);
	}

	public static final File getBrokenLinkFolder(String folder) {
		return getCacheFolder(folder, FOLDER_BROKEN_LINK);
	}

	public static final File getDataFolder(String folder) {
		return getCacheFolder(folder, FOLDER_DATA);
	}

	public static final File getSearchCacheFolder(String folder) {
		return getCacheFolder(folder, FOLDER_SCACHE);
	}
	
	public static final File getMetadataFolder(String folder) {
		return getCacheFolder(folder, FOLDER_METADATA);
	}
	
	public static File getResultFolder(String folder) {
		return getCacheFolder(folder, FOLDER_RESULT);
	}
	
	/*
	public static final File getGrantFolder(File folder) {
		return new File(folder, FOLDER_CACHE + "/" + FOLDER_GRANT);
	}
	
	public static final File getGrantPublication(File folder) {
		return new File(folder, FOLDER_CACHE + "/" + FOLDER_PUBLICATION);
	}*/	
	
	
	public static Set<String> loadBlackList(String blackListPath) throws IOException {
				
		Set<String> blackList = new HashSet<String>();
        
        try(BufferedReader br = new BufferedReader(new FileReader(new File(blackListPath)))) {
            for(String line; (line = br.readLine()) != null; ) {
            	String s = line.trim().toLowerCase();
            	
            	blackList.add(s);
            }            
        }
        
        return blackList;
    }	
	
	public static Map<String, Link> loadLinks(final File linksFolder, Map<String, Link> links) {
		if (null == links)
			links = new HashMap<String, Link>();
		
		File[] files = linksFolder.listFiles();
		for (File file : files) 
			if (!file.isDirectory())
			{
				try {
					Link link = (Link) jaxbUnmarshaller.unmarshal(file);
					if (link != null) 
						links.put(link.getLink(), link);
				} catch (JAXBException e) {
					e.printStackTrace();
				}
			}
		
		return links;
	}
	
	public static Map<String, Result> loadResuls(final File resultsFolder) {
		Map<String, Result> results = new HashMap<String, Result>();
		
		File[] files = resultsFolder.listFiles();
		for (File file : files) 
			if (!file.isDirectory())
			{
				try {
					Result result = (Result) jaxbUnmarshaller.unmarshal(file);
					if (result != null) 
						results.put(result.getText(), result);
				} catch (JAXBException e) {
					e.printStackTrace();
				}
			}
		
		return results;
	}

	public static String saveData(File folder, String data) {
		try {
			File file = File.createTempFile("data_", ".dat", folder);
			FileUtils.write(file, data);
		
			return file.getName();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	public static String savePagemap(File folder, Map<String, Object> pagemap) {
		try {
			File file = File.createTempFile("metadata_", ".json", folder);
			
			mapper.writeValue(file, pagemap);
			
			return file.getName();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	public static boolean saveLink(File folder, Link link) {
		try {
			File file = File.createTempFile("link_", ".xml", folder);
			link.setSelf(file.getName());

			jaxbMarshaller.marshal(link, file);
			
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return false;
	}
	
	public static boolean saveResult(File folder, Result result) {
		try {
			File file = File.createTempFile("result_", ".xml", folder);
			result.setSelf(file.getName());

			jaxbMarshaller.marshal(result, file);
			
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return false;
	}
}
