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

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.rdswitchboard.utils.google.cache2.GoogleUtils;
import org.rdswitchboard.utils.google.cache2.Link;

public abstract class AbstractMatcher implements Matcher {
	private String cacheFolder;
	private Link link;
	private Map<String, MatcherNodes> nodes;
	private Map<String, MatcherCache> cache;
	private MatcherResult result = null;
	private JAXBContext jaxbContext;
	private boolean cacheUpdated = false;
	
	public JAXBContext getContext() {
		return jaxbContext;
	}

	public void setContext(JAXBContext jaxbContext) {
		this.jaxbContext = jaxbContext;
	}

	public String getCacheFolder() {
		return cacheFolder;
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
	
	public MatcherResult getResult() {
		return result;
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
		return this.cache.get(title);
	}
	
	protected void addCahce(String title, int level, boolean found) {
		cache.put(title, new MatcherCache(level, found));
		cacheUpdated = true;
	}
	
	protected File getCacheFile() {
		return new File(GoogleUtils.getSearchCacheFolder(cacheFolder), new File(link.getData()).getName() + ".xml");
	}
	
	protected File getDataFile() {
		return new File(GoogleUtils.getDataFolder(cacheFolder), link.getData());
	}
	
	
	
	@Override
	public String toString() {
		return "AbstractMatcher [cacheFolder=" + cacheFolder + ", link=" + link + ", nodes=" + nodes + ", cache="
				+ cache + ", result=" + result + "]";
	}
}
