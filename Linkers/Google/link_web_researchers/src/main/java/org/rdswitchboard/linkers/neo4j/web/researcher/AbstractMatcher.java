package org.rdswitchboard.linkers.neo4j.web.researcher;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.io.FilenameUtils;
import org.rdswitchboard.utils.google.cache2.GoogleUtils;
import org.rdswitchboard.utils.google.cache2.Link;

public abstract class AbstractMatcher implements Matcher {
	private String cacheFolder = null;
	private Link link = null;
	private Map<String, MatcherNodes> nodes = null;
	private Map<String, MatcherCache> cache = null;
	private JAXBContext jaxbContext = null;
	private boolean cacheUpdated = false;

	public void setContext(JAXBContext jaxbContext) {
		this.jaxbContext = jaxbContext;
	}
	
	public AbstractMatcher withContext(JAXBContext jaxbContext) {
		setContext(jaxbContext);
		return this;
	}

	public void setCacheFolder(String cacheFolder) {
		this.cacheFolder = cacheFolder;
	}
	
	public AbstractMatcher withCacheFolder(String cacheFolder) {
		setCacheFolder(cacheFolder);
		return this;
	}
	
	public Link getLink() {
		return link;
	}
	
	public void setLink(Link link) {
		this.link = link;
	}
	
	public AbstractMatcher withLink(Link link) {
		setLink(link);
		return this;
	}	
	
	public Map<String, MatcherNodes> getNodes() {
		return nodes;
	}
	
	public void setNodes(Map<String, MatcherNodes> nodes) {
		this.nodes = nodes;
	}
	
	public AbstractMatcher withNodes(Map<String, MatcherNodes> nodes) {
		setNodes(nodes);
		return this;
	}
	
	
	protected void loadCache() throws JAXBException {
		Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		
		Cache cache = (Cache) jaxbUnmarshaller.unmarshal(getCacheFile());
		if (null != cache) {
			this.cache = new HashMap<String, MatcherCache>();
			
			for (Entry entry : cache.getEntries()) 
				this.cache.put(entry.getTitle(), 
						new MatcherCache(entry.getTime(), entry.getLevel(), entry.isFound()));
		}
		
		cacheUpdated = false; 
	}

	protected void saveCache() throws JAXBException {
		if (cacheUpdated) {
			List<Entry> entries = new ArrayList<Entry>();
			for (Map.Entry<String, MatcherCache> entry : this.cache.entrySet()) 
				entries.add(new Entry(entry.getKey(), 
						entry.getValue().getTimeString(), 
						entry.getValue().getLevel(), 
						entry.getValue().isFound()));
			
			Cache cache = new Cache();
			cache.setEntries(entries);
			
			Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
			
			jaxbMarshaller.marshal(cache, getCacheFile());
			
			cacheUpdated = false;
		}
	}
	
	protected MatcherCache getCahce(String title) {
		MatcherCache item = this.cache.get(title);
		if (null == item) {
			this.cache.put(title, item = new MatcherCache(0, false));
			
			cacheUpdated = true;
		}
		return item;		
	}
	
	public void updateCache() {
		cacheUpdated = true;
	}
	
	/*protected void addCahce(String title, int level, boolean found) throws MatcherThreadException {
		if (null != cache.put(title, new MatcherCache(level, found)))
			throw new MatcherThreadException("The cache already had this item");
		cacheUpdated = true;
	}*/
	
	protected File getCacheFile() {
		return new File(GoogleUtils.getSearchCacheFolder(cacheFolder), new File(FilenameUtils.removeExtension(link.getData()) + ".xml").getName());
	}
	
	protected File getDataFile() {
		return new File(GoogleUtils.getDataFolder(cacheFolder), link.getData());
	}
		
	@Override
	public String toString() {
		return "AbstractMatcher [cacheFolder=" + cacheFolder + ", link=" + link + ", nodes=" + nodes + ", cache="
				+ cache + "]";
	}
}
